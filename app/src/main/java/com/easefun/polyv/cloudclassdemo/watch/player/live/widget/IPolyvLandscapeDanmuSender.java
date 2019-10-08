package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

/**
 * date: 2019/6/6 0006
 *
 * @author hwj
 * description 弹幕发送器抽象
 */
public interface IPolyvLandscapeDanmuSender {

    /**
     * 设置发送弹幕监听器
     *
     * @param listener listener
     */
    void setOnSendDanmuListener(OnSendDanmuListener listener);

    /**
     * 打开弹幕发送器
     */
    void openDanmuSender();

    /**
     * 隐藏
     */
    void dismiss();

    /**
     * 发送弹幕监听器
     */
    interface OnSendDanmuListener {
        /**
         * 横屏发送的弹幕消息应同步到聊天室
         *
         * @param danmuMessage 横屏发送的弹幕消息
         */
        void onSendDanmu(String danmuMessage);
    }
}
