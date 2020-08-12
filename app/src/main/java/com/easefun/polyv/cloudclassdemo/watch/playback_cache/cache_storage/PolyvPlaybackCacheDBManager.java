package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage;

import android.content.Context;
import android.text.TextUtils;

import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.db.PolyvPlaybackCacheDB;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;

/**
 * date: 2019/8/19 0019
 *
 * @author hwj
 * description 回访缓存数据库管理器
 */
public class PolyvPlaybackCacheDBManager {
    private static IPolyvAsyncDBWrapper dbWrapper;


    public static IPolyvAsyncDBWrapper getDB() {
        //获取viewerId，用于做回放缓存多账户。用于数据库存储路径的区分。
        String viewerId = PolyvLiveSDKClient.getInstance().getViewerId();
        if (TextUtils.isEmpty(viewerId)){
            LogUtils.e("viewerId不可为空");
        }
        Context context = Utils.getApp();

        if (dbWrapper == null) {
            synchronized (PolyvPlaybackCacheDBManager.class) {
                if (dbWrapper == null) {
                    dbWrapper = new PolyvAsyncDBWrapper(PolyvPlaybackCacheDB.getInstance(context, viewerId).getPlaybackDBDao());
                }
            }
        }
        return dbWrapper;
    }
}
