package com.easefun.polyv.cloudclassdemo.watch.playback_cache;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;


import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;



/**
 * date: 2019/8/14 0014
 *
 * @author hwj
 * description 回放缓存配置
 */
public class PolyvPlaybackCacheConfig {
    // <editor-fold defaultstate="collapsed" desc="单例">
    private static PolyvPlaybackCacheConfig INSTANCE;

    public static PolyvPlaybackCacheConfig get() {
        if (INSTANCE == null) {
            synchronized (PolyvPlaybackCacheConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PolyvPlaybackCacheConfig();
                }
            }
        }
        return INSTANCE;
    }
    // </editor-fold>

    /**
     * 回放频道Id
     */
    private String channelId;

    /**
     * userId
     */
    private String userId;


    /**
     * 是否有缓存权限
     */
    private boolean cacheEnabled;

    /**
     * 下载根目录
     */
    private String downloadRootPath;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDownloadRootPath(String downloadRootPath) {
        this.downloadRootPath = downloadRootPath;
    }

    public String getDownloadRootPath() {
        return downloadRootPath;
    }


    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }


    public String getChannelId() {
        if (TextUtils.isEmpty(channelId)) {
            throw new RuntimeException("请为回放缓存设置正确的channelId");
        }
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDefaultDownloadRootDir() {
        Context context = Utils.getApp();
        String MOUNTED = "mounted";

        File[] files;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //列表中包含了可移除的存储介质（例如 SD 卡）的路径。
            files = context.getExternalFilesDirs(null);
        } else {
            files = ContextCompat.getExternalFilesDirs(context, null);
        }

        ArrayList<File> storageList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //判断存储设备可用性
            for (File file : files) {
                if (file != null) {
                    String state = Environment.getExternalStorageState(file);
                    if (MOUNTED.equals(state)) {
                        storageList.add(file);
                    }
                }
            }
        } else {
            storageList.addAll(Arrays.asList(files));
        }

        if (storageList.isEmpty()) {
            LogUtils.e("没有可用的存储设备,后续不能使用视频缓存功能");
            return "";
        } else {
            return storageList.get(0).getAbsolutePath();
        }
    }
}
