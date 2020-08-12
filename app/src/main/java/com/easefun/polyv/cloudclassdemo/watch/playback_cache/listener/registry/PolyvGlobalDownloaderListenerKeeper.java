package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.registry;

import com.easefun.polyv.cloudclass.download.IPolyvCloudClassDownloader;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.PolyvDemoDownloaderFactory;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.db_caller.PolyvDownloadListenerDBCaller;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 下载监听器全局持有类
 * <p>
 * 每一个下载器对应只有一个下载器注册处{@link IPolyvDownloaderListenerRegistry}
 * 默认的下载器注册处实现封装了数据库的操作。
 */
public class PolyvGlobalDownloaderListenerKeeper {

    //将下载器和他的监听器绑定，以便程序全局都可以通过下载器访问到监听器注册处。来为下载器增减监听器。
    private Map<IPolyvCloudClassDownloader, IPolyvDownloaderListenerTagable> registryMap = new ConcurrentHashMap<>();

    private static PolyvGlobalDownloaderListenerKeeper INSTANCE;

    private PolyvGlobalDownloaderListenerKeeper() {

    }

    public static PolyvGlobalDownloaderListenerKeeper getInstance() {
        if (INSTANCE == null) {
            synchronized (PolyvGlobalDownloaderListenerKeeper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PolyvGlobalDownloaderListenerKeeper();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * 获取注册处，注册处用于添加监听器
     * 如果注册处为空则添加再返回
     *
     * @param videoPoolId videoPoolId
     * @param downloader  下载器
     * @return 注册处
     */
    public IPolyvDownloaderListenerTagable getRegistryOrAddIfNull(String videoPoolId, IPolyvCloudClassDownloader downloader) {
        if (downloader == null) {
            LogUtils.e("downloader==null");
            return null;
        }

        if (registryMap.get(downloader) == null) {
            IPolyvDownloaderListenerTagable registry = createRegistry(videoPoolId);

            if (registryMap.get(downloader) == null) {
                registryMap.put(downloader, registry);
            }
            return registry;
        } else {
            return registryMap.get(downloader);
        }
    }

    /**
     * 移除下载器及其注册处。
     * 当下载器不在内存中驻留时，需要将下载器移除。
     *
     * @param downloader 下载器。
     */
    public void removeRegistry(IPolyvCloudClassDownloader downloader) {
        LogUtils.i("移除下载器注册处" + downloader);

        registryMap.remove(downloader);

        //必须从外部将下载器移除，否则sdk层就算下载器暂停了也会继续持有下载器。
        PolyvDemoDownloaderFactory.removeDownloader(downloader);
    }


    private IPolyvDownloaderListenerTagable createRegistry(String videoPoolId) {
        IPolyvDownloaderListenerRegistry downloaderListenerRegistry = new PolyvDownloaderListenerDBCallDecorator(
                new PolyvDownloaderListenerRegistry(),
                new PolyvDownloadListenerDBCaller(videoPoolId)
        );
        IPolyvDownloaderListenerTagable downloaderListenerTagable = new PolyvDownloadListenerTagDecorator(downloaderListenerRegistry);
        return downloaderListenerTagable;
    }

}
