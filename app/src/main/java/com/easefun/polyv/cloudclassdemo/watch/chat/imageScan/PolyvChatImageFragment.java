package com.easefun.polyv.cloudclassdemo.watch.chat.imageScan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendChatImageHelper;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.base.PolyvBaseFragment;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvCircleProgressView;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvOnProgressListener;
import com.easefun.polyv.commonui.widget.PolyvScaleImageView;

import java.io.File;
import java.security.MessageDigest;

public class PolyvChatImageFragment extends PolyvBaseFragment {
    private PolyvChatListAdapter.ChatTypeItem chatTypeItem;
    private PolyvScaleImageView ivChatImg;
    private PolyvCircleProgressView cpvImgLoading;
    private View.OnClickListener onClickListener;
    private int position;
    private String imgUrl;
    private int listenerPosition;

    @Override
    public int layoutId() {
        return R.layout.polyv_fragment_chat_image;
    }

    public static PolyvChatImageFragment newInstance(PolyvChatListAdapter.ChatTypeItem chatTypeItem, int position) {
        PolyvChatImageFragment chatImageFragment = new PolyvChatImageFragment();
        chatImageFragment.setData(chatTypeItem, position);
        return chatImageFragment;
    }

    private void setData(PolyvChatListAdapter.ChatTypeItem chatTypeItem, int position) {
        this.chatTypeItem = chatTypeItem;
        this.position = position;
    }

    public PolyvChatImageFragment setOnImgClickListener(View.OnClickListener l) {
        this.onClickListener = l;
        return this;
    }

    @Override
    public void loadDataDelay(boolean isFirst) {
    }

    @Override
    public void loadDataAhead() {
        cpvImgLoading = findViewById(R.id.cpv_img_loading);
        ivChatImg = findViewById(R.id.iv_chat_img);
        ivChatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
            }
        });

        //加载图片
        if (chatTypeItem == null)
            return;
        imgUrl = PolyvChatImageViewer.getImgUrl(chatTypeItem);
        if (imgUrl != null) {
            listenerPosition = hashCode();
            PolyvMyProgressManager.removeListener(imgUrl, listenerPosition);
            final PolyvOnProgressListener onProgressListener = new PolyvOnProgressListener() {
                @Override
                public void onStart(String url) {
                    if (cpvImgLoading.getProgress() == 0 && cpvImgLoading.getVisibility() != View.VISIBLE) {
                        cpvImgLoading.setVisibility(View.VISIBLE);
                        ivChatImg.setImageDrawable(null);
                    }
                }

                @Override
                public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                    if (isComplete) {
                        cpvImgLoading.setVisibility(View.GONE);
                        cpvImgLoading.setProgress(100);
                    } else if (ivChatImg.getDrawable() == null) {//onFailed之后可能触发onProgress
                        cpvImgLoading.setVisibility(View.VISIBLE);
                        cpvImgLoading.setProgress(percentage);
                    }
                }

                @Override
                public void onFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    cpvImgLoading.setVisibility(View.GONE);
                    cpvImgLoading.setProgress(0);
                }
            };
            PolyvMyProgressManager.addListener(imgUrl, listenerPosition, onProgressListener);
            Glide.with(this)
                    .load(imgUrl)
                    .apply(new RequestOptions().error(R.drawable.polyv_image_load_err).transform(new CompressTransformation(getContext(), imgUrl)))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            onProgressListener.onFailed(e, model, target, isFirstResource);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            PolyvMyProgressManager.removeListener(imgUrl, listenerPosition);
                            onProgressListener.onProgress(imgUrl, true, 100, 0, 0);
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onDestroy() {
                            PolyvMyProgressManager.removeListener(imgUrl, listenerPosition);//loadFailed时没移除(触发loadFailed时这里没回调了)，需在onDestroy里移除
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            ivChatImg.drawablePrepared(resource);
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        PolyvMyProgressManager.removeListener(imgUrl, listenerPosition);
        super.onDestroy();
    }

    public static class CompressTransformation implements Transformation<Bitmap> {

        private static final int VERSION = 1;
        private static final String ID = "CompressTransformation." + VERSION;
        private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

        private BitmapPool mBitmapPool;
        private String mUrl;

        public CompressTransformation(Context context, String url) {
            this(url, Glide.get(context).getBitmapPool());
        }

        public CompressTransformation(String url, BitmapPool pool) {
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
}
