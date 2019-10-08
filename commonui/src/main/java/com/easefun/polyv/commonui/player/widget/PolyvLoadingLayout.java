package com.easefun.polyv.commonui.player.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.api.common.player.IPolyvBaseVideoView;
import com.easefun.polyv.businesssdk.vodplayer.PolyvPlayType;
import com.easefun.polyv.cloudclass.playback.video.PolyvPlaybackVideoView;
import com.easefun.polyv.cloudclass.video.PolyvCloudClassVideoView;
import com.easefun.polyv.commonui.R;

import java.util.Locale;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PolyvLoadingLayout extends FrameLayout {
    private ProgressBar loadingProgress;
    private TextView loadingSpeed;

    private IPolyvBaseVideoView videoView;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (videoView instanceof PolyvCloudClassVideoView ||
                        (videoView instanceof PolyvPlaybackVideoView && ((PolyvPlaybackVideoView) videoView).getPlayType() == PolyvPlayType.ONLINE_PLAY)) {
                    IjkMediaPlayer mp = videoView.getIjkMediaPlayer();
                    if (mp != null) {
                        long tcpSpeed = mp.getTcpSpeed();
                        loadingSpeed.setVisibility(View.VISIBLE);
                        loadingSpeed.setText(formatedSpeed(tcpSpeed, 1000));

                        handler.sendEmptyMessageDelayed(1, 500);
                    }
                }
            }
        }
    };

    private static String formatedSpeed(long bytes, long elapsed_milli) {
        if (elapsed_milli <= 0) {
            return "0 B/S";
        }

        if (bytes <= 0) {
            return "0 B/S";
        }

        float bytes_per_sec = ((float) bytes) * 1000.f / elapsed_milli;
        if (bytes_per_sec >= 1000 * 1000) {
            return String.format(Locale.US, "%.2f MB/S", ((float) bytes_per_sec) / 1000 / 1000);
        } else if (bytes_per_sec >= 1000) {
            return String.format(Locale.US, "%.2f KB/S", ((float) bytes_per_sec) / 1000);
        } else {
            return String.format(Locale.US, "%d B/S", (long) bytes_per_sec);
        }
    }

    public PolyvLoadingLayout(@NonNull Context context) {
        this(context, null);
    }

    public PolyvLoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvLoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.polyv_loading_layout, this);
        initView();
    }

    private void initView() {
        loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        loadingSpeed = (TextView) findViewById(R.id.loading_speed);
    }

    public void bindVideoView(IPolyvBaseVideoView videoView) {
        this.videoView = videoView;
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        acceptVisibilityChange(visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        acceptVisibilityChange(getVisibility());
    }

    private void acceptVisibilityChange(int visibility) {
        handler.removeCallbacksAndMessages(null);
        if (visibility == View.VISIBLE) {
            handler.sendEmptyMessage(1);
        } else {
            loadingSpeed.setVisibility(View.GONE);
        }
    }
}
