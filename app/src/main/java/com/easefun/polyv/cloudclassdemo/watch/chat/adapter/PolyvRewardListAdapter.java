package com.easefun.polyv.cloudclassdemo.watch.chat.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PolyvRewardListAdapter extends PolyvBaseRecyclerViewAdapter {
    public List<RewardBean> rewardLists;
    private boolean isInitSelected;
    private RewardBean selectedRewardBean;
    private OnItemClickListener itemOnClickListener;

    public PolyvRewardListAdapter(RecyclerView recyclerView) {
        super(recyclerView);
        rewardLists = new ArrayList<>();
    }

    public void setItemOnClickListener(OnItemClickListener clickListener) {
        this.itemOnClickListener = clickListener;
    }

    public class GiftItemViewHolder extends ClickableViewHolder<Object, PolyvRewardListAdapter> {
        private TextView money;

        public GiftItemViewHolder(View itemView, PolyvRewardListAdapter adapter) {
            super(itemView, adapter);
            money = $(R.id.tv_money);
        }

        @Override
        public void processNormalMessage(Object item, int position) {
            money.setText(rewardLists.get(position).getMoney() + "");
            money.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedRewardBean = rewardLists.get(position);
                    notifyDataSetChanged();
                    if (itemOnClickListener != null) {
                        itemOnClickListener.onItemClick(position, null);
                    }
                }
            });
            if (position == 0 && !isInitSelected) {
                money.setSelected(true);
                isInitSelected = true;
                selectedRewardBean = rewardLists.get(position);
            }
            if (rewardLists.get(position) == selectedRewardBean) {
                money.setSelected(true);
            } else {
                money.setSelected(false);
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

    public RewardBean getSelectedRewardBean() {
        return selectedRewardBean;
    }

    @NonNull
    @Override
    public ClickableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new GiftItemViewHolder
                (LayoutInflater.from(getContext()).inflate(R.layout.polyv_chat_reward_item, parent, false), this);
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
        return rewardLists.size();
    }

    public static class RewardBean {
        private float money;

        public RewardBean(float money) {
            this.money = money;
        }

        public float getMoney() {
            return money;
        }

        public void setMoney(float money) {
            this.money = money;
        }
    }
}
