package com.easefun.polyv.commonui.player.ppt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.easefun.polyv.commonui.PolyvCommonMediacontroller;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;
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
    // TODO: 2019/8/15 需要从缓冲数据里获取
    PolyvPlaybackCacheDBEntity data = null;

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

        polyvPptView = (PolyvPPTView) findViewById(R.id.polyv_ppt_view);
        pptContiner = (FrameLayout) findViewById(R.id.polyv_ppt_container);
        videoSubviewClose = (ImageView) findViewById(R.id.video_subview_close);
        videoSubviewClose.setOnClickListener(this);
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
        }
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


    protected boolean playLocalPPT() {

        if (data == null) {
            return false;
        }
        //如果本地没有ppt缓存 隐藏
        show(TextUtils.isEmpty(data.getJsPath()) ? INVISIBLE : VISIBLE);

        getPPTView().loadLocalFile(data.getJsPath(), data.getPptPath(), data.getVideoPoolId(), data.getVideoLiveId());
        return true;
    }

    private boolean hasVideoCaches(PolyvPlaybackCacheDBEntity entity) {
        // TODO: 2019/8/19 从数据库获取数据
        this.data  =entity;
        return data != null && data.getStatus() == PolyvCacheStatus.FINISHED;
    }

    public void loadWeb(PolyvPlaybackCacheDBEntity entity) {
        if(polyvPptView == null){
            return;
        }
        if(hasVideoCaches(entity)){
            playLocalPPT();
        }else {
            polyvPptView.loadWeb();
        }
    }
}
