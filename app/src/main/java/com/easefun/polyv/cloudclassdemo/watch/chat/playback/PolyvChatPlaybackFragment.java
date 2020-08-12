package com.easefun.polyv.cloudclassdemo.watch.chat.playback;

import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.easefun.polyv.businesssdk.api.common.player.listener.IPolyvVideoViewListenerEvent;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.chat.PolyvChatAuthorization;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.event.PolyvEventHelper;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackBase;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackImg;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackSpeak;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendChatImageHelper;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendChatImageListener;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendChatImgEvent;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatBaseFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.utils.PolyvTextImageLoader;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseRetryFunction;
import com.easefun.polyv.foundationsdk.utils.PolyvTimeUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * 聊天回放
 */
public class PolyvChatPlaybackFragment extends PolyvChatBaseFragment {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    //用户信息
    private String viewerId, channelId, viewerName, imageUrl;
    private PolyvChatAuthorization chatAuthorization;
    //vid
    private String videoId;
    //聊天回放item
    private List<PolyvChatListAdapter.ChatTypeItem> chatPlaybackItems;
    //disposable
    private Disposable getChatPlaybackDisposable;
    //播放器seek后的位置
    private int seekPosition = -1;

    private static int DATATIME = 60 * 5, AHEADTIME = 30;//5分钟，提前30秒
    private static int MESSAGECOUNT = 300;//300条
    //下次请求接口的时间参数，下次请求接口提前的时间
    private int nextGetNewDataTime = DATATIME;
    //接口请求的时间、非id接口请求的时间
    private int second, timeRequestSecond;
    //接口请求的id
    private int id;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    @Override
    public int layoutId() {
        return R.layout.polyv_fragment_playbackchat;
    }

    @Override
    public void loadDataAhead() {
        super.loadDataAhead();
        initArguments();
        initCommonView();
        initMoreLayout();
        initView();
        startChatPlaybackTask();
        getChatPlaybackList(false, false);
    }

    private void initArguments() {
        videoId = getArguments().getString("videoId");
    }

    private void initView() {
        talk.setHint("我也来聊几句...");
        selectPhotoLayout.setVisibility(View.VISIBLE);
        openCameraLayout.setVisibility(View.VISIBLE);
        openBulletinLayout.setVisibility(View.GONE);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fragment方法">
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getChatPlaybackDisposable != null) {
            getChatPlaybackDisposable.dispose();
            getChatPlaybackDisposable = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="聊天回放任务">
    private void startChatPlaybackTask() {
        //seek监听
        listenSeekComplete();
        //定时任务
        disposables.add(Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return getVideoView() != null && chatPlaybackItems != null;
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        int position = getVideoView().getCurrentPosition() / 1000;
                        addItemAtPosition(position);
                        //获取新数据
                        if (position >= nextGetNewDataTime - AHEADTIME) {
                            second = nextGetNewDataTime;
                            getChatPlaybackList(true, false);

                            nextGetNewDataTime = nextGetNewDataTime + DATATIME;
                        }
                    }
                }));
    }

    private void addItemAtPosition(int position) {
        List<PolyvChatListAdapter.ChatTypeItem> showChatTypeItem = new ArrayList<>();
        for (PolyvChatListAdapter.ChatTypeItem chatTypeItem : chatPlaybackItems) {
            if (seekPosition != -1) {
                if (chatTypeItem.showTime >= seekPosition && chatTypeItem.showTime <= position) {
                    showChatTypeItem.add(chatTypeItem);
                }
            } else if (chatTypeItem.showTime <= position) {
                showChatTypeItem.add(chatTypeItem);
            }
        }
        if (showChatTypeItem.size() > 0) {
            chatPlaybackItems.removeAll(showChatTypeItem);
            chatTypeItems.addAll(showChatTypeItem);
            chatListAdapter.notifyItemRangeInserted(chatListAdapter.getItemCount() - showChatTypeItem.size(), showChatTypeItem.size());
            chatMessageList.scrollToBottomOrShowMore(showChatTypeItem.size());
        }
    }

    private void listenSeekComplete() {
        if (getVideoView() != null) {
            getVideoView().setOnSeekCompleteListener(new IPolyvVideoViewListenerEvent.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete() {
                    if (getVideoView() != null) {
                        seekPosition = getVideoView().getCurrentPosition() / 1000;
                    } else {
                        seekPosition = -1;
                    }
                    if (seekPosition != -1) {
                        nextGetNewDataTime = seekPosition + DATATIME;
                        chatPlaybackItems = null;
                        chatMessageList.hideUnredaView();
                        chatTypeItems.clear();
                        chatListAdapter.notifyDataSetChanged();

                        second = seekPosition;
                        getChatPlaybackList(false, false);
                    }
                }
            });
        }
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="设置学员信息">

    /**
     * 设置聊天回放时学员的信息，学员的信息请保持和直播登录聊天室的一致
     *
     * @param viewerId      学员id
     * @param channelId     频道号
     * @param viewerName    学员昵称
     * @param imageUrl      头像，如果直播没传，则这里传null
     * @param authorization 头衔，如果直播没传，则这里传null
     */
    public void setViewerInfo(String viewerId, String channelId, String viewerName, String imageUrl, PolyvChatAuthorization authorization) {
        this.viewerId = viewerId;
        this.channelId = channelId;
        this.viewerName = viewerName;
        this.imageUrl = TextUtils.isEmpty(imageUrl) ? PolyvChatManager.DEFAULT_AVATARURL : imageUrl;
        this.chatAuthorization = authorization;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取聊天回放信息">
    private void getChatPlaybackList(final boolean isAutoRequest, final boolean isId) {
        if (getChatPlaybackDisposable != null) {
            getChatPlaybackDisposable.dispose();
        }
        if (!isId) {
            timeRequestSecond = second;
        }
        getChatPlaybackDisposable = PolyvChatApiRequestHelper.getInstance()
                .getChatPlaybackMessage(videoId, MESSAGECOUNT, second, id, PolyvChatApiRequestHelper.ORIGIN_PLAYBACK, isId)
                .retryWhen(new PolyvRxBaseRetryFunction(Integer.MAX_VALUE, 3000))
                .observeOn(Schedulers.io())
                .map(new Function<ResponseBody, ChatPlaybackDataBean>() {
                    @Override
                    public ChatPlaybackDataBean apply(ResponseBody responseBody) throws Exception {
                        return acceptChatPlaybackMsg(new JSONArray(responseBody.string()), viewerId, isId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChatPlaybackDataBean>() {
                    @Override
                    public void accept(ChatPlaybackDataBean s) throws Exception {
                        if (chatPlaybackItems == null) {
                            chatPlaybackItems = new ArrayList<>();
                        }
                        if (isId || isAutoRequest) {
                            chatPlaybackItems.addAll(s.chatTypeItems);
                        } else {
                            chatPlaybackItems = s.chatTypeItems;
                        }
                        if (s.dataLength == MESSAGECOUNT && s.lastDataTime <= (DATATIME + timeRequestSecond) && s.lastDataId != -1) {
                            id = s.lastDataId;
                            second = s.lastDataTime;
                            getChatPlaybackList(false, true);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        toast.makeText(getContext(), "加载回放信息失败(" + throwable.getMessage() + ")", PolyvToast.LENGTH_LONG).show(true);
                    }
                });
    }

    private ChatPlaybackDataBean acceptChatPlaybackMsg(JSONArray jsonArray, String myChatUserId, boolean isId) throws Exception {
        List<PolyvChatListAdapter.ChatTypeItem> tempChatItems = new ArrayList<>();
        int dataLength = jsonArray.length();
        int lastDataTime = -1;
        int lastDataId = -1;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (isId && i == 0) {
                continue;
            }
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null) {
                String msgType = jsonObject.optString("msgType");
                lastDataTime = toSecond(jsonObject.optString("time"));
                lastDataId = jsonObject.optInt("id");
                PolyvChatPlaybackBase chatPlaybackBase = null;
                if (PolyvChatPlaybackSpeak.MSGTYPE_SPEAK.equals(msgType)) {
                    chatPlaybackBase = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PolyvChatPlaybackSpeak.class);
                    chatPlaybackBase.setObjects(PolyvTextImageLoader.messageToSpan(chatPlaybackBase.getMsg(), ConvertUtils.dp2px(14), false, getContext()));
                } else if (PolyvChatPlaybackImg.MSGTYPE_CHATIMG.equals(msgType)) {
                    chatPlaybackBase = PolyvEventHelper.gson.fromJson(jsonObject.toString(), PolyvChatPlaybackImg.class);
                }
                if (chatPlaybackBase != null && chatPlaybackBase.getUser() != null) {
                    int type;
                    //判断信息是否是自己发的
                    if (myChatUserId.equals(chatPlaybackBase.getUser().getUserId())) {
                        type = PolyvChatListAdapter.ChatTypeItem.TYPE_SEND;
                    } else {
                        type = PolyvChatListAdapter.ChatTypeItem.TYPE_RECEIVE;
                    }
                    PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(chatPlaybackBase, type, PolyvChatManager.SE_MESSAGE);
                    int chatPlaybackTime = toSecond(chatPlaybackBase.getTime());
                    if (chatPlaybackTime < DATATIME + timeRequestSecond) {
                        chatTypeItem.showTime = chatPlaybackTime;
                        tempChatItems.add(chatTypeItem);
                    }
                }
            }
        }
        return new ChatPlaybackDataBean(tempChatItems, dataLength, lastDataTime, lastDataId);
    }

    private int toSecond(String videoFormatTime) {
        try {
            String[] strings = videoFormatTime.split(":");
            return PolyvTimeUtils.formatToSecond(Integer.valueOf(strings[0]), Integer.valueOf(strings[1]), Integer.valueOf(strings[2]));
        } catch (Exception e) {
        }
        return -1;
    }

    public static class ChatPlaybackDataBean {
        private List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems;
        private int dataLength;
        private int lastDataTime;
        private int lastDataId;

        public ChatPlaybackDataBean(List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems, int dataLength, int lastDataTime, int lastDataId) {
            this.chatTypeItems = chatTypeItems;
            this.dataLength = dataLength;
            this.lastDataTime = lastDataTime;
            this.lastDataId = lastDataId;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="发送聊天回放信息">
    @Override
    protected void sendImage(PolyvSendLocalImgEvent sendLocalImgEvent, String sessionId) {
        try {
            PolyvSendChatImageHelper.sendChatImage(channelId, sendLocalImgEvent, new PolyvSendChatImageListener() {
                @Override
                public void onUploadFail(PolyvSendLocalImgEvent localImgEvent, Throwable t) {
                    onImgUploadFail(localImgEvent, t);
                }

                @Override
                public void onSendFail(PolyvSendLocalImgEvent localImgEvent, int sendValue) {
                    onImgSendFail(localImgEvent, sendValue);
                }

                @Override
                public void onSuccess(PolyvSendLocalImgEvent localImgEvent, String uploadImgUrl, String imgId) {
                    requestSendPlaybackMessageApi(null, localImgEvent, uploadImgUrl, imgId, true);
                }

                @Override
                public void onProgress(PolyvSendLocalImgEvent localImgEvent, float progress) {
                    onImgProgress(localImgEvent, progress);
                }
            }, disposables);
        } catch (Exception e) {
            onImgUploadFail(sendLocalImgEvent, e);
        }
    }

    @Override
    public void sendMessage() {
        sendPlaybackMessage();
    }

    private void sendPlaybackMessage() {
        String sendMessage = talk.getText().toString();
        if (sendMessage.trim().length() == 0) {
            toast.makeText(getContext(), "发送内容不能为空！", Toast.LENGTH_SHORT).show(true);
        } else {
            PolyvLocalMessage localMessage = new PolyvLocalMessage(sendMessage);

            //添加到列表中
            talk.setText("");
            hideSoftInputAndEmoList();

            //把带表情的信息解析保存下来
            localMessage.setObjects(PolyvTextImageLoader.messageToSpan(localMessage.getSpeakMessage(), ConvertUtils.dp2px(14), false, getContext()));
            PolyvChatListAdapter.ChatTypeItem chatTypeItem = new PolyvChatListAdapter.ChatTypeItem(localMessage, PolyvChatListAdapter.ChatTypeItem.TYPE_SEND, PolyvChatManager.SE_MESSAGE);
            chatTypeItems.add(chatTypeItem);
            chatListAdapter.notifyItemInserted(chatListAdapter.getItemCount() - 1);
            chatMessageList.scrollToPosition(chatListAdapter.getItemCount() - 1);

            //发送
            requestSendPlaybackMessageApi(localMessage.getSpeakMessage());
        }
    }

    private void requestSendPlaybackMessageApi(String message) {
        requestSendPlaybackMessageApi(message, null, null, null, false);
    }

    private void requestSendPlaybackMessageApi(String message, final PolyvSendLocalImgEvent localImgEvent, final String uploadImgUrl, final String imgId, final boolean isImgMessage) {
        if (getVideoView() != null) {
            int second = getVideoView().getCurrentPosition() / 1000;
            String user;
            try {
                user = PolyvChatApiRequestHelper.getInstance().generateUser(viewerId, channelId, viewerName, imageUrl, chatAuthorization, PolyvChatManager.USERTYPE_SLICE);
            } catch (Exception e) {
                toast.makeText(getContext(), "发送失败：(" + e.getMessage() + ")", PolyvToast.LENGTH_SHORT).show(true);
                return;
            }

            if (isImgMessage) {
                //图片信息
                PolyvSendChatImgEvent.ValueBean valueBean = new PolyvSendChatImgEvent.ValueBean();
                valueBean.setUploadImgUrl(uploadImgUrl);
                valueBean.setType("chatImg");
                valueBean.setStatus("upLoadingSuccess");
                valueBean.setId(imgId);
                PolyvSendChatImgEvent.ValueBean.SizeBean sizeBean = new PolyvSendChatImgEvent.ValueBean.SizeBean();
                sizeBean.setWidth(localImgEvent.getWidth());
                sizeBean.setHeight(localImgEvent.getHeight());
                valueBean.setSize(sizeBean);
                message = PolyvEventHelper.gson.toJson(valueBean);
            }

            disposables.add(
                    PolyvChatApiRequestHelper.getInstance().sendChatPlaybackMessage(videoId, message, second, PolyvChatApiRequestHelper.ORIGIN_PLAYBACK,
                            isImgMessage ? PolyvChatPlaybackImg.MSGTYPE_CHATIMG : PolyvChatPlaybackSpeak.MSGTYPE_SPEAK, user, getSessionId())
                            .observeOn(Schedulers.io())
                            .map(new Function<ResponseBody, String>() {
                                @Override
                                public String apply(ResponseBody responseBody) throws Exception {
                                    return responseBody.string();
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String string) throws Exception {
                                    JSONObject jsonObject = new JSONObject(string);
                                    String message = jsonObject.optString("message");
                                    if (jsonObject.optInt("code") != 200) {
                                        if (isImgMessage) {
                                            onImgUploadFail(localImgEvent, new Exception(message));
                                        } else {
                                            toast.makeText(getContext(), "发送失败：(" + message + ")", PolyvToast.LENGTH_SHORT).show(true);
                                        }
                                    } else if (isImgMessage) {
                                        onImgSuccess(localImgEvent, uploadImgUrl, imgId);
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    toast.makeText(getContext(), "发送失败：(" + throwable.getMessage() + ")", PolyvToast.LENGTH_SHORT).show(true);
                                }
                            })
            );
        }
    }
    // </editor-fold>
}
