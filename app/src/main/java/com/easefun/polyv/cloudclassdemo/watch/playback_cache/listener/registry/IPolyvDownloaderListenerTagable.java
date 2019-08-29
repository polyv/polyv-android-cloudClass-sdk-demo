package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderStopListener;

/**
 * date: 2019/8/28 0028
 *
 * @author hwj
 * description 描述一下方法的作用
 */
public interface IPolyvDownloaderListenerTagable extends IPolyvDownloaderListenerRegistry {
    void addBeforeStartListener(String tag, IPolyvCloudClassDownloaderBeforeStartListener li);

    void addSpeedListener(String tag, IPolyvCloudClassDownloaderSpeedListener li);

    void addDownloadListener(String tag, IPolyvCloudClassDownloaderListener li);

    void addStopListener(String tag, IPolyvCloudClassDownloaderStopListener li);

    void removeBeforeStartListener(String tag);

    void removeSpeedListener(String tag);

    void removeDownloadListener(String tag);

    void removeStopListener(String tag);
}
