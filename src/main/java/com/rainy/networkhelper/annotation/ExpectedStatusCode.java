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

/**
 * Annotation that can be used on a @{link BaseRequest} class.
 * The given values determine whether the response's status code meets our expectation.
 * By default, if no status code expectation is set, it uses Volley's default values (200-299).
 *
 * Note: only 200-299 values can be given here.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExpectedStatusCode {
    int[] values() default 0;
}