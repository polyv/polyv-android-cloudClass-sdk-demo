package com.easefun.polyv.commonui.utils.glide.progress;

import android.text.TextUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PolyvProgressManager {

    private static Map<String, PolyvOnProgressListener> listenersMap =
            Collections.synchronizedMap(new HashMap<String, PolyvOnProgressListener>());
    private static final PolyvProgressResponseBody.InternalProgressListener LISTENER =
            new PolyvProgressResponseBody.InternalProgressListener() {
                @Override
                public void onProgress(String url, long bytesRead, long totalBytes) {
                    PolyvOnProgressListener onProgressListener = getProgressListener(url);
                    if (onProgressListener != null) {
                        int percentage = (int) ((bytesRead * 1f / totalBytes) * 100f);
                        boolean isComplete = percentage >= 100;
                        if (!isComplete) {//交给glide回调
                            onProgressListener.onProgress(url, isComplete, percentage, bytesRead, totalBytes);
                        }
//                        if (isComplete) {
//                            removeListener(url);
//                        }
                    }
                }
            };
    private static OkHttpClient okHttpClient;

    private PolyvProgressManager() {
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            final String url = request.url().toString();
                            final PolyvOnProgressListener onProgressListener = getProgressListener(url);
                            if (onProgressListener != null) {
                                PolyvProgressResponseBody.mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onProgressListener.onStart(url);
                                    }
                                });
                            }
                            Response response = chain.proceed(request);
                            return response.newBuilder()
                                    .body(new PolyvProgressResponseBody(url, LISTENER,
                                            response.body()))
                                    .build();
                        }
                    })
                    .build();
        }
        return okHttpClient;
    }

    public static void addListener(String url, PolyvOnProgressListener listener) {
        if (!TextUtils.isEmpty(url) && listener != null) {
            listenersMap.put(url, listener);
//            listener.onProgress(url, false, 1, 0, 0);
        }
    }

    public static void removeListener(String url) {
        if (!TextUtils.isEmpty(url)) {
            listenersMap.remove(url);
        }
    }

    public static PolyvOnProgressListener getProgressListener(String url) {
        if (TextUtils.isEmpty(url) || listenersMap == null || listenersMap.size() == 0) {
            return null;
        }

        PolyvOnProgressListener listenerWeakReference = listenersMap.get(url);
        if (listenerWeakReference != null) {
            return listenerWeakReference;
        }
        return null;
    }
}
