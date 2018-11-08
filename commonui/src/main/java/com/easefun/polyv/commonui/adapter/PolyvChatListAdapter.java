package com.easefun.polyv.commonui.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.easefun.polyv.businesssdk.sub.gif.GifSpanTextView;
import com.easefun.polyv.cloudclass.chat.PolyvChatAuthorization;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvLocalMessage;
import com.easefun.polyv.cloudclass.chat.PolyvQuestionMessage;
import com.easefun.polyv.cloudclass.chat.event.PolyvBanIpEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCloseRoomEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLikesEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvTAnswerEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvUnshieldEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvCircleProgressView;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvOnProgressListener;
import com.easefun.polyv.commonui.view.PolyvFragmentGroupChat;
import com.easefun.polyv.commonui.view.PolyvFragmentPrivateChat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolyvChatListAdapter extends PolyvBaseRecyclerViewAdapter {
    private List<ChatTypeItem> chatTypeItems;
    private RequestOptions requestOptions_t;
    private RequestOptions requestOptions_s;
    private OnChatImgViewClickListener onChatImgViewClickListener;
    private Map<String, List<Integer>> loadImgMap = new HashMap<>();

    private PolyvChatListAdapter(RecyclerView recyclerView) {
        super(recyclerView);
    }

    public PolyvChatListAdapter(RecyclerView recyclerView, List<ChatTypeItem> chatTypeItems) {
        this(recyclerView);
        this.chatTypeItems = chatTypeItems;
    }

    public void setChatTypeItems(List<ChatTypeItem> chatTypeItems) {
        this.chatTypeItems = chatTypeItems;
    }

    public List<ChatTypeItem> getChatTypeItems() {
        return chatTypeItems;
    }

    public Map<String, List<Integer>> getLoadImgMap() {
        return loadImgMap;
    }

    private void putImgUrl(String imgUrl, int position) {
        List<Integer> values = loadImgMap.get(imgUrl);
        if (values != null) {
            boolean isExisted = false;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) == position) {
                    isExisted = true;
                    break;
                }
            }
            if (!isExisted) {
                values.add(position);
            }
        } else {
            values = new ArrayList<>();
            values.add(position);
            loadImgMap.put(imgUrl, values);
        }
    }

    private void removeImgUrl(String imgUrl, int position) {
        List<Integer> values = loadImgMap.get(imgUrl);
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                int ePosition = values.get(i);
                if (ePosition == position) {
                    values.remove(i);
                    break;
                }
            }
        }
    }

    public void setOnChatImgViewClickListener(OnChatImgViewClickListener l) {
        this.onChatImgViewClickListener = l;
    }

    public interface OnChatImgViewClickListener {
        void onClick(ImageView iv, int position);
    }

    public static class ChatTypeItem {
        public static final int TYPE_RECEIVE = 0;
        public static final int TYPE_SEND = 1;
        public static final int TYPE_TIPS = 2;

        @IntDef({
                TYPE_RECEIVE,
                TYPE_SEND,
                TYPE_TIPS
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface Type {
        }

        public Object object;
        public int type;

        public ChatTypeItem(Object object, @Type int type) {
            this.object = object;
            this.type = type;
        }

        @Override
        public String toString() {
            return "ChatTypeItem{" +
                    "object=" + object +
                    ", type=" + type +
                    '}';
        }
    }

    public class ReceiveMessageHolder extends PolyvBaseRecyclerViewAdapter.ClickableViewHolder {
        private ImageView avatar;
        private TextView type/*头衔*/, nick;
        private GifSpanTextView receiveMessage;
        private ImageView chatImg;
        private PolyvCircleProgressView imgLoading;

        public ReceiveMessageHolder(View itemView) {
            super(itemView);
            avatar = $(R.id.iv_avatar);
            type = $(R.id.tv_type);
            nick = $(R.id.tv_nick);
            receiveMessage = $(R.id.gtv_receive_message);
            chatImg = $(R.id.iv_chat_img);
            imgLoading = $(R.id.cpv_img_loading);
        }
    }

    public class SendMessageHolder extends PolyvBaseRecyclerViewAdapter.ClickableViewHolder {
        private GifSpanTextView sendMessage;

        public SendMessageHolder(View itemView) {
            super(itemView);
            sendMessage = $(R.id.gtv_send_message);
        }
    }

    public class TipsMessageHolder extends PolyvBaseRecyclerViewAdapter.ClickableViewHolder {
        private TextView tipsMessage;

        public TipsMessageHolder(View itemView) {
            super(itemView);
            tipsMessage = $(R.id.tv_tips_message);
        }
    }

    @NonNull
    @Override
    public ClickableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        ClickableViewHolder clickableViewHolder = null;
        switch (viewType) {
            case ChatTypeItem.TYPE_TIPS:
                clickableViewHolder = new TipsMessageHolder(LayoutInflater.from(getContext()).inflate(R.layout.polyv_chat_tips_message_item, parent, false));
                break;
            case ChatTypeItem.TYPE_SEND:
                clickableViewHolder = new SendMessageHolder(LayoutInflater.from(getContext()).inflate(R.layout.polyv_chat_send_message_item, parent, false));
                break;
            case ChatTypeItem.TYPE_RECEIVE:
                clickableViewHolder = new ReceiveMessageHolder(LayoutInflater.from(getContext()).inflate(R.layout.polyv_chat_receive_message_item, parent, false));
                break;
        }
        return clickableViewHolder;
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        ChatTypeItem chatTypeItem = chatTypeItems.get(position);
        if (holder instanceof ReceiveMessageHolder) {
            handleReceiveMessage((ReceiveMessageHolder) holder, chatTypeItem.object, position);
        } else if (holder instanceof SendMessageHolder) {
            handleSendMessage((SendMessageHolder) holder, chatTypeItem.object);
        } else if (holder instanceof TipsMessageHolder) {
            handleTipsMessage((TipsMessageHolder) holder, chatTypeItem.object);
        }
        super.onBindViewHolder(holder, position);
    }

    private void handleReceiveMessage(ReceiveMessageHolder receiveMessageHolder, Object object, int position) {
        String userType, actor, nick, pic;
        CharSequence message = null;
        String chatImg = null;
        int imgHeight = 0;
        int imgWidth = 0;
        PolyvChatAuthorization chatAuthorization = null;
        // 接收的发言信息
        if (object instanceof PolyvSpeakEvent) {
            PolyvSpeakEvent speakEvent = (PolyvSpeakEvent) object;
            userType = speakEvent.getUser().getUserType();
            actor = speakEvent.getUser().getActor();
            nick = speakEvent.getUser().getNick();
            pic = speakEvent.getUser().getPic();
            message = (CharSequence) speakEvent.getObjects()[0];
            PolyvSpeakEvent.UserBean.AuthorizationBean authorizationBean;
            if ((authorizationBean = speakEvent.getUser().getAuthorization()) != null) {//自定义授权头衔
                chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
            }
        } else if (object instanceof PolyvChatImgEvent) {//图片信息
            PolyvChatImgEvent chatImgEvent = (PolyvChatImgEvent) object;
            userType = chatImgEvent.getUser().getUserType();
            actor = chatImgEvent.getUser().getActor();
            nick = chatImgEvent.getUser().getNick();
            pic = chatImgEvent.getUser().getPic();
            chatImg = chatImgEvent.getValues().get(0).getUploadImgUrl();
            imgHeight = (int) chatImgEvent.getValues().get(0).getSize().getHeight();
            imgWidth = (int) chatImgEvent.getValues().get(0).getSize().getWidth();
            PolyvChatImgEvent.UserBean.AuthorizationBean authorizationBean;
            if ((authorizationBean = chatImgEvent.getUser().getAuthorization()) != null) {
                chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
            }
        } else if (object instanceof PolyvTAnswerEvent) {//回答信息
            PolyvTAnswerEvent tAnswerEvent = (PolyvTAnswerEvent) object;
            userType = tAnswerEvent.getUser().getUserType();
            actor = tAnswerEvent.getUser().getActor();
            nick = tAnswerEvent.getUser().getNick();
            pic = tAnswerEvent.getUser().getPic();
            message = (CharSequence) tAnswerEvent.getObjects()[0];
            PolyvTAnswerEvent.UserBean.AuthorizationBean authorizationBean;
            if ((authorizationBean = tAnswerEvent.getUser().getAuthorization()) != null) {
                chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
            }
        } else if (object instanceof PolyvSpeakHistory) {//历史他人的发言信息
            PolyvSpeakHistory speakHistory = (PolyvSpeakHistory) object;
            userType = speakHistory.getUser().getUserType();
            actor = speakHistory.getUser().getActor();
            nick = speakHistory.getUser().getNick();
            pic = speakHistory.getUser().getPic();
            message = (CharSequence) speakHistory.getObjects()[0];
            PolyvSpeakHistory.UserBean.AuthorizationBean authorizationBean;
            if ((authorizationBean = speakHistory.getUser().getAuthorization()) != null) {
                chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
            }
        } else if (object instanceof PolyvChatImgHistory) {//历史图片信息
            PolyvChatImgHistory chatImgHistory = (PolyvChatImgHistory) object;
            userType = chatImgHistory.getUser().getUserType();
            actor = chatImgHistory.getUser().getActor();
            nick = chatImgHistory.getUser().getNick();
            pic = chatImgHistory.getUser().getPic();
            chatImg = chatImgHistory.getContent().getUploadImgUrl();
            imgHeight = (int) chatImgHistory.getContent().getSize().getHeight();
            imgWidth = (int) chatImgHistory.getContent().getSize().getWidth();
            PolyvChatImgHistory.UserBean.AuthorizationBean authorizationBean;
            if ((authorizationBean = chatImgHistory.getUser().getAuthorization()) != null) {
                chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
            }
        } else if (object instanceof PolyvFragmentPrivateChat.PolyvQuestionTipsEvent) {//自定义的私聊的问候语信息
            userType = PolyvChatManager.USERTYPE_TEACHER;
            actor = "讲师";
            nick = "讲师";
            pic = "http://livestatic.videocc.net/uploaded/images/webapp/avatar/default-teacher.png";
            message = "同学，您好！请问有什么问题吗？";
        } else {
            return;
        }
        acceptReceiveMessage(receiveMessageHolder, userType, actor, nick, pic, message, chatImg, imgHeight, imgWidth, chatAuthorization, position);
    }

    private void acceptReceiveMessage(final ReceiveMessageHolder receiveMessageHolder, String userType, String actor, String nick, final String pic, CharSequence message, final String chatImg, int height, int width, PolyvChatAuthorization chatAuthorization, final int position) {
        //加载头像
        if (requestOptions_t == null) {
            requestOptions_t = new RequestOptions()
                    .placeholder(R.drawable.polyv_default_teacher)
                    .error(R.drawable.polyv_default_teacher)
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
        }
        if (requestOptions_s == null) {
            requestOptions_s = new RequestOptions()
                    .placeholder(R.drawable.polyv_missing_face)
                    .error(R.drawable.polyv_missing_face)
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
        }
        Glide.with(getContext())
                .load(pic)
                .apply(PolyvFragmentGroupChat.isTeacherType(userType) ? requestOptions_t : requestOptions_s)
                .into(receiveMessageHolder.avatar);
        //设置昵称
        receiveMessageHolder.nick.setText(nick);
        //设置头衔
        if (chatAuthorization != null) {
            receiveMessageHolder.type.setVisibility(View.VISIBLE);
            receiveMessageHolder.type.setText(chatAuthorization.getActor());
            receiveMessageHolder.type.getBackground().setColorFilter(Color.parseColor(chatAuthorization.getBgColor()), PorterDuff.Mode.SRC_OVER);
            receiveMessageHolder.type.setTextColor(Color.parseColor(chatAuthorization.getfColor()));
        } else if (!TextUtils.isEmpty(actor)) {
            receiveMessageHolder.type.setVisibility(View.VISIBLE);
            receiveMessageHolder.type.setText(actor);
            receiveMessageHolder.type.getBackground().setColorFilter(Color.parseColor(PolyvChatAuthorization.BGCOLOR_DEFAULT), PorterDuff.Mode.SRC_OVER);
            receiveMessageHolder.type.setTextColor(Color.parseColor(PolyvChatAuthorization.FCOLOR_DEFAULT));
        } else {
            receiveMessageHolder.type.setVisibility(View.GONE);
        }
        //设置其发言信息
        if (message != null) {//设置文本类型的发言信息
            receiveMessageHolder.chatImg.setVisibility(View.GONE);
            receiveMessageHolder.imgLoading.setVisibility(View.GONE);
            receiveMessageHolder.receiveMessage.setVisibility(View.VISIBLE);
            receiveMessageHolder.receiveMessage.setText(message);
        } else if (chatImg != null) {//设置图片类型的发言信息
            receiveMessageHolder.receiveMessage.setVisibility(View.GONE);
            receiveMessageHolder.chatImg.setVisibility(View.VISIBLE);
            receiveMessageHolder.chatImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onChatImgViewClickListener != null) {
                        onChatImgViewClickListener.onClick(receiveMessageHolder.chatImg, position);
                    }
                }
            });
            receiveMessageHolder.imgLoading.setVisibility(View.GONE);
            receiveMessageHolder.imgLoading.setProgress(0);

            int maxLength = ConvertUtils.dp2px(132);
            int minLength = ConvertUtils.dp2px(50);
            //计算显示的图片大小
            float percentage = width * 1f / height;
            if (percentage == 1) {//方图
                if (width < minLength) {
                    width = height = minLength;
                } else if (width > maxLength) {
                    width = height = maxLength;
                }
            } else if (percentage < 1) {//竖图
                height = maxLength;
                width = (int) Math.max(minLength, height * percentage);
            } else {//横图
                width = maxLength;
                height = (int) Math.max(minLength, width / percentage);
            }
            ViewGroup.LayoutParams vlp = receiveMessageHolder.chatImg.getLayoutParams();
            vlp.width = width;
            vlp.height = height;
            receiveMessageHolder.chatImg.setLayoutParams(vlp);
            //加载图片
            PolyvMyProgressManager.removeListener(chatImg, position);
            final PolyvOnProgressListener onProgressListener = new PolyvOnProgressListener() {
                @Override
                public void onStart(String url) {
                    receiveMessageHolder.imgLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                    if (isComplete) {
                        receiveMessageHolder.imgLoading.setVisibility(View.GONE);
                    } else {
                        receiveMessageHolder.imgLoading.setVisibility(View.VISIBLE);
                        receiveMessageHolder.imgLoading.setProgress(percentage);
                    }
                }

                @Override
                public void onFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    receiveMessageHolder.imgLoading.setVisibility(View.GONE);
                }
            };
            PolyvMyProgressManager.addListener(chatImg, position, onProgressListener);
            putImgUrl(chatImg, position);
            loadChatImg(chatImg, onProgressListener, receiveMessageHolder, position);
        }
    }

    private void loadChatImg(final String chatImg, final PolyvOnProgressListener onProgressListener, final ReceiveMessageHolder receiveMessageHolder, final int position) {
        Glide.with(getContext())
                .load(chatImg)
                .apply(new RequestOptions().error(R.drawable.polyv_image_load_err))//dontAnimate，不显示gif
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        onProgressListener.onFailed(e, model, target, isFirstResource);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        PolyvMyProgressManager.removeListener(chatImg, position);
                        removeImgUrl(chatImg, position);
                        onProgressListener.onProgress(chatImg, true, 100, 0, 0);
                        return false;
                    }
                })
                .into(receiveMessageHolder.chatImg);
    }

    private void handleSendMessage(SendMessageHolder sendMessageHolder, Object object) {
        //本地发言信息
        if (object instanceof PolyvLocalMessage) {
            PolyvLocalMessage localMessage = (PolyvLocalMessage) object;
            sendMessageHolder.sendMessage.setText((CharSequence) localMessage.getObjects()[0]);
        } else if (object instanceof PolyvQuestionMessage) {//提问信息
            PolyvQuestionMessage questionMessage = (PolyvQuestionMessage) object;
            sendMessageHolder.sendMessage.setText((CharSequence) questionMessage.getObjects()[0]);
        } else if (object instanceof PolyvSpeakHistory) {//历史自己的发言信息
            PolyvSpeakHistory speakHistory = (PolyvSpeakHistory) object;
            sendMessageHolder.sendMessage.setText((CharSequence) speakHistory.getObjects()[0]);
        }
    }

    private void handleTipsMessage(TipsMessageHolder tipsMessageHolder, Object object) {
        //送花(实则点赞)
        if (object instanceof PolyvLikesEvent) {
            resetTipsTextView(tipsMessageHolder, false);
            PolyvLikesEvent likesEvent = (PolyvLikesEvent) object;
            tipsMessageHolder.tipsMessage.setText((CharSequence) likesEvent.getObjects()[0]);
        } else if (object instanceof PolyvCloseRoomEvent) {//开启/关闭房间
            resetTipsTextView(tipsMessageHolder, true);
            PolyvCloseRoomEvent closeRoomEvent = (PolyvCloseRoomEvent) object;
            tipsMessageHolder.tipsMessage.setText(closeRoomEvent.getValue().isClosed() ? "房间已经关闭" : "房间已经开启");
        } else if (object instanceof PolyvLoginEvent) {//登陆
            resetTipsTextView(tipsMessageHolder, false);
            PolyvLoginEvent loginEvent = (PolyvLoginEvent) object;
            tipsMessageHolder.tipsMessage.setText("欢迎 " + loginEvent.getUser().getNick() + " 加入");
        } else if (object instanceof PolyvBanIpEvent) {//禁言
            resetTipsTextView(tipsMessageHolder, false);
            tipsMessageHolder.tipsMessage.setText("我已被管理员禁言！");
        } else if (object instanceof PolyvUnshieldEvent) {//取消禁言
            resetTipsTextView(tipsMessageHolder, false);
            tipsMessageHolder.tipsMessage.setText("我已被管理员取消禁言！");
        }
    }

    private void resetTipsTextView(TipsMessageHolder tipsMessageHolder, boolean hasBackground) {
        if (hasBackground) {
            tipsMessageHolder.tipsMessage.setBackgroundResource(R.drawable.polyv_tv_corner_gray);
            tipsMessageHolder.tipsMessage.setTextColor(Color.WHITE);
        } else {
            tipsMessageHolder.tipsMessage.setBackgroundDrawable(null);
            tipsMessageHolder.tipsMessage.setTextColor(Color.parseColor("#878787"));
        }
    }

    @Override
    public int getItemCount() {
        return chatTypeItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatTypeItems.get(position).type;
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }
}
