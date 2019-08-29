package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_management;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.PolyvPlaybackCacheDBManager;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db.IPolyvPlaybackCacheDB;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

import java.util.ArrayList;
import java.util.List;

public class PolyvPlaybackCacheFragment extends Fragment {
    private static final String IS_FINISHED = "is_finished";

    private View view;
    private ListView lv_download;
    private List<PolyvPlaybackCacheDBEntity> downloadInfos;
    private PolyvDownloadListViewAdapter downloadAdapter;

    public static PolyvPlaybackCacheFragment create(boolean isFinished) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_FINISHED, isFinished);
        PolyvPlaybackCacheFragment fragment = new PolyvPlaybackCacheFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_download, null) : view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PolyvPlaybackCacheDBManager.getDB().asyncWithResult(IPolyvPlaybackCacheDB::getAll, this::initView);
    }

    /**
     * 初始化view
     *
     * @param dbPlaybackList 数据库中读取到的全部的数据，包括下载完成的未下载完成的。
     */
    private void initView(List<PolyvPlaybackCacheDBEntity> dbPlaybackList) {
        //true表示已缓存Fragment，false表示缓存中Fragment。
        boolean isFinished = getArguments().getBoolean(IS_FINISHED);

        lv_download = view.findViewById(R.id.lv_download);
        downloadInfos = new ArrayList<>();
        downloadInfos.addAll(getTask(dbPlaybackList, isFinished));
        downloadAdapter = new PolyvDownloadListViewAdapter(downloadInfos, getContext(), lv_download, isFinished);
        if (!isFinished) {
            downloadAdapter.setDownloadSuccessListener(new PolyvDownloadListViewAdapter.DownloadSuccessListener() {
                @Override
                public void onDownloadSuccess(PolyvPlaybackCacheDBEntity downloadInfo) {
                    if (getActivity()==null){
                        return;
                    }
                    ((PolyvPlaybackCacheManageActivity) getActivity()).getDownloadedFragment().addTask(downloadInfo);
                }
            });
        }
        lv_download.setAdapter(downloadAdapter);
        lv_download.setEmptyView(view.findViewById(R.id.iv_empty));
    }

    //缓存视频从 缓存中 --> 已缓存
    public void addTask(PolyvPlaybackCacheDBEntity downloadInfo) {
        downloadInfos.add(downloadInfo);
        downloadAdapter.notifyDataSetChanged();
    }

    /**
     * 获取缓存任务。
     * 已缓存fragment:获取那些已经下载完了的视频。
     * 缓存中fragment:获取那些还未下载完的视频。
     *
     * @param downloadInfos 从数据库中读取的下载信息列表
     * @param isFinished    根据当前是 已缓存 还是 缓存中 fragment来添加。
     * @return 筛选后的缓存任务。
     */
    private List<PolyvPlaybackCacheDBEntity> getTask(List<PolyvPlaybackCacheDBEntity> downloadInfos, boolean isFinished) {
        if (downloadInfos == null) {
            return null;
        }
        List<PolyvPlaybackCacheDBEntity> infos = new ArrayList<>();

        if (isFinished) {
            //当前是已缓存Fragment
            for (PolyvPlaybackCacheDBEntity cacheEntity : downloadInfos) {
                updateTaskStatusToFINISHED(cacheEntity);

                //如果下载完成，添加到数组中，稍后传递到adapter中展示
                if (isTaskDownloaded(cacheEntity)) {
                    infos.add(cacheEntity);
                }
            }
        } else {
            //当前是缓存中Fragment
            for (PolyvPlaybackCacheDBEntity cacheEntity : downloadInfos) {
                updateTaskStatusToFINISHED(cacheEntity);

                //如果没下载完，则添加到数组中，稍后传递到adapter中展示
                if (!isTaskDownloaded(cacheEntity)) {
                    infos.add(cacheEntity);
                }
            }
        }
        return infos;
    }

    //如果进度为100，但是下载状态不是FINISHED，则更新一下状态。
    private void updateTaskStatusToFINISHED(PolyvPlaybackCacheDBEntity cacheEntity) {
        long percent = cacheEntity.getPercent();
        long total = cacheEntity.getTotal();
        int progress = 0;
        if (total != 0) {
            progress = (int) (percent * 100 / total);
        }
        if (progress == 100 && cacheEntity.getStatus() != PolyvCacheStatus.FINISHED) {
            cacheEntity.setStatus(PolyvCacheStatus.FINISHED);
            PolyvPlaybackCacheDBManager.getDB()
                    .asyncNoResult(iPolyvPlaybackCacheDB -> iPolyvPlaybackCacheDB.updateStatus(cacheEntity));
        }
    }

    //任务是否下载完成
    private boolean isTaskDownloaded(PolyvPlaybackCacheDBEntity cacheEntity) {
        //计算已下载的进度
        long percent = cacheEntity.getPercent();
        long total = cacheEntity.getTotal();
        int progress = 0;
        if (total != 0) {
            progress = (int) (percent * 100 / total);
        }
        //该任务是否已经下载完成
        return progress == 100 || cacheEntity.getStatus() == PolyvCacheStatus.FINISHED;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadAdapter.releaseDownloadListener();
    }
}
