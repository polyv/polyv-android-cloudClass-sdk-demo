package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.model.PolyvCustomGiftBean;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author df
 * @create 2019/1/18
 * @Describe
 */
public class PolyvCustomGiftMessageItemView extends IPolyvCustomMessageBaseItemView<PolyvCustomEvent<PolyvCustomGiftBean>> {
    private CircleImageView giftAvatar;
    private TextView giftName;
    private ImageView giftPic;
    private TextView giftCount;

    public PolyvCustomGiftMessageItemView(@NonNull Context context) {
        super(context);
    }

    public PolyvCustomGiftMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvCustomGiftMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void processMessage(PolyvCustomEvent<PolyvCustomGiftBean> eventMessage, int pos) {

        PolyvCustomGiftBean data = eventMessage.getData();
        PolyvCustomEvent.UserBean userBean = eventMessage.getUser();
        giftCount.setText(getContext().getString(R.string.gift_send_count,data.getGiftCount()));
        giftName.setText(getContext().getString(R.string.gift_send,data.getGiftName()));

        if (PolyvCustomGiftBean.GIFTTYPE_TEA.equals(data.getGiftType())) {
            giftPic.setImageResource(R.drawable.polyv_gift_tea);
        } else if (PolyvCustomGiftBean.GIFTTYPE_CLAP.equals(data.getGiftType())) {
            giftPic.setImageResource(R.drawable.polyv_gift_clap);
        } else if (PolyvCustomGiftBean.GIFTTYPE_FLOWER.equals(data.getGiftType())) {
            giftPic.setImageResource(R.drawable.polyv_gift_flower);
        } else {
            Glide.with(getContext())
                    .load(data.getGiftImgUrl())
                    .apply(requestOptions_s)
                    .into(giftPic);
        }
        Glide.with(getContext())
                .load(userBean.getPic())
                .apply(requestOptions_s)
                .into(giftAvatar);
    }

    @Override
    public void initView() {

        View.inflate(getContext(), R.layout.polyv_chat_custom_gift_item, this);

        giftAvatar = findViewById(R.id.gift_avatar);
        giftName = findViewById(R.id.gift_name);
        giftPic = findViewById(R.id.gift_pic);
        giftCount = findViewById(R.id.gift_count);

    }
}
