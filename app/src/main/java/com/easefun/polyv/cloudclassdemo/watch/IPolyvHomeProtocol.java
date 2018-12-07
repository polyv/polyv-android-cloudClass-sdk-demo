package com.easefun.polyv.cloudclassdemo.watch;

import android.view.ViewGroup;

/**
 * @author df
 * @create 2018/9/27
 * @Describe
 */
public interface IPolyvHomeProtocol {
    void sendDanmu(CharSequence content);

    ViewGroup getImageViewerContainer();

    ViewGroup getChatEditContainer();
}
