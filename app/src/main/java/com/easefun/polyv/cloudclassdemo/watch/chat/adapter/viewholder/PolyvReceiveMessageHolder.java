package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easefun.polyv.businesssdk.sub.gif.GifSpanTextView;
import com.easefun.polyv.cloudclass.chat.PolyvChatAuthorization;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvSpeakEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvTAnswerEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.history.PolyvSpeakHistory;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatGroupFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.PolyvChatPrivateFragment;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview.PolyvItemViewFactoy;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvCircleProgressView;

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

        nickTv.setVisibility(View.VISIBLE);
        nickTv.setText(user.getNick());
        PolyvCustomEvent.UserBean.AuthorizationBean authorizationBean = user.getAuthorization();
        String actor = authorizationBean != null ? authorizationBean.getActor() : null;
        if (TextUtils.isEmpty(actor)) {
            actor = user.getActor();
        }
        if (!TextUtils.isEmpty(actor)) {
            fillActorView(actor, authorizationBean != null ?
                            authorizationBean.getBgColor() : PolyvChatAuthorization.BGCOLOR_DEFAULT,
                    authorizationBean != null ? authorizationBean.getFColor() : PolyvChatAuthorization.FCOLOR_DEFAULT);
        } else {
            typeTv.setVisibility(View.GONE);
        }


        if (adapter != null) {
            pic = user.getPic();
            Glide.with(parentView.getContext())
                    .load(pic)
                    .apply(PolyvChatGroupFragment.isTeacherType(userType) ? adapter.getRequestOptions_t() : adapter.getRequestOptions_s())
                    .into(avatar);
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
        } else if (object instanceof PolyvChatPrivateFragment.PolyvQuestionTipsEvent) {//自定义的私聊的问候语信息
            userType = PolyvChatManager.USERTYPE_TEACHER;
            actor = "讲师";
            nick = "讲师";
            pic = "http://livestatic.videocc.net/uploaded/images/webapp/avatar/default-teacher.png";
            message = "同学，您好！请问有什么问题吗？";
        } else {
            return;
        }
        acceptReceiveMessage(this, userType, actor, nick, pic, message, chatImg, imgHeight, imgWidth, chatAuthorization, position);
    }


    private void acceptReceiveMessage(final PolyvReceiveMessageHolder receiveMessageHolder, String userType,
                                      String actor, String nick, final String pic, CharSequence message,
                                      final String chatImg, int height, int width,
                                      PolyvChatAuthorization chatAuthorization, final int position) {

        if (adapter != null) {

            Glide.with(parentView.getContext())
                    .load(pic)
                    .apply(PolyvChatGroupFragment.isTeacherType(userType) ? adapter.getRequestOptions_t() : adapter.getRequestOptions_s())
                    .into(receiveMessageHolder.avatar);
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
        //设置其发言信息
        if (message != null) {//设置文本类型的发言信息
            receiveMessageHolder.chatImg.setVisibility(View.GONE);
            receiveMessageHolder.imgLoading.setVisibility(View.GONE);
            receiveMessageHolder.receiveMessage.setVisibility(View.VISIBLE);

            int length = Math.min(nick.length(), 10);
            //昵称内容
            String nickContent = nick.substring(0, length) + "  ";
            //昵称和消息的总长度
            float tvWidth = receiveMessageHolder.receiveMessage.getPaint().measureText(nickContent + message);
            SpannableStringBuilder spannableString = new SpannableStringBuilder(nickContent);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#878787")), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            contentContainer.post(() -> {
                //需要换行
                if (tvWidth > contentContainer.getMeasuredWidth()) {
                    spannableString.append("\n");
                }
                spannableString.append(message);
                receiveMessageHolder.receiveMessage.setText(spannableString);
            });
            if (receiveMessageHolder.nickTv.getVisibility() != View.GONE) {
                receiveMessageHolder.nickTv.setVisibility(View.GONE);
            }
        } else if (chatImg != null) {//设置图片类型的发言信息
            receiveMessageHolder.nickTv.setVisibility(View.VISIBLE);

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
//        typeTv.getBackground().setColorFilter(Color.parseColor(bgColor), PorterDuff.Mode.SRC_OVER);
        typeTv.setTextColor(Color.parseColor(s));
    }
}
