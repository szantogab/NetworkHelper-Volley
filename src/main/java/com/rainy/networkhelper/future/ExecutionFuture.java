package com.rainy.networkhelper.future;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.common.util.concurrent.AbstractFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract future implementation that offers a
 * built-in executor, and optional generic listeners
 * for success and failure.
 * <p>
 * Created by szantogabor on 28/08/16.
 */
public abstract class ExecutionFuture<T> {
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    private OnProgressChangedListener onProgressChangedListener;

    public abstract T execute() throws DataSourceException;

    public void enqueue(final OnSuccessListener<T> onSuccessListener, final OnErrorListener onErrorListener) {
        final Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final T result = execute();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSuccessListener.onSuccess(result);
                        }
                    });
                } catch (final DataSourceException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(Constants.LOG_TAG, "Data source exception", e);
                            onErrorListener.onError(e);
                        }
                    });
                }
            }
        });
    }

    public DataSourceFuture<T> withProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        this.onProgressChangedListener = onProgressChangedListener;
        return this;
    }

    protected void updateProgress(final float progress, final String message) {
        if (this.onProgressChangedListener != null) {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressChangedListener.onProgressChanged(progress, message);
                }
            });
        }
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(float progress, String message);
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnErrorListener {
        void onError(DataSourceException e);
    }
}