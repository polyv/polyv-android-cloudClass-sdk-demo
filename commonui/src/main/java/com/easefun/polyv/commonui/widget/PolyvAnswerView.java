package com.easefun.polyv.commonui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
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
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.cloudclass.PolyvSocketEvent;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionResultVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionSResult;
import com.easefun.polyv.cloudclass.model.lottery.PolyvLottery2JsVO;
import com.easefun.polyv.cloudclass.model.lottery.PolyvLotteryEndVO;
import com.easefun.polyv.cloudclass.model.lottery.PolyvLotteryWinnerVO;
import com.easefun.polyv.cloudclass.video.PolyvAnswerWebView;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
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
    private CompositeDisposable disposables = new CompositeDisposable();

    private static final int DELAY_SOCKET_MSG = 2 * 1000;

    //学员Id
    private String studentUserId;

    //中奖信息是否显示o
    private boolean isWinLotteryShow = false;
    //答题卡是否回答了
    private boolean isQuestionAnswer = false;
    //当前的答题id
    private String curQuestionId;

    private ScrollView scrollView;
    private LinearLayout ll;
    private ImageView ivClose;
    private AlertDialog alertDialog;


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
        View.inflate(context, R.layout.polyv_answer_web_layout, this);
        answerWebView = findViewById(R.id.polyv_question_web);
        answerContainer = findViewById(R.id.polyv_answer_web_container);
        scrollView = findViewById(R.id.polyv_answer_scroll);
        ll = findViewById(R.id.polyv_answer_ll);
        ivClose = findViewById(R.id.polyv_answer_close);

        answerWebView.setWebChromeClient(new PolyvNoLeakWebChromeClient(getContext()));
        answerWebView.setAnswerContainer(answerContainer);
        answerWebView.setOnCloseLotteryWinnerListener(new PolyvAnswerWebView.OnCloseLotteryWinnerListener() {
            @Override
            public void onClose() {
                onWinLotteryDisappear();
            }
        });
        answerWebView.setOnChooseAnswerListener(new PolyvAnswerWebView.OnChooseAnswerListener() {
            @Override
            public void onChoose() {
                isQuestionAnswer = true;
            }
        });
        answerWebView.loadWeb();

        messageDispose = PolyvRxBus.get().toObservable(PolyvSocketMessageVO.class)
                .compose(new PolyvRxBaseTransformer<PolyvSocketMessageVO, PolyvSocketMessageVO>())
                .subscribe(new Consumer<PolyvSocketMessageVO>() {
                    @Override
                    public void accept(final PolyvSocketMessageVO polyvSocketMessage) throws Exception {
                        String event = polyvSocketMessage.getEvent();
                        processSocketMessage(polyvSocketMessage, event);
                    }
                });

        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPress();
            }
        });
        handleKeyboardOrientation();
    }

    private void delay(final Runnable runnable) {
        disposables.add(PolyvRxTimer.delay(DELAY_SOCKET_MSG, new Consumer() {
            @Override
            public void accept(Object o) {
                runnable.run();
            }
        }));
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
        final String msg = polyvSocketMessage.getMessage();
        if (msg == null || event == null) {
            return;
        }
        LogUtils.json(msg);
        switch (event) {
            //讲师发题
            case PolyvSocketEvent.GET_TEST_QUESTION_CONTENT:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        PolyvQuestionSResult polyvQuestionSResult = PolyvGsonUtil.fromJson(PolyvQuestionSResult.class, msg);
                        if (polyvQuestionSResult != null && "S".equals(polyvQuestionSResult.getType())) {
                            return;
                        }
                        curQuestionId = notNull(polyvQuestionSResult).getQuestionId();
                        isQuestionAnswer = false;
                        showAnswerContainer();
                        answerWebView.callUpdateNewQuestion(msg);
                    }
                });
                break;
            //讲师发送答题结果
            case PolyvSocketEvent.GET_TEST_QUESTION_RESULT:
                delay(new Runnable() {
                    @Override
                    public void run() {
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
                    }
                });
                break;
            //截止答题
            case PolyvSocketEvent.STOP_TEST_QUESTION:
                delay(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject stopJson = null;
                        String questionId = "";
                        try {
                            stopJson = new JSONObject(msg);
                            questionId = stopJson.getString("questionId");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (!isQuestionAnswer && questionId.equals(curQuestionId)) {
                            showAnswerContainer();
                            answerWebView.callStopQuestion();
                        }
                    }
                });
                break;
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
                        onWinLotteryShow();
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
                    onWinLotteryShow();
                    answerWebView.setWinnerCode(winnerJsonVO.getWinnerCode());
                }
                LogUtils.json(winnerJson2);
                answerWebView.callLotteryWinner(winnerJson2, winnerVO.getSessionId(), winnerVO.getLotteryId());
                break;
            //开始签到
            case PolyvSocketEvent.START_SIGN_IN:
//                showAnswerContainer();
//                PolyvSignInVO signInVO = PolyvGsonUtil.fromJson(PolyvSignInVO.class, msg);
//                PolyvSignIn2JsVO signIn2JsVO = new PolyvSignIn2JsVO(notNull(signInVO).getData().getLimitTime(), signInVO.getData().getMessage());
//                String signJson;
//                Gson gson = new Gson();
//                signJson = gson.toJson(signIn2JsVO);
//                LogUtils.json(signJson);
//                answerWebView.startSignIn(signJson,signInVO);
                break;
            //停止签到
            case PolyvSocketEvent.STOP_SIGN_IN:
//                answerWebView.stopSignIn();
                break;
            //其他
            default:
                if (event.contains(TEST_QUESTION)) {
                    answerWebView.callTestQuestion(msg);
                }
                break;
        }
    }

    //显示中奖
    private void onWinLotteryShow() {
        isWinLotteryShow = true;
        lockToPortrait();
    }

    //关闭了中奖
    private void onWinLotteryDisappear() {
        isWinLotteryShow = false;
        PolyvScreenUtils.unlockOrientation();
    }

    //锁定屏幕方向为竖屏
    private void lockToPortrait() {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            PolyvScreenUtils.unlockOrientation();
            PolyvScreenUtils.setPortrait(topActivity);
        }
        PolyvScreenUtils.lockOrientation();
    }

    private void showAnswerContainer() {
        KeyboardUtils.hideSoftInput(this);
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
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    public void setAnswerJsCallback(PolyvAnswerWebView.AnswerJsCallback answerJsCallback) {
        if (answerWebView != null) {
            answerWebView.setAnswerJsCallback(answerJsCallback);
        }
    }

    public void destroy() {
        if (answerWebView != null) {
            answerWebView = null;
        }
        if(messageDispose != null){
            messageDispose.dispose();
            messageDispose = null;
        }
        if(disposables != null){
            disposables.dispose();
            disposables = null;
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

    public void onBackPress() {
        if (isWinLotteryShow) {
            answerWebView.callCloseLotteryWinner();
            return;
        }
        hideAnswerContainer();
    }

    private static class PolyvNoLeakWebChromeClient extends WebChromeClient {
        private WeakReference<Context> wrContext;

        PolyvNoLeakWebChromeClient(Context context) {
            this.wrContext = new WeakReference<>(context);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            Context context = wrContext.get();
            if (context == null) {
                return true;
            }
            AlertDialog dialog = new AlertDialog.Builder(context)
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
            dialog.setCancelable(false);
            dialog.show();
            return true;
        }
    }
}
