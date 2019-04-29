package com.easefun.polyv.cloudclassdemo.watch.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

import com.easefun.polyv.commonui.PolyvCommonVideoHelper;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;

/**
 * @author df
 * @create 2018/10/8
 * @Describe
 */
public class PolyvOrientoinListener extends OrientationEventListener {
    private static final String TAG = "PolyvOrientoinListener";
    private Activity context;
    private int orientation;
    private PolyvCommonVideoHelper commonVideoHelper;
    public PolyvOrientoinListener(Context context, PolyvCommonVideoHelper commonVideoHelper) {
        this(context, SensorManager.SENSOR_DELAY_NORMAL, commonVideoHelper);
    }

    public PolyvOrientoinListener(Context context, int rate, PolyvCommonVideoHelper commonVideoHelper) {
        super(context, rate);
        this.commonVideoHelper = commonVideoHelper;
        initial(context);
    }

    void initial(Context context){
        if(context instanceof  Activity){
            this.context = (Activity) context;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        int clips = Math.abs(this.orientation - orientation);
        if(context == null || commonVideoHelper == null || clips <30 || clips >330){
            return;
        }
        this.orientation = orientation;
        int screenOrientation=context.getRequestedOrientation();
        PolyvCommonLog.d(TAG,"onOrientationChanged:"+orientation);
        if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {    //设置竖屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                commonVideoHelper.changeToPortrait();
            }
        } else if (orientation > 225 && orientation < 315) { //设置横屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                commonVideoHelper.changeToLandscape();
            }
        } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                commonVideoHelper.changeToLandscape();
            }
        } else if (orientation > 135 && orientation < 225) { //反向竖屏
            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                commonVideoHelper.changeToPortrait();
            }
        }
    }
}
