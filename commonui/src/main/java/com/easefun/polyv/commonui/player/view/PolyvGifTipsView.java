package com.easefun.polyv.commonui.player.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.player.widget.PolyvDotView;

public class PolyvGifTipsView extends FrameLayout {
    //gif tipsView
    private View view;
    private ProgressBar pb_progress;
    private PolyvDotView v_dot;
    private Button bt_gif;

    public PolyvGifTipsView(Context context) {
        this(context, null);
    }

    public PolyvGifTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvGifTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_tips_view_gif, this);
        initView();
    }

    private void initView() {
        hide();
        pb_progress = (ProgressBar) view.findViewById(R.id.pb_progress);
        v_dot = (PolyvDotView) view.findViewById(R.id.v_dot);
        bt_gif = (Button) view.findViewById(R.id.bt_gif);
    }

    public boolean isShow() {
        return getVisibility() == View.VISIBLE;
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void show(int[] location) {
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) bt_gif.getLayoutParams();
        rlp.leftMargin = location[0];
        rlp.topMargin = location[1];
        bt_gif.setLayoutParams(rlp);
        setVisibility(View.VISIBLE);
    }

    private static final int UPDATE = 12;
    private static final int space = 25;
    private static final int part = 5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE) {
                pb_progress.setProgress(pb_progress.getProgress() + space / 25);
                if (pb_progress.getProgress() == pb_progress.getMax()) {
                    hide();
                    pb_progress.setProgress(0);
                    if (timeDownListener != null)
                        timeDownListener.onTimeDown(pb_progress.getMax() * space / 1000);
                    return;
                }
                handler.sendEmptyMessageDelayed(UPDATE, space);
            }
        }
    };

    public void updateProgress() {
        pb_progress.setProgress(0);
        handler.sendEmptyMessageDelayed(UPDATE, space);
    }

    public boolean isValidTime() {
        handler.removeMessages(UPDATE);
        hide();
        if (pb_progress.getProgress() >= pb_progress.getMax() / part) {
            return true;
        }
        return false;
    }

    public void cancel() {
        handler.removeMessages(UPDATE);
        hide();
        pb_progress.setProgress(0);
    }


    private TimeDownListener timeDownListener;

    public void setTimeDownListener(TimeDownListener listener) {
        this.timeDownListener = listener;
    }

    public interface TimeDownListener {
        public void onTimeDown(int timeSecond);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        v_dot.init(pb_progress.getWidth() / part, pb_progress.getHeight() / 2, pb_progress.getHeight() / 2);
    }
}
