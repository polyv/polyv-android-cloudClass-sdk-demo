package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.RequestOptions;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

/**
 * @author df
 * @create 2018/11/30* @Describe
 */
public class PolyvNormalLinkMicView extends LinearLayout implements IPolyvRotateBaseView {
    private static final String TAG = "PolyvNormalLinkMicView";
    //键盘弹起前得位置
    private int beforeSoftLeft = 0;
    private int beforeSoftTop = 0;
    private int originTop = 0;

    private boolean canShow;

    public PolyvNormalLinkMicView(Context context) {
        super(context);
    }

    public PolyvNormalLinkMicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvNormalLinkMicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void topSubviewTo(int top) {
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

    @Override
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

    @Override
    public void resetFloatViewLand() {
        setVisibility(INVISIBLE);
    }

    @Override
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

        rlp.leftMargin = 0;
        rlp.topMargin = originTop;
        if(canShow){
            setVisibility(VISIBLE);
        }
        setLayoutParams(rlp);
    }

    @Override
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

    @Override
    public void setOriginTop(int originTop) {
        this.originTop = originTop;
    }

    @Override
    public void scrollToPosition(int pos, View parent) {

    }

    @Override
    public ViewGroup getOwnView() {
        return this;
    }

    @Override
    public void enableShow(boolean canShow) {
        this.canShow = canShow;
    }

}
