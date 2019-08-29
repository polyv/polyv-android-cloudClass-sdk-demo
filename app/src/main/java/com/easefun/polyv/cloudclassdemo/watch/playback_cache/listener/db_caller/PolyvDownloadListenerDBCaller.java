package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.db_caller;

import com.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;
import com.easefun.polyv.cloudclass.model.PolyvPlaybackVO;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.IPolyvAsyncDBWrapper;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.PolyvPlaybackCacheDBManager;
import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

import java.util.List;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 描述一下方法的作用
 */
public class PolyvDownloadListenerDBCaller implements IPolyvDownloadListenerDBCaller {

    private IPolyvAsyncDBWrapper db;
    private String videoPoolId;

    public PolyvDownloadListenerDBCaller(String videoPoolId) {
        this.videoPoolId = videoPoolId;
        db = PolyvPlaybackCacheDBManager.getDB();
    }

    @Override
    public void callBeforeStart(PolyvCloudClassPlaybackCacheVO vo) {
        LogUtils.d("onBeforeStart");
        if (vo.getPlaybackVO() == null) {
            return;
        }
        PolyvPlaybackVO.DataBean data = vo.getPlaybackVO().getData();

        String videoPoolId = data.getVideoPoolId();
        String videoLiveId = data.getVideoId();
        boolean isNormal = data.getLiveType().equals("alone");
        String duration = data.getDuration();
        long fileSize = (long) data.getVideoCache().getVideoSize();
        String title = data.getTitle();
        String channelId = String.valueOf(data.getChannelId());
        String firstImageUrl = data.getFirstImage();

        //通过下载进度回调返回
        long percent = 0;
        long total = 0;

        //下载完成回调返回
        String videoPath = "";
        String jsPath = "";
        String pptPath = "";

        PolyvPlaybackCacheDBEntity entity = new PolyvPlaybackCacheDBEntity(
                videoPoolId,
                videoLiveId,
                isNormal,
                duration,
                fileSize,
                title,
                percent,
                total,
                PolyvCacheStatus.DOWNLOADING,
                videoPath,
                jsPath,
                pptPath,
                channelId,
                firstImageUrl
        );

        //查找数据库是否已经有该entity
        PolyvPlaybackCacheDBManager
                .getDB()
                .asyncNoResult(iPolyvPlaybackCacheDB -> {
                    List<PolyvPlaybackCacheDBEntity> entityList = iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId);
                    if (entityList.isEmpty()) {
                        LogUtils.d("插入entity" + entity);
                        iPolyvPlaybackCacheDB.insert(entity);
                    } else {
                        long percentOld = entityList.get(0).getPercent();
                        long totalOld = entityList.get(0).getTotal();
                        entity.setPercent(percentOld);
                        entity.setTotal(totalOld);
                        LogUtils.d("更新entity" + entity);
                        iPolyvPlaybackCacheDB.updateStatus(entity);
                    }
                });
    }

    @Override
    public void callOnProgress(long percent, long total) {
        db.asyncNoResult(iPolyvPlaybackCacheDB -> {
            List<PolyvPlaybackCacheDBEntity> entityList = iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId);
            if (!entityList.isEmpty()) {
                PolyvPlaybackCacheDBEntity cacheDBEntity = entityList.get(0);
                cacheDBEntity.setPercent(percent);
                cacheDBEntity.setTotal(total);
                iPolyvPlaybackCacheDB.updateProgress(cacheDBEntity);
            }
        });
    }

    @Override
    public void callOnStop() {
        LogUtils.d("onStop");
        setStatus(PolyvCacheStatus.PAUSED);
    }

    @Override
    public void callOnSuccess(PolyvCloudClassPlaybackCacheVO playbackCacheVO) {
        LogUtils.d("onSuccess");
        db.asyncNoResult(iPolyvPlaybackCacheDB -> {
            List<PolyvPlaybackCacheDBEntity> entityList = iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId);
            if (!entityList.isEmpty()) {
                PolyvPlaybackCacheDBEntity cacheDBEntity = entityList.get(0);
                cacheDBEntity.setPercent(cacheDBEntity.getTotal());
                cacheDBEntity.setStatus(PolyvCacheStatus.FINISHED);
                cacheDBEntity.setJsPath(playbackCacheVO.getJsPath());
                cacheDBEntity.setPptPath(playbackCacheVO.getPptDir());
                cacheDBEntity.setVideoPath(playbackCacheVO.getVideoPath());
                iPolyvPlaybackCacheDB.updateProgress(cacheDBEntity);
            }
        });
    }

    @Override
    public void callOnError() {
        LogUtils.d("onError");
        setStatus(PolyvCacheStatus.ERROR);
    }


    private void setStatus(PolyvCacheStatus status) {
        db.asyncNoResult(iPolyvPlaybackCacheDB -> {
            List<PolyvPlaybackCacheDBEntity> entityList=iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId);
            if (entityList.isEmpty()){
                return;
            }
            PolyvPlaybackCacheDBEntity entity = entityList.get(0);
            entity.setStatus(status);
            iPolyvPlaybackCacheDB.updateStatus(entity);
        });
    }
}
