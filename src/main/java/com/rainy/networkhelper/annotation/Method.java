/**
 * @author szantogabor
 * <p/>
 * Class for parsing JSON strings.
 */

package com.rainy.networkhelper.annotation;

import com.android.volley.Request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Method
{
	int method() default Request.Method.GET;
	String url();
}