package com.rainy.networkhelper.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.rainy.networkhelper.annotation.Header;
import com.rainy.networkhelper.mapper.BodyMapper;
import com.rainy.networkhelper.mapper.GsonBodyMapper;
import com.rainy.networkhelper.util.ReflectionUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class BasicRequest<T> extends Request<T>
{
	public final String HEADER_CONTENT_TYPE = "Content-Type";

	private Map<String, String> headers = new HashMap<>();
	private Object requestDto = null;
	private Response.Listener<T> listener;
	private static RequestQueue queue;
	private int connectionType = -1;
	private BodyMapper bodyEncoder = new GsonBodyMapper();
	private BodyMapper responseDecoder = new GsonBodyMapper();

	public BasicRequest(int httpMethod, String url, Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(httpMethod, url, errorListener);

		this.listener = listener;
	}

	public BasicRequest(int httpMethod, String url, Map<String, String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(httpMethod, url, errorListener);

		this.listener = listener;
		this.headers = headers;
	}

	public BasicRequest(int httpMethod, String url, Object requestDto, Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(httpMethod, url, errorListener);

		this.requestDto = requestDto;
		this.listener = listener;
		setRequestDto(requestDto);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError
	{
		return this.headers != null ? this.headers : super.getHeaders();
	}

	@Override
	public String getBodyContentType()
	{
		return (this.headers != null && this.headers.containsKey(HEADER_CONTENT_TYPE) ? this.headers.get(HEADER_CONTENT_TYPE) : bodyEncoder.getContentType());
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError
	{
		return null;
	}

	@Override
	public byte[] getBody() throws AuthFailureError
	{
		if (requestDto != null)
		{
			try
			{
				return bodyEncoder.encodeParams(requestDto, getParamsEncoding());
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	public void sendRequest(Context context)
	{
		if (!isConnectionAvailable(context, connectionType))
		{
			deliverError(new NoConnectionError());
			return;
		}

		if (queue == null)
		{
			queue = Volley.newRequestQueue(context.getApplicationContext());
		}

		queue.add(this);
	}

	/**
	 * @param context context
	 * @param type    One of ConnectivityManager.TYPE_ constants.
	 * @return Returns whether the specified connection is active.
	 */
	public static final boolean isConnectionAvailable(Context context, int type)
	{
		if (type == -1)
			return true;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null)
		{
			networkInfo = connectivityManager.getNetworkInfo(type);
		}

		return networkInfo != null && networkInfo.isConnected();
	}

	public int getConnectionType()
	{
		return connectionType;
	}

	public void setConnectionType(int connectionType)
	{
		this.connectionType = connectionType;
	}

	public Object getRequestDto()
	{
		return requestDto;
	}

	public void setRequestDto(Object requestDto)
	{
		this.requestDto = requestDto;

		if (this.requestDto != null)
		{
			for (Field field : ReflectionUtil.getFieldsHavingAnnotation(requestDto.getClass(), Header.class))
			{
				field.setAccessible(true);

				Header headerAnnotation = field.getAnnotation(Header.class);
				if (headerAnnotation != null)
				{
					String fieldName = field.getName();
					if (!headerAnnotation.name().equals(""))
						fieldName = headerAnnotation.name();

					String value = ReflectionUtil.getFieldValue(field, requestDto);

					if (value != null)
						this.headers.put(fieldName, value);
				}
			}
		}
	}

	public BodyMapper getBodyEncoder()
	{
		return bodyEncoder;
	}

	public BasicRequest setBodyEncoder(BodyMapper bodyEncoder)
	{
		this.bodyEncoder = bodyEncoder;
		return this;
	}

	public BodyMapper getResponseDecoder()
	{
		return responseDecoder;
	}

	public BasicRequest setResponseDecoder(BodyMapper responseDecoder)
	{
		this.responseDecoder = responseDecoder;
		return this;
	}

	@Override
	protected void deliverResponse(T basicResponseDto)
	{
		if (listener != null)
		{
			listener.onResponse(basicResponseDto);
		}
	}
}
