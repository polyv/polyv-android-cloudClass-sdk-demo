package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.easefun.polyv.cloudclassdemo.watch.linkMic.IPolyvViewVisibilityChangedListener;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

/**
 * @author df
 * @create 2018/11/30*
 * @Describe
 */
public class PolyvNormalLinkMicView extends ScrollView implements IPolyvRotateBaseView {
    private static final String TAG = "PolyvNormalLinkMicView";
    //键盘弹起前得位置
    private int beforeSoftLeft = 0;
    private int beforeSoftTop = 0;
    //控件一开始的顶部位置
    private int originTop = 0;

    private boolean canShow, supportRTC;

    private IPolyvViewVisibilityChangedListener visibilityChangedListener;
    private String linkType;//连麦类型 "video":视频 "audio":音频

    public PolyvNormalLinkMicView(Context context) {
        super(context);
    }

    public PolyvNormalLinkMicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvNormalLinkMicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // <editor-fold defaultstate="collapsed" desc="set相关方法">
    @Override
    public void setOnVisibilityChangedListener(IPolyvViewVisibilityChangedListener listener) {
        this.visibilityChangedListener = listener;
    }

    @Override
    public void resetSoftTo() {
        post(new Runnable() {
            @Override
            public void run() {
                MarginLayoutParams rlp = getLayoutParamsLayout();
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
        //如果已经连麦了 且是音频连麦 则不显示
        if (!supportRTC) {
            super.setVisibility(INVISIBLE);
        } else {
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


            Log.d(TAG, "resetFloatViewLand: leftMargin :" + layoutParams.leftMargin + " parent height :topMargin"
                    + layoutParams.topMargin + "   height :" + getMeasuredHeight());

            layoutParams.leftMargin = 0;
            layoutParams.topMargin = 0;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(layoutParams);
        }
    }

    private boolean isAudio() {
        return "audio".equals(linkType);
    }

    @Override
    public void resetFloatViewPort() {
        MarginLayoutParams rlp = null;
        if (getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            rlp = (LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) getLayoutParams();
        } else {
            return;
        }

        rlp.leftMargin = 0;
        rlp.topMargin = originTop;
        rlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        rlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (canShow) {
            super.setVisibility(VISIBLE);
        }
        setLayoutParams(rlp);
    }

    @Override
    public void enableShow(boolean canShow) {
        this.canShow = canShow;
    }

    @Override
    public void setLinkType(String type) {
        this.linkType = type;
    }

    @Override
    public void setSupportRtc(boolean supportRTC) {
        this.supportRTC = supportRTC;
    }


    @Override
    public void setOriginTop(int originTop) {
        this.originTop = originTop;
    }

    @Override
    public void setVisibility(int visibility) {
        //如果是rtc 正常显示
        if (supportRTC) {
            super.setVisibility(visibility);
            return;
        }
        //音频连麦下 只响应横屏下得显示隐藏操作
        if (PolyvScreenUtils.isPortrait(getContext())) {
            super.setVisibility(visibility);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="移动相关">
    @Override
    public void scrollToPosition(int pos, View parent) {
        smoothScrollTo(parent.getRight(), 0);
    }

    @Override
    public void topSubviewTo(final int top) {
        post(new Runnable() {
            @Override
            public void run() {
                MarginLayoutParams rlp = getLayoutParamsLayout();
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

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="get相关">
    @Override
    public ViewGroup getOwnView() {
        return this;
    }

    @Override
    public MarginLayoutParams getLayoutParamsLayout() {
        MarginLayoutParams rlp = null;
        if (getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            rlp = (LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) getLayoutParams();
        }
        return rlp;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="系统方法">

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
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibilityChangedListener != null && changedView == this) {
            visibilityChangedListener.onVisibilityChanged(changedView, visibility);
        }
    }

    // </editor-fold>
}
