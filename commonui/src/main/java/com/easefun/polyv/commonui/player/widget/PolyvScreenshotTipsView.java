package com.easefun.polyv.commonui.player.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayerScreenRatio;
import com.easefun.polyv.commonui.R;

public class PolyvScreenshotTipsView extends FrameLayout {
    //screenshotView
    private View view;
    private ImageView iv_gallery;
    private RelativeLayout rl_top, rl_bot;
    private TextView tv_tips;
    private ProgressBar pb_progress;
    private Button bt_close;

    public PolyvScreenshotTipsView(Context context) {
        this(context, null);
    }

    public PolyvScreenshotTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvScreenshotTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_tips_view_screenshot, this);
        initView();
    }

    private void initView() {
        hide();
        iv_gallery = (ImageView) view.findViewById(R.id.iv_gallery);
        rl_top = (RelativeLayout) findViewById(R.id.rl_top);
        rl_bot = (RelativeLayout) findViewById(R.id.rl_bot);
        pb_progress = (ProgressBar) findViewById(R.id.pb_progress);
        tv_tips = (TextView) findViewById(R.id.tv_tips);
        bt_close = (Button) view.findViewById(R.id.bt_close);
        bt_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public void setOnCloseClickListener(OnClickListener listener) {
        bt_close.setOnClickListener(listener);
    }

    public boolean isShow() {
        return getVisibility() == View.VISIBLE;
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void show(Bitmap bitmap, int screenRatio) {
        pb_progress.setVisibility(View.GONE);
        rl_bot.setBackgroundResource(R.drawable.polyv_rl_bg_white);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) rl_top.getLayoutParams();
        rlp.width = -1;
        rlp.height = -1;
        rlp.topMargin = 0;
        rl_top.setLayoutParams(rlp);
        startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.polyv_scale_s));
        setVisibility(View.VISIBLE);
        if (screenRatio == PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT)
            iv_gallery.setScaleType(ImageView.ScaleType.CENTER_CROP);
        else if (screenRatio == PolyvPlayerScreenRatio.AR_MATCH_PARENT)
            iv_gallery.setScaleType(ImageView.ScaleType.FIT_XY);
        else
            iv_gallery.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv_gallery.setImageBitmap(bitmap);
    }

    public void showGifLayout() {
        pb_progress.setVisibility(View.VISIBLE);
        rl_bot.setBackgroundColor(Color.TRANSPARENT);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) rl_top.getLayoutParams();
        rlp.width = ((ViewGroup) getParent()).getWidth() / 3;
        rlp.height = ((ViewGroup) getParent()).getHeight() / 3;
        rlp.topMargin = ((ViewGroup) getParent()).getHeight() / 5;
        rl_top.setLayoutParams(rlp);
        tv_tips.setText("正在转换中，请稍等...");
        tv_tips.setVisibility(View.VISIBLE);
        iv_gallery.setImageBitmap(null);
        setVisibility(View.VISIBLE);
    }

    public void setErrorText(CharSequence msg) {
        pb_progress.setVisibility(View.GONE);
        tv_tips.setText(msg);
    }

    public void setGifData(byte[] data) {
        pb_progress.setVisibility(View.GONE);
        tv_tips.setVisibility(View.GONE);
//        Glide.with(getContext()).load(data).asGif().placeholder(Color.BLACK).into(iv_gallery);
    }
}
