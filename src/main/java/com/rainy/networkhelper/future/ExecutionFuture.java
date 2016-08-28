package com.rainy.networkhelper.future;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Abstract future that offers a
 * built-in executor, and generic listeners
 * for success and failure.
 * <p>
 * Created by szantogabor on 28/08/16.
 */
public abstract class ExecutionFuture<T> implements Future<T> {
    private static final ExecutorService sExecutorService = Executors.newFixedThreadPool(4);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private ExecutorService executorService;
    private volatile OnProgressChangedListener onProgressChangedListener;
    private volatile OnSuccessListener<T> onSuccessListener;
    private volatile OnErrorListener onErrorListener;
    private volatile boolean cancelled = false;
    private volatile boolean done = false;
    private final Object lock = new Object();

    public ExecutionFuture() {
        this.executorService = sExecutorService;
    }

    public ExecutionFuture(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected abstract T execute(Long timeoutMs) throws Exception;

    public void enqueue(OnSuccessListener<T> onSuccessListener, OnErrorListener onErrorListener) {
        this.onSuccessListener = onSuccessListener;
        this.onErrorListener = onErrorListener;
        this.cancelled = false;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final T result = execute(null);

                    synchronized (lock) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (ExecutionFuture.this.onSuccessListener != null && !cancelled)
                                    ExecutionFuture.this.onSuccessListener.onSuccess(result);
                            }
                        });
                    }
                } catch (final Exception e) {
                    synchronized (lock) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (ExecutionFuture.this.onErrorListener != null && !cancelled)
                                    ExecutionFuture.this.onErrorListener.onError(e);
                            }
                        });
                    }
                }
            }
        });
    }

    public ExecutionFuture<T> withProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (lock) {
            onSuccessListener = null;
            onErrorListener = null;
            onProgressChangedListener = null;
            cancelled = true;

            return !done;
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public final T get() throws InterruptedException, ExecutionException {
        try {
            T result = execute(null);
            done = true;
            return result;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public final T get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return execute(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(float progress, String message);
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnErrorListener {
        void onError(Exception e);
    }
}