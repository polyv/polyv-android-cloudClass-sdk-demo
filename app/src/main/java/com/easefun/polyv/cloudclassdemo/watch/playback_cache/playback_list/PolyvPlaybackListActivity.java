package com.easefun.polyv.cloudclassdemo.watch.playback_cache.playback_list;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.model.playback.PolyvPlaybackListVO;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.PolyvDemoDownloaderFactory;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.PolyvPlaybackCacheConfig;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_management.PolyvPlaybackCacheManageActivity;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.PolyvPlaybackCacheDBManager;
import com.easefun.polyv.commonui.adapter.PolyvHeaderViewRecyclerAdapter;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019/8/13 0013
 *
 * @author hwj
 * description 回放列表Activity
 */
public class PolyvPlaybackListActivity extends PolyvBaseActivity {
    //extra
    private static final String EXTRA_USER_ID = "extra_user_id";

    //extra value
    private String userId;

    //View
    private ImageView ivBack;
    private RecyclerView rvPlayback;
    private View loadMoreView;
    private ImageView ivCacheManagement;

    private PolyvHeaderViewRecyclerAdapter adapterFooter;

    //列表分页下标
    private int pageNum = 1;
    //每次请求的page中包含多少个item。
    private static final int PAGE_SIZE = 20;

    //数据仓库
    private IPolyvPlaybackListRepo dataRepo = new PolyvPlaybackListRepo();
    //列表数据
    private List<PolyvPlaybackListVO.ContentsBean> playbackList = new ArrayList<>();


    public static void launch(Activity activity, String userId) {
        boolean isGranted = PolyvPermissionManager.with(activity)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .opstrs(-1)
                .meanings("存储权限")
                .request();
        if (!isGranted) {
            ToastUtils.showShort("请打开存储权限");
            return;
        }

        Intent intent = new Intent(activity, PolyvPlaybackListActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_playback_list);

        getIntentExtra();
        findViewById();
        setOnClickListener();
        setAdapter();
        loadMore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataRepo.destroy();
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_USER_ID);
    }

    private void findViewById() {
        ivBack = findView(R.id.iv_back);
        rvPlayback = findView(R.id.rv_playback);
        ivCacheManagement = findView(R.id.iv_cache_management);
        //如果账号没有缓存权限，则隐藏缓存管理按钮。
        if (!PolyvPlaybackCacheConfig.get().isCacheEnabled()){
            ivCacheManagement.setVisibility(View.INVISIBLE);
        }
    }

    private void setOnClickListener() {
        ivBack.setOnClickListener(v -> finish());
        ivCacheManagement.setOnClickListener(v -> PolyvPlaybackCacheManageActivity.launch(this, PolyvPlaybackCacheConfig.get().getChannelId()));
    }

    private void setAdapter() {
        //创建回放列表适配器
        RvPlaybackListAdapter adapterList = new RvPlaybackListAdapter(
                this, playbackList,
                this::clickDownload,
                this::clickPlay);

        //创建FootView适配器
        adapterFooter = new PolyvHeaderViewRecyclerAdapter(adapterList);

        //给列表设置适配器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvPlayback.setLayoutManager(linearLayoutManager);
        rvPlayback.setAdapter(adapterFooter);
        rvPlayback.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                loadMore();
            }
        });

        //创建FootView对象
        loadMoreView = LayoutInflater.from(this).inflate(R.layout.polyv_bottom_loadmorelayout, rvPlayback, false);
        loadMoreView.setVisibility(View.GONE);
        adapterFooter.addFooterView(loadMoreView);
    }


    private void loadMore() {
        loadMoreView.setVisibility(View.VISIBLE);
        dataRepo.getPlaybackList(PolyvPlaybackCacheConfig.get().getChannelId(), pageNum, PAGE_SIZE,
                polyvPlaybackListVO -> {
                    pageNum++;
                    loadMoreView.setVisibility(View.GONE);
                    if (polyvPlaybackListVO == null) {
                        adapterFooter.removeFootView();
                        return;
                    }
                    if (polyvPlaybackListVO.getPageNumber() == polyvPlaybackListVO.getTotalPages()) {
                        adapterFooter.removeFootView();
                    }
                    playbackList.addAll(polyvPlaybackListVO.getContents());
                    adapterFooter.notifyDataSetChanged();
                },
                throwable -> showErrorTips(throwable.getMessage()));
    }

    //点击播放
    private void clickPlay(PolyvPlaybackListVO.ContentsBean bean) {
        String videoPoolId = bean.getVideoPoolId();
        PolyvPlaybackCacheDBManager.getDB()
                .asyncNoResult(iPolyvPlaybackCacheDB -> {
                    List<PolyvPlaybackCacheDBEntity> entityList = iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId);
                    boolean isCacheEnabled = bean.isCacheEnalbed();
                    boolean isCacheEntityDownloaded = false;
                    if (!entityList.isEmpty()) {
                        isCacheEntityDownloaded = entityList.get(0).getStatus() == PolyvCacheStatus.FINISHED;
                    }
                    boolean canCache = isCacheEnabled && !isCacheEntityDownloaded;
                    PolyvCloudClassHomeActivity.startActivityForPlayBack(this, bean.getVideoPoolId(), PolyvPlaybackCacheConfig.get().getChannelId(), userId, bean.isNormal(), canCache);
                });
    }

    //点击缓存
    private void clickDownload(PolyvPlaybackListVO.ContentsBean bean) {
        dataRepo.isVideoLiveIdExitInDB(bean.getVideoPoolId(), entityList -> {
            if (entityList.isEmpty()) {
                String videoPoolId = bean.getVideoPoolId();
                String downloadRootDir = PolyvPlaybackCacheConfig.get().getDownloadRootPath();

                //如果下载目录创建失败，则不开启下载任务，返回。
                if (!createDownloadRootDir(downloadRootDir)) {
                    return;
                }

                File downloadDirFile = new File(downloadRootDir);

                PolyvCloudClassDownloader downloader = PolyvDemoDownloaderFactory.getDownloaderAndSetGlobalListener(videoPoolId);

                PolyvDemoDownloaderFactory.startDownload(downloader);

                insertToDB(bean);
                ToastUtils.showShort("下载任务添加成功");
            } else {
                PolyvPlaybackCacheDBEntity entity = entityList.get(0);
                PolyvCacheStatus status = entity.getStatus();
                switch (status) {
                    case FINISHED:
                        ToastUtils.showShort("下载任务已完成");
                        break;
                    default:
                        ToastUtils.showShort("下载任务正在缓存中");
                        break;
                }
            }
        });
    }

    //插入数据库
    private void insertToDB(PolyvPlaybackListVO.ContentsBean bean) {
        String videoPoolId = bean.getVideoPoolId();
        String videoLiveId = bean.getVideoId();
        boolean isNormal = bean.isNormal();
        String duration = bean.getDuration();
        long fileSize = 0;
        String title = bean.getTitle();
        //通过下载进度回调返回
        long percent = 0;
        long total = 0;
        //下载完成回调返回
        String videoPath = "";
        String pptPath = "";
        String jsPath = "";
        String channelId = String.valueOf(bean.getChannelId());

        PolyvPlaybackCacheDBEntity entity = new PolyvPlaybackCacheDBEntity(
                videoPoolId,
                videoLiveId,
                isNormal,
                duration,
                fileSize,
                title,
                percent,
                total,
                PolyvCacheStatus.WAITING,
                videoPath,
                jsPath,
                pptPath,
                channelId,
                bean.getFirstImage()
        );
        PolyvPlaybackCacheDBManager.getDB().asyncNoResult(iPolyvPlaybackCache -> {
            LogUtils.d("插入entity" + entity);
            iPolyvPlaybackCache.insert(entity);
        });
    }

    //递归创建下载根路径。
    private boolean createDownloadRootDir(String downloadRootDir) {
        boolean isMkdirSuccess = FileUtils.createOrExistsDir(downloadRootDir);
        if (!isMkdirSuccess) {
            LogUtils.e("无法创建目录：" + downloadRootDir);
        }
        return isMkdirSuccess;
    }

    private void showErrorTips(String errorMsg) {
        ToastUtils.showShort(errorMsg);
    }


    // <editor-fold defaultstate="collapsed" desc="内部类">

    ///////////////////////
    //回放列表适配器
    ///////////////////////
    private static class RvPlaybackListAdapter extends RecyclerView.Adapter<RvPlaybackListAdapter.ItemViewHolder> {
        private Activity activity;
        private OnClickDownLoadListener onClickDownLoadListener;
        private OnClickPlayListener onClickPlayListener;
        private List<PolyvPlaybackListVO.ContentsBean> playbackList;

        RvPlaybackListAdapter(Activity activity, List<PolyvPlaybackListVO.ContentsBean> playbackList, OnClickDownLoadListener onClickDownLoadListener, OnClickPlayListener onClickPlayListener) {
            this.activity = activity;
            this.onClickDownLoadListener = onClickDownLoadListener;
            this.onClickPlayListener = onClickPlayListener;
            this.playbackList = playbackList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.polyv_rv_playback_list_item, parent, false);
            return new ItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            PolyvPlaybackListVO.ContentsBean video = playbackList.get(position);

            String title = video.getTitle();
            String duration = video.getDuration();
            String imageUrl = video.getFirstImage();
            //有的url没有协议，加上http协议
            if (!imageUrl.startsWith("http")) {
                imageUrl = "http:" + imageUrl;
            }

            holder.tv_title.setText(title);
            holder.tv_time.setText(duration);
            holder.tv_download.setOnClickListener(v -> {
                if (onClickDownLoadListener != null) {
                    onClickDownLoadListener.onClick(video);
                }
            });
            holder.tv_play.setOnClickListener(v -> {
                if (onClickPlayListener != null) {
                    onClickPlayListener.onClick(video);
                }
            });

            //如果不能缓存，则隐藏下载按钮。
            if (PolyvPlaybackCacheConfig.get().isCacheEnabled() && video.isCacheEnalbed()) {
                LogUtils.d("账号是否有缓存权限：" + PolyvPlaybackCacheConfig.get().isCacheEnabled());
                LogUtils.d("视频是否有缓存权限" + video.isCacheEnalbed() + " \n" + video);
                holder.tv_download.setVisibility(View.VISIBLE);
            } else {
                holder.tv_download.setVisibility(View.INVISIBLE);
            }

            Glide.with(activity)
                    .load(imageUrl)
                    .placeholder(R.drawable.polyv_playback_cache_video_placeholder)
                    .into(holder.iv_cover);
        }

        @Override
        public int getItemCount() {
            return playbackList.size();
        }

        private static class ItemViewHolder extends RecyclerView.ViewHolder {
            // 封面图
            ImageView iv_cover;
            // 标题，时间，下载按钮，播放按钮
            TextView tv_title, tv_time, tv_download, tv_play;

            ItemViewHolder(View itemView) {
                super(itemView);
                iv_cover = itemView.findViewById(R.id.iv_cover);
                tv_title = itemView.findViewById(R.id.tv_title);
                tv_time = itemView.findViewById(R.id.tv_time);
                tv_download = itemView.findViewById(R.id.tv_download);
                tv_play = itemView.findViewById(R.id.tv_play);
            }
        }

        //点击下载
        public interface OnClickDownLoadListener {
            void onClick(PolyvPlaybackListVO.ContentsBean bean);
        }

        //点击播放
        public interface OnClickPlayListener {
            void onClick(PolyvPlaybackListVO.ContentsBean bean);
        }
    }

    ///////////////////////
    //OnScrollListener
    ///////////////////////
    public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        private int previousTotal = 0;

        private boolean loading = true;

        private int currentPage = 1;

        private LinearLayoutManager mLinearLayoutManager;


        protected EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {

            this.mLinearLayoutManager = linearLayoutManager;
        }


        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            super.onScrolled(recyclerView, dx, dy);

            if (dy == 0) {
                return;
            }

            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = mLinearLayoutManager.getItemCount();
            int lastCompletelyVisiableItemPosition
                    = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }

            if (!loading && (visibleItemCount > 0) &&
                    (lastCompletelyVisiableItemPosition >= totalItemCount - 1)) {
                currentPage++;
                onLoadMore(currentPage);
                loading = true;
            }
        }


        public void refresh() {

            loading = true;
            previousTotal = 0;
            currentPage = 1;
        }


        public abstract void onLoadMore(int currentPage);
    }
    // </editor-fold>
}
