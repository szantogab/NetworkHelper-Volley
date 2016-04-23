package com.rainy.networkhelper.response;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

/**
 * Created by szantogabor on 11/03/16.
 */
public class ParsedVolleyError extends VolleyError {

    public ParsedVolleyError() {
    }

    public ParsedVolleyError(NetworkResponse networkResponse) {
        super(networkResponse);
    }
}
