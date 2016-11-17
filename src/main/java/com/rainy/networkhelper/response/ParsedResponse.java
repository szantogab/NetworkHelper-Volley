package com.rainy.networkhelper.response;

import com.android.volley.NetworkResponse;

/**
 * Created by szantogabor on 11/03/16.
 */
public class ParsedResponse<T> {
    private T parsedResponse;
    private NetworkResponse networkResponse;

    public ParsedResponse(NetworkResponse networkResponse, T parsedResponse) {
        this.parsedResponse = parsedResponse;
        this.networkResponse = networkResponse;
    }

    public NetworkResponse getNetworkResponse() {
        return networkResponse;
    }

    public T getParsedResponse() {
        return parsedResponse;
    }
}
