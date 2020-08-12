package com.easefun.polyv.commonui.utils.imageloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * description 图片加载引擎
 */
public interface IImageLoadEngine {
    void loadImage(Context context, String url, ImageView imageView);

    @WorkerThread
    File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException;

    void loadImage(Context context, String url, int position, @DrawableRes int errorRes, IPolyvProgressListener listener);

    void loadImageNoDiskCache(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView);

    Drawable getImageAsDrawable(Context context, String url);
}
