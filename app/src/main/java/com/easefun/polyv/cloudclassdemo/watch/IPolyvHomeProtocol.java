package com.easefun.polyv.cloudclassdemo.watch;

import android.view.ViewGroup;

import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;

/**
 * @author df
 * @create 2018/9/27
 * @Describe
 */
public interface IPolyvHomeProtocol {
    //发送弹幕
    void sendDanmu(CharSequence content);

    //添加提问的未读信息数
    void addUnreadQuiz(int unreadCount);

    //当前是否选择了提问tab
    boolean isSelectedQuiz();

    //添加聊天的未读信息数
    void addUnreadChat(int unreadCount);

    //当前是否选择了聊天tab
    boolean isSelectedChat();

    //获取播放器对象
    PolyvCommonVideoView getVideoView();

    //当前直播的场次Id
    String getSessionId();

    //获取聊天图片浏览的容器
    ViewGroup getImageViewerContainer();

    //获取聊天输入框的容器
    ViewGroup getChatEditContainer();

    //获取聊天室管理对象
    PolyvChatManager getChatManager();

    /**
     *  移动聊天室得位置
     * @param downChat 是否沉底
     */
    public void moveChatLocation(boolean downChat);

    /**
     * 更新画笔状态
     */
    void updatePaintStatus(boolean showPaint);
}
