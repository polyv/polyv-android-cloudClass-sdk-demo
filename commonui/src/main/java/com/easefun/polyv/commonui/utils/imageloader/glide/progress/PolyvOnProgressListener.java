package com.easefun.polyv.commonui.utils.imageloader.glide.progress;

public interface PolyvOnProgressListener {
    void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes);

    void onStart(String url);
}