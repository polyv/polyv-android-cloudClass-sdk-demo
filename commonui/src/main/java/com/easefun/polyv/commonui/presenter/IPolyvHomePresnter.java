package com.easefun.polyv.commonui.presenter;

import android.view.ViewGroup;

/**
 * @author df
 * @create 2018/9/27
 * @Describe
 */
public interface IPolyvHomePresnter {
    void sendDanmu(CharSequence content);

    ViewGroup getImageViewerContainer();

    ViewGroup getChatEditContainer();
}
