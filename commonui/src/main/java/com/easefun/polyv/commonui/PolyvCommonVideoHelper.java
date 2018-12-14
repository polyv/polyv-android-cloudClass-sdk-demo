package com.easefun.polyv.commonui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvBaseVideoView;
import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassVideoView;
import com.easefun.polyv.commonui.player.IPolyvVideoItem;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTView;
import com.easefun.polyv.commonui.widget.PolyvTouchContainerView;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

/**
 * @author df
 * @create 2018/8/15
 * @Describe 公共帮助类 。抽象出基本得帮助工具类
 */
public abstract class PolyvCommonVideoHelper<T extends IPolyvVideoItem<P, Q>, P extends PolyvCommonVideoView,
        Q extends PolyvCommonMediacontroller<P>> {
    protected static final String TAG = "PolyvCommonVideoHelper";
    protected Context context;
    protected T videoItem;

    protected ViewGroup pptContianer;
    protected PolyvTouchContainerView pptParent;
    protected PolyvPPTView pptView;

    protected ViewGroup playerParent, playerView;
    protected ViewGroup subVideoviewParent;
    protected P videoView;
    protected PolyvBaseVideoParams playOption;
    protected PolyvAuxiliaryVideoview subVideoview;
    protected Q controller;
    protected View loadingView, noStreamView;
    protected int videoViewVolume;

    protected  static final Handler S_HANDLER;

    private boolean firstSwitchLocation = true;//第一次切换主副屏 不用动画

    static {
        S_HANDLER = new Handler(Looper.getMainLooper());
    }

    public PolyvCommonVideoHelper(T videoItem, PolyvPPTItem polyvPPTItem) {
        this.videoItem = videoItem;
        this.videoView = videoItem.getVideoView();
        this.controller = videoItem.getController();
        this.subVideoview = videoItem.getSubVideoView();
        this.subVideoviewParent = (ViewGroup) subVideoview.getParent();


        this.playerParent = videoItem.getView().findViewById(R.id.rl_top);
        this.playerView = playerParent.findViewById(PolyvBaseVideoView.IJK_VIDEO_ID);
        this.loadingView = playerParent.findViewById(R.id.loadingview);
        this.noStreamView = playerParent.findViewById(R.id.no_stream);
        this.context = playerView.getContext();

        controller.setMediaPlayer(videoView);

        initPPT(videoItem, polyvPPTItem);

    }

    public void initPPT(T videoItem, PolyvPPTItem polyvPPTItem) {
        if(polyvPPTItem != null){
            pptView = (PolyvPPTView) polyvPPTItem.getPPTView();
            pptContianer = polyvPPTItem.getItemRootView().findViewById(R.id.polyv_ppt_container);
            polyvPPTItem.addMediaController(controller);
            videoItem.bindPPTView(polyvPPTItem);

        }
    }


    public void addVideoPlayer(ViewGroup container) {
        container.removeAllViews();
        ViewGroup viewGroup = (ViewGroup) playerParent.getParent();
        if (viewGroup != null)
            viewGroup.removeView(playerParent);
        container.addView(playerParent);

        controller.initialConfig(container);

    }

    public P getVideoView() {
        return videoView;
    }

    public abstract void initConfig(boolean isNormalLive);

    public void addPPT(PolyvTouchContainerView container) {
        if(pptContianer == null){
            return;
        }
        container.removeAllViews();
        ViewGroup viewGroup = (ViewGroup) pptContianer.getParent();
        if (viewGroup != null)
            viewGroup.removeView(pptContianer);
        container.addView(pptContianer);

        pptParent = container;
    }

    /**
     * 交换ppt 与videoview位置
     *
     * @param switchOpen ppt-》videoview 是否交换
     */
    public boolean changePPTViewToVideoView(final boolean switchOpen) {

        S_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                changeView(switchOpen);
            }
        });

        return true;
    }

    private void changeView(boolean changeToVideoView) {
        if(pptContianer == null || pptView == null){
            return;
        }
        PolyvCommonLog.d(TAG,"show ppt sub:"+changeToVideoView);
        pptContianer.removeView(changeToVideoView ? pptView : playerView);
        videoView.removeView(changeToVideoView ? playerView : pptView);

        videoView.addView(changeToVideoView ? pptView : playerView, 0);
        pptContianer.addView(changeToVideoView ? playerView : pptView, 0);

        startAnimation(changeToVideoView?pptView:playerView);

        if (changeToVideoView) {

            if (loadingView != null) {
                videoView.removeView(loadingView);
                pptContianer.addView(loadingView);
            }

            if (noStreamView != null) {
                videoView.removeView(noStreamView);
                pptContianer.addView(noStreamView);
            }
//            playerParent.removeView(subVideoviewParent);
//            pptContianer.addView(subVideoviewParent);
        } else {

            if (loadingView != null) {
                pptContianer.removeView(loadingView);
                videoView.addView(loadingView);
            }
            if (noStreamView != null) {
                pptContianer.removeView(noStreamView);
                videoView.addView(noStreamView);
            }
//            pptContianer.removeView(subVideoviewParent);
//            playerParent.addView(subVideoviewParent);
        }
    }

    protected void startAnimation(View animationView) {
        float originScaleX = (float) PolyvScreenUtils.dip2px(context,144)/ ScreenUtils.getScreenWidth() ;
        AnimatorSet animationSet = new AnimatorSet();


        if(!firstSwitchLocation){
            animationView.setPivotX(0);
            animationView.setPivotY(0);
            ObjectAnimator scaleAnimationX =
                    ObjectAnimator.ofFloat(animationView,"scaleX",originScaleX,1);
            ObjectAnimator scaleAnimationY =
                    ObjectAnimator.ofFloat(animationView,"scaleY",originScaleX,1);
            scaleAnimationX.setDuration(200);
            scaleAnimationY.setDuration(200);
            scaleAnimationX.setInterpolator(new LinearInterpolator());
            scaleAnimationY.setInterpolator(new LinearInterpolator());
            animationSet.playTogether(scaleAnimationX,scaleAnimationY);
            animationSet.start();
        }
        firstSwitchLocation = false;
    }

    public void showCamerView() {
        if(pptParent != null){
            pptParent.setVisibility(View.VISIBLE);
        }
    }

    public void changeToLandscape() {

        if (controller != null) {
            controller.changeToLandscape();
        }
    }

    public void changeToPortrait() {
        if (controller != null) {
            controller.changeToPortrait();
        }
    }

    public void startPlay(PolyvBaseVideoParams polyvBaseVideoParams) {
        playOption = polyvBaseVideoParams;
        if (videoView instanceof IPolyvCloudClassVideoView) {
            videoView.playByMode(polyvBaseVideoParams, PolyvPlayOption.PLAYMODE_LIVE);
        } else if (videoView instanceof PolyvVodVideoView) {
            videoView.playByMode(polyvBaseVideoParams, PolyvPlayOption.PLAYMODE_VOD);
        }
    }

    public void restartPlay() {
        if (playOption == null) {
            return;
        }
        openVideoViewSound();
        startPlay(playOption);
    }

    private void openVideoViewSound() {
        if(videoView != null){
            videoView.setVolume(videoViewVolume);
        }
    }

    public void pause() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
//
//        if(subVideoview != null && subVideoview.isShow()){
//            subVideoview.pause(false);
//        }
    }

    public void resume(){
        if(videoView != null && !videoView.isPlaying()){
            videoView.start();
        }
//
//        if(subVideoview != null && !subVideoview.isPlaying() && subVideoview.isShow()){
//            subVideoview.start();
//        }
    }

    public void destory() {
        PolyvCommonLog.d(TAG, "destroy helper video");
        videoView.destroy();
        controller.destroy();
        videoItem.destroy();

        videoView = null;
        controller = null;
        videoItem = null;

    }
}
