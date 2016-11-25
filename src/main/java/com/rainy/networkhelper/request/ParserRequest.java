package com.rainy.networkhelper.request;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.common.reflect.TypeToken;
import com.rainy.networkhelper.exception.UnexpectedStatusCodeError;
import com.rainy.networkhelper.future.ParsedAsyncRequestFuture;
import com.rainy.networkhelper.mapper.BodyMapper;
import com.rainy.networkhelper.mapper.GsonBodyMapper;
import com.rainy.networkhelper.response.ParsedResponse;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

public class ParserRequest<T> extends BaseRequest<ParsedResponse<T>> {
    private Type responseType = null;
    private Object requestDto = null;
    private BodyMapper bodyEncoder = new GsonBodyMapper();
    private BodyMapper responseDecoder = new GsonBodyMapper();

    public ParserRequest() {
        super();
        this.responseType = new TypeToken<T>(getClass()) {
        }.getType();
    }

    /**
     * Constructor that will fetch and use the response responseType from the
     * generic responseType of this class. If no response parsing is needed,
     * pass {@link Void} in the response responseType.
     *
     * @param listener      The success listener.
     * @param errorListener The error listener.
     * @throws IllegalArgumentException
     */
    public ParserRequest(Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) throws IllegalArgumentException {
        super(listener, errorListener);
        this.responseType = new TypeToken<T>(getClass()) {
        }.getType();
    }

    /**
     * Constructor used to dynamically set the response type. If no
     * response parsing is needed, pass {@code null} or {@link Void}
     * in the response type.
     *
     * @param responseType  The responseType of the response object.
     * @param listener      The success listener.
     * @param errorListener The error listener.
     * @throws IllegalArgumentException
     */
    public ParserRequest(Type responseType, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) throws IllegalArgumentException {
        super(listener, errorListener);
        this.responseType = responseType;
    }

    public ParserRequest(int httpMethod, String url, Type responseType, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, listener, errorListener);
        this.responseType = responseType;
    }

    public ParserRequest(int httpMethod, String url, Map<String, String> headers, Type responseType, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, headers, listener, errorListener);
        this.responseType = responseType;
    }

    public Object getRequestDto() {
        return requestDto;
    }

    public ParserRequest<T> setRequestDto(Object requestDto) {
        this.requestDto = requestDto;
        return this;
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
                throw new RuntimeException("failed to serialize body", e);
            }
        }

        return null;
    }

    @Override
    protected Response<ParsedResponse<T>> parseNetworkResponse(NetworkResponse response) {
        Boolean valid = isResponseValid(response);

        if (valid == null || valid) {
            try {
                if (responseType == null || responseType == Void.class) {
                    ParsedResponse<T> parsedResponse = new ParsedResponse<>(response, null);
                    return Response.success(parsedResponse, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    T parsed = getResponseDecoder().decodeParams(response.data, responseType, HttpHeaderParser.parseCharset(response.headers));
                    ParsedResponse<T> parsedResponse = new ParsedResponse<>(response, parsed);
                    return Response.success(parsedResponse, HttpHeaderParser.parseCacheHeaders(response));
                }
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        } else {
            return Response.error(new UnexpectedStatusCodeError());
        }
    }

    /**
     * @return The responseType of the response object that was obtained from
     * either the subclass' generic responseType or from calling one of the responseType
     * constructors.
     */
    public Type getResponseType() {
        return responseType;
    }

    /**
     * Returns a future to the request, without sending it.
     * This will give a possibility to either send the request
     * synchronously or asynchronously. You can even cancel the
     * request with the future.
     * <p>
     * <b>Please note that if this method is called, the previously
     * set listeners will be forgotten.</b>
     */
    public ParsedAsyncRequestFuture<T> getParsedFuture(Context context) {
        ParsedAsyncRequestFuture<T> future = ParsedAsyncRequestFuture.newFuture(context, this);
        setListener(future);
        setErrorListener(future);
        return future;
    }
}