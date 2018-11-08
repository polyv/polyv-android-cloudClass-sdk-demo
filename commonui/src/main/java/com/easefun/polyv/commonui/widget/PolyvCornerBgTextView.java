package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.easefun.polyv.commonui.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PolyvCornerBgTextView extends android.support.v7.widget.AppCompatTextView {
    private Disposable disposable;

    public PolyvCornerBgTextView(Context context) {
        this(context, null);
    }

    public PolyvCornerBgTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvCornerBgTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.drawable.polyv_tv_status);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dispose();
    }

    private void dispose() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        clearAnimation();
    }

    public void hide() {
        dispose();
        setVisibility(View.GONE);
    }

    public void show() {
        dispose();
        setVisibility(View.VISIBLE);
    }

    public void show(final long time) {
        dispose();
        disposable = Observable.just(1)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        setVisibility(View.VISIBLE);
                    }
                })
                .flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer integer) throws Exception {
                        return Observable.timer(time, TimeUnit.MILLISECONDS);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        setVisibility(View.GONE);
                        Animation animation = new AlphaAnimation(1, 0);
                        animation.setDuration(666);
                        startAnimation(animation);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }
}
