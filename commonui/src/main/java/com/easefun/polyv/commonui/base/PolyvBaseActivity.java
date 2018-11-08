package com.easefun.polyv.commonui.base;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.easefun.polyv.foundationsdk.permission.PolyvPermissionListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;

public class PolyvBaseActivity extends AppCompatActivity implements PolyvPermissionListener {
    protected CompositeDisposable disposables;
    protected PolyvPermissionManager permissionManager;
    private final int myRequestCode = 13333;
    //静态变量记录学员是否被踢，如果被踢后，需要结束应用后才能再次进来观看直播，这个逻辑可以更改
    public static Map<String, Boolean> kickMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.putParcelable("android:support:fragments", null);
        super.onCreate(savedInstanceState);
        disposables = new CompositeDisposable();
        permissionManager = PolyvPermissionManager.with(this)
                .addRequestCode(myRequestCode)
                .setPermissionsListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == myRequestCode && resultCode == Activity.RESULT_CANCELED)
            permissionManager.request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case myRequestCode:
                permissionManager.onPermissionResult(permissions, grantResults);
                break;
        }
    }

    public static void setKickValue(String channelId, boolean isKick) {
        kickMap.put(channelId, isKick);
    }

    public static boolean checkKick(String channelId) {
        return kickMap.containsKey(channelId) && kickMap.get(channelId);
    }

    public static boolean checkKickTips(final Activity activity, String channelId, String... message) {
        if (checkKick(channelId)) {
            new AlertDialog.Builder(activity)
                    .setTitle("温馨提示")
                    .setMessage(message != null && message.length > 0 ? message[0] : "您未被授权观看本直播！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposables != null) {
            disposables.dispose();
            disposables = null;
        }
    }

    @Override
    public void onGranted() {
    }

    @Override
    public void onDenied(String[] permissions) {
        permissionManager.showDeniedDialog(this, permissions);
    }

    @Override
    public void onShowRationale(String[] permissions) {
        permissionManager.showRationaleDialog(this, permissions);
    }
}
