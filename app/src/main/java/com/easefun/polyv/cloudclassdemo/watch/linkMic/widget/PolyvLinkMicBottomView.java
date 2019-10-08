package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.easefun.polyv.businesssdk.model.link.PolyvLinkMicMedia;
import com.easefun.polyv.businesssdk.model.ppt.PolyvPPTAuthentic;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoHelper;
import com.easefun.polyv.commonui.widget.PolyvMediaCheckView;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.EVENT_MUTE_USER_MICRO;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.TEACHER_SET_PERMISSION;

/**
 * @author df
 * @create 2019/7/19
 * @Describe
 */
public class PolyvLinkMicBottomView extends LinearLayout implements View.OnClickListener {
    private LinearLayout linkMicBottomLayout;
    private LinearLayout linkMicBrushLayout;
    private PolyvMediaCheckView controllerRed;
    private PolyvMediaCheckView controllerYellow;
    private PolyvMediaCheckView controllerBlue;
    private ImageView controllerErase;
    private LinearLayout linkMicBottomController;
    private ImageView controllerBrush;
    private ImageView controllerMic;
    private ImageView controllerCamera;
    private ImageView controllerCameraSwitch;
    private ImageView controllerLinkMicCall;

    private PolyvMediaCheckView lastSelectedColor;

    private PolyvCloudClassVideoHelper cloudClassVideoHelper;

    public PolyvLinkMicBottomView(Context context) {
        this(context, null);
    }

    public PolyvLinkMicBottomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PolyvLinkMicBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initWdiget();
    }

    private void initWdiget() {
        View.inflate(getContext(), R.layout.link_mic_bottom, this);
        initView();
        addListener();
        addSocketListener();
    }

    public void addClassHelper(PolyvCloudClassVideoHelper classVideoHelper){
        this.cloudClassVideoHelper = classVideoHelper;
    }

    /**
     * 监听socket事件
     */
    private void addSocketListener() {
        PolyvChatManager.getInstance().addNewMessageListener(new PolyvNewMessageListener() {
            @Override
            public void onNewMessage(String message, String event) {

                //离开连麦 隐藏自己 打开连麦显示自己
                //收到画笔可用信息 显示画笔
                //监听 mic 或者是摄像头被关闭或者打开事件

                if(EVENT_MUTE_USER_MICRO.equals(event)){//禁麦事件
                    PolyvLinkMicMedia micMedia = PolyvGsonUtil.fromJson(PolyvLinkMicMedia.class, message);
                    processMediaMessage(micMedia);
                }else if(TEACHER_SET_PERMISSION.equals(event)){//老师画笔赋予权限

                    PolyvPPTAuthentic polyvPPTAuthentic = PolyvGsonUtil.fromJson(PolyvPPTAuthentic.class,message);
                    String type = polyvPPTAuthentic.getType();
                    if(!polyvPPTAuthentic.getUserId().equals(PolyvChatManager.getInstance().userId)){
                        return;
                    }
                    if(polyvPPTAuthentic.hasPPTOrAboveType()){
                        //授权画笔权限
                        controllerBrush.setVisibility("1".equals(polyvPPTAuthentic.getStatus())?VISIBLE:GONE);
                        // 如果ppt显示在主屏 才显示画笔为可点击状态
                        if(cloudClassVideoHelper.pptShowMainScreen()){
                            linkMicBrushLayout.setVisibility("1".equals(polyvPPTAuthentic.getStatus())?VISIBLE:GONE);
                            controllerBrush.setSelected(false);
                        }else{
                            linkMicBrushLayout.setVisibility(GONE);
                            controllerBrush.setSelected(true);
                        }
                    }

                }
            }

            @Override
            public void onDestroy() {

            }
        });
    }

    private void processMediaMessage(PolyvLinkMicMedia micMedia) {
        if (micMedia == null) {
            return;
        }

        if ("video".equals(micMedia.getType())) {
            controllerCamera.setSelected(micMedia.isMute());
        } else {
            controllerMic.setSelected(micMedia.isMute());
        }
    }

    private void addListener() {
        controllerRed.setOnClickListener(this);
        controllerBlue.setOnClickListener(this);
        controllerYellow.setOnClickListener(this);
        controllerBrush.setOnClickListener(this);
        controllerCamera.setOnClickListener(this);
        controllerMic.setOnClickListener(this);
        controllerCameraSwitch.setOnClickListener(this);
        controllerLinkMicCall.setOnClickListener(this);
        controllerErase.setOnClickListener(this);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            setPadding(0, 0, 0, PolyvScreenUtils.dip2px(getContext(), 92));
            post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams controller = linkMicBottomController.getLayoutParams();
                    ViewGroup.LayoutParams brush = linkMicBrushLayout.getLayoutParams();
                    MarginLayoutParams erase = (MarginLayoutParams) controllerErase.getLayoutParams();

                    erase.leftMargin = PolyvScreenUtils.dip2px(getContext(),12);
                    erase.topMargin = 0;
                    controller.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    controller.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    brush.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    brush.width = ViewGroup.LayoutParams.MATCH_PARENT;

                    linkMicBottomLayout.setOrientation(VERTICAL);
                    linkMicBottomController.setOrientation(HORIZONTAL);
                    linkMicBrushLayout.setOrientation(HORIZONTAL);
                }
            });

        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            setPadding(0, 0, 0, 0);
            post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams controller = linkMicBottomController.getLayoutParams();
                    ViewGroup.LayoutParams brush = linkMicBrushLayout.getLayoutParams();
                    MarginLayoutParams erase = (MarginLayoutParams) controllerErase.getLayoutParams();

                    erase.leftMargin = 0;
                    erase.topMargin = PolyvScreenUtils.dip2px(getContext(),12);
                    controller.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    controller.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    brush.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    brush.width = ViewGroup.LayoutParams.WRAP_CONTENT;

                    linkMicBottomLayout.setOrientation(HORIZONTAL);
                    linkMicBottomController.setOrientation(VERTICAL);
                    linkMicBrushLayout.setOrientation(VERTICAL);
                }
            });

        }
    }

    private void initView() {
        linkMicBottomLayout = (LinearLayout) findViewById(R.id.link_mic_bottom_layout);
        linkMicBrushLayout = (LinearLayout) findViewById(R.id.link_mic_brush_layout);
        controllerRed =  findViewById(R.id.controller_red);
        lastSelectedColor = controllerRed;
        controllerRed.setChecked(true);
        controllerYellow = findViewById(R.id.controller_yellow);
        controllerBlue = findViewById(R.id.controller_blue);
        controllerErase = (ImageView) findViewById(R.id.controller_erase);
        linkMicBottomController = (LinearLayout) findViewById(R.id.link_mic_bottom_controller);
        controllerBrush = (ImageView) findViewById(R.id.controller_brush);
        controllerMic = (ImageView) findViewById(R.id.controller_mic);
        controllerCamera = (ImageView) findViewById(R.id.controller_camera);
        controllerCameraSwitch = (ImageView) findViewById(R.id.controller_camera_switch);
        controllerLinkMicCall = (ImageView) findViewById(R.id.controller_link_mic_call);

        showPaint = controllerBrush.isSelected();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.controller_blue:
            case R.id.controller_yellow:
            case R.id.controller_red:
                updateBraushColorSelected((PolyvMediaCheckView) v);
                break;
            case R.id.controller_brush:
                updateBrushLayout();
                break;
            case R.id.controller_camera_switch:
                PolyvLinkMicWrapper.getInstance().switchCamera();
                break;
            case R.id.controller_camera:
                controllerCameraStatus();
                break;
            case R.id.controller_link_mic_call:
                closeLinkMicCall();
                break;
            case R.id.controller_erase:
                updateEraseStatus(v);
                break;
            case R.id.controller_mic:
                controllerMicStatus();
                break;

        }
    }

    private void controllerCameraStatus() {
        controllerCamera.setSelected(!controllerCamera.isSelected());
        PolyvLinkMicMedia linkMicMedia = new PolyvLinkMicMedia();
        linkMicMedia.setMute(controllerCamera.isSelected());
        linkMicMedia.setType("video");
        if(!PolyvChatManager.getInstance().sendMuteEvent(linkMicMedia)){
            controllerCamera.setSelected(!controllerCamera.isSelected());
        }else {
            cloudClassVideoHelper.processMediaMessage(linkMicMedia);
        }
    }

    private void controllerMicStatus() {
        controllerMic.setSelected(!controllerMic.isSelected());

        PolyvLinkMicMedia linkMicMedia = new PolyvLinkMicMedia();
        linkMicMedia.setMute(controllerMic.isSelected());
        linkMicMedia.setType("audio");
        if(!PolyvChatManager.getInstance().sendMuteEvent(linkMicMedia)){
            controllerMic.setSelected(!controllerMic.isSelected());
        }else {
            cloudClassVideoHelper.processMediaMessage(linkMicMedia);
        }
    }

    protected void updateEraseStatus(View v) {
        v.setSelected(!v.isSelected());
        if(v.isSelected()){
            v.setEnabled(false);
            lastSelectedColor.setChecked(false);
        }
        cloudClassVideoHelper.updateEraseStatus(v.isSelected());
    }

    protected void updateBraushColorSelected(PolyvMediaCheckView v) {
        if(lastSelectedColor != null){
            lastSelectedColor.setChecked(false);
        }
        lastSelectedColor = v;
        lastSelectedColor.setChecked(true);
        controllerErase.setEnabled(true);
        controllerErase.setSelected(false);

        if(cloudClassVideoHelper != null){
            cloudClassVideoHelper.updateBrushColor(v.getBackgroundColor());
        }
    }

    public void updateBrushLayout() {
        View v = controllerBrush;
        if(!cloudClassVideoHelper.updateBrushStatus(!v.isSelected())){
            return;
        };

        //取消画笔 //更新画板状态 变为不可点击
        linkMicBrushLayout.setVisibility(v.isSelected() ? VISIBLE : GONE);
        v.setSelected(!v.isSelected());
    }

    private boolean showPaint;//记录上次的状态 回来以后好还原
    public void hideBrushColor(boolean pptShowMain){
        if(!pptShowMain){//如果是隐藏 记录上次状态
            showPaint = controllerBrush.isSelected();
            controllerBrush.setSelected(true);
        }else {//切换回来以后 更新为原来状态
            controllerBrush.setSelected(showPaint);
        }

        if(controllerBrush.isShown()){
            linkMicBrushLayout.setVisibility(!controllerBrush.isSelected()?VISIBLE :GONE);
        }
    }

    //关闭连麦
    private void closeLinkMicCall() {
        if(cloudClassVideoHelper != null){
            cloudClassVideoHelper.requestPermission();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cloudClassVideoHelper = null;
    }

    public void hideHandsUpLink(boolean hide) {
        controllerLinkMicCall.setVisibility(hide?GONE:VISIBLE);
    }

    public void updateLinkCameraController(boolean isVideo) {
        controllerCamera.setVisibility(isVideo?VISIBLE:GONE);
        controllerCameraSwitch.setVisibility(isVideo?VISIBLE:GONE);
    }


    public void updateLinkMicController(boolean show) {
        linkMicBottomLayout.setVisibility(show?VISIBLE:GONE);
    }
}
