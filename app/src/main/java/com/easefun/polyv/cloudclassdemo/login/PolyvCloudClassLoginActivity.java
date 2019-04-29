package com.easefun.polyv.cloudclassdemo.login;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.businesssdk.model.video.PolyvPlayBackVO;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodSDKClient;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.widget.PolyvSoftView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;

import java.io.IOException;

import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * @author df
 * @create 2018/8/27
 * @Describe
 */
public class PolyvCloudClassLoginActivity extends PolyvBaseActivity implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private ImageView loginLogo;
    private TextView loginLogoText;
    private EditText userId;
    private EditText channelId;
    private EditText appId;
    private EditText appSecert;
    private TextView loginTv;
    private PolyvSoftView softLayout;
    private LinearLayout playbackLayout, liveLayout;
    private EditText playbackVideoId,playbackChannelId;
    private EditText playbackAppId, playbackUserId;
    private RelativeLayout liveGroupLayout;
    private RelativeLayout playbackGroupLayout;
    private Disposable getTokenDisposable,verifyDispose;
    private ProgressDialog progress;

    private static final String TAG = "PolyvCloudClassLoginAct";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_cloudclass_login);
        initialView();

        setTestData();   // for test
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkLoginTvSelected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getTokenDisposable != null) {
            getTokenDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initialView() {
        initialTopLayout();
        initialLiveVideoView();
        initialPlayBackVideoView();
        intialLogoView();
    }

    private void initialTopLayout() {
        liveGroupLayout = findViewById(R.id.live_group_layout);
        playbackGroupLayout = findViewById(R.id.playback_group_layout);

        liveGroupLayout.setOnClickListener(this);
        playbackGroupLayout.setOnClickListener(this);

        liveGroupLayout.setSelected(true);
        playbackGroupLayout.setSelected(false);

        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.login_waiting));
        progress.setCanceledOnTouchOutside(false);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getTokenDisposable != null) {
                    getTokenDisposable.dispose();
                }
                if(verifyDispose != null){
                    verifyDispose.dispose();
                }
                loginTv.setEnabled(true);
            }
        });
    }

    private void initialLiveVideoView() {
        liveLayout = findViewById(R.id.live_layout);
        userId = findViewById(R.id.user_id);
        channelId = findViewById(R.id.channel_id);
        appId = findViewById(R.id.app_id);
        appSecert = findViewById(R.id.app_secert);

        userId.addTextChangedListener(textWatcher);
        channelId.addTextChangedListener(textWatcher);
        appId.addTextChangedListener(textWatcher);
        appSecert.addTextChangedListener(textWatcher);
    }

    private void initialPlayBackVideoView() {
        playbackLayout = findViewById(R.id.playback_layout);
        playbackVideoId = findViewById(R.id.playback_video_id);
        playbackChannelId = findViewById(R.id.playback_channel_id);
        playbackAppId = findViewById(R.id.playback_app_id);
        playbackUserId = findViewById(R.id.playback_user_id);

        playbackVideoId.addTextChangedListener(textWatcher);
        playbackChannelId.addTextChangedListener(textWatcher);
        playbackAppId.addTextChangedListener(textWatcher);
        playbackUserId.addTextChangedListener(textWatcher);
    }

    private void intialLogoView() {
        loginLogo = findViewById(R.id.login_logo);
        loginLogoText = findViewById(R.id.login_logo_text);
        loginTv = findViewById(R.id.login);

        softLayout = findViewById(R.id.polyv_soft_listener_layout);
        softLayout.setOnKeyboardStateChangedListener(new PolyvSoftView.IOnKeyboardStateChangedListener() {
            @Override
            public void onKeyboardStateChanged(int state) {
                showTitleLogo(state != PolyvSoftView.KEYBOARD_STATE_SHOW);
            }
        });

        loginTv.setOnClickListener(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置测试数据">
    private void setTestData() {
        appId.setText("");
        appSecert.setText("");
        userId.setText("");
        channelId.setText("");

        playbackChannelId.setText("218822");
        playbackUserId.setText("3e30e533a1");
        playbackVideoId.setText("");
        playbackAppId.setText("");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick方法">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.login:
                login();
                break;
            case R.id.live_group_layout:
                showLiveGroup();
                break;
            case R.id.playback_group_layout:
                showPlayBackGroup();
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View显示控制">
    private void showTitleLogo(boolean showlog) {
        loginLogoText.setVisibility(!showlog ? View.VISIBLE : View.GONE);
        loginLogo.setVisibility(showlog ? View.VISIBLE : View.GONE);
    }

    private void showLiveGroup() {
        liveGroupLayout.setSelected(true);
        playbackGroupLayout.setSelected(false);

        liveLayout.setVisibility(View.VISIBLE);
        playbackLayout.setVisibility(View.GONE);

        loginTv.setSelected(!TextUtils.isEmpty(userId.getText())
                && !TextUtils.isEmpty(appSecert.getText())
                && (!TextUtils.isEmpty(channelId.getText())
                && !TextUtils.isEmpty(appId.getText())));
    }

    private void showPlayBackGroup() {
        liveGroupLayout.setSelected(false);
        playbackGroupLayout.setSelected(true);

        liveLayout.setVisibility(View.GONE);
        playbackLayout.setVisibility(View.VISIBLE);

        loginTv.setSelected(!isEmpty(playbackAppId)
                && !isEmpty(playbackVideoId));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="textWatcher监听">
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkLoginTvSelected();
        }
    };

    private void checkLoginTvSelected() {
        if (liveGroupLayout.isSelected()) {
            loginTv.setSelected(!isEmpty(userId) && !isEmpty(appSecert) &&
                    !isEmpty(channelId)
                    && !isEmpty(appId)
            );
        } else {
            loginTv.setSelected(!isEmpty(playbackVideoId)
                    && !isEmpty(playbackAppId)
                    && !isEmpty(playbackUserId)
                    && !isEmpty(playbackChannelId)
            );//
        }
    }

    private boolean isEmpty(TextView v) {
        return TextUtils.isEmpty(v.getText().toString());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录处理">
    private void login() {
        if (!loginTv.isSelected()) {
            return;
        }

        loginTv.setEnabled(false);
        progress.show();
        if (liveGroupLayout.isSelected()) {
            checkToken(getTrim(userId), getTrim(appSecert),
                    getTrim(channelId), null, getTrim(appId));
        } else {
            checkToken(getTrim(playbackUserId), null, getTrim(playbackChannelId),
                    getTrim(playbackVideoId), getTrim(playbackAppId));
        }
    }

    private String getTrim(EditText playbackUserId) {
        return playbackUserId.getText().toString().trim();
    }

    private void checkToken(String userId, String appSecret, String channel, String vid, String appId) {
        //请求token接口
        getTokenDisposable = PolyvLoginManager.checkLoginToken(userId, appSecret, appId,
                channel, vid,
                new PolyvrResponseCallback<PolyvResponseBean>() {
                    @Override
                    public void onSuccess(PolyvResponseBean responseBean) {
                        PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecert.getText().toString());
                        PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecert.getText().toString());
                        PolyvVodSDKClient.getInstance().initConfig(appId, appSecert.getText().toString());

                        if(playbackGroupLayout.isSelected()){
                            requestPlayBackStatus(userId,vid);
                            return;
                        }
                        requestLiveStatus(userId);

                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvResponseBean> responseBean) {
                        super.onFailure(responseBean);
                        failedStatus(responseBean.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        errorStatus(e);
                    }
                });
    }

    private void requestPlayBackStatus(String userId, String vid) {
        if(TextUtils.isEmpty(vid)){
            return;
        }
        verifyDispose = PolyvLoginManager.getPlayBackType(vid, new PolyvrResponseCallback<PolyvPlayBackVO>() {
            @Override
            public void onSuccess(PolyvPlayBackVO playBack) {
                boolean isLivePlayBack = playBack.getLiveType() == 0;
                startActivityForPlayback(userId,isLivePlayBack);
            }

           @Override
           public void onFailure(PolyvResponseBean<PolyvPlayBackVO> responseBean) {
               super.onFailure(responseBean);
               failedStatus(responseBean.getMessage());
           }

           @Override
           public void onError(Throwable e) {
               super.onError(e);
               errorStatus(e);
           }
       });
    }

    public void failedStatus(String message) {
        ToastUtils.showLong(message);
        loginTv.setEnabled(true);
        progress.dismiss();
    }

    public void errorStatus(Throwable e) {
        PolyvCommonLog.exception(e);
        loginTv.setEnabled(true);
        progress.dismiss();
        if (e instanceof HttpException) {
            try {
                ToastUtils.showLong(((HttpException) e).response().errorBody().string());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            ToastUtils.showLong(e.getMessage());
        }
    }

    private void requestLiveStatus(String userId) {
        verifyDispose = PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().geLiveStatusJson(channelId.getText().toString())
                , new PolyvrResponseCallback<PolyvLiveStatusVO>() {
                    @Override
                    public void onSuccess(PolyvLiveStatusVO statusVO) {
                        String data = statusVO.getData();
                        String[] dataArr = data.split(",");

                        boolean isAlone = "alone".equals(dataArr[1]);//是否有ppt

                        if (liveGroupLayout.isSelected()) {
                            startActivityForLive(userId, isAlone);
                        }
                        progress.dismiss();
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvLiveStatusVO> responseBean) {
                        super.onFailure(responseBean);
                        failedStatus(responseBean.getMessage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        errorStatus(e);
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="startActivity">
    private void startActivityForLive(String userId, boolean isAlone) {
        PolyvCloudClassHomeActivity.startActivityForLive(PolyvCloudClassLoginActivity.this,
                getTrim(channelId), userId, isAlone);
    }

    private void startActivityForPlayback(String userId, boolean isNormalLivePlayBack) {
        progress.dismiss();
        PolyvCloudClassHomeActivity.startActivityForPlayBack(PolyvCloudClassLoginActivity.this,
                getTrim(playbackVideoId),getTrim(playbackChannelId),getTrim(playbackUserId),isNormalLivePlayBack);
    }
    // </editor-fold>

}
