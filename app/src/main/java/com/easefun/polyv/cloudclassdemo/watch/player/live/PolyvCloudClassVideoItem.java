package com.easefun.polyv.cloudclassdemo.watch.player.live;

import android.content.Context;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyv.thirdpart.blankj.utilcode.util.TimeUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.businesssdk.api.auxiliary.IPolyvAuxiliaryVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.link.PolyvMicphoneStatus;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeItem;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeUtils;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeView;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketSliceControlVO;
import com.easefun.polyv.cloudclass.model.PolyvTeacherStatusInfo;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassListenerEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.liveInfo.PolyvLiveInfoFragment;
import com.easefun.polyv.cloudclassdemo.watch.danmu.PolyvDanmuFragment;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.IPolyvLandscapeDanmuSender;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvCloudClassAudioModeView;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvLandscapeDanmuSendPanel;
import com.easefun.polyv.commonui.player.IPolyvVideoItem;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.widget.PolyvLightTipsView;
import com.easefun.polyv.commonui.player.widget.PolyvLoadingLayout;
import com.easefun.polyv.commonui.player.widget.PolyvVolumeTipsView;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvControlUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.OPEN_MICROPHONE;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_N0_PPT;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_NO_STREAM;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_OPEN_PPT;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_START;


/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public class PolyvCloudClassVideoItem extends FrameLayout
        implements IPolyvVideoItem<PolyvCloudClassVideoView, PolyvCloudClassMediaController>, View.OnClickListener {

    private static final String TAG = "PolyvCloudClassVideoIte";
    private AppCompatActivity context;
    private PolyvCloudClassMediaController controller;
    private PolyvCloudClassVideoView polyvCloudClassVideoView;
    //tips view
    private PolyvLightTipsView tipsviewLight;
    private PolyvVolumeTipsView tipsviewVolume;
    //手势滑动进度
    private RelativeLayout rlTop;
    private PolyvLoadingLayout loadingview;
    private TextView preparingview;
    private View noStream;
    private View rootView;
    private ImageView subBackLand;
    private FrameLayout flSubBackAndGradient;
    private FrameLayout audioModeLayoutRoot;

    //直播倒计时View
    private TextView tvStartTimeCountDown;

    //截图，用于刷新直播的时候防止黑屏
    private ImageView ivScreenshot;

    //只听音频View
    private IPolyvCloudClassAudioModeView audioModeView;

    //开始时间倒计时器
    private CountDownTimer startTimeCountDown;
    //直播开始时间
    private String liveStartTime;


    /**
     * 弹幕发送弹窗
     */
    private IPolyvLandscapeDanmuSender landscapeDanmuSender;
    /**
     * 跑马灯控件
     */
    private PolyvMarqueeView marqueeView = null;
    private PolyvMarqueeItem marqueeItem = null;
    private PolyvMarqueeUtils marqueeUtils = null;
    /**
     * ------------------sub---------------------------
     **/
    private PolyvAuxiliaryVideoview subVideoview;
    private ProgressBar subLoadingview;
    private TextView subPreparingview;
    private TextView tvCountdown;
    private TextView tvSkip;
    private PolyvDanmuFragment danmuFragment;
    private String nickName;

    /**** --------------------- ppt --------------------***/
    private PolyvPPTItem polyvPPTItem;


    private static final String NICK_NAME = "nick_name";

    private boolean isNoLiveAtPresent;

    private Runnable hideTask = new Runnable() {
        @Override
        public void run() {
            PolyvCommonLog.d(TAG, "hideTask");
            flSubBackAndGradient.setVisibility(INVISIBLE);
        }
    };

    private IPolyvVideoViewListenerEvent.OnGestureClickListener onGestureClickListener =
            new IPolyvVideoViewListenerEvent.OnGestureClickListener() {
                @Override
                public void callback(boolean start, boolean end) {
                    //暖场播放 或者是 非直播  非暖场下  显示返回按钮
                    if (polyvCloudClassVideoView != null && !polyvCloudClassVideoView.isPlaying()) {
                        if (flSubBackAndGradient.isShown()) {
                            flSubBackAndGradient.setVisibility(INVISIBLE);
                        } else {
                            showSubLandBack();
                        }
                    }

                }
            };
    private Disposable messageDispose;

    public PolyvCloudClassVideoItem(@NonNull Context context) {
        this(context, null);
    }

    public PolyvCloudClassVideoItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PolyvCloudClassVideoItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        registerSocketMessage();
        initialData();
        initialView();
        initialSubView();
        initialVideoView();

    }


    private void initialData() {
        context = (AppCompatActivity) getContext();
        nickName = context.getIntent().getStringExtra(NICK_NAME);
    }

    private void initialSubView() {

        subVideoview = findViewById(R.id.sub_videoview);
        flSubBackAndGradient = findViewById(R.id.fl_sub_back_gradient);
        subBackLand = findViewById(R.id.sub_video_back_land);
        subLoadingview = findViewById(R.id.sub_loadingview);
        subPreparingview = findViewById(R.id.sub_preparingview);
        tvCountdown = findViewById(R.id.tv_countdown);
        tvSkip = findViewById(R.id.tv_skip);

        subBackLand.setOnClickListener(this);
        subVideoview.setOnGestureClickListener(onGestureClickListener);
        subVideoview.setOnSubVideoViewLoadImage(new IPolyvAuxiliaryVideoViewListenerEvent.IPolyvOnSubVideoViewLoadImage() {
            @Override
            public void onLoad(String imageUrl, ImageView imageView) {
                PolyvImageLoader.getInstance().loadImage(getContext(), imageUrl, imageView);
            }
        });
    }


    private void showSubLandBack() {
        PolyvCommonLog.d(TAG, "showSubLandBack");
        flSubBackAndGradient.setVisibility(VISIBLE);
        flSubBackAndGradient.removeCallbacks(hideTask);
        flSubBackAndGradient.postDelayed(hideTask, 5000);
    }

    private void initialView() {

        rootView = View.inflate(context, R.layout.polyv_cloudclass_item, this);

        ivScreenshot = findViewById(R.id.iv_screenshot);

        rlTop = findViewById(R.id.rl_top);
        loadingview = findViewById(R.id.loadingview);
        preparingview = findViewById(R.id.preparingview);
        tipsviewLight = findViewById(R.id.tipsview_light);
        tipsviewVolume = findViewById(R.id.tipsview_volume);
        noStream = findViewById(R.id.no_stream);
        audioModeLayoutRoot = findViewById(R.id.fl_audio_mode_layout_root);
        marqueeView = findViewById(R.id.polyv_marquee_view);
        tvStartTimeCountDown = findViewById(R.id.tv_start_time_count_down);

        FragmentTransaction fragmentTransaction = context.getSupportFragmentManager().beginTransaction();
        danmuFragment = new PolyvDanmuFragment();
        fragmentTransaction.add(R.id.fl_danmu, danmuFragment, "danmuFragment").commit();

        landscapeDanmuSender = new PolyvLandscapeDanmuSendPanel(context, this);

        controller = findViewById(R.id.controller);
        controller.setOnClickOpenStartSendDanmuListener(new PolyvCloudClassMediaController.OnClickOpenStartSendDanmuListener() {
            @Override
            public void onStartSendDanmu() {
                controller.hide();
                landscapeDanmuSender.openDanmuSender();
            }
        });
        controller.setDanmuFragment(danmuFragment);

        //只听音频View
        PolyvCloudClassAudioModeView audioViewImpl = new PolyvCloudClassAudioModeView(getContext());
        audioViewImpl.setOnChangeVideoModeListener(new PolyvCloudClassAudioModeView.OnChangeVideoModeListener() {
            @Override
            public void onClickPlayVideo() {
                polyvCloudClassVideoView.changeMediaPlayMode(PolyvMediaPlayMode.MODE_VIDEO);
                controller.changeAudioOrVideoMode(PolyvMediaPlayMode.MODE_VIDEO);
            }
        });
        audioModeView = audioViewImpl;
        audioModeLayoutRoot.addView(audioModeView.getRoot(), LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        //打开弹幕下的竖屏
//        controller.enableDanmuInPortrait();
    }

    private void initialVideoView() {
        polyvCloudClassVideoView = findViewById(R.id.cloudschool_videoview);
        polyvCloudClassVideoView.setAudioModeView(audioModeView);
        polyvCloudClassVideoView.setMediaController(controller);
        polyvCloudClassVideoView.setNoStreamIndicator(noStream);
        polyvCloudClassVideoView.setPlayerBufferingIndicator(loadingview);
        loadingview.bindVideoView(polyvCloudClassVideoView);
        polyvCloudClassVideoView.setSubVideoView(subVideoview);
        // 设置跑马灯
        polyvCloudClassVideoView.setMarqueeView(marqueeView, marqueeItem = new PolyvMarqueeItem());

        polyvCloudClassVideoView.setOnErrorListener(new IPolyvVideoViewListenerEvent.OnErrorListener() {
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

                showDefaultIcon();

                Toast.makeText(context, tips + "播放异常\n" + error.errorDescribe + " (errorCode:" + error.errorCode +
                        "-" + error.playStage + ")\n" + error.playPath, Toast.LENGTH_LONG).show();
            }
        });
        polyvCloudClassVideoView.setOnVideoViewRestartListener(new IPolyvVideoViewListenerEvent.OnVideoViewRestart() {
            @Override
            public void restartLoad(boolean restart) {
                if (polyvPPTItem != null && polyvPPTItem.getPPTView() != null) {
                    polyvPPTItem.getPPTView().reLoad();
                }
            }
        });
        polyvCloudClassVideoView.setOnPreparedListener(new IPolyvVideoViewListenerEvent.OnPreparedListener() {
            @Override
            public void onPrepared() {

                isNoLiveAtPresent = false;
                hideScreenShotView();
                controller.show();
                controller.onVideoViewPrepared();

                sendVideoStartMessage();

                controller.handsUpAuto();

                stopLiveCountDown();
            }

            @Override
            public void onPreparing() {

            }
        });
        polyvCloudClassVideoView.setOnPPTShowListener(new IPolyvVideoViewListenerEvent.OnPPTShowListener() {
            @Override
            public void showPPTView(int visible) {
                if (visible == VISIBLE) {
                    controller.switchPPTToMainScreen();
                }
                notifyTeacherInfoShow(visible == VISIBLE);
                if (polyvPPTItem != null) {
                    polyvPPTItem.show(visible);
                }
            }

            @Override
            public void showNoPPTLive(boolean showPPT) {
                if (!showPPT && controller != null && polyvPPTItem != null && polyvPPTItem.getPPTView() != null) {
                    polyvPPTItem.getPPTView().getView().setVisibility(INVISIBLE);
                    controller.changePPTVideoLocation();
                }
            }
        });
        polyvCloudClassVideoView.setOnCameraShowListener(new IPolyvCloudClassListenerEvent.OnCameraShowListener() {
            @Override
            public void cameraOpen(boolean open) {
                if (!open) {
                    if (polyvPPTItem != null) {
                        polyvPPTItem.hideSubView();
                    }
                }
            }
        });
        polyvCloudClassVideoView.setOnGestureLeftDownListener(new IPolyvVideoViewListenerEvent.OnGestureLeftDownListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int brightness = polyvCloudClassVideoView.getBrightness(context) - 8;
                if (brightness < 0) {
                    brightness = 0;
                }
                if (start)
                    polyvCloudClassVideoView.setBrightness(context, brightness);
                tipsviewLight.setLightPercent(brightness, end);
            }
        });
        polyvCloudClassVideoView.setOnGestureLeftUpListener(new IPolyvVideoViewListenerEvent.OnGestureLeftUpListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int brightness = polyvCloudClassVideoView.getBrightness(context) + 8;
                if (brightness > 100) {
                    brightness = 100;
                }
                if (start)
                    polyvCloudClassVideoView.setBrightness(context, brightness);
                tipsviewLight.setLightPercent(brightness, end);
            }
        });
        polyvCloudClassVideoView.setOnGestureRightDownListener(new IPolyvVideoViewListenerEvent.OnGestureRightDownListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int volume = polyvCloudClassVideoView.getVolume() - PolyvControlUtils.getVolumeValidProgress(context, 8);
                if (volume < 0) {
                    volume = 0;
                }
                if (start)
                    polyvCloudClassVideoView.setVolume(volume);
                tipsviewVolume.setVolumePercent(volume, end);
            }
        });
        polyvCloudClassVideoView.setOnGestureRightUpListener(new IPolyvVideoViewListenerEvent.OnGestureRightUpListener() {
            @Override
            public void callback(boolean start, boolean end) {
                int volume = polyvCloudClassVideoView.getVolume() + PolyvControlUtils.getVolumeValidProgress(context, 8);
                if (volume > 100) {
                    volume = 100;
                }
                if (start)
                    polyvCloudClassVideoView.setVolume(volume);
                tipsviewVolume.setVolumePercent(volume, end);
            }
        });
        polyvCloudClassVideoView.setOnGestureSwipeLeftListener(new IPolyvVideoViewListenerEvent.OnGestureSwipeLeftListener() {
            @Override
            public void callback(boolean start, boolean end, int times) {
            }

        });
        polyvCloudClassVideoView.setOnGestureSwipeRightListener(new IPolyvVideoViewListenerEvent.OnGestureSwipeRightListener() {
            @Override
            public void callback(boolean start, boolean end, int times) {
            }

        });

        polyvCloudClassVideoView.setOnGetMarqueeVoListener(new IPolyvCloudClassListenerEvent.OnGetMarqueeVoListener() {
            @Override
            public void onGetMarqueeVo(PolyvLiveMarqueeVO marqueeVo) {
                if (marqueeUtils == null)
                    marqueeUtils = new PolyvMarqueeUtils();
                // 更新为后台设置的跑马灯类型
                marqueeUtils.updateMarquee(context, marqueeVo,
                        marqueeItem, nickName);
            }
        });

        polyvCloudClassVideoView.setMicroPhoneListener(new IPolyvCloudClassListenerEvent.MicroPhoneListener() {
            @Override
            public void showMicPhoneLine(int visiable) {
                PolyvCommonLog.d(TAG, "showMicPhoneLine");
                if (controller != null) {
                    controller.showMicPhoneLine(visiable);
                }

//                if(visiable == INVISIBLE){//关闭连麦
//                    PolyvLinkMicWrapper.getInstance().leaveChannel();
//                }

                PolyvTeacherStatusInfo live = new PolyvTeacherStatusInfo();
                live.setWatchStatus(visiable == INVISIBLE ?
                        PolyvLiveClassDetailVO.LiveStatus.LIVE_CLOSECALLLINKMIC:
                        PolyvLiveClassDetailVO.LiveStatus.LIVE_OPENCALLLINKMIC);
                PolyvRxBus.get().post(live);
            }
        });
        polyvCloudClassVideoView.setOnNoLiveAtPresentListener(new IPolyvCloudClassListenerEvent.OnNoLiveAtPresentListener() {
            @Override
            public void onNoLiveAtPresent() {
                isNoLiveAtPresent = true;
                ToastUtils.showShort("暂无直播");

                PolyvTeacherStatusInfo dataBean = new PolyvTeacherStatusInfo();
                dataBean.setWatchStatus(LIVE_NO_STREAM);
                PolyvRxBus.get().post(dataBean);
            }

            @Override
            public void onLiveEnd() {
                PolyvLiveClassDetailVO.DataBean dataBean = new PolyvLiveClassDetailVO.DataBean();
                dataBean.setWatchStatus(PolyvLiveInfoFragment.WATCH_STATUS_END);
                PolyvRxBus.get().post(dataBean);

                PolyvLinkMicWrapper.getInstance().leaveChannel();

                startLiveTimeCountDown(liveStartTime);
            }
        });
        polyvCloudClassVideoView.setOnGestureClickListener(new IPolyvVideoViewListenerEvent.OnGestureClickListener() {
            @Override
            public void callback(boolean start, boolean end) {
                //如果当前没有直播，才会将单击事件传递，并显示没有直播时的按钮。
                if (!polyvCloudClassVideoView.isOnline()) {
                    onGestureClickListener.callback(start, end);
                }
            }
        });

        polyvCloudClassVideoView.setOnDanmuServerOpenListener(new IPolyvCloudClassListenerEvent.OnDanmuServerOpenListener() {
            @Override
            public void onDanmuServerOpenListener(boolean isServerDanmuOpen) {
                controller.onServerDanmuOpen(isServerDanmuOpen);
            }
        });

        polyvCloudClassVideoView.setOnLinesChangedListener(new IPolyvCloudClassListenerEvent.OnLinesChangedListener() {
            @Override
            public void OnLinesChanged(int pos) {
                controller.updateMoreLayout(pos);
            }
        });
    }

    private void sendVideoStartMessage() {
        if(!isJoinLinkMic){
            PolyvTeacherStatusInfo statusInfo =new PolyvTeacherStatusInfo();
            statusInfo.setWatchStatus(LIVE_START);
            PolyvRxBus.get().post(statusInfo);
        }

        PolyvLiveClassDetailVO.DataBean dataBean = new PolyvLiveClassDetailVO.DataBean();
        dataBean.setWatchStatus(PolyvLiveInfoFragment.WATCH_STATUS_LIVE);
        PolyvRxBus.get().post(dataBean);
    }

    protected void notifyTeacherInfoShow(boolean show) {
        PolyvTeacherStatusInfo dataBean = new PolyvTeacherStatusInfo();
        dataBean.setWatchStatus(show?LIVE_OPEN_PPT:LIVE_N0_PPT);
        PolyvRxBus.get().post(dataBean);
    }

    public void showDefaultIcon() {
        if (loadingview != null) {
            loadingview.setVisibility(GONE);
        }
        if (noStream != null) {
            noStream.setVisibility(VISIBLE);
        }
    }


    @Override
    public View getView() {
        return rootView;
    }

    @Override
    public PolyvCloudClassVideoView getVideoView() {
        return polyvCloudClassVideoView;
    }

    @Override
    public PolyvAuxiliaryVideoview getSubVideoView() {
        return subVideoview;
    }

    @Override
    public PolyvCloudClassMediaController getController() {
        return controller;
    }

    public View getAudioModeView() {
        return audioModeLayoutRoot;
    }

    public View getScreenShotView() {
        return ivScreenshot;
    }

    @Override
    public void resetUI() {

    }

    @Override
    public void bindPPTView(PolyvPPTItem polyvPPTItem) {
        this.polyvPPTItem = polyvPPTItem;
    }

    @Override
    public PolyvPPTItem getPPTItem() {
        return polyvPPTItem;
    }

    @Override
    public void destroy() {
        if (messageDispose != null) {
            messageDispose.dispose();
        }
        if (polyvPPTItem != null && polyvPPTItem.getPPTView() != null) {
            polyvPPTItem.getPPTView().destroy();
            polyvPPTItem.removeAllViews();
            polyvPPTItem = null;
        }
        if (tipsviewLight != null) {
            tipsviewLight.removeAllViews();
            tipsviewLight = null;
        }

        if (tipsviewVolume != null) {
            tipsviewVolume.removeAllViews();
            tipsviewVolume = null;
        }

        if (danmuFragment != null) {
            danmuFragment.release();
            danmuFragment = null;
        }

        if (landscapeDanmuSender != null) {
            landscapeDanmuSender.dismiss();
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.sub_video_back_land) {
            if (controller != null && ScreenUtils.isLandscape()) {
                controller.changeToPortrait();
            } else {
                context.finish();
            }

        }
    }

    //设置横屏发送弹幕监听器
    public void setOnSendDanmuListener(IPolyvLandscapeDanmuSender.OnSendDanmuListener onSendDanmuListener) {
        landscapeDanmuSender.setOnSendDanmuListener(onSendDanmuListener);
    }

    //横屏发送弹幕消息
    public void sendDanmuMessage(CharSequence message) {
        if (danmuFragment != null) {
            danmuFragment.sendDanmaku(message);
        }
    }

    //开始直播倒计时
    public void startLiveTimeCountDown(String startTime) {
        this.liveStartTime = startTime;
        //2019/08/01 12:22:00

        if (TextUtils.isEmpty(startTime)) {
            tvStartTimeCountDown.setVisibility(View.GONE);
            return;
        }
        tvStartTimeCountDown.setVisibility(View.VISIBLE);

        //解析时间
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        long startTimeMillis = TimeUtils.string2Millis(startTime, dateFormat);
        long timeSpanMillis = startTimeMillis - System.currentTimeMillis();

        //初始化计时器
        startTimeCountDown = new CountDownTimer(timeSpanMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished = millisUntilFinished / 1000;
                long days = millisUntilFinished / (60 * 60 * 24);
                long hours = (millisUntilFinished % (60 * 60 * 24)) / (60 * 60);
                long minutes = (millisUntilFinished % (60 * 60)) / 60;
                long seconds = millisUntilFinished % 60;

                String dayString=zeroFill(days);
                String hourString=zeroFill(hours);
                String minuteString=zeroFill(minutes);
                String secondString=zeroFill(seconds);

                String timeText;
                if (days > 0) {
                    timeText = dayString + "天" + hourString + "小时" + minuteString + "分钟" + secondString + "秒";
                } else if (hours > 0) {
                    timeText = hourString + "小时" + minuteString + "分钟" + secondString + "秒";
                } else if (minutes > 0) {
                    timeText = minuteString + "分钟" + secondString + "秒";
                } else {
                    timeText = secondString + "秒";
                }
                timeText = "倒计时：" + timeText;
                tvStartTimeCountDown.setText(timeText);
            }

            @Override
            public void onFinish() {
                tvStartTimeCountDown.setVisibility(View.GONE);
            }

            private String zeroFill(long input){
                //三位数的就直接显示三位数了
                if (input>99){
                    return String.valueOf(input);
                }
                //二位数的就补零
                String format="%02d";
                return String.format(Locale.getDefault(),format,input);
            }
        };
        startTimeCountDown.start();
    }
    //停止直播倒计时
    private void stopLiveCountDown(){
        if (startTimeCountDown!=null){
            startTimeCountDown.cancel();
        }
        tvStartTimeCountDown.setVisibility(GONE);
    }

    private void hideScreenShotView() {
        ivScreenshot.setVisibility(GONE);
    }

    public void showScreenShotView() {
        Bitmap screenshot = polyvCloudClassVideoView.screenshot();
        ivScreenshot.setImageBitmap(screenshot);
        ivScreenshot.setVisibility(VISIBLE);
    }

    private void registerSocketMessage() {
        messageDispose = PolyvRxBus.get().toObservable(PolyvSocketMessageVO.class).
                subscribe(new Consumer<PolyvSocketMessageVO>() {
                    @Override
                    public void accept(PolyvSocketMessageVO polyvSocketMessage) throws Exception {

                        String event = polyvSocketMessage.getEvent();
                        if (ONSLICECONTROL.equals(event) || ONSLICEID.equals(event)) {
                            processCameraMessage(polyvSocketMessage);
                        } else if (OPEN_MICROPHONE.equals(event)) {
                            processMicroPhoneMessage(polyvSocketMessage);
                        }

                    }
                });
    }

    private void processMicroPhoneMessage(PolyvSocketMessageVO polyvSocketMessage) {
        String message = polyvSocketMessage.getMessage();
        PolyvMicphoneStatus micphoneStatus = PolyvGsonUtil.fromJson(PolyvMicphoneStatus.class, message);
        if (micphoneStatus != null) {
            if ("video".equals(micphoneStatus.getType()) || "audio".equals(micphoneStatus.getType())) {//连麦开关

                controller.showMicPhoneLine("open".equals(micphoneStatus.getStatus())
                        ? VISIBLE : INVISIBLE);

            }

        }
    }

    private void processCameraMessage(PolyvSocketMessageVO polyvSocketMessage) {

        PolyvSocketSliceControlVO polyvSocketSliceControl = PolyvGsonUtil.
                fromJson(PolyvSocketSliceControlVO.class, polyvSocketMessage.getMessage());
        if (polyvSocketSliceControl != null && polyvSocketSliceControl.getData() != null) {
            if (polyvSocketSliceControl.getData().getIsCamClosed() == 0) {//打开摄像头
                //摄像头控制类型
                if (controller != null && "closeCamera".equals(polyvSocketSliceControl.getData().getType())) {
                    controller.performClickCamera();
                }
            } else {
                if (controller.isPPTSubView()) {
                    controller.changePPTVideoLocation();
                }

                controller.performClickCamera();

            }

        }
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    // <editor-fold defaultstate="collapsed" desc="根据连麦状态调整布局位置">
    private boolean isJoinLinkMic;
    private int linkMicLayoutWidth = (int) getResources().getDimension(R.dimen.ppt_width);

    public void notifyLinkMicStatusChange(boolean isJoinLinkMic) {
        this.isJoinLinkMic = isJoinLinkMic;
        if (PolyvScreenUtils.isLandscape(getContext())) {
            if (isJoinLinkMic) {
                adjustLocation();
            } else {
                resetLocation();
            }
        }
    }

    private void adjustLocation() {
        if (polyvCloudClassVideoView != null) {
            MarginLayoutParams mlp = (MarginLayoutParams) polyvCloudClassVideoView.getLayoutParams();
//            mlp.topMargin = linkMicLayoutHeight;//当连麦列表在顶部的时候
            mlp.leftMargin = linkMicLayoutWidth;
            //72 为连麦控制栏的宽度
            mlp.rightMargin = PolyvScreenUtils.dip2px(getContext(),72);
            polyvCloudClassVideoView.setLayoutParams(mlp);
        }
    }

    private void resetLocation() {
        if (polyvCloudClassVideoView != null) {
            MarginLayoutParams mlp = (MarginLayoutParams) polyvCloudClassVideoView.getLayoutParams();
//            mlp.topMargin = 0;
            mlp.leftMargin = 0;
            mlp.rightMargin = 0;
            polyvCloudClassVideoView.setLayoutParams(mlp);
        }
    }

    public void notifyOnConfigChangedListener(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isJoinLinkMic) {
                adjustLocation();
            }
        } else {
            resetLocation();
        }
    }
    // </editor-fold>
}
