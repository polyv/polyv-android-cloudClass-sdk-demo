package com.easefun.polyv.cloudclassdemo.watch.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder.PolyvReceiveMessageHolder;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder.PolyvSendMessageHolder;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder.PolyvTipsMessageHolder;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;

/**
 * @author df
 * @create 2019/1/15
 * @Describe viewholder生成类
 */
public class PolyvViewHolderCreateFactory {
    public static ClickableViewHolder createViewHolder
            (int viewType, Context context, ViewGroup parent,PolyvChatListAdapter adapter) {
        ClickableViewHolder clickableViewHolder = null;
        switch (viewType) {
            case PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS:
                clickableViewHolder = new PolyvTipsMessageHolder(LayoutInflater.from(context).
                        inflate(com.easefun.polyv.commonui.R.layout.polyv_chat_tips_message_item,
                                parent, false),adapter);
                break;
            case PolyvChatListAdapter.ChatTypeItem.TYPE_SEND:
                clickableViewHolder = new PolyvSendMessageHolder(LayoutInflater.from(context).
                        inflate(com.easefun.polyv.commonui.R.layout.polyv_chat_send_message_item,
                                parent, false),adapter);
                break;
            case PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE:
                clickableViewHolder = new PolyvReceiveMessageHolder(LayoutInflater.from(context).
                        inflate(com.easefun.polyv.commonui.R.layout.polyv_chat_receive_message_item,
                                parent, false),adapter);
                break;
            default:
                    //create default viewholder
                clickableViewHolder = new PolyvTipsMessageHolder(LayoutInflater.from(context).
                        inflate(com.easefun.polyv.commonui.R.layout.polyv_chat_tips_message_item,
                                parent, false),adapter);
                break;
        }
        return clickableViewHolder;
    }
}
