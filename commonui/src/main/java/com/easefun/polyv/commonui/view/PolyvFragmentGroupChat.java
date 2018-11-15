package com.easefun.polyv.commonui.view;

import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.businesssdk.sub.gif.RelativeImageSpan;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvConnectStatusListener;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.event.PolyvBanIpEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCloseRoomEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCustomerMessageEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.event.PolyvGongGaoEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvKickEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLikesEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvRemoveContentEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvRemoveHistoryEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvUnshieldEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvHistoryConstant;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.utils.PolyvChatEventBus;
import com.easefun.polyv.commonui.utils.PolyvTextImageLoader;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.widget.PolyvCornerBgTextView;
import com.easefun.polyv.commonui.widget.PolyvMarqueeTextView;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * 群聊
 */
public class PolyvFragmentGroupChat extends PolyvBaseChatFragment {
    //只看讲师信息的集合（讲师包括：管理员、讲师、助教）
    private List<PolyvChatListAdapter.ChatTypeItem> teacherItems = new ArrayList<>();
    //连接状态ui
    private PolyvCornerBgTextView bgStatus;
    //只看讲师、送花
    private ImageView onlyHostSwitch, flower;
    //公告
    private FrameLayout flGongGao;
    private PolyvMarqueeTextView tvGongGao;
    //下拉加载历史记录
    private SwipeRefreshLayout chatPullLoad;
    //当前列表中显示的是否是禁言状态，在当前列表中是否是房间关闭状态，重连时当前列表中是否是房间关闭状态
    private boolean isBanIp, isCloseRoom, isCloseRoomReconnect;
    // 获取的历史记录条数
    private int messageCount = 20;
    // 获取历史记录的次数
    private int count = 1;
    private Disposable gonggaoCdDisposable;

    @Override
    public int layoutId() {
        return R.layout.polyv_fragment_groupchat;
    }

    @Override
    public void loadDataDelay(boolean isFirst) {
        super.loadDataDelay(isFirst);
        if (!isFirst)
            return;
        initCommonView();
        initView();
        //控件初始化之后再接收聊天室的事件
        acceptConnectStatus();
        acceptEventMessage();
    }

    private void loadHistory(final boolean isFirstLoad) {
        disposables.add(
                PolyvApiManager.getPolyvApichatApi()
                        .getChatHistory(chatManager.roomId, (count - 1) * messageCount, count * messageCount, 1)
                        .map(new Function<ResponseBody, JSONArray>() {
                            @Override
                            public JSONArray apply(ResponseBody responseBody) throws Exception {
                                return new JSONArray(responseBody.string());
                            }
                        })
                        .compose(new PolyvRxBaseTransformer<JSONArray, JSONArray>())
                        .map(new Function<JSONArray, JSONArray>() {
                            @Override
                            public JSONArray apply(JSONArray jsonArray) throws Exception {
                                if (jsonArray.length() <= messageCount) {
                                    chatPullLoad.setEnabled(false);
                                    toast.makeText(getContext(), "历史信息已全部加载完成！", PolyvToast.LENGTH_SHORT).show(true);
                                }
                                return jsonArray;
                            }
                        })
                        .observeOn(Schedulers.io())
                        .map(new Function<JSONArray, List<PolyvChatListAdapter.ChatTypeItem>[]>() {
                            @Override
                            public List<PolyvChatListAdapter.ChatTypeItem>[] apply(JSONArray jsonArray) throws Exception {
                                return acceptHistorySpeak(jsonArray, chatManager.userId);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                //需要先登录聊天室再加载历史记录，或者把所需参数传到这里来
                                if (TextUtils.isEmpty(chatManager.roomId) || TextUtils.isEmpty(chatManager.userId))
                                    throw new IllegalArgumentException("roomId or userId is empty");
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                chatPullLoad.setRefreshing(false);
                            }
                        })
                        .subscribe(new Consumer<List<PolyvChatListAdapter.ChatTypeItem>[]>() {
                            @Override
                            public void accept(List<PolyvChatListAdapter.ChatTypeItem>[] listArr) throws Exception {
                                //获取完再添加到聊天列表中
                                updateListData(listArr, isFirstLoad, true);
                                count++;
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                if (throwable instanceof IOException) {
                                    toast.makeText(getContext(), "加载历史信息失败，请重试！", PolyvToast.LENGTH_LONG).show(true);
                                } else {
                                    toast.makeText(getContext(), "加载历史信息失败(" + throwable.getMessage() + ")", PolyvToast.LENGTH_LONG).show(true);
                                }
                            }
                        })
        );
    }

    private void updateListData(List<PolyvChatListAdapter.ChatTypeItem>[] listArr, boolean isFirstLoad, boolean isHistory) {
        if (isHistory) {
            chatTypeItems.addAll(0, listArr[0]);
            teacherItems.addAll(0, listArr[1]);
            if (isOnlyWatchTeacher() && listArr[1].size() > 0) {
                chatListAdapter.notifyItemRangeInserted(0, listArr[1].size());
                chatMessageList.scrollToPosition(isFirstLoad ? chatListAdapter.getItemCount() - 1 : 0);

            } else if (!isOnlyWatchTeacher() && listArr[0].size() > 0) {
                chatListAdapter.notifyItemRangeInserted(0, listArr[0].size());
                chatMessageList.scrollToPosition(isFirstLoad ? chatListAdapter.getItemCount() - 1 : 0);
            }
        } else {
            int srcMaxPosition = chatListAdapter.getItemCount() - 1;
            chatTypeItems.addAll(listArr[0]);
            teacherItems.addAll(listArr[1]);
            if (isOnlyWatchTeacher() && listArr[1].size() > 0) {
                if (listArr[1].size() > 1)
                    chatListAdapter.notifyItemRangeInserted(srcMaxPosition + 1, chatListAdapter.getItemCount() - 1);
                else
                    chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                chatMessageList.scrollToBottomOrShowMore(listArr[1].size());
            } else if (!isOnlyWatchTeacher() && listArr[0].size() > 0) {
                if (listArr[0].size() > 1)
                    chatListAdapter.notifyItemRangeInserted(srcMaxPosition + 1, chatListAdapter.getItemCount() - 1);
                else
                    chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                chatMessageList.scrollToBottomOrShowMore(listArr[0].size());
            }
        }
    }

    private List<PolyvChatListAdapter.ChatTypeItem>[] acceptHistorySpeak(JSONArray jsonArray, String myChatUserId) {
        List<PolyvChatListAdapter.ChatTypeItem> tempChatItems = new ArrayList<>();
        List<PolyvChatListAdapter.ChatTypeItem> tempTeacherChatItems = new ArrayList<>();
        for (int i = 0; i < (jsonArray.length() <= messageCount ? jsonArray.length() : jsonArray.length() - 1); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String messageSource = jsonObject.optString("msgSource");
                if (!TextUtils.isEmpty(messageSource)) {
                    //收/发红包/图片信息，这里仅取图片信息
                    if (PolyvHistoryConstant.MSGSOURCE_CHATIMG.equals(messageSource)) {
                        PolyvChatImgHistory chatImgHistory = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PolyvChatImgHistory.class);
                        int type;
                        //判断信息是否是自己发的
                        if (myChatUserId.equals(chatImgHistory.getUser().getUserId())) {
                            type = PolyvChatListAdapter.ChatTypeItem.TYPE_SEND;
                        } else {
                            type = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                        }
                        PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(chatImgHistory, type);
                        tempChatItems.add(0, chatTypeItem);
                        if (isTeacherType(chatImgHistory.getUser().getUserType())) {
                            tempTeacherChatItems.add(0, chatTypeItem);
                        }
                    }
                    continue;
                }
                JSONObject jsonObject_user = jsonObject.optJSONObject("user");
                if (jsonObject_user != null) {
                    String uid = jsonObject_user.optString("uid");
                    if (PolyvHistoryConstant.UID_REWARD.equals(uid) || PolyvHistoryConstant.UID_CUSTOMMSG.equals(uid)) {
                        //打赏/自定义信息，这里过滤掉
                        continue;
                    }
                    JSONObject jsonObject_content = jsonObject.optJSONObject("content");
                    if (jsonObject_content != null) {
                        //content不为字符串的信息，这里过滤掉
                        continue;
                    }
                    PolyvSpeakHistory speakHistory = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PolyvSpeakHistory.class);
                    int type;
                    //判断信息是否是自己发的
                    if (myChatUserId.equals(speakHistory.getUser().getUserId())) {
                        type = PolyvChatListAdapter.ChatTypeItem.TYPE_SEND;
                    } else {
                        type = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                    }
                    //把带表情的信息解析保存下来
                    speakHistory.setObjects(PolyvTextImageLoader.messageToSpan(speakHistory.getContent(), ConvertUtils.dp2px(14), false, getContext()));
                    PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(speakHistory, type);
                    tempChatItems.add(0, chatTypeItem);
                    if (isTeacherType(speakHistory.getUser().getUserType())) {
                        tempTeacherChatItems.add(0, chatTypeItem);
                    }
                }
            }
        }
        return new List[]{tempChatItems, tempTeacherChatItems};
    }

    private void delayLoadHistory(long time) {
        disposables.add(
                Observable.timer(time, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                chatPullLoad.setEnabled(true);
                                chatPullLoad.setRefreshing(true);
                                loadHistory(true);
                                chatPullLoad.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                    @Override
                                    public void onRefresh() {
                                        loadHistory(false);
                                    }
                                });
                            }
                        })
        );
    }

    private void initView() {
        //下拉控件
        chatPullLoad = findViewById(R.id.chat_pull_load);
        chatPullLoad.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        chatPullLoad.setEnabled(false);
        delayLoadHistory(1666);
        //状态ui
        bgStatus = findViewById(R.id.tv_status);
        //只看讲师
        onlyHostSwitch = findViewById(R.id.iv_switch);
        onlyHostSwitch.setSelected(false);
        onlyHostSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyHostSwitch.setSelected(!onlyHostSwitch.isSelected());
                toast.makeText(getContext(), onlyHostSwitch.isSelected() ? "只看讲师信息" : "查看所有人信息", PolyvToast.LENGTH_LONG).show(true);
                chatListAdapter.setChatTypeItems(onlyHostSwitch.isSelected() ? teacherItems : chatTypeItems);
                chatListAdapter.notifyDataSetChanged();
                chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);
            }
        });
        //送花(实则为聊天室的点赞)
        flower = findViewById(R.id.iv_flower);
        flower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnlyWatchTeacher()) {
                    toast.makeText(getContext(), "目前处于只看讲师状态，不可发言及送花！", PolyvToast.LENGTH_SHORT).show(true);
                    return;
                }
                //发送点赞
                int sendValue = chatManager.sendLikes();
                if (sendValue < 0) {
                    toast.makeText(getContext(), "送花失败：" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
                }
                //成功后不用添加到列表里，因为后续会收到自己的送花事件，在那里添加
            }
        });
        //公告
        flGongGao = findViewById(R.id.fl_gonggao);
        tvGongGao = findViewById(R.id.tv_gonggao);
    }

    @Override
    public void sendMessage() {
        if (isOnlyWatchTeacher()) {
            toast.makeText(getContext(), "目前处于只看讲师状态，不可发言及送花！", PolyvToast.LENGTH_SHORT).show(true);
        } else {
            sendLocalMessage();
        }
    }

    private void sendLocalMessage() {
        String sendMessage = talk.getText().toString();
        if (sendMessage.trim().length() == 0) {
            toast.makeText(getContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show(true);
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(sendMessage);
            int sendValue = chatManager.sendChatMessage(localMessage);
            //添加到列表中
            if (sendValue > 0 || sendValue == PolyvLocalMessage.SENDVALUE_BANIP) {//被禁言后还会显示，但不会广播给其他用户
                talk.setText("");
                hideSoftInputAndEmoList();

                //把带表情的信息解析保存下来
                localMessage.setObjects(PolyvTextImageLoader.messageToSpan(localMessage.getSpeakMessage(), ConvertUtils.dp2px(14), false, getContext()));
                chatTypeItems.add(new PolyvChatListAdapter.ChatTypeItem(localMessage, PolyvChatListAdapter.ChatTypeItem.TYPE_SEND));
                chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
                chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);
                //发送弹幕
                sendDanmu((CharSequence) localMessage.getObjects()[0]);
            } else {
                toast.makeText(getContext(), "发送失败：" + sendValue, PolyvToast.LENGTH_SHORT).show(true);
            }
        }
    }

    private void acceptConnectStatus() {
        disposables.add(PolyvChatEventBus.get().toObservable(ConnectStatus.class).subscribe(new Consumer<ConnectStatus>() {
            @Override
            public void accept(ConnectStatus connectStatus) throws Exception {
                int status = connectStatus.status;
                Throwable t = connectStatus.t;

                switch (status) {
                    case PolyvConnectStatusListener.STATUS_DISCONNECT:
                        if (t != null) {
                            bgStatus.setText("连接失败(" + t.getMessage() + ")");
                            bgStatus.show();
                        }//t为null时为断开重连或者退出，无需处理
                        break;
                    case PolyvConnectStatusListener.STATUS_LOGINING:
                        bgStatus.setText("正在登陆中...");
                        bgStatus.show();
                        break;
                    case PolyvConnectStatusListener.STATUS_LOGINSUCCESS:
                        bgStatus.setText("登陆成功");
                        bgStatus.show(2000);
                        break;
                    case PolyvConnectStatusListener.STATUS_RECONNECTING:
                        isCloseRoomReconnect = isCloseRoom;
                        bgStatus.setText("正在重连中...");
                        bgStatus.show();
                        break;
                    case PolyvConnectStatusListener.STATUS_RECONNECTSUCCESS:
                        bgStatus.setText("重连成功");
                        bgStatus.show(2000);
                        break;
                }
            }
        }));
    }

    private void acceptEventMessage() {
        disposables.add(PolyvChatEventBus.get().toObservable(EventMessage.class).buffer(500, TimeUnit.MILLISECONDS).map(new Function<List<EventMessage>, List<PolyvChatListAdapter.ChatTypeItem>[]>() {
            @Override
            public List<PolyvChatListAdapter.ChatTypeItem>[] apply(List<EventMessage> eventMessages) throws Exception {
                final List<PolyvChatListAdapter.ChatTypeItem> tempChatItems = new ArrayList<>();
                final List<PolyvChatListAdapter.ChatTypeItem> tempTeacherChatItems = new ArrayList<>();
                for (EventMessage eventMessage : eventMessages) {
                    String event = eventMessage.event;
                    String message = eventMessage.message;
                    Object eventObject = null;
                    int eventType = -1;
                    switch (event) {
                        //文本类型发言
                        case PolyvChatManager.EVENT_SPEAK:
                            final PolyvSpeakEvent speakEvent = PolyvEventHelper.getEventObject(PolyvSpeakEvent.class, message, event);
                            if (speakEvent != null) {
                                eventObject = speakEvent;
                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                                //把带表情的信息解析保存下来
                                speakEvent.setObjects(PolyvTextImageLoader.messageToSpan(speakEvent.getValues().get(0), ConvertUtils.dp2px(14), false, getContext()));

                                //判断是不是讲师类型
                                if (isTeacherType(speakEvent.getUser().getUserType())) {
                                    tempTeacherChatItems.add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType));
                                }
                                //判断是不是管理员类型
                                if (PolyvChatManager.USERTYPE_MANAGER.equals(speakEvent.getUser().getUserType())) {
                                    startMarquee((CharSequence) speakEvent.getObjects()[0]);//开启跑马灯公告
                                }
                                //发送弹幕
                                disposables.add(AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendDanmu((CharSequence) speakEvent.getObjects()[0]);
                                    }
                                }));
                            }
                            break;
                        //图片类型发言
                        case PolyvChatManager.EVENT_CHAT_IMG:
                            PolyvChatImgEvent chatImgEvent = PolyvEventHelper.getEventObject(PolyvChatImgEvent.class, message, event);
                            if (chatImgEvent != null) {
                                eventObject = chatImgEvent;
                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;

                                //判断是不是讲师类型
                                if (isTeacherType(chatImgEvent.getUser().getUserType())) {
                                    tempTeacherChatItems.add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType));
                                }
                            }
                            break;
                        //点赞(此处用作送花)
                        case PolyvChatManager.EVENT_LIKES:
                            PolyvLikesEvent likesEvent = PolyvEventHelper.getEventObject(PolyvLikesEvent.class, message, event);
                            if (likesEvent != null) {
                                eventObject = likesEvent;
                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;

                                //把需要显示的信息先解析保存下来
                                String sendNick = likesEvent.getNick();
                                SpannableStringBuilder span = new SpannableStringBuilder(sendNick + " 赠送了鲜花p");
                                Drawable drawable = getContext().getResources().getDrawable(R.drawable.polyv_gift_flower);
                                int textSize = ConvertUtils.dp2px(12);
                                drawable.setBounds(0, 0, textSize * 2, textSize * 2);
                                span.setSpan(new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER), span.length() - 1, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                likesEvent.setObjects(span);
                            }
                            break;
                        //聊天室开启/关闭，于登录事件前收到
                        case PolyvChatManager.EVENT_CLOSEROOM:
                            PolyvCloseRoomEvent closeRoomEvent = PolyvEventHelper.getEventObject(PolyvCloseRoomEvent.class, message, event);
                            if (closeRoomEvent != null) {
                                eventObject = closeRoomEvent;
                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
                                isCloseRoom = closeRoomEvent.getValue().isClosed();
                                if (isCloseRoomReconnect) {
                                    isCloseRoomReconnect = !isCloseRoomReconnect;
                                }
                            }
                            break;
                        //公告
                        case PolyvChatManager.EVENT_GONGGAO:
                            final PolyvGongGaoEvent gongGaoEvent = PolyvEventHelper.getEventObject(PolyvGongGaoEvent.class, message, event);
//                            if (gongGaoEvent != null) {
//                                gongGaoEvent.setObjects(PolyvTextImageLoader.messageToSpan(gongGaoEvent.getContent(), ConvertUtils.dp2px(14), false, getContext()));
//                                startMarquee((CharSequence) gongGaoEvent.getObjects()[0]);
//                            }
                            break;
                        //删除某条聊天记录
                        case PolyvChatManager.EVENT_REMOVE_CONTENT:
                            final PolyvRemoveContentEvent removeContentEvent = PolyvEventHelper.getEventObject(PolyvRemoveContentEvent.class, message, event);
                            if (removeContentEvent != null) {
                                removeItem(tempChatItems, removeContentEvent.getId(), false, false);
                                removeItem(tempTeacherChatItems, removeContentEvent.getId(), true, false);
                                disposables.add(AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        String chatMessageId = removeContentEvent.getId();
                                        removeItemWithMessageId(chatMessageId);
                                    }
                                }));
                            }
                            break;
                        //清空聊天记录
                        case PolyvChatManager.EVENT_REMOVE_HISTORY:
                            final PolyvRemoveHistoryEvent removeHistoryEvent = PolyvEventHelper.getEventObject(PolyvRemoveHistoryEvent.class, message, event);
                            if (removeHistoryEvent != null) {
                                tempChatItems.clear();
                                tempTeacherChatItems.clear();
                                disposables.add(AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        teacherItems.clear();
                                        chatTypeItems.clear();
                                        chatListAdapter.notifyDataSetChanged();
                                        toast.makeText(getContext(), "管理员清空了聊天记录！", PolyvToast.LENGTH_LONG).show(true);
                                    }
                                }));
                            }
                            break;
                        //自定义信息（可以使用自己的方式去显示，这里暂不添加到列表里）
                        case PolyvChatManager.EVENT_CUSTOMER_MESSAGE:
                            PolyvCustomerMessageEvent customerMessageEvent = PolyvEventHelper.getEventObject(PolyvCustomerMessageEvent.class, message, event);
                            if (customerMessageEvent != null) {
//                            eventObject = customerMessageEvent;
//                            eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
                            }
                            break;
                        //踢人
                        case PolyvChatManager.EVENT_KICK:
                            final PolyvKickEvent kickEvent = PolyvEventHelper.getEventObject(PolyvKickEvent.class, message, event);
                            if (kickEvent != null) {
                                disposables.add(AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (chatManager.userId.equals(kickEvent.getUser().getUserId())) {
                                            PolyvBaseActivity.setKickValue(kickEvent.getChannelId(), true);
                                            PolyvBaseActivity.checkKickTips(getActivity(), kickEvent.getChannelId(), "您已被管理员踢出聊天室！");
                                        }
                                    }
                                }));
                            }
                            break;
                        //欢迎语（登录时，用户是否被禁言通过这里的loginEvent获取）
                        case PolyvChatManager.EVENT_LOGIN:
                            PolyvLoginEvent loginEvent = PolyvEventHelper.getEventObject(PolyvLoginEvent.class, message, event);
                            if (loginEvent != null) {
                                eventObject = loginEvent;
                                eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
                                if (chatManager.userId.equals(loginEvent.getUser().getUserId())) {
                                    //判断是否被禁言/解除禁言
//                                    if (isBanIp != loginEvent.getUser().isBanned()) {
//                                        tempChatItems.add(new PolyvChatListAdapter.ChatTypeItem(loginEvent.getUser().isBanned() ? new PolyvBanIpEvent() : new PolyvUnshieldEvent(), PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS));
//                                    }
                                    //判断重连时列表中是否是房间关闭状态
                                    if (isCloseRoomReconnect) {
                                        isCloseRoom = false;
                                        //自定义房间打开的事件添加到列表中
                                        PolyvCloseRoomEvent closeRoomEventS = new PolyvCloseRoomEvent();
                                        PolyvCloseRoomEvent.ValueBean valueBean = new PolyvCloseRoomEvent.ValueBean();
                                        valueBean.setClosed(isCloseRoom);
                                        closeRoomEventS.setValue(valueBean);
                                        tempChatItems.add(new PolyvChatListAdapter.ChatTypeItem(closeRoomEventS, PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS));
                                    }
                                }
                            }
                            break;
                        //禁言
                        case PolyvChatManager.EVENT_BANIP:
                            PolyvBanIpEvent banIpEvent = PolyvEventHelper.getEventObject(PolyvBanIpEvent.class, message, event);
//                            if (banIpEvent != null) {
//                                if (banIpEvent.getUserIds() != null) {
//                                    for (PolyvBanIpEvent.UserIdsBean userIdsBean : banIpEvent.getUserIds()) {
//                                        if (chatManager.userId.equals(userIdsBean.getUserId())) {//通过userId判断是否自己被禁言了
//                                            eventObject = banIpEvent;
//                                            eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
//                                            isBanIp = true;
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
                            break;
                        //解除禁言
                        case PolyvChatManager.EVENT_UNSHIELD:
                            PolyvUnshieldEvent unshieldEvent = PolyvEventHelper.getEventObject(PolyvUnshieldEvent.class, message, event);
//                            if (unshieldEvent != null) {
//                                if (unshieldEvent.getUserIds() != null) {
//                                    for (PolyvUnshieldEvent.UserIdsBean userIdsBean : unshieldEvent.getUserIds()) {
//                                        if (chatManager.userId.equals(userIdsBean.getUserId())) {//通过userId判断是否自己被解除禁言了
//                                            eventObject = unshieldEvent;
//                                            eventType = PolyvChatListAdapter.ChatTypeItem.TYPE_TIPS;
//                                            isBanIp = false;
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
                            break;
                    }
                    //把符合条件的eventObject添加到聊天列表中
                    if (eventObject != null && eventType != -1) {
                        tempChatItems.add(new PolyvChatListAdapter.ChatTypeItem(eventObject, eventType));
                    }
                }
                return new List[]{tempChatItems, tempTeacherChatItems};
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<PolyvChatListAdapter.ChatTypeItem>[]>() {
            @Override
            public void accept(List<PolyvChatListAdapter.ChatTypeItem>[] listArr) throws Exception {
                updateListData(listArr, false, false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                toast.makeText(getContext(), "聊天室异常，无法接收信息(" + throwable.getMessage() + ")", PolyvToast.LENGTH_LONG).show(true);
            }
        }));
    }

    private void removeItem(List<PolyvChatListAdapter.ChatTypeItem> lists, String chatMessageId, boolean isTeacherLists, boolean notifiyList) {
        for (int i = 0; i < lists.size(); i++) {
            PolyvChatListAdapter.ChatTypeItem chatTypeItem = lists.get(i);
            if (chatTypeItem.object instanceof PolyvSpeakEvent) {
                if (chatMessageId.equals(((PolyvSpeakEvent) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            } else if (chatTypeItem.object instanceof PolyvSpeakHistory) {
                if (chatMessageId.equals(((PolyvSpeakHistory) chatTypeItem.object).getId())) {
                    lists.remove(chatTypeItem);
                    if (notifiyList &&
                            (!isOnlyWatchTeacher() && !isTeacherLists
                                    || (isOnlyWatchTeacher() && isTeacherLists))) {
                        chatListAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            }
        }
    }

    private void removeItemWithMessageId(String chatMessageId) {
        if (TextUtils.isEmpty(chatMessageId))
            return;
        removeItem(chatTypeItems, chatMessageId, false, true);
        removeItem(teacherItems, chatMessageId, true, true);
    }

    //如果只看讲师，那么不能发言以及送花
    private boolean isOnlyWatchTeacher() {
        return onlyHostSwitch.isSelected();
    }

    public static boolean isTeacherType(String userType) {
        //这里把管理员、讲师、助教都视为只看讲师的类型，适用于只看讲师
        return PolyvChatManager.USERTYPE_MANAGER.equals(userType)
                || PolyvChatManager.USERTYPE_TEACHER.equals(userType)
                || PolyvChatManager.USERTYPE_ASSISTANT.equals(userType);
    }

    private void startMarquee(final CharSequence msg) {
        disposableGonggaoCd();
        disposables.add(AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
            @Override
            public void run() {
                flGongGao.setVisibility(View.VISIBLE);
                tvGongGao.setText(msg);
                tvGongGao.setOnGetRollDurationListener(new PolyvMarqueeTextView.OnGetRollDurationListener() {
                    @Override
                    public void onFirstGetRollDuration(int rollDuration) {
                        startCountDown(rollDuration * 3);
                    }
                });
                tvGongGao.stopScroll();
                tvGongGao.startScroll();
            }
        }));
    }

    private void startCountDown(long time) {
        gonggaoCdDisposable = Observable.timer(time, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        tvGongGao.stopScroll();
                        flGongGao.setVisibility(View.GONE);
                        flGongGao.clearAnimation();
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1f, 1f, 0f);
                        scaleAnimation.setDuration(555);
                        flGongGao.startAnimation(scaleAnimation);
                    }
                });
    }

    private void disposableGonggaoCd() {
        if (gonggaoCdDisposable != null) {
            gonggaoCdDisposable.dispose();
            gonggaoCdDisposable = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposableGonggaoCd();
        if (chatListAdapter != null && chatListAdapter.getLoadImgMap() != null) {
            for (String key : chatListAdapter.getLoadImgMap().keySet()) {
                for (int value : chatListAdapter.getLoadImgMap().get(key)) {
                    PolyvMyProgressManager.removeListener(key, value);
                }
            }
        }
    }
}
