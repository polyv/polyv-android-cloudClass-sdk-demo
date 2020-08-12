package com.easefun.polyv.commonui.player;

import com.easefun.polyv.businesssdk.api.common.player.IPolyvBaseVideoView;

/**
 * 视频信息类型
 */
public class PolyvMediaInfoType {
    public static final int MEDIA_INFO_UNKNOWN = IPolyvBaseVideoView.MEDIA_INFO_UNKNOWN;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = IPolyvBaseVideoView.MEDIA_INFO_STARTED_AS_NEXT;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = IPolyvBaseVideoView.MEDIA_INFO_VIDEO_RENDERING_START;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = IPolyvBaseVideoView.MEDIA_INFO_VIDEO_TRACK_LAGGING;
    /**
     * 缓冲开始
     */
    public static final int MEDIA_INFO_BUFFERING_START = IPolyvBaseVideoView.MEDIA_INFO_BUFFERING_START;
    /**
     * 缓冲结束
     */
    public static final int MEDIA_INFO_BUFFERING_END = IPolyvBaseVideoView.MEDIA_INFO_BUFFERING_END;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = IPolyvBaseVideoView.MEDIA_INFO_NETWORK_BANDWIDTH;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = IPolyvBaseVideoView.MEDIA_INFO_BAD_INTERLEAVING;
    public static final int MEDIA_INFO_NOT_SEEKABLE = IPolyvBaseVideoView.MEDIA_INFO_NOT_SEEKABLE;
    public static final int MEDIA_INFO_METADATA_UPDATE = IPolyvBaseVideoView.MEDIA_INFO_METADATA_UPDATE;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = IPolyvBaseVideoView.MEDIA_INFO_TIMED_TEXT_ERROR;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = IPolyvBaseVideoView.MEDIA_INFO_UNSUPPORTED_SUBTITLE;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = IPolyvBaseVideoView.MEDIA_INFO_SUBTITLE_TIMED_OUT;

    public static final int MEDIA_INFO_VIDEO_ROTATION_CHANGED = IPolyvBaseVideoView.MEDIA_INFO_VIDEO_ROTATION_CHANGED;
    public static final int MEDIA_INFO_AUDIO_RENDERING_START = IPolyvBaseVideoView.MEDIA_INFO_AUDIO_RENDERING_START;
}
