package com.easefun.polyv.commonui.player.ppt;

import android.view.View;

import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.ppt.IPolyvPPTView;

/**
 * @author df
 * @create 2018/8/11
 * @Describe
 */
public interface IPolyvPPTItem<T extends IPolyvMediaController> {

    View getItemRootView();

    IPolyvPPTView getPPTView();

    void addMediaController(T mediaController);
}
