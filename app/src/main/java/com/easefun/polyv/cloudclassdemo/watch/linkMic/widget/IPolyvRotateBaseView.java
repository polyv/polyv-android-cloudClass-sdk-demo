package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.easefun.polyv.cloudclassdemo.watch.linkMic.IPolyvViewVisibilityChangedListener;

/**
 * @author df
 * @create 2018/11/30
 * @Describe 旋转view的抽象定义 (云课堂连麦与普通直播连麦的接口定义)
 */
public interface IPolyvRotateBaseView {

   // <editor-fold defaultstate="collapsed" desc="set相关方法">
    public void resetSoftTo();

    public void resetFloatViewLand();

    public void resetFloatViewPort();

    public void setOriginTop(int originTop);

    public void setLayoutParams(ViewGroup.LayoutParams params);

    public void setVisibility(int visibility);

    public void enableShow(boolean show);

    public void setLinkType(String type);

    public void setSupportRtc(boolean type);

 public void setOnVisibilityChangedListener(IPolyvViewVisibilityChangedListener listener);
   // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="移动相关">
    public void scrollToPosition(int pos, View parent);

    public void topSubviewTo(final int top);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="get 相关代码">
    public ViewTreeObserver getViewTreeObserver();

    public ViewGroup.MarginLayoutParams getLayoutParamsLayout();

    public ViewGroup getOwnView();
    // </editor-fold>

}
