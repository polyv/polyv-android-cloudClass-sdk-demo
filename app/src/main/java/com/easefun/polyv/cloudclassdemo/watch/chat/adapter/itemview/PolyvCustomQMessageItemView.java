package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.model.PolyvCustomQBean;

/**
 * @author df
 * @create 2019/1/16
 * @Describe
 */
public class PolyvCustomQMessageItemView extends IPolyvCustomMessageBaseItemView<PolyvCustomEvent<PolyvCustomQBean>> {
    private ImageView ivQ;

    public PolyvCustomQMessageItemView(@NonNull Context context) {
        super(context);
    }

    public PolyvCustomQMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvCustomQMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void processMessage(PolyvCustomEvent<PolyvCustomQBean> message, int pos) {
        PolyvCustomQBean data= message.getData();
        int type = data.getContentType();
        ivQ.setImageResource(getDrawResourceID("polyv_q" + type));
    }

    public void initView() {
        View.inflate(getContext(), R.layout.polyv_chat_message_q_item, this);
        ivQ = findViewById(R.id.message_q_iv);
    }

    @Override
    public void playAnimation() {

    }

}
