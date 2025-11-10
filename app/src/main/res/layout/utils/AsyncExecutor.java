package com.example.library.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class để xử lý background tasks một cách đơn giản
 */
public class AsyncExecutor {
    
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    /**
     * Execute background task với callback
     */
    public static <T> void execute(Supplier<T> backgroundTask, ResultCallback<T> callback) {
        executor.execute(() -> {
            try {
                T result = backgroundTask.get();
                mainHandler.post(() -> callback.onResult(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }
    
    /**
     * Execute simple background task
     */
    public static void execute(Runnable backgroundTask, Runnable onComplete) {
        executor.execute(() -> {
            try {
                backgroundTask.run();
                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }
            } catch (Exception e) {
                // Log error if needed
                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }
            }
        });
    }
    
    /**
     * Execute task với error handling
     */
    public static <T> void executeWithErrorHandling(Supplier<T> backgroundTask, 
                                                   SuccessCallback<T> onSuccess, 
                                                   ErrorCallback onError) {
        executor.execute(() -> {
            try {
                T result = backgroundTask.get();
                mainHandler.post(() -> {
                    if (result != null) {
                        onSuccess.onSuccess(result);
                    } else {
                        onError.onError("Task returned null result");
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> onError.onError(e.getMessage()));
            }
        });
    }
    
    /**
     * Execute task với transform function
     */
    public static <T, R> void executeWithTransform(Supplier<T> backgroundTask, 
                                                  Function<T, R> transform, 
                                                  ResultCallback<R> callback) {
        executor.execute(() -> {
            try {
                T backgroundResult = backgroundTask.get();
                R transformedResult = transform.apply(backgroundResult);
                mainHandler.post(() -> callback.onResult(transformedResult));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }
    
    /**
     * Post task to main thread
     */
    public static void runOnMainThread(Runnable task) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            mainHandler.post(task);
        }
    }
    
    /**
     * Post delayed task to main thread
     */
    public static void runOnMainThreadDelayed(Runnable task, long delayMillis) {
        mainHandler.postDelayed(task, delayMillis);
    }
    
    /**
     * Shutdown executor (call this in Application.onTerminate())
     */
    public static void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    // Callback interfaces
    public interface ResultCallback<T> {
        void onResult(T result);
    }
    
    public interface SuccessCallback<T> {
        void onSuccess(T result);
    }
    
    public interface ErrorCallback {
        void onError(String error);
    }
}