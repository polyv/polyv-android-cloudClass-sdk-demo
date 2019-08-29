package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderStopListener;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 下载监听器绑定器
 */
public interface IPolyvDownloaderListenerBinder {
    void addBeforeStartListener(IPolyvCloudClassDownloaderBeforeStartListener li);

    void addSpeedListener(IPolyvCloudClassDownloaderSpeedListener li);

    void addDownloadListener(IPolyvCloudClassDownloaderListener li);

    void addStopListener(IPolyvCloudClassDownloaderStopListener li);
}
