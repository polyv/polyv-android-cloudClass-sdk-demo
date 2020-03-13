package com.easefun.polyv.cloudclassdemo.watch.linkMic;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.event.PolyvSendCupEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvAppUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;

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
    public static final int CAMERA_VIEW_ID = 817;

    private int rowCount= 3;//每行个数 默认三个 可设置
    private int linkMicRegionWidth =  ScreenUtils.getScreenWidth();

    private List<String> uids = new ArrayList<>();
    private Map<String, PolyvJoinInfoEvent> joins = new LinkedHashMap<>();
    private String myUid, teacherId;
    private PolyvJoinInfoEvent teacher;
    private View teacherView,teacherLogoView;//老师的布局view
    private View teacherParentView;//   teacherParentView:老师外层布局view
    private View cameraView, surfaceView;//surfaceview 是其他连麦者的摄像头，cameraView 是老师的摄像头

    private boolean cameraOpen = true,isNormalLive;

    private List<SurfaceView> cachesView = new ArrayList<>();

    //item点击事件的回掉
    private View.OnClickListener itemClicker;

    public PolyvLinkMicDataBinder(String myUid, boolean isNormalLive) {
        this.myUid = myUid;
        this.isNormalLive = isNormalLive;
    }

    public void addOwner(String myUid, PolyvJoinInfoEvent owern) {
        //如果不是参与者 不能显示mic
        if(owern != null &&"viewer".equals(owern.getUserType())){
            if( owern.getClassStatus() == null || !owern.getClassStatus().isVoice()){
                PolyvCommonLog.d(TAG,"add data is not voice"+owern.toString());
                return ;
            }
        }

        //将自己先放在第一位 老师来的顺序可能在后面 老师再放置再第一位 保证前两位一直室老师跟自己
        if (!uids.contains(myUid)) {
            uids.add(0, myUid);
        }
        if (owern == null) {
            PolyvCommonLog.e(TAG, "owern is null :"+myUid);
            owern = new PolyvJoinInfoEvent();
        }
        joins.put(myUid, owern);

        onBindViewHolder(onCreateViewHolder(parentView, uids.size()-1), 0);
    }

    @Override
    public void bindLinkMicFrontView(ViewGroup linkMicLayoutParent) {
        if(!isNormalLive){
            return;
        }
        ViewGroup frontView = ((ViewGroup) linkMicLayoutParent.getParent()).findViewById(R.id.link_mic_fixed_position);
        if(frontView != null){
            frontView.setVisibility(View.GONE);//isAudio?View.VISIBLE
        }
    }

    @NonNull
    public PolyvLinkMicDataBinder.PolyvMicHodler onCreateViewHolder(@NonNull ViewGroup parent, int pos) {
        PolyvCommonLog.d(TAG, "onCreateViewHolder:");
        ViewGroup child = (ViewGroup) View.inflate(parent.getContext(), R.layout.link_mic_scroll_item, null);
        PolyvLinkMicDataBinder.PolyvMicHodler polyvMicHodler = new PolyvLinkMicDataBinder.PolyvMicHodler(child);
        SurfaceView surfaceView = createSurfaceView();
        if(surfaceView != null){
            polyvMicHodler.camerLayout.addView(surfaceView, 1);
            cachesView.add(surfaceView);
        }

        if(child != null){
            FrameLayout.LayoutParams childParams = createLayoutParams(pos);
            parentView.addView(child,pos,childParams);
        }
        return polyvMicHodler;
    }

    protected SurfaceView createSurfaceView() {
        SurfaceView surfaceView = PolyvLinkMicWrapper.getInstance().createRendererView(PolyvAppUtils.getApp());
        if (surfaceView == null) {
            return null;
        }
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceView.setId(CAMERA_VIEW_ID);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        return surfaceView;
    }

    //构建每个item得layoutparams
    protected FrameLayout.LayoutParams createLayoutParams(int pos) {
        int itemCount = rowCount;//一行三个
        int leftMargin = 0, topMargin = 0;
        int itemWidth = linkMicRegionWidth / itemCount;
        int itemHeight = (int) (itemWidth * PolyvScreenUtils.getRatio());
        FrameLayout.LayoutParams childParams =
                new FrameLayout.LayoutParams(itemWidth, itemHeight);
        topMargin = pos / (itemCount ) * itemHeight;
        if (pos % itemCount == 0) {//需要换行
            leftMargin = 0;
        } else {
            leftMargin = pos % (itemCount)*itemWidth;
        }
        childParams.leftMargin = leftMargin;
        childParams.topMargin = topMargin;

        PolyvScreenUtils.setItemHeight(itemHeight);
        PolyvScreenUtils.setItemWidth(itemWidth);
        return childParams;
    }

    @Override
    public void updateLayoutStyle(int orientation) {
        ViewGroup linkMicParent = (ViewGroup) parentView.getParent();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) linkMicParent.getLayoutParams();

        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            rowCount = 1;
            linkMicRegionWidth = (int) parentView.getResources().getDimension(R.dimen.ppt_width);

            layoutParams.addRule(RelativeLayout.ABOVE,0);
        }else {
            layoutParams.addRule(RelativeLayout.ABOVE,R.id.link_mic_bottom);
            rowCount = 3;
            linkMicRegionWidth = ScreenUtils.getScreenWidth();
        }
        resetChildViewPos(false);
    }

    @Override
    public void bindItemClickListener(View.OnClickListener clickListener) {
        this.itemClicker = clickListener;
    }

    public void onBindViewHolder(PolyvLinkMicDataBinder.PolyvMicHodler holder, int position) {

        String uid = uids.get(position);

        if (TextUtils.isEmpty(uid)) {
            PolyvCommonLog.e(TAG, "uid is null:" + uids.toString());
            return;
        }
        holder.itemView.setTag(uid);
        holder.camerLayout.setTag(uid);
        PolyvJoinInfoEvent polyvJoinRequestSEvent = joins.get(uid);

        if (polyvJoinRequestSEvent != null) {
            holder.setLoginId(polyvJoinRequestSEvent.getLoginId());
        }

//        holder.camer.setVisibility(uid.equals(myUid) ? View.VISIBLE : View.INVISIBLE);
        if (uid.equals(myUid)) {
            holder.polyvLinkNick.setText("我");
            ownerView = holder.itemView;
            ownerMic = holder.cameraLinkMicOff;
            ownerLinkView = holder.camerLayout;
//            if (!isAudio) {
//                ownerView.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        ownerMic.setVisibility(View.VISIBLE);
//                        startShowTimer();
//                        return false;
//                    }
//                });
//                startShowTimer();
//            }

        } else if (polyvJoinRequestSEvent != null) {
            holder.polyvLinkNick.setVisibility(TextUtils.isEmpty(polyvJoinRequestSEvent.getNick())?View.GONE:View.VISIBLE);
            holder.polyvLinkNick.setText(polyvJoinRequestSEvent.getNick());
        }
        SurfaceView surfaceView = holder.camerLayout.findViewById(CAMERA_VIEW_ID);
        PolyvCommonLog.d(TAG, "onBindViewHolder:uid :" + uid + "  pos :" + position);
        if (polyvJoinRequestSEvent != null) {
            surfaceView.setVisibility(polyvJoinRequestSEvent.isMute() ? View.INVISIBLE : View.VISIBLE);
        }

        if (polyvJoinRequestSEvent != null && polyvJoinRequestSEvent.getCupNum() != 0) {
            holder.cupLayout.setVisibility(View.VISIBLE);
            holder.cupNumView.setText(polyvJoinRequestSEvent.getCupNum() > 99 ? "99+" : (polyvJoinRequestSEvent.getCupNum() + ""));
        } else {
            holder.cupLayout.setVisibility(View.GONE);
        }

        if (isAudio && !uid.equals(teacherId)) {//音频 只显示教师
            surfaceView.setVisibility(View.GONE);
            holder.cameraLinkMicOff.setVisibility(View.GONE);
            return;
        }

        if (teacher != null && teacher.getUserId().equals(uid)) {
            teacherParentView = holder.itemView;
            teacherView = holder.camerLayout;
            teacherLogoView = holder.teacherLogo;
            surfaceView.setVisibility(cameraOpen ? View.VISIBLE : View.INVISIBLE);
            holder.teacherLogo.setVisibility(View.VISIBLE);
        }else {
            holder.teacherLogo.setVisibility(View.GONE);
        }
        long longUid = Long.valueOf(uid);
        if (uid.equals(myUid)) {
             PolyvLinkMicWrapper.getInstance().setupLocalVideo(surfaceView,
                    VideoCanvas.RENDER_MODE_FIT, (int) longUid);
        } else {
            PolyvLinkMicWrapper.getInstance().setupRemoteVideo(surfaceView,
                    VideoCanvas.RENDER_MODE_FIT, (int) longUid);
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

    public void updateCameraStatus(boolean cameraOpen) {
        this.cameraOpen = cameraOpen;
    }

    public View getOwnerView() {
        return ownerView;
    }

    @Override
    public void showMicOffLineView(boolean mute) {
        super.showMicOffLineView(mute);
        if (ownerMic != null)
        ownerMic.setVisibility(mute?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public void showCameraOffLineView(boolean mute) {
        super.showCameraOffLineView(mute);
        SurfaceView surfaceView = ownerLinkView.findViewById(CAMERA_VIEW_ID);
        if(surfaceView != null){
            surfaceView.setVisibility(mute?View.INVISIBLE:View.VISIBLE);
        }
    }

    public boolean showMicOffLineView(boolean mute, int  pos) {
         View child = parentView.getChildAt(pos);
         if(child == null){
             return false;
         }
         View muteView = child.findViewById(R.id.polyv_camera_switch);
         if(muteView == null){
             return false;
         }
         muteView.setVisibility(mute?View.VISIBLE:View.INVISIBLE);

         return true;
    }

    public PolyvJoinInfoEvent getJoinInfo(String uid) {
        return joins.get(uid);
    }

    public int getItemCount() {
        return uids.size();
    }

    public boolean notifyItemChanged(int pos, boolean mute) {
        View child = parentView.getChildAt(pos);
        SurfaceView surfaceView = child.findViewById(CAMERA_VIEW_ID);
        if(surfaceView ==null){
            return false;
        }
        surfaceView.setVisibility(mute ? View.INVISIBLE : View.VISIBLE);

        return true;
    }

    @Override
    public void updateTeacherLogoView(View updateView) {
        super.updateTeacherLogoView(updateView);
        teacherLogoView = updateView;
    }

    @Override
    public boolean changeTeacherLogo(String toTeacherId,boolean hasPermission) {
        super.changeTeacherLogo(toTeacherId,hasPermission);
        List<PolyvJoinInfoEvent> joinInfoEvents = new ArrayList<>(joins.values());
        PolyvJoinInfoEvent joinInfoEvent = null;
        for (PolyvJoinInfoEvent info :joinInfoEvents) {
            if(hasPermission){
                String userId = info.getUserId();
                if(TextUtils.isEmpty(userId)){
                    userId = info.getLoginId();
                }
                if(toTeacherId.equals(userId)){
                    joinInfoEvent = info;
                    break;
                }
            }else {
                joinInfoEvent = teacher;
                break;
            }
        }

        if(joinInfoEvent == null){
            return false;
        }

        int pos = joinInfoEvent.getPos();
        if (this.teacherLogoView != null) {
            this.teacherLogoView.setVisibility(View.GONE);
        }
        View toTeacherView = parentView.getChildAt(pos);
        View teacherLogoView = toTeacherView.findViewById(R.id.teacher_logo);
        if(teacherLogoView == null){
            return false;
        }
        teacherLogoView.setVisibility(View.VISIBLE);
        this.teacherLogoView = teacherLogoView;

        return true;
    }

    public void switchView(String originUid){
        PolyvJoinInfoEvent joinInfoEvent = joins.get(originUid);
        if (uids.isEmpty()){
            return;
        }
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

        ViewGroup.LayoutParams switchParams = switchView.getLayoutParams();
        ViewGroup.LayoutParams firstViewParams  = firstView.getLayoutParams();

        parentView.addView(switchView,0,firstViewParams);
        parentView.addView(firstView,joinInfoEvent.getPos(),switchParams);

        String isSwitchUid = uids.get(0);
        uids.set(0,uids.get(joinInfoEvent.getPos()));
        uids.set(joinInfoEvent.getPos(),isSwitchUid);

        firstInfo.setPos(joinInfoEvent.getPos());
        joinInfoEvent.setPos(0);
    }

    public ViewGroup getSwitchView(String userId) {
        PolyvJoinInfoEvent joinInfoEvent = joins.get(userId);
        if(joinInfoEvent == null){
            return null;
        }
        PolyvCommonLog.e(TAG,"getSwitchView pos :"+joinInfoEvent.getPos());
        ViewGroup switchParent = (ViewGroup) parentView.getChildAt(joinInfoEvent.getPos());
        ViewGroup switchItem = switchParent.findViewById(R.id.polyv_link_mic_camera_layout);
        return switchItem;
    }

    public int getJoinsPos(String uid){
        PolyvJoinInfoEvent joinInfoEvent = joins.get(uid);
        if(joinInfoEvent == null){
            return -1;
        }
        return joinInfoEvent.getPos();
    }

    public void updateSwitchViewStatus(String subLinkMicViewUid, String mainLinkMicViewUid) {
        if(TextUtils.isEmpty(subLinkMicViewUid) || TextUtils.isEmpty(mainLinkMicViewUid)){
            return;
        }

        PolyvCommonLog.e(TAG,"subLinkMicViewUid:"+subLinkMicViewUid+"  mainLinkMicViewUid:"+mainLinkMicViewUid);
        PolyvJoinInfoEvent joinInfoEvent = joins.get(subLinkMicViewUid);
        PolyvJoinInfoEvent firstInfo = joins.get(mainLinkMicViewUid);
        PolyvCommonLog.d(TAG,"before switch view :first "+firstInfo.getPos()+"  switch : "+joinInfoEvent.getPos());
        String isSwitchUid = mainLinkMicViewUid;
        uids.set(firstInfo.getPos(),subLinkMicViewUid);
        uids.set(joinInfoEvent.getPos(),isSwitchUid);

        int firstPos = firstInfo.getPos();
        firstInfo.setPos(joinInfoEvent.getPos());
        joinInfoEvent.setPos(firstPos);

        PolyvCommonLog.d(TAG,"switch view :first "+firstInfo.getPos()+"  switch : "+joinInfoEvent.getPos());
    }

    public class PolyvMicHodler extends RecyclerView.ViewHolder {

        public View camer;
        public ImageView cameraLinkMicOff;
        public ImageView teacherLogo;
        public TextView polyvLinkNick;
        public FrameLayout camerLayout;
        public LinearLayout cupLayout;
        public TextView cupNumView;
        public int pos;

        public String loginId;

        public PolyvMicHodler(View itemView) {
            super(itemView);
            camer = itemView.findViewById(R.id.polyv_link_camera_switch_container);
            cameraLinkMicOff = itemView.findViewById(R.id.polyv_camera_switch);
            teacherLogo = itemView.findViewById(R.id.teacher_logo);
            polyvLinkNick = itemView.findViewById(R.id.polyv_link_nick);
            camerLayout = itemView.findViewById(R.id.polyv_link_mic_camera_layout);
            cupLayout = itemView.findViewById(R.id.cup_layout);
            cupNumView = itemView.findViewById(R.id.cup_num_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedLinkMicView = camerLayout;
                    if(itemClicker != null ){
                        itemClicker.onClick(v);
                    }
                }
            });

            registerSocketEventListener();
        }

        public void setLoginId(String loginId) {
            this.loginId = loginId;
        }

        private void registerSocketEventListener() {
            PolyvChatManager.getInstance().addNewMessageListener(new PolyvNewMessageListener() {
                @Override
                public void onNewMessage(String message, String event) {
                    if (PolyvChatManager.EVENT_SEND_CUP.equals(event)) {
                        PolyvSendCupEvent sendCupEvent = PolyvEventHelper.getEventObject(PolyvSendCupEvent.class, message, event);
                        if (sendCupEvent != null && sendCupEvent.getOwner() != null && sendCupEvent.getOwner().getUserId() != null) {
                            if (sendCupEvent.getOwner().getUserId().equals(loginId)) {
                                cupLayout.setVisibility(View.VISIBLE);
                                cupNumView.setText(sendCupEvent.getOwner().getNum() > 99 ? "99+" : (sendCupEvent.getOwner().getNum() + ""));
                            }
                        }
                    }
                }

                @Override
                public void onDestroy() {
                }
            });
        }
    }

    public ViewGroup getFirstLinkMicView() {
        return (ViewGroup) parentView.getChildAt(0);
    }

    //点击选中的连麦人
    public ViewGroup getSelectedLinkMicView() {
        return selectedLinkMicView;
    }

    public View getTeacherParentView() {
        return teacherParentView.findViewById(R.id.polyv_link_mic_camera_layout);
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


    public synchronized boolean addData(PolyvJoinInfoEvent requestSEvent, boolean updateImmidately) {
        if (requestSEvent == null || joins.containsKey(requestSEvent.getUserId()) || TextUtils.isEmpty(requestSEvent.getUserId())) {
            PolyvCommonLog.d(TAG, "contains userid  || userid is  :");
            return false;
        }
        try {
            //如果不是参与者 不能显示mic
            if("viewer".equals(requestSEvent.getUserType())){
                if(requestSEvent.getClassStatus() == null || !requestSEvent.getClassStatus().isVoice()){
                    PolyvCommonLog.d(TAG,"add data is not voice"+requestSEvent.toString());
                    return false;
                }
            }

            if (!uids.contains(requestSEvent.getUserId())) {
                //老师放在第一位ol
                if ("teacher".equals(requestSEvent.getUserType())) {
                    if(isNormalLive){
                        return false;
                    }
                    PolyvCommonLog.d(TAG,"add data is teacher"+requestSEvent.toString());
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

                    //将第一个位置腾出来 整体往后往下移动
                    resetChildViewPos(true);

                    onBindViewHolder(onCreateViewHolder(parentView, 0), 0);
                } else {
                    requestSEvent.setPos(uids.size() - 1);
                    onBindViewHolder(onCreateViewHolder(parentView, uids.size() - 1), uids.size() - 1);
//                    notifyItemInserted(uids.size() - 1);//通知数据与界面重新绑定
                }
            }

            arrangeDataPos();
        } catch (Exception e) {
            PolyvCommonLog.e(TAG, e.getMessage());
        }

        return true;
    }

    /**
     * 重排连麦布局位置
     * @param idleFirst 是否把第一个位置留出来给老师
     */
    private void resetChildViewPos(boolean idleFirst) {
        int childCount = parentView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parentView.getChildAt(i);
            FrameLayout.LayoutParams layoutParams = createLayoutParams(idleFirst?i+1:i);
            child.setLayoutParams(layoutParams);
        }
    }

    // 对连麦者位置序号重新排序
    private void arrangeDataPos() {

        int length = uids.size();
        for (int i = 0; i < length; i++) {
            PolyvJoinInfoEvent infoEvent = joins.get(uids.get(i));
            if(infoEvent != null){
                PolyvCommonLog.e(TAG, "update :" + infoEvent.getNick());
                infoEvent.setPos(i);
            }
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

        resetChildViewPos(false);
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

    @Override
    public int getCount() {
        if(joins == null){
            return 0;
        }
        return joins.size();
    }

    public void clear() {
        clearSurfaceview();
        uids.clear();
        joins.clear();
        teacherView = null;
        cameraView = null;

        ownerView = null;
    }


    public  void setRowCount(int rowCount) {
        this.rowCount = rowCount;
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
