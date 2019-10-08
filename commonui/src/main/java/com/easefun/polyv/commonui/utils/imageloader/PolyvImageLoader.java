package com.easefun.polyv.commonui.utils.imageloader;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import com.easefun.polyv.commonui.utils.imageloader.glide.GlideImageLoadEngine;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * date: 2019/9/4 0004
 *
 * @author hwj
 * <p>
 * 图片加载器。默认使用Glide作为图片加载引擎{@link GlideImageLoadEngine}，
 * 开发者如果用了其他的图片加载框架，
 * 可以通过实现{@link IImageLoadEngine}接口，
 * 并在下方代码块中实例化，来快速替换图片加载库。
 */
public class PolyvImageLoader {
    // <editor-fold defaultstate="collapsed" desc="单例">
    private static PolyvImageLoader INSTANCE;

    private PolyvImageLoader() {/**/}

    public static PolyvImageLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (PolyvImageLoader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PolyvImageLoader();
                }
            }
        }
        return INSTANCE;
    }
    // </editor-fold>


    private IImageLoadEngine loadEngine;

    {
        loadEngine = new GlideImageLoadEngine();
    }

    /**
     * 加载图片
     */
    public void loadImage(Context context, String url, ImageView imageView) {
        loadEngine.loadImage(context, url, imageView);
    }

    /**
     * 加载图片：带有进度监听。
     */
    public void loadImage(Context context, final String url, final int position, @DrawableRes int errorRes, final IPolyvProgressListener listener) {
        loadEngine.loadImage(context, url, position, errorRes, listener);
    }

    /**
     * 加载图片，不进行本地磁盘缓存
     */
    public void loadImageNoDiskCache(Context context, String url, @DrawableRes int placeHolder, @DrawableRes int error, ImageView imageView) {
        loadEngine.loadImageNoDiskCache(context, url, placeHolder, error, imageView);
    }

    /**
     * 将图片保存成文件
     */
    @WorkerThread
    public File saveImageAsFile(Context context, String url) throws ExecutionException, InterruptedException {
        return loadEngine.saveImageAsFile(context, url);
    }
}
