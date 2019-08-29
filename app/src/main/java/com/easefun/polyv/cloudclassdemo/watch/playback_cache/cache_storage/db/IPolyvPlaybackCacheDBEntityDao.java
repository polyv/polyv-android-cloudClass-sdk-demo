package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

import java.util.List;

/**
 * date: 2019/8/16 0016
 *
 * @author hwj
 * description 回放缓存实体Dao，由Room框架用注解生成实现类操作原生数据库。
 */

@Dao
public interface IPolyvPlaybackCacheDBEntityDao extends IPolyvPlaybackCacheDB {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PolyvPlaybackCacheDBEntity entity);

    @Delete
    void delete(PolyvPlaybackCacheDBEntity entity);

    @Update
    void updateProgress(PolyvPlaybackCacheDBEntity entity);

    @Update
    void updateStatus(PolyvPlaybackCacheDBEntity entity);

    @Query("SELECT * FROM download_list WHERE videoPoolId=:videoPoolId")
    List<PolyvPlaybackCacheDBEntity> getByVideoPoolId(String videoPoolId);

    @Query("SELECT * FROM download_list")
    List<PolyvPlaybackCacheDBEntity> getAll();
}
