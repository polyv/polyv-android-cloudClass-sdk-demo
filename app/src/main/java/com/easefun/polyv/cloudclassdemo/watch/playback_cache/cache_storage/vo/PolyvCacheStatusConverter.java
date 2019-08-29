package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.vo;

import android.arch.persistence.room.TypeConverter;

import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;


/**
 * date: 2019/8/16 0016
 *
 * @author hwj
 * description 枚举无法直接保存到room的数据库，用转换器转换成String。
 */
public class PolyvCacheStatusConverter {
    @TypeConverter
    public static PolyvCacheStatus stringToEnum(String status) {
        PolyvCacheStatus enumStatus;
        try {
            enumStatus = PolyvCacheStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            enumStatus = PolyvCacheStatus.ERROR;
        }
        return enumStatus;
    }

    @TypeConverter
    public static String enumToString(PolyvCacheStatus status) {
        return status.toString();
    }
}
