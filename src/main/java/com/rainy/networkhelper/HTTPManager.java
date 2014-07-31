/**
 * @author Gabor Szanto, 2014
 *
 * Class for handling HTTP communication. 
 */

package com.rainy.networkhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.rainy.networkhelper.exception.NoInternetException;
import com.rainy.networkhelper.util.SerializationUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPManager
{
	private static final String PREFERENCES_NAME = "NetworkHelperCache";
	private static final String PREFERENCES_KEY_CACHE = "HTTPRequestCache";

	private static final Handler mHandler = new Handler();
	private static int cacheTimeToLive = 24 * 60 * 60 * 1000;
	private static boolean cacheEnabled = false;
	private static int cacheConnectionType = -1;

	public interface HTTPManagerListener
	{
		public void onResponseSuccess(String response, int statusCode);

		public void onResponseFail(Exception ex);
	}

	public enum HttpMethod
	{
		GET, POST, PUT, DELETE, HEAD, OPTIONS
	}

	private static HttpResponse startHTTPRequest(Context context, HTTPRequest request) throws ClientProtocolException, IOException, NoInternetException
	{
		if (!isNetworkConnectionAvailable(context))
		{
			throw new NoInternetException();
		}

		HttpClient client = SSLHttpFactory.createHttpsClient(); // new
		// DefaultHttpClient()

		HttpResponse response = null;
		HttpRequestBase httpRequest = null;

		if (request.getHttpMethod() == HttpMethod.GET)
		{
			httpRequest = new HttpGet(request.getUrl());
		} else if (request.getHttpMethod() == HttpMethod.POST)
		{
			httpRequest = new HttpPost(request.getUrl());
			if (request.getPostValues() != null)
			{
				((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(convertMapToList(request.getPostValues()), HTTP.UTF_8));
			}
		} else if (request.getHttpMethod() == HttpMethod.DELETE)
		{
			httpRequest = new HttpDelete(request.getUrl());
		} else if (request.getHttpMethod() == HttpMethod.PUT)
		{
			httpRequest = new HttpPut(request.getUrl());
			if (request.getPostValues() != null)
			{
				((HttpPut) httpRequest).setEntity(new UrlEncodedFormEntity(convertMapToList(request.getPostValues()), HTTP.UTF_8));
			}
		} else if (request.getHttpMethod() == HttpMethod.OPTIONS)
		{
			httpRequest = new HttpOptions(request.getUrl());
		} else if (request.getHttpMethod() == HttpMethod.HEAD)
		{
			httpRequest = new HttpHead(request.getUrl());
		}

		if (httpRequest != null)
		{
			if (request.getHeaderValues() != null)
			{
				for (Map.Entry<String, String> entry : request.getHeaderValues().entrySet())
				{
					httpRequest.addHeader(entry.getKey(), entry.getValue());
				}
			}

			response = client.execute(httpRequest);
		}

		return response;
	}

	public static final boolean isNetworkConnectionAvailable(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

		if (activeNetwork == null || !activeNetwork.isConnected())
		{
			return false;
		}

		return true;
	}

	public static final boolean isWiFiAvailable(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

		if (activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
		{
			return true;
		}

		return false;
	}

	/**
	 * Sends an HTTP request on a background thread. The callback listener will
	 * be called on the main thread, so you don't need to handle threading.
	 *
	 * @param context   The context.
	 * @param request   The HTTPRequest object that represents the actual request.
	 * @param _listener The callback listener that will be called on the main thread
	 *                  when the response arrives.
	 */
	public static void sendRequest(final Context context, final HTTPRequest request, final HTTPManagerListener _listener)
	{
		// final String request = (!_url.startsWith("http://") ? Constants.HOST
		// + _url : _url);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final HttpResponse response = startHTTPRequest(context, request);

					BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					final StringBuilder builder = new StringBuilder();
					for (String line = null; (line = reader.readLine()) != null; )
					{
						builder.append(line).append("\n");
					}

					if (_listener != null)
					{
						mHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								_listener.onResponseSuccess(builder.toString(), response.getStatusLine().getStatusCode());
							}
						});
					}

				} catch (final Exception ex)
				{
					if (request.isCached())
					{
						addRequestToCache(context, request);
					}

					if (_listener != null)
					{
						mHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								_listener.onResponseFail(ex);
							}
						});
					}
				}
			}

		}).start();
	}

	/**
	 * This method only works on Android 4.0+. It enables / disables the caching
	 * of HTTP requests.
	 *
	 * @param enableCache
	 */
	public static void setEnableCaching(Context context, boolean enableCache)
	{
		cacheEnabled = enableCache;

		if (cacheEnabled)
		{
			try
			{
				long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
				File httpCacheDir = new File(context.getCacheDir(), "http");
				Class.forName("android.net.http.HttpResponseCache").getMethod("install", File.class, long.class).invoke(null, httpCacheDir, httpCacheSize);
			} catch (Exception httpResponseCacheNotAvailable)
			{
				httpResponseCacheNotAvailable.printStackTrace();
			}
		}
	}

	/**
	 * Sets the default time to live for caching. The default is 24 hours.
	 *
	 * @param timeToLive Time in milliseconds.
	 */
	public static void setCacheTimeToLive(int timeToLive)
	{
		cacheTimeToLive = timeToLive;
	}

	public static int getCacheTimeToLive()
	{
		return cacheTimeToLive;
	}

	/**
	 * Sets the cache connection type. One of ConnectivityManager.TYPE_WIFI, MOBILE, etc.. Pass -1 if all types are allowed.
	 *
	 * @param cacheConnectionType
	 */
	public static void setCacheConnectionType(int cacheConnectionType)
	{
		HTTPManager.cacheConnectionType = cacheConnectionType;
	}

	public static int getCacheConnectionType()
	{
		return cacheConnectionType;
	}

	public static List<HTTPRequest> getCachedRequests(Context context)
	{
		SharedPreferences prefs = getSharedPrefs(context);
		String serialized = prefs.getString(PREFERENCES_KEY_CACHE, null);

		if (serialized == null)
		{
			return new ArrayList<HTTPRequest>();
		}

		return (List<HTTPRequest>) SerializationUtil.deserialize(serialized);
	}

	public static void addRequestToCache(Context context, HTTPRequest request)
	{
		List<HTTPRequest> cachedHttpRequests = getCachedRequests(context);
		if (cachedHttpRequests == null || cachedHttpRequests.contains(request))
		{
			return;
		}

		cachedHttpRequests.add(request);

		setCachedRequests(context, cachedHttpRequests);
	}

	public static void setCachedRequests(Context context, List<HTTPRequest> cachedHttpRequests)
	{
		SharedPreferences prefs = getSharedPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();

		String serialized = SerializationUtil.serialize(cachedHttpRequests);
		if (serialized != null)
		{
			editor.putString(PREFERENCES_KEY_CACHE, serialized).commit();
		}
	}

	/**
	 * Clears all the previously cached requests.
	 */
	public static void clearRequestCache(Context context)
	{
		SharedPreferences prefs = getSharedPrefs(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.remove(PREFERENCES_KEY_CACHE).commit();
	}

	private static SharedPreferences getSharedPrefs(Context context)
	{
		return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	private static List<NameValuePair> convertMapToList(Map<String, String> parameters)
	{
		List<NameValuePair> result = new ArrayList<NameValuePair>();

		for (Map.Entry<String, String> entry : parameters.entrySet())
		{
			result.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
		}

		return result;
	}

}