package com.easefun.polyv.commonui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.cloudclass.PolyvSocketEvent;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionResultVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionSResult;
import com.easefun.polyv.cloudclass.model.lottery.PolyvLottery2JsVO;
import com.easefun.polyv.cloudclass.model.lottery.PolyvLotteryEndVO;
import com.easefun.polyv.cloudclass.model.lottery.PolyvLotteryWinnerVO;
import com.easefun.polyv.cloudclass.model.sign_in.PolyvSignIn2JsVO;
import com.easefun.polyv.cloudclass.model.sign_in.PolyvSignInVO;
import com.easefun.polyv.cloudclass.video.PolyvAnswerWebView;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.google.gson.Gson;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.TEST_QUESTION;

/**
 * 答题
 *
 * @author df
 * @create 2018/9/3
 * @Describe
 */
public class PolyvAnswerView extends FrameLayout {

    private static final String TAG = "PolyvAnswerView";

    private PolyvAnswerWebView answerWebView;
    private ViewGroup answerContainer;
    //    private ImageView close;
    private Disposable messageDispose;

    private static final int DELAY_SOCKET_MSG = 2 * 1000;

    //学员Id
    private String studentUserId;

    private ScrollView scrollView;
    private LinearLayout ll;
    private ImageView ivClose;

    private volatile boolean isDestroy = false;

    public PolyvAnswerView(@NonNull Context context) {
        this(context, null);
    }

    public PolyvAnswerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvAnswerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView(context);
    }

    private void initialView(Context context) {
        isDestroy = false;
        View.inflate(context, R.layout.polyv_answer_web_layout, this);
        answerWebView = findViewById(R.id.polyv_question_web);
        answerContainer = findViewById(R.id.polyv_answer_web_container);
        scrollView = findViewById(R.id.polyv_answer_scroll);
        ll = findViewById(R.id.polyv_answer_ll);
        ivClose = findViewById(R.id.polyv_answer_close);

        answerWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setMessage(message)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                                dialog.dismiss();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return true;
            }
        });
        answerWebView.setAnswerContainer(answerContainer);
        answerWebView.loadUrl("file:///android_asset/index.html");

        messageDispose = PolyvRxBus.get().toObservable(PolyvSocketMessageVO.class)
                .compose(new PolyvRxBaseTransformer<PolyvSocketMessageVO, PolyvSocketMessageVO>())
                .subscribe(new Consumer<PolyvSocketMessageVO>() {
                    @Override
                    public void accept(final PolyvSocketMessageVO polyvSocketMessage) throws Exception {
                        final String event = polyvSocketMessage.getEvent();
                        if (event.contains("SIGN_IN") || event.contains("Lottery") || event.contains("LOTTERY")) {
                            processSocketMessage(polyvSocketMessage, event);
                        } else {
                            PolyvRxTimer.delay(DELAY_SOCKET_MSG, new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    if (!isDestroy){
                                        processSocketMessage(polyvSocketMessage, event);
                                    }
                                }
                            });
                        }
                    }
                });

        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAnswerContainer();
            }
        });
        handleKeyboardOrientation();
    }

    private void handleKeyboardOrientation() {
        post(new Runnable() {
            @Override
            public void run() {
                new PolyvAnswerKeyboardHelper(ActivityUtils.getTopActivity());
            }
        });
    }

    public void processSocketMessage(PolyvSocketMessageVO polyvSocketMessage, String event) {
        String msg = polyvSocketMessage.getMessage();
        if (msg == null || event == null) {
            return;
        }
        LogUtils.json(msg);
        switch (event) {
            //讲师发题
            case PolyvSocketEvent.GET_TEST_QUESTION_CONTENT:
                PolyvQuestionSResult polyvQuestionSResult = PolyvGsonUtil.fromJson(PolyvQuestionSResult.class, msg);
                if (polyvQuestionSResult != null && "S".equals(polyvQuestionSResult.getType())) {
                    break;
                }
                showAnswerContainer();
                answerWebView.callUpdateNewQuestion(msg);
                break;
            //讲师发送答题结果
            case PolyvSocketEvent.GET_TEST_QUESTION_RESULT:
                PolyvQuestionResultVO socketVO;
                socketVO = PolyvGsonUtil.fromJson(PolyvQuestionResultVO.class, msg);
                if (socketVO == null) {
                    return;
                }
                if (socketVO.getResult() != null && "S".equals(socketVO.getResult().getType())) {
                    return;
                }
                showAnswerContainer();
                answerWebView.callHasChooseAnswer(socketVO.getQuestionId(), msg);
                break;
            //截止答题
            case PolyvSocketEvent.STOP_TEST_QUESTION:
                answerWebView.callStopQuestion();
                break;
            //润德没有问卷调查
            //开始问卷调查
            case PolyvSocketEvent.START_QUESTIONNAIRE:
//                showAnswerContainer();
//                answerWebView.callStartQuestionnaire(msg);
                break;
            //停止问卷调查
            case PolyvSocketEvent.STOP_QUESTIONNAIRE:
//                answerWebView.callStopQuestionnaire(msg);
                break;
            //开始抽奖
            case PolyvSocketEvent.LOTTERY_START:
                showAnswerContainer();
                answerWebView.callStartLottery();
                break;
            //当前频道正在抽奖
            case PolyvSocketEvent.ON_LOTTERY:
                showAnswerContainer();
                answerWebView.callStartLottery();
                break;
            //停止抽奖
            case PolyvSocketEvent.LOTTERY_END:
                showAnswerContainer();
                boolean win = false;
                String winnerCode = "";
                PolyvLotteryEndVO vo = PolyvGsonUtil.fromJson(PolyvLotteryEndVO.class, msg);
                List<PolyvLotteryEndVO.DataBean> winnerList = notNull(vo).getData();
                for (PolyvLotteryEndVO.DataBean bean : winnerList) {
                    if (studentUserId.equals(bean.getUserId())) {
                        win = true;
                        winnerCode = bean.getWinnerCode();
                        answerWebView.setWinnerCode(winnerCode);
                        break;
                    }
                }
                PolyvLottery2JsVO winnerVo = new PolyvLottery2JsVO(win, vo.getPrize(), winnerCode);
                String winnerJson1 = winnerVo.toJson();
                LogUtils.d(winnerJson1);
                answerWebView.callLotteryEnd(winnerJson1, vo.getSessionId(), vo.getLotteryId());
                break;
            //未领奖的中奖人信息
            case PolyvSocketEvent.LOTTERY_WINNER:
                PolyvLotteryWinnerVO winnerVO = PolyvGsonUtil.fromJson(PolyvLotteryWinnerVO.class, msg);
                showAnswerContainer();
                PolyvLottery2JsVO winnerJsonVO = new PolyvLottery2JsVO(true, notNull(winnerVO).getPrize(), winnerVO.getWinnerCode());
                String winnerJson2 = winnerJsonVO.toJson();
                if (winnerJsonVO.isWin()) {
                    answerWebView.setWinnerCode(winnerJsonVO.getWinnerCode());
                }
                LogUtils.json(winnerJson2);
                answerWebView.callLotteryWinner(winnerJson2, winnerVO.getSessionId(), winnerVO.getLotteryId());
                break;
            //开始签到
            case PolyvSocketEvent.START_SIGN_IN:
                showAnswerContainer();
                PolyvSignInVO signInVO = PolyvGsonUtil.fromJson(PolyvSignInVO.class, msg);
                PolyvSignIn2JsVO signIn2JsVO = new PolyvSignIn2JsVO(notNull(signInVO).getData().getLimitTime(), signInVO.getData().getMessage());
                String signJson;
                Gson gson = new Gson();
                signJson = gson.toJson(signIn2JsVO);
                LogUtils.json(signJson);
                answerWebView.startSignIn(signJson, signInVO);
                break;
            //停止签到
            case PolyvSocketEvent.STOP_SIGN_IN:
                answerWebView.stopSignIn();
                break;
            //其他
            default:
                if (event.contains(TEST_QUESTION)) {
                    answerWebView.callTestQuestion(msg);
                }
                break;
        }
    }

    private void showAnswerContainer() {
        answerWebView.requestFocusFromTouch();
        answerContainer.setVisibility(VISIBLE);
    }

    private void hideAnswerContainer() {
        answerContainer.setVisibility(INVISIBLE);
    }

    public void setStudentUserId(String studentUserId) {
        this.studentUserId = studentUserId;
    }

    private <T> T notNull(T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return object;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (messageDispose != null && !messageDispose.isDisposed()) {
            messageDispose.dispose();
        }
    }

    public void setAnswerJsCallback(PolyvAnswerWebView.AnswerJsCallback answerJsCallback) {
        if (answerWebView != null) {
            answerWebView.setAnswerJsCallback(answerJsCallback);
        }
    }

    public void destroy() {
        isDestroy = true;
        if (answerWebView != null) {
            answerWebView = null;
        }

        if (messageDispose != null) {
            messageDispose.dispose();
            messageDispose = null;
        }
    }

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            onLandscape();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            onPortrait();
//        }
//    }
//
//    //横屏回调
//    private void onLandscape() {
//        LogUtils.d("横屏");
//    }
//
//    //竖屏回调
//    private void onPortrait() {
//        LogUtils.d("竖屏");
//    }

    /**
     * 横屏全屏键盘遮挡帮助类
     */
    private class PolyvAnswerKeyboardHelper {

        private View mChildOfContent;

        private int usableHeightPrevious;

        private View bottomPlaceHolder;

        private PolyvAnswerKeyboardHelper(Activity activity) {
            FrameLayout content = activity.findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    possiblyResizeChildOfContent();
                }
            });
        }

        @SuppressLint("ClickableViewAccessibility")
        private void possiblyResizeChildOfContent() {
            //当答题卡不可见时，答题卡不处理任何键盘事件
            if (answerContainer.getVisibility() != VISIBLE) {
                return;
            }

            int usableHeightNow = computeUsableHeight();
            if (usableHeightNow != usableHeightPrevious) {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - usableHeightNow;
                if (heightDifference > (usableHeightSansKeyboard / 4)) {
                    scrollView.setOnTouchListener(null);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    //键盘弹起
                    if (ScreenUtils.isPortrait()) {
                        return;
                    }
                    if (bottomPlaceHolder == null) {
                        bottomPlaceHolder = new View(getContext());
                    }
                    ll.addView(bottomPlaceHolder, ViewGroup.LayoutParams.MATCH_PARENT, heightDifference - 100);
                    ll.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                } else {
                    //键盘收缩
                    if (ll.indexOfChild(bottomPlaceHolder) > 0) {
                        ll.removeView(bottomPlaceHolder);
                    }
                    scrollView.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return true;
                        }
                    });
                }
                usableHeightPrevious = usableHeightNow;
            }
        }

        //计算剩余高度
        private int computeUsableHeight() {
            Rect r = new Rect();
            mChildOfContent.getWindowVisibleDisplayFrame(r);
            return (r.bottom - r.top);// 全屏模式下： return r.bottom
        }
    }
}

