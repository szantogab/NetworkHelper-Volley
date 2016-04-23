package com.rainy.networkhelper.mapper;

import java.lang.reflect.Type;

/**
 * Created by szantogabor on 20/02/15.
 */
public interface BodyMapper
{
	String CONTENT_TYPE_JSON = "application/json";
	String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	byte[] encodeParams(Object object, String encoding) throws Exception;
	<T> T decodeParams(byte[] data, Class<T> clazz, String encoding) throws Exception;
	<T> T decodeParams(byte[] data, Type type, String encoding) throws Exception;

	String getContentType();
}