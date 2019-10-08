package com.easefun.polyv.cloudclassdemo.watch.chat.adapter;

import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.easefun.polyv.cloudclass.chat.PolyvChatManager.SE_CUSTOMMESSAGE;

public class PolyvChatListAdapter extends PolyvBaseRecyclerViewAdapter {
    private List<ChatTypeItem> chatTypeItems;
    private OnChatImgViewClickListener onChatImgViewClickListener;
    private OnResendMessageViewClickListener onResendMessageViewClickListener;


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

    public void setOnChatImgViewClickListener(OnChatImgViewClickListener l) {
        this.onChatImgViewClickListener = l;
    }

    public void setOnResendMessageViewClickListener(OnResendMessageViewClickListener l) {
        this.onResendMessageViewClickListener = l;
    }


    public OnChatImgViewClickListener getOnChatImgViewClickListener() {
        return onChatImgViewClickListener;
    }

    public OnResendMessageViewClickListener getOnResendMessageViewClickListener() {
        return onResendMessageViewClickListener;
    }

    public interface OnChatImgViewClickListener {
        void onClick(ImageView iv, int position);
    }

    public interface OnResendMessageViewClickListener {
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
        public String socketListen;

        public ChatTypeItem(Object object, @Type int type, String socketListen) {
            this.object = object;
            this.type = type;
            this.socketListen = socketListen;
        }

        @Override
        public String toString() {
            return "ChatTypeItem{" +
                    "object=" + object +
                    ", type=" + type +
                    ", socketListen='" + socketListen + '\'' +
                    '}';
        }
    }


    @NonNull
    @Override
    public ClickableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
//        ClickableViewHolder clickableViewHolder = null;
//        switch (viewType) {
//            case ChatTypeItem.TYPE_TIPS:
//                clickableViewHolder = new PolyvTipsMessageHolder(LayoutInflater.from(getContext()).
//                        inflate(R.layout.polyv_chat_tips_message_item, parent, false),this);
//                break;
//            case ChatTypeItem.TYPE_SEND:
//                clickableViewHolder = new PolyvSendMessageHolder(LayoutInflater.from(getContext()).
//                        inflate(R.layout.polyv_chat_send_message_item, parent, false),this);
//                break;
//            case ChatTypeItem.TYPE_RECEIVE:
//                clickableViewHolder = new PolyvReceiveMessageHolder(LayoutInflater.from(getContext()).
//                        inflate(R.layout.polyv_chat_receive_message_item, parent, false),this);
//                break;
//        }
        return PolyvViewHolderCreateFactory.createViewHolder(viewType, getContext(), parent, this);
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        ChatTypeItem chatTypeItem = chatTypeItems.get(position);

        if (chatTypeItem != null) {
            if (SE_CUSTOMMESSAGE.equals(chatTypeItem.socketListen)) {//处理自定义消息
                bindCustomView(chatTypeItem.object, holder, position);

            } else {//此处处理以前的固定消息
                holder.processNormalMessage(chatTypeItem.object, position);
            }
        }

        super.onBindViewHolder(holder, position);
    }

    private <T> void bindCustomView(Object item, ClickableViewHolder holder, int position) {
        PolyvCustomEvent<T> baseCustomEvent = (PolyvCustomEvent) item;
        IPolyvCustomMessageBaseItemView<PolyvCustomEvent<T>> sendMessageItemView;
        int childIndex = holder.findReuseChildIndex(baseCustomEvent.getEVENT());
        if (childIndex < 0) {
            sendMessageItemView = holder.createItemView(baseCustomEvent);
            holder.contentContainer.addView(sendMessageItemView);
        } else {
            sendMessageItemView = (IPolyvCustomMessageBaseItemView<PolyvCustomEvent<T>>) holder.contentContainer.getChildAt(childIndex);
        }
        sendMessageItemView.setTag(baseCustomEvent.getEVENT());
        sendMessageItemView.processMessage(baseCustomEvent, position);

        holder.processCustomMessage(baseCustomEvent, position);
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
