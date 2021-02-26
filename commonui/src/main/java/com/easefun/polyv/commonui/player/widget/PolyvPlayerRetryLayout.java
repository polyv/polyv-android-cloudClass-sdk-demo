package com.easefun.polyv.commonui.player.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.commonui.R;

/**
 * date: 2021/2/22
 * author: HWilliamgo
 * description: 播放器重试布局
 */
public class PolyvPlayerRetryLayout extends FrameLayout {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private TextView playerRetryTv;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    public PolyvPlayerRetryLayout(@NonNull Context context) {
        this(context, null);
    }

    public PolyvPlayerRetryLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerRetryLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.plv_player_retry_layout, this, true);

        playerRetryTv = view.findViewById(R.id.plv_player_retry_tv);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void setOnClickPlayerRetryListener(OnClickListener onClickListener) {
        playerRetryTv.setOnClickListener(onClickListener);
    }
    // </editor-fold>
}
