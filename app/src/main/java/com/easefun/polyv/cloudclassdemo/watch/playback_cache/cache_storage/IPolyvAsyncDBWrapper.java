package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage;

import android.support.annotation.Nullable;

import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db.IPolyvPlaybackCacheDB;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * date: 2019/8/19 0019
 *
 * @author hwj
 * description 回放缓存抽象的拓展
 */
public interface IPolyvAsyncDBWrapper extends IPolyvPlaybackCacheDB {

    /**
     * 带返回值的异步
     *
     * @param ioEnv io线程环境
     * @param uiEnv ui线程环境
     * @param <T>   返回值
     * @return disposable
     */
    <T> Disposable asyncWithResult(Function<IPolyvPlaybackCacheDB, T> ioEnv, @Nullable Consumer<T> uiEnv);


    /**
     * 不带返回值的异步
     *
     * @param uiEnv io线程环境
     * @return disposable
     */
    Disposable asyncNoResult(Consumer<IPolyvPlaybackCacheDB> uiEnv);
}
