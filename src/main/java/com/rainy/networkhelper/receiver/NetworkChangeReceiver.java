/**
 * @author
 *
 * Class for parsing JSON strings. 
 */

package com.rainy.networkhelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rainy.networkhelper.HTTPManager;
import com.rainy.networkhelper.HTTPManager.HTTPManagerListener;
import com.rainy.networkhelper.HTTPRequest;

import java.util.Iterator;
import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver
{
	public static final String ACTION_BEFORE_SEND_REQUEST = "com.rainy.networkhelper.ACTION_BEFORE_SEND_REQUEST";
	public static final String EXTRA_KEY_REQUEST = "EXTRA_KEY_REQUEST";

	@Override
	public void onReceive(final Context context, Intent intent)
	{
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

			if (activeNetwork != null && activeNetwork.isConnected() && (HTTPManager.getCacheConnectionType() == -1 || activeNetwork.getType() == HTTPManager.getCacheConnectionType()))
			{
				final List<HTTPRequest> cachedRequests = HTTPManager.getCachedRequests(context);
				if (cachedRequests == null || cachedRequests.size() == 0)
				{
					return;
				}

				Iterator<HTTPRequest> iterator = cachedRequests.iterator();
				while (iterator.hasNext())
				{
					final HTTPRequest httpRequest = iterator.next();

					if (httpRequest.isSending())
					{
						continue;
					}

					if (httpRequest.getBeforeSendingIntent() == null)
					{
						sendRequest(context, cachedRequests, httpRequest, iterator);
					} else
					{
						Intent i = httpRequest.getBeforeSendingIntent();
						i.putExtra(EXTRA_KEY_REQUEST, httpRequest);
						context.sendBroadcast(i);

						iterator.remove();
					}
				}

				HTTPManager.setCachedRequests(context, cachedRequests);
			}
		}
	}

	/**
	 * Sends the request.
	 *
	 * @param context        The context
	 * @param cachedRequests The list of the currently cached requests
	 * @param httpRequest    The HTTP request
	 * @param iterator       If specified and needed, the request will be deleted from the iterator's array. Can be null.
	 */
	private void sendRequest(final Context context, final List<HTTPRequest> cachedRequests, final HTTPRequest httpRequest, final Iterator<HTTPRequest> iterator)
	{
		if (httpRequest.getTimestamp() < System.currentTimeMillis() - HTTPManager.getCacheTimeToLive())
		{
			//cache expired
			if (iterator != null)
			{
				iterator.remove();
			}
			return;
		}

		httpRequest.setSending(true);

		HTTPManager.sendRequest(context, httpRequest, new HTTPManagerListener()
		{
			@Override
			public void onResponseSuccess(String response, int statusCode)
			{
				cachedRequests.remove(httpRequest);
				HTTPManager.setCachedRequests(context, cachedRequests);
			}

			@Override
			public void onResponseFail(Exception ex)
			{
				//request failed
				httpRequest.setSending(false);
			}
		});
	}
}