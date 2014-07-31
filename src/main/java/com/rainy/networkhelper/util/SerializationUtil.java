package com.rainy.networkhelper.util;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A simple class with generic serialize and deserialize method implementations
 * 
 * @author Gabor Szanto
 * 
 */
public class SerializationUtil
{
	public static Object deserialize(String s)
	{
		try
		{
			byte[] data = Base64.decode(s, Base64.DEFAULT);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	// serialize the given object and save it to file
	public static String serialize(Object obj)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			return new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

}
