package com.easefun.polyv.commonui.player;

import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.commonui.PolyvCommonVideoHelper;

/**
 * @author df
 * @create 2018/8/15
 * @Describe
 */
public interface IPolyvBusinessMediaController<T,R extends PolyvCommonVideoHelper> extends IPolyvMediaController<T> {
    void addHelper(R tpqPolyvCommonVideoHelper);
}
