package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderStopListener;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.db_caller.IPolyvDownloadListenerDBCaller;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 数据库调用装饰器
 */
class PolyvDownloaderListenerDBCallDecorator implements IPolyvDownloaderListenerRegistry {

    private IPolyvDownloaderListenerRegistry registry;
    private IPolyvDownloadListenerDBCaller dbCaller;


    PolyvDownloaderListenerDBCallDecorator(IPolyvDownloaderListenerRegistry registry,
                                           IPolyvDownloadListenerDBCaller dbCaller) {
        this.registry = registry;
        this.dbCaller = dbCaller;
    }

    @Override
    public void onProgress(long current, long total) {
        dbCaller.callOnProgress(current, total);
        registry.onProgress(current, total);
    }

    @Override
    public void onSuccess(PolyvCloudClassPlaybackCacheVO playbackCacheVO) {
        dbCaller.callOnSuccess(playbackCacheVO);
        registry.onSuccess(playbackCacheVO);
    }

    @Override
    public void onFailure(int errorReason) {
        dbCaller.callOnError();
        registry.onFailure(errorReason);
    }

    @Override
    public boolean onBeforeStart(IPolyvCloudClassDownloader downloader, PolyvCloudClassPlaybackCacheVO vo) {
        dbCaller.callBeforeStart(vo);
        boolean abort = registry.onBeforeStart(downloader, vo);
        return abort;
    }

    @Override
    public void onStop() {
        dbCaller.callOnStop();
        registry.onStop();
    }

    @Override
    public void addBeforeStartListener(IPolyvCloudClassDownloaderBeforeStartListener li) {
        registry.addBeforeStartListener(li);
    }

    @Override
    public void addSpeedListener(IPolyvCloudClassDownloaderSpeedListener li) {
        registry.addSpeedListener(li);
    }

    @Override
    public void addDownloadListener(IPolyvCloudClassDownloaderListener li) {
        registry.addDownloadListener(li);
    }

    @Override
    public void addStopListener(IPolyvCloudClassDownloaderStopListener li) {
        registry.addStopListener(li);
    }

    @Override
    public void onSpeed(int speed) {
        registry.onSpeed(speed);
    }

    @Override
    public void releaseListener() {
        registry.releaseListener();
    }
}
