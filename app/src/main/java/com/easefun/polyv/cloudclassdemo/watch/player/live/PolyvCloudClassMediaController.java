package com.easefun.polyv.cloudclassdemo.watch.player.live;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.businesssdk.model.video.PolyvBitrateVO;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclass.video.api.IPolyvCloudClassController;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.danmu.PolyvDanmuFragment;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvCloudClassMoreLayout;
import com.easefun.polyv.commonui.PolyvCommonMediacontroller;
import com.easefun.polyv.commonui.player.IPolyvBusinessMediaController;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public class PolyvCloudClassMediaController extends PolyvCommonMediacontroller<PolyvCloudClassVideoView>
        implements IPolyvCloudClassController, IPolyvBusinessMediaController<PolyvCloudClassVideoView, PolyvCloudClassVideoHelper>, View.OnClickListener {

    private static final String TAG = "PolyvCloudClassMediaController";
    private static final int TOAST_SHOW_TIME = 5 * 1000;
    private static final int LINK_UP_TIMEOUT = 20 * 1000;

    private ImageView videoRefreshPort;
    private ImageView videoScreenSwitchPort;
    private ImageView videoDanmuPort;
    private ImageView videoPptChangeSwitchPort;
    private ImageView videoHandsUpPort;
    private ImageView videoBackPort;
    private ImageView ivVideoPausePortrait;
    private FrameLayout flGradientBarPort;

    private ImageView videoRefreshLand;
    private ImageView videoDanmuLand;
    private ImageView videoScreenSwitchLand;
    private ImageView videoHandsUpLand;
    private ImageView videoBackLand;
    private ImageView ivVideoPauseLand;
    private FrameLayout flGradientBarLand;

    //横屏的打开发送弹幕发送器的开关
    private TextView tvStartSendDanmuLand;

    //更多
    private PolyvCloudClassMoreLayout moreLayout;
    //控制弹幕可见性
    private DanmuController danmuController;

    private PolyvCloudClassVideoHelper polyvCloudClassPlayerHelper;
    private PolyvDanmuFragment danmuFragment;

    private boolean showCamer;
    private boolean isPaused;

    // 控制栏是否是显示状态
    private boolean isShowing, showPPT;
    private PopupWindow bitRatePopupWindow;
    private Disposable popupWindowTimer, linkUpTimer;
    // 提示对话框
    private AlertDialog alertDialog;

    private ImageView ivMorePortrait;
    private ImageView ivMoreLand;

    private OnClickOpenStartSendDanmuListener onClickOpenStartSendDanmuListener;


    public PolyvCloudClassMediaController(@NonNull Context context) {
        this(context, null);
    }

    public PolyvCloudClassMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PolyvCloudClassMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    protected void initialView() {
        context = (Activity) getContext();
        rootView = View.inflate(context, R.layout.polyv_cloudclass_controller, this);

        initialOtherView();

        addListener();
    }

    private void initialOtherView() {
        //竖屏
        videoControllerPort = findViewById(R.id.video_controller_port);
        videoRefreshPort = findViewById(R.id.video_refresh_port);
        videoScreenSwitchPort = findViewById(R.id.video_screen_switch_port);
        videoDanmuPort = findViewById(R.id.video_danmu_port);
        videoPptChangeSwitchPort = findViewById(R.id.video_ppt_change_switch_port);
        videoHandsUpPort = findViewById(R.id.video_hands_up_port);
        videoBackPort = findViewById(R.id.iv_video_back_portrait);
        ivMorePortrait = findViewById(R.id.iv_more_portrait);
        ivVideoPausePortrait = findViewById(R.id.iv_video_pause_portrait);
        flGradientBarPort = findViewById(R.id.fl_gradient_bar_port);

        //横屏
        videoControllerLand = findViewById(R.id.video_controller_land);
        videoRefreshLand = findViewById(R.id.video_refresh_land);
        videoDanmuLand = findViewById(R.id.video_danmu_land);
        videoScreenSwitchLand = findViewById(R.id.video_ppt_change_switch_land);
        videoHandsUpLand = findViewById(R.id.video_hands_up_land);
        videoBackLand = findViewById(R.id.iv_video_back_land);
        ivMoreLand = findViewById(R.id.iv_more_land);
        ivVideoPauseLand = findViewById(R.id.iv_video_pause_land);
        flGradientBarLand = findViewById(R.id.fl_gradient_bar_land);

        //更多
        moreLayout = new PolyvCloudClassMoreLayout(context, this);
        moreLayout.injectShowMediaControllerFunction(this::show);
        moreLayout.injectShowGradientBarFunction(show -> {
            flGradientBarLand.setVisibility(show ? View.VISIBLE : View.GONE);
            flGradientBarPort.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        moreLayout.setOnBitrateSelectedListener((definitionVO, pos) -> polyvVideoView.changeBitRate(pos));
        moreLayout.setOnOnlyAudioSwitchListener(onlyAudio -> {
            if (polyvCloudClassPlayerHelper.isJoinLinkMick()) {
                return false;
            } else {
                if (onlyAudio) {
                    polyvVideoView.changeMediaPlayMode(PolyvMediaPlayMode.MODE_AUDIO);
                } else {
                    polyvVideoView.changeMediaPlayMode(PolyvMediaPlayMode.MODE_VIDEO);
                }
                if (showPPT){
                    showCamerView();
                }
                return true;
            }
        });

        tvStartSendDanmuLand = findViewById(R.id.tv_start_send_danmu_land);

        videoControllerLand.setVisibility(View.GONE);

        danmuController = new DanmuController();
        danmuController.init();
    }


    private void addListener() {
        videoRefreshPort.setOnClickListener(this);
        videoScreenSwitchPort.setOnClickListener(this);
        videoDanmuPort.setOnClickListener(this);
        videoPptChangeSwitchPort.setOnClickListener(this);
        videoHandsUpPort.setOnClickListener(this);
        videoBackPort.setOnClickListener(this);
        ivMorePortrait.setOnClickListener(this);
        ivVideoPausePortrait.setOnClickListener(this);

        videoRefreshLand.setOnClickListener(this);
        videoDanmuLand.setOnClickListener(this);
        videoScreenSwitchLand.setOnClickListener(this);
        videoHandsUpLand.setOnClickListener(this);
        videoBackLand.setOnClickListener(this);
        ivMoreLand.setOnClickListener(this);
        ivVideoPauseLand.setOnClickListener(this);

        tvStartSendDanmuLand.setOnClickListener(this);
    }

    @Override
    public void release() {

    }

    @Override
    public void destroy() {
        if (danmuFragment != null) {
            danmuFragment.onDestroy();
            danmuFragment = null;
        }
        if (popupWindowTimer != null) {
            popupWindowTimer.dispose();
            popupWindowTimer = null;
        }

        cancleLinkUpTimer();
    }

    @Override
    public void setViewBitRate(String vid, int bitRate) {

    }

    @Override
    public void onPrepared(PolyvCloudClassVideoView mp) {

    }


    @Override
    public void onLongBuffering(String tip) {
        showBitrateChangeView();
    }

    private void showBitrateChangeView() {
        if (polyvLiveBitrateVO == null || polyvLiveBitrateVO.getDefinitions() == null ||
                currentBitratePos == polyvLiveBitrateVO.getDefinitions().size() - 1) {
            return;
        }
        if (bitRatePopupWindow == null) {
            creatBitrateChangeWindow();
        }
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        View showView = videoRefreshPort;
        if (videoRefreshLand.isShown()) {
            showView = videoRefreshLand;
        }
        showView.getLocationOnScreen(location);
        //在控件上方显示
        View child = bitRatePopupWindow.getContentView();
        TextView definition = (TextView) child.findViewById(R.id.live_bitrate_popup_definition);

        PolyvDefinitionVO definitionVO = polyvLiveBitrateVO.getDefinitions().get(Math.max(0, currentBitratePos + 1));
        definition.setText(definitionVO.definition);

        definition.setOnClickListener(this);

        child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int popupHeight = child.getMeasuredHeight();
        int popupWidth = child.getMeasuredWidth();
        bitRatePopupWindow.showAtLocation(showView, Gravity.NO_GRAVITY, (location[0] + 10), location[1] - popupHeight - 10);
//        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_TOAST,TOAST_SHOW_TIME);
        popupWindowTimer = PolyvRxTimer.delay(TOAST_SHOW_TIME, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                hideBitPopup();
            }
        });

    }


    @Override
    public void initialBitrate(PolyvBitrateVO bitrateVO) {
        super.initialBitrate(bitrateVO);
        moreLayout.initBitrate(bitrateVO);
    }

    private void hideBitPopup() {
        if (bitRatePopupWindow != null) {
            bitRatePopupWindow.dismiss();
        }
    }

    private void creatBitrateChangeWindow() {

        View child = View.inflate(getContext(), R.layout.polyv_live_bitrate_popu_layout, null);
        bitRatePopupWindow = new PopupWindow(child, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        bitRatePopupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        bitRatePopupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        bitRatePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bitRatePopupWindow.setOutsideTouchable(true);
        bitRatePopupWindow.update();

        bitRatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bitRatePopupWindow = null;
                if (popupWindowTimer != null && !popupWindowTimer.isDisposed()) {
                    popupWindowTimer.dispose();
                }
//                handler.removeMessages(MESSAGE_HIDE_TOAST);
            }
        });
    }

    @Override
    public void changeToLandscape() {
        super.changeToLandscape();
//        danmuController.onLandscape();
        videoDanmuPort.post(() -> {
            danmuController.refreshDanmuStatus();
        });
    }

    @Override
    public void changeToPortrait() {
        super.changeToPortrait();
//        danmuController.onPortrait();
        videoDanmuPort.post(() -> {
            danmuController.refreshDanmuStatus();
        });
    }

    @Override
    public void hide() {
        super.hide();
        moreLayout.hide();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void setAnchorView(View view) {
    }

    @Deprecated
    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    @Override
    public void showOnce(View view) {
        setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        switch (id) {
            case R.id.video_danmu_land:
            case R.id.video_danmu_port:
                danmuController.toggleDanmu();
                break;
            case R.id.video_hands_up_land:
            case R.id.video_hands_up_port:
                if (!polyvCloudClassPlayerHelper.requestPermission()) {
                    return;
                }
                break;
            case R.id.video_ppt_change_switch_port:
            case R.id.video_ppt_change_switch_land:
                if (showCamer) {
                    showCamerView();
                } else {
                    changePPTVideoLocation();
                }
                break;
            case R.id.video_refresh_land:
            case R.id.video_refresh_port:
                refreshVideoView();
                if (isPaused){
                    togglePauseBtn();
                }
                break;
//            case R.id.video_screen_switch_land:
//                changeToPortrait();
//                polyvCloudClassPlayerHelper.resetFloatViewPort();
//                break;
            case R.id.video_screen_switch_port:
                PolyvScreenUtils.unlockOrientation();
                changeToLandscape();
                break;
            case R.id.iv_video_back_portrait:
                if (context != null) {
                    context.finish();
                }
                break;
            case R.id.iv_video_back_land:
                if (ScreenUtils.isLandscape()) {
                    PolyvScreenUtils.unlockOrientation();
                    changeToPortrait();
                }
                break;
            case R.id.iv_more_land:
                moreLayout.showWhenLandscape();
                break;
            case R.id.iv_more_portrait:
                moreLayout.showWhenPortrait();
                break;
            case R.id.iv_video_pause_land:
            case R.id.iv_video_pause_portrait:
                togglePauseBtn();
                break;
            case R.id.tv_start_send_danmu_land:
                onClickOpenStartSendDanmuListener.onStartSendDanmu();
        }
    }

    @Override
    public void updatePPTShowStatus(boolean showPPT) {
        this.showPPT = showPPT;
        videoPptChangeSwitchPort.setVisibility(showPPT ? VISIBLE : GONE);
        videoScreenSwitchLand.setVisibility(showPPT ? VISIBLE : INVISIBLE);
    }

    public void showCamerView() {
        showCamer = false;
        videoPptChangeSwitchPort.setImageResource(R.drawable.controller_exchange);
        videoScreenSwitchLand.setImageResource(R.drawable.controller_exchange);
        polyvCloudClassPlayerHelper.showCamerView();
    }


    public void onServerDanmuOpen(boolean isServerDanmuOpen) {
        danmuController.onServerDanmuOpen(isServerDanmuOpen);
    }

    /**
     * 打开竖屏下的弹幕（默认关闭）
     */
    public void enableDanmuInPortrait() {
        danmuController.enableDanmuInPortrait();
    }

    public void changeAudioOrVideoMode(@PolyvMediaPlayMode.Mode int mediaPlayMode) {
        moreLayout.onChangeAudioOrVideoMode(mediaPlayMode);
    }


    private void refreshVideoView() {
        polyvCloudClassPlayerHelper.initVolume();
        polyvCloudClassPlayerHelper.restartPlay();
    }

    public void changePPTVideoLocation() {
        if (!showPPT) {//如果不显示ppt  不触发此功能
            return;
        }
        if (polyvCloudClassPlayerHelper != null) {
            if (!polyvCloudClassPlayerHelper.changePPTViewToVideoView(showPPTSubView)) {
                return;
            }

            showPPTSubView = !showPPTSubView;
        }
    }

    //将ppt显示在主屏位置 因为在连麦后 要主动切换ppt到主屏
    public void switchPPTToMainScreen() {
        if (!showPPTSubView) {//如果已经显示在主屏了 不再执行此逻辑
            return;
        }
        if (polyvCloudClassPlayerHelper != null && (
                videoHandsUpLand.isSelected() || videoHandsUpPort.isSelected())) {
            polyvCloudClassPlayerHelper.changePPTViewToVideoView(true);
            showPPTSubView = false;
        }
    }

    public void handsUp(boolean joinSuccess) {
        View v = videoHandsUpPort;
        if (!videoHandsUpPort.isSelected()) {
            resetSelectedStatus(v);
            startHandsUpTimer();
            //初始化的时候已经传入channel
            polyvCloudClassPlayerHelper.sendJoinRequest();


        } else {
            showStopLinkDialog(joinSuccess, false);
        }


    }

    private void resetSelectedStatus(View v) {
        videoHandsUpLand.setSelected(!v.isSelected());
        videoHandsUpPort.setSelected(!v.isSelected());
    }

    private void togglePauseBtn() {
        isPaused=!isPaused;
        boolean toPause = isPaused;
        if (toPause) {
            polyvVideoView.pause();
        } else {
            refreshVideoView();
        }
        ivVideoPauseLand.setSelected(toPause);
        ivVideoPausePortrait.setSelected(toPause);
    }

    private void startHandsUpTimer() {
        cancleLinkUpTimer();
        linkUpTimer = PolyvRxTimer.delay(LINK_UP_TIMEOUT, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                resetSelectedStatus(videoHandsUpPort);
            }
        });
    }

    public void cancleLinkUpTimer() {
        if (linkUpTimer != null) {
            PolyvCommonLog.d(TAG, "cancleLinkUpTimer");
            linkUpTimer.dispose();
            linkUpTimer = null;
        }
    }

    private void showStopLinkDialog(boolean joinSuccess, final boolean isExit) {
        String message = joinSuccess ? String.format("您将断开与老师同学间的通话%s。", isExit ? "并退出" : "") :
                "您将取消连线申请";
        String btnMsg = joinSuccess ? String.format("挂断%s", isExit ? "并退出" : "") : "取消连线";
        alertDialog = new AlertDialog.Builder(getContext()).setTitle(joinSuccess ? "即将退出连麦功能\n" : "您将取消连线申请\n")
                .setNegativeButton(joinSuccess ? "继续连麦" : "继续申请", null)
                .setPositiveButton(btnMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isExit) {
                            context.finish();
                            return;
                        }
                        videoHandsUpPort.setSelected(!videoHandsUpPort.isSelected());
                        videoHandsUpLand.setSelected(!videoHandsUpPort.isSelected());
                        if (joinSuccess) {
                            PolyvLinkMicWrapper.getInstance().leaveChannel();
                        } else {
                            polyvCloudClassPlayerHelper.leaveChannel();
                        }
                        startHandsUpTimer();
                    }
                })
                .create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
    }


    public void addHelper(PolyvCloudClassVideoHelper polyvCloudClassPlayerHelper) {
        this.polyvCloudClassPlayerHelper = polyvCloudClassPlayerHelper;
    }

    //关闭小窗口后 更新控制栏
    public void updateControllerWithCloseSubView() {
        showCamer = true;
        if (showPPTSubView) {
            videoPptChangeSwitchPort.setImageResource(R.drawable.ppt);
            videoScreenSwitchLand.setImageResource(R.drawable.ppt);
        } else {
            videoPptChangeSwitchPort.setImageResource(R.drawable.camera);
            videoScreenSwitchLand.setImageResource(R.drawable.camera);

        }
    }

    /**
     * 更新连麦状态
     *
     * @param link
     */
    public void updateLinkMicStatus(boolean link) {

        videoHandsUpPort.setSelected(link);
        videoHandsUpLand.setSelected(link);

        enableLinkBtn(true);
    }

    public void enableLinkBtn(boolean enable) {
        videoHandsUpPort.setEnabled(enable);
        videoHandsUpLand.setEnabled(enable);
    }

    public void setDanmuFragment(PolyvDanmuFragment danmuFragment) {
        this.danmuFragment = danmuFragment;
    }

    public void setOnClickOpenStartSendDanmuListener(OnClickOpenStartSendDanmuListener onClickOpenStartSendDanmuListener) {
        this.onClickOpenStartSendDanmuListener = onClickOpenStartSendDanmuListener;
    }

    @Override
    public boolean isPPTSubView() {
        return showPPTSubView;
    }

    @Override
    public void showMicPhoneLine(int visiable) {

        if (videoHandsUpPort != null) {
            videoHandsUpPort.setVisibility(visiable);
        }

        if (videoHandsUpLand != null) {
            videoHandsUpLand.setVisibility(visiable);
        }
    }

    //加入连麦
    public void onJoinLinkMic() {
        ivMoreLand.setVisibility(INVISIBLE);
        ivMorePortrait.setVisibility(INVISIBLE);

        ivVideoPauseLand.setVisibility(INVISIBLE);
        ivVideoPausePortrait.setVisibility(INVISIBLE);

        videoRefreshLand.setVisibility(INVISIBLE);
        videoRefreshPort.setVisibility(INVISIBLE);
    }

    //离开连麦
    public void onLeaveLinkMic() {
        ivMoreLand.setVisibility(VISIBLE);
        ivMorePortrait.setVisibility(VISIBLE);

        ivVideoPauseLand.setVisibility(VISIBLE);
        ivVideoPausePortrait.setVisibility(VISIBLE);

        videoRefreshLand.setVisibility(VISIBLE);
        videoRefreshPort.setVisibility(VISIBLE);
        if (isPaused){
            togglePauseBtn();
        }
    }


    /**
     * 弹幕控制器
     * <p>
     * DanmuFragment可见性：1.由横竖屏结合开发者竖屏开关决定。2.用户点击Danmu按钮决定
     * Danmu按钮可见性：由服务端开关决定。
     */
    private class DanmuController {
        //弹幕按钮是否被打开
        boolean isDanmuToggleOpen = false;

        //弹幕竖屏开关
        boolean isEnableDanmuInPortrait = false;
        //弹幕服务端开关
        boolean isServerDanmuOpen = false;


        void init() {
            videoDanmuPort.post(() -> {
                toggleDanmu();
                refreshDanmuStatus();
            });
        }

        // <editor-fold defaultstate="collapsed" desc="弹幕toggle">
        void toggleDanmu() {
            isDanmuToggleOpen = !isDanmuToggleOpen;
            videoDanmuPort.setSelected(isDanmuToggleOpen);
            videoDanmuLand.setSelected(isDanmuToggleOpen);
            if (isDanmuToggleOpen) {
                danmuFragment.show();
                tvStartSendDanmuLand.setVisibility(VISIBLE);
            } else {
                danmuFragment.hide();
                tvStartSendDanmuLand.setVisibility(GONE);
            }
        }
        // </editor-fold>

        void onServerDanmuOpen(boolean isServerDanmuOpen) {
            this.isServerDanmuOpen = isServerDanmuOpen;
            refreshDanmuStatus();
        }

        /**
         * 在竖屏下也显示弹幕（默认不显示）
         */
        void enableDanmuInPortrait() {
            isEnableDanmuInPortrait = true;
            refreshDanmuStatus();
        }

        void refreshDanmuStatus() {
            if (isServerDanmuOpen) {
                //后台弹幕打开
                videoDanmuLand.setVisibility(VISIBLE);

                if (isEnableDanmuInPortrait) {
                    //打开竖屏弹幕
                    videoDanmuPort.setVisibility(VISIBLE);
                    if (isDanmuToggleOpen) {
                        danmuFragment.show();
                        tvStartSendDanmuLand.setVisibility(VISIBLE);
                    } else {
                        danmuFragment.hide();
                        tvStartSendDanmuLand.setVisibility(INVISIBLE);
                    }
                } else {
                    //关闭竖屏弹幕
                    videoDanmuPort.setVisibility(INVISIBLE);
                    if (PolyvScreenUtils.isPortrait(getContext())) {
                        danmuFragment.hide();
                    } else {
                        if (isDanmuToggleOpen) {
                            danmuFragment.show();
                            tvStartSendDanmuLand.setVisibility(VISIBLE);
                        } else {
                            danmuFragment.hide();
                            tvStartSendDanmuLand.setVisibility(INVISIBLE);
                        }
                    }
                }
            } else {
                //后台弹幕关闭
                danmuFragment.hide();
                videoDanmuLand.setVisibility(GONE);
                videoDanmuPort.setVisibility(GONE);
                tvStartSendDanmuLand.setVisibility(GONE);
            }
        }
    }

    public interface OnClickOpenStartSendDanmuListener {
        void onStartSendDanmu();
    }
}
