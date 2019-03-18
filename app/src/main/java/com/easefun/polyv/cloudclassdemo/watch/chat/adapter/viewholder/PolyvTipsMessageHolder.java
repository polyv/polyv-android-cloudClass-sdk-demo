package com.easefun.polyv.cloudclassdemo.watch.chat.adapter.viewholder;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.PolyvBanIpEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvCloseRoomEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLikesEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvUnshieldEvent;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.itemview.PolyvItemViewFactoy;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;

/**
 * @author df
 * @create 2019/1/15
 * @Describe 提示消息样式
 */

public class PolyvTipsMessageHolder extends ClickableViewHolder<Object, PolyvChatListAdapter> {
    public TextView tipsMessage;

    public PolyvTipsMessageHolder(View itemView, PolyvChatListAdapter adapter) {
        super(itemView, adapter);

    }

    @Override
    public <T> IPolyvCustomMessageBaseItemView createItemView(PolyvCustomEvent<T> baseCustomEvent) {
        return PolyvItemViewFactoy.createTipItemView(baseCustomEvent.getEVENT(), context);
    }

    @Override
    public void processNormalMessage(Object item, int position) {
        handleTipsMessage(this, item);
    }

    public void processCustomMessage(PolyvCustomEvent item, int position) {
    }


    private void handleTipsMessage(PolyvTipsMessageHolder tipsMessageHolder, Object object) {
        int childIndex = findReuseChildIndex(PolyvChatManager.SE_MESSAGE);
        if(childIndex < 0){
            View child = View.inflate(context,
                    R.layout.polyv_chat_tip_normal_message_item, null);
            child.setTag(PolyvChatManager.SE_MESSAGE);
            contentContainer.addView(child);
            initView();
        }
        //送花(实则点赞)
        if (object instanceof PolyvLikesEvent) {
            resetTipsTextView(tipsMessageHolder, false);
            PolyvLikesEvent likesEvent = (PolyvLikesEvent) object;
            tipsMessageHolder.tipsMessage.setText((CharSequence) likesEvent.getObjects()[0]);
        } else if (object instanceof PolyvCloseRoomEvent) {//开启/关闭房间
            resetTipsTextView(tipsMessageHolder, true);
            PolyvCloseRoomEvent closeRoomEvent = (PolyvCloseRoomEvent) object;
            tipsMessageHolder.tipsMessage.setText(closeRoomEvent.getValue().isClosed() ? "房间已经关闭" : "房间已经开启");
        } else if (object instanceof PolyvBanIpEvent) {//禁言
            resetTipsTextView(tipsMessageHolder, false);
            tipsMessageHolder.tipsMessage.setText("我已被管理员禁言！");
        } else if (object instanceof PolyvUnshieldEvent) {//取消禁言
            resetTipsTextView(tipsMessageHolder, false);
            tipsMessageHolder.tipsMessage.setText("我已被管理员取消禁言！");
        } else {
            resetTipsTextView(tipsMessageHolder, false);
            tipsMessageHolder.tipsMessage.setText("暂不支持的消息类型");
        }
    }

    private void initView() {
        tipsMessage = $(com.easefun.polyv.commonui.R.id.tv_tips_message);
    }

    private void resetTipsTextView(PolyvTipsMessageHolder tipsMessageHolder, boolean hasBackground) {
        if (hasBackground) {
            tipsMessageHolder.tipsMessage.setBackgroundResource(com.easefun.polyv.commonui.R.drawable.polyv_tv_corner_gray);
            tipsMessageHolder.tipsMessage.setTextColor(Color.WHITE);
        } else {
            tipsMessageHolder.tipsMessage.setBackgroundDrawable(null);
            tipsMessageHolder.tipsMessage.setTextColor(Color.parseColor("#878787"));
        }
    }
}
