package com.rainy.networkhelper.request;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class ParserRequest<T> extends BasicRequest<T>
{
	private final Class<T> clazz;

	public ParserRequest(int httpMethod, String url, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(httpMethod, url, listener, errorListener);
		this.clazz = clazz;
	}

	public ParserRequest(int httpMethod, String url, Map<String, String> headers, Class<T> clazz, Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(httpMethod, url, headers, listener, errorListener);
		this.clazz = clazz;
	}

	public ParserRequest(int httpMethod, String url, Object requestDto, Class<T> responseClazz, Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(httpMethod, url, requestDto, listener, errorListener);
		this.clazz = responseClazz;
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response)
	{
		try
		{
			if (clazz == null || clazz == NetworkResponse.class)
			{
				return (Response<T>) Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
			}
			else
			{
				T parsed = getResponseDecoder().decodeParams(response.data, clazz, HttpHeaderParser.parseCharset(response.headers));
				return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
			}
		} catch (UnsupportedEncodingException e)
		{
			return Response.error(new ParseError(e));
		} catch (Exception e)
		{
			return Response.error(new ParseError(e));
		}
	}
}