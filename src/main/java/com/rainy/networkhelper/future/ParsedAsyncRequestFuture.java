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

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rainy.networkhelper.request.BaseRequest;
import com.rainy.networkhelper.response.ParsedResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
public class ParsedAsyncRequestFuture<T> extends ExecutionFuture<T> implements Response.Listener<ParsedResponse<T>>, Response.ErrorListener {
    protected BaseRequest<ParsedResponse<T>> mRequest;
    private Context context;
    private Exception mException;
    private T mResult;
    private boolean mResultReceived = false;

    protected ParsedAsyncRequestFuture(Context context, BaseRequest<ParsedResponse<T>> request) {
        this.context = context;
        this.mRequest = request;
    }

    public static <E> ParsedAsyncRequestFuture<E> newFuture(Context context, BaseRequest<ParsedResponse<E>> request) {
        return new ParsedAsyncRequestFuture<>(context, request);
    }


    @Override
    protected synchronized T execute(Long timeoutMs) throws Exception {
        if (mException != null) {
            throw new ExecutionException(mException);
        }

        if (mResultReceived) {
            return mResult;
        }

        mRequest.send(context);

        if (timeoutMs == null) {
            wait(0);
        } else if (timeoutMs > 0) {
            wait(timeoutMs);
        }

        if (mException != null) {
            throw new ExecutionException(mException);
        }

        if (!mResultReceived) {
            throw new TimeoutException();
        }

        return mResult;
    }

    public void setRequest(BaseRequest<ParsedResponse<T>> mRequest) {
        this.mRequest = mRequest;
    }

    public Request<ParsedResponse<T>> getRequest() {
        return mRequest;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancel = super.cancel(mayInterruptIfRunning);
        if (mRequest != null)
            mRequest.cancel();
        return cancel;
    }

    @Override
    public synchronized void onResponse(ParsedResponse<T> response) {
        if (response != null)
            mResult = response.getParsedResponse();

        mResultReceived = true;
        notifyAll();
    }

    @Override
    public synchronized void onErrorResponse(VolleyError error) {
        mException = error;
        mResultReceived = true;
        notifyAll();
    }
}