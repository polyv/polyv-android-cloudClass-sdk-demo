package com.easefun.polyv.cloudclassdemo.watch.chat.event;

/**
 * @author df
 * @create 2019/1/17
 * @Describe
 */
public class PolyvChatMessageEvent {
    public interface  MessageEvent{
        public static final String QMessage = "QMessage";//扣消息
        public static final String RewardMessage = "RewardMessage";//打赏消息
        public static final String GiftMessage = "GiftMessage";//送礼消息
    }
}
