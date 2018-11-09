package com.easefun.polyv.cloudclassdemo.player;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.businesssdk.model.link.PolyvLinkMicMedia;
import com.easefun.polyv.businesssdk.model.link.PolyvMicphoneStatus;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclass.chat.event.linkmic.PolyvJoinLeaveSEvent;
import com.easefun.polyv.cloudclass.chat.event.linkmic.PolyvJoinRequestSEvent;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketSliceControlVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketSliceIdVO;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.adapter.PolyvLinkMicAdapter;
import com.easefun.polyv.commonui.PolyvCommonVideoHelper;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.widget.PolyvLinkMicListView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicAGEventHandler;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;
import com.easefun.polyv.linkmic.model.PolyvLinkMicJoinStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.OPEN_MICROPHONE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.EVENT_MUTE_USER_MICRO;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_LEAVE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_REQUEST;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_RESPONSE;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_JOIN_SUCCESS;

/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public class PolyvCloudClassVideoHelper extends PolyvCommonVideoHelper<PolyvCloudClassVideoItem,
        PolyvCloudClassVideoView, PolyvCloudClassMediaController> implements PolyvNewMessageListener, PolyvPermissionListener {
    private static final String TAG = "PolyvCloudClassVideoHelper";

    private static final int LINK_JOIN_TIME = 20 * 1000;//加入连麦的超时事件

    private static final String JOIN_DEFAULT_TYPE = "JOIN_DEFAULT_TYPE";
    private PolyvPermissionManager permissionManager;
    protected PolyvChatManager polyvChatManager;
    private Disposable linkJoinTimer,getLinkMicJoins;
    private static final int REQUEST_CODE = 612;
    private boolean joinSuccess, pptShowSub;
    private boolean camerShowInVideoView;//连麦是否显示再大屏

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

    private PolyvLinkMicListView linkMicLayout;
    private PolyvLinkMicAdapter polyvLinkMicAdapter;
    private Map<String, PolyvJoinInfoEvent> joinRequests = new ConcurrentHashMap<>();
    private PolyvSocketSliceIdVO sliceIdVo;
    private String sessionId = "";
    private boolean cameraOpen = true;
    private String teacherId;

    private Disposable joinListTimer;
    private Set<Long> noCachesIds = new HashSet<>();//在缓存中没有找到数据得uid

    public PolyvCloudClassVideoHelper(PolyvCloudClassVideoItem videoItem,
                                      PolyvPPTItem polyvPPTItem, PolyvChatManager polyvChatManager) {
        super(videoItem, polyvPPTItem);

        polyvChatManager.addNewMessageListener(this);
        this.polyvChatManager = polyvChatManager;
        PolyvLinkMicWrapper.getInstance().addEventHandler(polyvLinkMicAGEventHandler);

        permissionManager = PolyvPermissionManager.with((Activity) context)
                .permissions(permissions)
                .meanings(permissionsYips)
                .opstrs(ops)
                .addRequestCode(REQUEST_CODE)
                .setPermissionsListener(this);
    }


    public PolyvCloudClassVideoHelper(PolyvCloudClassVideoItem videoItem, PolyvPPTItem polyvPPTItem) {
        super(videoItem, polyvPPTItem);
    }

    @Override
    public void initConfig() {
        controller.addHelper(this);
        controller.changePPTVideoLocation();

    }

    public void sendDanmuMessage(CharSequence message) {
        if (videoItem != null) {
            videoItem.sendDanmuMessage(message);
        }
    }

    public void sendJoinRequest() {
        joinSuccess = false;
        if (polyvLinkMicAdapter != null) {
            polyvLinkMicAdapter.setAudio("audio".equals(videoView.getLinkMicType()));
        }
        PolyvLinkMicWrapper.getInstance().muteLocalVideo("audio".equals(videoView.getLinkMicType()));
        if (polyvChatManager != null) {
            polyvChatManager.sendJoinRequestMessage(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
        }
    }

    public void leaveChannel() {
        if (polyvChatManager != null) {
            polyvChatManager.sendJoinLeave(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
        }

    }

    @Override
    public void pause() {
        if (joinSuccess) {
            return;
        }
        super.pause();
    }

    @Override
    public void resume() {
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

            PolyvLinkMicWrapper.getInstance().joinChannel("");
            controller.updateLinkMicStatus(true);
            startLinkTimer(false);

        } else if (SE_JOIN_REQUEST.equals(event)) {//保存加入的用户数据

            PolyvJoinRequestSEvent joinRequestSEvent = PolyvGsonUtil.fromJson(PolyvJoinRequestSEvent.class, message);
            if (joinRequestSEvent != null && joinRequestSEvent.getUser() != null) {
                joinRequests.put(joinRequestSEvent.getUser().getUserId(), joinRequestSEvent.getUser());
                //可靠消息
                if (joinRequestSEvent.getUser().getUserId().equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())) {
                    PolyvCommonLog.d(TAG, joinRequestSEvent.getUser().getUserId() + PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid);
                    controller.cancleLinkUpTimer();
                }
            }


        } else if (SE_JOIN_LEAVE.equals(event)) {

            PolyvJoinLeaveSEvent polyvJoinLeaveSEvent = PolyvGsonUtil.fromJson(PolyvJoinLeaveSEvent.class, message);
            if (polyvJoinLeaveSEvent != null && polyvJoinLeaveSEvent.getUser() != null) {
                processLeaveMessage(polyvJoinLeaveSEvent.getUser().getUserId());
            }

            if (polyvJoinLeaveSEvent.getUser().getUserId().equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())) {
                controller.cancleLinkUpTimer();
            }

        } else if (SE_JOIN_SUCCESS.equals(event)) {

        } else if (EVENT_MUTE_USER_MICRO.equals(event)) {//禁麦事件
            PolyvLinkMicMedia micMedia = PolyvGsonUtil.fromJson(PolyvLinkMicMedia.class, message);
            processMediaMessage(micMedia);

        } else if (OPEN_MICROPHONE.equals(event)) {
            PolyvMicphoneStatus micphoneStatus = PolyvGsonUtil.fromJson(PolyvMicphoneStatus.class, message);
            processMicPhone(micphoneStatus);
        } else if (ONSLICEID.equals(event)) {
            sliceIdVo = PolyvGsonUtil.fromJson(PolyvSocketSliceIdVO.class, message);

            if (sliceIdVo == null || sliceIdVo.getData() == null) {
                return;
            }
            this.sessionId = sliceIdVo.getData().getSessionId();

            getLinkMicJoins(false);
            initialCameraStatus();
        } else if (ONSLICECONTROL.equals(event) ) {
            PolyvCommonLog.d(TAG, "receive ONSLICECONTROL message");
            processMicSlice(message);
        }
    }

    private void initialCameraStatus() {
        cameraOpen = sliceIdVo.getData().getIsCamClosed() == 0;
        if (polyvLinkMicAdapter == null) {
            return;
        }
        polyvLinkMicAdapter.updateCamerStatus(cameraOpen);
    }

    //是否需要去更新连麦人列表 第一次进来不用 只获取数据
    private void getLinkMicJoins(boolean needUpdate) {
        cancleGetLinkMicJoinsTask();

        getLinkMicJoins =  PolyvLinkMicWrapper.getInstance().getLinkStatus(
                new PolyvrResponseCallback<PolyvLinkMicJoinStatus>() {

                    @Override
                    public void onSuccess(PolyvLinkMicJoinStatus data) {
                        initJoinDatas(data, sliceIdVo.getData().getRoomId());

                        if(needUpdate){
                            processJoinUnCachesStatus();
                        }
                    }
                },
                sliceIdVo.getData().getRoomId(), sliceIdVo.getData().getSessionId()
        );
    }

    private void processJoinUnCachesStatus() {
        int size = noCachesIds.size();
        Iterator<Long> iterator = noCachesIds.iterator();
        while (iterator.hasNext()){
            Long longUid = iterator.next();
            PolyvJoinInfoEvent joinInfoEvent = joinRequests.get(longUid+"");
            if(joinInfoEvent != null){
                iterator.remove();
                polyvLinkMicAdapter.addData(joinRequests.get(longUid + ""),false);
                PolyvCommonLog.d(TAG, "processJoinUnCachesStatus :" + longUid );
            }
        }

        if(size >0){
            polyvLinkMicAdapter.notifyItemRangeChanged(polyvLinkMicAdapter.getItemCount()-size, size);
        }
        linkMicLayout.scrollToPosition(polyvLinkMicAdapter.getItemCount() - 1);

        cancleLinkTimer();
        changeViewToRtc(true);
    }

    private void cancleGetLinkMicJoinsTask() {
        if(getLinkMicJoins != null){
            getLinkMicJoins.dispose();
            getLinkMicJoins = null;
        }
    }

    private void processMicSlice(String message) {
        PolyvSocketSliceControlVO polyvSocketSliceControl = PolyvGsonUtil.
                fromJson(PolyvSocketSliceControlVO.class, message);
        if (polyvSocketSliceControl != null && polyvSocketSliceControl.getData() != null) {
            cameraOpen = polyvSocketSliceControl.getData().getIsCamClosed() == 0;
            if (polyvLinkMicAdapter == null) {
                return;
            }
            polyvLinkMicAdapter.updateCamerStatus(cameraOpen);
            View surfaceView = polyvLinkMicAdapter.getCameraView();
            if (surfaceView != null) {//关闭打开摄像头
                S_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        surfaceView.setVisibility(polyvSocketSliceControl.getData().getIsCamClosed() == 0 ? VISIBLE : INVISIBLE);
                    }
                });

            }
        }
    }

    private void processMicPhone(PolyvMicphoneStatus micphoneStatus) {
        if (micphoneStatus == null) {
            return;
        }
        videoView.setLinkType(micphoneStatus.getType());
        String type = micphoneStatus.getType();
        if (("Video".equals(type) || "Audio".equals(type))
                && "close".equals(micphoneStatus.getStatus())) {//挂断
//                restartPlay();//用restart比较好
            processLeaveMessage(micphoneStatus.getUserId());
        } else if (("audio".equals(type) || "video".equals(type))
                && "close".equals(micphoneStatus.getStatus())) {//断开连麦
            PolyvLinkMicWrapper.getInstance().leaveChannel();

            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    controller.updateLinkMicStatus(false);
                }
            });

        }
    }

    private void processMediaMessage(PolyvLinkMicMedia micMedia) {
        if (micMedia == null) {
            return;
        }

        if ("video".equals(micMedia.getType())) {
            View owernCamera = polyvLinkMicAdapter.getOwernView().findViewById(R.id.link_mic_camera_layout);
            polyvLinkMicAdapter.getCameraView(owernCamera).setVisibility(micMedia.isMute() ? INVISIBLE : VISIBLE);
            ToastUtils.showLong(micMedia.isMute() ? "摄像头已关闭" : "摄像头已打开");
            PolyvLinkMicWrapper.getInstance().muteLocalVideo(micMedia.isMute());
        } else {
            ToastUtils.showLong(micMedia.isMute() ? "麦克风已关闭" : "麦克风已打开");
            PolyvLinkMicWrapper.getInstance().muteLocalAudio(micMedia.isMute());
        }
    }

    //中途进来需要先添加已经在连麦列表的人
    private void initJoinDatas(PolyvLinkMicJoinStatus data, String roomId) {
        if (data == null) {
            return;
        }

        List<PolyvJoinInfoEvent> joinListBeans = data.getJoinList();
        for (PolyvJoinInfoEvent joinListBean : joinListBeans) {
            if(!joinRequests.containsKey(joinListBean.getUserId()) || JOIN_DEFAULT_TYPE.equals(joinListBean.getUserType())){
                joinRequests.put(joinListBean.getUserId(), joinListBean);
            }
        }
    }

    private void processLeaveMessage(String userId) {

        if (userId.equals(PolyvLinkMicWrapper.getInstance().getLinkMicUid())) {
            PolyvCommonLog.d(TAG, "processLeaveMessage");
            PolyvLinkMicWrapper.getInstance().leaveChannel();
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showLong("关闭连麦");
                    controller.updateLinkMicStatus(false);
                    startLinkTimer(true);
                }
            });

        }
    }

    //20秒内如果定时器被取消 则表明joinchannel rtc的回掉成功 加入成功 否则 置位
    private void startLinkTimer(boolean leave) {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
        }
        linkJoinTimer = PolyvRxTimer.delay(LINK_JOIN_TIME, new Consumer<Long>() {
            @Override
            public void accept(Long l) throws Exception {
                if (leave) {
                    PolyvLinkMicWrapper.getInstance().leaveChannel();
                    controller.updateLinkMicStatus(false);
                } else {
                    PolyvLinkMicWrapper.getInstance().joinChannel("");
                    controller.updateLinkMicStatus(true);
                }
            }
        });
    }

    public void cancleLinkTimer() {
        if (linkJoinTimer != null) {
            linkJoinTimer.dispose();
            linkJoinTimer = null;
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean changePPTViewToVideoView(boolean switchOpen) {
        if (!joinSuccess) {
            return super.changePPTViewToVideoView(switchOpen);
        } else {
            changeLinkMicView(pptShowSub);
            pptShowSub = !pptShowSub;
        }

        return false;
    }

    private void changeLinkMicView(boolean changeToVideoView) {
        try {
            PolyvCommonLog.d(TAG, "changeToVideoView:" + changeToVideoView);
            ViewGroup teacherParent = (ViewGroup) polyvLinkMicAdapter.getTeacherParentView();
            View teacherView = polyvLinkMicAdapter.getTeacherView();

            teacherParent.removeView(changeToVideoView ? pptView : teacherView);
            videoView.removeView(changeToVideoView ? teacherView : pptView);

            videoView.addView(changeToVideoView ? pptView : teacherView, 0, new ViewGroup.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            teacherParent.addView(changeToVideoView ? teacherView : pptView, 1, new ViewGroup.LayoutParams
                    (PolyvScreenUtils.dip2px(context, 144), PolyvScreenUtils.dip2px(context, 108)));

            startAnimation(changeToVideoView?pptView:teacherView);
            camerShowInVideoView = !changeToVideoView;
        } catch (Exception e) {
            PolyvCommonLog.e(TAG, e.getMessage());
        }

    }

    PolyvLinkMicAGEventHandler polyvLinkMicAGEventHandler = new PolyvLinkMicAGEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
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
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    sendJoinSuccess();
                    cancleLinkTimer();
                    hideSubView();
                    pause();
                    changeViewToRtc(true);
                    joinSuccess = true;
                }
            });

        }

        @Override
        public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
            PolyvCommonLog.d(TAG, "onLeaveChannel");
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
//                    if(!joinSuccess){
//                        return;
//                    }
                    PolyvCommonLog.d(TAG, "onLeaveChannel success");
                    joinSuccess = false;
                    cancleLinkTimer();
                    restartPlay();//restartPlay();
                    showSubView();
                    changeViewToRtc(false);
                    joinRequests.remove(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            PolyvCommonLog.d(TAG, "onUserOffline");
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    long longUid = uid & 0xFFFFFFFFL;
                    PolyvJoinInfoEvent joinInfoEvent = joinRequests.remove(uid);
                    if (joinInfoEvent != null) {
                        ToastUtils.showLong(joinInfoEvent.getNick() + "离开连麦室");
                    }
                    polyvLinkMicAdapter.removeData(longUid + "");
                    linkMicLayout.scrollToPosition(polyvLinkMicAdapter.getItemCount() - 1);
                }
            });


        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (sliceIdVo == null) {
                        ToastUtils.showLong("请重新登陆 获取正确状态");
                        leaveChannel();
                        return;
                    }
                    processJoinStatus(uid, elapsed);

                }
            });
        }

        @Override
        public void onUserMuteVideo(int uid, boolean mute) {
            long longUid = uid & 0xFFFFFFFFL;
            PolyvJoinInfoEvent joinInfo = joinRequests.get(longUid + "");
            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String nick = "";
                    if (joinInfo != null) {
                        nick = joinInfo.getNick();
                        int pos = joinInfo.getPos();
                        joinInfo.setMute(mute);
                        if (pos != 0) {
                            polyvLinkMicAdapter.notifyItemChanged(pos);
                        }
                    }
                    ToastUtils.showLong(nick + (mute ? "摄像头已关闭" : "摄像头已打开"));


                }
            });

        }

        @Override
        public void onUserMuteAudio(int uid, boolean mute) {
            long longUid = uid & 0xFFFFFFFFL;
            PolyvJoinInfoEvent joinInfo = joinRequests.get(longUid + "");

            S_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    String nick = "";
                    if (joinInfo != null) {
                        nick = joinInfo.getNick();
                        int pos = joinInfo.getPos();
//                    joinInfo.setMute(mute);
//                    if(pos != 0){//不是老师的更新
//                        polyvLinkMicAdapter.notifyItemChanged(pos);
//                    }
                        PolyvCommonLog.d(TAG, "pos :" + pos);
                    }
                    ToastUtils.showLong(nick + (mute ? "离开音频连麦" : "加入音频连麦"));

                }
            });

        }

    };

    private void processJoinStatus(int uid,int elapsed) {
        long longUid = uid & 0xFFFFFFFFL;
        if(!joinRequests.containsKey(""+longUid)){//不包含 需要更新数据
            PolyvJoinInfoEvent defaultEvent = createDefaultJoin(longUid);
            joinRequests.put(longUid+"",defaultEvent);
            noCachesIds.add(longUid);

            //3秒以后去查询 数据  以免同时多个人加入
            startJoinListTimer();
            return;
        }
        polyvLinkMicAdapter.addData(joinRequests.get(longUid + ""),true);

        linkMicLayout.scrollToPosition(polyvLinkMicAdapter.getItemCount() - 1);

        PolyvCommonLog.d(TAG, "userjoin :" + longUid + " elapseed:" + elapsed);
        cancleLinkTimer();
        changeViewToRtc(true);
    }

    @NonNull
    private PolyvJoinInfoEvent createDefaultJoin(long longUid) {
        PolyvJoinInfoEvent defaultEvent = new PolyvJoinInfoEvent();
        defaultEvent.setUserId(longUid+"");
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
        if(joinListTimer != null){
            joinListTimer.dispose();
            joinListTimer = null;
        }
    }

    private void sendJoinSuccess() {
        polyvChatManager.sendJoinSuccessMessage(sessionId, PolyvLinkMicWrapper.getInstance().getLinkMicUid());
    }

    private void showSubView() {
        if(polyvChatManager != null){
            polyvChatManager.sendJoinLeave(PolyvLinkMicWrapper.getInstance().getLinkMicUid());
        }
        linkMicLayout.setVisibility(View.GONE);
        pptContianer.setVisibility(VISIBLE);
        if (camerShowInVideoView) {
            changeLinkMicView(camerShowInVideoView);
        }
    }

    private void hideSubView() {
        linkMicLayout.setVisibility(VISIBLE);
        pptContianer.setVisibility(INVISIBLE);
        if(controller == null){
            return;
        }
        if (controller.isPPTSubView()) {
            controller.changePPTVideoLocation();
        }

        controller.showCamerView();

    }

    private void changeViewToRtc(boolean change) {
        linkMicLayout.setVisibility(change ? VISIBLE : INVISIBLE);
        if (!change) {
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

    public boolean requestPermission() {
        permissionManager.request();
        return true;
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
        // 可能由于在允许权限的过程中，老师关闭了连麦，故这里需要进行判断
        if (PolyvBaseActivity.checkKick(polyvChatManager.roomId)) {
            Toast.makeText(context, "您当前无法申请发言", Toast.LENGTH_SHORT).show();
            return;
        }
        PolyvCommonLog.d(TAG,"onGranted");
        controller.handsUp(joinSuccess);
        return;
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
    }

    private void clearLinkStatus() {
        if(joinSuccess){
            PolyvLinkMicWrapper.getInstance().leaveChannel();
        }else {
            leaveChannel();
        }

        linkMicLayout.removeAllViews();
        polyvLinkMicAdapter.clear();
        polyvChatManager.removeNewMessageListener(this);
        PolyvLinkMicWrapper.getInstance().removeEventHandler(polyvLinkMicAGEventHandler);
    }

    public void addLinkMicLayout(PolyvLinkMicListView linkMicLayout) {
        this.linkMicLayout = linkMicLayout;

        polyvLinkMicAdapter = new PolyvLinkMicAdapter
                (PolyvLinkMicWrapper.getInstance().getEngineConfig().mUid + "");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        linkMicLayout.setLayoutManager(linearLayoutManager);

        linkMicLayout.setAdapter(polyvLinkMicAdapter);

        polyvLinkMicAdapter.updateCamerStatus(cameraOpen);
    }

    public boolean isJoinLinkMick() {
        return joinSuccess;
    }

    /**
     * 更新控制栏 主副平切换按钮得状态  （切换 或者是 显示副屏）
     * @param polyvSocketMessage
     */
    public void updateMainScreenStatus(String polyvSocketMessage,String event) {
        PolyvSocketSliceControlVO polyvSocketSliceControl = PolyvGsonUtil.
                fromJson(PolyvSocketSliceControlVO.class, polyvSocketMessage);
        if (polyvSocketSliceControl != null && polyvSocketSliceControl.getData() != null) {
            videoView.updateMainScreenStatus(polyvSocketSliceControl.getData().getIsCamClosed() == 0 );
        }
        if(polyvSocketSliceControl.getData().getIsCamClosed() == 1){//关闭摄像头
            if(controller != null){
                controller.showCamerView();
            }
        }

        if(pptView != null){
            pptView.processSocketMessage(new PolyvSocketMessageVO(polyvSocketMessage, event));
        }
    }
}
