package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.event.PolyvChatMessageEvent;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;

/**
 * @author df
 * @create 2019/1/16
 * @Describe 自定义view的工厂类
 */
public class PolyvItemViewFactoy {
    private static final String DEFAULTTYPE = "DEFAULTTYPE";// test

    //创建发送接收样式的view
    public static IPolyvCustomMessageBaseItemView createItemView(String type, Context context) {
        IPolyvCustomMessageBaseItemView itemView = createDefaultView(context);
        if (PolyvChatMessageEvent.MessageEvent.QMessage.equals(type)) {
            itemView = new PolyvCustomQMessageItemView(context);
        }

        return itemView;
    }

    //创建显示在中间的提示性的view
    public static IPolyvCustomMessageBaseItemView createTipItemView(String type, Context context) {
        IPolyvCustomMessageBaseItemView itemView = createDefaultView(context);
        if (DEFAULTTYPE.equals(type)) {
            itemView = new PolyvCustomTipMessageItemView(context);
        }

        return itemView;
    }

    @NonNull
    private static IPolyvCustomMessageBaseItemView createDefaultView(Context context) {
        return new IPolyvCustomMessageBaseItemView(context) {
            TextView defaultView = null;


            @Override
            public void processMessage(Object data, int pos) {

            }

            @Override
            public void initView() {

                View.inflate(context, R.layout.polyv_chat_default_message,this);
                defaultView = findViewById(R.id.chat_message_default_tip);
            }
        };
    }
}
