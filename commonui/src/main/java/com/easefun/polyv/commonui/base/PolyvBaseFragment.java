package com.easefun.polyv.commonui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;

import io.reactivex.disposables.CompositeDisposable;

public abstract class PolyvBaseFragment extends Fragment implements PolyvPermissionListener {
    protected CompositeDisposable disposables;
    protected View view;
    protected PolyvPermissionManager permissionManager;
    protected PolyvToast toast;
    private final int myRequestCode = 16666;
    private boolean isCreatedFlag, isActivityCreatedFlag, isCallFirstDelay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(layoutId(), null) : view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreatedFlag = true;
        toast = new PolyvToast();
        disposables = new CompositeDisposable();//adapter, onAttach，onCreate
        permissionManager = PolyvPermissionManager.with(this)
                .addRequestCode(myRequestCode)
                .setPermissionsListener(this);
    }

    public abstract int layoutId();

    public abstract void loadDataDelay(boolean isFirst);

    public abstract void loadDataAhead();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        canLoadDataDelay();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        canLoadDataAhead();
        canLoadDataDelay();
    }

    private void canLoadDataAhead() {
        if (!isActivityCreatedFlag) {
            isActivityCreatedFlag = !isActivityCreatedFlag;
            loadDataAhead();
        }
    }

    private void canLoadDataDelay() {
        if (isCreatedFlag && getUserVisibleHint()) {
            isCreatedFlag = !isCreatedFlag;
            loadDataDelay(isCallFirstDelay = true);
        } else if (getUserVisibleHint() && isCallFirstDelay) {
            loadDataDelay(false);
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCreatedFlag = false;
        isActivityCreatedFlag = false;
        isCallFirstDelay = false;
        view = null;
        if (toast != null) {
            toast.destroy();
            toast = null;
        }
        if (disposables != null) {
            disposables.dispose();
            disposables = null;
        }
        if (permissionManager != null) {
            permissionManager.destroy();
            permissionManager = null;
        }
    }

    @Override
    public void onGranted() {
    }

    @Override
    public void onDenied(String[] permissions) {
        permissionManager.showDeniedDialog(getContext(), permissions);
    }

    @Override
    public void onShowRationale(String[] permissions) {
        permissionManager.showRationaleDialog(getContext(), permissions);
    }

    protected final <T extends View> T findViewById(int id) {
        return view.findViewById(id);
    }
}
