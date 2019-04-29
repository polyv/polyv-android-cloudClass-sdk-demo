package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.cloudclass.chat.event.PolyvCustomerMessageEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;

/**
 * @author df
 * @create 2019/1/16
 * @Describe
 */
public class PolyvCustomTipMessageItemView extends IPolyvCustomMessageBaseItemView<PolyvCustomEvent<PolyvCustomerMessageEvent>> {
    public PolyvCustomTipMessageItemView(@NonNull Context context) {
        super(context);
    }

    public PolyvCustomTipMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvCustomTipMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void processMessage(PolyvCustomEvent<PolyvCustomerMessageEvent> data, int pos) {

    }

    @Override
    public void initView() {
        View.inflate(getContext(), R.layout.polyv_chat_message_q_item,this);
    }
}
