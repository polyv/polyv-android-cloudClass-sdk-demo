package com.easefun.polyv.cloudclassdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtc.video.VideoCanvas;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author df
 * @create 2018/10/17
 * @Describe
 */
public class PolyvLinkMicAdapter extends RecyclerView.Adapter<PolyvLinkMicAdapter.PolyvMicHodler> {

    private static final String TAG = "PolyvLinkMicAdapter";
    private static final int SHOW_TIME = 5 * 1000;

    private List<String> uids = new ArrayList<>();
    private Map<String, PolyvJoinInfoEvent> joins = new LinkedHashMap<>();
    private String myUid,teacherId;
    private PolyvJoinInfoEvent teacher;
    private View teacherView;//老师的布局view
    private View teacherParentView;//   teacherParentView:老师外层布局view
    private View cameraView, surfaceView;//surfaceview 是其他连麦者的摄像头，cameraView 是老师的摄像头

    private View owernView, owernCamera;//自己的布局view

    private Disposable owernShowTimer;//头像显示切换定时器

    private boolean isAudio, cameraOpen = true;

    private List<SurfaceView> cachesView = new ArrayList<>();

    public PolyvLinkMicAdapter(String myUid) {
        this.myUid = myUid;
        //将自己先放在第一位 老师来的顺序可能在后面 老师再放置再第一位 保证前两位一直室老师跟自己
        if (!uids.contains(myUid)) {
            uids.add(0, myUid);
        }
        if (!joins.containsKey(myUid)) {
            joins.put(myUid, new PolyvJoinInfoEvent());
        }

    }

    @NonNull
    @Override
    public PolyvMicHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PolyvCommonLog.d(TAG, "onCreateViewHolder:");
        Context context = parent.getContext();
        ViewGroup child = (ViewGroup) View.inflate(parent.getContext(), R.layout.link_mic_scroll_layout, null);
        PolyvMicHodler polyvMicHodler = new PolyvMicHodler(child);
        SurfaceView surfaceView = PolyvLinkMicWrapper.getInstance().createRendererView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        polyvMicHodler.camerLayout.addView(surfaceView, 1, layoutParams);

        cachesView.add(surfaceView);
        return polyvMicHodler;
    }

    @Override
    public void onBindViewHolder(PolyvMicHodler holder, int position) {

        String uid = uids.get(position);

        if (TextUtils.isEmpty(uid)) {
            PolyvCommonLog.e(TAG, "uid is null:" + uids.toString());
            return;
        }
        PolyvJoinInfoEvent polyvJoinRequestSEvent = joins.get(uid);
        holder.camer.setVisibility(uid.equals(myUid) ? View.VISIBLE : View.INVISIBLE);
        if (uid.equals(myUid)) {
            holder.polyvLinkNick.setText("我");
            owernView = holder.itemView;
            owernCamera = holder.camerSwitch;
            if(!isAudio){
                owernView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        owernCamera.setVisibility(View.VISIBLE);
                        startShowTimer();
                    }
                });
                startShowTimer();
            }

        }else if (polyvJoinRequestSEvent != null ) {
            holder.polyvLinkNick.setText(polyvJoinRequestSEvent.getNick());
        }
        SurfaceView surfaceView = (SurfaceView) holder.camerLayout.getChildAt(1);
        PolyvCommonLog.d(TAG, "onBindViewHolder:uid :" + uid+"  pos :"+position);
        if (polyvJoinRequestSEvent != null) {
            surfaceView.setVisibility(polyvJoinRequestSEvent.isMute() ? View.INVISIBLE : View.VISIBLE);
        }
        if (isAudio && !uid.equals(teacherId)) {//音频 只显示教师
            surfaceView.setVisibility(View.GONE);
            holder.camerSwitch.setVisibility(View.GONE);
            return;
        }

        if (teacher != null && teacher.getUserId().equals(uid)) {
            teacherParentView = holder.itemView;
            teacherView = holder.camerLayout;
            PolyvCommonLog.d(TAG, "cameraOpen:" + cameraOpen);
            surfaceView.setVisibility(cameraOpen ? View.VISIBLE : View.INVISIBLE);
        }
        long longUid = Long.valueOf(uid);
        if (uid == myUid) {
            PolyvLinkMicWrapper.getInstance().setupLocalVideo(surfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN, (int) longUid);
        } else {
            PolyvLinkMicWrapper.getInstance().setupRemoteVideo(surfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN, (int) longUid);
        }

    }

    private void startShowTimer() {
        if (owernShowTimer != null) {
            owernShowTimer.dispose();
            owernShowTimer = null;
        }
        owernShowTimer = PolyvRxTimer.delay(SHOW_TIME, new Consumer<Long>() {
            @Override
            public void accept(Long l) throws Exception {
                if (owernCamera != null) {
                    owernCamera.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public synchronized void addData(PolyvJoinInfoEvent requestSEvent,boolean updateImmidately) {
        if (requestSEvent == null || joins.containsKey(requestSEvent.getUserId()) || TextUtils.isEmpty(requestSEvent.getUserId())) {
            PolyvCommonLog.d(TAG, "contains userid  || userid is  :");
            return;
        }
        try {

            if (!uids.contains(myUid)) {
                uids.add(0, myUid);
            }
            if (!joins.containsKey(myUid)) {
                joins.put(myUid, new PolyvJoinInfoEvent());
            }

            if (!uids.contains(requestSEvent.getUserId())) {
                //老师放在第一位
                if("teacher".equals(requestSEvent.getUserType())){
                    teacherId =  requestSEvent.getUserId();
                    addTeacher(teacherId,requestSEvent);
                    uids.add(0, requestSEvent.getUserId());
                }
                else {
                    uids.add(requestSEvent.getUserId());
                }
            }


            joins.put(requestSEvent.getUserId(), requestSEvent);

            if(updateImmidately){
                PolyvCommonLog.e(TAG,"update updateImmidately:"+requestSEvent.getUserType());
                if ("teacher".equals(requestSEvent.getUserType())) {
                    requestSEvent.setPos(0);
//                    notifyItemInserted(0);//通知数据与界面重新绑定
                    notifyItemRangeChanged(0,uids.size() - 1);
                } else {
                    requestSEvent.setPos(uids.size() - 1);
                    notifyItemInserted(uids.size() - 1);//通知数据与界面重新绑定
                }
            }

            PolyvCommonLog.e(TAG,"update :"+requestSEvent.getUserType());
            arrangeDataPos();

            Thread.sleep(200);
        } catch (Exception e) {
            PolyvCommonLog.e(TAG,e.getMessage());
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

    public void removeData(String uid) {
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
        notifyItemRemoved(pos);
    }

    public void clear() {
        clearSurfaceview();
        uids.clear();
        joins.clear();
        teacherView = null;
        cameraView = null;

        owernView = null;
    }

    private void clearSurfaceview() {
        for (SurfaceView surfaceView : cachesView) {
            if (surfaceView.getHolder() != null && surfaceView.getHolder().getSurface() != null) {
                surfaceView.getHolder().getSurface().release();
                surfaceView = null;
            }
        }
        cachesView.clear();
    }

    @Override
    public int getItemCount() {
        return uids.size();
    }

    private  void addTeacher(String teacherId,PolyvJoinInfoEvent teacherEvent) {
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

    public View getOwernView() {
        return owernView;
    }

    public class PolyvMicHodler extends RecyclerView.ViewHolder {

        public View camer;
        public ImageView camerSwitch;
        public TextView polyvLinkNick;
        public FrameLayout camerLayout;

        public PolyvMicHodler(View itemView) {
            super(itemView);
            camer = itemView.findViewById(R.id.polyv_link_camer_shape);
            camerSwitch = itemView.findViewById(R.id.polyv_camera_switch);
            polyvLinkNick = itemView.findViewById(R.id.polyv_link_nick);
            camerLayout = itemView.findViewById(R.id.link_mic_camera_layout);
            camerSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PolyvLinkMicWrapper.getInstance().switchCamera();
                }
            });
        }
    }

    public View getTeacherView() {
        return teacherView;
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
            surfaceView = ((ViewGroup) parent).getChildAt(1);
        }
        return surfaceView;
    }

    //获取讲师的摄像头view
    public View getCameraView() {
        if (cameraView != null) {
            return cameraView;
        }
        if (teacherView != null) {
            cameraView = ((ViewGroup) teacherView).getChildAt(1);
        }
        return cameraView;
    }
}
