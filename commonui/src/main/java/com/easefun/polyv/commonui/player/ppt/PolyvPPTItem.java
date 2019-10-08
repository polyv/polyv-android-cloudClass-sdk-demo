package com.easefun.polyv.commonui.player.ppt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;
import com.easefun.polyv.commonui.PolyvCommonMediacontroller;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;

/**
 * @author df
 * @create 2018/8/11
 * @Describe
 */
public class PolyvPPTItem<T extends PolyvCommonMediacontroller> extends
        FrameLayout implements View.OnClickListener, IPolyvPPTItem<T> {
    private static final String TAG = "PolyvPPTItem";
    private View rootView;
    private PolyvPPTView polyvPptView;
    private FrameLayout pptContiner;
    private ImageView videoSubviewClose;

    private T mediaController;
    private boolean hasClosed;
    private GestureDetector gestureDetector;

    public PolyvPPTItem(@NonNull Context context) {
        this(context, null);
    }

    public PolyvPPTItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PolyvPPTItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    private void initialView() {
        rootView = View.inflate(getContext(), R.layout.polyv_ppt_item_view, this);

        polyvPptView = findViewById(R.id.polyv_ppt_view);
        pptContiner = findViewById(R.id.polyv_ppt_container);
        videoSubviewClose = findViewById(R.id.video_subview_close);

//        pptContiner.setOnClickListener(this);
        videoSubviewClose.setOnClickListener(this);

        listenerSingleTap();
    }

    /**
     * 监听单击手势事件，消费事件后 同时将事件继续上抛给父类 进行手势处理
     */
    private void listenerSingleTap() {
        pptContiner.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                ((ViewGroup)pptContiner.getParent()).onTouchEvent(event);
                return true;
            }
        });
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                PolyvCommonLog.e(TAG,"onSingleTapUp");
                mediaController.changePPTVideoLocation();
                return true;
            }
        });
    }

    public void show(int show) {
        PolyvCommonLog.d(TAG,"show polyvPPTWebView:"+show);
        polyvPptView.polyvPPTWebView.setVisibility(show);
        polyvPptView.pptLoadingView.setVisibility(show == VISIBLE ? INVISIBLE : VISIBLE);
        if (hasClosed) {
            return;
        }
        ((ViewGroup) pptContiner.getParent()).setVisibility(show);
    }

    public void resetStatus() {
        hasClosed = false;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.video_subview_close) {
            hasClosed = true;
            hideSubView();
        }else if(id == R.id.polyv_ppt_container){
//            mediaController.changePPTVideoLocation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PolyvCommonLog.e(TAG,"onTouchEvent:"+event.getAction());
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void hideSubView() {
        ((ViewGroup) pptContiner.getParent()).setVisibility(INVISIBLE);
        mediaController.updateControllerWithCloseSubView();

    }

    @Override
    public PolyvPPTView getPPTView() {
        return polyvPptView;
    }

    @Override
    public void addMediaController(T mediaController) {
        this.mediaController = mediaController;
    }

    @Override
    public ViewGroup getItemRootView() {
        return (ViewGroup) rootView;
    }
}
