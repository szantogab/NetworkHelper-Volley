package com.rainy.networkhelper.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.common.reflect.TypeToken;
import com.rainy.networkhelper.exception.UnexpectedStatusCodeError;
import com.rainy.networkhelper.mapper.BodyMapper;
import com.rainy.networkhelper.mapper.GsonBodyMapper;
import com.rainy.networkhelper.response.ParsedResponse;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ParserRequest<T> extends BaseRequest<ParsedResponse<T>> {
    private Type type = null;
    private Object requestDto = null;
    private BodyMapper bodyEncoder = new GsonBodyMapper();
    private BodyMapper responseDecoder = new GsonBodyMapper();

    public ParserRequest(Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) throws IllegalArgumentException {
        super(listener, errorListener);
        this.type = new TypeToken<T>(getClass()) {
        }.getType();
    }

    public ParserRequest(int httpMethod, String url, Type type, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, listener, errorListener);
        this.type = type;
    }

    public ParserRequest(int httpMethod, String url, Map<String, String> headers, Type type, Response.Listener<ParsedResponse<T>> listener, Response.ErrorListener errorListener) {
        super(httpMethod, url, headers, listener, errorListener);
        this.type = type;
    }

    public Object getRequestDto() {
        return requestDto;
    }

    public void setRequestDto(Object requestDto) {
        this.requestDto = requestDto;
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
        Boolean valid = isResponseValid(response);

        if (valid == null || valid) {
            try {
                if (type == null) {
                    ParsedResponse<T> parsedResponse = new ParsedResponse<>(response, null);
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
        } else {
            return Response.error(new UnexpectedStatusCodeError());
        }
    }
}