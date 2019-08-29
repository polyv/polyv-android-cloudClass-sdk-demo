package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage;

import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db.IPolyvPlaybackCacheDB;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * date: 2019/8/19 0019
 *
 * @author hwj
 * description 异步数据库装饰类
 */
public class PolyvAsyncDBWrapper implements IPolyvAsyncDBWrapper {
    private IPolyvPlaybackCacheDB iPolyvPlaybackCacheDB;

    PolyvAsyncDBWrapper(IPolyvPlaybackCacheDB iPolyvPlaybackCacheDB) {
        this.iPolyvPlaybackCacheDB = iPolyvPlaybackCacheDB;
    }

    @Override
    public void insert(PolyvPlaybackCacheDBEntity entity) {
        iPolyvPlaybackCacheDB.insert(entity);
    }

    @Override
    public void delete(PolyvPlaybackCacheDBEntity entity) {
        iPolyvPlaybackCacheDB.delete(entity);
    }

    @Override
    public void updateProgress(PolyvPlaybackCacheDBEntity entity) {
        iPolyvPlaybackCacheDB.updateProgress(entity);
    }

    @Override
    public void updateStatus(PolyvPlaybackCacheDBEntity entity) {
        iPolyvPlaybackCacheDB.updateStatus(entity);
    }

    @Override
    public List<PolyvPlaybackCacheDBEntity> getByVideoPoolId(String videoPoolId) {
        return iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId);
    }

    @Override
    public List<PolyvPlaybackCacheDBEntity> getAll() {
        return iPolyvPlaybackCacheDB.getAll();
    }


    @Override
    public <T> Disposable asyncWithResult(Function<IPolyvPlaybackCacheDB, T> ioEnv, @Nullable Consumer<T> uiEnv) {
        return Observable.just(1)
                .map(integer -> {
                    T resultValue = ioEnv.apply(PolyvAsyncDBWrapper.this);
                    if (resultValue == null) {
                        throw new RuntimeException("ioEnv方法返回了Null");
                    } else {
                        return resultValue;
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t -> {
                    if (uiEnv != null) {
                        uiEnv.accept(t);
                    }
                });
    }

    @Override
    public Disposable asyncNoResult(Consumer<IPolyvPlaybackCacheDB> uiEnv) {
        return Observable.just(1)
                .doOnNext(integer -> uiEnv.accept(this))
                .subscribeOn(Schedulers.single())
                .subscribe();
    }
}
