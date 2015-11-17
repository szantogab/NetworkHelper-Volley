package com.rainy.networkhelper.mapper;

/**
 * Created by szantogabor on 20/02/15.
 */
public interface BodyMapper
{
	public static final String CONTENT_TYPE_JSON = "application/json";
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	byte[] encodeParams(Object object, String encoding) throws Exception;
	<T> T decodeParams(byte[] data, Class<T> clazz, String encoding) throws Exception;
	String getContentType();
}