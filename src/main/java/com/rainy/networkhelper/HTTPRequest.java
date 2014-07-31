/**
 * @author
 *
 * Class for parsing JSON strings. 
 */

package com.rainy.networkhelper;

import android.content.ComponentName;
import android.content.Intent;

import com.rainy.networkhelper.HTTPManager.HttpMethod;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class HTTPRequest implements Serializable
{
	private HttpMethod httpMethod;
	private String url;
	private HashMap<String, String> postValues;
	private HashMap<String, String> headerValues;
	private long timestamp;
	private String tag;
	private boolean cacheRequest = false;
	private boolean sending = false;
	private transient Intent beforeSendingIntent = null;

	public HTTPRequest()
	{
	}

	public HTTPRequest(HttpMethod httpMethod, String url)
	{
		this.httpMethod = httpMethod;
		this.url = url;
		this.timestamp = System.currentTimeMillis();
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException, NoSuchMethodException
	{
		out.defaultWriteObject();

		if (beforeSendingIntent != null)
		{
			String packageName = (beforeSendingIntent.getComponent() != null && beforeSendingIntent.getComponent().getPackageName() != null ?beforeSendingIntent.getComponent().getPackageName() : "");
			String className = (beforeSendingIntent.getComponent() != null && beforeSendingIntent.getComponent().getClassName() != null ? beforeSendingIntent.getComponent().getClassName() : "");
			String action = (beforeSendingIntent.getAction() != null ? beforeSendingIntent.getAction() : "");

			out.writeUTF(packageName);
			out.writeUTF(className);
			out.writeUTF(action);
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		try
		{
			String packageName = in.readUTF();
			String className = in.readUTF();
			String action = in.readUTF();

			beforeSendingIntent = new Intent();
			if (packageName != null && !packageName.equals("")) beforeSendingIntent.setComponent(new ComponentName(packageName, className));
			if (action != null && !action.equals("")) beforeSendingIntent.setAction(action);
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public HTTPRequest(HttpMethod httpMethod, String url, HashMap<String, String> postValues, HashMap<String, String> headerValues)
	{
		this(httpMethod, url);

		this.postValues = postValues;
		this.headerValues = headerValues;
	}

	public HTTPRequest(HttpMethod httpMethod, String url, HashMap<String, String> postValues, HashMap<String, String> headerValues, boolean cacheRequest)
	{
		this(httpMethod, url);

		this.postValues = postValues;
		this.headerValues = headerValues;
		this.cacheRequest = cacheRequest;
	}

	public HttpMethod getHttpMethod()
	{
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod)
	{
		this.httpMethod = httpMethod;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public HashMap<String, String> getPostValues()
	{
		return postValues;
	}

	public void setPostValues(HashMap<String, String> postValues)
	{
		this.postValues = postValues;
	}

	public HashMap<String, String> getHeaderValues()
	{
		return headerValues;
	}

	public void setHeaderValues(HashMap<String, String> headerValues)
	{
		this.headerValues = headerValues;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public boolean isCached()
	{
		return cacheRequest;
	}

	public void setCacheRequest(boolean cacheRequest)
	{
		this.cacheRequest = cacheRequest;
	}

	public void setSending(boolean sending)
	{
		this.sending = sending;
	}

	public boolean isSending()
	{
		return sending;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public Intent getBeforeSendingIntent()
	{
		return beforeSendingIntent;
	}

	/**
	 * If set and the request is cached, the intent will be executed just before sending the request. Always pass a BroadcastReceiver to the intent. This way you can modify your request before actually sending out to the server.
	 * @param beforeSendingIntent Only the action paramtere of the Intent is going to be relevant.
	 */
	public void setBeforeSendingIntent(Intent beforeSendingIntent)
	{
		this.beforeSendingIntent = beforeSendingIntent;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (!(other instanceof HTTPRequest))
		{
			return false;
		}

		HTTPRequest otherRequest = (HTTPRequest) other;
		if (otherRequest.getHttpMethod().equals(httpMethod) && otherRequest.getUrl().equals(url) && otherRequest.getPostValues().equals(postValues))
		{
			return true;
		}

		return false;
	}
}