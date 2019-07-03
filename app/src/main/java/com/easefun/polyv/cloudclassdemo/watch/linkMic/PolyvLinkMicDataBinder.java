package com.easefun.polyv.cloudclassdemo.watch.linkMic;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvAppUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtc.video.VideoCanvas;

/**
 * @author df
 * @create 2018/11/13
 * @Describe 用于展示云课堂的适配器
 */
public class PolyvLinkMicDataBinder extends IPolyvDataBinder{
    private static final String TAG = "PolyvLinkMicDataBinder";
    private static final int CAMERA_VIEW_ID = 817;

    private List<String> uids = new ArrayList<>();
    private Map<String, PolyvJoinInfoEvent> joins = new LinkedHashMap<>();
    private String myUid, teacherId;
    private PolyvJoinInfoEvent teacher;
    private View teacherView;//老师的布局view
    private View teacherParentView;//   teacherParentView:老师外层布局view
    private View cameraView, surfaceView;//surfaceview 是其他连麦者的摄像头，cameraView 是老师的摄像头

    private boolean isAudio, cameraOpen = true;

    private List<SurfaceView> cachesView = new ArrayList<>();
    private ViewGroup parentView;

    public PolyvLinkMicDataBinder(String myUid) {
        this.myUid = myUid;
    }

    public void addOwner(String myUid, PolyvJoinInfoEvent owern) {
        //将自己先放在第一位 老师来的顺序可能在后面 老师再放置再第一位 保证前两位一直室老师跟自己
        if (!uids.contains(myUid)) {
            uids.add(0, myUid);
        }
        if (owern == null) {
            PolyvCommonLog.e(TAG, "owern is null");
            owern = new PolyvJoinInfoEvent();
        }
        joins.put(myUid, owern);

        onBindViewHolder(onCreateViewHolder(parentView, 0), 0);
    }

    public void addParent(LinearLayout linkMicLayout) {
        parentView = linkMicLayout;
    }

    @NonNull
    public PolyvLinkMicDataBinder.PolyvMicHodler onCreateViewHolder(@NonNull ViewGroup parent, int pos) {
        PolyvCommonLog.d(TAG, "onCreateViewHolder:");
        ViewGroup child = (ViewGroup) View.inflate(parent.getContext(), R.layout.link_mic_scroll_item, null);
        PolyvLinkMicDataBinder.PolyvMicHodler polyvMicHodler = new PolyvLinkMicDataBinder.PolyvMicHodler(child);
        SurfaceView surfaceView = PolyvLinkMicWrapper.getInstance().createRendererView(PolyvAppUtils.getApp());
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView.setId(CAMERA_VIEW_ID);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        if(surfaceView != null){
            polyvMicHodler.camerLayout.addView(surfaceView, 1);
        }

        cachesView.add(surfaceView);

        if(child != null){
            parentView.addView(child, pos);
        }
        return polyvMicHodler;
    }

    public void onBindViewHolder(PolyvLinkMicDataBinder.PolyvMicHodler holder, int position) {

        String uid = uids.get(position);

        if (TextUtils.isEmpty(uid)) {
            PolyvCommonLog.e(TAG, "uid is null:" + uids.toString());
            return;
        }
        holder.itemView.setTag(uid);
        PolyvJoinInfoEvent polyvJoinRequestSEvent = joins.get(uid);
        holder.camer.setVisibility(uid.equals(myUid) ? View.VISIBLE : View.INVISIBLE);
        if (uid.equals(myUid)) {
            holder.polyvLinkNick.setText("我");
            ownerView = holder.itemView;
            ownerCamera = holder.cameraSwitch;
            if (!isAudio) {
                ownerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ownerCamera.setVisibility(View.VISIBLE);
                        startShowTimer();
                        return false;
                    }
                });
                startShowTimer();
            }

        } else if (polyvJoinRequestSEvent != null) {
            holder.polyvLinkNick.setText(polyvJoinRequestSEvent.getNick());
        }
        SurfaceView surfaceView = (SurfaceView) holder.camerLayout.findViewById(CAMERA_VIEW_ID);
        PolyvCommonLog.d(TAG, "onBindViewHolder:uid :" + uid + "  pos :" + position);
        if (polyvJoinRequestSEvent != null) {
            surfaceView.setVisibility(polyvJoinRequestSEvent.isMute() ? View.INVISIBLE : View.VISIBLE);
        }
        if (isAudio && !uid.equals(teacherId)) {//音频 只显示教师
            surfaceView.setVisibility(View.GONE);
            holder.cameraSwitch.setVisibility(View.GONE);
            return;
        }

        if (teacher != null && teacher.getUserId().equals(uid)) {
            teacherParentView = holder.itemView;
            teacherView = holder.camerLayout;
            PolyvCommonLog.d(TAG, "cameraOpen:" + cameraOpen);
            surfaceView.setVisibility(cameraOpen ? View.VISIBLE : View.INVISIBLE);
        }
        long longUid = Long.valueOf(uid);
        if (uid.equals(myUid)) {
             PolyvLinkMicWrapper.getInstance().setupLocalVideo(surfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN, (int) longUid);
        } else {
            PolyvLinkMicWrapper.getInstance().setupRemoteVideo(surfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN, (int) longUid);
        }

    }

    private void addTeacher(String teacherId, PolyvJoinInfoEvent teacherEvent) {
        this.teacher = teacherEvent;
        this.teacherId = teacherId;
        teacher.setUserId(teacherId);


        if (joins.containsKey(teacherId)) {
            return;
        }
        joins.put(teacherId, teacher);
    }

    public void setAudio(boolean audio) {
        isAudio = audio;
    }

    public void updateCamerStatus(boolean cameraOpen) {
        this.cameraOpen = cameraOpen;
    }

    public View getOwnerView() {
        return ownerView;
    }

    public PolyvJoinInfoEvent getJoinInfo(String uid) {
        return joins.get(uid);
    }

    public int getItemCount() {
        return uids.size();
    }

    public void notifyItemChanged(int pos, boolean mute) {
        View child = parentView.getChildAt(pos);
        SurfaceView surfaceView = (SurfaceView) child.findViewById(CAMERA_VIEW_ID);
        surfaceView.setVisibility(mute ? View.INVISIBLE : View.VISIBLE);
    }

    public void switchView(String originUid){
        PolyvJoinInfoEvent joinInfoEvent = joins.get(originUid);
        PolyvJoinInfoEvent firstInfo = joins.get(uids.get(0));
        if(joinInfoEvent == null){
            PolyvCommonLog.e(TAG,"no such uid");
            return ;
        }
        //教师位置默认在第一位，当重新进来的时候，教师从小屏切换到主屏 就不需要调整
        if(joinInfoEvent.getPos() == 0){
            return;
        }

        View switchView = parentView.getChildAt(joinInfoEvent.getPos());
        View firstView = parentView.getChildAt(0);

        parentView.removeView(firstView);
        parentView.removeView(switchView);

        parentView.addView(switchView,0);
        parentView.addView(firstView,joinInfoEvent.getPos());

        String isSwitchUid = uids.get(0);
        uids.set(0,uids.get(joinInfoEvent.getPos()));
        uids.set(joinInfoEvent.getPos(),isSwitchUid);

        firstInfo.setPos(joinInfoEvent.getPos());
        joinInfoEvent.setPos(0);
    }

    public ViewGroup switchViewToMianScreen(String userId) {
        PolyvJoinInfoEvent joinInfoEvent = joins.get(userId);
        if(joinInfoEvent == null){
            return null;
        }
        PolyvCommonLog.e(TAG,"switchViewToMianScreen pos :"+joinInfoEvent.getPos());
        return (ViewGroup) parentView.getChildAt(joinInfoEvent.getPos());
    }

    public int getJoinsPos(String uid){
        PolyvJoinInfoEvent joinInfoEvent = joins.get(uid);
        if(joinInfoEvent == null){
            return -1;
        }
        return joinInfoEvent.getPos();
    }

    public void updateSwitchViewStatus(String subLinkMicViewUid, String mainLinkMicViewUid) {
        PolyvCommonLog.e(TAG,"subLinkMicViewUid:"+subLinkMicViewUid+"  mainLinkMicViewUid:"+mainLinkMicViewUid);
        PolyvJoinInfoEvent joinInfoEvent = joins.get(subLinkMicViewUid);
        PolyvJoinInfoEvent firstInfo = joins.get(mainLinkMicViewUid);

        String isSwitchUid = mainLinkMicViewUid;
        uids.set(0,subLinkMicViewUid);
        uids.set(joinInfoEvent.getPos(),isSwitchUid);

        firstInfo.setPos(joinInfoEvent.getPos());
        joinInfoEvent.setPos(0);
    }

    public class PolyvMicHodler extends RecyclerView.ViewHolder {

        public View camer;
        public ImageView cameraSwitch;
        public TextView polyvLinkNick;
        public FrameLayout camerLayout;
        public int pos;

        public PolyvMicHodler(View itemView) {
            super(itemView);
            camer = itemView.findViewById(R.id.polyv_link_camera_switch_container);
            cameraSwitch = (ImageView) itemView.findViewById(R.id.polyv_camera_switch);
            polyvLinkNick = (TextView) itemView.findViewById(R.id.polyv_link_nick);
            camerLayout = (FrameLayout) itemView.findViewById(R.id.polyv_link_mic_camera_layout);
            cameraSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PolyvLinkMicWrapper.getInstance().switchCamera();
                }
            });
        }
    }

    public ViewGroup getFirstLinkMicView() {
        return (ViewGroup) parentView.getChildAt(0);
    }

    public View getTeacherParentView() {
        return teacherParentView;
    }

    //根据父类获取相应的摄像头view
    public View getCameraView(View parent) {
        if (parent == null) {
            return surfaceView;
        }
        if (parent != null) {
            surfaceView = parent.findViewById(CAMERA_VIEW_ID);
        }
        return surfaceView;
    }

    //获取讲师的摄像头view
    public View getCameraView() {
        if (cameraView != null) {
            return cameraView;
        }
        if (teacherView != null) {
            cameraView = teacherView.findViewById(CAMERA_VIEW_ID);
        }
        return cameraView;
    }


    public synchronized void addData(PolyvJoinInfoEvent requestSEvent, boolean updateImmidately) {
        if (requestSEvent == null || joins.containsKey(requestSEvent.getUserId()) || TextUtils.isEmpty(requestSEvent.getUserId())) {
            PolyvCommonLog.d(TAG, "contains userid  || userid is  :");
            return;
        }
        try {

            if (!uids.contains(requestSEvent.getUserId())) {
                //老师放在第一位ol
                if ("teacher".equals(requestSEvent.getUserType())) {
                    teacherId = requestSEvent.getUserId();
                    addTeacher(teacherId, requestSEvent);
                    uids.add(0, requestSEvent.getUserId());
                } else {
                    uids.add(requestSEvent.getUserId());
                }
            }


            joins.put(requestSEvent.getUserId(), requestSEvent);

            if (updateImmidately) {
                PolyvCommonLog.e(TAG, "update updateImmidately:" + requestSEvent.getUserType());
                if ("teacher".equals(requestSEvent.getUserType())) {
                    requestSEvent.setPos(0);
//                    notifyItemInserted(0);//通知数据与界面重新绑定
                    onBindViewHolder(onCreateViewHolder(parentView, 0), 0);
                } else {
                    requestSEvent.setPos(uids.size() - 1);
                    onBindViewHolder(onCreateViewHolder(parentView, uids.size() - 1), uids.size() - 1);
//                    notifyItemInserted(uids.size() - 1);//通知数据与界面重新绑定
                }
            }

            PolyvCommonLog.e(TAG, "update :" + requestSEvent.getUserType());
            arrangeDataPos();
        } catch (Exception e) {
            PolyvCommonLog.e(TAG, e.getMessage());
        }


    }

    // 对连麦者位置序号重新排序
    private void arrangeDataPos() {

        int length = uids.size();
        for (int i = 0; i < length; i++) {
            PolyvJoinInfoEvent infoEvent = joins.get(uids.get(i));
            infoEvent.setPos(i);
        }
    }

    public void removeData(String uid,boolean removeView) {
        if (TextUtils.isEmpty(uid)) {
            return;
        }
        uids.remove(uid);
        PolyvJoinInfoEvent infoEvent = joins.remove(uid);
        int pos = uids.size();
        if (infoEvent != null) {
            pos = infoEvent.getPos();
        }
        PolyvCommonLog.d(TAG, "remove pos :" + pos);
        if(removeView){
            notifyItemRemoved(pos);
        }

        arrangeDataPos();
    }

    private void notifyItemRemoved(int pos) {
        if (parentView != null) {

            if(parentView.getChildAt(pos) != null){
                parentView.removeViewAt(pos);
            }else {
//                ToastUtils.showLong("notifyItemRemoved:is null"+pos);
            }
        }
    }

    public void clear() {
        clearSurfaceview();
        uids.clear();
        joins.clear();
        teacherView = null;
        cameraView = null;

        ownerView = null;
    }

    private void clearSurfaceview() {
        for (SurfaceView surfaceView : cachesView) {
            if (surfaceView.getHolder() != null && surfaceView.getHolder().getSurface() != null) {
                surfaceView.getHolder().getSurface().release();
            }
        }
        cachesView.clear();
    }

}
