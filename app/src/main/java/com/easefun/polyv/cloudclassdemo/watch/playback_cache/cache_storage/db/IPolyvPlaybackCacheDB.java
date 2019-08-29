package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db;

import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

import java.util.List;

/**
 * date: 2019/8/19 0019
 *
 * @author hwj
 * description 回放缓存数据库抽象
 */
public interface IPolyvPlaybackCacheDB {
    void insert(PolyvPlaybackCacheDBEntity entity);

    void delete(PolyvPlaybackCacheDBEntity entity);

    void updateProgress(PolyvPlaybackCacheDBEntity entity);

    void updateStatus(PolyvPlaybackCacheDBEntity entity);

    List<PolyvPlaybackCacheDBEntity> getByVideoPoolId(String videoPoolId);

    List<PolyvPlaybackCacheDBEntity> getAll();
}
