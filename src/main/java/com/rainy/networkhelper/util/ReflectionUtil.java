package com.rainy.networkhelper.util;

import com.google.gson.annotations.SerializedName;
import com.rainy.networkhelper.annotation.HeaderParam;
import com.rainy.networkhelper.annotation.PathParam;
import com.rainy.networkhelper.annotation.QueryParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for reading fields.
 *
 * @author Gabor Szanto
 */
public class ReflectionUtil {
    public static List<Field> getInheritedPrivateFields(Class<?> type) {
        List<Field> result = new ArrayList<>();

        Class<?> i = type;
        while (i != null && i != Object.class) {
            for (Field field : i.getDeclaredFields())
                result.add(field);

            i = i.getSuperclass();
        }

        return result;
    }

    public static List<Field> getFieldsHavingAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : getInheritedPrivateFields(type)) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(annotation))
                fields.add(field);
        }

        return fields;
    }

    public static List<Field> getMethodsHavingAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
        ArrayList<Field> fields = new ArrayList<>();
        for (Field field : getInheritedPrivateFields(type)) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(annotation))
                fields.add(field);
        }

        return fields;
    }

    public static Annotation getClassAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
        for (Annotation ann : type.getDeclaredAnnotations()) {
            if (ann.annotationType().equals(annotation))
                return ann;
        }

        return null;
    }

    public static Map<Method, Annotation> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        final Map<Method, Annotation> methods = new HashMap<>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                method.setAccessible(true);

                if (method.isAnnotationPresent(annotation)) {
                    Annotation annotInstance = method.getAnnotation(annotation);
                    methods.put(method, annotInstance);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

    /**
     * Gets the value of a field, first running its getter, and if the getter cannot be found, it returns the field's direct value.
     */
    public static String getFieldValue(Field field, Object o) {
        field.setAccessible(true);

        String value = runGetter(field, o);
        if (value == null)
            try {
                Object obj = field.get(o);
                if (obj != null)
                    value = obj.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

        return value;
    }

    public static String runGetter(Field field, Object o) {
        // MZ: Find the correct method
        for (Method method : o.getClass().getMethods()) {
            method.setAccessible(true);

            if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))) {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                    // MZ: RequestMethod found, run it
                    return invokeMethod(method, o);
                }
            }
        }


        return null;
    }

    public static String invokeMethod(Method method, Object receiver) {
        try {
            Object o = method.invoke(receiver);
            if (o != null)
                return o.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Map<String, String> convertObjectToMap(Object object) {
        Map<String, String> map = new HashMap();

        List<Field> fieldList = ReflectionUtil.getInheritedPrivateFields(object.getClass());
        for (Field field : fieldList) {
            if (!field.isAnnotationPresent(HeaderParam.class)) {
                String key = field.getName();
                SerializedName ann = field.getAnnotation(SerializedName.class);
                if (ann != null) {
                    key = ann.value();
                }

                String value = ReflectionUtil.getFieldValue(field, object);
                if (value != null) map.put(key, value);
            }
        }

        return map;
    }

    public static Map<String, String> getMethodsAndFieldValuesAnnotatedWithHeaderParam(Object o)
    {
        Map<String, String> headers = new HashMap<>();
        for (Field field : ReflectionUtil.getFieldsHavingAnnotation(o.getClass(), HeaderParam.class)) {
            field.setAccessible(true);

            HeaderParam headerParamAnnotation = field.getAnnotation(HeaderParam.class);
            if (headerParamAnnotation != null) {
                String fieldName = field.getName();
                if (!headerParamAnnotation.name().equals(""))
                    fieldName = headerParamAnnotation.name();

                String value = ReflectionUtil.getFieldValue(field, o);

                if (value != null)
                    headers.put(fieldName, value);
            }
        }

        for (Map.Entry<java.lang.reflect.Method, Annotation> methodMap : ReflectionUtil.getMethodsAnnotatedWith(o.getClass(), HeaderParam.class).entrySet()) {
            String fieldName = methodMap.getKey().getName();
            HeaderParam headerParamAnnotation = (HeaderParam) methodMap.getValue();

            if (headerParamAnnotation.name() != null && headerParamAnnotation.name().length() != 0)
                fieldName = headerParamAnnotation.name();
            else {
                if (fieldName.startsWith("get") && fieldName.length() > 3) {
                    fieldName = fieldName.substring(3);
                    char c[] = fieldName.toCharArray();
                    c[0] = Character.toLowerCase(c[0]);
                    fieldName = new String(c);
                }
            }

            String value = ReflectionUtil.invokeMethod(methodMap.getKey(), o);
            if (value != null)
                headers.put(fieldName, value);
        }

        return headers;
    }

    public static Map<String, String> getMethodsAndFieldValuesAnnotatedWithPathParam(Object o)
    {
        Map<String, String> items = new HashMap<>();
        for (Field field : ReflectionUtil.getFieldsHavingAnnotation(o.getClass(), PathParam.class)) {
            field.setAccessible(true);

            PathParam headerParamAnnotation = field.getAnnotation(PathParam.class);
            if (headerParamAnnotation != null) {
                String fieldName = field.getName();
                if (!headerParamAnnotation.name().equals(""))
                    fieldName = headerParamAnnotation.name();

                String value = ReflectionUtil.getFieldValue(field, o);

                if (value != null)
                    items.put(fieldName, value);
            }
        }

        for (Map.Entry<java.lang.reflect.Method, Annotation> methodMap : ReflectionUtil.getMethodsAnnotatedWith(o.getClass(), PathParam.class).entrySet()) {
            String fieldName = methodMap.getKey().getName();
            PathParam headerParamAnnotation = (PathParam) methodMap.getValue();

            if (headerParamAnnotation.name() != null && headerParamAnnotation.name().length() != 0)
                fieldName = headerParamAnnotation.name();
            else {
                if (fieldName.startsWith("get") && fieldName.length() > 3) {
                    fieldName = fieldName.substring(3);
                    char c[] = fieldName.toCharArray();
                    c[0] = Character.toLowerCase(c[0]);
                    fieldName = new String(c);
                }
            }

            String value = ReflectionUtil.invokeMethod(methodMap.getKey(), o);
            if (value != null)
                items.put(fieldName, value);
        }

        return items;
    }

    public static Map<String, String> getMethodsAndFieldValuesAnnotatedWithQueryParam(Object o)
    {
        Map<String, String> items = new HashMap<>();
        for (Field field : ReflectionUtil.getFieldsHavingAnnotation(o.getClass(), QueryParam.class)) {
            field.setAccessible(true);

            QueryParam headerParamAnnotation = field.getAnnotation(QueryParam.class);
            if (headerParamAnnotation != null) {
                String fieldName = field.getName();
                if (!headerParamAnnotation.name().equals(""))
                    fieldName = headerParamAnnotation.name();

                String value = ReflectionUtil.getFieldValue(field, o);

                if (value != null)
                    items.put(fieldName, value);
            }
        }

        for (Map.Entry<java.lang.reflect.Method, Annotation> methodMap : ReflectionUtil.getMethodsAnnotatedWith(o.getClass(), QueryParam.class).entrySet()) {
            String fieldName = methodMap.getKey().getName();
            QueryParam headerParamAnnotation = (QueryParam) methodMap.getValue();

            if (headerParamAnnotation.name() != null && headerParamAnnotation.name().length() != 0)
                fieldName = headerParamAnnotation.name();
            else {
                if (fieldName.startsWith("get") && fieldName.length() > 3) {
                    fieldName = fieldName.substring(3);
                    char c[] = fieldName.toCharArray();
                    c[0] = Character.toLowerCase(c[0]);
                    fieldName = new String(c);
                }
            }

            String value = ReflectionUtil.invokeMethod(methodMap.getKey(), o);
            if (value != null)
                items.put(fieldName, value);
        }

        return items;
    }


}
