package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

/**
 * @author df
 * @create 2018/10/18
 * @Describe
 */
public class PolyvLinkMicListView extends HorizontalScrollView implements IPolyvRotateBaseView{
    private static final String TAG = "PolyvLinkMicListView";
    // 竖屏下的位置
    private int portraitLeft = 0;
    private int portraitTop = 0;

    //键盘弹起前得位置
    private int beforeSoftLeft = 0;
    private int beforeSoftTop = 0;
    private Scroller scroller;

    private int originTop = 0;

    public PolyvLinkMicListView(Context context) {
        this(context,null);
    }

    public PolyvLinkMicListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PolyvLinkMicListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initView(Context context){
        scroller = new Scroller(context);
    }
    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        post(new Runnable() {
            @Override
            public void run() {
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    resetFloatViewPort();
                } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    resetFloatViewLand();
                }
            }
        });

    }

    public void topSubviewTo(final int top) {
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams rlp = getLayoutParamsLayout();
                if (rlp == null) {
                    return;
                }
                beforeSoftLeft = rlp.leftMargin;
                beforeSoftTop = rlp.topMargin;
                if (rlp.topMargin + rlp.height < top) {
                    return;
                }

                PolyvCommonLog.d(TAG, "topSubviewTo left :" + beforeSoftLeft + "   top " + top);
                rlp.topMargin = top - rlp.height;
                setLayoutParams(rlp);
            }
        });

    }

    public void resetSoftTo() {
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams rlp = getLayoutParamsLayout();
                if (rlp == null) {
                    return;
                }
                PolyvCommonLog.d(TAG, "resetSoftTo left :" + beforeSoftLeft + "   top " + beforeSoftTop);
                rlp.leftMargin = beforeSoftLeft;
                rlp.topMargin = beforeSoftTop;
                setLayoutParams(rlp);
            }
        });

    }

    public void resetFloatViewLand() {
        ViewGroup.MarginLayoutParams layoutParams = null;
        if (getParent() instanceof RelativeLayout) {
            layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        } else {
            return;
        }
        portraitLeft = layoutParams.leftMargin;
        portraitTop = layoutParams.topMargin;


        Log.d(TAG, "resetFloatViewLand: leftMargin :" + layoutParams.leftMargin + " parent height :topMargin"
                + layoutParams.topMargin + "   height :" + getMeasuredHeight());

        layoutParams.leftMargin = PolyvScreenUtils.dip2px(getContext(), 100);
        layoutParams.topMargin = 0;
        layoutParams.width= ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(layoutParams);
        setBackground(null);
    }

    public void resetFloatViewPort() {
        MarginLayoutParams rlp = null;
        if (getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            rlp = (LinearLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) getLayoutParams();
        } else {
            return;
        }

        Log.d(TAG, "resetFloatViewPort: leftMargin :" + portraitLeft + " parent height :topMargin"
                + portraitTop + "   width :" + getMeasuredWidth());
        rlp.leftMargin = 0;
        rlp.topMargin = originTop;
        rlp.width=LayoutParams.MATCH_PARENT;
        setLayoutParams(rlp);
        setBackgroundColor(Color.WHITE);
    }

    public ViewGroup.MarginLayoutParams getLayoutParamsLayout() {
        ViewGroup.MarginLayoutParams rlp = null;
        if (getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            rlp = (LinearLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) getLayoutParams();
        }

        return rlp;
    }


    public Scroller getScroller() {
        return scroller;
    }

    public void scrollToPosition(int pos, View parent) {

        smoothScrollTo(parent.getRight(),0);
    }

    @Override
    public ViewGroup getOwnView() {
        return this;
    }

    @Override
    public void enableShow( boolean canshow) {

    }

    public void setOriginTop(int originTop) {
        this.originTop = originTop;
    }
}
