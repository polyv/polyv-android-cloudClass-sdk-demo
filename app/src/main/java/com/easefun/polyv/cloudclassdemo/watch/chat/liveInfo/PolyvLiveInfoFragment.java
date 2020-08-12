package com.easefun.polyv.cloudclassdemo.watch.chat.liveInfo;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.feature.liveinfo.PolyvLiveInfoDataSource;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.foundationsdk.config.PolyvPlayOption;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


public class PolyvLiveInfoFragment extends Fragment {
    //argument
    public static final String ARGUMENT_VIEWER_ID = "arg_viewer_id";
    public static final String ARGUMENT_CLASS_DETAIL = "classDetail";
    public static final String ARGUMENT_PLAY_MODE = "playMode";
    public static final String ARGUMENT_CLASS_DETAIL_ITEM = "classDetailItem";

    //观看状态
    public static final String WATCH_STATUS_LIVE = "live";
    public static final String WATCH_STATUS_PLAYBACK = "playback";
    public static final String WATCH_STATUS_WAITING = "waiting";
    public static final String WATCH_STATUS_END = "end";

    //View
    private View view;
    private PolyvSafeWebView wv_desc;
    private RelativeLayout rl_parent;
    private TextView tv_viewer;
    private TextView tv_title;
    private ImageView iv_livecover;
    private TextView tv_publisher;
    private TextView tv_likes;
    TextView tv_starttime;
    TextView tv_status;

    //状态变量
    private int viewerCount = 0;

    //直播属性
    private int channelId;
    private String viewerId = "";
    private int playMode;
    private String liveInfoWebContent = "";
    private PolyvLiveClassDetailVO classDetailEntity;

    //Disposable
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PolyvLiveInfoDataSource liveInfoDataSource;

    // <editor-fold defaultstate="collapsed" desc="Fragment方法">

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_liveintroduce, container, false) : view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findAllView();
        initView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fetchDataFromArgument();
        observeLoginEvent();
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
        dispose(compositeDisposable);
        liveInfoDataSource.destroy();
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
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private void findAllView() {
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        iv_livecover = (ImageView) view.findViewById(R.id.iv_livecover);
        tv_publisher = (TextView) view.findViewById(R.id.tv_publisher);
        tv_viewer = (TextView) view.findViewById(R.id.tv_viewer);
        tv_likes = (TextView) view.findViewById(R.id.tv_likes);
        tv_starttime = (TextView) view.findViewById(R.id.tv_starttime);
        tv_status = (TextView) view.findViewById(R.id.tv_status);
    }

    private void initView() {
        //填充数据
        tv_title.setText(classDetailEntity.getData().getName());
        PolyvImageLoader.getInstance().loadImage(getContext(), classDetailEntity.getData().getCoverImage(), iv_livecover);
        tv_publisher.setText(TextUtils.isEmpty(classDetailEntity.getData().getPublisher()) ? "主持人" : classDetailEntity.getData().getPublisher());
        tv_likes.setText(String.valueOf(classDetailEntity.getData().getLikes()));
        refreshViewerTv(viewerCount);
        String liveTime = "直播时间：" + ((classDetailEntity.getData().getStartTime() == null) ? "无" : classDetailEntity.getData().getStartTime());
        tv_starttime.setText(liveTime);

        //若是直播，要监听直播状态的改变
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

        //web的直播介绍
        if (TextUtils.isEmpty(liveInfoWebContent)) {
            return;
        }
        String style = "style=\" width:100%;\"";
        liveInfoWebContent = liveInfoWebContent.replaceAll("img src=\"//", "img src=\\\"https://");
        liveInfoWebContent = liveInfoWebContent.replace("<img ", "<img " + style + " ");
        liveInfoWebContent = liveInfoWebContent.replaceAll("<p>", "<p style=\"word-break:break-all\">");
        liveInfoWebContent = liveInfoWebContent.replaceAll("<table>", "<table border='1' rules=all>");
        liveInfoWebContent = liveInfoWebContent.replaceAll("<td>", "<td width=\"36\">");
        liveInfoWebContent= "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "        <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                "        <title>Document</title>\n" +
                "</head>\n" +
                "<body>" +
                liveInfoWebContent + "</body>\n" +
                "</html>";
//        content = "<a href=\"https://live.polyv.cn/watch/110827\" target=\"_blank\" style>https://live.polyv.cn/watch/110827</a>&nbsp;<p><br></p><p><a href=\"https://www.baidu.com\" target=\"_blank\" style>www.baidu.com</a><br></p>";
        if (!TextUtils.isEmpty(liveInfoWebContent)) {
            if (wv_desc == null) {
                if (view != null) {
                    rl_parent = (RelativeLayout) view.findViewById(R.id.rl_parent);
                    wv_desc = new PolyvSafeWebView(getContext());
                    RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-1, -1);
                    rlp.addRule(RelativeLayout.BELOW, R.id.rl_top);
                    wv_desc.setLayoutParams(rlp);
                    rl_parent.addView(wv_desc);
                    PolyvWebViewHelper.initWebView(getContext(), wv_desc);
                    wv_desc.loadData(liveInfoWebContent, "text/html; charset=UTF-8", null);
                }
            } else {
                if (rl_parent != null) {
                    rl_parent.addView(wv_desc);
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化Data">
    private void fetchDataFromArgument() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        classDetailEntity = (PolyvLiveClassDetailVO) getArguments().getSerializable(ARGUMENT_CLASS_DETAIL);
        if (classDetailEntity == null) {
            return;
        }
        playMode = getArguments().getInt(ARGUMENT_PLAY_MODE);
        viewerId = getArguments().getString(ARGUMENT_VIEWER_ID);
        viewerCount = classDetailEntity.getData().getPageView();
        channelId = classDetailEntity.getData().getChannelId();
        PolyvLiveClassDetailVO.DataBean.ChannelMenusBean channelMenusBean = (PolyvLiveClassDetailVO.DataBean.ChannelMenusBean) getArguments().getSerializable(ARGUMENT_CLASS_DETAIL_ITEM);
        if (channelMenusBean != null) {
            liveInfoWebContent = channelMenusBean.getContent();
        }
    }


    //监听登录事件
    private void observeLoginEvent() {
        liveInfoDataSource = new PolyvLiveInfoDataSource(channelId, viewerId);
        liveInfoDataSource.observePageViewer(new Action() {
            @Override
            public void run() throws Exception {
                refreshViewerTv(++viewerCount);
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="刷新View">
    private void refreshViewerTv(int viewerCount) {
        String viewerCountText;
        if (viewerCount > 10000) {
            viewerCountText = String.format(Locale.CHINA, "%.1f", (double) viewerCount / 10000) + "w";
        } else {
            viewerCountText = String.valueOf(viewerCount);
        }
        tv_viewer.setText(viewerCountText);
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
    // </editor-fold>

    private void dispose(Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
}

