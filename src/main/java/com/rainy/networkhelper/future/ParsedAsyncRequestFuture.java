package com.rainy.networkhelper.future;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.util.concurrent.AbstractFuture;
import com.rainy.networkhelper.request.ParserRequest;
import com.rainy.networkhelper.response.ParsedResponse;

/**
 * A Future that represents a Volley request.
 * <p>
 * Used by providing as your response and error listeners. For example:
 * <pre>
 * RequestFuture&lt;JSONObject&gt; future = RequestFuture.newFuture();
 * MyRequest request = new MyRequest(URL, future, future);
 *
 * // If you want to be able to cancel the request:
 * future.setRequest(requestQueue.add(request));
 *
 * // Otherwise:
 * requestQueue.add(request);
 *
 * try {
 *   JSONObject response = future.get();
 *   // do something with response
 * } catch (InterruptedException e) {
 *   // handle the error
 * } catch (ExecutionException e) {
 *   // handle the error
 * }
 * </pre>
 *
 * @param <T> The type of parsed response this future expects.
 */
public class ParsedAsyncRequestFuture<T> extends AbstractFuture<T> implements Response.Listener<ParsedResponse<T>>, Response.ErrorListener {
    protected ParserRequest<T> mRequest;

    protected ParsedAsyncRequestFuture() {
    }

    protected ParsedAsyncRequestFuture(ParserRequest<T> request) {
        this.mRequest = request;
    }

    public static <E> ParsedAsyncRequestFuture<E> newFuture() {
        return new ParsedAsyncRequestFuture<>();
    }

    public static <E> ParsedAsyncRequestFuture<E> newFuture(ParserRequest<E> request) {
        return new ParsedAsyncRequestFuture<>(request);
    }

    public void setRequest(ParserRequest<T> mRequest) {
        this.mRequest = mRequest;
    }

    public ParserRequest<?> getRequest() {
        return mRequest;
    }

    @Override
    public synchronized void onResponse(ParsedResponse<T> response) {
        set(response.getParsedResponse());
    }

    @Override
    public synchronized void onErrorResponse(VolleyError error) {
        setException(error);
    }
}


