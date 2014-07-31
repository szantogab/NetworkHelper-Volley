/**
 * @author Gabor Szanto, 2014
 * 
 * Class for handling HTTP communication. 
 */

package com.rainy.networkhelper;

import android.app.PendingIntent;

public abstract class HttpPendingIntent
{
	private PendingIntent pendingIntent;

	public HttpPendingIntent(PendingIntent pendingIntent)
	{
		this.pendingIntent = pendingIntent;
	}

	public PendingIntent getPendingIntent()
	{
		return pendingIntent;
	}

	public void setHTTPRequest(HTTPRequest request)
	{

	}
}