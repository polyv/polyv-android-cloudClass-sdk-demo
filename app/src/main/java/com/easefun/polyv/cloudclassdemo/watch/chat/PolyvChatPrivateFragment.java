package com.easefun.polyv.cloudclassdemo.watch.chat;

import android.widget.Toast;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvQuestionMessage;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.event.PolyvTAnswerEvent;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.utils.PolyvTextImageLoader;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;

import io.reactivex.functions.Consumer;

/**
 * 私聊
 */
public class PolyvChatPrivateFragment extends PolyvChatBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="初始化">
    @Override
    public int layoutId() {
        return R.layout.polyv_fragment_personchat;
    }

    @Override
    public void loadDataAhead() {
        super.loadDataAhead();
        initCommonView();
        addQuestionTips();
        //需要在tab选中之前加载，可以获取未读的提问信息以更新右上角的未读信息数
        acceptEventMessage();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="添加问候语item">
    private void addQuestionTips() {
        //添加自定义的问候语事件
        chatListAdapter.getChatTypeItems().add(new PolyvChatListAdapter.ChatTypeItem(new PolyvQuestionTipsEvent(), PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE, PolyvChatManager.SE_MESSAGE));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送提问信息">
    @Override
    public void sendMessage() {
        sendQuestionMessage();
    }

    private void sendQuestionMessage() {
        String sendMessage = talk.getText().toString();
        if (sendMessage.trim().length() == 0) {
            toast.makeText(getContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show(true);
        } else {
            PolyvQuestionMessage questionMessage = new PolyvQuestionMessage(sendMessage);
            int sendValue = chatManager.sendQuestionMessage(questionMessage);
            //添加到列表中
            if (sendValue > 0) {
                talk.setText("");
                hideSoftInputAndEmoList();

                //把带表情的信息解析保存下来
                questionMessage.setObjects(PolyvTextImageLoader.messageToSpan(questionMessage.getQuestionMessage(), ConvertUtils.dp2px(14), false, getContext()));
                chatListAdapter.getChatTypeItems().add(new PolyvChatListAdapter.ChatTypeItem(questionMessage, PolyvChatListAdapter.ChatTypeItem.TYPE_SEND, PolyvChatManager.SE_MESSAGE));
                chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);
            } else {
                toast.makeText(getContext(), "发送失败：" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天室事件监听及处理">
    private void acceptEventMessage() {
        disposables.add(PolyvRxBus.get().toObservable(EventMessage.class).subscribe(new Consumer<EventMessage>() {
            @Override
            public void accept(EventMessage eventMessage) throws Exception {
                String event = eventMessage.event;
                String message = eventMessage.message;
                Object eventObject = null;
                int eventType = -1;
                switch (event) {
                    //回答
                    case PolyvChatManager.EVENT_T_ANSWER:
                        PolyvTAnswerEvent tAnswerEvent = PolyvEventHelper.getEventObject(PolyvTAnswerEvent.class, message, event);
                        if (tAnswerEvent != null) {
                            //判断是否是回复自己的
                            if (chatManager.userId.equals(tAnswerEvent.getS_userId())) {
                                eventObject = tAnswerEvent;
                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                                //把带表情的信息解析保存下来
                                tAnswerEvent.setObjects(PolyvTextImageLoader.messageToSpan(tAnswerEvent.getContent(), ConvertUtils.dp2px(14), false, getContext()));
                            }
                        }
                        break;
                }
                //把符合条件的eventObject添加到问答列表中
                if (eventObject != null && eventType != -1) {
                    chatListAdapter.getChatTypeItems().add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType, PolyvChatManager.SE_MESSAGE));
                    chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                    chatMessageList.scrollToBottomOrShowMore(1);

                    //是否需要添加到右上角的未读信息中
                    if (!isSelectedQuiz()) {
                        addUnreadQuiz(1);
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                toast.makeText(getContext(), "聊天室异常，无法接收信息！\n" + throwable.getMessage(), PolyvToast.LENGTH_LONG).show(true);
            }
        }));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部类">
    public class PolyvQuestionTipsEvent {
    }
    // </editor-fold>
}
