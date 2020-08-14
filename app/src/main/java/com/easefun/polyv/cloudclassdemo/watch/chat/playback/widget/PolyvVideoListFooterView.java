package com.easefun.polyv.cloudclassdemo.watch.chat.playback.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.easefun.polyv.cloudclassdemo.R;

import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;

public class PolyvVideoListFooterView extends FrameLayout implements IRefreshView {

    public PolyvVideoListFooterView(@NonNull Context context) {
        this(context, null);
    }

    public PolyvVideoListFooterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvVideoListFooterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.polyv_no_more_footer_layout, this);
    }

    @Override
    public int getType() {
        return TYPE_FOOTER;
    }

    @Override
    public int getStyle() {
        return STYLE_DEFAULT;
    }

    @Override
    public int getCustomHeight() {
        return -1;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onReset(SmoothRefreshLayout layout) {

    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout layout) {

    }

    @Override
    public void onRefreshBegin(SmoothRefreshLayout layout, IIndicator indicator) {

    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout layout, boolean isSuccessful) {

    }

    @Override
    public void onRefreshPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {

    }

    @Override
    public void onPureScrollPositionChanged(SmoothRefreshLayout layout, byte status, IIndicator indicator) {

    }
}
