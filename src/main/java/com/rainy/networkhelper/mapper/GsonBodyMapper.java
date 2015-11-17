package com.rainy.networkhelper.mapper;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.rainy.networkhelper.annotation.Header;

/**
 * Created by szantogabor on 20/02/15.
 */
public class GsonBodyMapper implements BodyMapper
{
	protected GsonBuilder getGsonBuilder()
	{
		return new GsonBuilder().setExclusionStrategies(new ExcludeHeaderStrategy());
	}

	@Override
	public byte[] encodeParams(Object object, String encoding) throws Exception
	{
		return getGsonBuilder().create().toJson(object).getBytes(encoding);
	}

	@Override
	public <T> T decodeParams(byte[] data, Class<T> clazz, String encoding) throws Exception
	{
		String json = new String(data, encoding);
		return getGsonBuilder().create().fromJson(json, clazz);
	}

	public class ExcludeHeaderStrategy implements ExclusionStrategy
	{
		public boolean shouldSkipClass(Class<?> clazz)
		{
			return false;
		}

		public boolean shouldSkipField(FieldAttributes f)
		{
			return f.getAnnotation(Header.class) != null;
		}
	}

	@Override
	public String getContentType()
	{
		return CONTENT_TYPE_JSON;
	}
}