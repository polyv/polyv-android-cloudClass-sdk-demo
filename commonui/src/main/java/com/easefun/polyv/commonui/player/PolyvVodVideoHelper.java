package com.easefun.polyv.commonui.player;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTVodProcessor;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodVideoView;
import com.easefun.polyv.businesssdk.web.IPolyvWebMessageProcessor;
import com.easefun.polyv.commonui.PolyvCommonVideoHelper;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.widget.PolyvVodMediaController;
import com.easefun.polyv.commonui.player.widget.PolyvVodVideoItem;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PolyvVodVideoHelper extends PolyvCommonVideoHelper<PolyvVodVideoItem,
        PolyvVodVideoView,PolyvVodMediaController>{
    private static final String TAG = PolyvVodVideoHelper.class.getSimpleName();
    private int playPosition = -1;
    private boolean isPlayingOnStop;

    public PolyvVodVideoHelper(PolyvVodVideoItem videoItem, PolyvPPTItem polyvPPTItem) {
        super(videoItem, polyvPPTItem);
    }

    @Override
    public void initConfig(boolean isNoramlLivePlayBack) {

        controller.addHelper(this);
        controller.updatePPTShowStatus(!isNoramlLivePlayBack);
        if(!isNoramlLivePlayBack){
            controller.changePPTVideoLocation();
        }
    }

    @Override
    public void resetView(boolean isNoramlLivePlayBack) {

    }

    @Override
    protected void addCloudClassWebProcessor() {
        if(pptView != null){
            IPolyvWebMessageProcessor<PolyvPPTVodProcessor.PolyvVideoPPTCallback> processor = new
                    PolyvPPTVodProcessor(null);
            pptView.addWebProcessor(processor);
            processor.registerJSHandler(new PolyvPPTVodProcessor.PolyvVideoPPTCallback() {
                @Override
                public void callVideoDuration(CallBackFunction function) {
                    PolyvCommonLog.d(TAG,"callVideoDuration:");
                    if (videoView == null) {
                        return;
                    }
                    String time = "{\"time\":" + videoView.getCurrentPosition() + "}";
                    PolyvCommonLog.d(TAG,"time:"+time);
                    function.onCallBack(time);
                }

                @Override
                public void pptPrepare() {
                    pptView.setLoadingViewVisible(View.INVISIBLE);
                }
            });
        }
    }

    public void pause() {
        videoView.pause();
    }

    public void stopPlay() {
        videoView.stopPlay();
    }

    public void setOptionPlay(PolyvBaseVideoParams params,int mode) {
        videoItem.resetUI();
        videoView.playByMode(params,mode);
//        videoView.playFromHeadAd();
    }

    public void playFromHeadAd() {
        videoItem.resetUI();
        videoView.playFromHeadAd();
    }


    public void playTeaser() {
        if (subVideoview.isOpenTeaser()) {
            videoItem.resetUI();
            videoView.playTeaser();
        } else {
            Toast.makeText(context, "播放失败，没有开启暖场视频", Toast.LENGTH_SHORT).show();
            PolyvCommonLog.i(TAG, "播放失败，没有开启暖场视频" + "&PlayOption：");
        }
    }

    public void playTailAd() {
        if (subVideoview.isOpenTailAd()) {
            videoItem.resetUI();
            videoView.playTailAd();
        } else {
            Toast.makeText(context, "播放失败，没有开启片尾广告", Toast.LENGTH_SHORT).show();
            PolyvCommonLog.i(TAG, "播放失败，没有开启片尾广告" + "&PlayOption：" );
        }
    }

    public boolean hideUI(MotionEvent ev) {
        return controller.hideUI(ev);
    }

    public boolean hideUI() {
        return controller.hideUI();
    }

    public void changeToPortrait() {
        controller.changeToPortrait();
    }

    public void changeToLandscape() {
        controller.changeToLandscape();
    }

    public void onRestart() {
        if (!videoView.isBackgroundPlayEnabled()) {
            if (isPlayingOnStop) {
                videoView.start();
            }
        }
    }

    public void onStop() {
        isPlayingOnStop = videoView.isPlaying() || subVideoview.isShow();
        if (videoView.isBackgroundPlayEnabled())
            videoView.enterBackground();
        else
            videoView.pause();
        IjkMediaPlayer.native_profileEnd();
    }

}
