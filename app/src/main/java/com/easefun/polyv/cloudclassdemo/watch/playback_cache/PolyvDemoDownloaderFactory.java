package com.easefun.polyv.cloudclassdemo.watch.playback_cache;

import android.support.annotation.NonNull;

import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassDownloader;
import com.easefun.polyv.cloudclass.download.PolyvCloudClassDownloaderManager;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry.IPolyvDownloaderListenerRegistry;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry.PolyvGlobalDownloaderListenerKeeper;

import java.io.File;

/**
 * date: 2019/8/23 0023
 *
 * @author hwj
 * description demo层下载器工厂类。
 */
public class PolyvDemoDownloaderFactory {

    /**
     * 创建并返回下载器，并为下载器设置全局监听器。
     */
    @NonNull
    public static PolyvCloudClassDownloader getDownloaderAndSetGlobalListener(String videoPoolId) {

        //以下3个都是全局设置的配置参数
        String channelId = PolyvPlaybackCacheConfig.get().getChannelId();
        File downloadDir = new File(PolyvPlaybackCacheConfig.get().getDownloadRootPath());
        String viewerId = PolyvLiveSDKClient.getInstance().getViewerId();


        //初始化下载器
        PolyvCloudClassDownloader downloader = PolyvCloudClassDownloaderManager
                .getInstance()
                .addDownloader(
                        new IPolyvCloudClassDownloader
                                .Builder(videoPoolId, channelId)
                                .downloadDir(downloadDir)
                                .viewerId(viewerId)
                );

        //初始化全局监听器注册处
        IPolyvDownloaderListenerRegistry registry = PolyvGlobalDownloaderListenerKeeper.getInstance().getRegistryOrAddIfNull(
                videoPoolId,
                downloader
        );

        //设置全局监听器
        downloader.setPolyvDownloadBeforeStartListener(registry);
        downloader.setPolyvDownloadListener(registry);
        downloader.setPolyvDownloadStopListener(registry);
        downloader.setPolyvDownloadSpeedListener(registry);

        return downloader;
    }

    /**
     * 开始下载
     *
     * @param downloader sdk下载器
     */
    public static void startDownload(PolyvCloudClassDownloader downloader) {
        PolyvCloudClassDownloaderManager.getInstance().startDownload(downloader);
    }

    /**
     * 移除并停止下载器
     *
     * @param downloader 下载器
     */
    public static void removeDownloader(IPolyvCloudClassDownloader downloader) {

        //先检查内存中存在有下载器，若存在，则移除。
        //如果不检查的话，remove操作会调用stop,引起onStop回调
        IPolyvCloudClassDownloader iPolyvCloudClassDownloader = PolyvCloudClassDownloaderManager.getInstance()
                .getPolyvDownloader(downloader.getKey());

        if (iPolyvCloudClassDownloader != null) {
            PolyvCloudClassDownloaderManager.getInstance()
                    .removeDownloader(downloader.getKey());
        }

    }
}
