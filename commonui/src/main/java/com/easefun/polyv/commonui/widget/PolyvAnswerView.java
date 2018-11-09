package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.cloudclass.PolyvSocketEvent;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionResultVO;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.model.answer.PolyvQuestionSResult;
import com.easefun.polyv.cloudclass.video.PolyvAnswerWebView;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.GET_TEST_QUESTION_CONTENT;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.GET_TEST_QUESTION_RESULT;
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
    private ImageView close;
    private Disposable messageDispose;


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
        close = findViewById(R.id.answer_close);
        answerWebView.loadWeb();
        messageDispose = PolyvRxBus.get().toObservable(PolyvSocketMessageVO.class).subscribe(new Consumer<PolyvSocketMessageVO>() {
            @Override
            public void accept(PolyvSocketMessageVO polyvSocketMessage) throws Exception {

                String event = polyvSocketMessage.getEvent();
                processSocketMessage(polyvSocketMessage, event);
            }
        });

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                answerContainer.setVisibility(GONE);
            }
        });
    }

    public void processSocketMessage(PolyvSocketMessageVO polyvSocketMessage, String event) {
        if (GET_TEST_QUESTION_CONTENT.equals(event) ||
                GET_TEST_QUESTION_RESULT.equals(event)) {
            PolyvCommonLog.d(TAG, "receive question message");
            showAnswerWebView(polyvSocketMessage);
        }

        if (event != null && event.contains(TEST_QUESTION)) {
            if (answerWebView != null) {
                answerWebView.callTestQuestion(polyvSocketMessage.getMessage());
            }
        }
    }


    private void showAnswerWebView(PolyvSocketMessageVO polyvSocketMessage) {
        if (polyvSocketMessage == null || ScreenUtils.isLandscape() ) {
            return;
        }
        if(PolyvSocketEvent.GET_TEST_QUESTION_CONTENT.equals(polyvSocketMessage.getEvent())){
            PolyvQuestionSResult polyvQuestionSResult =
                    PolyvGsonUtil.fromJson(PolyvQuestionSResult.class,polyvSocketMessage.getMessage());
            if(polyvQuestionSResult != null && "S".equals(polyvQuestionSResult.getType())){
                return;
            }
        }
        PolyvQuestionResultVO socketVO = null;
        if(PolyvSocketEvent.GET_TEST_QUESTION_RESULT.equals(polyvSocketMessage.getEvent())){
            socketVO = PolyvGsonUtil.fromJson(PolyvQuestionResultVO.class, polyvSocketMessage.getMessage());
            if(socketVO != null&&socketVO.getResult() != null && "S".equals(socketVO.getResult().getType())){
                return;
            }
        }

        answerContainer.setVisibility(VISIBLE);
        if (PolyvSocketEvent.GET_TEST_QUESTION_RESULT.equals(polyvSocketMessage.getEvent())) {
            if(socketVO != null){
                answerWebView.callHasChooseAnswer(socketVO.getQuestionId(), polyvSocketMessage.getMessage());
            }
        } else if (PolyvSocketEvent.GET_TEST_QUESTION_CONTENT.equals(polyvSocketMessage.getEvent())) {
            answerWebView.callUpdateNewQuestion(polyvSocketMessage.getMessage());
        }
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
        if (answerWebView != null) {
            answerWebView = null;
        }
    }
}
