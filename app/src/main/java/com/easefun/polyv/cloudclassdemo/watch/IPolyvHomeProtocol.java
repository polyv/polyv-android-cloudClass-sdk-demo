package com.easefun.polyv.cloudclassdemo.watch;

import android.view.ViewGroup;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;

/**
 * @author df
 * @create 2018/9/27
 * @Describe
 */
public interface IPolyvHomeProtocol {
    void sendDanmu(CharSequence content);

    //添加提问的未读信息数
    void addUnreadQuiz(int unreadCount);

    //当前是否选择了提问tab
    boolean isSelectedQuiz();

    //添加聊天的未读信息数
    void addUnreadChat(int unreadCount);

    //当前是否选择了聊天tab
    boolean isSelectedChat();

    //当前直播的场次Id
    String getSessionId();

    ViewGroup getImageViewerContainer();

    ViewGroup getChatEditContainer();

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
