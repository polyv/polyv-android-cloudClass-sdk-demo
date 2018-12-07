package com.easefun.polyv.cloudclassdemo.watch.linkMic;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.widget.PolyvLinkMicWaveView;
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
    protected View ownerView, ownerCamera;//自己的布局view
    protected static final int SHOW_TIME = 5 * 1000;

    public abstract void addOwner(String myUid, PolyvJoinInfoEvent owern);

    public abstract void addParent(LinearLayout linkMicLayout);

    public abstract void notifyItemChanged(int pos, boolean mute);

    public void switchView(String originUid) {
    }

    ;

    public ViewGroup switchViewToMianScreen(String userId) {
        return null;
    }

    ;

    public int getJoinsPos(String uid) {
        return -1;
    }

    ;

    public void updateSwitchViewStatus(String subLinkMicViewUid, String mainLinkMicViewUid) {

    }

    ;

    public ViewGroup getFirstLinkMicView() {
        return null;
    }

    ;

    public View getTeacherParentView() {
        return null;
    }

    ;

    public View getCameraView(View parent) {
        return null;
    }

    ;

    public View getCameraView() {
        return null;
    }

    ;

    public void updateCamerStatus(boolean cameraOpen) {

    }

    public void setAudio(boolean audio) {
    }

    public View getOwnerView() {
        return null;
    }

    public abstract void addData(PolyvJoinInfoEvent requestSEvent, boolean updateImmidately);

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
                if (ownerCamera != null) {
                    ownerCamera.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
