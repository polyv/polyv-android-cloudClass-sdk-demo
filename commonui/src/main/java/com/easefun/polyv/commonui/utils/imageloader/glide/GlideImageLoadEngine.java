package com.easefun.polyv.commonui.utils.imageloader.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.easefun.polyv.commonui.utils.imageloader.IImageLoadEngine;
import com.easefun.polyv.commonui.utils.imageloader.IPolyvProgressListener;
import com.easefun.polyv.commonui.utils.imageloader.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.utils.imageloader.glide.progress.PolyvOnProgressListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class GlideImageLoadEngine implements IImageLoadEngine {


    @Override
    public void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    @Override
    @WorkerThread
    public File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException {
        return Glide.with(context)
                .load(url)
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();
    }

    @Override
    public void loadImage(Context context, final String url, final int position, @DrawableRes int errorRes, final IPolyvProgressListener listener) {

        PolyvMyProgressManager.removeListener(url, position);
        PolyvMyProgressManager.addListener(url, position, new PolyvOnProgressListener() {
            @Override
            public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                listener.onProgress(url, isComplete, percentage, bytesRead, totalBytes);
            }


            @Override
            public void onStart(String url) {
                listener.onStart(url);
            }
        });
        Glide.with(context)
                .load(url)
                .error(errorRes)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        listener.onFailed(e, model);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        PolyvMyProgressManager.removeListener(url, position);
                        listener.onProgress(url, true, 100, 0, 0);
                        return false;
                    }
                })
                .bitmapTransform(new CompressTransformation(context, url))
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        listener.onResourceReady(resource);
                        if (resource instanceof GifDrawable) {
                            resource.start();//显示gif
                        }
                    }

                    @Override
                    public void onDestroy() {
                        PolyvMyProgressManager.removeListener(url, position);
                    }
                });
    }


    @Override
    public void loadImageNoDiskCache(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .placeholder(placeHolder)
                .error(error)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    @Override
    public Drawable getImageAsDrawable(Context context, String url) {
        try {
            Bitmap bitmap = Glide.with(context)
                    .load(url)
                    .asBitmap() //必须
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            return new BitmapDrawable(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}