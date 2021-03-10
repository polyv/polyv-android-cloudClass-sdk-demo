package com.easefun.polyv.commonui.utils.imageloader.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.easefun.polyv.commonui.utils.imageloader.IImageLoadEngine;
import com.easefun.polyv.commonui.utils.imageloader.IPolyvProgressListener;
import com.easefun.polyv.commonui.utils.imageloader.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.utils.imageloader.glide.progress.PolyvOnProgressListener;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * description 用Glide做为图片加载引擎
 */
public class GlideImageLoadEngine implements IImageLoadEngine {


    @Override
    public void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resId, ImageView imageView) {
        Glide.with(context).load(resId).into(imageView);
    }

    @Override
    @WorkerThread
    public File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException {
        return Glide.with(context)
                .asFile()
                .load(url)
                .submit()
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
                .apply(new RequestOptions()
                        .error(errorRes)
                        .transform(new CompressTransformation(context, url))
                )
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        listener.onFailed(e, model);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        PolyvMyProgressManager.removeListener(url, position);
                        listener.onProgress(url, true, 100, 0, 0);
                        return false;
                    }
                })
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        listener.onResourceReady(resource);
                        if (resource instanceof GifDrawable) {
                            ((GifDrawable) resource).start();//显示gif
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
                .apply(new RequestOptions()
                        .placeholder(placeHolder)
                        .error(error)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(imageView);
    }

    @Override
    public Drawable getImageAsDrawable(Context context, String url) {
        try {
            return Glide.with(context)
                    .load(url)
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
