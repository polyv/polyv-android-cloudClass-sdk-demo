package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoHelper;

/**
 * @author df
 * @create 2019/8/6
 * @Describe
 */
public class PolyvLinkMicParent {
    //连麦底部控制栏区域
    private PolyvLinkMicBottomView linkMicBottomView;
    private ViewGroup linkMicLayout;
    private IPolyvRotateBaseView linkMicView;

    private Context mContext;

    public void initView(@NonNull View linkMicStubView, boolean isParticipant,final View teacherInfoLayout){
        mContext= linkMicStubView.getContext();
        linkMicLayout = linkMicStubView.findViewById(R.id.link_mic_layout);
        linkMicView = linkMicStubView.findViewById(R.id.link_mic_layout_parent);
        linkMicBottomView = linkMicStubView.findViewById(R.id.link_mic_bottom);
        linkMicBottomView.hideHandsUpLink(isParticipant);

        linkMicView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams rlp = linkMicView.getLayoutParamsLayout();
                if (rlp == null) {
                    return;
                }

                rlp.leftMargin = 0;
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rlp.topMargin = 0;
                } else { // 若初始为竖屏
                    if(teacherInfoLayout != null){
                        rlp.topMargin = teacherInfoLayout.getTop();
                    }
                }

                linkMicView.setOriginTop(rlp.topMargin);
                linkMicView.setLayoutParams(rlp);

                if (Build.VERSION.SDK_INT >= 16) {
                    linkMicView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    linkMicView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        linkMicLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (oldRight < right) {
                    linkMicView.scrollToPosition(0, linkMicLayout);
                }
            }
        });
    }

    public void addClassHelper(PolyvCloudClassVideoHelper livePlayerHelper) {
        linkMicBottomView.addClassHelper(livePlayerHelper);
    }

    //显示看我按钮
    public void showLookAtMeView() {
        if (linkMicBottomView != null) {
            linkMicBottomView.showLookAtMeView();
        }
    }

    public void hideBrushColor(boolean showPaint) {
        linkMicBottomView.hideBrushColor(showPaint);
    }

    public ViewGroup getLinkMicLayout() {
        return linkMicLayout;
    }

    public IPolyvRotateBaseView getLinkMicView() {
        return linkMicView;
    }

    public void updateLinkController(boolean isVideo) {
        linkMicBottomView.updateLinkCameraController(isVideo);
    }
    public void updateBottomController(boolean show) {
        linkMicBottomView.updateLinkMicController(show);
    }
}
