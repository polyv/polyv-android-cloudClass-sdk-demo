package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.vo.PolyvCacheStatusConverter;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

/**
 * date: 2019/8/16 0016
 *
 * @author hwj
 * description Room数据库实现类
 */
@Database(entities = {PolyvPlaybackCacheDBEntity.class}, version = 1, exportSchema = false)
@TypeConverters({PolyvCacheStatusConverter.class})
public abstract class PolyvPlaybackCacheDB extends RoomDatabase {

    private static PolyvPlaybackCacheDB INSTANCE;

    public static PolyvPlaybackCacheDB getInstance(Context context, String viewerId) {
        if (INSTANCE == null) {
            synchronized (PolyvPlaybackCacheDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, PolyvPlaybackCacheDB.class, getDBSavePath(viewerId)).build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract IPolyvPlaybackCacheDBEntityDao getPlaybackDBDao();

    private static String getDBSavePath(String viewerId) {
        String dbName = "download_list";
        String finalDBName=dbName+"_"+viewerId+".db";
        String path = Utils.getApp().getDatabasePath(finalDBName).getAbsolutePath();
        LogUtils.i("回放缓存数据库的存储路径为：" + path);
        return path;
    }
}
