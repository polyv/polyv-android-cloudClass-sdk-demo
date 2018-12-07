package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PolyvGreetingTextView extends AppCompatTextView {
    private List<PolyvLoginEvent> loginEventList = new ArrayList<>();
    private Disposable acceptLoginDisposable;

    public PolyvGreetingTextView(Context context) {
        super(context);
    }

    public PolyvGreetingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvGreetingTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loginEventList = Collections.synchronizedList(loginEventList);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loginEventList.clear();
        acceptLoginDisposable.dispose();
    }

    private int getShowTime() {
        int showTime = 3;
        if (loginEventList.size() >= 3)
            showTime = 1;
        return showTime;
    }

    private void showGreetingText() {
        if (loginEventList.size() < 1) {
            setVisibility(View.GONE);
            return;
        }
        final int showTime = getShowTime();
        final PolyvLoginEvent loginEvent = loginEventList.remove(0);
        acceptLoginDisposable = Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        SpannableStringBuilder span = new SpannableStringBuilder("欢迎 " + loginEvent.getUser().getNick() + " 加入");
                        span.setSpan(new ForegroundColorSpan(Color.rgb(129, 147, 199)), 3, span.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        setText(span);
                        setVisibility(View.VISIBLE);
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer integer) throws Exception {
                        return Observable.timer(showTime, TimeUnit.SECONDS);
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
        if (getVisibility() != View.VISIBLE) {
            showGreetingText();
        }
    }
}
