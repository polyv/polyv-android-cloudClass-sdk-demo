package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.easefun.polyv.cloudclass.chat.send.custom.PolyvBaseCustomEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.event.PolyvChatMessageEvent;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;

import static com.easefun.polyv.cloudclassdemo.watch.chat.event.PolyvChatMessageEvent.MessageEvent.GiftMessage;
import static com.easefun.polyv.cloudclassdemo.watch.chat.event.PolyvChatMessageEvent.MessageEvent.RewardMessage;

/**
 * @author df
 * @create 2019/1/16
 * @Describe 自定义view的工厂类
 */
public class PolyvItemViewFactoy {

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
        if (RewardMessage.equals(type)) {
            itemView = new PolyvCustomRewardMessageItemView(context);
        }else if(GiftMessage.equals(type)){
            itemView = new PolyvCustomGiftMessageItemView(context);
        }
        return itemView;
    }

    @NonNull
    private static IPolyvCustomMessageBaseItemView createDefaultView(Context context) {
        return new IPolyvCustomMessageBaseItemView(context) {
            @Override
            public void processMessage(Object data, int pos) {
                if (data instanceof PolyvCustomEvent) {
                    String tip = ((PolyvCustomEvent) data).getTip();
                    defaultView.setText(TextUtils.isEmpty(tip) ? PolyvBaseCustomEvent.TIP_DEFAULT : tip);
                }
            }

            @Override
            public void initView() {

                View.inflate(context, R.layout.polyv_chat_default_message,this);
                defaultView = findViewById(R.id.chat_message_default_tip);
            }
        };
    }
}
