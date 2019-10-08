package com.easefun.polyv.cloudclassdemo.watch.chat.liveInfo;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;


public class PolyvLiveInfoFragment extends Fragment {
    private boolean isInitialized;
    private View view;
    private PolyvSafeWebView wv_desc;
    private RelativeLayout rl_parent;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //观看状态
    public static final String WATCH_STATUS_LIVE = "live";
    public static final String WATCH_STATUS_PLAYBACK = "playback";
    public static final String WATCH_STATUS_WAITING = "waiting";
    public static final String WATCH_STATUS_END = "end";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_liveintroduce, null) : view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isInitialized) {
            isInitialized = true;
            initView();
        }
    }

    private void initView() {
        PolyvLiveClassDetailVO classDetailEntity = (PolyvLiveClassDetailVO) getArguments().getSerializable("classDetail");
        int playMode = getArguments().getInt("playMode");

        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(classDetailEntity.getData().getName());

        final ImageView iv_livecover = (ImageView) view.findViewById(R.id.iv_livecover);
        PolyvImageLoader.getInstance()
                .loadImage(getContext(), classDetailEntity.getData().getCoverImage(), iv_livecover);

        TextView tv_publisher = (TextView) view.findViewById(R.id.tv_publisher);
        tv_publisher.setText(TextUtils.isEmpty(classDetailEntity.getData().getPublisher()) ? "主持人" : classDetailEntity.getData().getPublisher());

        TextView tv_viewer = (TextView) view.findViewById(R.id.tv_viewer);
        tv_viewer.setText(classDetailEntity.getData().getPageView() + "");

        TextView tv_likes = (TextView) view.findViewById(R.id.tv_likes);
        tv_likes.setText(classDetailEntity.getData().getLikes() + "");

        TextView tv_starttime = (TextView) view.findViewById(R.id.tv_starttime);
        tv_starttime.setText("直播时间：" + ((classDetailEntity.getData().getStartTime() == null) ? "无" : classDetailEntity.getData().getStartTime()));

        final TextView tv_status = (TextView) view.findViewById(R.id.tv_status);
        if (playMode == PolyvPlayOption.PLAYMODE_VOD) {
            tv_status.setVisibility(View.GONE);
        } else {
            String watchStatus = classDetailEntity.getData().getWatchStatus();
            updateWatchStatus(tv_status, watchStatus);

            compositeDisposable.add(PolyvRxBus.get()
                    .toObservable(PolyvLiveClassDetailVO.DataBean.class)
                    .subscribe(new Consumer<PolyvLiveClassDetailVO.DataBean>() {
                        @Override
                        public void accept(PolyvLiveClassDetailVO.DataBean dataBean) throws Exception {
                            PolyvLiveInfoFragment.this.updateWatchStatus(tv_status, dataBean.getWatchStatus());
                        }
                    }));
        }

        PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean = (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean) getArguments().getSerializable("classDetailItem");
        String content = channelMenusBean.getContent();
        if (TextUtils.isEmpty(content)){
            return;
        }
        String style = "style=\" width:100%;\"";
        content = content.replaceAll("img src=\"//", "img src=\\\"http://");
        content = content.replace("<img ", "<img " + style + " ");
//        content = "<a href=\"https://live.polyv.cn/watch/110827\" target=\"_blank\" style>https://live.polyv.cn/watch/110827</a>&nbsp;<p><br></p><p><a href=\"https://www.baidu.com\" target=\"_blank\" style>www.baidu.com</a><br></p>";
        if (!TextUtils.isEmpty(content)) {
            if (wv_desc == null) {
                if (view != null) {
                    rl_parent = (RelativeLayout) view.findViewById(R.id.rl_parent);
                    wv_desc = new PolyvSafeWebView(getContext());
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-1, -1);
                    rlp.addRule(RelativeLayout.BELOW, R.id.rl_top);
                    wv_desc.setLayoutParams(rlp);
                    rl_parent.addView(wv_desc);
                    PolyvWebViewHelper.initWebView(getContext(), wv_desc);
                    wv_desc.loadData(content, "text/html; charset=UTF-8", null);
                }
            } else {
                if (rl_parent != null) {
                    rl_parent.addView(wv_desc);
                }
            }
        }

    }

    private void updateWatchStatus(TextView tv_status, String watchStatus) {
        if (TextUtils.isEmpty(watchStatus)) {
            return;
        }
        tv_status.setText(getWatchStatus(watchStatus));
        if (WATCH_STATUS_LIVE.equals(watchStatus)) {
            tv_status.setTextColor(getContext().getResources().getColor(R.color.text_red));
            tv_status.setBackgroundResource(R.drawable.polyv_live_status_live);

        } else if (WATCH_STATUS_PLAYBACK.equals(watchStatus)
                || WATCH_STATUS_END.equals(watchStatus)) {
            tv_status.setTextColor(getContext().getResources().getColor(R.color.text_gray));
            tv_status.setBackgroundResource(R.drawable.polyv_live_status_noactive);
        } else {
            tv_status.setTextColor(getContext().getResources().getColor(R.color.text_green));
            tv_status.setBackgroundResource(R.drawable.polyv_live_status_waitting);
        }
    }

    private String getWatchStatus(String watchStatus) {
        String status = "暂无直播";
        if (WATCH_STATUS_LIVE.equals(watchStatus)) {
            status = "直播中";
        } else if (WATCH_STATUS_PLAYBACK.equals(watchStatus)) {
            status = "暂无直播";
        } else if (WATCH_STATUS_END.equals(watchStatus)) {
            status = "暂无直播";
        } else if (WATCH_STATUS_WAITING.equals(watchStatus)) {
            status = "等待中";
        }
        return status;
    }

    public PolyvSafeWebView getWebView() {
        return wv_desc;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wv_desc != null) {
            wv_desc.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wv_desc != null) {
            wv_desc.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        if (rl_parent != null) {
            rl_parent.removeView(wv_desc);
        }
        if (wv_desc != null) {
            wv_desc.stopLoading();
            wv_desc.clearMatches();
            wv_desc.clearHistory();
            wv_desc.clearSslPreferences();
            wv_desc.clearCache(true);
            wv_desc.loadUrl("about:blank");
            wv_desc.removeAllViews();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                wv_desc.removeJavascriptInterface("AndroidNative");
            }
            wv_desc.destroy();
        }
        wv_desc = null;
    }
}

