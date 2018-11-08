package com.easefun.polyv.commonui.view;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.base.PolyvBaseFragment;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvCircleProgressView;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvOnProgressListener;
import com.easefun.polyv.commonui.widget.PolyvImageViewer;
import com.easefun.polyv.commonui.widget.PolyvScaleImageView;

public class PolyvChatImageFragment extends PolyvBaseFragment {
    private PolyvChatListAdapter.ChatTypeItem chatTypeItem;
    private PolyvScaleImageView ivChatImg;
    private PolyvCircleProgressView cpvImgLoading;
    private View.OnClickListener onClickListener;
    private int position;

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
        final String imgUrl = PolyvImageViewer.getImgUrl(chatTypeItem);
        if (imgUrl != null) {
            final int position = hashCode();
            PolyvMyProgressManager.removeListener(imgUrl, position);
            final PolyvOnProgressListener onProgressListener = new PolyvOnProgressListener() {
                @Override
                public void onStart(String url) {
                    cpvImgLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                    if (isComplete) {
                        cpvImgLoading.setVisibility(View.GONE);
                    } else {
                        cpvImgLoading.setVisibility(View.VISIBLE);
                        cpvImgLoading.setProgress(percentage);
                    }
                }

                @Override
                public void onFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    cpvImgLoading.setVisibility(View.GONE);
                }
            };
            PolyvMyProgressManager.addListener(imgUrl, position, onProgressListener);
            Glide.with(this)
                    .load(imgUrl)
                    .apply(new RequestOptions().error(R.drawable.polyv_image_load_err))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            onProgressListener.onFailed(e, model, target, isFirstResource);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            PolyvMyProgressManager.removeListener(imgUrl, position);
                            onProgressListener.onProgress(imgUrl, true, 100, 0, 0);
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onDestroy() {
                            PolyvMyProgressManager.removeListener(imgUrl, position);
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            ivChatImg.drawablePrepared(resource);
                        }
                    });
        }
    }
}
