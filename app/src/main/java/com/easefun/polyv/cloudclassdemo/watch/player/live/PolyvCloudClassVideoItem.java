package com.easefun.polyv.cloudclassdemo.watch.player.live;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.player.PolyvPlayError;
import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.businesssdk.model.link.PolyvMicphoneStatus;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveMarqueeVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeItem;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeUtils;
import com.easefun.polyv.businesssdk.sub.marquee.PolyvMarqueeView;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketSliceControlVO;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassAudioModeView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassListenerEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.danmu.PolyvDanmuFragment;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.IPolyvLandscapeDanmuSender;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvCloudClassAudioModeView;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvLandscapeDanmuSendPanel;
import com.easefun.polyv.commonui.player.IPolyvVideoItem;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.widget.PolyvLightTipsView;
import com.easefun.polyv.commonui.player.widget.PolyvVolumeTipsView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvControlUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.OPEN_MICROPHONE;

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
    //    private PolyvProgressTipsView tipsviewProgress;
    //手势滑动进度
    private RelativeLayout rlTop;
    private ProgressBar loadingview;
    private TextView preparingview;
    private View noStream;
    private View rootView;
    private ImageView subBackLand;
    private FrameLayout flSubBackAndGradient;
    private FrameLayout audioModeLayoutRoot;
    //截图，用于刷新直播的时候防止黑屏
    private ImageView ivScreenshot;

    //只听音频View
    private IPolyvCloudClassAudioModeView audioModeView;


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

        subVideoview = (PolyvAuxiliaryVideoview) findViewById(R.id.sub_videoview);
        flSubBackAndGradient= (FrameLayout) findViewById(R.id.fl_sub_back_gradient);
        subBackLand = (ImageView) findViewById(R.id.sub_video_back_land);
        subLoadingview = (ProgressBar) findViewById(R.id.sub_loadingview);
        subPreparingview = (TextView) findViewById(R.id.sub_preparingview);
        tvCountdown = (TextView) findViewById(R.id.tv_countdown);
        tvSkip = (TextView) findViewById(R.id.tv_skip);

        subBackLand.setOnClickListener(this);
        subVideoview.setOnGestureClickListener(onGestureClickListener);
    }


    private void showSubLandBack() {
        PolyvCommonLog.d(TAG, "showSubLandBack");
        flSubBackAndGradient.setVisibility(VISIBLE);
        flSubBackAndGradient.removeCallbacks(hideTask);
        flSubBackAndGradient.postDelayed(hideTask, 5000);
    }

    private void initialView() {

        rootView = View.inflate(context, R.layout.polyv_cloudclass_item, this);

        ivScreenshot = (ImageView) findViewById(R.id.iv_screenshot);

        rlTop = (RelativeLayout) findViewById(R.id.rl_top);
        loadingview = (ProgressBar) findViewById(R.id.loadingview);
        preparingview = (TextView) findViewById(R.id.preparingview);
        tipsviewLight = (PolyvLightTipsView) findViewById(R.id.tipsview_light);
        tipsviewVolume = (PolyvVolumeTipsView) findViewById(R.id.tipsview_volume);
        noStream = findViewById(R.id.no_stream);
        audioModeLayoutRoot = (FrameLayout) findViewById(R.id.fl_audio_mode_layout_root);
        marqueeView = (PolyvMarqueeView) findViewById(R.id.polyv_marquee_view);

        FragmentTransaction fragmentTransaction = context.getSupportFragmentManager().beginTransaction();
        danmuFragment = new PolyvDanmuFragment();
        fragmentTransaction.add(R.id.fl_danmu, danmuFragment, "danmuFragment").commit();

        landscapeDanmuSender=new PolyvLandscapeDanmuSendPanel(context,this);

        controller = (PolyvCloudClassMediaController) findViewById(R.id.controller);
        controller.setOnClickOpenStartSendDanmuListener(()->{
            controller.hide();
           landscapeDanmuSender.openDanmuSender();
        });
        controller.setDanmuFragment(danmuFragment);

        //只听音频View
        PolyvCloudClassAudioModeView audioViewImpl=new PolyvCloudClassAudioModeView(getContext());
        audioViewImpl.setOnChangeVideoModeListener(() -> {
            polyvCloudClassVideoView.changeMediaPlayMode(PolyvMediaPlayMode.MODE_VIDEO);
            controller.changeAudioOrVideoMode(PolyvMediaPlayMode.MODE_VIDEO);
        });
        audioModeView=audioViewImpl;
        audioModeLayoutRoot.addView(audioModeView.getRoot(),LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        //打开弹幕下的竖屏
        controller.enableDanmuInPortrait();
    }

    private void initialVideoView() {
        polyvCloudClassVideoView = (PolyvCloudClassVideoView) findViewById(R.id.cloudschool_videoview);
        polyvCloudClassVideoView.setAudioModeView(audioModeView);
        polyvCloudClassVideoView.setMediaController(controller);
        polyvCloudClassVideoView.setNoStreamIndicator(noStream);
        polyvCloudClassVideoView.setPlayerBufferingIndicator(loadingview);
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
                isNoLiveAtPresent=false;
                hideScreenShotView();
                controller.show();
            }

            @Override
            public void onPreparing() {

            }
        });
        polyvCloudClassVideoView.setOnPPTShowListener(new IPolyvVideoViewListenerEvent.OnPPTShowListener() {
            @Override
            public void showPPTView(int visiable) {
                if(visiable == VISIBLE){
                    controller.switchPPTToMainScreen();
                }
                if (polyvPPTItem != null) {
                    polyvPPTItem.show(visiable);
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
                if(!open){
                    if(polyvPPTItem != null){
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
                PolyvCommonLog.d(TAG,"showMicPhoneLine");
                if (controller != null) {
                    controller.showMicPhoneLine(visiable);
                }
                if(visiable == INVISIBLE){//关闭连麦
                    PolyvLinkMicWrapper.getInstance().leaveChannel();
                }
            }
        });
        polyvCloudClassVideoView.setOnNoLiveAtPresentListener(new IPolyvCloudClassListenerEvent.OnNoLiveAtPresentListener() {
            @Override
            public void onNoLiveAtPresent() {
                isNoLiveAtPresent=true;
                ToastUtils.showShort("暂无直播");
            }
        });
        polyvCloudClassVideoView.setOnGestureClickListener((start, end) -> {
            //如果当前没有直播，才会将单击事件传递，并显示没有直播时的按钮。
            if (!polyvCloudClassVideoView.isOnline()){
                onGestureClickListener.callback(start,end);
            }
        });

        polyvCloudClassVideoView.setOnDanmuServerOpenListener(open -> controller.onServerDanmuOpen(open));
    }

    public void showDefaultIcon() {
        if(loadingview != null){
            loadingview.setVisibility(GONE);
        }
        if(noStream != null){
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

    public View getScreenShotView(){
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
            danmuFragment.onDestroy();
            danmuFragment = null;
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.sub_video_back_land:
                if (controller != null && ScreenUtils.isLandscape()) {
                    controller.changeToPortrait();
                } else {
                    context.finish();
                }
                break;
        }
    }

    //设置横屏发送弹幕监听器
    public void setOnSendDanmuListener(IPolyvLandscapeDanmuSender.OnSendDanmuListener onSendDanmuListener){
        landscapeDanmuSender.setOnSendDanmuListener(onSendDanmuListener);
    }

    //横屏发送弹幕消息
    public void sendDanmuMessage(CharSequence message) {
        if (danmuFragment != null) {
            danmuFragment.sendDanmaku(message);
        }
    }

    private void hideScreenShotView(){
        ivScreenshot.setVisibility(GONE);
    }
    public void showScreenShotView(){
        Bitmap screenshot=polyvCloudClassVideoView.screenshot();
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

        PolyvCommonLog.d(TAG, "receive ONSLICECONTROL message");
        PolyvSocketSliceControlVO polyvSocketSliceControl = PolyvGsonUtil.
                fromJson(PolyvSocketSliceControlVO.class, polyvSocketMessage.getMessage());
        if (polyvSocketSliceControl != null && polyvSocketSliceControl.getData() != null) {
            if (polyvSocketSliceControl.getData().getIsCamClosed() == 0) {//打开摄像头
                //摄像头控制类型
                if (controller != null && "closeCamera".equals(polyvSocketSliceControl.getData().getType())) {
                    controller.showCamerView();
                }
            } else {
                if (controller.isPPTSubView()) {
                    controller.changePPTVideoLocation();
                }

                if(polyvPPTItem != null){
                    polyvPPTItem.hideSubView();
                }
            }

        }
    }


    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

}
