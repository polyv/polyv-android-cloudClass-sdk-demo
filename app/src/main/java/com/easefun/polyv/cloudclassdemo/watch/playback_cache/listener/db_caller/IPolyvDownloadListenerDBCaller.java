package com.easefun.polyv.cloudclassdemo.watch.playback_cache.listener.db_caller;

import com.easefun.polyv.cloudclass.download.PolyvCloudClassPlaybackCacheVO;

/**
 * date: 2019/8/22 0022
 *
 * @author hwj
 * description 下载回调数据库调用抽象
 */
public interface IPolyvDownloadListenerDBCaller {
    void callBeforeStart(PolyvCloudClassPlaybackCacheVO vo);

    void callOnProgress(long percent, long total);

    void callOnStop();

    void callOnSuccess(PolyvCloudClassPlaybackCacheVO vo);

    void callOnError();
}
