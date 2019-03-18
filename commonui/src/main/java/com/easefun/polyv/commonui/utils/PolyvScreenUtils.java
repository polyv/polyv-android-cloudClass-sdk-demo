package com.easefun.polyv.commonui.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author df
 * @create 2019/2/18
 * @Describe
 */
public class PolyvScreenUtils {
    public static ViewGroup.MarginLayoutParams getLayoutParamsLayout(View layout) {
        ViewGroup.MarginLayoutParams rlp = null;
        if (layout.getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        } else if (layout.getParent() instanceof LinearLayout) {
            rlp = (LinearLayout.LayoutParams) layout.getLayoutParams();
        } else if (layout.getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) layout.getLayoutParams();
        }

        return rlp;
    }
}
