/**
 * @author 
 * 
 * Class for parsing JSON strings. 
 */

package com.rainy.networkhelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils
{
	public static String getString(JSONObject json, String key)
	{
		try
		{
			String value = json.getString(key);
			if (value != null && value.equals("null"))
				return null;
			else
				return value;
		} catch (JSONException ex)
		{
			return null;
		}
	}

	public static JSONArray getJSONArray(JSONObject json, String key)
	{
		try
		{
			return json.getJSONArray(key);
		} catch (JSONException ex)
		{
			return null;
		}
	}

	public static JSONObject getJSONObject(JSONObject json, String key)
	{
		try
		{
			return json.getJSONObject(key);
		} catch (JSONException ex)
		{
			return null;
		}
	}
}