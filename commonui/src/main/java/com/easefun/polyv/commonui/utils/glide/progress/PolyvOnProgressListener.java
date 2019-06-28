package com.easefun.polyv.commonui.utils.glide.progress;

import android.support.annotation.Nullable;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;

public interface PolyvOnProgressListener {
    void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes);

    void onFailed(@Nullable Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource);

    void onStart(String url);
}