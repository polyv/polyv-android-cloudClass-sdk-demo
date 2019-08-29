package com.easefun.polyv.commonui.modle.db;

/**
 * date: 2019/8/15 0015
 *
 * @author hwj
 * description 缓存任务的状态
 */
public enum PolyvCacheStatus {
    /**
     * 等待中。通过创建下载任务或从暂停点击开始下载，将任务加入了下载队列，等待被工作线程调度才能开始下载。
     * 状态更新时机：主动更新
     */
    WAITING,

    /**
     * 正在下载。此时在下载队列中，已经开始下载了。
     * 状态更新时机：下载器回调更新
     */
    DOWNLOADING,

    /**
     * 暂停。被移出下载队列。
     * 状态更新时机：主动更新
     */
    PAUSED,

    /**
     * 下载出错。下载过程出错，任务中断，被移出下载队列。
     * 状态更新时机：下载器回调更新
     */
    ERROR,

    /**
     * 下载完成。任务结束，移出下载队列。
     * 状态更新时机：下载器回调更新
     */
    FINISHED
}
