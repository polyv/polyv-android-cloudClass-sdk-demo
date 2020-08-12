package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_management;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassDownloaderManager;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;
import com.easefun.polyv.cloudclass.download.PolyvDownloadDirManager;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackListType;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassPlayErrorType;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.PolyvCloudClassHomeActivity;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.PolyvDemoDownloaderFactory;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.PolyvPlaybackCacheConfig;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.IPolyvAsyncDBWrapper;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.PolyvPlaybackCacheDBManager;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db.IPolyvPlaybackCacheDB;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry.IPolyvDownloaderListenerTagable;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry.IPolyvRegistryRelease;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry.PolyvGlobalDownloaderListenerKeeper;
import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ActivityUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.FileUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class PolyvDownloadListViewAdapter extends BaseSwipeAdapter {
    private static final String DOWNLOADED = "已下载";
    private static final String DOWNLOADING = "正在下载";
    private static final String PAUSEED = "暂停下载";
    private static final String WAITED = "等待下载";
    private static final String DOWNLOAD_ERROR = "下载出错";

    private IPolyvAsyncDBWrapper downloadSQLiteHelper;
    private Context context;
    private ListView lv_download;
    private List<PolyvPlaybackCacheDBEntity> lists;
    private ViewHolder viewHolder;

    private Set<IPolyvRegistryRelease> downloaderRegistryReleasableSet = new HashSet<>();

    private DownloadSuccessListener downloadSuccessListener;

    private boolean isFinished;

    PolyvDownloadListViewAdapter(List<PolyvPlaybackCacheDBEntity> lists, Context context, ListView lv_download, boolean isFinished) {
        this.lists = lists;
        this.context = context;
        this.isFinished = isFinished;
        this.lv_download = lv_download;
        downloadSQLiteHelper = PolyvPlaybackCacheDBManager.getDB();
        init();
    }

    private void init() {
        if (isFinished) {
            return;
        }

        List<PolyvCloudClassDownloader> downloadingList = new ArrayList<>(lists.size());
        List<PolyvCloudClassDownloader> waitingList = new ArrayList<>(lists.size());

        for (int i = 0; i < lists.size(); i++) {
            PolyvPlaybackCacheDBEntity cacheEntity = lists.get(i);
            String videoPoolId = cacheEntity.getVideoPoolId();

            PolyvCloudClassDownloader downloader = PolyvDemoDownloaderFactory
                    .getDownloaderAndSetGlobalListener(videoPoolId);
            IPolyvDownloaderListenerTagable registry = PolyvGlobalDownloaderListenerKeeper
                    .getInstance()
                    .getRegistryOrAddIfNull(videoPoolId, downloader);

            //设置内存监听器，实时更新list中的cacheEntity
            registry.addBeforeStartListener(new MemoryUpdaterOnBeforeStartListener(cacheEntity));
            registry.addDownloadListener(new MemoryUpdaterOnDownloadListener(cacheEntity));

            downloaderRegistryReleasableSet.add(registry);

            switch (cacheEntity.getStatus()) {
                case DOWNLOADING:
                    downloadingList.add(downloader);
                    break;
                case WAITING:
                    waitingList.add(downloader);
            }
        }

        //先启动上次是下载中的任务
        for (PolyvCloudClassDownloader downloader : downloadingList) {
            PolyvDemoDownloaderFactory.startDownload(downloader);
        }
        //再启动上次是等待中的任务
        for (PolyvCloudClassDownloader downloader : waitingList) {
            PolyvDemoDownloaderFactory.startDownload(downloader);
        }
    }

    private void loadImage(String firstImageUrl, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(firstImageUrl)
                .placeholder(R.drawable.polyv_playback_cache_video_placeholder)
                .into(imageView);
    }

    /**
     * 删除任务
     */
    private void deleteTask(int position) {
        PolyvPlaybackCacheDBEntity cacheEntity = lists.remove(position);
        String videoPoolId = cacheEntity.getVideoPoolId();
        String channelId = cacheEntity.getChannelId();
        //移除下载任务
        IPolyvCloudClassDownloader downloader = PolyvCloudClassDownloaderManager
                .getInstance()
                .clearPolyvDownload(new IPolyvCloudClassDownloader.Builder(videoPoolId, channelId));
        //删除下载文件
        String downloadRootDir = PolyvDownloadDirManager.getRootDir(videoPoolId, PolyvPlaybackCacheConfig.get().getDownloadRootPath());
        boolean deleteSuccess = FileUtils.deleteAllInDir(downloadRootDir);
        LogUtils.d("删除回放缓存" + deleteSuccess + " " + downloadRootDir);
        //移除数据库的下载信息
        downloadSQLiteHelper.asyncNoResult(iPolyvPlaybackCache -> iPolyvPlaybackCache.delete(cacheEntity));
        notifyDataSetChanged();
    }

    // <editor-fold defaultstate="collapsed" desc="Adapter方法">
    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.sl_download;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.polyv_listview_download_item, null);
        SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewWithTag("ll_delete"));

        viewHolder = new ViewHolder();
        viewHolder.ivFirstImage = view.findViewById(R.id.iv_first_image);
        viewHolder.fl_start = view.findViewById(R.id.fl_start);
        viewHolder.iv_start = view.findViewById(R.id.iv_start);
        viewHolder.tv_seri = view.findViewById(R.id.tv_seri);
        viewHolder.tv_size = view.findViewById(R.id.tv_size);
        viewHolder.tv_speed = view.findViewById(R.id.tv_speed);
        viewHolder.tv_status = view.findViewById(R.id.tv_status);
        viewHolder.tv_title = view.findViewById(R.id.tv_title);
        viewHolder.pb_progress = view.findViewById(R.id.pb_progress);
        viewHolder.tv_delete = view.findViewById(R.id.tv_delete);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        viewHolder = (ViewHolder) convertView.getTag();

        PolyvPlaybackCacheDBEntity cacheEntity = lists.get(position);
        String videoPoolId = cacheEntity.getVideoPoolId();
        long percent = cacheEntity.getPercent();
        long total = cacheEntity.getTotal();
        String title = cacheEntity.getTitle();
        long fileSize = cacheEntity.getFilesize();

        String firstImageUrl = cacheEntity.getFirstImageUrl();

        loadImage(firstImageUrl, viewHolder.ivFirstImage);

        // 已下载的百分比
        int progress = 0;
        if (total != 0) {
            progress = (int) (percent * 100 / total);
        }

        viewHolder.pb_progress.setVisibility(View.VISIBLE);
        viewHolder.tv_speed.setVisibility(View.VISIBLE);
        viewHolder.tv_status.setSelected(false);

        //下载任务序号textView
        String positionText;
        if (position + 1 < 10) {
            positionText = "0" + (position + 1);
        } else {
            positionText = "" + (position + 1);
        }
        viewHolder.tv_seri.setText(positionText);
        viewHolder.tv_title.setText(title);
        viewHolder.tv_size.setText(Formatter.formatFileSize(context, fileSize));
        viewHolder.pb_progress.setProgress(progress);

        switch (cacheEntity.getStatus()) {

            case FINISHED:
                viewHolder.iv_start.setImageResource(R.drawable.polyv_btn_play);
                viewHolder.tv_status.setText(DOWNLOADED);
                viewHolder.pb_progress.setVisibility(View.GONE);
                viewHolder.tv_speed.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                String speed = "0.00B/S";
                viewHolder.iv_start.setImageResource(R.drawable.polyv_btn_dlpause);
                viewHolder.tv_status.setText(DOWNLOADING);
                viewHolder.tv_speed.setText(speed);
                break;
            case PAUSED:
                viewHolder.iv_start.setImageResource(R.drawable.polyv_btn_download);
                viewHolder.tv_status.setText(PAUSEED);
                viewHolder.tv_status.setSelected(true);
                viewHolder.tv_speed.setText(Formatter.formatFileSize(context, fileSize * progress / 100));
                break;
            case ERROR:
                viewHolder.iv_start.setImageResource(R.drawable.polyv_btn_download);
                viewHolder.tv_status.setText(DOWNLOAD_ERROR);
                viewHolder.tv_status.setSelected(true);
                break;
            case WAITING:
                viewHolder.iv_start.setImageResource(R.drawable.polyv_btn_download);
                viewHolder.tv_status.setText(WAITED);
                viewHolder.tv_status.setSelected(true);
                viewHolder.tv_speed.setText(Formatter.formatFileSize(context, fileSize * progress / 100));
                break;
        }

        //设置点击监听器
        viewHolder.fl_start.setOnClickListener(new DownloadOnClickListener(cacheEntity, viewHolder, viewHolder.iv_start, viewHolder.tv_status, viewHolder.tv_speed, position));
        viewHolder.tv_delete.setOnClickListener(v -> {
            ((SwipeLayout) lv_download.getChildAt(position - lv_download.getFirstVisiblePosition())).close(false);
            deleteTask(position);
        });

        if (cacheEntity.getStatus() != PolyvCacheStatus.FINISHED) {
            //从内存中获取下载器
            PolyvCloudClassDownloader downloader = PolyvCloudClassDownloaderManager
                    .getInstance()
                    .getPolyvDownloader(new IPolyvCloudClassDownloader.Builder(videoPoolId, cacheEntity.getChannelId()));
            if (downloader != null) {
                viewHolder.setDownloadListener(downloader, cacheEntity, position, lists, downloadSuccessListener);
            }
//            if (downloader == null) {
//                LogUtils.d("重建下载器\n" + downloader);
//                downloader = PolyvDemoDownloaderFactory.getDownloaderAndSetGlobalListener(videoPoolId);
//            }
        }
    }
    // </editor-fold>

    void setDownloadSuccessListener(DownloadSuccessListener downloadSuccessListener) {
        this.downloadSuccessListener = downloadSuccessListener;
    }

    private static void showPauseSpeeView(PolyvPlaybackCacheDBEntity downloadInfo, TextView tv_speed) {
        long percent = downloadInfo.getPercent();
        long total = downloadInfo.getTotal();
        int progress = 0;
        if (total != 0)
            progress = (int) (percent * 100 / total);
        // 已下载的文件大小
        long downloaded = downloadInfo.getFilesize() * progress / 100;
        tv_speed.setText(Formatter.formatFileSize(Utils.getApp(), downloaded));
    }

    //点击，更新下载状态
    private static void updateDownloadStatusByOnClick(PolyvCacheStatus targetStatus, PolyvPlaybackCacheDBEntity entity) {
        entity.setStatus(targetStatus);
        PolyvPlaybackCacheDBManager.getDB()
                .asyncNoResult(iPolyvPlaybackCache -> iPolyvPlaybackCache.updateStatus(entity));
        LogUtils.d("更新entity" + entity);
    }

    //释放监听器
    void releaseDownloadListener() {
        for (IPolyvRegistryRelease registryRelease : downloaderRegistryReleasableSet) {
            registryRelease.releaseListener();
        }
        downloadSuccessListener = null;
    }


    //////////////////////////////////
    // 内部类
    //////////////////////////////////

    // <editor-fold defaultstate="collapsed" desc="ViewHolder">
    private class ViewHolder {
        FrameLayout fl_start;
        ImageView iv_start, ivFirstImage;
        TextView tv_seri, tv_title, tv_size, tv_status, tv_speed, tv_delete;
        ProgressBar pb_progress;

        private static final String TAG_BEFORE_START = "before_start_listener";
        private static final String TAG_DOWNLOAD = "download_listener";
        private static final String TAG_SPEED = "speed_listener";

        void setDownloadListener(PolyvCloudClassDownloader downloader, final PolyvPlaybackCacheDBEntity downloadInfo, final int position, List<PolyvPlaybackCacheDBEntity> lists, DownloadSuccessListener downloadSuccessListener) {
            //获取downloader对应的全局监听器
            IPolyvDownloaderListenerTagable registry = PolyvGlobalDownloaderListenerKeeper.getInstance()
                    .getRegistryOrAddIfNull(downloadInfo.getVideoPoolId(), downloader);

            registry.removeBeforeStartListener(TAG_BEFORE_START);
            registry.removeDownloadListener(TAG_DOWNLOAD);
            registry.removeSpeedListener(TAG_SPEED);

            //设置监听器
            registry.addBeforeStartListener(TAG_BEFORE_START, new ItemUiOnBeforeStartListener(this, position));
            registry.addDownloadListener(TAG_DOWNLOAD, new ItemUiOnDownloadListener(lv_download, this, downloadInfo, position, lists, downloadSuccessListener));
            registry.addSpeedListener(TAG_SPEED, new ItemUiOnDownloadSpeedListener(lv_download, this, downloader, downloadInfo, position));

            //捕捉监听器的释放器
            downloaderRegistryReleasableSet.add(registry);
        }
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件监听器">

    private class DownloadOnClickListener implements View.OnClickListener {
        private PolyvPlaybackCacheDBEntity downloadInfo;
        private ImageView iv_start;
        private TextView tv_status;
        private TextView tv_speed;
        private int position;
        private ViewHolder viewHolder;

        DownloadOnClickListener(PolyvPlaybackCacheDBEntity downloadInfo, ViewHolder viewHolder, ImageView iv_start, TextView tv_status, TextView tv_speed, int position) {
            this.downloadInfo = downloadInfo;
            this.iv_start = iv_start;
            this.tv_status = tv_status;
            this.tv_speed = tv_speed;
            this.position = position;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            LogUtils.i("点击了\n" + downloadInfo);

            String videoPoolId = downloadInfo.getVideoPoolId();
            String channelId = downloadInfo.getChannelId();

            switch (downloadInfo.getStatus()) {
                case FINISHED:
                    PolyvCloudClassHomeActivity.startActivityForPlayBack(
                            ActivityUtils.getTopActivity(),
                            videoPoolId,
                            channelId,
                            PolyvPlaybackCacheConfig.get().getUserId(),
                            downloadInfo.isNormal(),
                            PolyvPlaybackListType.PLAYBACK,
                            false
                    );
                    break;
                case PAUSED:
                case ERROR:
                    //更新下载任务状态
                    updateDownloadStatusByOnClick(PolyvCacheStatus.WAITING, downloadInfo);

                    //开始下载
                    PolyvCloudClassDownloader downloader = PolyvDemoDownloaderFactory
                            .getDownloaderAndSetGlobalListener(videoPoolId);

                    IPolyvDownloaderListenerTagable registry = PolyvGlobalDownloaderListenerKeeper
                            .getInstance()
                            .getRegistryOrAddIfNull(videoPoolId, downloader);

                    downloaderRegistryReleasableSet.add(registry);

                    //设置内存监听器，实时更新list中的cacheEntity
                    registry.addBeforeStartListener(new MemoryUpdaterOnBeforeStartListener(downloadInfo));
                    registry.addDownloadListener(new MemoryUpdaterOnDownloadListener(downloadInfo));

                    //设置ui监听器
                    this.viewHolder.setDownloadListener(downloader, downloadInfo, position, lists, downloadSuccessListener);

                    PolyvDemoDownloaderFactory.startDownload(downloader);

                    iv_start.setImageResource(R.drawable.polyv_btn_dlpause);
                    tv_status.setText(WAITED);
                    tv_status.setSelected(true);
                    break;
                case WAITING:
                case DOWNLOADING:
                    //更新下载任务状态
                    updateDownloadStatusByOnClick(PolyvCacheStatus.PAUSED, downloadInfo);
                    //停止下载
                    PolyvCloudClassDownloaderManager.getInstance()
                            .removeDownloader(new IPolyvCloudClassDownloader.Builder(
                                    videoPoolId,
                                    channelId
                            ));

                    tv_status.setText(PAUSEED);
                    tv_status.setSelected(true);
                    iv_start.setImageResource(R.drawable.polyv_btn_download);
                    showPauseSpeeView(downloadInfo, tv_speed);
                    break;
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ItemUi更新器：下载进度、下载成功、下载失败监听">
    private static class ItemUiOnDownloadListener implements IPolyvCloudClassDownloaderListener {
        private WeakReference<ListView> wr_lv_download;
        private WeakReference<ViewHolder> viewHolder;
        private PolyvPlaybackCacheDBEntity downloadInfo;
        private List<PolyvPlaybackCacheDBEntity> lists;
        private int position;
        private DownloadSuccessListener downloadSuccessListener;
        private boolean isStarted = false;

        ItemUiOnDownloadListener(ListView lv_download, ViewHolder viewHolder, PolyvPlaybackCacheDBEntity downloadInfo, int position, List<PolyvPlaybackCacheDBEntity> lists, DownloadSuccessListener downloadSuccessListener) {
            this.wr_lv_download = new WeakReference<>(lv_download);
            this.viewHolder = new WeakReference<>(viewHolder);
            this.downloadInfo = downloadInfo;
            this.position = position;
            this.lists = lists;
            this.downloadSuccessListener = downloadSuccessListener;
        }

        private boolean canUpdateView() {
            ListView lv_download = wr_lv_download.get();
            return lv_download != null && viewHolder.get() != null && lv_download.getChildAt(position - lv_download.getFirstVisiblePosition()) != null;
        }

        @Override
        public void onProgress(long current, long total) {
            if (canUpdateView()) {
                // 已下载的百分比
                int progress = (int) (current * 100 / total);
                viewHolder.get().pb_progress.setProgress(progress);

                if (!isStarted) {
                    isStarted = true;
                    viewHolder.get().iv_start.setImageResource(R.drawable.polyv_btn_dlpause);
                    viewHolder.get().tv_status.setText(DOWNLOADING);
                    viewHolder.get().tv_status.setSelected(false);
                }
            }
        }

        @Override
        public void onSuccess(PolyvCloudClassPlaybackCacheVO playbackCacheVO) {
            if (canUpdateView()) {
                viewHolder.get().tv_status.setText(DOWNLOADED);
                viewHolder.get().tv_status.setSelected(false);
                viewHolder.get().iv_start.setImageResource(R.drawable.polyv_btn_play);
                viewHolder.get().pb_progress.setVisibility(View.GONE);
                viewHolder.get().tv_speed.setVisibility(View.GONE);

                ToastUtils.showShort("第" + (position + 1) + "个任务下载成功");
            }
        }

        @Override
        public void onFailure(@PolyvCloudClassPlayErrorType.PlayCloudClassErrorReason int errorReason) {
            if (canUpdateView()) {
                viewHolder.get().tv_status.setText(DOWNLOAD_ERROR);
                viewHolder.get().tv_status.setSelected(true);//setSelected(true)就是变红色
                viewHolder.get().iv_start.setImageResource(R.drawable.polyv_btn_download);
                showPauseSpeeView(downloadInfo, viewHolder.get().tv_speed);
//                String message = "第" + (position + 1) + "个任务";

                ToastUtils.showLong(String.valueOf(errorReason));
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ItemUi更新器：下载开始前的监听器">

    private class ItemUiOnBeforeStartListener implements IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO> {

        private int pos;
        private ViewHolder viewHolder;

        ItemUiOnBeforeStartListener(ViewHolder viewHolder, int pos) {
            this.pos = pos;
            this.viewHolder = viewHolder;
        }

        private boolean canUpdateView() {
            return lv_download.getChildAt(pos - lv_download.getFirstVisiblePosition()) != null;
        }

        @Override
        public boolean onBeforeStart(IPolyvCloudClassDownloader downloader, PolyvCloudClassPlaybackCacheVO polyvCloudClassPlaybackCacheVO) {

            if (canUpdateView()) {
                viewHolder.tv_status.setText(DOWNLOADING);
                viewHolder.tv_status.setSelected(false);
            }
            return true;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ItemUi更新器：下载速度监听">
    private static class ItemUiOnDownloadSpeedListener implements IPolyvCloudClassDownloaderSpeedListener {
        private WeakReference<ListView> wr_lv_download;
        private WeakReference<ViewHolder> viewHolder;
        private IPolyvCloudClassDownloader downloader;
        private PolyvPlaybackCacheDBEntity cacheEntity;
        private int position;

        ItemUiOnDownloadSpeedListener(ListView lv_download, ViewHolder viewHolder, IPolyvCloudClassDownloader downloader, PolyvPlaybackCacheDBEntity cacheEntity, int position) {
            this.wr_lv_download = new WeakReference<>(lv_download);
            this.viewHolder = new WeakReference<>(viewHolder);
            this.downloader = downloader;
            this.cacheEntity = cacheEntity;
            this.position = position;
        }

        private boolean canUpdateView() {
            ListView lv_download = wr_lv_download.get();
            return lv_download != null && viewHolder.get() != null && lv_download.getChildAt(position - lv_download.getFirstVisiblePosition()) != null;
        }

        @Override
        public void onSpeed(int speed) {
//            LogUtils.i("速度=" + speed);
            if (canUpdateView() && downloader.isDownloading()) {
                String speedText = Formatter.formatShortFileSize(Utils.getApp(), speed) + "/S";
                viewHolder.get().tv_speed.setText(speedText);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内存实体更新器：下载进度、下载成功、下载失败监听">

    /**
     * 下载监听器
     * 用于更新内存中的缓存实体。
     */
    private class MemoryUpdaterOnDownloadListener implements IPolyvCloudClassDownloaderListener {
        private long total;
        private PolyvPlaybackCacheDBEntity cacheDBEntity;

        MemoryUpdaterOnDownloadListener(PolyvPlaybackCacheDBEntity cacheDBEntity) {
            this.cacheDBEntity = cacheDBEntity;
        }

        @Override
        public void onProgress(long current, long total) {
            this.total = total;
            cacheDBEntity.setPercent(current);
            cacheDBEntity.setTotal(total);
        }

        @Override
        public void onSuccess(PolyvCloudClassPlaybackCacheVO playbackCacheVO) {
            if (total == 0)
                total = 1;
            cacheDBEntity.setPercent(total);
            cacheDBEntity.setTotal(total);
            cacheDBEntity.setJsPath(playbackCacheVO.getJsPath());
            cacheDBEntity.setPptPath(playbackCacheVO.getPptDir());
            cacheDBEntity.setVideoPath(playbackCacheVO.getVideoPath());
            cacheDBEntity.setStatus(PolyvCacheStatus.FINISHED);

            lists.remove(cacheDBEntity);
            notifyDataSetChanged();
            if (downloadSuccessListener != null) {
                downloadSuccessListener.onDownloadSuccess(cacheDBEntity);
            }
        }

        @Override
        public void onFailure(int errorReason) {
            cacheDBEntity.setStatus(PolyvCacheStatus.ERROR);
            notifyDataSetChanged();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内存实体更新器：下载开始前的监听器">

    /**
     * 下载监听器
     * 用于更新内存中的缓存实体
     */
    private class MemoryUpdaterOnBeforeStartListener implements IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO> {
        private PolyvPlaybackCacheDBEntity cacheDBEntity;


        MemoryUpdaterOnBeforeStartListener(PolyvPlaybackCacheDBEntity cacheDBEntity) {
            this.cacheDBEntity = cacheDBEntity;
        }

        @Override
        public boolean onBeforeStart(IPolyvCloudClassDownloader downloader, PolyvCloudClassPlaybackCacheVO polyvCloudClassPlaybackCacheVO) {
            PolyvPlaybackCacheDBManager.getDB().asyncWithResult(new Function<IPolyvPlaybackCacheDB, PolyvPlaybackCacheDBEntity>() {
                @Override
                public PolyvPlaybackCacheDBEntity apply(IPolyvPlaybackCacheDB iPolyvPlaybackCacheDB) throws Exception {
                    return iPolyvPlaybackCacheDB.getByVideoPoolId(polyvCloudClassPlaybackCacheVO.getPlaybackVO().getData().getVideoPoolId()).get(0);
                }
            }, new Consumer<PolyvPlaybackCacheDBEntity>() {
                @Override
                public void accept(PolyvPlaybackCacheDBEntity entity) throws Exception {
                    cacheDBEntity.setFilesize(entity.getFilesize());
                    cacheDBEntity.setStatus(entity.getStatus());
                    notifyDataSetChanged();
                }
            });
            return false;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="与Activity的通信协议">
    public interface DownloadSuccessListener {
        void onDownloadSuccess(PolyvPlaybackCacheDBEntity downloadInfo);
    }
// </editor-fold>
}
