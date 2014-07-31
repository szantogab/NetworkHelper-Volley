/**
 * @author
 *
 * Class for parsing JSON strings. 
 */

package com.rainy.networkhelper;

import android.content.Context;

import com.rainy.networkhelper.HTTPManager.HTTPManagerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONParser
{
	public interface JSONParserListener<T>
	{
		public void onParsingFinished(T result, int statusCode);

		public void onParsingFailed(Exception ex);
	}

	/**
	 * Sends an HTTP request, and parses the JSON response.
	 *
	 * @param context  context
	 * @param request  The HTTPRequest object to send
	 * @param listener The callback listener that will be called when the parsing has
	 *                 been completed, or when the parsing fails.
	 * @throws JSONException If the parsing fails, JSONException is thrown.
	 */
	public static void parseUrl(final Context context, HTTPRequest request, final JSONParserListener<JSONObject> listener)
	{
		HTTPManager.sendRequest(context, request, new HTTPManagerListener()
		{
			@Override
			public void onResponseSuccess(String response, int statusCode)
			{
				try
				{
					JSONObject json = parseString(response, JSONObject.class);

					if (listener != null)
					{
						listener.onParsingFinished(json, statusCode);
					}
				} catch (JSONException ex)
				{
					if (listener != null)
					{
						listener.onParsingFailed(ex);
					}
				}
			}

			@Override
			public void onResponseFail(Exception ex)
			{
				if (listener != null)
				{
					listener.onParsingFailed(ex);
				}
			}
		});
	}

	/**
	 * Sends an HTTP request, and parses the JSON response array.
	 *
	 * @param context  context
	 * @param request  The HTTPRequest object to send
	 * @param listener The callback listener that will be called when the parsing has
	 *                 been completed, or when the parsing fails.
	 * @throws JSONException If the parsing fails, JSONException is thrown.
	 */
	public static void parseUrlArray(final Context context, HTTPRequest request, final JSONParserListener<ArrayList<JSONObject>> listener)
	{
		HTTPManager.sendRequest(context, request, new HTTPManagerListener()
		{
			@Override
			public void onResponseSuccess(String response, int statusCode)
			{
				try
				{
					@SuppressWarnings("unchecked") ArrayList<JSONObject> json = parseString(response, ArrayList.class);

					if (listener != null)
					{
						listener.onParsingFinished(json, statusCode);
					}
				} catch (JSONException ex)
				{
					if (listener != null)
					{
						listener.onParsingFailed(ex);
					}
				}
			}

			@Override
			public void onResponseFail(Exception ex)
			{
				if (listener != null)
				{
					listener.onParsingFailed(ex);
				}
			}
		});
	}

	/**
	 * Parses a JSON string.
	 *
	 * @param json The JSON string to parse.
	 * @param type The result object's type, i.e. JSONObject.class,
	 *             JSONArray.class, or ArrayList<JSONObject>.class
	 * @throws JSONException If the parsing fails (i.e. the JSON string cannot be casted
	 *                       to the desired type) JSONException is thrown.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseString(String json, Class<T> type) throws JSONException
	{
		if (type == JSONObject.class)
		{
			JSONObject jsonObject = new JSONObject(json);
			return (T) jsonObject;
		} else if (type == JSONArray.class)
		{
			JSONArray jsonArray = new JSONArray(json);
			return (T) jsonArray;
		} else if (type == ArrayList.class)
		{
			JSONArray jsonArray = new JSONArray(json);
			ArrayList<JSONObject> array = jsonToArray(jsonArray);

			return (T) array;
		}

		return null;
	}

	/**
	 * Helper method for converting JSONArray to ArrayList<JSONObject>
	 *
	 * @param the JSONArray
	 * @return the ArrayList<JSONObject>
	 */
	public static ArrayList<JSONObject> jsonToArray(JSONArray array)
	{
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		if (array != null)
		{
			int len = array.length();
			for (int i = 0; i < len; i++)
			{
				try
				{
					list.add((JSONObject) array.get(i));
				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		return list;
	}
}