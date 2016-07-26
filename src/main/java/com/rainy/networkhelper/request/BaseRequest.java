package com.rainy.networkhelper.request;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.rainy.networkhelper.annotation.QueryConstantParam;
import com.rainy.networkhelper.annotation.QueryConstantParams;
import com.rainy.networkhelper.annotation.RequestMethod;
import com.rainy.networkhelper.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequest<T> extends Request<T> {
    public final String HEADER_CONTENT_TYPE = "Content-Type";

    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private Map<String, String> pathParams = new HashMap<>();
    private Response.Listener<T> listener;
    private static RequestQueue queue;
    private int connectionType = -1;

    private Integer method = null;
    private String url;

    /**
     * Constructor for creating a new request. When using this constructor, the class must be used with the @{code RequestMethod} annotation, where the method and the URL must be specified.
     *
     * @param listener      The normal success listener
     * @param errorListener The error listener
     */
    public BaseRequest(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IllegalArgumentException {
        super(Method.GET, null, errorListener);
        this.listener = listener;

        RequestMethod requestMethod = (RequestMethod) ReflectionUtil.getClassAnnotation(getClass(), RequestMethod.class);
        if (requestMethod == null)
            throw new IllegalArgumentException("This class must be annotated with RequestMethod annotation.");

        if (requestMethod.url() == null || requestMethod.url().length() == 0)
            throw new IllegalArgumentException("The RequestMethod annotation's url must be specified.");

        this.method = requestMethod.method();
        this.url = requestMethod.url();

        QueryConstantParam queryConstantParam = (QueryConstantParam) ReflectionUtil.getClassAnnotation(getClass(), QueryConstantParam.class);
        if (queryConstantParam != null) {
            queryParams.put(queryConstantParam.name(), queryConstantParam.value());
        }

        QueryConstantParams queryConstantParams = (QueryConstantParams) ReflectionUtil.getClassAnnotation(getClass(), QueryConstantParams.class);
        if (queryConstantParams != null) {
            for (QueryConstantParam queryConstantParam1 : queryConstantParams.value())
                queryParams.put(queryConstantParam1.name(), queryConstantParam1.value());
        }
    }

    public BaseRequest(int httpMethod, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, errorListener);
        this.listener = listener;
    }

    public BaseRequest(int httpMethod, String url, Map<String, String> headers, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, errorListener);

        this.listener = listener;
        this.headers = headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    public void send(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context.getApplicationContext());
        }

        send(context, queue);
    }

    public void send(Context context, RequestQueue requestQueue) {
        if (!isConnectionAvailable(context, connectionType)) {
            deliverError(new NoConnectionError());
            return;
        }

        requestQueue.add(this);
    }

    /**
     * @return Returns the query parameters that have been added either with the addQueryParam method, or with the QueryConstantParam annotation.
     */
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * @return Returns the path parameters.
     */
    public Map<String, String> getPathParams() {
        return pathParams;
    }

    /**
     * Sets a path parameter to the given value. Note: this will only work if the URL contains placeholder for the parameter, like this: http://somedomain.com/users/{user}.
     *
     * @param pathParamName The name of the URL path parameter
     * @param value         The value of the path parameter.
     */
    public void setPathParam(String pathParamName, String value) {
        pathParams.put(pathParamName, value);
    }

    public void addQueryParam(String name, String value) {
        queryParams.put(name, value);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        headers.putAll(ReflectionUtil.getMethodsAndFieldValuesAnnotatedWithHeaderParam(this));
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeaderParam(String name, String value) {
        if (this.headers == null)
            this.headers = new HashMap<>();

        this.headers.put(name, value);
    }

    /**
     * @param context context
     * @param type    One of ConnectivityManager.TYPE_ constants.
     * @return Returns whether the specified connection is active.
     */
    public static boolean isConnectionAvailable(Context context, int type) {
        if (type == -1)
            return true;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getNetworkInfo(type);
        }

        return networkInfo != null && networkInfo.isConnected();
    }

    public int getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public int getMethod() {
        if (method != null)
            return method;

        return super.getMethod();
    }

    @Override
    public String getUrl() {
        String _url = (url != null ? url : super.getUrl());

        if (_url != null) {
            pathParams.putAll(ReflectionUtil.getMethodsAndFieldValuesAnnotatedWithPathParam(this));
            if (getPathParams() != null) {
                for (Map.Entry<String, String> entry : getPathParams().entrySet()) {
                    _url = _url.replaceAll("\\{" + entry.getKey() + "\\}", entry.getValue());
                }
            }

            queryParams.putAll(ReflectionUtil.getMethodsAndFieldValuesAnnotatedWithQueryParam(this));
            if (getQueryParams() != null) {
                for (Map.Entry<String, String> entry : getQueryParams().entrySet()) {
                    String c = _url.contains("?") ? "&" : "?";
                    _url += c + entry.getKey() + "=" + entry.getValue();
                }
            }
        }

        return _url;
    }

    @Override
    protected void deliverResponse(T basicResponseDto) {
        if (listener != null) {
            listener.onResponse(basicResponseDto);
        }
    }

    public static RequestQueue getQueue() {
        return queue;
    }
}