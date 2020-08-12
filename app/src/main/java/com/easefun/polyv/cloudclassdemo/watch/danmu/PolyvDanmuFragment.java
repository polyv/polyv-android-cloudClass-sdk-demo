package com.easefun.polyv.cloudclassdemo.watch.danmu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.cloudclassdemo.R;

import java.util.HashMap;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * 弹幕Fragment
 */
public class PolyvDanmuFragment extends Fragment {
    private static boolean status_canauto_resume = true;
    private static boolean status_pause_fromuser = true;
    // danmuLayoutView
    private View view;
    private IDanmakuView mDanmakuView;
    private BaseDanmakuParser mParser;
    private DanmakuContext mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_cloudclass_danmu, container, false);
        return view;
    }

    private void findIdAndNew() {
        mDanmakuView = (IDanmakuView) view.findViewById(R.id.dv_danmaku);
        mDanmakuView.hide();
    }

    private void initView() {
        //-------------------仅对加载的弹幕有效-------------------//
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 2); // 滚动弹幕最大显示5行
        maxLinesPair.put(BaseDanmaku.TYPE_FIX_TOP, 2);
        maxLinesPair.put(BaseDanmaku.TYPE_FIX_BOTTOM, 2);
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
        //--------------------------------------------------------//

        mContext = DanmakuContext.create();
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f).setScaleTextSize(1.0f)
                // 图文混排使用SpannedCacheStuffer
                .setCacheStuffer(new SpannedCacheStuffer(), null)
                .setMaximumLines(maxLinesPair).preventOverlapping(overlappingEnablePair);
        mDanmakuView.showFPS(false);

        mDanmakuView.enableDanmakuDrawingCache(false);

        mDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                mDanmakuView.start();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        mDanmakuView.prepare(mParser = new BaseDanmakuParser() {
            @Override
            protected IDanmakus parse() {
                return new Danmakus();
            }
        }, mContext);
    }

    //隐藏
    public void hide() {
        if (mDanmakuView != null) {
            mDanmakuView.hide();
        }
    }

    //显示
    public void show() {
        if (mDanmakuView != null) {
            mDanmakuView.show();
        }
    }

    //暂停
    public void pause() {
        pause(true);
    }

    public void pause(boolean fromuser) {
        if (!fromuser)
            status_pause_fromuser = false;
        else
            status_canauto_resume = false;
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    //恢复
    public void resume() {
        resume(true);
    }

    public void resume(boolean fromuser) {
        if (status_pause_fromuser && fromuser || (!status_pause_fromuser && !fromuser)) {
            if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
                if (!status_pause_fromuser) {
                    status_pause_fromuser = true;
                    if (status_canauto_resume)
                        mDanmakuView.resume();
                } else {
                    status_canauto_resume = true;
                    mDanmakuView.resume();
                }
            }
        }
    }

    //发送
    public void sendDanmaku(CharSequence message) {
        if (mContext == null) {
            return;
        }
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = message;
        danmaku.padding = 0;
        danmaku.priority = 1; // 一定会显示, 一般用于本机发送的弹幕
//        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 100);
//        18f * (mParser.getDisplayer().getDensity() - 0.6f)
        danmaku.textSize = getResources().getDimension(R.dimen.danmaku_tv_textsize);
        danmaku.textColor = Color.WHITE;
//        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
//        danmaku.underlineColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);
    }

    //释放
    public void release() {
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findIdAndNew();
        initView();
    }

    @Override
    public void onDestroy() {
        //26 support调用后context为null，多次调用会有异常
        super.onDestroy();
        release();
        mContext = null;
    }
}
