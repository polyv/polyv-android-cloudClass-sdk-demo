package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.easefun.polyv.businesssdk.sub.gif.GifSpanTextView;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.PolyvQuestionMessage;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview.PolyvItemViewFactoy;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvCircleProgressView;

/**
 * @author df
 * @create 2019/1/15
 * @Describe 发送消息item
 */

public class PolyvSendMessageHolder extends ClickableViewHolder<Object,PolyvChatListAdapter> {
    public GifSpanTextView sendMessage;
    public ImageView chatImg;
    public PolyvCircleProgressView imgLoading;

    public PolyvSendMessageHolder(View itemView, PolyvChatListAdapter adapter) {
        super(itemView, adapter);

    }

    private void initView() {
        sendMessage =  $(com.easefun.polyv.commonui.R.id.gtv_send_message);
        chatImg =  $(com.easefun.polyv.commonui.R.id.iv_chat_img);
        imgLoading =  $(com.easefun.polyv.commonui.R.id.cpv_img_loading);
    }

    @Override
    public void processNormalMessage(Object item, int position) {
            handleSendMessage(this,item,position);
    }

    @Override
    public   void processCustomMessage(PolyvCustomEvent item, int position) {
        resendMessageButton.setVisibility(View.GONE);

    }

    @Override
    public <T> IPolyvCustomMessageBaseItemView createItemView(PolyvCustomEvent<T> baseCustomEvent) {
        return PolyvItemViewFactoy.createItemView(baseCustomEvent.getEVENT(),context);
    }

    private void handleSendMessage(PolyvSendMessageHolder sendMessageHolder, Object object, int position) {
        int childIndex = findReuseChildIndex(PolyvChatManager.SE_MESSAGE);
        if(childIndex < 0){
            View child = View.inflate(context,
                    R.layout.polyv_chat_send_normal_message_content_item, null);
            child.setTag(PolyvChatManager.SE_MESSAGE);
            contentContainer.addView(child);
            initView();
        }

        sendMessageHolder.imgLoading.setTag(position);
        if (!(object instanceof PolyvSendLocalImgEvent || object instanceof PolyvChatImgHistory)) {
            sendMessageHolder.resendMessageButton.setVisibility(View.GONE);
            sendMessageHolder.chatImg.setVisibility(View.GONE);
            sendMessageHolder.imgLoading.setVisibility(View.GONE);
            sendMessageHolder.sendMessage.setVisibility(View.VISIBLE);
        } else {
            sendMessageHolder.resendMessageButton.setVisibility(View.GONE);
            sendMessageHolder.sendMessage.setVisibility(View.GONE);
            sendMessageHolder.chatImg.setVisibility(View.VISIBLE);
            sendMessageHolder.chatImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.getOnChatImgViewClickListener() != null) {
                        adapter.getOnChatImgViewClickListener().onClick(sendMessageHolder.chatImg, position);
                    }
                }
            });
            sendMessageHolder.resendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.getOnResendMessageViewClickListener() != null) {
                        adapter.getOnResendMessageViewClickListener().onClick(sendMessageHolder.resendMessageButton, position);
                    }
                }
            });
        }
        //本地发言信息
        if (object instanceof PolyvLocalMessage) {
            PolyvLocalMessage localMessage = (PolyvLocalMessage) object;
            sendMessageHolder.sendMessage.setText((CharSequence) localMessage.getObjects()[0]);
        } else if (object instanceof PolyvSendLocalImgEvent) {//本地图片信息
            PolyvSendLocalImgEvent sendLocalImgEvent = (PolyvSendLocalImgEvent) object;

            sendMessageHolder.resendMessageButton.setVisibility(sendLocalImgEvent.isSendFail() ? View.VISIBLE : View.GONE);
            sendMessageHolder.imgLoading.setVisibility(sendLocalImgEvent.isSendSuccess() || sendLocalImgEvent.isSendFail() ? View.GONE : View.VISIBLE);
            sendMessageHolder.imgLoading.setProgress(sendLocalImgEvent.getSendProgress());

            fitChatImgWH(sendLocalImgEvent.getWidth(), sendLocalImgEvent.getHeight(), sendMessageHolder.chatImg);
            Glide.with(parentView.getContext())
                    .load(sendLocalImgEvent.getImageFilePath())
                    .into(sendMessageHolder.chatImg);
        } else if (object instanceof PolyvQuestionMessage) {//提问信息
            PolyvQuestionMessage questionMessage = (PolyvQuestionMessage) object;
            sendMessageHolder.sendMessage.setText((CharSequence) questionMessage.getObjects()[0]);
        } else if (object instanceof PolyvSpeakHistory) {//历史自己的发言信息
            PolyvSpeakHistory speakHistory = (PolyvSpeakHistory) object;
            sendMessageHolder.sendMessage.setText((CharSequence) speakHistory.getObjects()[0]);
        } else if (object instanceof PolyvChatImgHistory) {//历史自己的图片信息
            PolyvChatImgHistory chatImgHistory = (PolyvChatImgHistory) object;
            PolyvChatImgHistory.ContentBean contentBean = chatImgHistory.getContent();

            sendMessageHolder.imgLoading.setVisibility(View.GONE);
            sendMessageHolder.imgLoading.setProgress(0);
            fitChatImgWH((int) contentBean.getSize().getWidth(), (int) contentBean.getSize().getHeight(), sendMessageHolder.chatImg);
            loadNetImg(contentBean.getUploadImgUrl(), position, sendMessageHolder.imgLoading, sendMessageHolder.chatImg);
        }
    }
}
