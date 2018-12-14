package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;

/**
 * @author df
 * @create 2018/11/30
 * @Describe 旋转view的抽象定义
 */
public interface IPolyvRotateBaseView {

    public void topSubviewTo(final int top);

    public void resetSoftTo();

    public void resetFloatViewLand();

    public void resetFloatViewPort();

    public ViewGroup.MarginLayoutParams getLayoutParamsLayout();

    public void setOriginTop(int originTop);

    public void scrollToPosition(int pos, View parent);

    public ViewTreeObserver getViewTreeObserver();

    public void setLayoutParams(ViewGroup.LayoutParams params);

    public void setVisibility( int visibility);

    public ViewGroup getOwnView();

    public void enableShow(boolean show);
}
