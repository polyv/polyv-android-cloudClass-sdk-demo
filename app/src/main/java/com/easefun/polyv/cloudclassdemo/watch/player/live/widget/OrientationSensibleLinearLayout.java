package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description 能收到方向回调的LinearLayout
 */
public class OrientationSensibleLinearLayout extends LinearLayout {
    public Runnable onPortrait;
    public Runnable onLandscape;

    public OrientationSensibleLinearLayout(Context context) {
        super(context);
    }

    public OrientationSensibleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OrientationSensibleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (onPortrait != null) {
                onPortrait.run();
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (onLandscape != null) {
                onLandscape.run();
            }
        }
    }
}
