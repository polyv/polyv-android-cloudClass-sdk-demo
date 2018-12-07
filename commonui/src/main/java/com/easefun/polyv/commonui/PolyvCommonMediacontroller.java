package com.easefun.polyv.commonui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.businesssdk.model.video.PolyvBitrateVO;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.commonui.player.adapter.PolyvBitRateAdapter;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import java.util.List;
import java.util.ListIterator;

/**
 * @author df
 * @create 2018/8/16
 * @Describe 公共的控制类
 */
public abstract class PolyvCommonMediacontroller<T extends PolyvCommonVideoView> extends FrameLayout
        implements IPolyvMediaController<T>, View.OnClickListener {

    private static final String TAG = "PolyvCommonMediacontoller";
    protected View rootView, parentView;
    protected TextView bitrateChange;
    protected TextView bitrateChangeLand;
    //控制栏显示的时间
    protected static final int SHOW_TIME = 5000;
    protected boolean showPPTSubView = true;//ppt显示在副屏
    protected RelativeLayout videoControllerPort;
    protected RelativeLayout videoControllerLand;
    protected Activity context;
    protected T polyvVideoView;

    //清晰度选择view
    protected RelativeLayout liveControllerBottom;
    protected LinearLayout bitrateLayout;
    protected RecyclerView bitrateListPort;
    protected PolyvBitRateAdapter polyvBitAdapter;
    protected volatile int currentBitratePos;
    protected boolean hasData;
    protected PolyvBitrateVO polyvLiveBitrateVO;
    protected ListIterator<PolyvDefinitionVO> iterator;
    protected ImageView videoBack;

    private ViewGroup contentView, fullVideoViewParent;
    private ViewGroup.LayoutParams portraitLP;//(需要移动的整个播放器布局)在竖屏下的LayoutParams
    private ViewGroup fullVideoView;//需要移动的整个播放器布局
    private static final String landTag = "land";
    private static final String portraitTag = "portrait";

    private Runnable hideTask = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    public PolyvCommonMediacontroller(@NonNull Context context) {
        this(context, null);
    }

    public PolyvCommonMediacontroller(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvCommonMediacontroller(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    protected abstract void initialView();


    /**
     * 切换主副平
     */
    public abstract void changePPTVideoLocation();

    protected void initialBitrate() {
        bitrateLayout = findViewById(R.id.bitrate_layout);
        bitrateListPort = findViewById(R.id.bitrate_List);
        bitrateChange = findViewById(R.id.bitrate_change);
        bitrateChangeLand = findViewById(R.id.bitrate_change_land);
        bitrateLayout.setOnClickListener(this);
        bitrateChange.setOnClickListener(this);
        bitrateChangeLand.setOnClickListener(this);
    }

    @Override
    public void setMediaPlayer(T player) {

        polyvVideoView = player;
    }

    @Override
    public T getMediaPlayer() {
        return polyvVideoView;
    }


    @Override
    public void changeToLandscape() {
        if(PolyvScreenUtils.isLandscape(context)){
            return;
        }
        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        hide();
    }

    public void setLandscapeController() {
        post(new Runnable() {
            @Override
            public void run() {

//        // 通过移除整个播放器布局到窗口的上层布局中，并改变整个播放器布局的大小，实现全屏的播放器。
//        if (fullVideoView == null) {
//            fullVideoView = ((ViewGroup) polyvVideoView.getParent().getParent());
//            fullVideoViewParent = (ViewGroup) fullVideoView.getParent();
//            contentView = (ViewGroup) context.findViewById(Window.ID_ANDROID_CONTENT);
//        }
//        if (!landTag.equals(fullVideoView.getTag())) {
//            fullVideoView.setTag(landTag);
//            portraitLP = fullVideoView.getLayoutParams();
//            fullVideoViewParent.removeView(fullVideoView);
//            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//            fullVideoView.setLayoutParams(flp);
//            contentView.addView(fullVideoView);
//        }

                ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
                vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;

                videoControllerPort.setVisibility(View.GONE);
                videoControllerLand.setVisibility(View.VISIBLE);
            }
        });



    }

    @Override
    public void changeToPortrait() {
       if(PolyvScreenUtils.isPortrait(context)){
           return;
       }
        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        hide();

    }

    @Override
    public void initialConfig(ViewGroup view) {
        parentView = view;
        changeToPortrait();

    }

    private void setPortraitController() {

        post(new Runnable() {
            @Override
            public void run() {

                ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
                vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                vlp.height = PolyvScreenUtils.getHeight();

                videoControllerLand.setVisibility(View.GONE);
                videoControllerPort.setVisibility(View.VISIBLE);
            }
        });

    }


    @Override
    public void hide() {
        setVisibility(View.GONE);
        bitrateLayout.setVisibility(GONE);
    }


    @Override
    public boolean isShowing() {
        return isShown();
    }


    @Override
    public void show() {
        show(SHOW_TIME);
    }

    @Override
    public void show(int timeout) {
        setVisibility(VISIBLE);
        if(getHandler() != null){
            getHandler().removeCallbacks(hideTask);
        }
        postDelayed(hideTask, timeout);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        PolyvCommonLog.d(TAG,"onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            PolyvScreenUtils.hideStatusBar(context);
            setLandscapeController();
            resetAdapter(true);
        } else {
            PolyvScreenUtils.showStatusBar(context);
            setPortraitController();
            resetAdapter(false);
        }
    }

    //信息获取完成以后刷新列表
    public void initBitList(PolyvBitrateVO polyvLiveBitrateVO) {
        if (polyvLiveBitrateVO == null) {
            return;
        }
        try {
            bitrateChange.setVisibility(VISIBLE);
            bitrateChangeLand.setVisibility(VISIBLE);
            this.polyvLiveBitrateVO = polyvLiveBitrateVO;
            if (polyvBitAdapter == null && polyvLiveBitrateVO.getDefinitions() != null) {
                polyvBitAdapter = new PolyvBitRateAdapter(polyvLiveBitrateVO, getContext());
                List<PolyvDefinitionVO> definitions = polyvLiveBitrateVO.getDefinitions();
                bitrateListPort.setLayoutManager(new GridLayoutManager(getContext(),
                        !definitions.isEmpty() ? definitions.size() : 3));
                bitrateListPort.setAdapter(polyvBitAdapter);
                polyvBitAdapter.setOnClickListener(this);
                bitrateChange.setText(polyvLiveBitrateVO.getDefaultDefinition());
                bitrateChangeLand.setText(polyvLiveBitrateVO.getDefaultDefinition());

            } else {
                polyvBitAdapter.updateBitrates(polyvLiveBitrateVO);
            }

            locateDefaultDefPos();

            polyvBitAdapter.notifyDataSetChanged();
            hasData = true;

        } catch (Exception e) {
            PolyvCommonLog.exception(e);
        }

    }

    private void locateDefaultDefPos() {

        if (polyvLiveBitrateVO == null || polyvLiveBitrateVO.getDefinitions() == null) {
            return;
        }

        //找到默认分辨率得位置
        if (iterator == null) {
            iterator = polyvLiveBitrateVO.getDefinitions().listIterator();
            while (iterator.hasNext()) {
                PolyvDefinitionVO definitionVO = iterator.next();
                if (definitionVO.definition != null && definitionVO.definition.equals(polyvLiveBitrateVO.getDefaultDefinition())) {
                    definitionVO.hasSelected = true;
                    currentBitratePos = iterator.nextIndex() - 1;
                    break;
                }
            }
        }


        PolyvDefinitionVO definitionVO = polyvLiveBitrateVO.getDefinitions().get(currentBitratePos);
        definitionVO.hasSelected = true;
    }

    private void resetAdapter(boolean port) {
        bitrateLayout.setVisibility(GONE);
        if (!hasData) {
            return;
        }
        if (polyvLiveBitrateVO != null) {
            polyvBitAdapter = new PolyvBitRateAdapter(polyvLiveBitrateVO, getContext());
            polyvBitAdapter.setOnClickListener(this);
            bitrateListPort.setLayoutManager(port ? new LinearLayoutManager(getContext())
                    : new GridLayoutManager(getContext(), polyvLiveBitrateVO.getDefinitions().size()));
        }

        if (bitrateListPort != null) {
            bitrateListPort.setAdapter(polyvBitAdapter);
        }
    }

    public void showBitrate(boolean port) {

        ViewGroup.LayoutParams layoutParams = bitrateLayout.getLayoutParams();
        if (port) {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            bitrateLayout.setVisibility(VISIBLE);
        } else {
            layoutParams.width = PolyvScreenUtils.dip2px(getContext(), 200);
            visibleWithAnimation(bitrateLayout);
        }
    }

    private boolean setLiveBit(View view) {

        try {
            int pos = (int) view.getTag();
            if (currentBitratePos == pos) {
                return false;
            }
            currentBitratePos = pos;
            polyvBitAdapter.notifyDataSetChanged();

            if (polyvVideoView != null) {
                polyvVideoView.changeBitRate(pos);
            }
        } catch (Exception e) {
            PolyvCommonLog.e(TAG, "setLiveBit: ");
        }

        return true;
    }

    protected void goneWithAnimation(View view) {
        view.setVisibility(View.GONE);
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.polyv_ltor_right));
    }

    protected void visibleWithAnimation(View view) {
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.polyv_rtol_right));
    }

    private void updateBitSelectedView(String content) {
        bitrateChangeLand.setText(content);
        bitrateChange.setText(content);
        polyvBitAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.live_bit_name) {
            if (setLiveBit(view)) {
                updateBitSelectedView(((TextView) view).getText().toString());
            }

            bitrateLayout.setVisibility(GONE);

        } else if (id == R.id.bitrate_change) {
            showBitrate(true);

        } else if (id == R.id.bitrate_change_land) {

            showBitrate(false);

        }else if(id == R.id.bitrate_layout){
            hideBitrate();
        }
    }

    private void hideBitrate() {
        bitrateLayout.setVisibility(INVISIBLE);
    }

    @Override
    public void initialBitrate(PolyvBitrateVO bitrateVO) {
        initBitList(bitrateVO);
    }

    public abstract void updatePPTShowStatus(boolean showPPT);
}
