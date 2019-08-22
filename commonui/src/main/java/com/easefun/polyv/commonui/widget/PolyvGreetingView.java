package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.commonui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PolyvGreetingView extends FrameLayout {
    private TextView greetingText;
    private List<PolyvLoginEvent> loginEventList = new ArrayList<>();
    private boolean isStart;
    private Disposable acceptLoginDisposable;

    public PolyvGreetingView(@NonNull Context context) {
        this(context, null);
    }

    public PolyvGreetingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvGreetingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.polyv_chat_greeting_layout, this);
        greetingText = findViewById(R.id.greeting_text);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (acceptLoginDisposable != null) {
            acceptLoginDisposable.dispose();
        }
    }

    private void showGreetingText() {
        if (loginEventList.size() < 1) {
            setVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(0f, -getWidth(), 0f, 0f);
            animation.setDuration(500);
            startAnimation(animation);
            isStart = !isStart;
            return;
        }

        SpannableStringBuilder span;
        if (loginEventList.size() >= 10) {
            StringBuilder stringBuilder = new StringBuilder();
            int lf = 0, ls = 0;
            for (int i = 0; i <= 2; i++) {
                PolyvLoginEvent loginEvent = loginEventList.get(i);
                if (i != 2)
                    stringBuilder.append(loginEvent.getUser().getNick()).append("、");
                else
                    stringBuilder.append(loginEvent.getUser().getNick());
                if (i == 0)
                    lf = stringBuilder.toString().length() - 1;
                else if (i == 1)
                    ls = stringBuilder.toString().length() - lf - 2;
            }
            span = new SpannableStringBuilder("欢迎 " + stringBuilder.toString() + " 等" + loginEventList.size() + "人加入");
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, 3 + lf, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1, 3 + lf + 1 + ls, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3 + lf + 1 + ls + 1, span.length() - 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            loginEventList.clear();
        } else {
            PolyvLoginEvent loginEvent = loginEventList.remove(0);
            span = new SpannableStringBuilder("欢迎 " + loginEvent.getUser().getNick() + " 加入");
            span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, span.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        final SpannableStringBuilder finalSpan = span;

        acceptLoginDisposable = Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setVisibility(View.VISIBLE);
                        greetingText.setText(finalSpan);
                        TranslateAnimation animation = new TranslateAnimation(-getWidth(), 0f, 0f, 0f);
                        animation.setDuration(500);
                        startAnimation(animation);
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer integer) throws Exception {
                        return Observable.timer(500 + 1 * 1000, TimeUnit.MILLISECONDS);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        showGreetingText();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    public void acceptLoginEvent(final PolyvLoginEvent loginEvent) {
        loginEventList.add(loginEvent);
        if (!isStart) {
            isStart = !isStart;
            showGreetingText();
        }
    }
}
