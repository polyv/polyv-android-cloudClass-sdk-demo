package com.easefun.polyv.cloudclassdemo.watch.chat.playback;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.model.PolyvPlaybackListVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvEmoListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.playback.widget.PolyvVideoListFooterView;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.indicator.IIndicator;

/**
 * 回放列表Fragment
 */
public class PolyvPlaybackListFragment extends Fragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //view
    private View view;
    //加载失败的提示view
    private TextView errorTipsTv;
    //刷新视图view
    private SmoothRefreshLayout smoothRefreshLy;
    //回放视频列表view
    private RecyclerView playbackListRv;
    private PolyvPlaybackListAdapter playbackListAdapter;

    //频道号
    private String channelId;
    //列表类型
    private int videoListType;

    private int page;//页数
    private static final int PAGE_SIZE = 20;//每页获取的数据条数

    private Disposable playbackListDisposable;
    private Disposable completionEventDisposable;
    private Disposable liveStatusDisposable;

    private CountDownTimer countDownTimer;//倒计时器

    private boolean isStartedLivePage;//是否跳转过直播页面，避免多次跳转

    private AlertDialog liveStartDialog;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期方法">
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.polyv_playback_list_fragment, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initParams();
        requestPlaybackListData(true);
        observeCompletionEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        isStartedLivePage = false;
        observeLiveStatus();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (liveStatusDisposable != null) {
            liveStatusDisposable.dispose();
        }
        if (liveStartDialog != null && liveStartDialog.isShowing()) {
            liveStartDialog.dismiss();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playbackListDisposable != null) {
            playbackListDisposable.dispose();
        }
        if (completionEventDisposable != null) {
            completionEventDisposable.dispose();
        }
        if (liveStatusDisposable != null) {
            liveStatusDisposable.dispose();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化view">
    private void initView() {
        //find
        errorTipsTv = view.findViewById(R.id.error_tips_tv);
        smoothRefreshLy = view.findViewById(R.id.smooth_refresh_ly);
        playbackListRv = view.findViewById(R.id.playback_list_rv);

        //init
        playbackListRv.setHasFixedSize(true);
        playbackListRv.setLayoutManager(new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false));
        playbackListRv.addItemDecoration(new PolyvEmoListAdapter.GridSpacingItemDecoration(2, ConvertUtils.dp2px(16), true));
        playbackListAdapter = new PolyvPlaybackListAdapter();
        playbackListAdapter.setOnViewActionListener(new PolyvPlaybackListAdapter.ViewActionListener() {
            @Override
            public void onItemClick(int position, PolyvPlaybackListVO.DataBean.ContentsBean contentsBean) {
                if (playActionListener != null) {
                    playActionListener.onPlayPlayback(contentsBean.getVideoPoolId(), contentsBean.isAlone());
                }
            }
        });
        playbackListRv.setAdapter(playbackListAdapter);

        ClassicHeader<IIndicator> headerView = new ClassicHeader<>(getContext());
        headerView.setTitleTextColor(Color.parseColor("#B2B2B2"));
        ClassicFooter<IIndicator> footerView = new ClassicFooter<>(getContext());
        footerView.setTitleTextColor(Color.parseColor("#B2B2B2"));
        smoothRefreshLy.setHeaderView(headerView);
        smoothRefreshLy.setFooterView(footerView);
        smoothRefreshLy.setOnRefreshListener(new SmoothRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefreshing() {
                errorTipsTv.setVisibility(View.GONE);
                requestPlaybackListData(true);
            }

            @Override
            public void onLoadingMore() {
                requestPlaybackListData(false);
            }
        });
        smoothRefreshLy.setDisableRefresh(true);//禁用header刷新
        smoothRefreshLy.setEnableOverScroll(false);//禁用滚动回弹
        smoothRefreshLy.setLoadingMinTime(50);//设置最小关闭刷新动画的时间
        smoothRefreshLy.setDurationToCloseFooter(0);//设置关闭footer的时间
        smoothRefreshLy.setEnableCompatSyncScroll(false);//关闭footer刷新时会显示下一个item
        smoothRefreshLy.setDisableLoadMoreWhenContentNotFull(true);//数据没满时关闭footer
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化页面参数">
    private void initParams() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        channelId = bundle.getString("channelId");
        videoListType = bundle.getInt("videoListType");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="加载回放列表数据">
    private void requestPlaybackListData(final boolean isFirstLoad) {
        if (isFirstLoad) {
            page = 1;
        }
        playbackListDisposable = PolyvChatApiRequestHelper.getInstance()
                .getPlaybackList(channelId, page, PAGE_SIZE, videoListType)
                .subscribe(new Consumer<PolyvPlaybackListVO>() {
                    @Override
                    public void accept(PolyvPlaybackListVO playbackListVO) throws Exception {
                        page++;
                        smoothRefreshLy.setDisableRefresh(true);
                        smoothRefreshLy.refreshComplete();
                        smoothRefreshLy.setDisableLoadMoreWhenContentNotFull(false);
                        if (playbackListVO.getData().isLastPage()) {
                            smoothRefreshLy.setMaxMoveRatioOfFooter(1);//footer可以移动的占比
                            smoothRefreshLy.setDisablePerformLoadMore(true);//不触发加载更多
                            smoothRefreshLy.setFooterView(new PolyvVideoListFooterView(getContext()));
                        }
                        playbackListAdapter.addDataNotify(playbackListVO.getData().getContents());
                        //第一次加载成功时使用第一个视频播放
                        if (isFirstLoad && playbackListAdapter.getItemCount() > 0) {
                            if (playActionListener != null) {
                                PolyvPlaybackListVO.DataBean.ContentsBean contentsBean = playbackListAdapter.getContentsBeanList().get(0);
                                playActionListener.onPlayPlayback(contentsBean.getVideoPoolId(), contentsBean.isAlone());
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PolyvCommonLog.exception(throwable);
                        if (isFirstLoad) {
                            errorTipsTv.setVisibility(View.VISIBLE);
                            smoothRefreshLy.setDisableRefresh(false);
                        }
                        smoothRefreshLy.refreshComplete();
                    }
                });

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="观察直播状态变化">
    private void observeLiveStatus() {
        if (liveStatusDisposable != null) {
            liveStatusDisposable.dispose();
        }
        liveStatusDisposable = Observable.interval(0, 6, TimeUnit.SECONDS, Schedulers.io())
                .flatMap(new Function<Long, ObservableSource<PolyvLiveStatusVO>>() {
                    @Override
                    public ObservableSource<PolyvLiveStatusVO> apply(Long aLong) throws Exception {
                        return PolyvApiManager.getPolyvLiveStatusApi().getLiveStatusJson2(channelId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PolyvLiveStatusVO>() {
                    @Override
                    public void accept(final PolyvLiveStatusVO polyvLiveStatusVO) throws Exception {
                        if (polyvLiveStatusVO.isLive()) {
                            if (liveStatusDisposable != null) {
                                liveStatusDisposable.dispose();
                            }

                            liveStartDialog = new AlertDialog.Builder(getContext())
                                    .setTitle("温馨提示")
                                    .setMessage("直播开始了，马上前往直播")
                                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (isStartedLivePage) {
                                                return;
                                            }
                                            isStartedLivePage = true;
                                            dialog.dismiss();
                                            if (countDownTimer != null) {
                                                countDownTimer.cancel();
                                            }
                                            if (playActionListener != null) {
                                                playActionListener.onLiveStart(polyvLiveStatusVO.isAlone());
                                            }
                                        }
                                    }).create();
                            liveStartDialog.setCancelable(false);
                            liveStartDialog.show();
                            final Button positiveButton = liveStartDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            positiveButton.setAllCaps(false);

                            if (countDownTimer == null) {
                                countDownTimer = new CountDownTimer(5000, 1000) {
                                    int timeSecond = 5;

                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        positiveButton.setText("知道了(" + (timeSecond--) + "s)");
                                    }

                                    @Override
                                    public void onFinish() {
                                        if (isStartedLivePage) {
                                            return;
                                        }
                                        isStartedLivePage = true;
                                        liveStartDialog.dismiss();
                                        if (playActionListener != null) {
                                            playActionListener.onLiveStart(polyvLiveStatusVO.isAlone());
                                        }
                                    }
                                };
                                countDownTimer.start();
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        PolyvCommonLog.exception(throwable);
                    }
                });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="观察播放完成事件">
    public static class CompletionEvent {
        public boolean checkNextVideoPlay;

        public CompletionEvent(boolean checkNextVideoPlay) {
            this.checkNextVideoPlay = checkNextVideoPlay;
        }
    }

    private void observeCompletionEvent() {
        completionEventDisposable = PolyvRxBus.get().toObservable(CompletionEvent.class).subscribe(new Consumer<CompletionEvent>() {
            @Override
            public void accept(CompletionEvent completionEvent) throws Exception {
                if (completionEvent.checkNextVideoPlay && playActionListener != null) {
                    List<PolyvPlaybackListVO.DataBean.ContentsBean> dataList = playbackListAdapter.getContentsBeanList();
                    if (playbackListAdapter.getSelPosition() < dataList.size() - 1) {
                        int nextPosition = playbackListAdapter.getSelPosition() + 1;
                        PolyvPlaybackListVO.DataBean.ContentsBean nextContentsBean = dataList.get(nextPosition);
                        playbackListAdapter.updateSelPosition(nextPosition);
                        playActionListener.onPlayPlayback(nextContentsBean.getVideoPoolId(), nextContentsBean.isAlone());
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="播放事件监听器">
    private PlayActionListener playActionListener;

    public void setPlayActionListener(PlayActionListener listener) {
        this.playActionListener = listener;
    }

    public interface PlayActionListener {
        void onPlayPlayback(String vid, boolean isNormalLivePlayBack);

        void onLiveStart(boolean isNormalLive);
    }
    // </editor-fold>
}
