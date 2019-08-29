package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderStopListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * date: 2019/8/28 0028
 *
 * @author hwj
 * description 为全局监听器添加带标记的监听器的功能
 */
class PolyvDownloadListenerTagDecorator implements IPolyvDownloaderListenerTagable {

    private IPolyvDownloaderListenerRegistry registry;

    private Map<String, IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO>> beforeStartListenerMap = new ConcurrentHashMap<>();
    private Map<String, IPolyvCloudClassDownloaderSpeedListener> speedListenerMap = new ConcurrentHashMap<>();
    private Map<String, IPolyvCloudClassDownloaderListener> downloaderListenerMap = new ConcurrentHashMap<>();
    private Map<String, IPolyvCloudClassDownloaderStopListener> stopListenerMap = new ConcurrentHashMap<>();

    public PolyvDownloadListenerTagDecorator(IPolyvDownloaderListenerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onProgress(long current, long total) {
        registry.onProgress(current, total);
        for (IPolyvCloudClassDownloaderListener value : downloaderListenerMap.values()) {
            value.onProgress(current, total);
        }
    }

    @Override
    public void onFailure(int errorReason) {
        registry.onFailure(errorReason);
        for (IPolyvCloudClassDownloaderListener value : downloaderListenerMap.values()) {
            value.onFailure(errorReason);
        }
    }

    @Override
    public boolean onBeforeStart(IPolyvCloudClassDownloader downloader, PolyvCloudClassPlaybackCacheVO polyvCloudClassPlaybackCacheVO) {
        for (IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO> value : beforeStartListenerMap.values()) {
            value.onBeforeStart(downloader, polyvCloudClassPlaybackCacheVO);
        }
        return registry.onBeforeStart(downloader, polyvCloudClassPlaybackCacheVO);
    }


    // <editor-fold defaultstate="collapsed" desc="same">
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
// </editor-fold>

    @Override
    public void onSpeed(int speed) {
        registry.onSpeed(speed);
        for (IPolyvCloudClassDownloaderSpeedListener value : speedListenerMap.values()) {
            value.onSpeed(speed);
        }
    }

    @Override
    public void onSuccess(PolyvCloudClassPlaybackCacheVO playbackCacheVO) {
        registry.onSuccess(playbackCacheVO);
        for (IPolyvCloudClassDownloaderListener value : downloaderListenerMap.values()) {
            value.onSuccess(playbackCacheVO);
        }
        releaseAllListener();
    }

    @Override
    public void onStop() {
        registry.onStop();
        for (IPolyvCloudClassDownloaderStopListener value : stopListenerMap.values()) {
            value.onStop();
        }
        releaseAllListener();
    }

    @Override
    public void releaseListener() {
        registry.releaseListener();
        releaseAllListener();
    }

    @Override
    public void addBeforeStartListener(String tag, IPolyvCloudClassDownloaderBeforeStartListener li) {
        beforeStartListenerMap.put(tag, li);
    }

    @Override
    public void addSpeedListener(String tag, IPolyvCloudClassDownloaderSpeedListener li) {
        speedListenerMap.put(tag, li);
    }

    @Override
    public void addDownloadListener(String tag, IPolyvCloudClassDownloaderListener li) {
        downloaderListenerMap.put(tag, li);
    }

    @Override
    public void addStopListener(String tag, IPolyvCloudClassDownloaderStopListener li) {
        stopListenerMap.put(tag, li);
    }

    @Override
    public void removeBeforeStartListener(String tag) {
        beforeStartListenerMap.remove(tag);
    }

    @Override
    public void removeSpeedListener(String tag) {
        speedListenerMap.remove(tag);
    }

    @Override
    public void removeDownloadListener(String tag) {
        downloaderListenerMap.remove(tag);
    }

    @Override
    public void removeStopListener(String tag) {
        stopListenerMap.remove(tag);
    }

    private void releaseAllListener() {
        beforeStartListenerMap.clear();
        speedListenerMap.clear();
        downloaderListenerMap.clear();
        stopListenerMap.clear();
    }
}
