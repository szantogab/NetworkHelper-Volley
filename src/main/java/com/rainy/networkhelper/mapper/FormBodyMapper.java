package com.rainy.networkhelper.mapper;

import com.rainy.networkhelper.util.ReflectionUtil;

import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Created by szantogabor on 20/02/15.
 */
public class FormBodyMapper implements BodyMapper
{
	@Override
	public byte[] encodeParams(Object object, String encoding) throws Exception
	{
		StringBuilder encodedParams = new StringBuilder();

		Iterator uee = ReflectionUtil.convertObjectToMap(object).entrySet().iterator();

		while (uee.hasNext())
		{
			java.util.Map.Entry entry = (java.util.Map.Entry) uee.next();
			encodedParams.append(URLEncoder.encode((String) entry.getKey(), encoding));
			encodedParams.append('=');
			encodedParams.append(URLEncoder.encode(entry.getValue().toString(), encoding));
			encodedParams.append('&');
		}

		return encodedParams.toString().getBytes(encoding);
	}

	public <T> T decodeParams(byte[] data, Class<T> clazz, String encoding) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType()
	{
		return CONTENT_TYPE_FORM;
	}
}