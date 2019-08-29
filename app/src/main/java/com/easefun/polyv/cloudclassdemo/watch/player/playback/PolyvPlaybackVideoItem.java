package com.easefun.polyv.cloudclassdemo.watch.player.playback;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeItem;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeUtils;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeView;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.cloudclass.playback.video.api.IPolyvPlaybackListenerEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.IPolyvHomeProtocol;
import com.easefun.polyv.commonui.player.IPolyvVideoItem;
import com.easefun.polyv.commonui.player.PolyvMediaInfoType;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.widget.PolyvLightTipsView;
import com.easefun.polyv.commonui.player.widget.PolyvProgressTipsView;
import com.easefun.polyv.commonui.player.widget.PolyvVolumeTipsView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvControlUtils;

public class PolyvPlaybackVideoItem extends FrameLayout implements View.OnClickListener,
        IPolyvVideoItem<PolyvPlaybackVideoView, PolyvPlaybackMediaController> {
    private final String TAG = PolyvPlaybackVideoItem.class.getSimpleName();
    private Activity context;
    private PolyvPlaybackVideoView videoView;
    private PolyvPlaybackMediaController controller;
    //载入状态指示器
    private ProgressBar loadingview;
    //准备中状态显示的视图
    private View preparingview;
    //tips view
    private PolyvLightTipsView polyvLightTipsView;
    private PolyvVolumeTipsView tipsviewVolume;
    private PolyvProgressTipsView tipsviewProgress;
    //手势滑动进度
    private int fastForwardPos = 0;

    private View view;
    private PolyvPPTItem polyvPPTItem;
    private View noStreamView;

    /**
     * 跑马灯控件
     */
    private PolyvMarqueeView marqueeView = null;
    private PolyvMarqueeItem marqueeItem = null;
    private PolyvMarqueeUtils marqueeUtils = null;
    private String nickName;

    private IPolyvHomeProtocol polyvHomeProtocol;

    public PolyvPlaybackVideoItem(@NonNull Context context) {
        this(context, null);
    }

    public PolyvPlaybackVideoItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlaybackVideoItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initVideoView();
    }

    private void initView(Context context) {
        if (!(context instanceof Activity))
            throw new RuntimeException("must use activity create videoitem");

        this.context = (Activity) context;
        if(this.context instanceof IPolyvHomeProtocol){
            this.polyvHomeProtocol = (IPolyvHomeProtocol) this.context;
        }
        this.view = LayoutInflater.from(this.context).inflate(R.layout.polyv_playback_video_item, this);
        videoView = (PolyvPlaybackVideoView) findViewById(R.id.pb_videoview);
        controller = (PolyvPlaybackMediaController) findViewById(R.id.controller);
        loadingview = (ProgressBar) findViewById(R.id.loadingview);
        polyvLightTipsView = (PolyvLightTipsView) findViewById(R.id.tipsview_light);
        tipsviewVolume = (PolyvVolumeTipsView) findViewById(R.id.tipsview_volume);
        tipsviewProgress = (PolyvProgressTipsView) findViewById(R.id.tipsview_progress);
        marqueeView = (PolyvMarqueeView) findViewById(R.id.polyv_marquee_view);

        preparingview = findViewById(R.id.preparingview);
        //init controller
        videoView.setMediaController(controller);
        controller.addOtherContolLayout(this);

        noStreamView = findViewById(R.id.no_stream);
        videoView.setNoStreamIndicator(noStreamView);
    }

    private void initVideoView() {
        // 设置跑马灯
        videoView.setMarqueeView(marqueeView, marqueeItem = new PolyvMarqueeItem());
        videoView.setKeepScreenOn(true);
        videoView.setPlayerBufferingIndicator(loadingview);
        videoView.setNeedGestureDetector(true);
        videoView.setOnVideoDownloadListener(new IPolyvPlaybackListenerEvent.OnVideoDownloadListener() {
            @Override
            public void onVideoDownload(boolean canDownload) {
                polyvHomeProtocol.updateVideoDownloadStatus(canDownload);
            }
        });
        videoView.setOnPPTShowListener(new IPolyvVideoViewListenerEvent.OnPPTShowListener() {
            @Override
            public void showPPTView(int visiable) {
                if (polyvPPTItem != null) {
                    polyvPPTItem.show(visiable);
                }
            }

            @Override
            public void showNoPPTLive(boolean showPPT) {

            }
        });
        videoView.setOnPreparedListener(new IPolyvVideoViewListenerEvent.OnPreparedListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onPreparing() {
                preparingview.setVisibility(View.VISIBLE);
                PolyvCommonLog.i(TAG, "onPreparing");
            }
        });
        videoView.setOnVideoPlayListener(new IPolyvVideoViewListenerEvent.OnVideoPlayListener() {
            @Override
            public void onPlay(boolean isFirst) {
                preparingview.setVisibility(View.GONE);
                PolyvCommonLog.i(TAG, "onPlay：" + isFirst);
            }
        });
        videoView.setOnVideoPauseListener(new IPolyvVideoViewListenerEvent.OnVideoPauseListener() {
            @Override
            public void onPause() {
                PolyvCommonLog.i(TAG, "onPause");
            }
        });

        videoView.setOnCompletionListener(new IPolyvVideoViewListenerEvent.OnCompletionListener() {
            @Override
            public void onCompletion() {
                PolyvCommonLog.i(TAG, "onCompletion");
            }
        });
        videoView.setOnErrorListener(new IPolyvVideoViewListenerEvent.OnErrorListener() {
            @Override
            public void onError(int what, int extra) {

            }

            @Override
            public void onError(PolyvPlayError error) {
                String tips = error.playStage == PolyvPlayError.PLAY_STAGE_HEADAD ? "片头广告"
                        : error.playStage == PolyvPlayError.PLAY_STAGE_TAILAD ? "片尾广告"
                        : error.playStage == PolyvPlayError.PLAY_STAGE_TEASER ? "暖场视频"
                        : error.isMainStage() ? "主视频" : "";
                if (error.isMainStage()) {
                    preparingview.setVisibility(View.GONE);
                }
                Toast.makeText(context, tips + "播放异常\n" + error.errorDescribe + "(" + error.errorCode + "-" + error.playStage + ")\n" + error.playPath, Toast.LENGTH_SHORT).show();
            }
        });
        videoView.setOnInfoListener(new IPolyvVideoViewListenerEvent.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                if (what == PolyvMediaInfoType.MEDIA_INFO_BUFFERING_START) {
                    PolyvCommonLog.i(TAG, "开始缓冲");
                } else if (what == PolyvMediaInfoType.MEDIA_INFO_BUFFERING_END) {
                    PolyvCommonLog.i(TAG, "缓冲结束");
                }
            }
        });
        videoView.setOnGestureDoubleClickListener(new IPolyvVideoViewListenerEvent.OnGestureDoubleClickListener() {
            @Override
            public void callback() {
                if (videoView.isInPlaybackStateEx()) {
                    controller.playOrPause();
                }
            }
        });
        videoView.setOnGestureLeftDownListener(new IPolyvVideoViewListenerEvent.OnGestureLeftDownListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int brightness = videoView.getBrightness(context) - 8;
                if (brightness < 0) {
                    brightness = 0;
                }
                if (start)
                    videoView.setBrightness(context, brightness);
                polyvLightTipsView.setLightPercent(brightness, end);
            }
        });
        videoView.setOnGestureLeftUpListener(new IPolyvVideoViewListenerEvent.OnGestureLeftUpListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int brightness = videoView.getBrightness(context) + 8;
                if (brightness > 100) {
                    brightness = 100;
                }
                if (start)
                    videoView.setBrightness(context, brightness);
                polyvLightTipsView.setLightPercent(brightness, end);
            }
        });
        videoView.setOnGestureRightDownListener(new IPolyvVideoViewListenerEvent.OnGestureRightDownListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int volume = videoView.getVolume() - PolyvControlUtils.getVolumeValidProgress(context, 8);
                if (volume < 0) {
                    volume = 0;
                }
                if (start)
                    videoView.setVolume(volume);
                tipsviewVolume.setVolumePercent(volume, end);
            }
        });
        videoView.setOnGestureRightUpListener(new IPolyvVideoViewListenerEvent.OnGestureRightUpListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int volume = videoView.getVolume() + PolyvControlUtils.getVolumeValidProgress(context, 8);
                if (volume > 100) {
                    volume = 100;
                }
                if (start)
                    videoView.setVolume(volume);
                tipsviewVolume.setVolumePercent(volume, end);
            }
        });
        videoView.setOnGestureSwipeLeftListener(new IPolyvVideoViewListenerEvent.OnGestureSwipeLeftListener() {
            @Override
            public void callback(boolean start, boolean end, int times) {
                if (videoView.isInPlaybackStateEx() && videoView.isVodPlayMode()) {
                    if (fastForwardPos == 0) {
                        fastForwardPos = videoView.getCurrentPosition();
                    }
                    if (end) {
                        if (fastForwardPos < 0)
                            fastForwardPos = 0;
                        videoView.seekTo(fastForwardPos);
                        if (videoView.isCompletedState()) {
                            videoView.start();
                        }
                        fastForwardPos = 0;
                    } else {
                        fastForwardPos -= 1000 * times;
                        if (fastForwardPos <= 0)
                            fastForwardPos = -1;
                    }
                    tipsviewProgress.setProgressPercent(fastForwardPos, videoView.getDuration(), end, false);
                } else if (end) {
                    fastForwardPos = 0;
                    tipsviewProgress.delayHide();
                }
            }
        });
        videoView.setOnGestureSwipeRightListener(new IPolyvVideoViewListenerEvent.OnGestureSwipeRightListener() {
            @Override
            public void callback(boolean start, boolean end, int times) {
                if (videoView.isInPlaybackStateEx() && videoView.isVodPlayMode()) {
                    if (fastForwardPos == 0) {
                        fastForwardPos = videoView.getCurrentPosition();
                    }
                    if (end) {
                        if (fastForwardPos > videoView.getDuration())
                            fastForwardPos = videoView.getDuration();
                        if (!videoView.isCompletedState()) {
                            videoView.seekTo(fastForwardPos);
                        } else if (fastForwardPos < videoView.getDuration()) {
                            videoView.seekTo(fastForwardPos);
                            videoView.start();
                        }
                        fastForwardPos = 0;
                    } else {
                        fastForwardPos += 1000 * times;
                        if (fastForwardPos > videoView.getDuration())
                            fastForwardPos = videoView.getDuration();
                    }
                    tipsviewProgress.setProgressPercent(fastForwardPos, videoView.getDuration(), end, true);
                } else if (end) {
                    fastForwardPos = 0;
                    tipsviewProgress.delayHide();
                }
            }


        });

        videoView.setOnGetMarqueeVoListener(new IPolyvVideoViewListenerEvent.OnGetMarqueeVoListener() {
            @Override
            public void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo) {
                if (marqueeUtils == null)
                    marqueeUtils = new PolyvMarqueeUtils();
                // 更新为后台设置的跑马灯类型
                marqueeUtils.updateMarquee(context, marqueeVo,
                        marqueeItem, nickName);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_skip) {
            if (videoView.canPlaySkipHeadAd()) {
                resetUI();
                videoView.playSkipHeadAd(false);
            } else {
                Toast.makeText(context, "跳过广告播放失败，当前没有播放片头广告", Toast.LENGTH_SHORT).show();
                PolyvCommonLog.i(TAG, "跳过广告播放失败，当前没有播放片头广告" + "&PlayOption：");
            }

        }
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public PolyvPlaybackVideoView getVideoView() {
        return videoView;
    }

    @Override
    public PolyvAuxiliaryVideoview getSubVideoView() {
        return null;
    }

    @Override
    public PolyvPlaybackMediaController getController() {
        return controller;
    }

    //每次调用播放器的videoView.playxx方法前，都需要重置ui
    @Override
    public void resetUI() {
        preparingview.setVisibility(View.GONE);
        controller.hideUI();
    }

    @Override
    public void bindPPTView(PolyvPPTItem polyvPPTItem) {

        this.polyvPPTItem = polyvPPTItem;
        if (videoView != null && polyvPPTItem != null) {
            videoView.bindPPTView(polyvPPTItem.getPPTView());
        }
    }

    @Override
    public PolyvPPTItem getPPTItem() {
        return polyvPPTItem;
    }

    @Override
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public void destroy() {

        if (polyvPPTItem != null && polyvPPTItem.getPPTView() != null) {
            polyvPPTItem.getPPTView().destroy();
            polyvPPTItem = null;
        }

        if (polyvLightTipsView != null) {
            polyvLightTipsView.removeAllViews();
            polyvLightTipsView = null;
        }

        if (tipsviewVolume != null) {
            tipsviewVolume.removeAllViews();
            tipsviewVolume = null;
        }

        polyvHomeProtocol = null;
    }

}
