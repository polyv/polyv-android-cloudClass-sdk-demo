package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloaderListener;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderBeforeStartListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderSpeedListener;
import com.easefun.polyv.cloudclass.download.listener.IPolyvCloudClassDownloaderStopListener;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 下载器监听器注册处
 * <p>
 * 每一个下载器只有一个监听器变量，注册处对下载器的回调做了拓展，让当个回调能具有更多不同的功能。
 */
public interface IPolyvDownloaderListenerRegistry extends IPolyvCloudClassDownloaderBeforeStartListener<PolyvCloudClassPlaybackCacheVO>,
        IPolyvCloudClassDownloaderSpeedListener,
        IPolyvCloudClassDownloaderListener,
        IPolyvCloudClassDownloaderStopListener,
        IPolyvDownloaderListenerBinder,
        IPolyvRegistryRelease {

}
