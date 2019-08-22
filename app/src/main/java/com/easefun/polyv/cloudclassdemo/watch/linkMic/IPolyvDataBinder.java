package com.easefun.polyv.cloudclassdemo.watch.linkMic;

import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;

import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author df
 * @create 2018/11/20
 * @Describe
 */
public abstract class IPolyvDataBinder {

    protected Disposable ownerShowTimer;//头像显示切换定时器
    protected View ownerView, ownerMic,ownerLinkView;//自己的布局view
    protected ViewGroup parentView;
    protected ViewGroup selectedLinkMicView;

    protected static final int SHOW_TIME = 5 * 1000;
    protected boolean isAudio;

    public abstract void addOwner(String myUid, PolyvJoinInfoEvent owern);

    public abstract boolean notifyItemChanged(int pos, boolean mute);


    public  void addParent(ViewGroup linkMicLayout){
        parentView = linkMicLayout;
    };

    public void switchView(String originUid) {
    }

    public ViewGroup getSwitchView(String userId) {
        return null;
    }

    public int getJoinsPos(String uid) {
        return -1;
    }

    public void updateSwitchViewStatus(String subLinkMicViewUid, String mainLinkMicViewUid) {

    }

    public ViewGroup getFirstLinkMicView() {
        return null;
    }

    public ViewGroup getSelectedLinkMicView() {
        return null;
    }

    public View getTeacherParentView() {
        return null;
    }

    public View getCameraView(View parent) {
        return null;
    }

    public View getCameraView() {
        return null;
    }

    public void updateCameraStatus(boolean cameraOpen) {

    }

    public void setAudio(boolean audio) {
        isAudio = audio;
    }

    public View getOwnerView() {
        return null;
    }

    public abstract boolean addData(PolyvJoinInfoEvent requestSEvent, boolean updateImmidately);

    public abstract void removeData(String uid, boolean removeView);

    public abstract void clear();

    public void startAudioWave(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume){

    };

    public void bindLinkMicFrontView(ViewGroup frontView){

    }

    protected void startShowTimer() {
        if (ownerShowTimer != null) {
            ownerShowTimer.dispose();
            ownerShowTimer = null;
        }
        ownerShowTimer = PolyvRxTimer.delay(SHOW_TIME, new Consumer<Long>() {
            @Override
            public void accept(Long l) throws Exception {
                if (ownerMic != null) {
                    ownerMic.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void updateLayoutStyle(int orientation) {

    }

    public abstract void bindItemClickListener(View.OnClickListener clickListener);

    /**
     *  连麦麦克风静音状态更新
     * @param mute
     */
    public void showMicOffLineView(boolean mute) {

    }

    /**
     *    自己摄像头状态更新
     * @param mute
     */
    public void showCameraOffLineView(boolean mute) {

    }

    /**
     *  连麦麦克风静音状态更新
     * @param mute
     * @param pos
     */
    public  boolean showMicOffLineView(boolean mute, int pos){

        return true;
    };

    public int getCount(){
        return 0;
    }

    /**
     * 更改主讲老师图标
     *  变为主讲的userid
     */
    public boolean changeTeacherLogo(String toTeacherId,boolean hasPermission) {

        return true;
    }

    /**
     * 更新主讲老师的view  当主讲老师被更新到主屏幕后需要手动更新缓存的主讲人view
     * @param updateView
     */
    public void updateTeacherLogoView(View updateView) {

    }
}
