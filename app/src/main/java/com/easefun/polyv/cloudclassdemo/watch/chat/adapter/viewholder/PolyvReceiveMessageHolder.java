package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.sub.gif.GifSpanTextView;
import com.easefun.polyv.cloudclass.chat.PolyvChatAuthorization;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.PolyvChatUser;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvTAnswerEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackImg;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackSpeak;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatGroupFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatPrivateFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview.PolyvItemViewFactoy;
import com.easefun.polyv.cloudclassdemo.watch.chat.config.PolyvChatUIConfig;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.utils.PolyvWebUtils;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;
import com.easefun.polyv.commonui.widget.PolyvCircleProgressView;

import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.USERTYPE_ASSISTANT;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.USERTYPE_GUEST;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.USERTYPE_MANAGER;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.USERTYPE_STUDENT;
import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.USERTYPE_TEACHER;

/**
 * @author df
 * @create 2019/1/15
 * @Describe 接受消息item
 */
public class PolyvReceiveMessageHolder extends ClickableViewHolder<Object, PolyvChatListAdapter> {

    public ImageView avatar;
    public TextView typeTv/*头衔*/, nickTv;
    public GifSpanTextView receiveMessage;
    public ImageView chatImg;
    public PolyvCircleProgressView imgLoading;

    private String userType, actor, nick, pic;

    public PolyvReceiveMessageHolder(View itemView, PolyvChatListAdapter adapter) {
        super(itemView, adapter);
        initCommonView();

    }

    private void initView() {

        receiveMessage = $(com.easefun.polyv.commonui.R.id.gtv_receive_message);
        chatImg = $(com.easefun.polyv.commonui.R.id.iv_chat_img);
        imgLoading = $(com.easefun.polyv.commonui.R.id.cpv_img_loading);

        receiveMessage.setWebLinkClickListener(new GifSpanTextView.WebLinkClickListener() {
            @Override
            public void webLinkOnClick(String url) {
                // TODO: 2019/11/11 监听消息的链接点击事件
                PolyvWebUtils.openWebLink(url,context);
            }
        });

        receiveMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                processItemLongClick(receiveMessage,true,receiveMessage.getText().toString());
                return true;
            }
        });
    }

    private void initCommonView() {
        avatar = parentView.findViewById(com.easefun.polyv.commonui.R.id.iv_avatar);
        typeTv = $(com.easefun.polyv.commonui.R.id.tv_type);
        nickTv = $(com.easefun.polyv.commonui.R.id.tv_nick);
    }


    @Override
    public void processNormalMessage(Object object, int position) {
        processReceiveMessage(object, position);
    }

    @Override
    public void processCustomMessage(PolyvCustomEvent baseCustomEvent, int position) {
        displayCommonView(baseCustomEvent.getUser());
    }

    @Override
    public <T> IPolyvCustomMessageBaseItemView createItemView(PolyvCustomEvent<T> baseCustomEvent) {
        return PolyvItemViewFactoy.createItemView(baseCustomEvent.getEVENT(), context);
    }

    private void displayCommonView(PolyvCustomEvent.UserBean user) {
        if (user == null) {
            return;
        }

        nickTv.setText(user.getNick());
        PolyvCustomEvent.UserBean.AuthorizationBean authorizationBean = user.getAuthorization();
        if (authorizationBean != null) {
            fillActorView(authorizationBean.getActor(), authorizationBean.getBgColor(), authorizationBean.getFColor());
        } else {
            typeTv.setVisibility(View.GONE);
        }
    }

    private void processReceiveMessage(Object object, int position) {
        int childIndex = findReuseChildIndex(PolyvChatManager.SE_MESSAGE);
        if (childIndex < 0) {
            View child = View.inflate(context,
                    R.layout.polyv_chat_receive_normal_message_content_item, null);
            child.setTag(PolyvChatManager.SE_MESSAGE);
            contentContainer.addView(child);
            initView();
        }
        imgLoading.setTag(position);

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
        } else if (object instanceof PolyvChatPlaybackSpeak) {//历史回放信息
            PolyvChatPlaybackSpeak chatPlaybackSpeak = (PolyvChatPlaybackSpeak) object;
            if (chatPlaybackSpeak.getUser() != null) {
                userType = chatPlaybackSpeak.getUser().getUserType();
                actor = chatPlaybackSpeak.getUser().getActor();
                nick = chatPlaybackSpeak.getUser().getNick();
                pic = chatPlaybackSpeak.getUser().getPic();
                message = (CharSequence) chatPlaybackSpeak.getObjects()[0];
                PolyvChatUser.AuthorizationBean authorizationBean;
                if ((authorizationBean = chatPlaybackSpeak.getUser().getAuthorization()) != null) {
                    chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
                }
            }
        } else if (object instanceof PolyvChatPlaybackImg) {//历史回放图片信息
            PolyvChatPlaybackImg chatPlaybackImg = (PolyvChatPlaybackImg) object;
            if (chatPlaybackImg.getUser() != null) {
                userType = chatPlaybackImg.getUser().getUserType();
                actor = chatPlaybackImg.getUser().getActor();
                nick = chatPlaybackImg.getUser().getNick();
                pic = chatPlaybackImg.getUser().getPic();
                if (chatPlaybackImg.getContent() != null && chatPlaybackImg.getContent().getSize() != null) {
                    chatImg = chatPlaybackImg.getContent().getUploadImgUrl();
                    imgHeight = (int) chatPlaybackImg.getContent().getSize().getHeight();
                    imgWidth = (int) chatPlaybackImg.getContent().getSize().getWidth();
                    PolyvChatUser.AuthorizationBean authorizationBean;
                    if ((authorizationBean = chatPlaybackImg.getUser().getAuthorization()) != null) {
                        chatAuthorization = new PolyvChatAuthorization(authorizationBean.getActor(), authorizationBean.getFColor(), authorizationBean.getBgColor());
                    }
                }
            }
        } else if (object instanceof PolyvChatPrivateFragment.PolyvQuestionTipsEvent) {//自定义的私聊的问候语信息
            userType = USERTYPE_TEACHER;
            actor = "讲师";
            nick = "讲师";
            pic = "http://livestatic.videocc.net/uploaded/images/webapp/avatar/default-teacher.png";
            message = "同学，您好！请问有什么问题吗？";
        } else {
            return;
        }
        acceptReceiveMessage(this, userType, actor, nick, pic, message, chatImg, imgHeight, imgWidth, chatAuthorization, position);
    }


    private void acceptReceiveMessage(final PolyvReceiveMessageHolder receiveMessageHolder, String userType, String actor, String nick, final String pic, CharSequence message, final String chatImg, int height, int width, PolyvChatAuthorization chatAuthorization, final int position) {

        if (adapter != null) {
            if (PolyvChatGroupFragment.isTeacherType(userType)) {
                PolyvImageLoader.getInstance()
                        .loadImageNoDiskCache(
                                parentView.getContext(),
                                pic,
                                R.drawable.polyv_default_teacher,
                                R.drawable.polyv_default_teacher,
                                receiveMessageHolder.avatar
                        );
            } else {
                PolyvImageLoader.getInstance()
                        .loadImageNoDiskCache(
                                parentView.getContext(),
                                pic,
                                R.drawable.polyv_missing_face,
                                R.drawable.polyv_missing_face,
                                receiveMessageHolder.avatar
                        );
            }
        }
        //设置昵称
        receiveMessageHolder.nickTv.setText(nick);
        //设置头衔
        if (chatAuthorization != null) {
            fillActorView(chatAuthorization.getActor(), chatAuthorization.getBgColor(), chatAuthorization.getfColor());
        } else if (!TextUtils.isEmpty(actor)) {
            fillActorView(actor, PolyvChatAuthorization.BGCOLOR_DEFAULT, PolyvChatAuthorization.FCOLOR_DEFAULT);
        } else {
            receiveMessageHolder.typeTv.setVisibility(View.GONE);
        }

        //设置不同用户类型的字体颜色
        int fontColor;
        switch (userType) {
            case USERTYPE_TEACHER:
                fontColor = PolyvChatUIConfig.FontColor.color_teacher;
                break;
            case USERTYPE_ASSISTANT:
                fontColor = PolyvChatUIConfig.FontColor.color_assistant;
                break;
            case USERTYPE_STUDENT:
                fontColor = PolyvChatUIConfig.FontColor.color_student;
                break;
            case USERTYPE_MANAGER:
                fontColor = PolyvChatUIConfig.FontColor.color_manager;
                break;
            default:
                fontColor = PolyvChatUIConfig.FontColor.color_student;
        }
        receiveMessageHolder.receiveMessage.setTextColor(fontColor);

        //设置其发言信息
        if (message != null) {//设置文本类型的发言信息
            receiveMessageHolder.chatImg.setVisibility(View.GONE);
            receiveMessageHolder.imgLoading.setVisibility(View.GONE);
            receiveMessageHolder.receiveMessage.setVisibility(View.VISIBLE);
            if(USERTYPE_TEACHER.equals(userType)  || USERTYPE_ASSISTANT.equals(userType)
                    || USERTYPE_MANAGER.equals(userType) || USERTYPE_GUEST.equals(userType)){
                receiveMessageHolder.receiveMessage.setTextInner(message,true);
            }else {
                receiveMessageHolder.receiveMessage.setText(message);
            }

        } else if (chatImg != null) {//设置图片类型的发言信息
            receiveMessageHolder.receiveMessage.setVisibility(View.GONE);
            receiveMessageHolder.chatImg.setVisibility(View.VISIBLE);
            receiveMessageHolder.chatImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.getOnChatImgViewClickListener() != null) {
                        adapter.getOnChatImgViewClickListener().onClick(receiveMessageHolder.chatImg, position);
                    }
                }
            });

            receiveMessageHolder.imgLoading.setVisibility(View.GONE);
            receiveMessageHolder.imgLoading.setProgress(0);
            //适配图片视图的宽高
            fitChatImgWH(width, height, receiveMessageHolder.chatImg);
            //加载图片
            loadNetImg(chatImg, position, receiveMessageHolder.imgLoading, receiveMessageHolder.chatImg);
        }
    }

    private void fillActorView(String actor2, String bgColor, String s) {
        typeTv.setVisibility(View.VISIBLE);
        typeTv.setText(actor2);
        typeTv.getBackground().setColorFilter(Color.parseColor(bgColor), PorterDuff.Mode.SRC_OVER);
        typeTv.setTextColor(Color.parseColor(s));
    }

}
