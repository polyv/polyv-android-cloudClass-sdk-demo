package com.easefun.polyv.commonui.utils.glide.progress;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.target.Target;

public interface PolyvOnProgressListener {
    void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes);

    void onFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource);

    void onStart(String url);
}