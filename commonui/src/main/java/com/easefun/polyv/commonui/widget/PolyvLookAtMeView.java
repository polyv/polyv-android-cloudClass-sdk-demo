package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 看我事件的view，需要使用clickable属性
 */
public class PolyvLookAtMeView extends AppCompatImageView {
    private final int msgWhat = 1;
    private final int timeInterval = 1000;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == msgWhat) {
                postLookAtMeEvent();
                handler.sendEmptyMessageDelayed(msgWhat, timeInterval);
            }
        }
    };

    public PolyvLookAtMeView(Context context) {
        super(context);
    }

    public PolyvLookAtMeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvLookAtMeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void postLookAtMeEvent() {
        PolyvRxBus.get().post(new LookAtMeEvent());
    }

    public static Disposable registerLookAtMeEvent(Consumer<? super LookAtMeEvent> onNext) {
        return PolyvRxBus.get().toObservable(LookAtMeEvent.class).subscribe(onNext, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                PolyvCommonLog.exception(throwable);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);//on listView need clickable, on viewPager need request, or else can move
                postLookAtMeEvent();
                handler.removeMessages(msgWhat);
                handler.sendEmptyMessageDelayed(msgWhat, timeInterval);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                handler.removeMessages(1);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    public class LookAtMeEvent {

    }
}
