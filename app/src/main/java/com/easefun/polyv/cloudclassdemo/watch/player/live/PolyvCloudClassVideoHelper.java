package com.easefun.polyv.cloudclassdemo.watch.player.live;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.easefun.polyv.businesssdk.api.common.ppt.PolyvCloudClassPPTProcessor;
import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.businesssdk.model.link.PolyvLinkMicMedia;
import com.easefun.polyv.businesssdk.model.link.PolyvMicphoneStatus;
import com.easefun.polyv.businesssdk.model.ppt.PolyvPPTAuthentic;
import com.easefun.polyv.businesssdk.sp.PolyvSharePreference;
import com.easefun.polyv.businesssdk.web.IPolyvWebMessageProcessor;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSendCupEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvTeacherInfo;
import com.easefun.polyv.cloudclass.chat.event.linkmic.PolyvJoinLeaveSEvent;
import com.easefun.polyv.cloudclass.chat.event.linkmic.PolyvJoinRequestSEvent;
import com.easefun.polyv.cloudclass.chat.event.linkmic.PolyvLinkMicToken;
import com.easefun.polyv.cloudclass.config.PolyvVClassGlobalConfig;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketSliceControlVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketSliceIdVO;
import com.easefun.polyv.cloudclass.model.PolyvTeacherStatusInfo;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.IPolyvHomeProtocol;
import com.easefun.polyv.cloudclassdemo.watch.PolyvDemoClient;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.IPolyvDataBinder;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.PolyvLinkMicDataBinder;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.PolyvNormalLiveLinkMicDataBinder;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.widget.IPolyvRotateBaseView;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.widget.PolyvLinkMicParent;
import com.easefun.polyv.commonui.PolyvCommonVideoHelper;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvAppUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvFormatUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicAGEventHandler;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;
import com.easefun.polyv.linkmic.model.PolyvLinkMicJoinStatus;
import com.easefun.polyv.linkmic.model.PolyvLinkMicSwitchView;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.plv.rtc.PLVARTCAudioVolumeInfo;
import com.plv.rtc.PLVARTCConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvCloudClassPPTProcessor.AUTHORIZATION_PPT_PAINT;
import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvCloudClassPPTProcessor.CHANGE_COLOR;
import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvCloudClassPPTProcessor.CHAT_LOGIN;
import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvCloudClassPPTProcessor.ERASE_STATUS;
import static com.easefun.polyv.businesssdk.api.common.ppt.PolyvCloudClassPPTProcessor.PPT_PAINT_STATUS;
import static com.easefun.polyv.businesssdk.model.ppt.PolyvPPTAuthentic.PermissionType.VOICE;
import static com.easefun.polyv.businesssdk.sp.PolyvPreConstant.LINK_MIC_TOKEN;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.OPEN_MICROPHONE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.EVENT_LOGIN;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.EVENT_MUTE_USER_MICRO;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.EVENT_REJOIN_MIC;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.O_TEACHER_INFO;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_LEAVE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_REQUEST;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_RESPONSE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_SUCCESS;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_SWITCH_MESSAGE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_SWITCH_PPT_MESSAGE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.TEACHER_SET_PERMISSION;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.TOKEN_VALIDATE;
import static com.easefun.polyv.cloudclassdemo.watch.linkMic.PolyvLinkMicDataBinder.CAMERA_VIEW_ID;

/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public class PolyvCloudClassVideoHelper extends PolyvCommonVideoHelper<PolyvCloudClassVideoItem,
        PolyvCloudClassVideoView, PolyvCloudClassMediaController>
        implements PolyvNewMessageListener, PolyvPermissionListener {
    private static final String TAG = "PolyvCloudClassVideoHelper";

    private static final int LINK_JOIN_TIME = 20 * 1000;//加入连麦的超时事件

    private static final String JOIN_DEFAULT_TYPE = "JOIN_DEFAULT_TYPE";

    protected PolyvChatManager polyvChatManager;
    private Disposable linkJoinTimer, getLinkMicJoins,delayToJoinAsParticipant;
    private static final int REQUEST_CODE = 612;
    private boolean joinSuccess, subShowPPT;//连麦是否显示再大屏

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable viewerJoinLinkDispose;

    // 需请求的权限组
    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    private int[] ops = new int[]{PolyvPermissionManager.OP_CAMERA, PolyvPermissionManager.OP_RECORD_AUDIO};

    // 需请求的权限组
    private String[] permissionsYips = new String[]{
            "相机权限",
            "麦克风权限",
    };

    private ViewGroup mainScreenLinkView;//主屏显示连麦人的时候的view
    private ViewGroup linkMicSelected;//被选中要交换的连麦人
    private ViewGroup linkMicLayout;
    private IPolyvRotateBaseView linkMicLayoutParent;
    private IPolyvDataBinder polyvLinkMicAdapter;
    private PolyvLinkMicParent linkMicParent;
    private boolean isAudio;
    private LinearLayoutManager linearLayoutManager;
    private Map<String, PolyvJoinInfoEvent> joinRequests = new ConcurrentHashMap<>();
    private PolyvSocketSliceIdVO sliceIdVo;
    private String sessionId = "";
    private String roomId = "";
    private String channelId;
    private boolean cameraOpen = true, showPPT, supportRTC;
    private boolean isLinkMicInit=false;

    private Disposable joinListTimer;
    private Set<String> noCachesIds = new HashSet<>();//在缓存中没有找到数据得uid
    private String teacherId;

    private int RTC_VIEW_ID = 0x10000001;

    //首页相关得ui协议
    private IPolyvHomeProtocol homeProtocol;

    //是否是主讲模式
    private boolean isTeacherType,
    //是否是参与者角色
    isParticipant;
    //ppt授權信息
    private PolyvPPTAuthentic polyvPPTAuthentic;

    private int curMusicStreamVolume =-1;
    private int sendJoinRequestTime = 0;

    public PolyvCloudClassVideoHelper(PolyvCloudClassVideoItem videoItem,
                                      PolyvPPTItem polyvPPTItem, PolyvChatManager polyvChatManager, String channelId) {
        super(videoItem, polyvPPTItem);
        audioModeView = videoItem.getAudioModeView();
        screenShotView = videoItem.getScreenShotView();


        polyvChatManager.addNewMessageListener(this);
        this.polyvChatManager = polyvChatManager;
        this.channelId=channelId;

        permissionManager = PolyvPermissionManager.with((Activity) context)
                .permissions(permissions)
                .meanings(permissionsYips)
                .opstrs(ops)
                .addRequestCode(REQUEST_CODE)
                .setPermissionsListener(this);

        registerSocketEventListener();
    }

    @Override
    public void initConfig(boolean isNormalLive) {
        this.showPPT = !isNormalLive;
        controller.addHelper(this);
        controller.updatePPTShowStatus(showPPT);
        controller.changePPTVideoLocation();

    }

    private SurfaceView addRTCView() {
        SurfaceView surfaceView = PolyvLinkMicWrapper.getInstance().createRendererView(PolyvAppUtils.getApp());
        surfaceView.setId(RTC_VIEW_ID);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        surfaceView.setVisibility(View.GONE);
        videoView.addView(surfaceView);
        return surfaceView;
    }

    @Override
    protected void addCloudClassWebProcessor() {
        IPolyvWebMessageProcessor<PolyvCloudClassPPTProcessor.CloudClassJSCallback> processor =
                new PolyvCloudClassPPTProcessor(null);
        if (pptView != null) {
            pptView.addWebProcessor(processor);
        }
        processor.registerJSHandler(new PolyvCloudClassPPTProcessor.CloudClassJSCallback() {
            @Override
            public void brushPPT(String message) {
                //发送画笔事件
                polyvChatManager.sendScoketMessage(PolyvChatManager.SE_MESSAGE, message);
            }

            @Override
            public void screenBSSwitch(boolean pptSubShow) {
                changePPTViewToVideoView(!pptSubShow);
            }

            @Override
            public void screenPLSwitch(boolean landscapeShow) {
                if (landscapeShow) {
                    changeToLandscape();
                } else {
                    changeToPortrait();
                }
            }

            @Override
            public void startOrPause(boolean start) {
                if (start) {
                    videoView.start();
                } else {
                    videoView.pause();
                }
            }

            @Override
            public void reloadVideo() {
                initVolume();
                restartPlay();
            }

            @Override
            public void backTopActivity() {
                if(ScreenUtils.isLandscape()){
                    controller.changeToPortrait();
                }else{
                    ActivityUtils.getTopActivity().finish();
                }
            }

        });

    }

    @Override
    public void resetView(boolean isNormalLive) {

    }

    public void sendDanmuMessage(CharSequence message) {
        if (videoItem != null) {
            videoItem.sendDanmuMessage(message);
        }
    }

    private void initSupportRTC(){
        supportRTC = videoView.getModleVO() != null && videoView.getModleVO().isSupportRTCLive();
        if(showPPT){
            //三分屏默认是rtc流
            supportRTC = true;
        }
    }

    public void sendJoinRequest() {
        initSupportRTC();
        linkMicLayoutParent.setSupportRtc(supportRTC);
        createLinkMicLayout(linkMicLayout, supportRTC)                                                                                                           ;

        updateLinkMicStatus(false);
        if (polyvLinkMicAdapter != null) {
            isAudio= "audio".equals(videoView.getLinkMicType());
            PolyvCommonLog.d(TAG,"sendJoinRequest: isAudio="+isAudio);
            polyvLinkMicAdapter.setAudio(isAudio);
            //如果是不支持rtc 都需要隐藏摄像头控制按钮
            linkMicParent.updateLinkController( !isAudio);
            polyvLinkMicAdapter.bindLinkMicFrontView(linkMicLayoutParent.getOwnView());
        }
        if (sendJoinRequestTime==0){
            PolyvRxTimer.delay(1500, new Consumer<Object>() {
                @Override
                public void accept(Object o) throws Exception {
                    PolyvLinkMicWrapper.getInstance().enableLocalVideo(!"audio".equals(videoView.getLinkMicType()));
                }
            });
        }else {
            PolyvLinkMicWrapper.getInstance().enableLocalVideo(!"audio".equals(videoView.getLinkMicType()));
        }
        if (polyvChatManager != null) {
            polyvChatManager.sendJoinRequestMessage(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
        }
        sendJoinRequestTime++;
    }

    public void leaveChannel() {
        if (polyvChatManager != null) {
            polyvChatManager.sendJoinLeave(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
        }

    }

    @Override
    public void restartPlay() {
        if (videoItem != null) {
            videoItem.showScreenShotView();
        }
        super.restartPlay();
    }

    @Override
    public void pause() {
        if (joinSuccess) {
            if (showPPT) {
                super.pause();
            }
            return;
        }
//        muteVideoView();
    }

    private void muteVideoView() {
        //防止连麦以后静音 然后退到后台又静音得问题
        if (videoView != null && videoView.isPlaying() && videoView.getIjkMediaPlayer() != null) {
//            videoViewVolume = videoView.getVolume();
//            videoView.setVolume(0);
            videoView.getIjkMediaPlayer().setVolume(0, 0);
        }
    }

    @Override
    public void resume() {
//        openVideoViewSound();
        if (joinSuccess) {
            return;
        }

        super.resume();
    }

    @Override
    public void onNewMessage(String message, String event) {
        //这里回掉涉及到view的操作尽量放在handler里进行
        PolyvCommonLog.d(TAG, "onNewMessage:" + event + "  message :"
                + message);
        if (SE_JOIN_RESPONSE.equals(event)) {//收到连麦回应的消息

            processJoinResponseMessage();

        } else if (SE_JOIN_REQUEST.equals(event)) {//保存加入的用户数据

            processJoinRequestMessage(message);

        } else if (SE_JOIN_LEAVE.equals(event)) {

            processJoinLeaveMessage(message);

        } else if (SE_JOIN_SUCCESS.equals(event)) {

        } else if (EVENT_MUTE_USER_MICRO.equals(event)) {//禁麦事件
            PolyvLinkMicMedia micMedia = PolyvGsonUtil.fromJson(PolyvLinkMicMedia.class, message);
            processMediaMessage(micMedia);

        } else if (OPEN_MICROPHONE.equals(event)) {
            PolyvMicphoneStatus micphoneStatus = PolyvGsonUtil.fromJson(PolyvMicphoneStatus.class, message);
            processMicPhone(micphoneStatus);
        } else if (ONSLICEID.equals(event)) {
            processSliceIdMessage(message);
        } else if (ONSLICECONTROL.equals(event)) {
            PolyvCommonLog.d(TAG, "receive ONSLICECONTROL message");
            processMicSlice(message);
        } else if (SE_SWITCH_MESSAGE.equals(event)) {
            if (joinSuccess) {
                processSwitchView(message);
            }
        } else if (O_TEACHER_INFO.equals(event)) {
            processTeacherInfo(message);
        } else if (EVENT_LOGIN.equals(event)) {
            processLoginMessage(message, event);

        } else if (TEACHER_SET_PERMISSION.equals(event)) {
            processBrushPermission(message, event);
        } else if(SE_SWITCH_PPT_MESSAGE.equals(event)){
            processSwitchPPTMessage(message, event);
        }
    }

    private void processSwitchPPTMessage(String message, String event) {
        final PolyvPPTAuthentic pptAuthentic = PolyvGsonUtil.fromJson(PolyvPPTAuthentic.class, message);

        S_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if("1".equals(pptAuthentic.getStatus()) && !controller.isShowPPTSubView()){
                    controller.changePPTVideoLocation();
                }else if("0".equals(pptAuthentic.getStatus()) && controller.isPPTSubView()){
                    controller.changePPTVideoLocation();
                }
            }
        });
    }

    private void sendReJoinMessage() {
        if(!joinSuccess){
            return;
        }
        String token = PolyvSharePreference.getSharedPreferences().getString(LINK_MIC_TOKEN,"");
        if(!TextUtils.isEmpty(token)){
            PolyvLinkMicToken linkMicToken = PolyvGsonUtil.fromJson(PolyvLinkMicToken.class,token);
            if((System.currentTimeMillis() - linkMicToken.getTs()) < TOKEN_VALIDATE){
                polyvChatManager.sendScoketMessage(EVENT_REJOIN_MIC,linkMicToken.getToken());
            }
        }
    }

    private void processBrushPermission(String message, String event) {
        if (pptView == null) {
            return;
        }

        polyvPPTAuthentic = PolyvGsonUtil.fromJson(PolyvPPTAuthentic.class, message);

        //老师跟画笔角色进去画笔状态
        if (polyvPPTAuthentic.hasPPTOrAboveType()) {
            //如果被授予或取消画笔权限 更新主讲老师的状态
            if(polyvLinkMicAdapter != null){
//                if(!polyvLinkMicAdapter.changeTeacherLogo(polyvPPTAuthentic.getUserId(),polyvPPTAuthentic.hasTeacherAthuentic())){
//                    View updateView = mainScreenLinkView.findViewById(R.id.teacher_logo);
//                    if(updateView != null){
//                        updateView.setVisibility(VISIBLE);
//                        polyvLinkMicAdapter.updateTeacherLogoView(updateView);
//                    }
//                }
            }
            if (polyvPPTAuthentic != null) {
                if (!polyvPPTAuthentic.getUserId().equals(PolyvChatManager.getInstance().userId)) {
                    return;
                }
                pptView.sendWebMessage(AUTHORIZATION_PPT_PAINT, "{\"userType\":\"" + polyvPPTAuthentic.getType() + "\"}");
                //每次收到画笔或者是主讲信息  都要把画笔状态重置一次
                pptView.sendWebMessage(PPT_PAINT_STATUS, "{\"status\":\"" + "open\"" + "}");
            }
            isTeacherType = polyvPPTAuthentic.hasTeacherAthuentic();

            //当授予画笔权限时  且ppt 在副屏时 ppt不授予权限
            if (pptShowMainScreen() ||
                    (polyvPPTAuthentic.hasNoAthuentic())) {
                pptView.updateBrushPermission(message);
            }

        } else {

            //polyvPPTAuthentic.getUserId().equals(PolyvChatManager.getInstance().userId) ||
            if ( !joinSuccess) {
                PolyvCommonLog.d(TAG, "owern is set permission");
                return;
            }
            PolyvCommonLog.d(TAG, "userid :" + PolyvChatManager.getInstance().userId);
            //是否是参与者
            if (VOICE.equals(polyvPPTAuthentic.getType())) {
                //是否被邀请上麦
                boolean isInvitedToLinkMic=polyvPPTAuthentic.hasVoicePermission();

                //如果是自己被上下麦 更新底部栏
                if(polyvPPTAuthentic.getUserId().equals(PolyvChatManager.getInstance().userId) && isParticipant){
                    linkMicParent.updateBottomController(isInvitedToLinkMic);
                    //对于上下麦需要进行静音操作
                    PolyvLinkMicWrapper.getInstance().muteLocalAudio(!isInvitedToLinkMic);
                    PolyvLinkMicWrapper.getInstance().muteLocalVideo(!isInvitedToLinkMic);
                    if (isInvitedToLinkMic){
                        //被邀请上麦后，设为主播角色
                        PolyvLinkMicWrapper.getInstance().switchRoleToBroadcaster();
                    }else {
                        //被下麦后，设为观众角色
                        PolyvLinkMicWrapper.getInstance().switchRoleToAudience();
                    }
                }
                if(isInvitedToLinkMic){
                    PolyvJoinInfoEvent joinInfoEvent = joinRequests.get(polyvPPTAuthentic.getUserId());
                    if (joinInfoEvent != null) {
                        PolyvJoinInfoEvent.ClassStatus classStatus = joinInfoEvent.getClassStatus();
                        if (classStatus == null) {
                            classStatus = new PolyvJoinInfoEvent.ClassStatus();
                        }
                        classStatus.setVoice(1);
                        joinInfoEvent.setClassStatus(classStatus);
                    }
                    PolyvCommonLog.d(TAG, "polyvPPTAuthentic has vocie permission " + polyvPPTAuthentic.getUserId());
                    processJoinStatus(polyvPPTAuthentic.getUserId());
                }else {
                    processUserOffline(polyvPPTAuthentic.getUserId());
                }

            } else {

            }
        }

    }

    private void processLoginMessage(String message, String event) {
        PolyvCommonLog.d(TAG,"receive login message:"+message);
        PolyvLoginEvent loginEvent = PolyvEventHelper.getEventObject(PolyvLoginEvent.class, message, event);
        if(!polyvChatManager.userId.equals(loginEvent.getUser().getUserId())){
            return;
        }
        if (loginEvent != null) {
            roomId = loginEvent.getUser().getRoomId();
        }

        //登录成功后 发送user 给ppt
        PolyvDemoClient.getInstance().setLoginEvent(loginEvent);
        sendPPTWebChatlogin(loginEvent);

        sendReJoinMessage();
    }


    private void processTeacherInfo(String message) {
        PolyvTeacherInfo joinInfoEvent = PolyvGsonUtil.fromJson(PolyvTeacherInfo.class, message);
        teacherId = joinInfoEvent.getData().getUserId();
        PolyvCommonLog.e(TAG, "teacher id is " + teacherId);
        PolyvDemoClient.getInstance().setTeacher(joinInfoEvent);

    }

    public void processJoinLeaveMessage(String message) {
        PolyvJoinLeaveSEvent polyvJoinLeaveSEvent = PolyvGsonUtil.fromJson(PolyvJoinLeaveSEvent.class, message);
        if (polyvJoinLeaveSEvent != null && polyvJoinLeaveSEvent.getUser() != null) {
            processLeaveMessage(polyvJoinLeaveSEvent.getUser().getUserId());
            if (polyvJoinLeaveSEvent.getUser().getUserId().equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())) {
                controller.cancleLinkUpTimer();
            }
        }

    }

    public void processJoinResponseMessage() {
        PolyvLinkMicWrapper.getInstance().joinChannel("");
        controller.enableLinkBtn(true);
        controller.updateLinkBtn2Ready(true);
        startLinkTimer(false);
    }

    public void processJoinRequestMessage(String message) {
        PolyvJoinRequestSEvent joinRequestSEvent = PolyvGsonUtil.fromJson(PolyvJoinRequestSEvent.class, message);
        if (joinRequestSEvent != null && joinRequestSEvent.getUser() != null) {
            joinRequests.put(joinRequestSEvent.getUser().getUserId(), joinRequestSEvent.getUser());
            //可靠消息
            if (joinRequestSEvent.getUser().getUserId().equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())) {
                PolyvCommonLog.d(TAG, joinRequestSEvent.getUser().getUserId() +
                        PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid);
                controller.cancleLinkUpTimer();
            }
        }
    }

    public void processSliceIdMessage(String message) {
        sliceIdVo = PolyvGsonUtil.fromJson(PolyvSocketSliceIdVO.class, message);

        if (sliceIdVo != null && sliceIdVo.getData() != null) {
            this.sessionId = sliceIdVo.getData().getSessionId();
            initialCameraStatus();
        }

        getLinkMicJoins(false);
    }

    public String getSessionId() {
        if (videoView != null && videoView.getModleVO() != null && videoView.getModleVO().getChannelSessionId() != null) {
            return videoView.getModleVO().getChannelSessionId();
        }
        return sessionId;
    }

    public String getRoomId() {
        if (videoView != null && videoView.getModleVO() != null && videoView.getModleVO().getChannelId() != 0) {
            return videoView.getModleVO().getChannelId() + "";
        }
        return roomId;
    }

    /**
     * 此处版本迭代后  没有通知 协议表现发生变化
     * version 1：该协议用于客户端交换摄像头  sdk进行响应
     * version 2：该协议用于客户端更改主讲人 sdk进行响应 响应者为主讲人
     * @param message
     */
    private void processSwitchView(final String message) {
        S_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                PolyvLinkMicSwitchView switchView = PolyvGsonUtil.fromJson(PolyvLinkMicSwitchView.class, message);

                if (polyvLinkMicAdapter != null) {
                    if (!polyvLinkMicAdapter.changeTeacherLogo(switchView.getUserId(), true)) {
                       if(mainScreenLinkView != null){
                           View updateView = mainScreenLinkView.findViewById(R.id.teacher_logo);
                           if (updateView != null) {
                               updateView.setVisibility(VISIBLE);
                               polyvLinkMicAdapter.updateTeacherLogoView(updateView);
                           }
                       }

                    }
                    if (!subShowPPT) {//如果副屏不显示的是ppt
                        polyvLinkMicAdapter.switchView(switchView.getUserId());
                    } else {
                        //如果没有切换过主副屏
                        if (mainScreenLinkView == null) {
                            mainScreenLinkView = pptView;
                        }
                        //教师位置默认在第一位（或者是在sdk大屏），当重新进来的时候，教师从小屏切换到主屏（pc） 就不需要调整
                        if (mainScreenLinkView != null && switchView.getUserId().equals((String) mainScreenLinkView.getTag())) {
                            return;
                        }
                        ViewGroup cameraView = polyvLinkMicAdapter.getSwitchView(switchView.getUserId());
                        try {
                            if (cameraView == null) {
                                return;
                            }
                            linkMicSelected = cameraView;
                            changeLinkMicView(subShowPPT);

                        } catch (Exception e) {
                            PolyvCommonLog.e(TAG, e.getMessage());
                        }
                    }
                }
            }
        });

    }

    public void setMainScreenSize(ViewGroup cameraView) {
        View cameraParent = cameraView.findViewById(R.id.polyv_link_mic_camera_layout);
        if (cameraParent == null) {
            cameraParent = mainScreenLinkView.findViewById(R.id.polyv_link_mic_camera_layout);
        }
        if (cameraParent == null) {
            PolyvCommonLog.e(TAG, "cannot find link parent");
            return;
        }
        cameraParent.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initialCameraStatus() {
        cameraOpen = sliceIdVo.getData().getIsCamClosed() == 0;
        if (polyvLinkMicAdapter == null) {
            return;
        }
        polyvLinkMicAdapter.updateCameraStatus(cameraOpen);
    }

    //是否需要去更新连麦人列表 第一次进来不用 只获取数据
    private void getLinkMicJoins(final boolean needUpdate) {
        cancleGetLinkMicJoinsTask();

        getLinkMicJoins = PolyvLinkMicWrapper.getInstance().getLinkStatus(
                new PolyvrResponseCallback<PolyvLinkMicJoinStatus>() {

                    @Override
                    public void onSuccess(PolyvLinkMicJoinStatus data) {
                        initJoinDatas(data);

                        if (needUpdate) {
                            processJoinUnCachesStatus();
                        }else {
//                            PolyvRxBus.get().post();
                        }
                    }
                },
                getRoomId(), getSessionId(),true
        );
    }

    private void processJoinUnCachesStatus() {
        Iterator<String> iterator = noCachesIds.iterator();
        while (iterator.hasNext()) {
            String longUid = iterator.next();
            PolyvJoinInfoEvent joinInfoEvent = joinRequests.get(longUid);
            if (joinInfoEvent != null) {
                iterator.remove();
//                if(joinRequests.containsKey(longUid)){
                    polyvLinkMicAdapter.addData(joinRequests.get(longUid), true);
//                }
                PolyvCommonLog.d(TAG, "processJoinUnCachesStatus :" + longUid);
            }
        }

//        if(size >0){
//            polyvLinkMicAdapter.notifyItemRangeChanged(polyvLinkMicAdapter.getItemCount()-size, size);
//        }
//        linkMicLayoutParent.scrollToPosition(polyvLinkMicAdapter.getItemCount() - 1,linkMicLayout);

        cancleLinkTimer();
        changeViewToRtc(true);
    }

    private void cancleGetLinkMicJoinsTask() {
        if (getLinkMicJoins != null) {
            getLinkMicJoins.dispose();
            getLinkMicJoins = null;
        }
    }

    private void processMicSlice(String message) {
//        PolyvSocketSliceControlVO polyvSocketSliceControl = PolyvGsonUtil.
//                fromJson(PolyvSocketSliceControlVO.class, message);
//        if (polyvSocketSliceControl != null && polyvSocketSliceControl.getData() != null) {
//            cameraOpen = polyvSocketSliceControl.getData().getIsCamClosed() == 0;
//            if (polyvLinkMicAdapter == null) {
//                return;
//            }
//            polyvLinkMicAdapter.updateCameraStatus(cameraOpen);
//            View surfaceView = polyvLinkMicAdapter.getCameraView();
//
//            S_HANDLER.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (surfaceView != null) {//关闭打开摄像头
//                        surfaceView.setVisibility(polyvSocketSliceControl.getData().getIsCamClosed() == 0 ? VISIBLE : INVISIBLE);
//                    }
//                }
//            });
//
//        }
    }

    private void processMicPhone(PolyvMicphoneStatus micphoneStatus) {
        if (micphoneStatus == null) {
            return;
        }
//
//        if(PolyvLinkMicWrapper.getInstance().getLinkMicUid().equals(micphoneStatus.getUserId())){
//            videoView.setLinkType(micphoneStatus.getType());
//        }
        videoView.setLinkType(micphoneStatus.getType());
        String type = micphoneStatus.getType();
        if (("Video".equals(type) || "Audio".equals(type))
                && "close".equals(micphoneStatus.getStatus())) {//挂断
//                restartPlay();//用restart比较好
            processLeaveMessage(micphoneStatus.getUserId());
        } else if(("Video".equals(type) || "Audio".equals(type))
                && "open".equals(micphoneStatus.getStatus())){
            if(isParticipant && !joinSuccess){
                controller.performClickLinkMic();
            }
        }else if (("audio".equals(type) || "video".equals(type))
                && "open".equals(micphoneStatus.getStatus())) {//打开连麦

            if(TextUtils.isEmpty(micphoneStatus.getUserId())){
                PolyvTeacherStatusInfo live = new PolyvTeacherStatusInfo();
                live.setWatchStatus(PolyvLiveClassDetailVO.LiveStatus.LIVE_OPENCALLLINKMIC);
                PolyvRxBus.get().post(live);
            }

            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    controller.updateLinkBtn2Ready(false);
                    controller.enableLinkBtn(true);
                }
            });

            if(micphoneStatus.getUserId() != null &&
                    !micphoneStatus.getUserId().equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())){
                return;
            }
            if(isParticipant && !joinSuccess){
                controller.performClickLinkMic();
            }
        }else if (("audio".equals(type) || "video".equals(type))
                && "close".equals(micphoneStatus.getStatus())) {//断开连麦
            if(isParticipant){//当连麦者是参与者得时候 不执行退出操作
                if(PolyvLinkMicWrapper.getInstance().getLinkMicUid().equals(micphoneStatus.getUserId())){
                    sendJoinSuccess();
                }
                return;
            }

            if(micphoneStatus.getUserId() != null &&
                    !micphoneStatus.getUserId().equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())){
                return;
            }

            if(TextUtils.isEmpty(micphoneStatus.getUserId())){
                PolyvTeacherStatusInfo live = new PolyvTeacherStatusInfo();
                live.setWatchStatus(PolyvLiveClassDetailVO.LiveStatus.LIVE_CLOSECALLLINKMIC);
                PolyvRxBus.get().post(live);
            }

            PolyvLinkMicWrapper.getInstance().leaveChannel();

            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (controller != null) {
//                        controller.enableLinkBtn(false);
                    }
                }
            });

        }
    }

    public void processMediaMessage(PolyvLinkMicMedia micMedia) {
        if (micMedia == null) {
            return;
        }

        if ("video".equals(micMedia.getType())) {
            if(polyvLinkMicAdapter.getOwnerView() != null){
                View owernCamera = polyvLinkMicAdapter.getOwnerView().findViewById(R.id.polyv_link_mic_camera_layout);
                polyvLinkMicAdapter.showCameraOffLineView(micMedia.isMute());
            }
            ToastUtils.showShort(micMedia.isMute() ? "摄像头已关闭" : "摄像头已打开");
            PolyvLinkMicWrapper.getInstance().muteLocalVideo(micMedia.isMute());
        } else {
            ToastUtils.showShort(micMedia.isMute() ? "麦克风已关闭" : "麦克风已打开");
            polyvLinkMicAdapter.showMicOffLineView(micMedia.isMute());
            PolyvLinkMicWrapper.getInstance().muteLocalAudio(micMedia.isMute());
        }
    }

    //中途进来需要先添加已经在连麦列表的人
    private void initJoinDatas(PolyvLinkMicJoinStatus data) {
        if (data == null) {
            return;
        }

        List<PolyvJoinInfoEvent> joinListBeans = data.getJoinList();
        for (PolyvJoinInfoEvent joinListBean : joinListBeans) {
            PolyvCommonLog.e(TAG, "join id is:" + joinListBean.getUserId());
            if(!joinRequests.containsKey(joinListBean.getUserId())) {
                joinRequests.put(joinListBean.getUserId(), joinListBean);
                if ("teacher".equals(joinListBean.getUserType())) {
                    teacherId = joinListBean.getUserId();
                    PolyvCommonLog.e(TAG, "teacher id is " + teacherId);
                }
            }
        }
    }

    private void registerSocketEventListener() {
        PolyvChatManager.getInstance().addNewMessageListener(new PolyvNewMessageListener() {
            @Override
            public void onNewMessage(String message, String event) {
                if (PolyvChatManager.EVENT_SEND_CUP.equals(event)) {
                    PolyvSendCupEvent sendCupEvent = PolyvEventHelper.getEventObject(PolyvSendCupEvent.class, message, event);
                    if (sendCupEvent != null && sendCupEvent.getOwner() != null && sendCupEvent.getOwner().getUserId() != null) {
                        if (joinRequests != null) {
                            for (PolyvJoinInfoEvent joinInfoEvent : joinRequests.values()) {
                                if (sendCupEvent.getOwner().getUserId().equals(joinInfoEvent.getLoginId())) {
                                    joinInfoEvent.setCupNum(sendCupEvent.getOwner().getNum());
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onDestroy() {
            }
        });

        // 进入直播间时 主副屏跟随讲师端显示
        PolyvChatManager.getInstance().addNewMessageListener(new PolyvNewMessageListener() {
            @Override
            public void onNewMessage(String message, String event) {
                if (ONSLICEID.equals(event)) {
                    PolyvChatManager.getInstance().removeNewMessageListener(this);

                    JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();
                    // pptAndVideoPosition 0表示讲师端目前ppt在主屏 1表示讲师端目前播放器在主屏
                    int pptAndVideoPosition = jsonObject.get("pptAndVedioPosition").getAsInt();
                    // 跟随讲师端主副屏位置
                    boolean needToChangePptVideoPosition = pptAndVideoPosition == 0 ^ pptShowMainScreen();
                    if (needToChangePptVideoPosition) {
                        controller.changePPTVideoLocationUncheckPPT();
                    }
                }
            }

            @Override
            public void onDestroy() {

            }
        });
    }

    private void processLeaveMessage(String userId) {

        if (userId.equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())) {
            PolyvCommonLog.d(TAG, "processLeaveMessage");
            PolyvLinkMicWrapper.getInstance().leaveChannel();
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showLong("关闭连麦");
                    if (controller!=null){
                        controller.updateLinkBtn2Ready(false);
                    }
                    startLinkTimer(true);
                    linkMicLayout.setKeepScreenOn(false);
                }
            });

        }
    }

    //20秒内如果定时器被取消 则表明joinchannel rtc的回掉成功 加入成功 否则 置位
    private void startLinkTimer(final boolean leave) {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
        }
        linkJoinTimer = PolyvRxTimer.delay(LINK_JOIN_TIME, new Consumer<Long>() {
            @Override
            public void accept(Long l) throws Exception {
                if (leave) {
                    PolyvLinkMicWrapper.getInstance().leaveChannel();
                    if (controller!=null){
                        controller.updateLinkBtn2Ready(false);
//                        controller.enableLinkBtn(false);
                    }
                } else {
//                    PolyvLinkMicWrapper.getInstance().joinChannel("");
                    if (controller!=null){
                        controller.updateLinkBtn2Ready(true);
                        controller.enableLinkBtn(true);
                    }
                }
            }
        });
    }

    public void cancleLinkTimer() {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
            linkJoinTimer = null;
        }
        if(viewerJoinLinkDispose != null){
            viewerJoinLinkDispose.dispose();
            viewerJoinLinkDispose = null;
        }

    }

    @Override
    public void onDestroy() {
        if (delayToJoinAsParticipant!=null){
            delayToJoinAsParticipant.dispose();
        }
        PolyvDemoClient.getInstance().onDestory();
    }

    @Override
    public boolean changePPTViewToVideoView(boolean switchOpen) {
        if (!joinSuccess) {
            return super.changePPTViewToVideoView(switchOpen);
        } else {
            changeLinkMicView(subShowPPT);
        }

        return false;
    }

    private void changeLinkMicView(boolean changeToVideoView) {
        try {

            //主屏默认是ppt
            if (mainScreenLinkView == null) {
                mainScreenLinkView = pptView;
            }

            //更新画笔刷与画笔权限状态
            updateBrushStatusWithPPTChange();

            ViewGroup linkMicParent = (ViewGroup) linkMicSelected.getParent();//连麦的父布局
            ViewGroup mainScreenParent = videoView;//主屏的父布局

            linkMicParent.removeView(linkMicSelected);
            mainScreenParent.removeView(mainScreenLinkView);

            setMainScreenSize(linkMicSelected);

            SurfaceView surfaceView = (SurfaceView) polyvLinkMicAdapter.getCameraView(linkMicSelected);
            if (surfaceView != null) {
                surfaceView.setZOrderOnTop(changeToVideoView);
                surfaceView.setZOrderMediaOverlay(changeToVideoView);
            }

            mainScreenParent.addView(linkMicSelected, 0, new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            linkMicParent.addView(mainScreenLinkView, 0, new ViewGroup.LayoutParams
                    (PolyvScreenUtils.getItemWidth(), PolyvScreenUtils.getItemHeight()));

            startAnimation(mainScreenParent);

            polyvLinkMicAdapter.updateSwitchViewStatus((String)mainScreenLinkView.getTag(),(String)linkMicSelected.getTag());
            mainScreenLinkView = linkMicSelected;

        } catch (Exception e) {
            PolyvCommonLog.e(TAG, e.getMessage());
        }

    }

    private void updateBrushStatusWithPPTChange() {
        PolyvPPTAuthentic polyvPPTAuthentic = new PolyvPPTAuthentic();
        if(mainScreenLinkView == pptView){
            //ppt从主屏切换下去  隐藏画笔功能
            if(homeProtocol != null){
                homeProtocol.updatePaintStatus(false);
            }
            polyvPPTAuthentic.setStatus( "0" );
            sendAuthenticMessage(polyvPPTAuthentic);

        }else if(linkMicSelected == pptView){
            if(homeProtocol != null){
                homeProtocol.updatePaintStatus(true);
            }

            if(this.polyvPPTAuthentic == null){
                polyvPPTAuthentic.setStatus( "0" );
            }else {
                polyvPPTAuthentic.setStatus( this.polyvPPTAuthentic.hasNoAthuentic()?"0":"1" );

            }
            sendAuthenticMessage(polyvPPTAuthentic);
        }
    }

    private void sendAuthenticMessage(PolyvPPTAuthentic polyvPPTAuthentic) {
        if(this.polyvPPTAuthentic != null && this.polyvPPTAuthentic.hasPPTOrAboveType()){

            if(pptView != null){
                pptView.updateBrushPermission(PolyvGsonUtil.toJson(polyvPPTAuthentic));
            }
        }
    }

    PolyvLinkMicAGEventHandler polyvLinkMicAGEventHandler = new PolyvLinkMicAGEventHandler() {
        @Override
        public void onAudioVolumeIndication(final PLVARTCAudioVolumeInfo[] speakers, final int totalVolume) {
            super.onAudioVolumeIndication(speakers, totalVolume);
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (polyvLinkMicAdapter != null) {
                        polyvLinkMicAdapter.startAudioWave(speakers, totalVolume);
                    }
                }
            });
        }

        @Override
        public void onAudioQuality(int uid, int quality, short delay, short lost) {
            super.onAudioQuality(uid, quality, delay, lost);

        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    PolyvCommonLog.d(TAG, "uid:" + uid);
                    if (uid == PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid) {
                        PolyvCommonLog.d(TAG, "receive owner uid");
                        return;
                    }
                }
            });

        }

        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    long longUid = uid & 0xFFFFFFFFL;
                    if(!isParticipant){
                        polyvLinkMicAdapter.addOwner(longUid + "", joinRequests.get(longUid + ""));
                        linkMicParent.updateBottomController(true);
                    }else {
                        linkMicParent.updateBottomController(false);
                        PolyvLinkMicWrapper.getInstance().muteLocalAudio(true);
                        PolyvLinkMicWrapper.getInstance().muteLocalVideo(true);
                    }
                    //显示RTC连麦区域
                    showRtcView(true, teacherId);
                    //发送连麦成功socket
                    sendJoinSuccess();
                    //取消连麦倒计时
                    cancleLinkTimer();
                    //隐藏副屏幕
                    hideSubView(true);
                    //更新控制栏得连麦状态
                    updateLinkMicStatus(true);
                    //暂停cdn流
                    pause();
                    muteMusicStreamWhenJoinChannel();
                    //显示连麦区域
                    changeViewToRtc(true);

                    //更新ppt 延迟显示时间
                    if (pptView != null) {
                        pptView.updateDelayTime(0);
                    }
                    linkMicLayout.setKeepScreenOn(true);
                    controller.onJoinLinkMic();
                    videoView.setNeedGestureDetector(false);

                    //更新聊天室得位置
                    updateChatLocation(true);

                }
            });

        }

        @Override
        public void onLeaveChannel() {
            PolyvCommonLog.d(TAG, "onLeaveChannel");
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
//                    if(!joinSuccess){
//                        return;
//                    }
                    PolyvCommonLog.d(TAG, "onLeaveChannel success");
                    showRtcView(false, null);
                    updateLinkMicStatus(false);
                    cancleLinkTimer();
                    restartPlay();//restartPlay();
                    unmuteMusicStreamWhenLeaveChannel();
                    showSubView();
                    changeViewToRtc(false);
                    if (pptView != null) {
                        pptView.resetDelayTime();
                    }
                    linkMicLayout.setKeepScreenOn(false);
                    joinRequests.remove(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
                    controller.onLeaveLinkMic();
                    videoView.setNeedGestureDetector(true);

                    joinRequests.clear();
                    noCachesIds.clear();

                    //更新聊天室得位置
                    updateChatLocation(false);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            PolyvCommonLog.d(TAG, "onUserOffline");
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    long longUid = uid & 0xFFFFFFFFL;
                    PolyvJoinInfoEvent joinInfoEvent = joinRequests.remove(longUid);
                    if (joinInfoEvent != null) {
                        ToastUtils.showLong(joinInfoEvent.getNick() + "离开连麦室");
                    }

                    processUserOffline(longUid+"");
                }
            });


        }

        @Override
        public void onUserJoined(final int uid, final int elapsed) {
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(getRoomId())) {
                        ToastUtils.showLong("请重新登录 获取正确状态");
                        leaveChannel();
                        return;
                    }
                    long longUid = uid & 0xFFFFFFFFL;
                    PolyvCommonLog.d(TAG,"onUserJoined:"+longUid);
                    processJoinStatus(longUid+"");
                }
            });
        }

        @Override
        public void onUserMuteVideo(int uid, final boolean mute) {
            final long longUid = uid & 0xFFFFFFFFL;
            final PolyvJoinInfoEvent joinInfo = joinRequests.get(longUid + "");
            LogUtils.d("onUserMuteVideo");
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String nick = "";
                    if (joinInfo != null) {
                        nick = joinInfo.getNick();
                        int pos = joinInfo.getPos();
                        joinInfo.setMute(mute);
                        //如果被mute的用户还在列表中，才去执行将他从列表中移除。因为参与者条件下，收到TEACHER_SET_PERMISSION事件后，会将被下麦的观众从列表中移除掉。
                        if (polyvLinkMicAdapter.getJoinsPos(longUid + "") >= 0) {
                            if (!polyvLinkMicAdapter.notifyItemChanged(pos, mute)) {
                                if (mainScreenLinkView != null) {

                                    SurfaceView surfaceView = mainScreenLinkView.findViewById(CAMERA_VIEW_ID);
                                    if (surfaceView != null) {
                                        surfaceView.setVisibility(mute ? INVISIBLE : VISIBLE);
                                    }
                                }
                            }
                        }
                    }else {
                        polyvLinkMicAdapter.addUnhandledMutedVideoUser(longUid+"");
                    }
                }
            });
        }

        @Override
        public void onUserMuteAudio(int uid, final boolean mute) {
            final long longUid = uid & 0xFFFFFFFFL;
            final PolyvJoinInfoEvent joinInfo = joinRequests.get(longUid + "");

            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String nick = "";
                    if (joinInfo != null) {
                        nick = joinInfo.getNick();
                        int pos = joinInfo.getPos();
//                    joinInfo.setMute(mute);
                        if (pos >= 0) {//不是老师的更新

                            if(!polyvLinkMicAdapter.showMicOffLineView(mute, pos)){
                                if(mainScreenLinkView != null){
                                    View muteView =  mainScreenLinkView.findViewById(R.id.polyv_camera_switch);
                                    if(muteView != null){
                                        muteView.setVisibility(mute?View.VISIBLE:View.INVISIBLE);
                                    }
                                }
                            };
                        }
                        PolyvCommonLog.d(TAG, "pos :" + pos);
                    }else {
                        polyvLinkMicAdapter.addUnHandledMutedAudioUser(longUid+"");
                    }
                }
            });

        }

    };

    private void muteMusicStreamWhenJoinChannel() {
        curMusicStreamVolume = videoView.getVolume();
        if (videoView != null) {
            videoView.setVolume(0);
        }
    }

    private void unmuteMusicStreamWhenLeaveChannel() {
        if (curMusicStreamVolume != -1) {
            if (videoView != null) {
                videoView.setVolume(curMusicStreamVolume);
            }
        }
    }


    private void updateChatLocation(boolean downChat) {

        if (homeProtocol != null) {
            homeProtocol.moveChatLocation(downChat);
        }

        if (polyvLinkMicAdapter != null) {
            polyvLinkMicAdapter.updateLayoutStyle(context.getResources().getConfiguration().orientation);
        }
    }

    private void showRtcView(boolean show, String teacherId) {
        linkMicLayout.setVisibility(show ? VISIBLE : View.GONE);
        ((ViewGroup) linkMicLayoutParent.getOwnView().getParent()).setVisibility(show ? VISIBLE : View.GONE);
        linkMicLayoutParent.enableShow(show);

        if (showPPT) {
            PolyvCommonLog.e(TAG, "is not teacher");
            return;
        }
        if (!supportRTC) {//
            PolyvCommonLog.e(TAG, "live is not support rtc live");
            return;
        }
        SurfaceView surfaceView = videoView.findViewById(RTC_VIEW_ID);
        if (surfaceView==null){
            surfaceView=addRTCView();
        }
        surfaceView.setVisibility(show ? VISIBLE : INVISIBLE);
        try {
            if (show) {
                PolyvLinkMicWrapper.getInstance().setupRemoteVideo(surfaceView,
                        PLVARTCConstants.RENDER_MODE_FIT, PolyvFormatUtils.parseInt(teacherId));
            }
        } catch (Exception e) {
            PolyvCommonLog.exception(e);
        }
    }

    public void processUserOffline(String longUid) {
        PolyvJoinInfoEvent joinInfoEvent = joinRequests.get(longUid);
        if (joinInfoEvent != null) {
            PolyvJoinInfoEvent.ClassStatus classStatus = joinInfoEvent.getClassStatus();
            if (classStatus == null) {
                classStatus = new PolyvJoinInfoEvent.ClassStatus();
            }
            classStatus.setVoice(0);
            joinInfoEvent.setClassStatus(classStatus);
        }

        int pos = polyvLinkMicAdapter.getJoinsPos(longUid);

        //主屏的连麦者被移除  讲师移到主屏
        if (mainScreenLinkView != null && !TextUtils.isEmpty((String)mainScreenLinkView.getTag()) &&subShowPPT) {
            linkMicSelected = pptView;
            changeLinkMicView(subShowPPT);
        }

        polyvLinkMicAdapter.removeData(longUid + "", true);

    }

    private void processJoinStatus(String longUid) {

        if (!joinRequests.containsKey(longUid)) {//不包含 需要更新数据
//            PolyvJoinInfoEvent defaultEvent = createDefaultJoin(longUid);
//            joinRequests.put(longUid , defaultEvent);
            noCachesIds.add(longUid);

            //3秒以后去查询 数据  以免同时多个人加入
            startJoinListTimer();
            return;
        }
        if (polyvLinkMicAdapter!=null){
            polyvLinkMicAdapter.addData(joinRequests.get(longUid), true);
        }

//        linkMicLayoutParent.scrollToPosition(polyvLinkMicAdapter.getItemCount() - 1,linkMicLayout);

        cancleLinkTimer();
        changeViewToRtc(true);
    }

    @NonNull
    private PolyvJoinInfoEvent createDefaultJoin(String longUid) {
        PolyvJoinInfoEvent defaultEvent = new PolyvJoinInfoEvent();
        defaultEvent.setUserId(longUid);
        defaultEvent.setNick("");
        defaultEvent.setUserType(JOIN_DEFAULT_TYPE);
        return defaultEvent;
    }

    private void startJoinListTimer() {
        cancleJoinListTimer();
        joinListTimer = PolyvRxTimer.delay(3 * 1000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                getLinkMicJoins(true);
            }
        });
    }

    private void cancleJoinListTimer() {
        if (joinListTimer != null) {
            joinListTimer.dispose();
            joinListTimer = null;
        }
    }

    private void updateLinkMicStatus(boolean isJoinSuccess) {
        this.joinSuccess = isJoinSuccess;
        if (videoItem != null && linkMicLayoutParent != null) {
            videoItem.notifyLinkMicStatusChange(isJoinSuccess);
        }
    }

    private void sendJoinSuccess() {
        polyvChatManager.sendJoinSuccessMessage(getSessionId(), PolyvLinkMicWrapper.getInstance().getLinkMicUid());
    }

    private void showSubView() {
        if (pptContianer != null) {
            pptContianer.setVisibility(VISIBLE);
        }
        if (polyvChatManager != null) {
            polyvChatManager.sendJoinLeave(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
        }

        if (subShowPPT) {
            linkMicSelected = pptView;
            changeLinkMicView(subShowPPT);
        }
    }

    /**
     * 隐藏副屏
     *
     * @param resetPPTLocation 如果ppt 在副屏显示 是否需要更正ppt到主屏
     */
    public void hideSubView(boolean resetPPTLocation) {

        if (pptContianer != null) {
            pptContianer.setVisibility(INVISIBLE);
        }
        if (controller == null || !resetPPTLocation) {
            return;
        }
        if (controller.isPPTSubView()) {
            controller.changePPTVideoLocation();
        }

    }

    private void changeViewToRtc(boolean change) {
        linkMicLayout.setVisibility(change ? VISIBLE : INVISIBLE);
        if (!change) {
            subShowPPT = false;
            mainScreenLinkView = null;
            linkMicLayout.removeAllViews();
            polyvLinkMicAdapter.clear();
        }
    }


    private void showDialog(String title, String message, final boolean isRequestSetting, String[] permissions) {
        String tipsMessage = permissions.length == 2 ? String.format(message, "录音和相机")
                : (Manifest.permission.CAMERA.equals(permissions[0]) ? String.format(message, "相机")
                : String.format(message, "录音"));
        new AlertDialog.Builder(context).setTitle(title)
                .setMessage(tipsMessage)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isRequestSetting)
                            permissionManager.requestSetting();
                        else
                            permissionManager.request();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "权限不足，申请发言失败", Toast.LENGTH_SHORT).show();
                    }
                }).setCancelable(false).show();
    }

    public void bindCallMicView(ImageView callMicView) {
        controller.setCallMicView(callMicView);
    }

    //初始化连麦模块配置
    private void initLinkMic(){
        if (isLinkMicInit){
            return;
        }
        isLinkMicInit=true;
        PolyvLinkMicWrapper.getInstance().init(Utils.getApp());
        PolyvLinkMicWrapper.getInstance().intialConfig(channelId);
        PolyvLinkMicWrapper.getInstance().addEventHandler(polyvLinkMicAGEventHandler);
    }

    @Override
    public void onGranted() {
        List<String> permissions = new ArrayList<String>();
        int[] ops = new int[]{PolyvPermissionManager.OP_CAMERA, PolyvPermissionManager.OP_RECORD_AUDIO};
        boolean checkOp = permissionManager.checkGrandedPermissions(context, ops, permissions);
        if (!checkOp) {
            showDialog("提示", "通话所需的%s权限被拒绝，请到应用设置的权限管理中恢复", true, permissions.toArray(new String[permissions.size()]));
            return;
        }
        PolyvCommonLog.d(TAG, "onGranted");

        initLinkMic();

        if(isParticipant){
           if(!joinSuccess){
               //延迟2.5秒了再以参与者身份加入连麦。
               delayToJoinAsParticipant=PolyvRxTimer.delay(2500, new Consumer<Object>() {
                   @Override
                   public void accept(Object o) throws Exception {
                       joinLinkByParticipant();
                   }
               });
           }else {
               controller.showStopLinkDialog(joinSuccess,false);
           }
        }else {
            controller.handsUp(joinSuccess);
        }
        return;
    }

    private void joinLinkByParticipant() {
        initSupportRTC();
        if(!TextUtils.isEmpty(PolyvVClassGlobalConfig.viewerId)){
            PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid = PolyvFormatUtils.parseInt(PolyvVClassGlobalConfig.viewerId);
        }
        createLinkMicLayout(linkMicLayout, true);

        updateLinkMicStatus(false);
        if (polyvLinkMicAdapter != null) {
            isAudio= "audio".equals(videoView.getLinkMicType());
            linkMicParent.updateLinkController(!isAudio);
            polyvLinkMicAdapter.setAudio(isAudio);
            polyvLinkMicAdapter.bindLinkMicFrontView(linkMicLayoutParent.getOwnView());
        }
        PolyvLinkMicWrapper.getInstance().muteLocalVideo("audio".equals(videoView.getLinkMicType()));
        //参与者加入时，将角色调整为观众角色
        PolyvLinkMicWrapper.getInstance().switchRoleToAudience();
        processJoinResponseMessage();
    }

    @Override
    public void onDenied(String[] permissions) {
        permissionManager.showDeniedDialog(context, permissions);
    }

    @Override
    public void onShowRationale(String[] permissions) {
        permissionManager.showRationaleDialog(context, permissions);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            permissionManager.request();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            permissionManager.onPermissionResult(permissions, grantResults);
        }
    }

    @Override
    public void destory() {
        super.destory();
        cancleLinkTimer();
        cancleGetLinkMicJoinsTask();
        cancleJoinListTimer();
        clearLinkStatus();
        clearStatus();
    }

    private void clearStatus() {
        homeProtocol = null;
        if(compositeDisposable != null){
            compositeDisposable.clear();
        }
    }

    private void clearLinkStatus() {
        if (joinSuccess) {
            PolyvLinkMicWrapper.getInstance().leaveChannel();
        }

        linkMicLayoutParent.setVisibility(INVISIBLE);
        linkMicLayout.removeAllViews();
        if (polyvLinkMicAdapter != null) {
            polyvLinkMicAdapter.clear();
        }

        polyvChatManager.removeNewMessageListener(this);
        PolyvLinkMicWrapper.getInstance().removeEventHandler(polyvLinkMicAGEventHandler);
    }

    public void addLinkMicLayout(PolyvLinkMicParent polyvLinkMicParent) {
        this.linkMicParent = polyvLinkMicParent;
        this.linkMicLayout = polyvLinkMicParent.getLinkMicLayout();
        this.linkMicLayoutParent = polyvLinkMicParent.getLinkMicView();


    }

    private void createLinkMicLayout(ViewGroup linkMicLayout, boolean supportRTC) {
        //云课堂直播 或者是支持rtc得普通直播进行视频连麦
//        ViewGroup.LayoutParams layoutParams = linkMicRegion.getLayoutParams();
        if (showPPT || (supportRTC)) {//&& !"audio".equals(videoView.getLinkMicType())
            polyvLinkMicAdapter = new PolyvLinkMicDataBinder
                    (PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid + "", !showPPT);
//            layoutParams.height = PolyvScreenUtils.getItemHeight();
        } else {
            polyvLinkMicAdapter = new PolyvNormalLiveLinkMicDataBinder
                    (PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid + "");
//            layoutParams.height = PolyvScreenUtils.dip2px(context,0);
        }

//        linkMicRegion.setLayoutParams(layoutParams);

        linkMicLayoutParent.setLinkType(videoView.getLinkMicType());
        PolyvLinkMicWrapper.getInstance().setPPTStatus(showPPT);

        polyvLinkMicAdapter.addParent(linkMicLayout);
        polyvLinkMicAdapter.updateCameraStatus(cameraOpen);
        polyvLinkMicAdapter.bindItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //即将要去主屏的试图
                linkMicSelected = ((ViewGroup) v).findViewById(R.id.polyv_link_mic_camera_layout);
                String uid = (String) v.getTag();
                if (linkMicSelected == null) {
                    //如果没有找到 说明已经连麦人已经切换到主屏  副屏显示的为ppt
                    linkMicSelected = pptView;
                }
                //如果自己變爲主講 需要发送消息到客户端 切换主副屏
//                if(uid.equals(PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid + "")){
//                    sendChangePPTAndVideoPosition();
//                }
                subShowPPT = linkMicSelected != null;
                controller.changePPTVideoLocation();
            }
        });
    }

    private void sendChangePPTAndVideoPosition() {
        if(isTeacherType){
            polyvChatManager.sendScoketMessage(SE_SWITCH_PPT_MESSAGE,polyvPPTAuthentic);
        }
    }

    /**
     * 更新控制栏 主副平切换按钮得状态  （切换 或者是 显示副屏）
     *
     * @param polyvSocketMessage
     */
    public void updateMainScreenStatus(String polyvSocketMessage, String event) {
        PolyvSocketSliceControlVO polyvSocketSliceControl = PolyvGsonUtil.
                fromJson(PolyvSocketSliceControlVO.class, polyvSocketMessage);
        if (polyvSocketSliceControl != null && polyvSocketSliceControl.getData() != null) {
            videoView.updateMainScreenStatus(polyvSocketSliceControl.getData().getIsCamClosed() == 0);

            if (polyvSocketSliceControl.getData().getIsCamClosed() == 1) {//关闭摄像头
                if (controller != null) {
                    controller.showCameraView();
                }
            }
        }

        if (pptView != null) {
            pptView.processSocketMessage(new PolyvSocketMessageVO(polyvSocketMessage, event));
        }
    }

    public void notifyOnConfigChangedListener(Configuration newConfig) {
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            controller.showTopController(false);
        }

        if (videoItem != null) {
            videoItem.notifyOnConfigChangedListener(newConfig);
        }

        if (polyvLinkMicAdapter != null) {
            polyvLinkMicAdapter.updateLayoutStyle(newConfig.orientation);
        }
    }


    public void addHomeProtocol(IPolyvHomeProtocol homeProtocol) {
        this.homeProtocol = homeProtocol;
    }
    // <editor-fold defaultstate="collapsed" desc="发送给ppt 的事件方法">
    //更新画板的触摸状态
    public boolean updateBrushStatus(boolean unSelected) {
        //如果主屏不是ppt  不触发画笔的显示状态
        if(!unSelected &&  mainScreenLinkView != null && mainScreenLinkView !=  pptView){
            ToastUtils.showLong("请将ppt切换到主屏再进行画笔操作");
            return false;
        }
        PolyvPPTAuthentic polyvPPTAuthentic = new PolyvPPTAuthentic();
        polyvPPTAuthentic.setStatus(unSelected ? "0" : "1");
        if (pptView != null) {

            //不是講師 一直获取权限不更新
            if (pptShowMainScreen() && !this.polyvPPTAuthentic.hasTeacherAthuentic()) {
                pptView.updateBrushPermission(PolyvGsonUtil.toJson(polyvPPTAuthentic));
            }
            //发送消息给web端
            pptView.sendWebMessage(PPT_PAINT_STATUS, "{\"status\":\"" + (unSelected ? "close\"" : "open\"") + "}");
        }

        return true;
    }

    //发送更新画笔颜色事件给web
    public void updateBrushColor(String color) {
        if (pptView != null) {
            pptView.sendWebMessage(CHANGE_COLOR, ""+color+"");
        }
    }

    private void sendPPTWebChatlogin(final PolyvLoginEvent loginEvent) {
        compositeDisposable.add(PolyvRxTimer.delay(1000, new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (pptView != null) {
                    pptView.sendWebMessage(CHAT_LOGIN, loginEvent.getUser().toString());
                }
            }
        }));

    }

    public void updateEraseStatus(boolean toErase) {
        if (pptView != null) {
            pptView.sendWebMessage(ERASE_STATUS, "{\"toDelete\":\"" + toErase + "\"}");
        }
    }

    //ppt是否显示在主屏
    public boolean pptShowMainScreen() {
        return (mainScreenLinkView == null || mainScreenLinkView ==  pptView);
    }

    //保存自动连麦标志位 监听连麦事件
    public void joinLink(boolean isParticipant) {
        this.isParticipant = isParticipant;
//        viewerJoinLinkDispose = PolyvRxBus.get().toObservable(PolyvTeacherStatusInfo.class).
//                subscribe(new Consumer<PolyvTeacherStatusInfo>() {
//                    @Override
//                    public void accept(PolyvTeacherStatusInfo polyvTeacherStatusInfo) throws Exception {
//                        String watchStatus = polyvTeacherStatusInfo.getWatchStatus();
//                        /**
//                         * 进来等待连麦状态 监听到连麦可用 马上连麦 然后销毁监听器
//                         * 如果不可用 等待连麦状态来临后 立马连麦
//                         */
//
//                        if (LIVE_OPENCALLLINKMIC.equals(watchStatus)) {
//                            controller.performClickLinkMic();
//                            viewerJoinLinkDispose.dispose();
//                            viewerJoinLinkDispose = null;
//                        }
//                    }
//                });

    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="is 方法">

    public boolean isJoinLinkMick() {
        return joinSuccess;
    }

    public boolean isParticipant(){
        return isParticipant;
    }

    public boolean isSupportRTC() {
        return supportRTC;
    }
    // </editor-fold>
}
