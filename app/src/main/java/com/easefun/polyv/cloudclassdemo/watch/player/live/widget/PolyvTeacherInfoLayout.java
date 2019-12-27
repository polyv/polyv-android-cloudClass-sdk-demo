package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.PolyvTeacherInfo;
import com.easefun.polyv.cloudclass.model.PolyvTeacherStatusInfo;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.PolyvDemoClient;
import com.easefun.polyv.cloudclassdemo.watch.player.live.PolyvCloudClassVideoHelper;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.commonui.widget.PolyvTouchContainerView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_CLOSECALLLINKMIC;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_END;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_HIDESUBVIEW;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_N0_PPT;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_NO_STREAM;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_OPENCALLLINKMIC;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_OPEN_PPT;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_SHOWSUBVIEW;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LIVE_START;
import static com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO.LiveStatus.LOGIN_CHAT_ROOM;

/**
 * @author df
 * @create 2019/7/17
 * @Describe
 */
public class PolyvTeacherInfoLayout extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "PolyvTeacherInfoLayout";

    private LinearLayout teacherInfo;
    private LinearLayout teacherInfoMiddleContainer;
    private FrameLayout teacherSubView;

    private ImageView teacherImg;
    private TextView teacherNameVertical;
    private TextView onlineNumber;
    private LinearLayout linkMicLayout, linkMicCallLayout;
    private ImageView linkMicBackTeacherInfo;
    private TextView linkMicStatus;
    private ImageView linkMicStatusImg;

    private boolean linkMicCallAbove;//连麦界面是否在最上面

    //是否第一次收到开启连麦
    private boolean isFirstTimeReceiveLinkMicOpen = true;
    //是否第一次收到关闭连麦
    private boolean isFirstTimeReceiveLinkMicClose = true;
    //是否还没有点击连麦按钮
    private boolean hasNotClickLinkMic = true;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private PolyvCloudClassVideoHelper cloudClassVideoHelper;

    public PolyvTeacherInfoLayout(Context context) {
        this(context, null);
    }

    public PolyvTeacherInfoLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvTeacherInfoLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
        addListener();
        regsiterBeanListener();

        initData();
    }

    private void initData() {
        fillTeacherInfo();
    }

    // <editor-fold defaultstate="collapsed" desc="初始化">
    @SuppressLint("ClickableViewAccessibility")
    private void addListener() {
        linkMicBackTeacherInfo.setOnClickListener(this);
        linkMicStatusImg.setOnClickListener(this);

        //防止别的地方设置onClickListener()覆盖了这里设置的（controller里面可能会设置），改用OnTouchListener
        linkMicStatusImg.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hasNotClickLinkMic = false;
                }
                return false;
            }
        });
    }

    private void initView() {
        View.inflate(getContext(), R.layout.polyv_teacher_info, this);

        teacherSubView = findViewById(R.id.subview_layout);
        teacherInfo = (LinearLayout) findViewById(R.id.teacher_info);
        teacherInfoMiddleContainer = (LinearLayout) findViewById(R.id.teacher_info_middle_container);
        teacherImg = (ImageView) findViewById(R.id.teacher_img);
        teacherNameVertical = (TextView) findViewById(R.id.teacher_name_vertical);
        onlineNumber = (TextView) findViewById(R.id.online_number);
        linkMicLayout = (LinearLayout) findViewById(R.id.link_mic_layout);
        linkMicCallLayout = (LinearLayout) findViewById(R.id.link_mic_call_layout);
        linkMicBackTeacherInfo = (ImageView) findViewById(R.id.link_mic_arrow);
        linkMicStatus = (TextView) findViewById(R.id.link_mic_status);
        linkMicStatusImg = (ImageView) findViewById(R.id.link_mic_status_img);

        linkMicStatusImg.setEnabled(false);

        moveLinkMicToRight();

    }

    private void regsiterBeanListener() {

        compositeDisposable.add(PolyvRxBus.get().toObservable(PolyvTeacherStatusInfo.class).
                subscribe(new Consumer<PolyvTeacherStatusInfo>() {
                    @Override
                    public void accept(final PolyvTeacherStatusInfo polyvLiveClassDetailVO) throws Exception {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                String watchStatus = polyvLiveClassDetailVO.getWatchStatus();
                                PolyvCommonLog.d(TAG, "teacher receive status:" + watchStatus);
                                if (LIVE_END.equals(watchStatus) || LIVE_NO_STREAM.equals(watchStatus)) {
                                    setVisibility(GONE);
                                } else if (LIVE_START.equals(watchStatus)) {
                                    setVisibility(VISIBLE);
                                } else if (LIVE_OPEN_PPT.equals(watchStatus)) {
                                    showWithPPTStatus(VISIBLE);
                                } else if (LIVE_N0_PPT.equals(watchStatus)) {
                                    showWithPPTStatus(GONE);
                                } else if (LIVE_HIDESUBVIEW.equals(watchStatus)) {
                                    teacherSubView.setVisibility(GONE);
                                    if (!linkMicCallAbove) {
                                        moveLinkMicToRight();
                                    }
                                } else if (LIVE_SHOWSUBVIEW.equals(watchStatus)) {
                                    teacherSubView.setVisibility(VISIBLE);
                                    if (!linkMicCallAbove) {
                                        moveLinkMicToRight();
                                    }
                                } else if (LIVE_OPENCALLLINKMIC.equals(watchStatus)) {
                                    linkMicStatusImg.setEnabled(true);
                                    linkMicStatus.setText("讲师已开启连线");

                                    handleAutoMoveLinkMicLayoutWhenReceiveBusEvent(true);
                                } else if (LIVE_CLOSECALLLINKMIC.equals(watchStatus)) {
                                    linkMicStatusImg.setEnabled(false);
                                    linkMicStatus.setText("讲师已关闭连线");

                                    handleAutoMoveLinkMicLayoutWhenReceiveBusEvent(false);
                                } else if (LOGIN_CHAT_ROOM.equals(watchStatus)) {
                                    fillTeacherInfo();
                                }
                            }
                        });

                    }
                }));
    }

    protected void showWithPPTStatus(int visible) {
        if (cloudClassVideoHelper != null && !cloudClassVideoHelper.isJoinLinkMick()) {
            setVisibility(visible);
            teacherSubView.setVisibility(visible);
            if (!linkMicCallAbove) {
                moveLinkMicToRight();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="外部调用">
    public void init(@NonNull PolyvCloudClassVideoHelper cloudClassVideoHelper,
                     @NonNull PolyvTouchContainerView videoPptContainer) {
        this.cloudClassVideoHelper = cloudClassVideoHelper;
        cloudClassVideoHelper.bindCallMicView(linkMicStatusImg);
//        insertTeacherCamera(videoPptContainer);
    }

    public void fillTeacherInfo() {
        onlineNumber.setText(PolyvChatManager.getInstance().getOnlineCount() + "人在线");
        PolyvTeacherInfo teacherInfo = PolyvDemoClient.getInstance().getTeacher();
        if (teacherInfo != null) {
            teacherNameVertical.setText(teacherInfo.getData().getNick());
            PolyvImageLoader.getInstance().loadImage(this.getContext(), teacherInfo.getData().getPic(), teacherImg);
        }
    }

    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
            compositeDisposable = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="处理连麦面板左移显示和右移隐藏">

    /**
     * 显示或隐藏连麦面板。带动画
     *
     * @param show true就会把连麦面板左拉显示，false就会把连麦面板右拉隐藏。
     */
    private void showOrHideLinkMicLayout(final boolean show) {
        if (linkMicCallAbove == show) {
            return;
        }
        linkMicLayout.post(new Runnable() {
            @Override
            public void run() {
                int moveX = linkMicLayout.getMeasuredWidth() - PolyvScreenUtils.dip2px(getContext(), 36);
                PolyvCommonLog.d(TAG, "movex :" + moveX);
                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(linkMicLayout,
                                "translationX", show ? moveX : 0, show ? 0 : moveX);
                animator.setDuration(1000);
                animator.start();
                linkMicCallAbove = show;
            }
        });

    }

    /**
     * 隐藏连麦面板，不带动画。
     */
    private void moveLinkMicToRight() {
        linkMicCallLayout.setVisibility(INVISIBLE);
        post(new Runnable() {
            @Override
            public void run() {
                int moveX = linkMicLayout.getMeasuredWidth() - PolyvScreenUtils.dip2px(getContext(), 36);
                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(linkMicLayout,
                                "translationX", moveX, moveX);
                animator.setDuration(1);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        linkMicCallLayout.setVisibility(VISIBLE);
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

    /**
     * 当收到讲师开启连麦和关闭连麦的bus event的时候，处理自动显示和隐藏连麦面板。
     * <p>
     * 注意：由于连麦的开启和关闭状态既通过socket更新，也通过轮询接口更新，因此该方法
     * 会被调用多次，那么就通过 {@link #isFirstTimeReceiveLinkMicClose}和{@link #isFirstTimeReceiveLinkMicOpen}
     * 来过滤掉多余的连麦打开和关闭。
     *
     * @param isLinkMicOpen true:连麦打开;false:连麦关闭
     */
    private void handleAutoMoveLinkMicLayoutWhenReceiveBusEvent(boolean isLinkMicOpen) {
        if (isLinkMicOpen) {
            if (isFirstTimeReceiveLinkMicOpen) {
                //reset flag
                isFirstTimeReceiveLinkMicOpen = false;
                isFirstTimeReceiveLinkMicClose = true;
                hasNotClickLinkMic = true;

                showOrHideLinkMicLayout(true);
                linkMicBackTeacherInfo.setSelected(true);
            }
        } else {
            if (isFirstTimeReceiveLinkMicClose) {
                //reset flag
                isFirstTimeReceiveLinkMicClose = false;
                isFirstTimeReceiveLinkMicOpen = true;

                //如果没有点击连麦并且连麦面板还在，就收起连麦面板。
                if (hasNotClickLinkMic && linkMicCallAbove) {
                    showOrHideLinkMicLayout(false);
                    linkMicBackTeacherInfo.setSelected(false);
                }
            }
        }
    }
// </editor-fold>


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.link_mic_arrow:
                linkMicBackTeacherInfo.setSelected(!v.isSelected());
                showOrHideLinkMicLayout(v.isSelected());
                break;

            case R.id.link_mic_status_img:
                if (cloudClassVideoHelper != null) {
                    cloudClassVideoHelper.requestPermission();
                }
                break;
        }
    }


    // <editor-fold defaultstate="collapsed" desc="override">
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (cloudClassVideoHelper != null) {
            cloudClassVideoHelper = null;
        }

    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!linkMicCallAbove) {
                moveLinkMicToRight();
            }
        }
    }

    public void hideHandsUpLink(boolean isParticipant) {
        linkMicLayout.setVisibility(isParticipant ? INVISIBLE : VISIBLE);
    }

    // </editor-fold>

}
