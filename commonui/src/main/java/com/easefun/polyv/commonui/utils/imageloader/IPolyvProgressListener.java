package com.easefun.polyv.commonui.utils.imageloader;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * description 图片加载监听
 */
public interface IPolyvProgressListener {
    /**
     * 加载图片
     */
    void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes);

    void onFailed(@Nullable Exception e, Object model);

    void onStart(String url);

    void onResourceReady(Drawable drawable);
}
