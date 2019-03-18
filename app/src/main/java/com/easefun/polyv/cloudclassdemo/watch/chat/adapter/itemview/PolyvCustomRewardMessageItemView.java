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
import com.easefun.polyv.cloudclassdemo.watch.PolyvDemoClient;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.model.PolyvCustomRewardBean;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author df
 * @create 2019/1/18
 * @Describe
 */
public class PolyvCustomRewardMessageItemView extends IPolyvCustomMessageBaseItemView<PolyvCustomEvent<PolyvCustomRewardBean>> {
    private CircleImageView rewardAvatar;
    private TextView acceptRewardPersonName;
    private TextView rewardValue;
    private ImageView rewardPic;
    private TextView rewardValueUnit;

    public PolyvCustomRewardMessageItemView(@NonNull Context context) {
        super(context);
    }

    public PolyvCustomRewardMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvCustomRewardMessageItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void processMessage(PolyvCustomEvent<PolyvCustomRewardBean> message, int pos) {
        PolyvCustomRewardBean data = message.getData();
        PolyvCustomEvent.UserBean userBean = message.getUser();
        if(PolyvDemoClient.getInstance().getTeacher() != null){
            acceptRewardPersonName.setText(PolyvDemoClient.getInstance().getTeacher().getNick());
        }
        rewardValue.setText(data.getPrice());
        rewardValueUnit.setText(data.getUnit());

        Glide.with(getContext())
                .load(userBean.getPic())
                .apply(requestOptions_s)
                .into(rewardAvatar);
    }

    @Override
    public void initView() {
        View.inflate(getContext(), R.layout.polyv_chat_custom_reward_item, this);

        rewardAvatar = findViewById(R.id.reward_avatar);
        acceptRewardPersonName = findViewById(R.id.accept_reward_person_name);
        rewardValue = findViewById(R.id.reward_value);
        rewardPic = findViewById(R.id.reward_pic);
        rewardValueUnit = findViewById(R.id.reward_value_unit);
    }
}
