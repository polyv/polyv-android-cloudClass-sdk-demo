package com.easefun.polyv.commonui.utils.imageloader.glide.progress;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PolyvMyProgressManager {

    private static Map<String, List<Object[]>> listenersMap =
            Collections.synchronizedMap(new HashMap<String, List<Object[]>>());
    private static final PolyvProgressResponseBody.InternalProgressListener LISTENER =
            new PolyvProgressResponseBody.InternalProgressListener() {
                @Override
                public void onProgress(String url, long bytesRead, long totalBytes) {
                    List<PolyvOnProgressListener> onProgressListenerList = getProgressListener(url);
                    if (onProgressListenerList != null) {
                        int percentage = (int) ((bytesRead * 1f / totalBytes) * 100f);
                        boolean isComplete = percentage >= 100;
                        if (!isComplete) {//交给glide回调(可以回调，因为多个相同url请求时glide只会回调其中一个，但是回调的话图片不会显示)
                            for (PolyvOnProgressListener onProgressListener : onProgressListenerList)
                                onProgressListener.onProgress(url, isComplete, percentage, bytesRead, totalBytes);
                        }
//                        if (isComplete) {
//                            removeListener(url);
//                        }
                    }
                }
            };
    private static OkHttpClient okHttpClient;

    private PolyvMyProgressManager() {
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            final String url = request.url().toString();
                            final List<PolyvOnProgressListener> onProgressListenerList = getProgressListener(url);
                            if (onProgressListenerList != null) {
                                PolyvProgressResponseBody.mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (PolyvOnProgressListener onProgressListener : onProgressListenerList)
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

    public static void addListener(String url, int position, PolyvOnProgressListener listener) {
        if (!TextUtils.isEmpty(url) && listener != null) {
            List<Object[]> objectList = getProgressListenerList(url);
            if (objectList != null) {
                boolean isExisted = false;
                for (Object[] objects : objectList) {
                    if (((int) objects[0]) == position) {
                        isExisted = true;
                        break;
                    }
                }
                if (!isExisted) {
                    objectList.add(new Object[]{position, listener});
                }
            } else {
                objectList = new ArrayList<>();
                objectList.add(new Object[]{position, listener});
                listenersMap.put(url, objectList);
            }
//            listener.onProgress(url, false, 1, 0, 0);
        }
    }

    public static void removeListener(String url, int position) {
        if (!TextUtils.isEmpty(url)) {
            List<Object[]> objectList = getProgressListenerList(url);
            if (objectList != null) {
                for (int i = 0; i < objectList.size(); i++) {
                    Object[] objects = objectList.get(i);
                    if (((int) objects[0]) == position) {
                        objectList.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public static void removeAllListener() {
        listenersMap.clear();
    }

    public static List<Object[]> getProgressListenerList(String url) {
        if (TextUtils.isEmpty(url) || listenersMap == null || listenersMap.size() == 0
                || listenersMap.get(url) == null || listenersMap.get(url).size() == 0) {
            return null;
        }

        return listenersMap.get(url);
    }

    public static Map<String, List<Object[]>> getListenersMap() {
        return listenersMap;
    }

    public static List<PolyvOnProgressListener> getProgressListener(String url) {
        List<Object[]> objectList = getProgressListenerList(url);
        List<PolyvOnProgressListener> onProgressListenerList = new ArrayList<>();
        if (objectList != null) {
            for (Object[] objects : objectList) {
                onProgressListenerList.add((PolyvOnProgressListener) objects[1]);
            }
        }
        return onProgressListenerList.size() > 0 ? onProgressListenerList : null;
    }
}
