/**
 * @author szantogabor
 * <p/>
 * Class for parsing JSON strings.
 */

package com.rainy.networkhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Header
{
	String name() default "";
}