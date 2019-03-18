package com.easefun.polyv.cloudclassdemo.watch.chat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PolyvGiftListAdapter extends PolyvBaseRecyclerViewAdapter {
    public List<GiftBean> giftLists;
    private boolean isInitSelected;
    private GiftBean selectedGiftBean;

    public PolyvGiftListAdapter(RecyclerView recyclerView) {
        super(recyclerView);
        giftLists = new ArrayList<>();
    }

    public class GiftItemViewHolder extends ClickableViewHolder<Object, PolyvGiftListAdapter> {
        private ImageView gift;

        public GiftItemViewHolder(View itemView, PolyvGiftListAdapter adapter) {
            super(itemView, adapter);
            gift = $(R.id.iv_gift);
        }

        @Override
        public void processNormalMessage(Object item, int position) {
            gift.setImageDrawable(getContext().getResources().getDrawable(giftLists.get(position).getDrawableId()));
            gift.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedGiftBean = giftLists.get(position);
                    notifyDataSetChanged();
                }
            });
            if (position == 0 && !isInitSelected) {
                ((View)gift.getParent()).setSelected(true);
                isInitSelected = true;
                selectedGiftBean = giftLists.get(position);
            }
            if (giftLists.get(position) == selectedGiftBean) {
                ((View)gift.getParent()).setSelected(true);
            } else {
                ((View)gift.getParent()).setSelected(false);
            }
        }

        @Override
        public void processCustomMessage(PolyvCustomEvent item, int position) {

        }

        @Override
        public <T> IPolyvCustomMessageBaseItemView createItemView(PolyvCustomEvent<T> baseCustomEvent) {
            return null;
        }

    }

    public GiftBean getSelectedGiftBean() {
        return selectedGiftBean;
    }

    @NonNull
    @Override
    public ClickableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new GiftItemViewHolder
                (LayoutInflater.from(getContext()).inflate(R.layout.polyv_chat_gift_item, parent, false), this);
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof GiftItemViewHolder) {
            holder.processNormalMessage(null, position);
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return giftLists.size();
    }

    public static class GiftBean {
        private int drawableId;
        private String giftType;

        public GiftBean(String giftType, int drawableId) {
            this.giftType = giftType;
            this.drawableId = drawableId;
        }

        public String getGiftType() {
            return giftType;
        }

        public void setGiftType(String giftType) {
            this.giftType = giftType;
        }

        public int getDrawableId() {
            return drawableId;
        }

        public void setDrawableId(int drawableId) {
            this.drawableId = drawableId;
        }
    }
}
