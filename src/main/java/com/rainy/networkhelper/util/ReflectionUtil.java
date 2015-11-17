package com.rainy.networkhelper.util;

import com.google.gson.annotations.SerializedName;
import com.rainy.networkhelper.annotation.Header;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for reading fields.
 *
 * @author Gabor Szanto
 */
public class ReflectionUtil
{
	public static List<Field> getInheritedPrivateFields(Class<?> type)
	{
		List<Field> result = new ArrayList<Field>();

		Class<?> i = type;
		while (i != null && i != Object.class)
		{
			for (Field field : i.getDeclaredFields())
				result.add(field);

			i = i.getSuperclass();
		}

		return result;
	}

	public static List<Field> getFieldsHavingAnnotation(Class<?> type, Class<? extends Annotation> annotation)
	{
		ArrayList fields = new ArrayList();
		for (Field field : getInheritedPrivateFields(type))
		{
			field.setAccessible(true);

			if (field.isAnnotationPresent(annotation))
				fields.add(field);
		}

		return fields;
	}

	/**
	 * Gets the value of a field, first running its getter, and if the getter cannot be found, it returns the field's direct value.
	 */
	public static String getFieldValue(Field field, Object o)
	{
		field.setAccessible(true);

		String value = runGetter(field, o);
		if (value == null)
			try
			{
				value = field.get(o).toString();
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}

		return value;
	}

	public static String runGetter(Field field, Object o)
	{
		// MZ: Find the correct method
		for (Method method : o.getClass().getMethods())
		{
			if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3)))
			{
				if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase()))
				{
					// MZ: Method found, run it
					try
					{
						return method.invoke(o).toString();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}


		return null;
	}

	public static Map<String, String> convertObjectToMap(Object object)
	{
		Map<String, String> map = new HashMap();

		List<Field> fieldList = ReflectionUtil.getInheritedPrivateFields(object.getClass());
		for (Field field : fieldList)
		{
			if (!field.isAnnotationPresent(Header.class))
			{
				String key = field.getName();
				SerializedName ann =  field.getAnnotation(SerializedName.class);
				if (ann != null)
				{
					key = ann.value();
				}

				String value = ReflectionUtil.getFieldValue(field, object);
				if (value != null)  map.put(key, value);
			}
		}

		return map;
	}
}
