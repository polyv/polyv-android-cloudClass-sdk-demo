package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderStopListener;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 下载器监听器注册处默认实现
 */
class PolyvDownloaderListenerRegistry implements
        IPolyvDownloaderListenerRegistry {

    private List<IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO>> beforeStartListeners = new ArrayList<>();
    private List<IPolyvCloudClassDownloaderSpeedListener> speedListeners = new ArrayList<>();
    private List<IPolyvCloudClassDownloaderListener> downloaderListeners = new ArrayList<>();
    private List<IPolyvCloudClassDownloaderStopListener> stopListeners = new ArrayList<>();

    private IPolyvCloudClassDownloader downloader;

    @Override
    public boolean onBeforeStart(IPolyvCloudClassDownloader downloader, PolyvCloudClassPlaybackCacheVO polyvCloudClassPlaybackCacheVO) {
        if (polyvCloudClassPlaybackCacheVO.getPlaybackVO() == null) {
            return false;
        }
        this.downloader = downloader;

        boolean abort = false;
        for (IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO> li : beforeStartListeners) {
            if (li.onBeforeStart(downloader, polyvCloudClassPlaybackCacheVO)) {
                abort = true;
            }
        }
        return abort;
    }

    @Override
    public void onStop() {
        for (IPolyvCloudClassDownloaderStopListener li : stopListeners) {
            li.onStop();
        }
        releaseAllListener();

        //如果下载器未开始，就未赋值过，就是null的。
        if (downloader != null) {
            PolyvGlobalDownloaderListenerKeeper
                    .getInstance()
                    .removeRegistry(downloader);
        }
    }

    @Override
    public void onSpeed(int speed) {
        for (IPolyvCloudClassDownloaderSpeedListener li : speedListeners) {
            li.onSpeed(speed);
        }
    }

    @Override
    public void onProgress(long current, long total) {
        for (IPolyvCloudClassDownloaderListener li : downloaderListeners) {
            li.onProgress(current, total);
        }
    }

    @Override
    public void onSuccess(PolyvCloudClassPlaybackCacheVO playbackCacheVO) {
        for (IPolyvCloudClassDownloaderListener li : downloaderListeners) {
            li.onSuccess(playbackCacheVO);
        }
        releaseAllListener();
        PolyvGlobalDownloaderListenerKeeper
                .getInstance()
                .removeRegistry(downloader);
    }

    @Override
    public void onFailure(int errorReason) {
        for (IPolyvCloudClassDownloaderListener li : downloaderListeners) {
            li.onFailure(errorReason);
        }
    }

    @Override
    public void addBeforeStartListener(IPolyvCloudClassDownloaderBeforeStartListener li) {
        this.beforeStartListeners.add(li);
    }

    @Override
    public void addSpeedListener(IPolyvCloudClassDownloaderSpeedListener li) {
        this.speedListeners.add(li);
    }

    @Override
    public void addDownloadListener(IPolyvCloudClassDownloaderListener li) {
        this.downloaderListeners.add(li);
    }

    @Override
    public void addStopListener(IPolyvCloudClassDownloaderStopListener li) {
        this.stopListeners.add(li);
    }

    @Override
    public void releaseListener() {
        releaseAllListener();
    }

    //移除所有监听器
    private void releaseAllListener() {
        beforeStartListeners.clear();
        stopListeners.clear();
        downloaderListeners.clear();
        stopListeners.clear();
    }
}
