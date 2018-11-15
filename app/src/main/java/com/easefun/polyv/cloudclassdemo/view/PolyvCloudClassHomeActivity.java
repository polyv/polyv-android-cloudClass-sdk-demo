package com.easefun.polyv.cloudclassdemo.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvConnectStatusListener;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvJSQuestionVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionSocketVO;
import com.easefun.polyv.cloudclass.video.PolyvAnswerWebView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.player.PolyvCloudClassMediaController;
import com.easefun.polyv.cloudclassdemo.player.PolyvCloudClassVideoHelper;
import com.easefun.polyv.cloudclassdemo.player.PolyvCloudClassVideoItem;
import com.easefun.polyv.cloudclassdemo.player.PolyvOrientoinListener;
import com.easefun.polyv.commonui.PolyvCommonVideoHelper;
import com.easefun.polyv.commonui.adapter.PolyvFragmentAdapter;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.PolyvVodVideoHelper;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.view.PolyvVodVideoItem;
import com.easefun.polyv.commonui.player.widget.PolyvSlideSwitch;
import com.easefun.polyv.commonui.presenter.IPolyvHomePresnter;
import com.easefun.polyv.commonui.utils.PolyvChatEventBus;
import com.easefun.polyv.commonui.view.PolyvBaseChatFragment;
import com.easefun.polyv.commonui.view.PolyvFragmentGroupChat;
import com.easefun.polyv.commonui.view.PolyvFragmentPrivateChat;
import com.easefun.polyv.commonui.widget.PolyvAnswerView;
import com.easefun.polyv.commonui.widget.PolyvLinkMicListView;
import com.easefun.polyv.commonui.widget.PolyvTouchContainer;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;

public class PolyvCloudClassHomeActivity extends PolyvBaseActivity
        implements View.OnClickListener, IPolyvHomePresnter {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private LinearLayout chatTopSelectLayout;
    private RelativeLayout personalChatItemLayout;
    private RelativeLayout groupChatItemLayout;
    private FrameLayout flContainer, imageViewerContainer, chatEditContainer;
    private ViewPager cloudClassMainViewPager;
    private View lastView;
    private PolyvFragmentAdapter chatPagerAdapter;
    private PolyvCloudClassVideoHelper livePlayerHelper;
    private PolyvCommonVideoHelper vodVideoHelper;
    private PolyvSlideSwitch polyvSlideSwitch;

    private String userId, channelId, videoId;

    private PolyvTouchContainer videoPptContainer;
    //聊天室管理类
    private PolyvChatManager chatManager = new PolyvChatManager();
    private PolyvAnswerView answerView;
    private ViewGroup answerContainer;
    private LinearLayout linkMicLayout;
    private PolyvLinkMicListView linkMicLayoutParent;

    private static final String TAG = "PolyvCloudClassHomeActivity";
    private static final String CHANNELID_KEY = "channelid";
    private static final String USERID_KEY = "userid";
    private static final String VIDEOID_KEY = "videoid";
    private static final String PLAY_TYPE_KEY = "playtype";
    private static final String DEFAULT_NICKNAME = "POLYV";

    private int playMode;

    private RotationObserver rotationObserver;
    private PolyvOrientoinListener orientoinListener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="入口">
    public static void startActivityForLive(Activity activity, String channelId, String userId) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_LIVE);
        activity.startActivity(intent);
    }

    public static void startActivityForPlayBack(Activity activity, String videoId) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(VIDEOID_KEY, videoId);
        intent.putExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_VOD);
        activity.startActivity(intent);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果创建失败，则不初始化
        if (!isCreateSuccess) {
            return;
        }

        initialParams();

        //如果用户被踢，则不初始化
        if (checkKickTips(channelId)) {
            return;
        }

        setContentView(R.layout.polyv_activity_cloudclass_home);

        initial();

        if (playMode == PolyvPlayOption.PLAYMODE_LIVE) {
            loginChatRoom();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInitialize()) {
            //未初始化时，不执行以下代码，避免出现空指针异常
            return;
        }
        if (livePlayerHelper != null) {
            livePlayerHelper.resume();
        }
        if (vodVideoHelper != null) {
            vodVideoHelper.resume();
        }

        //注册观察变化
        rotationObserver.startObserver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isInitialize()) {
            //未初始化时，不执行以下代码，避免出现空指针异常
            return;
        }
        if (livePlayerHelper != null) {
            livePlayerHelper.pause();
        }
        if (vodVideoHelper != null) {
            vodVideoHelper.pause();
        }

        //解除观察变化
        rotationObserver.stopObserver();
    }

    @Override
    protected void onDestroy() {
        PolyvCommonLog.d(TAG, "home ondestory");
        super.onDestroy();
        if (!isInitialize()) {
            //未初始化时，不执行以下代码，避免出现空指针异常
            return;
        }
        if (livePlayerHelper != null) {
            livePlayerHelper.destory();
        }
        if (vodVideoHelper != null) {
            vodVideoHelper.destory();
        }

        if (answerView != null) {
            answerView.destroy();
            answerView = null;
        }

        if (orientoinListener != null) {
            orientoinListener.disable();
            orientoinListener = null;
        }

        if (chatManager != null) {
            chatManager.destroy();
        }

        PolyvLinkMicWrapper.getInstance().destroy(linkMicLayout);
        PolyvChatEventBus.clear();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initial() {
        PolyvScreenUtils.generateHeight4_3(this);

        initialLinkMic();
        initialChatRoom();
        intialPpt();
        initialAnswer();
        initialVideo();
        intialOretation();
    }

    private void initialParams() {
        Intent intent = getIntent();
        channelId = intent.getStringExtra(CHANNELID_KEY);
        userId = intent.getStringExtra(USERID_KEY);
        videoId = intent.getStringExtra(VIDEOID_KEY);
        playMode = intent.getIntExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_VOD);
    }

    private void initialLinkMic() {
        linkMicLayout = findViewById(R.id.link_mic_layout);
        linkMicLayoutParent = findViewById(R.id.link_mic_layout_parent);
        if (playMode == PolyvPlayOption.PLAYMODE_LIVE) {
            PolyvLinkMicWrapper.getInstance().init(this);
            PolyvLinkMicWrapper.getInstance().intialConfig(channelId);
        }
    }

    private void initialChatRoom() {
        imageViewerContainer = findViewById(R.id.fl_image_viewer_container);
        chatEditContainer = findViewById(R.id.fl_chat_edit_container);
        chatTopSelectLayout = findViewById(R.id.chat_top_select_layout);
        personalChatItemLayout = findViewById(R.id.personal_chat_item_layout);
        groupChatItemLayout = findViewById(R.id.group_chat_item_layout);
        cloudClassMainViewPager = findViewById(R.id.cloud_class_main_viewpager);
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) { // 回放
            chatTopSelectLayout.setVisibility(View.GONE);
            cloudClassMainViewPager.setVisibility(View.GONE);
        } else { // 直播
            personalChatItemLayout.setOnClickListener(this);
            groupChatItemLayout.setOnClickListener(this);

            List<Fragment> fragments = new ArrayList<>();
            fragments.add(new PolyvFragmentGroupChat().setChatManager(chatManager));
            fragments.add(new PolyvFragmentPrivateChat().setChatManager(chatManager));
            chatPagerAdapter = new PolyvFragmentAdapter(getSupportFragmentManager(), fragments);
            cloudClassMainViewPager.setAdapter(chatPagerAdapter);
            cloudClassMainViewPager.setPageMargin(ConvertUtils.dp2px(10));
            cloudClassMainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (lastView != null) {
                        lastView.setSelected(false);
                        lastView = chatTopSelectLayout.getChildAt(position);
                        if (lastView != null) {
                            lastView.setSelected(true);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            cloudClassMainViewPager.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top,
                                           int right, int bottom, int oldLeft,
                                           int oldTop, int oldRight, int oldBottom) {
                    // 当键盘弹出导致布局发生改变时，需要对应的改变播放器的位置，键盘关闭时恢复到原来的位置
                    if (bottom > 0 && oldBottom > 0 && right == oldRight) {
                        if (Math.abs(bottom - oldBottom) > PolyvScreenUtils.getNormalWH(PolyvCloudClassHomeActivity.this)[1] * 0.3)
                            // 键盘关闭
                            if (bottom > oldBottom) {
                                resetSubVideo();
                            }// 键盘弹出
                            else if (bottom < oldBottom) {
                                moveSubVideo();
                            }
                    }
                }
            });
            groupChatItemLayout.setSelected(true);
            lastView = groupChatItemLayout;
            cloudClassMainViewPager.setCurrentItem(0);
        }
    }

    private void intialPpt() {
        videoPptContainer = findViewById(R.id.video_ppt_container);

        videoPptContainer.setOriginLeft(ScreenUtils.getScreenWidth()- PolyvScreenUtils.dip2px
                (PolyvCloudClassHomeActivity.this,144));

        videoPptContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams rlp = getLayoutParamsLayout(videoPptContainer);
                if (rlp == null) {
                    return;
                }

                rlp.leftMargin = ((View) videoPptContainer.getParent()).getMeasuredWidth() - videoPptContainer.getMeasuredWidth();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rlp.topMargin = 0;
                } else { // 若初始为竖屏
                    rlp.topMargin = chatTopSelectLayout.getBottom();
                }
                videoPptContainer.setOriginTop(chatTopSelectLayout.getBottom());

                videoPptContainer.setLayoutParams(rlp);
                PolyvCommonLog.d(TAG, "top:" + PolyvScreenUtils.px2dip(PolyvCloudClassHomeActivity.this, rlp.topMargin));

                if (Build.VERSION.SDK_INT >= 16) {
                    videoPptContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    videoPptContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    /**
     * 答题相关
     */
    private void initialAnswer() {
        answerView = findViewById(R.id.answer_layout);
        answerContainer = answerView.findViewById(R.id.polyv_answer_web_container);
        answerView.setAnswerJsCallback(new PolyvAnswerWebView.AnswerJsCallback() {
            @Override
            public void callOnHasAnswer(PolyvJSQuestionVO polyvJSQuestionVO) {
                PolyvCommonLog.d(TAG, "send to server has choose answer");
                if (chatManager != null) {
                    PolyvQuestionSocketVO socketVO = new PolyvQuestionSocketVO
                            (polyvJSQuestionVO.getAnswerId(), DEFAULT_NICKNAME, polyvJSQuestionVO.getQuestionId(),
                                    channelId, chatManager.userId);
                    chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, socketVO);
                }
            }
        });
    }

    private void initialVideo() {
        PolyvCommonLog.d(TAG, "initialVodVideo");

        // 播放器
        flContainer = findViewById(R.id.fl_container);
        ViewGroup.LayoutParams vlp = flContainer.getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = PolyvScreenUtils.getHeight4_3();

        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            initialVodVideo();
        } else {
            initialLiveVideo();
        }
    }

    private void intialOretation() {
        //创建观察类对象
        rotationObserver = new RotationObserver(new Handler());
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            orientoinListener = new PolyvOrientoinListener(this, vodVideoHelper);
        } else {
            orientoinListener = new PolyvOrientoinListener(this, livePlayerHelper);
        }

        boolean autoRotateOn = (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        //检查系统是否开启自动旋转
        if (autoRotateOn) {
            orientoinListener.enable();
        } else {
            orientoinListener.disable();
        }
    }

    private void initialVodVideo() {
        PolyvVodVideoItem polyvVodVideoItem = new PolyvVodVideoItem(this);
        PolyvPPTItem polyvPPTItem = new PolyvPPTItem<PolyvCloudClassMediaController>(this);
        vodVideoHelper = new PolyvVodVideoHelper(polyvVodVideoItem, polyvPPTItem);
        vodVideoHelper.addVideoPlayer(flContainer);
        vodVideoHelper.initConfig();
        vodVideoHelper.addPPT(videoPptContainer);

        // TODO: 2018/9/12 videoId 为直播平台的 videopoolid为点播平台的视频id
        PolyvBaseVideoParams polyvBaseVideoParams = new PolyvBaseVideoParams(videoId//videoId
                , "123");
        polyvBaseVideoParams.setChannelId(channelId);
        polyvBaseVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(polyvBaseVideoParams.IS_PPT_PLAY, true);
        vodVideoHelper.startPlay(polyvBaseVideoParams);
    }


    private void initialLiveVideo() {
        PolyvCloudClassVideoItem cloudClassVideoItem = new PolyvCloudClassVideoItem(this);
        PolyvPPTItem polyvPPTItem = new PolyvPPTItem<PolyvCloudClassMediaController>(this);

        livePlayerHelper = new PolyvCloudClassVideoHelper(cloudClassVideoItem, polyvPPTItem, chatManager);
        livePlayerHelper.addVideoPlayer(flContainer);
        livePlayerHelper.initConfig();
        livePlayerHelper.addPPT(videoPptContainer);

        livePlayerHelper.addLinkMicLayout(linkMicLayout,linkMicLayoutParent);
        linkMicLayoutParent.resetFloatViewPort();

        PolyvBaseVideoParams polyvBaseVideoParams = new PolyvBaseVideoParams(channelId, userId, "123");
        polyvBaseVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true);
        livePlayerHelper.startPlay(polyvBaseVideoParams);
    }

    private ViewGroup.MarginLayoutParams getLayoutParamsLayout(View layout) {
        ViewGroup.MarginLayoutParams rlp = null;
        if (layout.getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        } else if (layout.getParent() instanceof LinearLayout) {
            rlp = (LinearLayout.LayoutParams) layout.getLayoutParams();
        } else if (layout.getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) layout.getLayoutParams();
        }

        return rlp;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View控制">
    private void resetSubVideo() {
        if (videoPptContainer != null) {
            videoPptContainer.resetSoftTo();
        }

        if (linkMicLayoutParent != null) {
            linkMicLayoutParent.resetSoftTo();
        }
    }

    private void moveSubVideo() {
        if (videoPptContainer != null) {
            videoPptContainer.topSubviewTo(chatTopSelectLayout.getTop());
        }

        if (linkMicLayoutParent != null) {
            linkMicLayoutParent.topSubviewTo(chatTopSelectLayout.getTop());
        }
    }

    private void removeView() {
        flContainer.removeAllViews();
        videoPptContainer.removeAllViews();
        livePlayerHelper = null;
        vodVideoHelper = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick方法">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.group_chat_item_layout:
                cloudClassMainViewPager.setCurrentItem(0);
                break;
            case R.id.personal_chat_item_layout:
                cloudClassMainViewPager.setCurrentItem(1);
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onActivityResult方法">
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (livePlayerHelper != null) {
            livePlayerHelper.onActivityResult(requestCode, resultCode, data);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onRequestPermissionsResult方法">
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (livePlayerHelper != null) {
            livePlayerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onKeyDown方法">
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (answerContainer != null && answerContainer.isShown()) {
                answerContainer.setVisibility(View.INVISIBLE);
                return true;
            }
            if (PolyvScreenUtils.isLandscape(this)) {
                if (livePlayerHelper != null) {
                    livePlayerHelper.changeToPortrait();
                }
                if (vodVideoHelper != null) {
                    vodVideoHelper.changeToPortrait();
                }
                return true;
            } else if (chatPagerAdapter != null) {
                Fragment fragment = chatPagerAdapter.getItem(cloudClassMainViewPager.getCurrentItem());
                if (fragment instanceof PolyvBaseChatFragment) {
                    if (((PolyvBaseChatFragment) fragment).onBackPressed())
                        return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录聊天室">
    private void loginChatRoom() {
        PolyvChatEventBus.clear();
        //先添加监听器再登录
        chatManager.addConnectStatusListener(new PolyvConnectStatusListener() {
            @Override
            public void onConnectStatusChange(int status, @Nullable Throwable t) {
                //由于登录成功后，Fragment可能还没初始化完成，所以这里使用Rxjava的ReplayRelay把信息保存下来，在Fragment初始化完成那里获取
                PolyvChatEventBus.get().post(new PolyvBaseChatFragment.ConnectStatus(status, t));
            }
        });
        chatManager.addNewMessageListener(new PolyvNewMessageListener() { // 用于监听登录的相关消息
            @Override
            public void onNewMessage(String message, String event) {
                //由于登录成功后，Fragment可能还没初始化完成，所以这里使用Rxjava的ReplayRelay把信息保存下来，在Fragment初始化完成那里获取
                PolyvChatEventBus.get().post(new PolyvBaseChatFragment.EventMessage(message, event));
            }

            @Override
            public void onDestroy() {
            }
        });
        chatManager.addNewMessageListener(new PolyvNewMessageListener() { // 用于监听PPT的相关消息
            @Override
            public void onNewMessage(String message, String event) {
                if (ONSLICECONTROL.equals(event) || ONSLICEID.equals(event)) {
                    //不影响主屏幕得切换跟隐藏显示按钮 主屏幕逻辑下  该消息会触发 交换按钮得逻辑变化
                    if (livePlayerHelper != null && livePlayerHelper.isJoinLinkMick()) {
                        livePlayerHelper.updateMainScreenStatus(message,event);
                        if(answerView != null){
                            answerView.processSocketMessage(new PolyvSocketMessageVO(message, event),event);
                        }
                        return;
                    }
                }
                PolyvRxBus.get().post(new PolyvSocketMessageVO(message, event));
            }

            @Override
            public void onDestroy() {
            }
        });

        //TODO 登录聊天室(userId：学员的Id(聊天室用户的唯一标识，非直播后台的userId，不同的学员应当使用不同的userId)，roomId：频道号，nickName：学员的昵称)
        //TODO 登录聊天室后一些功能才可以正常使用，例如：连麦
        String studentUserId = "" + Build.SERIAL;
        String studentNickName = "学员" + studentUserId;
        chatManager.login(studentUserId, channelId, studentNickName);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="屏幕旋转开关监听器">
    private class RotationObserver extends ContentObserver {
        ContentResolver mResolver;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public RotationObserver(Handler handler) {
            super(handler);
            mResolver = getContentResolver();
        }

        public void startObserver() {
            mResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, this);
        }

        public void stopObserver() {
            mResolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            PolyvCommonLog.d(TAG, "oreitation has changed");
            boolean autoRotateOn = (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
            //检查系统是否开启自动旋转
            if (autoRotateOn) {
                if (orientoinListener != null) {
                    orientoinListener.enable();
                }
            } else {
                if (orientoinListener != null) {
                    orientoinListener.disable();
                }
            }

        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPolyvHomePresnter实现">
    @Override
    public void sendDanmu(CharSequence content) {
        if (livePlayerHelper != null) {
            livePlayerHelper.sendDanmuMessage(content);
        }
    }

    @Override
    public ViewGroup getImageViewerContainer() {
        return imageViewerContainer;
    }

    @Override
    public ViewGroup getChatEditContainer() {
        return chatEditContainer;
    }
    // </editor-fold>

}
