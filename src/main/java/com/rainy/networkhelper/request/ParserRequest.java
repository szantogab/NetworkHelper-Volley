package com.rainy.networkhelper.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.rainy.networkhelper.annotation.Header;
import com.rainy.networkhelper.mapper.BodyMapper;
import com.rainy.networkhelper.mapper.GsonBodyMapper;
import com.rainy.networkhelper.response.ParsedResponse;
import com.rainy.networkhelper.util.ReflectionUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

public class ParserRequest<T> extends BaseRequest<ParsedResponse<T>> {
    private Class<T> clazz = null;
    private Type type = null;
    private Object requestDto = null;
    private BodyMapper bodyEncoder = new GsonBodyMapper();
    private BodyMapper responseDecoder = new GsonBodyMapper();

    public ParserRequest(Class<T> clazz, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) throws IllegalArgumentException {
        super(listener, errorListener);
        this.clazz = clazz;
    }

    public ParserRequest(Type type, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) throws IllegalArgumentException {
        super(listener, errorListener);
        this.type = type;
    }

    public ParserRequest(int httpMethod, String url, Class<T> clazz, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, listener, errorListener);
        this.clazz = clazz;
    }

    public ParserRequest(int httpMethod, String url, Type type, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, listener, errorListener);
        this.type = type;
    }

    public ParserRequest(int httpMethod, String url, Map<String, String> headers, Class<T> clazz, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, headers, listener, errorListener);
        this.clazz = clazz;
    }

    public Object getRequestDto() {
        return requestDto;
    }

    public void setRequestDto(Object requestDto) {
        this.requestDto = requestDto;

        if (this.requestDto != null) {
            for (Field field : ReflectionUtil.getFieldsHavingAnnotation(requestDto.getClass(), Header.class)) {
                field.setAccessible(true);

                Header headerAnnotation = field.getAnnotation(Header.class);
                if (headerAnnotation != null) {
                    String fieldName = field.getName();
                    if (!headerAnnotation.name().equals(""))
                        fieldName = headerAnnotation.name();

                    String value = ReflectionUtil.getFieldValue(field, requestDto);

                    if (value != null)
                        try {
                            Map<String, String> headers = getHeaders();
                            headers.put(fieldName, value);
                            setHeaders(headers);
                        } catch (AuthFailureError authFailureError) {
                            authFailureError.printStackTrace();
                        }
                }
            }
        }
    }

    @Override
    public String getBodyContentType() {
        try {
            return (getHeaders() != null && getHeaders().containsKey(HEADER_CONTENT_TYPE) ? getHeaders().get(HEADER_CONTENT_TYPE) : bodyEncoder.getContentType());
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }

        return super.getBodyContentType();
    }

    public BodyMapper getBodyEncoder() {
        return bodyEncoder;
    }

    public BaseRequest setBodyEncoder(BodyMapper bodyEncoder) {
        this.bodyEncoder = bodyEncoder;
        return this;
    }

    public BodyMapper getResponseDecoder() {
        return responseDecoder;
    }

    public BaseRequest setResponseDecoder(BodyMapper responseDecoder) {
        this.responseDecoder = responseDecoder;
        return this;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (requestDto != null) {
            try {
                return bodyEncoder.encodeParams(requestDto, getParamsEncoding());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected Response<ParsedResponse<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            if ((clazz == null && type == null) || clazz == NetworkResponse.class) {
                ParsedResponse<T> parsedResponse = new ParsedResponse<>(response, null);
                return Response.success(parsedResponse, HttpHeaderParser.parseCacheHeaders(response));
            } else if (clazz != null) {
                T parsed = getResponseDecoder().decodeParams(response.data, clazz, HttpHeaderParser.parseCharset(response.headers));
                ParsedResponse<T> parsedResponse = new ParsedResponse<>(response, parsed);
                return Response.success(parsedResponse, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                T parsed = getResponseDecoder().decodeParams(response.data, type, HttpHeaderParser.parseCharset(response.headers));
                ParsedResponse<T> parsedResponse = new ParsedResponse<>(response, parsed);
                return Response.success(parsedResponse, HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}