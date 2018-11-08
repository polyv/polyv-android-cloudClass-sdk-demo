package com.easefun.polyv.commonui.player;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 视频信息类型
 */
public class PolyvMediaInfoType {
    public static final int MEDIA_INFO_UNKNOWN = IMediaPlayer.MEDIA_INFO_UNKNOWN;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = IMediaPlayer.MEDIA_INFO_STARTED_AS_NEXT;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING;
    /**
     * 缓冲开始
     */
    public static final int MEDIA_INFO_BUFFERING_START = IMediaPlayer.MEDIA_INFO_BUFFERING_START;
    /**
     * 缓冲结束
     */
    public static final int MEDIA_INFO_BUFFERING_END = IMediaPlayer.MEDIA_INFO_BUFFERING_END;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING;
    public static final int MEDIA_INFO_NOT_SEEKABLE = IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE;
    public static final int MEDIA_INFO_METADATA_UPDATE = IMediaPlayer.MEDIA_INFO_METADATA_UPDATE;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = IMediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT;

    public static final int MEDIA_INFO_VIDEO_ROTATION_CHANGED = IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED;
    public static final int MEDIA_INFO_AUDIO_RENDERING_START = IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START;
}
