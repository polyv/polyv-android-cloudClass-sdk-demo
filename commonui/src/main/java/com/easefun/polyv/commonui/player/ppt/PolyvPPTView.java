package com.easefun.polyv.commonui.player.ppt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.businesssdk.api.common.ppt.PolyvPPTWebView;
import com.easefun.polyv.businesssdk.model.ppt.PolyvPPTAuthentic;
import com.easefun.polyv.businesssdk.web.IPolyvWebMessageProcessor;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.rx.PolyvRxTimer;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICECONTROL;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEDRAW;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEID;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICEOPEN;
import static com.easefun.polyv.cloudclass.PolyvSocketEvent.ONSLICESTART;

/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public class PolyvPPTView extends FrameLayout implements IPolyvPPTView {
    private static final String TAG = "PolyvPPTView";
    public static final int DELAY_TIME =  5 *1000;
    public int delayTime = DELAY_TIME;
    protected PolyvPPTWebView polyvPPTWebView;
    protected ImageView pptLoadingView;
    private Disposable socketDispose;
    private CompositeDisposable delayDisposes = new CompositeDisposable();

    public PolyvPPTView(Context context) {
        this(context, null);
    }

    public PolyvPPTView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPPTView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView(context);
    }


    protected void initialView(Context context) {

        View.inflate(context, R.layout.polyv_ppt_webview_layout, this);
        polyvPPTWebView = findViewById(R.id.polyv_ppt_web);
        pptLoadingView = findViewById(R.id.polyv_ppt_default_icon);
        loadWeb();
    }

    @Override
    public void addWebProcessor(IPolyvWebMessageProcessor processor) {
        processor.bindWebView(polyvPPTWebView);
        polyvPPTWebView.registerProcessor(processor);
    }

    private void loadWeb() {
        polyvPPTWebView.loadWeb();//"file:///android_asset/startForMobile.html"
        registerSocketMessage();
    }

    private void registerSocketMessage() {
        socketDispose = PolyvRxBus.get().toObservable(PolyvSocketMessageVO.class).subscribe(new Consumer<PolyvSocketMessageVO>() {
            @Override
            public void accept(final PolyvSocketMessageVO polyvSocketMessageVO) throws Exception {
                PolyvCommonLog.d(TAG, "accept ppt message " + polyvSocketMessageVO);
                if (polyvSocketMessageVO == null) {
                    return;
                }

                processSocketMessage(polyvSocketMessageVO);
            }
        });
    }

    public void reLoad(){
        polyvPPTWebView.loadWeb();
    }

    public void processSocketMessage(final PolyvSocketMessageVO polyvSocketMessageVO) {
        String event = polyvSocketMessageVO.getEvent();
        if (ONSLICESTART.equals(event) ||
                ONSLICEDRAW.equals(event) ||
                ONSLICECONTROL.equals(event) ||
                ONSLICEOPEN.equals(event) || ONSLICEID.equals(event)) {
            PolyvCommonLog.d(TAG, "receive ppt message:"+event);
            hideLoading();
            delayDisposes.add(PolyvRxTimer.delay(delayTime, new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    PolyvCommonLog.d(TAG, "receive ppt message: delay"+polyvSocketMessageVO.getMessage());
                    String message = polyvSocketMessageVO.getMessage();
                    if(delayTime >0){
                        int lastPos = message.lastIndexOf("}");
                        message = message.substring(0,lastPos)+",\"delayTime\":"+delayTime+"}";
                    }

                    PolyvCommonLog.d(TAG, "receive ppt message: delay"+message);
                    sendWebMessage(message);
                }
            }));
        }
    }

    private void hideLoading() {
        if (pptLoadingView.isShown() && polyvPPTWebView.isShown()) {
            pptLoadingView.setVisibility(GONE);
        }
    }

    @Override
    public void pptPrepare(String message) {
        hideLoading();
        polyvPPTWebView.callPPTParams(message);
    }

    @Override
    public void play(String message) {
        polyvPPTWebView.callStart(message);
    }

    @Override
    public void pause(String message) {
        polyvPPTWebView.callPause(message);
    }

    @Override
    public void seek(String message) {
        polyvPPTWebView.callSeek(message);
    }

    @Override
    public void destroy() {

        PolyvCommonLog.d(TAG, "destroy ppt view");
        if (polyvPPTWebView != null) {
            polyvPPTWebView.removeAllViews();
            polyvPPTWebView.destroy();
            removeView(polyvPPTWebView);
        }
        polyvPPTWebView = null;

        if (socketDispose != null) {
            socketDispose.dispose();
            socketDispose = null;
        }

        if (delayDisposes != null) {
            delayDisposes.dispose();
            delayDisposes = null;
        }
    }

    @Override
    public boolean isPPTViewCanMove() {
        return false;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void sendWebMessage(String message) {
        if (polyvPPTWebView != null) {
            polyvPPTWebView.callUpdateWebView(message);
        }
    }

    @Override
    public void sendWebMessage(String event, String message) {
        polyvPPTWebView.callMessage(event,message);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PolyvCommonLog.e(TAG,"onTouchEvent");
        return super.onTouchEvent(event);
    }

    public void setLoadingViewVisible(int visible) {
        if (pptLoadingView != null) {
            pptLoadingView.setVisibility(visible);
        }
    }

    public void updateDelayTime(int delay_time){
        this.delayTime = delay_time;
    }

    public void resetDelayTime(){
        this.delayTime = DELAY_TIME;
    }

    public void updateBrushPermission(String message) {
        PolyvPPTAuthentic polyvPPTAuthentic = PolyvGsonUtil.fromJson(PolyvPPTAuthentic.class,message);
        polyvPPTWebView.setNeedGestureAction("1".equals(polyvPPTAuthentic.getStatus()));
    }
}
