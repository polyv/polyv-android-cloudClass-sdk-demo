package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
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
        giftCount.setText(getContext().getString(R.string.gift_send_count, data.getGiftCount()));
        giftCount.setTag(data.getGiftCount());
        giftName.setText(getContext().getString(R.string.gift_send, PolyvCustomGiftBean.getGiftName(data.getGiftType())));

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

    @Override
    public void playAnimation() {
        giftCount.post(() -> GiftCountZoomAnim.startZoom(giftCount, (Integer) giftCount.getTag()));
    }

    //礼物个数缩放动画
    private static class GiftCountZoomAnim {
        //缩放比例
        private static float ZOOM_RATIO = 1.3f;
        private static long MAGNIFY_DURATION = 200;
        private static long SHRINK_DURATION = 100;

        static void startZoom(TextView tv, int count) {
            float width = tv.getPaint().measureText(tv.getContext().getString(R.string.gift_send_count, count));
            tv.getLayoutParams().width = (int) width;
            tv.setLayoutParams(tv.getLayoutParams());
            tv.setText("");

            //放大
            Animation magnify = new ScaleAnimation(0f, ZOOM_RATIO, 0f, ZOOM_RATIO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            magnify.setDuration(MAGNIFY_DURATION);
            magnify.setRepeatCount(count);
            magnify.setInterpolator(new OvershootInterpolator());
            magnify.setAnimationListener(new Animation.AnimationListener() {
                int i = 0;

                @Override
                public void onAnimationStart(Animation animation) {/**/}

                @Override
                public void onAnimationEnd(Animation animation) {
                    //缩小
                    Animation shrink = new ScaleAnimation(ZOOM_RATIO, 1f, ZOOM_RATIO, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    shrink.setDuration(SHRINK_DURATION);
                    tv.startAnimation(shrink);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    tv.setText(tv.getContext().getString(R.string.gift_send_count, ++i));
                }
            });
            tv.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {/**/}

                @Override
                public void onViewDetachedFromWindow(View v) {
                    tv.removeOnAttachStateChangeListener(this);
                    tv.clearAnimation();
                    tv.setText(tv.getContext().getString(R.string.gift_send_count, count));
                }
            });
            tv.startAnimation(magnify);
        }
    }
}
