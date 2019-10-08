package com.easefun.polyv.commonui.utils.imageloader.glide;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendChatImageHelper;

import java.io.File;
import java.security.MessageDigest;

/**
 * 压缩
 */
public class CompressTransformation implements Transformation<Bitmap> {

    private static final int VERSION = 1;
    private static final String ID = "CompressTransformation." + VERSION;
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private BitmapPool mBitmapPool;
    private String mUrl;

    CompressTransformation(Context context, String url) {
        this(url, Glide.get(context).getBitmapPool());
    }

    private CompressTransformation(String url, BitmapPool pool) {
        mBitmapPool = pool;
        mUrl = url;
    }

    @Override
    public Resource<Bitmap> transform(Context context, Resource<Bitmap> resource, int outWidth, int outHeight) {
        if (new File(mUrl).isFile()) {
            try {
                Bitmap bitmap = PolyvSendChatImageHelper.compressImage(mUrl);
                if (bitmap != null) {
                    return BitmapResource.obtain(bitmap, mBitmapPool);
                }
            } catch (Exception e) {
            }
        }
        return resource;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompressTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}