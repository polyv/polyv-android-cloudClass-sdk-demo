package com.easefun.polyv.cloudclassdemo.watch;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvCloudClassVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvPlaybackVideoParams;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvConnectStatusListener;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener2;
import com.easefun.polyv.cloudclass.config.PolyvVClassGlobalConfig;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvJSQuestionVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionSocketVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionnaireSocketVO;
import com.easefun.polyv.cloudclass.model.sign_in.PolyvSignIn2SocketVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.cloudclass.video.PolyvAnswerWebView;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatBaseFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatFragmentAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatGroupFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatPrivateFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.liveInfo.PolyvLiveInfoFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.menu.PolyvCustomMenuFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.menu.PolyvTuWenMenuFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.playback.PolyvChatPlaybackFragment;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.widget.PolyvLinkMicParent;
import com.easefun.polyv.cloudclassdemo.watch.player.PolyvOrientoinListener;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassMediaController;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoHelper;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoItem;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.IPolyvLandscapeDanmuSender;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvChatPullLayout;
import com.easefun.polyv.cloudclassdemo.watch.player.live.widget.PolyvTeacherInfoLayout;
import com.easefun.polyv.cloudclassdemo.watch.player.playback.PolyvPlaybackVideoHelper;
import com.easefun.polyv.cloudclassdemo.watch.player.playback.PolyvPlaybackVideoItem;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.utils.PolyvSingleRelayBus;
import com.easefun.polyv.commonui.widget.PolyvAnswerView;
import com.easefun.polyv.commonui.widget.PolyvSimpleViewPager;
import com.easefun.polyv.commonui.widget.PolyvTouchContainerView;
import com.easefun.polyv.commonui.widget.badgeview.DisplayUtil;
import com.easefun.polyv.commonui.widget.badgeview.QBadgeView;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.socket.client.Socket;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.USERTYPE_VIEWER;
import static com.easefun.polyv.foundationsdk.config.PolyvPlayOption.PLAYMODE_LIVE;

public class PolyvCloudClassHomeActivity extends PolyvBaseActivity
        implements IPolyvHomeProtocol {

    // <editor-fold defaultstate="collapsed" desc="成员变量">

    //聊天室管理类
    private PolyvChatManager chatManager = PolyvChatManager.getInstance();
    private PolyvChatGroupFragment chatGroupFragment;
    private PolyvChatPrivateFragment chatPrivateFragment;
    private HashMap<Fragment, RelativeLayout> fagmentTapMap = new HashMap<Fragment, RelativeLayout>();
    private LinearLayout chatTopSelectLayout;
    private int chatTopSelectLayoutHeight;
    private LinearLayout chatContainerLayout;
    private RelativeLayout personalChatItemLayout;
    private RelativeLayout groupChatItemLayout;
    private RelativeLayout liveInfoChatItemLayout;
    private QBadgeView personalChatBadgeView, groupChatBadgeView;
    private FrameLayout playerContainer, imageViewerContainer, chatEditContainer;
    private PolyvSimpleViewPager chatViewPager;
    private PolyvChatFragmentAdapter chatPagerAdapter;
    private View lastSelectTabItem;
    private PolyvChatPullLayout chatPullIcon;
    private boolean isChatBottom, fullScreenDown;//聊天室是否已经在底部
    private float chatMoveY;//聊天室移动到底部得距离

    //聊天回放相关
    private PolyvChatPlaybackFragment chatPlaybackFragment;

    //直播与点播辅助类
    private PolyvCloudClassVideoHelper livePlayerHelper;
    private PolyvPlaybackVideoHelper playbackVideoHelper;

    private PolyvCloudClassVideoItem cloudClassVideoItem;

    private String userId, channelId, videoId;

    private PolyvTouchContainerView videoPptContainer;

    //答题相关
    private PolyvAnswerView answerView;
    private ViewGroup answerContainer;

    //连麦相关
//    private ViewGroup linkMicLayout;
//    private IPolyvRotateBaseView linkMicLayoutParent;
    private ViewStub linkMicStub;
    private ViewGroup linkMicStubView;
    private PolyvLinkMicParent linkMicParent;

    //参数传递相关标签
    private static final String TAG = "PolyvCloudClassHomeActivity";
    private static final String CHANNELID_KEY = "channelid";
    private static final String USERID_KEY = "userid";
    private static final String VIDEOID_KEY = "videoid";
    private static final String PLAY_TYPE_KEY = "playtype";
    private static final String NORMALLIVE = "normallive";
    private static final String SUPPORT_RTC = "supportrtc";
    private static final String NORMALLIVE_PLAYBACK = "normallive_playback";
    private static final String DEFAULT_NICKNAME = "POLYV";
    private static final String EXTRA_IS_PARTICIPANT = "is_participant";
    private static final String VIDEO_LISTTYPE = "video_listtype";

    //直播与点播类型选择

    @PolyvPlayOption.PlayMode
    private int playMode;

    //手机重力感应相关
    private RotationObserver rotationObserver;
    private PolyvOrientoinListener orientoinListener;

    //是否是普通直播  是否直播回放
    private boolean isNormalLive, isNormalLivePlayBack;

    //观众id
    private String viewerId;
    //观众昵称
    private String viewerName;

    //教师信息栏
    private PolyvTeacherInfoLayout teacherInfoLayout;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //是否是参与者
    private boolean isParticipant;

    //回放视频所在列表的类型
    private int videoListType;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="入口">
    public static void startActivityForLive(Activity activity, String channelId, String userId, boolean isNormalLive) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(NORMALLIVE, isNormalLive);
        intent.putExtra(PLAY_TYPE_KEY, PLAYMODE_LIVE);
        activity.startActivity(intent);
    }


    //从VClass进入
    public static void startActivityForLiveFromVClass(Activity activity, String channelId, String userId, boolean isNormalLive, boolean isParticipant) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(NORMALLIVE, isNormalLive);
        intent.putExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_LIVE);
        intent.putExtra(EXTRA_IS_PARTICIPANT, isParticipant);
        activity.startActivity(intent);
    }

    //带有参与者标志位的直播入口
    public static void startActivityForLiveWithParticipant(Activity activity, String channelId, String userId, boolean isNormalLive, boolean isParticipant) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(NORMALLIVE, isNormalLive);
        intent.putExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_LIVE);
        intent.putExtra(EXTRA_IS_PARTICIPANT, isParticipant);
        activity.startActivity(intent);
    }

    public static void startActivityForPlayBack(Activity activity, String videoId, String channelId, String userId, boolean isNormalLivePlayBack, int videoListType) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(VIDEOID_KEY, videoId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(NORMALLIVE_PLAYBACK, isNormalLivePlayBack);
        intent.putExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_VOD);
        intent.putExtra(VIDEO_LISTTYPE, videoListType);
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

        setContentView(R.layout.polyv_activity_cloudclass_home);

        initialStudentIdAndNickName();

        initial();

        //获取直播后台的功能开关
        requestLiveClassDetailApi();

        if (playMode == PLAYMODE_LIVE) {
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
        if (playbackVideoHelper != null) {
            playbackVideoHelper.resume();
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
        if (playbackVideoHelper != null) {
            playbackVideoHelper.pause();
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
        if (playbackVideoHelper != null) {
            playbackVideoHelper.destory();
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

        if (teacherInfoLayout != null) {
            teacherInfoLayout.onDestroy();
        }

        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }

        PolyvLinkMicWrapper.getInstance().destroy(linkMicStubView);
        PolyvSingleRelayBus.clear();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initialStudentIdAndNickName() {
        //初始化观众id和观众昵称，用于统计数据
        if (isParticipant) {
            viewerId = PolyvVClassGlobalConfig.viewerId;
            viewerName = PolyvVClassGlobalConfig.username;
        } else {
            viewerId = "" + Build.SERIAL;
            viewerName = "学员" + viewerId;
        }

        PolyvVClassGlobalConfig.userId = userId;
    }

    private void initial() {
        float ratio = 9.0f / 16;//播放器使用16:9比例
        PolyvScreenUtils.generateHeightByRatio(this, ratio);

        initCommonView();
        initialLinkMic();
        initialChatRoom();
        initialPPT();
        initialAnswer();
        initialVideo();

        initialOretation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
    }

    private void initCommonView() {
        teacherInfoLayout = findView(R.id.teacher_info_layout);
    }

    private void initialTeacherInfo() {

        if (playMode == PLAYMODE_LIVE) {
            teacherInfoLayout.init(livePlayerHelper, videoPptContainer);
        } else {
            teacherInfoLayout.setVisibility(View.GONE);
        }

        teacherInfoLayout.hideHandsUpLink(isParticipant);
    }

    private void initialParams() {
        Intent intent = getIntent();
        channelId = intent.getStringExtra(CHANNELID_KEY);
        userId = intent.getStringExtra(USERID_KEY);
        videoId = intent.getStringExtra(VIDEOID_KEY);
        isNormalLive = intent.getBooleanExtra(NORMALLIVE, true);
        isNormalLivePlayBack = intent.getBooleanExtra(NORMALLIVE_PLAYBACK, true);
        playMode = intent.getIntExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_VOD);
        isParticipant = intent.getBooleanExtra(EXTRA_IS_PARTICIPANT, false);
        videoListType = intent.getIntExtra(VIDEO_LISTTYPE, PolyvPlaybackListType.PLAYBACK);
    }

    private void initialLinkMic() {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            return;
        }

        linkMicParent = new PolyvLinkMicParent();
        if (isNormalLive) {
            linkMicStub = findViewById(R.id.polyv_normal_live_link_mic_stub);
        } else {
            linkMicStub = findViewById(R.id.polyv_link_mic_stub);
        }
        if (linkMicStubView == null) {
            linkMicStubView = (ViewGroup) linkMicStub.inflate();
        }

        linkMicParent.initView(linkMicStubView, isParticipant, teacherInfoLayout);

    }

    private void initialChatRoom() {
        chatPullIcon = findView(R.id.chat_top_pull);
        imageViewerContainer = findViewById(R.id.image_viewer_container);
        chatEditContainer = findViewById(R.id.chat_edit_container);
        chatTopSelectLayout = findViewById(R.id.chat_top_select_layout);
        //由于聊天tab默认隐藏，这里使用布局里定义的高度
        chatTopSelectLayoutHeight = ConvertUtils.dp2px(48);
        chatContainerLayout = findViewById(R.id.chat_container_layout);
        chatViewPager = findViewById(R.id.chat_viewpager);

//        if (playMode == PolyvPlayOption.PLAYMODE_LIVE) {//直播
        chatContainerLayout.setVisibility(View.VISIBLE);
        //test
        chatPullIcon.setChatPullLayoutCallback(new PolyvChatPullLayout.ChatPullLayoutCallback() {
            @Override
            public void pullUp() {
                if (livePlayerHelper != null && livePlayerHelper.isJoinLinkMick()) {
                    upChatLayout(chatContainerLayout);
                }
            }

            @Override
            public void pullDown() {
                if (livePlayerHelper != null && livePlayerHelper.isJoinLinkMick()) {
                    downChatLayout(chatContainerLayout);
                }
            }
        });

        List<Fragment> fragments = new ArrayList<>();
        chatPagerAdapter = new PolyvChatFragmentAdapter(getSupportFragmentManager(), fragments);
        chatViewPager.setAdapter(chatPagerAdapter);
        chatViewPager.setPageMargin(ConvertUtils.dp2px(10));
        chatViewPager.setOffscreenPageLimit(5);
        chatViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (lastSelectTabItem != null) {
                    lastSelectTabItem.setSelected(false);
                    lastSelectTabItem = chatTopSelectLayout.getChildAt(position);
                    if (lastSelectTabItem != null) {
                        lastSelectTabItem.setSelected(true);
                    }

                    if (chatPrivateFragment != null) {
                        personalChatBadgeView.setBadgeNumber(lastSelectTabItem == personalChatItemLayout ? 0 : chatPrivateFragment.getChatListUnreadCount());
                    }
                    if (groupChatBadgeView != null) {
                        groupChatBadgeView.setBadgeNumber(lastSelectTabItem == groupChatItemLayout ? 0 : chatGroupFragment.getChatListUnreadCount());
                    }
                    scrollToVisibleTab(lastSelectTabItem);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        chatContainerLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
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
//        }
    }

    private void initialPPT() {
        videoPptContainer = findViewById(R.id.video_ppt_container);

        videoPptContainer.setOriginLeft(ScreenUtils.getScreenWidth() - PolyvScreenUtils.dip2px
                (PolyvCloudClassHomeActivity.this, 144));

        videoPptContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams rlp = getLayoutParamsLayout(videoPptContainer);
                if (rlp == null) {
                    return;
                }

                boolean isLive = playMode == PLAYMODE_LIVE;
                rlp.leftMargin = isLive ? 0 :
                        ((View) videoPptContainer.getParent()).getMeasuredWidth() - videoPptContainer.getMeasuredWidth();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rlp.topMargin = 0;
                    videoPptContainer.setContainerMove(true);
                } else { // 若初始为竖屏
                    rlp.topMargin = playerContainer.getBottom();
                    videoPptContainer.setContainerMove(!isLive);
                }
                videoPptContainer.setOriginTop(rlp.topMargin);

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
        answerView.setViewerId(viewerId);
        answerView.setAnswerJsCallback(new PolyvAnswerWebView.AnswerJsCallback() {
            @Override
            public void callOnHasAnswer(PolyvJSQuestionVO polyvJSQuestionVO) {
                PolyvCommonLog.d(TAG, "send to server has choose answer");
                if (chatManager != null) {
                    PolyvQuestionSocketVO socketVO = new PolyvQuestionSocketVO
                            (polyvJSQuestionVO.getAnswerId(), viewerName, polyvJSQuestionVO.getQuestionId(),
                                    channelId, chatManager.userId);
                    chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, socketVO);
                }
            }

            @Override
            public void callOnHasQuestionnaireAnswer(PolyvQuestionnaireSocketVO polyvQuestionnaireSocketVO) {
                PolyvCommonLog.d(TAG, "发送调查问卷答案");
                polyvQuestionnaireSocketVO.setNick(viewerName);
                polyvQuestionnaireSocketVO.setRoomId(channelId);
                polyvQuestionnaireSocketVO.setUserId(chatManager.userId);
                chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, polyvQuestionnaireSocketVO);
            }

            @Override
            public void callOnSignIn(PolyvSignIn2SocketVO socketVO) {
                socketVO.setUser(new PolyvSignIn2SocketVO.UserBean(viewerName, viewerId));
                chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, socketVO);
            }

            @Override
            public void callOnLotteryWin(String lotteryId, String winnerCode, String viewerId, String telephone, String realName, String address) {
                PolyvResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvApichatApi()
                                .postLotteryWinnerInfo(channelId, lotteryId, winnerCode, PolyvCloudClassHomeActivity.this.viewerId, realName, telephone, address),
                        String.class, new PolyvrResponseCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                LogUtils.d("抽奖信息上传成功" + s);
                            }

                            @Override
                            public void onFailure(PolyvResponseBean<String> responseBean) {
                                super.onFailure(responseBean);
                                LogUtils.e("抽奖信息上传失败" + responseBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                LogUtils.e("抽奖信息上传失败");
                                if (e instanceof HttpException) {
                                    try {
                                        ResponseBody errorBody = ((HttpException) e).response().errorBody();
                                        if (errorBody != null) {
                                            LogUtils.e(errorBody.string());
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
            }

            @Override
            public void callOnLotteryWinNew(String lotteryId, String winnerCode, String viewerId,
                                            String receiveInfo, String seesionId) {
                PolyvResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvApichatApi()
                                .postLotteryWinnerInfoNew(channelId, lotteryId, winnerCode, PolyvCloudClassHomeActivity.this.viewerId, receiveInfo, seesionId),
                        String.class, new PolyvrResponseCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                LogUtils.d("抽奖信息上传成功" + s);
                            }

                            @Override
                            public void onFailure(PolyvResponseBean<String> responseBean) {
                                super.onFailure(responseBean);
                                LogUtils.e("抽奖信息上传失败" + responseBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                LogUtils.e("抽奖信息上传失败");
                                if (e instanceof HttpException) {
                                    try {
                                        ResponseBody errorBody = ((HttpException) e).response().errorBody();
                                        if (errorBody != null) {
                                            LogUtils.e(errorBody.string());
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
            }


            @Override
            public void callOnAbandonLottery() {
                PolyvResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvApichatApi()
                                .postLotteryAbandon(channelId, viewerId), String.class,
                        new PolyvrResponseCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                LogUtils.d("放弃领奖信息上传成功 " + s);
                            }

                            @Override
                            public void onFailure(PolyvResponseBean<String> responseBean) {
                                super.onFailure(responseBean);
                                LogUtils.d("放弃领奖信息上传失败 " + responseBean);
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                LogUtils.e("放弃领奖信息上传失败");
                                if (e instanceof HttpException) {
                                    try {
                                        ResponseBody errorBody = ((HttpException) e).response().errorBody();
                                        if (errorBody != null) {
                                            LogUtils.e(errorBody.string());
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        });
    }

    private void initialVideo() {
        PolyvCommonLog.d(TAG, "initialVodVideo");

        // 播放器
        playerContainer = findViewById(R.id.player_container);
        ViewGroup.LayoutParams vlp = playerContainer.getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = PolyvScreenUtils.getHeight();

        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            initialPlaybackVideo();
        } else {
            initialLiveVideo();
        }
        initialTeacherInfo();
    }

    private void initialOretation() {
        //创建观察类对象
        rotationObserver = new RotationObserver(new Handler());
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            orientoinListener = new PolyvOrientoinListener(this, playbackVideoHelper);
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

    private void initialPlaybackVideo() {
        PolyvPlaybackVideoItem playbackVideoItem = new PolyvPlaybackVideoItem(this);
        playbackVideoHelper = new PolyvPlaybackVideoHelper(playbackVideoItem,
                isNormalLivePlayBack ? null : new PolyvPPTItem<PolyvCloudClassMediaController>(this));
        playbackVideoHelper.addVideoPlayer(playerContainer);
        playbackVideoHelper.initConfig(isNormalLivePlayBack);
        playbackVideoHelper.addPPT(videoPptContainer);

        playbackVideoHelper.setNickName(viewerName);

        playPlaybackVideo();
    }

    private void playPlaybackVideo() {
        playbackVideoHelper.resetView(isNormalLivePlayBack);

        // TODO: 2018/9/12 videoId 为直播平台的 videopoolid为点播平台的视频id
        PolyvPlaybackVideoParams playbackVideoParams = new PolyvPlaybackVideoParams(videoId,//videoId
                channelId,
                userId, viewerId//viewerid
        );
        playbackVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(PolyvBaseVideoParams.IS_PPT_PLAY, true)
                // TODO: 2019/3/25 请在此处填入用户的昵称
                .buildOptions(PolyvBaseVideoParams.PARAMS2, viewerName)
                .buildOptions(PolyvPlaybackVideoParams.ENABLE_ACCURATE_SEEK, true)
                .buildOptions(PolyvPlaybackVideoParams.VIDEO_LISTTYPE, videoListType);
        playbackVideoHelper.startPlay(playbackVideoParams);
    }

    private void initialLiveVideo() {
        cloudClassVideoItem = new PolyvCloudClassVideoItem(this);
        cloudClassVideoItem.setOnSendDanmuListener(new IPolyvLandscapeDanmuSender.OnSendDanmuListener() {
            @Override
            public void onSendDanmu(String danmuMessage) {
                if (chatGroupFragment != null) {
                    chatGroupFragment.sendChatMessageByDanmu(danmuMessage);
                }
            }
        });

        livePlayerHelper = new PolyvCloudClassVideoHelper(cloudClassVideoItem,
                isNormalLive ? null : new PolyvPPTItem<PolyvCloudClassMediaController>(this), chatManager, channelId);
        livePlayerHelper.addVideoPlayer(playerContainer);
        livePlayerHelper.initConfig(isNormalLive);
        livePlayerHelper.addPPT(videoPptContainer);

        livePlayerHelper.addLinkMicLayout(linkMicParent);

        PolyvCloudClassVideoParams cloudClassVideoParams = new PolyvCloudClassVideoParams(channelId, userId
                , viewerId// viewerid
        );
        cloudClassVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                // TODO: 2019/3/25 请在此处填入用户的昵称
                .buildOptions(PolyvBaseVideoParams.PARAMS2, viewerName);
        livePlayerHelper.startPlay(cloudClassVideoParams);

        livePlayerHelper.addHomeProtocol(this);

        if (linkMicParent != null) {
            linkMicParent.addClassHelper(livePlayerHelper);
        }

        if (isParticipant) {
            livePlayerHelper.joinLink(true);
        }
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
    }

    private void moveSubVideo() {
        if (videoPptContainer != null) {
            videoPptContainer.topSubviewTo(chatContainerLayout.getTop());
        }
    }

    private void removeView() {
        playerContainer.removeAllViews();
        videoPptContainer.removeAllViews();
        livePlayerHelper = null;
        playbackVideoHelper = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="动画">

    private void downChatLayout(final View animationView) {
        if (isChatBottom) {
            return;
        }
        teacherInfoLayout.post(new Runnable() {
            @Override
            public void run() {
                chatMoveY = animationView.getBottom() - animationView.getTop() - chatTopSelectLayout.getHeight() -
                        DisplayUtil.dp2px(PolyvCloudClassHomeActivity.this, 32);
                if (chatMoveY < 0) {
                    fullScreenDown = true;
                    return;
                }
                ObjectAnimator animator = ObjectAnimator.ofFloat(animationView, "translationY", 0, chatMoveY);
                animator.setDuration(1000);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isChatBottom = true;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        });

    }

    private void upChatLayout(final View animationView) {
        if (!isChatBottom) {
            return;
        }
        teacherInfoLayout.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animator = ObjectAnimator.ofFloat
                        (animationView, "translationY", chatMoveY, 0);
                animator.setDuration(1000);
                animator.start();
                isChatBottom = false;
            }
        });

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
                answerView.onBackPress();
                return true;
            }
            if (PolyvScreenUtils.isLandscape(this)) {
                if (livePlayerHelper != null) {
                    livePlayerHelper.changeToPortrait();
                }
                if (playbackVideoHelper != null) {
                    playbackVideoHelper.changeToPortrait();
                }
                return true;
            } else if (chatPagerAdapter != null && chatPagerAdapter.getCount() > 1) {
                Fragment fragment = chatPagerAdapter.getItem(chatViewPager.getCurrentItem());
                if (fragment instanceof PolyvChatBaseFragment) {
                    if (((PolyvChatBaseFragment) fragment).onBackPressed())
                        return true;
                } else if (fragment instanceof PolyvCustomMenuFragment) {
                    if (((PolyvCustomMenuFragment) fragment).goBack())
                        return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室初始化">
    private void requestLiveClassDetailApi() {
        disposables.add(PolyvChatApiRequestHelper.getInstance()
                .requestLiveClassDetailApi(channelId)
                .subscribe(new Consumer<PolyvLiveClassDetailVO>() {
                    @Override
                    public void accept(PolyvLiveClassDetailVO polyvLiveClassDetailVO) throws Exception {
                        boolean isLive = polyvLiveClassDetailVO.getData().getWatchStatus().equals("live");
                        if (!isLive) {
                            Object startTime = polyvLiveClassDetailVO.getData().getStartTime();
                            //startTime==null表示没设置直播开始时间
                            if (startTime != null && cloudClassVideoItem != null) {
                                cloudClassVideoItem.startLiveTimeCountDown(startTime.toString());
                            }
                        }

                        for (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean : polyvLiveClassDetailVO.getData().getChannelMenus()) {
                            if (PolyvLiveClassDetailVO.MENUTYPE_DESC.equals(channelMenusBean.getMenuType())) {
                                setupLiveInfoFragment(polyvLiveClassDetailVO, channelMenusBean);
                            } else if (PolyvLiveClassDetailVO.MENUTYPE_CHAT.equals(channelMenusBean.getMenuType())) {
                                setupChatGroupFragment(channelMenusBean);
                            } else if (PolyvLiveClassDetailVO.MENUTYPE_QUIZ.equals(channelMenusBean.getMenuType())) {
                                setupChatPrivateFragment(channelMenusBean);
                            } else if (PolyvLiveClassDetailVO.MENUTYPE_TEXT.equals(channelMenusBean.getMenuType())) {
                                setupCustomMenuFragment(channelMenusBean, false);
                            } else if (PolyvLiveClassDetailVO.MENUTYPE_IFRAME.equals(channelMenusBean.getMenuType())) {
                                setupCustomMenuFragment(channelMenusBean, true);
                            } else if (PolyvLiveClassDetailVO.MENUTYPE_TUWEN.equals(channelMenusBean.getMenuType())) {
                                setupTuWenMenuFragment(channelMenusBean);
                            }
                        }

                        setupChatPlaybackFragment();
                        refreshChatPagerAdapter();

                        if (isParticipant && polyvLiveClassDetailVO.isViewerSignalEnabled() && linkMicParent != null) {
                            linkMicParent.showLookAtMeView();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //获取失败
                        setupChatPlaybackFragment();
                        refreshChatPagerAdapter();
                    }
                }));
    }

    private void setupTuWenMenuFragment(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {//直播
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("channelId", channelId);
        PolyvTuWenMenuFragment tuWenMenuFragment = new PolyvTuWenMenuFragment();
        tuWenMenuFragment.setArguments(bundle);

        // 把 tuWenMenuFragment 对象添加到 chatPagerAdapter
        chatPagerAdapter.add(tuWenMenuFragment);
        // 创建 customMenuFragment 对象对应的 tabbar item
        int index = chatPagerAdapter.getCount() - 1;
        String title = channelMenusBean.getName();
        if (TextUtils.isEmpty(title)) {
            title = PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_tuwen_menu_text_default);
        }
        RelativeLayout tuWenMenuChatItemLayout = addTabItemView(index, title, chatTopSelectLayout);

        fagmentTapMap.put(tuWenMenuFragment, tuWenMenuChatItemLayout);
    }

    private void setupCustomMenuFragment(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean, boolean isIFrameMenu) {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {//直播
            return;
        }
        Bundle bundle = new Bundle();
        if (!isIFrameMenu) {
            bundle.putString("text", channelMenusBean.getContent());
        } else {
            bundle.putString("url", channelMenusBean.getContent());
        }
        bundle.putBoolean("isIFrameMenu", isIFrameMenu);
        PolyvCustomMenuFragment customMenuFragment = new PolyvCustomMenuFragment();
        customMenuFragment.setArguments(bundle);

        // 把 customMenuFragment 对象添加到 chatPagerAdapter
        chatPagerAdapter.add(customMenuFragment);

        // 创建 customMenuFragment 对象对应的 tabbar item
        int index = chatPagerAdapter.getCount() - 1;
        String title = channelMenusBean.getName();
        if (TextUtils.isEmpty(title)) {
            title = !isIFrameMenu ? PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_custom_menu_text_default) :
                    PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_iframe_menu_text_default);
        }
        RelativeLayout customMenuChatItemLayout = addTabItemView(index, title, chatTopSelectLayout);

        fagmentTapMap.put(customMenuFragment, customMenuChatItemLayout);
    }

    private void setupLiveInfoFragment(PolyvLiveClassDetailVO polyvLiveClassDetailVO, PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {//直播
            return;
        }
        // 创建 liveInfoFragment 对象
        Bundle bundle = new Bundle();
        bundle.putSerializable(PolyvLiveInfoFragment.ARGUMENT_CLASS_DETAIL, polyvLiveClassDetailVO);
        bundle.putSerializable(PolyvLiveInfoFragment.ARGUMENT_CLASS_DETAIL_ITEM, channelMenusBean);
        bundle.putString(PolyvLiveInfoFragment.ARGUMENT_VIEWER_ID, viewerId);
        bundle.putInt(PolyvLiveInfoFragment.ARGUMENT_PLAY_MODE, playMode);
        PolyvLiveInfoFragment liveInfoFragment = new PolyvLiveInfoFragment();
        liveInfoFragment.setArguments(bundle);

        // 把 liveInfoFragment 对象添加到 chatPagerAdapter
        chatPagerAdapter.add(liveInfoFragment);

        // 创建 liveInfoFragment 对象对应的 tabbar item
        int index = chatPagerAdapter.getCount() - 1;
        String title = channelMenusBean.getName();
        if (TextUtils.isEmpty(title)) {
            title = PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_live_info_text_default);
        }
        liveInfoChatItemLayout = addTabItemView(index, title, chatTopSelectLayout);

        fagmentTapMap.put(liveInfoFragment, liveInfoChatItemLayout);
    }

    private void setupChatPlaybackFragment() {
        if (playMode != PolyvPlayOption.PLAYMODE_VOD) {//回放，只保留回放聊天tab
            return;
        }
        // 创建 chatPlaybackFragment 对象
        Bundle bundle = new Bundle();
        bundle.putString("videoId", videoId);
        chatPlaybackFragment = new PolyvChatPlaybackFragment();
        chatPlaybackFragment.setArguments(bundle);

        // 设置学员信息给 chatPlaybackFragment
        chatPlaybackFragment.setViewerInfo(viewerId, channelId, viewerName, null, null);

        // 把 chatPlaybackFragment 对象添加到 chatPagerAdapter
        chatPagerAdapter.add(chatPlaybackFragment);

        // 创建 chatPlaybackFragment 对象对应的 tabbar item
        int index = chatPagerAdapter.getCount() - 1;
        String title = PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_group_chat_text_default);
        RelativeLayout playbackChatItemLayout = addTabItemView(index, title, chatTopSelectLayout);

        fagmentTapMap.put(chatPlaybackFragment, playbackChatItemLayout);
    }

    private void setupChatGroupFragment(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {//直播
            return;
        }
        // 创建 chatGroupFragment 对象
        chatGroupFragment = new PolyvChatGroupFragment();
        chatGroupFragment.setNormalLive(isNormalLive);

        // 把 chatGroupFragment 对象添加到 chatPagerAdapter
        chatPagerAdapter.add(chatGroupFragment);

        // 创建 chatGroupFragment 对象对应的 tabbar item
        int index = chatPagerAdapter.getCount() - 1;
        String title = channelMenusBean.getName();
        if (TextUtils.isEmpty(title)) {
            title = PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_group_chat_text_default);
        }
        groupChatItemLayout = addTabItemView(index, title, chatTopSelectLayout);

        TextView groupChatItemTv = (TextView) groupChatItemLayout.findViewById(R.id.tv_live_chat_item);
        groupChatBadgeView = new QBadgeView(PolyvCloudClassHomeActivity.this);
        groupChatBadgeView.bindTarget(groupChatItemTv).setBadgeGravity(Gravity.END | Gravity.TOP);

        fagmentTapMap.put(chatGroupFragment, groupChatItemLayout);
    }

    private void setupChatPrivateFragment(PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean) {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {//直播
            return;
        }
        // 创建 chatPrivateFragment 对象
        chatPrivateFragment = new PolyvChatPrivateFragment();

        // 把 chatPrivateFragment 对象添加到 chatPagerAdapter
        chatPagerAdapter.add(chatPrivateFragment);

        // 创建 chatPrivateFragment 对象对应的 tabbar item
        int index = chatPagerAdapter.getCount() - 1;
        String title = channelMenusBean.getName();
        if (TextUtils.isEmpty(title)) {
            title = PolyvCloudClassHomeActivity.this.getString(R.string.chat_tab_personal_chat_text_default);
        }
        personalChatItemLayout = addTabItemView(index, title, chatTopSelectLayout);

        TextView personalChatItemTv = (TextView) personalChatItemLayout.findViewById(R.id.tv_live_chat_item);
        personalChatBadgeView = new QBadgeView(PolyvCloudClassHomeActivity.this);
        personalChatBadgeView.bindTarget(personalChatItemTv).setBadgeGravity(Gravity.END | Gravity.TOP);

        fagmentTapMap.put(chatPrivateFragment, personalChatItemLayout);
    }

    private RelativeLayout addTabItemView(int index, String title, LinearLayout parent) {
        RelativeLayout rl = (RelativeLayout) View.inflate(PolyvCloudClassHomeActivity.this, R.layout.polyv_chat_tab_item_layout, null);
        rl.setOnClickListener(tabItemOnClickListener);
        rl.setTag(new Integer(index));

        TextView personalChatItemTv = (TextView) rl.findViewById(R.id.tv_live_chat_item);
        personalChatItemTv.setText(title);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        parent.addView(rl, lp);

        return rl;
    }

    private View.OnClickListener tabItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Integer index = Integer.valueOf(v.getTag().toString());
                chatViewPager.setCurrentItem(index);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    };

    private void scrollToVisibleTab(View v) {
        if (v.getParent().getParent() instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v.getParent().getParent();
            int[] location = new int[2];
            v.getLocationInWindow(location);
            if (location[0] < 0) {
                vg.scrollTo(0, 0);
            } else if (location[0] + v.getWidth() > ScreenUtils.getScreenWidth()) {
                vg.scrollTo(((ViewGroup) v.getParent()).getChildAt(((ViewGroup) v.getParent()).getChildCount() - 1).getRight(), 0);
            }
        }
    }

    private void refreshChatPagerAdapter() {
        if (chatPagerAdapter.getCount() > 0) {
            chatTopSelectLayout.setVisibility(View.VISIBLE);
            chatPagerAdapter.notifyDataSetChanged();
            chatViewPager.setCurrentItem(0);

            Fragment fragment = chatPagerAdapter.getItem(0);
            RelativeLayout rl = fagmentTapMap.get(fragment);
            if (rl != null) {
                rl.setSelected(true);
                lastSelectTabItem = rl;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室登录">
    private void loginChatRoom() {
        PolyvSingleRelayBus.clear();
        chatManager.setAccountId(userId);
        //先添加监听器再登录
        chatManager.addConnectStatusListener(new PolyvConnectStatusListener() {
            @Override
            public void onConnectStatusChange(int status, @Nullable Throwable t) {
                //由于登录成功后，Fragment可能还没初始化完成，所以这里使用Rxjava的ReplayRelay把信息保存下来，在Fragment初始化完成那里获取
//                PolyvChatEventBus.get().post(new PolyvChatBaseFragment.ConnectStatus(status, t));
                PolyvSingleRelayBus.get().post(new PolyvChatBaseFragment.ConnectStatus(status, t));
            }
        });
        chatManager.addNewMessageListener(new PolyvNewMessageListener2() { // 用于监听登录的相关消息
            @Override
            public void onNewMessage(String message, String event, String socketListen) {
                //由于登录成功后，Fragment可能还没初始化完成，所以这里使用Rxjava的ReplayRelay把信息保存下来，在Fragment初始化完成那里获取
//                PolyvChatEventBus.get().post(new PolyvChatBaseFragment.EventMessage(message, event, socketListen));
                PolyvRxBus.get().post(new PolyvChatBaseFragment.EventMessage(message, event, socketListen));
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
                        livePlayerHelper.updateMainScreenStatus(message, event);
                        if (answerView != null) {
                            answerView.processSocketMessage(new PolyvSocketMessageVO(message, event), event);
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

        //设置字体聊天室字体颜色
//        PolyvChatUIConfig.FontColor.set(PolyvChatUIConfig.FontColor.USER_ASSISTANT, Color.BLUE);
//        PolyvChatUIConfig.FontColor.set(PolyvChatUIConfig.FontColor.USER_MANAGER,Color.BLUE);
//        PolyvChatUIConfig.FontColor.set(PolyvChatUIConfig.FontColor.USER_TEACHER,Color.BLUE);

        //TODO 登录聊天室(userId：学员的Id(聊天室用户的唯一标识，非直播后台的userId，不同的学员应当使用不同的userId)，roomId：频道号，nickName：学员的昵称)
        //TODO 登录聊天室后一些功能才可以正常使用，例如：连麦

        //根据直播类型(普通直播、三分屏直播)设置聊天室的学员的类型
        chatManager.userType = isNormalLive ? PolyvChatManager.USERTYPE_STUDENT : PolyvChatManager.USERTYPE_SLICE;
        if (isParticipant) {
            chatManager.userType = USERTYPE_VIEWER;
        }
        chatManager.login(viewerId, channelId, viewerName);

        if (livePlayerHelper != null) {
            livePlayerHelper.setNickName(viewerName);
        }
        if (playbackVideoHelper != null) {
            playbackVideoHelper.setNickName(viewerName);
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (livePlayerHelper != null) {
            livePlayerHelper.notifyOnConfigChangedListener(newConfig);
            videoPptContainer.setContainerMove(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
        }

        //解决由于在全屏下连麦 动画效果异常 返回竖屏后需要正常显示连麦区域
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            compositeDisposable.add(PolyvRxTimer.delay(2000, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    if (fullScreenDown && livePlayerHelper.isJoinLinkMick()) {//如果全屏下 聊天室没有置底
                        downChatLayout(chatContainerLayout);
                        fullScreenDown = false;
                    }
                }
            }));
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPolyvHomePresnter实现">
    @Override
    public String getSessionId() {
        if (getVideoView() instanceof PolyvCloudClassVideoView && getVideoView().getModleVO() != null) {
            return ((PolyvCloudClassVideoView) getVideoView()).getModleVO().getChannelSessionId();
        } else if (getVideoView() instanceof PolyvPlaybackVideoView && getVideoView().getModleVO() != null) {
            return ((PolyvPlaybackVideoView) getVideoView()).getModleVO().getChannelSessionId();
        }
        return null;
    }

    @Override
    public PolyvCommonVideoView getVideoView() {
        if (playMode == PolyvPlayOption.PLAYMODE_LIVE && livePlayerHelper != null) {
            return livePlayerHelper.getVideoView();
        } else if (playMode == PolyvPlayOption.PLAYMODE_VOD && playbackVideoHelper != null) {
            return playbackVideoHelper.getVideoView();
        }
        return null;
    }

    @Override
    public void sendDanmu(CharSequence content) {
        if (livePlayerHelper != null) {
            livePlayerHelper.sendDanmuMessage(content);
        }
    }

    @Override
    public void addUnreadQuiz(int unreadCount) {
        if (personalChatBadgeView != null) {
            personalChatBadgeView.setBadgeNumber(personalChatBadgeView.getBadgeNumber() + unreadCount);
        }
    }

    @Override
    public boolean isSelectedQuiz() {
        return lastSelectTabItem != null && lastSelectTabItem == personalChatItemLayout;
    }

    @Override
    public void addUnreadChat(int unreadCount) {
        if (groupChatBadgeView != null) {
            groupChatBadgeView.setBadgeNumber(groupChatBadgeView.getBadgeNumber() + unreadCount);
        }
    }

    @Override
    public boolean isSelectedChat() {
        return lastSelectTabItem != null && lastSelectTabItem == groupChatItemLayout;
    }

    @Override
    public ViewGroup getImageViewerContainer() {
        return imageViewerContainer;
    }

    @Override
    public ViewGroup getChatEditContainer() {
        return chatEditContainer;
    }

    @Override
    public PolyvChatManager getChatManager() {
        return chatManager;
    }

    @Override
    public void moveChatLocation(boolean downChat) {

        if (teacherInfoLayout != null) {
            teacherInfoLayout.setVisibility(downChat ? View.GONE : (livePlayerHelper.getVideoView().isOnline() ? View.VISIBLE : View.GONE));
        }

        if (downChat) {
            downChatLayout(chatContainerLayout);
        } else {
            upChatLayout(chatContainerLayout);
        }
    }

    @Override
    public void updatePaintStatus(boolean showPaint) {
        if (linkMicParent != null) {
            linkMicParent.hideBrushColor(showPaint);
        }
    }
    // </editor-fold>

}
