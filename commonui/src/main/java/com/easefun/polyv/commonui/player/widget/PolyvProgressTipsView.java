package com.easefun.polyv.commonui.player.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyv.commonui.R;
import com.easefun.polyv.foundationsdk.utils.PolyvTimeUtils;


public class PolyvProgressTipsView extends FrameLayout {
    //progressView
    private View view;
    private TextView tv_currenttime, tv_totaltime;
    private SeekBar seekBar;
//    private ImageView iv_left, iv_right;

    public PolyvProgressTipsView(Context context) {
        this(context, null);
    }

    public PolyvProgressTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvProgressTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_tips_view_progress, this);
        initView();
    }

    private void initView() {
        hide();
        tv_currenttime = (TextView) view.findViewById(R.id.tv_currenttime);
        tv_totaltime = (TextView) view.findViewById(R.id.tv_totaltime);
        seekBar = view.findViewById(R.id.sb_playprogress);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void delayHide() {
        handler.removeMessages(View.GONE);
        handler.sendEmptyMessageDelayed(View.GONE, 300);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == View.GONE)
                setVisibility(View.GONE);
        }
    };

    public void setProgressPercent(int fastForwardPos, int totaltime, boolean slideEnd, boolean isRightSwipe) {
        handler.removeMessages(View.GONE);
        if (slideEnd) {
            handler.sendEmptyMessageDelayed(View.GONE, 300);
        } else {
            setVisibility(View.VISIBLE);
//            if (isRightSwipe) {
//                iv_left.setVisibility(View.GONE);
//                iv_right.setVisibility(View.VISIBLE);
//            } else {
//                iv_left.setVisibility(View.VISIBLE);
//                iv_right.setVisibility(View.GONE);
//            }
            seekBar.setProgress((int) (1000L * fastForwardPos / totaltime));
            if (fastForwardPos < 0)
                fastForwardPos = 0;
            if (fastForwardPos > totaltime)
                fastForwardPos = totaltime;
            tv_currenttime.setText(PolyvTimeUtils.generateTime(fastForwardPos));
            tv_totaltime.setText(PolyvTimeUtils.generateTime(totaltime));
        }
    }
}
