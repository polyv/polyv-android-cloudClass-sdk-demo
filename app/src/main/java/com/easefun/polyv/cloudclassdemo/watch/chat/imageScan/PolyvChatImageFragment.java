package com.easefun.polyv.cloudclassdemo.watch.chat.imageScan;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.base.PolyvBaseFragment;
import com.easefun.polyv.commonui.widget.PolyvCircleProgressView;
import com.easefun.polyv.commonui.utils.imageloader.IPolyvProgressListener;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.commonui.widget.PolyvScaleImageView;

public class PolyvChatImageFragment extends PolyvBaseFragment {
    private PolyvChatListAdapter.ChatTypeItem chatTypeItem;
    private PolyvScaleImageView ivChatImg;
    private PolyvCircleProgressView cpvImgLoading;
    private View.OnClickListener onClickListener;
    private int position;
    private String imgUrl;

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
            PolyvImageLoader.getInstance()
                    .loadImage(getContext(), imgUrl, hashCode(), R.drawable.polyv_image_load_err, new IPolyvProgressListener() {

                        @Override
                        public void onStart(String url) {
                            if (cpvImgLoading.getProgress() == 0 && cpvImgLoading.getVisibility() != View.VISIBLE) {
                                cpvImgLoading.setVisibility(View.VISIBLE);
                                ivChatImg.setImageDrawable(null);
                            }
                        }

                        @Override
                        public void onResourceReady(Drawable drawable) {
                            ivChatImg.drawablePrepared(drawable);
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
                        public void onFailed(@Nullable Exception e, Object model) {
                            cpvImgLoading.setVisibility(View.GONE);
                            cpvImgLoading.setProgress(0);
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
