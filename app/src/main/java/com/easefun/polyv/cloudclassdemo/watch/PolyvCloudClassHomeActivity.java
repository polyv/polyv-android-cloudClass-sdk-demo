package com.easefun.polyv.cloudclassdemo.watch;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;
import com.easefun.polyv.businesssdk.model.video.PolyvBaseVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvCloudClassVideoParams;
import com.easefun.polyv.businesssdk.model.video.PolyvPlaybackVideoParams;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvConnectStatusListener;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener;
import com.easefun.polyv.cloudclass.chat.PolyvNewMessageListener2;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvJSQuestionVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionSocketVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionnaireSocketVO;
import com.easefun.polyv.cloudclass.model.sign_in.PolyvSignIn2SocketVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclass.video.PolyvAnswerWebView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatBaseFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatFragmentAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatGroupFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatPrivateFragment;
import com.easefun.polyv.cloudclassdemo.watch.linkMic.widget.IPolyvRotateBaseView;
import com.easefun.polyv.cloudclassdemo.watch.player.PolyvOrientoinListener;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassMediaController;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoHelper;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoItem;
import com.easefun.polyv.cloudclassdemo.watch.player.playback.PolyvPlaybackVideoHelper;
import com.easefun.polyv.cloudclassdemo.watch.player.playback.PolyvPlaybackVideoItem;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;
import com.easefun.polyv.commonui.player.widget.PolyvSlideSwitchView;
import com.easefun.polyv.commonui.utils.PolyvChatEventBus;
import com.easefun.polyv.commonui.widget.PolyvAnswerView;
import com.easefun.polyv.commonui.widget.PolyvTouchContainerView;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.linkmic.PolyvLinkMicWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import io.socket.client.Socket;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;

public class PolyvCloudClassHomeActivity extends PolyvBaseActivity
        implements View.OnClickListener, IPolyvHomeProtocol {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private LinearLayout chatTopSelectLayout;
    private LinearLayout chatContainerLayout;
    private RelativeLayout personalChatItemLayout;
    private RelativeLayout groupChatItemLayout;
    private FrameLayout playerContainer, imageViewerContainer, chatEditContainer;
    private ViewPager chatViewPager;
    private PolyvChatFragmentAdapter chatPagerAdapter;
    private View lastView;

    //直播与点播辅助类
    private PolyvCloudClassVideoHelper livePlayerHelper;
    private PolyvPlaybackVideoHelper playbackVideoHelper;
    private PolyvSlideSwitchView polyvSlideSwitch;

    private String userId, channelId, videoId;

    private PolyvTouchContainerView videoPptContainer;
    //聊天室管理类
    private PolyvChatManager chatManager = new PolyvChatManager();
    private PolyvChatGroupFragment polyvChatGroupFragment;

    //答题相关
    private PolyvAnswerView answerView;
    private ViewGroup answerContainer;

    //连麦相关
    private LinearLayout linkMicLayout;
    private IPolyvRotateBaseView linkMicLayoutParent;
    private ViewStub linkMicStub;
    private View linkMicStubView;

    //参数传递相关标签
    private static final String TAG = "PolyvCloudClassHomeActivity";
    private static final String CHANNELID_KEY = "channelid";
    private static final String USERID_KEY = "userid";
    private static final String VIDEOID_KEY = "videoid";
    private static final String PLAY_TYPE_KEY = "playtype";
    private static final String NORMALLIVE = "normallive";
    private static final String NORMALLIVE_PLAYBACK = "normallive_playback";
    private static final String DEFAULT_NICKNAME = "POLYV";

    //直播与点播类型选择

    @PolyvPlayOption.PlayMode
    private int playMode;

    //手机重力感应相关
    private RotationObserver rotationObserver;
    private PolyvOrientoinListener orientoinListener;

    //是否是普通直播  是否直播回放
    private boolean isNormalLive, isNormalLivePlayBack;

    private String studentUserId;
    private String studentNickName;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="入口">
    public static void startActivityForLive(Activity activity, String channelId, String userId, boolean isNormalLive) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(NORMALLIVE, isNormalLive);
        intent.putExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_LIVE);
        activity.startActivity(intent);
    }

    public static void startActivityForPlayBack(Activity activity, String videoId, String channelId, String userId, boolean isNormalLivePlayBack) {
        Intent intent = new Intent(activity, PolyvCloudClassHomeActivity.class);
        intent.putExtra(VIDEOID_KEY, videoId);
        intent.putExtra(USERID_KEY, userId);
        intent.putExtra(CHANNELID_KEY, channelId);
        intent.putExtra(NORMALLIVE_PLAYBACK, isNormalLivePlayBack);
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

        initialStudentIdAndNickName();

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

        PolyvLinkMicWrapper.getInstance().destroy(linkMicLayout);
        PolyvChatEventBus.clear();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initialStudentIdAndNickName() {
        //初始化学员用户id和用户昵称，用于统计数据
        studentUserId = "" + Build.SERIAL;
        studentNickName = "学员" + studentUserId;
    }

    private void initial() {
        float ratio = isNormalLive ? 9.0f / 16 : 3.0f / 4;//普通直播16:9 云课堂4:3
        PolyvScreenUtils.generateHeightByRatio(this, ratio);

        initialLinkMic();
        initialChatRoom();
        intialPpt();
        initialAnswer();
        initialVideo();
        intialOretation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
    }

    private void initialParams() {
        Intent intent = getIntent();
        channelId = intent.getStringExtra(CHANNELID_KEY);
        userId = intent.getStringExtra(USERID_KEY);
        videoId = intent.getStringExtra(VIDEOID_KEY);
        isNormalLive = intent.getBooleanExtra(NORMALLIVE, true);
        isNormalLivePlayBack = intent.getBooleanExtra(NORMALLIVE_PLAYBACK, true);
        playMode = intent.getIntExtra(PLAY_TYPE_KEY, PolyvPlayOption.PLAYMODE_VOD);
    }

    private void initialLinkMic() {
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            return;
        }
        if (isNormalLive) {
            linkMicStub = findViewById(R.id.polyv_normal_live_link_mic_stub);
        } else {
            linkMicStub = findViewById(R.id.polyv_link_mic_stub);
        }
        if (linkMicStubView == null) {
            linkMicStubView = linkMicStub.inflate();
        }
        linkMicLayout = linkMicStubView.findViewById(R.id.link_mic_layout);
        if (linkMicStubView instanceof IPolyvRotateBaseView) {
            linkMicLayoutParent = (IPolyvRotateBaseView) linkMicStubView;
        }

        if (playMode == PolyvPlayOption.PLAYMODE_LIVE) {
            PolyvLinkMicWrapper.getInstance().init(Utils.getApp());
            PolyvLinkMicWrapper.getInstance().intialConfig(channelId);
        }

        linkMicLayoutParent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams rlp = linkMicLayoutParent.getLayoutParamsLayout();
                if (rlp == null) {
                    return;
                }

                rlp.leftMargin = 0;
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rlp.topMargin = 0;
                } else { // 若初始为竖屏
                    rlp.topMargin = chatContainerLayout.getTop() + chatTopSelectLayout.getMeasuredHeight();
                }

                linkMicLayoutParent.setOriginTop(rlp.topMargin);
                linkMicLayoutParent.setLayoutParams(rlp);

                if (Build.VERSION.SDK_INT >= 16) {
                    linkMicLayoutParent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    linkMicLayoutParent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private void initialChatRoom() {
        imageViewerContainer = findViewById(R.id.image_viewer_container);
        chatEditContainer = findViewById(R.id.chat_edit_container);
        chatTopSelectLayout = findViewById(R.id.chat_top_select_layout);
        chatContainerLayout = findViewById(R.id.chat_container_layout);
        personalChatItemLayout = findViewById(R.id.personal_chat_item_layout);
        groupChatItemLayout = findViewById(R.id.group_chat_item_layout);
        chatViewPager = findViewById(R.id.chat_viewpager);
        if (playMode == PolyvPlayOption.PLAYMODE_LIVE) {//直播
            chatContainerLayout.setVisibility(View.VISIBLE);
            personalChatItemLayout.setOnClickListener(this);
            groupChatItemLayout.setOnClickListener(this);

            List<Fragment> fragments = new ArrayList<>();
            polyvChatGroupFragment = new PolyvChatGroupFragment();
            polyvChatGroupFragment.setNormalLive(isNormalLive);
            fragments.add(polyvChatGroupFragment);
            chatPagerAdapter = new PolyvChatFragmentAdapter(getSupportFragmentManager(), fragments);
            chatViewPager.setAdapter(chatPagerAdapter);
            chatViewPager.setPageMargin(ConvertUtils.dp2px(10));
            chatViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
            groupChatItemLayout.setSelected(true);
            lastView = groupChatItemLayout;
            chatViewPager.setCurrentItem(0);
        }
    }

    private void intialPpt() {
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

                rlp.leftMargin = ((View) videoPptContainer.getParent()).getMeasuredWidth() - videoPptContainer.getMeasuredWidth();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rlp.topMargin = 0;
                } else { // 若初始为竖屏
                    rlp.topMargin = chatContainerLayout.getTop() + chatTopSelectLayout.getBottom();
                }
                videoPptContainer.setOriginTop(chatContainerLayout.getTop() + chatTopSelectLayout.getBottom());

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
        answerView.setStudentUserId(studentUserId);
        answerView.setAnswerJsCallback(new PolyvAnswerWebView.AnswerJsCallback() {
            @Override
            public void callOnHasAnswer(PolyvJSQuestionVO polyvJSQuestionVO) {
                PolyvCommonLog.d(TAG, "send to server has choose answer");
                if (chatManager != null) {
                    PolyvQuestionSocketVO socketVO = new PolyvQuestionSocketVO
                            (polyvJSQuestionVO.getAnswerId(), studentNickName, polyvJSQuestionVO.getQuestionId(),
                                    channelId, chatManager.userId);
                    chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, socketVO);
                }
            }

            @Override
            public void callOnHasQuestionnaireAnswer(PolyvQuestionnaireSocketVO polyvQuestionnaireSocketVO) {
                PolyvCommonLog.d(TAG, "发送调查问卷答案");
                polyvQuestionnaireSocketVO.setNick(studentNickName);
                polyvQuestionnaireSocketVO.setRoomId(channelId);
                polyvQuestionnaireSocketVO.setUserId(chatManager.userId);
                chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, polyvQuestionnaireSocketVO);
            }

            @Override
            public void callOnSignIn(PolyvSignIn2SocketVO socketVO) {
                socketVO.setUser(new PolyvSignIn2SocketVO.UserBean(studentNickName, studentUserId));
                chatManager.sendScoketMessage(Socket.EVENT_MESSAGE, socketVO);
            }

            @Override
            public void callOnLotteryWin(String lotteryId, String winnerCode, String viewerId, String telephone,String realName,String address) {
                PolyvResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvApichatApi()
                                .postLotteryWinnerInfo(channelId, lotteryId, winnerCode, studentUserId, realName, telephone,address),
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
                                .postLotteryAbandon(channelId, studentUserId), String.class,
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
    }

    private void intialOretation() {
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

        playbackVideoHelper.setNickName(studentNickName);

        playPlaybackVideo();
    }

    private void playPlaybackVideo() {
        playbackVideoHelper.resetView(isNormalLivePlayBack);

        // TODO: 2018/9/12 videoId 为直播平台的 videopoolid为点播平台的视频id
        PolyvPlaybackVideoParams playbackVideoParams = new PolyvPlaybackVideoParams(videoId,//videoId
                channelId,
                userId,studentUserId//viewerid
        );
        playbackVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                .buildOptions(PolyvBaseVideoParams.IS_PPT_PLAY, true)
                // TODO: 2019/3/25 请在此处填入用户的昵称
                .buildOptions(PolyvBaseVideoParams.PARAMS2, studentNickName)
                .buildOptions(PolyvPlaybackVideoParams.ENABLE_ACCURATE_SEEK, true);
        playbackVideoHelper.startPlay(playbackVideoParams);
    }

    private void initialLiveVideo() {
        PolyvCloudClassVideoItem cloudClassVideoItem = new PolyvCloudClassVideoItem(this);
        cloudClassVideoItem.setOnSendDanmuListener((danmuMessage)->{
            polyvChatGroupFragment.sendChatMessageByDanmu(danmuMessage);
        });

        livePlayerHelper = new PolyvCloudClassVideoHelper(cloudClassVideoItem,
                isNormalLive ? null : new PolyvPPTItem<PolyvCloudClassMediaController>(this), chatManager);
        livePlayerHelper.addVideoPlayer(playerContainer);
        livePlayerHelper.initConfig(isNormalLive);
        livePlayerHelper.addPPT(videoPptContainer);

        livePlayerHelper.addLinkMicLayout(linkMicLayout, linkMicLayoutParent);
//        linkMicLayoutParent.resetFloatViewPort();

        PolyvCloudClassVideoParams cloudClassVideoParams = new PolyvCloudClassVideoParams(channelId, userId
                , studentUserId// viewerid
                 );
        cloudClassVideoParams.buildOptions(PolyvBaseVideoParams.WAIT_AD, true)
                .buildOptions(PolyvBaseVideoParams.MARQUEE, true)
                // TODO: 2019/3/25 请在此处填入用户的昵称
                .buildOptions(PolyvBaseVideoParams.PARAMS2,studentNickName);
        livePlayerHelper.startPlay(cloudClassVideoParams);
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
            videoPptContainer.topSubviewTo(chatContainerLayout.getTop() + chatTopSelectLayout.getTop());
        }

        if (linkMicLayoutParent != null) {
            linkMicLayoutParent.topSubviewTo(chatContainerLayout.getTop() + chatTopSelectLayout.getTop());
        }
    }

    private void removeView() {
        playerContainer.removeAllViews();
        videoPptContainer.removeAllViews();
        livePlayerHelper = null;
        playbackVideoHelper = null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick方法">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.group_chat_item_layout:
                chatViewPager.setCurrentItem(0);
                break;
            case R.id.personal_chat_item_layout:
                chatViewPager.setCurrentItem(1);
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
            } else if (chatPagerAdapter != null) {
                Fragment fragment = chatPagerAdapter.getItem(chatViewPager.getCurrentItem());
                if (fragment instanceof PolyvChatBaseFragment) {
                    if (((PolyvChatBaseFragment) fragment).onBackPressed())
                        return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录聊天室">
    private void requestLiveClassDetailApi() {
        disposables.add(PolyvChatApiRequestHelper.getInstance()
                .requestLiveClassDetailApi(channelId)
                .subscribe(new Consumer<PolyvLiveClassDetailVO>() {
                    @Override
                    public void accept(PolyvLiveClassDetailVO polyvLiveClassDetailVO) throws Exception {
                        if (polyvLiveClassDetailVO.isOpenQuizMenu()) {
                            chatPagerAdapter.add(new PolyvChatPrivateFragment());
                            chatPagerAdapter.notifyDataSetChanged();
                            personalChatItemLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //获取失败，私聊tab默认关闭
                    }
                }));
    }

    private void loginChatRoom() {
        //获取私聊开关
        requestLiveClassDetailApi();

        PolyvChatEventBus.clear();
        chatManager.setAccountId(userId);
        //先添加监听器再登录
        chatManager.addConnectStatusListener(new PolyvConnectStatusListener() {
            @Override
            public void onConnectStatusChange(int status, @Nullable Throwable t) {
                //由于登录成功后，Fragment可能还没初始化完成，所以这里使用Rxjava的ReplayRelay把信息保存下来，在Fragment初始化完成那里获取
                PolyvChatEventBus.get().post(new PolyvChatBaseFragment.ConnectStatus(status, t));
            }
        });
        chatManager.addNewMessageListener(new PolyvNewMessageListener2() { // 用于监听登录的相关消息
            @Override
            public void onNewMessage(String message, String event, String socketListen) {
                //由于登录成功后，Fragment可能还没初始化完成，所以这里使用Rxjava的ReplayRelay把信息保存下来，在Fragment初始化完成那里获取
                PolyvChatEventBus.get().post(new PolyvChatBaseFragment.EventMessage(message, event, socketListen));
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

        //TODO 登录聊天室(userId：学员的Id(聊天室用户的唯一标识，非直播后台的userId，不同的学员应当使用不同的userId)，roomId：频道号，nickName：学员的昵称)
        //TODO 登录聊天室后一些功能才可以正常使用，例如：连麦

        //根据直播类型(普通直播、三分屏直播)设置聊天室的学员的类型
        chatManager.userType = isNormalLive ? PolyvChatManager.USERTYPE_STUDENT : PolyvChatManager.USERTYPE_SLICE;
        chatManager.login(studentUserId, channelId, studentNickName);

        if (livePlayerHelper != null) {
            livePlayerHelper.setNickName(studentNickName);
        }
        if (playbackVideoHelper != null) {
            playbackVideoHelper.setNickName(studentNickName);
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IPolyvHomePresnter实现">
    @Override
    public String getSessionId() {
        if (playMode == PolyvPlayOption.PLAYMODE_LIVE && livePlayerHelper != null) {
            if (livePlayerHelper.getVideoView() != null && livePlayerHelper.getVideoView().getModleVO() != null) {
                return livePlayerHelper.getVideoView().getModleVO().getChannelSessionId();
            }
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
    // </editor-fold>

}
