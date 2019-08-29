package com.easefun.polyv.commonui.modle.db;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.easefun.polyv.commonui.modle.db.PolyvCacheStatus;


/**
 * date: 2019/8/16 0016
 *
 * @author hwj
 * description 回放缓存数据库实体类。
 */
@Entity(tableName = "download_list")
public class PolyvPlaybackCacheDBEntity {


    //videoPoolId;
    @PrimaryKey
    @NonNull
    private String videoPoolId;
    // videoLiveId
    private String videoLiveId;
    //是否是普通直播
    private boolean isNormal;
    // 时长
    private String duration;
    // 文件大小
    private long filesize;
    // 标题
    private String title;
    // 已下载的文件大小(mp4)/已下载的文件个数(ts)
    private long percent;
    // 总文件大小(mp4)/总个数(ts)
    private long total;
    //状态
    private PolyvCacheStatus status;
    //视频路径
    private String videoPath;
    //ppt数据路径
    private String pptPath;
    //js数据路径
    private String jsPath;
    //channelId
    private String channelId;
    //封面图
    private String firstImageUrl;

    public PolyvPlaybackCacheDBEntity() {
    }

    public PolyvPlaybackCacheDBEntity(@NonNull String videoPoolId, String videoLiveId, boolean isNormal, String duration, long filesize, String title, long percent, long total, PolyvCacheStatus status, String videoPath, String pptPath, String jsPath, String channelId, String firstImageUrl) {
        this.videoPoolId = videoPoolId;
        this.videoLiveId = videoLiveId;
        this.isNormal = isNormal;
        this.duration = duration;
        this.filesize = filesize;
        this.title = title;
        this.percent = percent;
        this.total = total;
        this.status = status;
        this.videoPath = videoPath;
        this.pptPath = pptPath;
        this.jsPath = jsPath;
        this.channelId = channelId;
        this.firstImageUrl = firstImageUrl;
    }

    @NonNull
    public String getVideoPoolId() {
        return videoPoolId;
    }

    public void setVideoPoolId(@NonNull String videoPoolId) {
        this.videoPoolId = videoPoolId;
    }

    public String getVideoLiveId() {
        return videoLiveId;
    }

    public void setVideoLiveId(String videoLiveId) {
        this.videoLiveId = videoLiveId;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public void setNormal(boolean normal) {
        isNormal = normal;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPercent() {
        return percent;
    }

    public void setPercent(long percent) {
        this.percent = percent;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public PolyvCacheStatus getStatus() {
        return status;
    }

    public void setStatus(PolyvCacheStatus status) {
        this.status = status;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getPptPath() {
        return pptPath;
    }

    public void setPptPath(String pptPath) {
        this.pptPath = pptPath;
    }

    public String getJsPath() {
        return jsPath;
    }

    public void setJsPath(String jsPath) {
        this.jsPath = jsPath;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getFirstImageUrl() {
        return firstImageUrl;
    }

    public void setFirstImageUrl(String firstImageUrl) {
        this.firstImageUrl = firstImageUrl;
    }

    @Override
    public String toString() {
        return "PolyvPlaybackCacheDBEntity{" +
                "videoPoolId='" + videoPoolId + '\'' +
                ", videoLiveId='" + videoLiveId + '\'' +
                ", isNormal=" + isNormal +
                ", duration='" + duration + '\'' +
                ", filesize=" + filesize +
                ", title='" + title + '\'' +
                ", percent=" + percent +
                ", total=" + total +
                ", status=" + status +
                ", videoPath='" + videoPath + '\'' +
                ", pptPath='" + pptPath + '\'' +
                ", jsPath='" + jsPath + '\'' +
                ", channelId='" + channelId + '\'' +
                ", firstImageUrl='" + firstImageUrl + '\'' +
                '}';
    }
}
