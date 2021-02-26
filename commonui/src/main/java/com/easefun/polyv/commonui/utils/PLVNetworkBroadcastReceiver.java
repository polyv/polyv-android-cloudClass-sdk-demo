package com.easefun.polyv.commonui.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * date: 2021/2/22
 * author: HWilliamgo
 * description: 网络变化监听器
 */
public class PLVNetworkBroadcastReceiver extends BroadcastReceiver {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private static final String TAG = PLVNetworkBroadcastReceiver.class.getSimpleName();

    @Nullable
    private OnNetworkBroadcastReceiverListener listener;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    public void init(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        context.registerReceiver(this, intentFilter);
    }

    public void setListener(OnNetworkBroadcastReceiverListener listener) {
        this.listener = listener;
    }

    public void destroy(Context context) {
        context.unregisterReceiver(this);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写BroadcastReceiver">
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        Log.e(TAG, "当前WiFi连接可用 ");
                        if (listener != null) {
                            listener.onConnectedWIFI();
                        }
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        Log.e(TAG, "当前移动网络连接可用 ");
                        if (listener != null) {
                            listener.onConnectedMobile();
                        }
                    }
                } else {
                    Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                    if (listener != null) {
                        listener.onDisconnected();
                    }
                }
            } else {   // not connected to the internet
                Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="监听器声明">
    public static abstract class OnNetworkBroadcastReceiverListener {
        /**
         * 连接到WIFI
         */
        public void onConnectedWIFI() {
        }


        /**
         * 连接到移动网络
         */
        public void onConnectedMobile() {
        }

        /**
         * 断开网络
         */
        public void onDisconnected() {
        }
    }
// </editor-fold>
}
