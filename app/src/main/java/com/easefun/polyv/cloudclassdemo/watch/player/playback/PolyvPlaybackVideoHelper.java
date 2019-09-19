package com.easefun.polyv.cloudclassdemo.watch.player.playback;

import android.view.MotionEvent;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTCacheProcessor;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTVodProcessor;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvPlaybackVideoParams;
import com.easefun.polyv.businesssdk.web.IPolyvWebMessageProcessor;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.commonui.PolyvCommonVideoHelper;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import java.io.File;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PolyvPlaybackVideoHelper extends PolyvCommonVideoHelper<PolyvPlaybackVideoItem,
        PolyvPlaybackVideoView, PolyvPlaybackMediaController> {
    private static final String TAG = PolyvPlaybackVideoHelper.class.getSimpleName();
    private boolean isPlayingOnStop;
    private boolean isNormalLivePlayback;

    public PolyvPlaybackVideoHelper(PolyvPlaybackVideoItem videoItem, PolyvPPTItem polyvPPTItem) {
        super(videoItem, polyvPPTItem);
    }

    @Override
    public void initConfig(boolean isNoramlLivePlayBack) {
        this.isNormalLivePlayback = isNoramlLivePlayBack;
        controller.addHelper(this);
        controller.updatePPTShowStatus(!isNoramlLivePlayBack);
        if (!isNoramlLivePlayBack) {
            controller.changePPTVideoLocation();
        }
    }

    @Override
    public void resetView(boolean isNoramlLivePlayBack) {
        if (isNormalLivePlayback == isNoramlLivePlayBack)
            return;
        this.isNormalLivePlayback = isNoramlLivePlayBack;
        controller.updatePPTShowStatus(!isNoramlLivePlayBack);
        if (isNoramlLivePlayBack) {
            //把ppt切到副屏，并隐藏其父布局
            controller.switchPPTToScreen(true);
            PolyvPPTItem pptItem = videoItem.getPPTItem();
            if (pptItem != null) {
                //重置状态
                pptItem.resetStatus();
                pptItem.show(View.INVISIBLE);
            }
        } else {
            //如果还没ppt，则把ppt加上
            if (pptContianer == null) {
                initPPT(videoItem, new PolyvPPTItem(context));
                addPPT(pptParent);
            }
            //添加ppt后再获取pptItem
            PolyvPPTItem pptItem = videoItem.getPPTItem();
            //把ppt切到主屏
            controller.switchPPTToScreen(false);
            if (pptItem != null) {
                //重置状态
                pptItem.resetStatus();
            }
        }
    }

    @Override
    protected void addCloudClassWebProcessor() {
        if (pptView != null) {
            IPolyvWebMessageProcessor<PolyvPPTVodProcessor.PolyvVideoPPTCallback> processor = new
                    PolyvPPTCacheProcessor(null);
            pptView.addWebProcessor(processor);
            processor.registerJSHandler(new PolyvPPTVodProcessor.PolyvVideoPPTCallback() {
                @Override
                public void callVideoDuration(CallBackFunction function) {
                    PolyvCommonLog.d(TAG, "callVideoDuration:");
                    if (videoView == null) {
                        return;
                    }
                    String time = "{\"time\":" + videoView.getCurrentPosition() + "}";
                    PolyvCommonLog.d(TAG, "time:" + time);
                    function.onCallBack(time);
                }

                @Override
                public void pptPrepare() {
                    pptView.setLoadingViewVisible(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public void pause() {
        videoView.pause();
    }

    @Override
    public void resume() {
        super.resume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }

    public void startLocal(String videoPath, PolyvPlaybackVideoParams playbackVideoParams) {
        videoView.playLocalVideo(videoPath, playbackVideoParams);
    }

    /**
     * 开始本地播放
     *
     * @param unzipDownloadedFile 解压后的下载文件，里面包括ppt，js，video。
     * @param videoId             视频在直播中的id
     * @param playbackVideoParams 播放参数
     */
    public void startLocal(File unzipDownloadedFile, String videoId, PolyvPlaybackVideoParams playbackVideoParams) {
        //从文件名取出videoPoolId。
        String videoPoolId = unzipDownloadedFile.getName();

        //取出视频文件
        File videoDir = new File(unzipDownloadedFile, "video/");
        File firstBitrateDir = videoDir.listFiles()[0];
        File videoFile = firstBitrateDir.listFiles()[0];

        //给播放器设置本地视频路径
        startLocal(videoFile.getAbsolutePath(), playbackVideoParams);

        //给pptView设置js文件路径
        videoItem.getPPTItem().loadFromLocal(unzipDownloadedFile, videoPoolId, videoId);
    }

    public void stopPlay() {
        videoView.stopPlay();
    }

    public void setOptionPlay(PolyvBaseVideoParams params, int mode) {
        videoItem.resetUI();
        videoView.playByMode(params, mode);
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
        isPlayingOnStop = videoView.isPlaying();
        if (videoView.isBackgroundPlayEnabled())
            videoView.enterBackground();
        else
            videoView.pause();
        IjkMediaPlayer.native_profileEnd();
    }

}
