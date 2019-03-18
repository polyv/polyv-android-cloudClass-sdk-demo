package com.easefun.polyv.cloudclassdemo.watch.chat.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvEmoListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvGiftListAdapter;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.model.PolyvCustomGiftBean;

import java.util.ArrayList;

public class PolyvGiftLayout extends FrameLayout {
    private PopupWindow popupWindow;
    private OnClickListener onClickSendGiftButtonListener;
    private int selectedGiftCount;
    private PolyvGiftListAdapter giftListAdapter;
    private PopupWindow.OnDismissListener onDismissListener;

    public PolyvGiftLayout(@NonNull Context context) {
        this(context, null);
    }

    public PolyvGiftLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvGiftLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.polyv_chat_gift_layout, this);
        init();
    }

    private void init() {
        View giftLayout = this;
        //礼物列表
        RecyclerView giftList = giftLayout.findViewById(R.id.gift_list);
        giftList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false);
        giftList.setLayoutManager(gridLayoutManager);
        giftList.addItemDecoration(new PolyvEmoListAdapter.GridSpacingItemDecoration(4, ConvertUtils.dp2px(6), true));
        giftListAdapter = new PolyvGiftListAdapter(giftList);
        giftListAdapter.giftLists = new ArrayList<>();
        giftListAdapter.giftLists.add(new PolyvGiftListAdapter.GiftBean(PolyvCustomGiftBean.GIFTTYPE_TEA, R.drawable.polyv_gift_tea));
        giftListAdapter.giftLists.add(new PolyvGiftListAdapter.GiftBean(PolyvCustomGiftBean.GIFTTYPE_CLAP, R.drawable.polyv_gift_clap));

        giftList.setAdapter(giftListAdapter);

        View closeGiftButton = giftLayout.findViewById(R.id.close_gift_button);
        closeGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        TextView sendGiftButton = giftLayout.findViewById(R.id.send_gift_button);
        sendGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickSendGiftButtonListener != null)
                    onClickSendGiftButtonListener.onClick(v);
            }
        });

        final TextView giftCount1 = giftLayout.findViewById(R.id.gift_count_1);
        final TextView giftCount5 = giftLayout.findViewById(R.id.gift_count_5);
        final TextView giftCount10 = giftLayout.findViewById(R.id.gift_count_10);
        giftCount1.setSelected(true);
        selectedGiftCount = 1;
        giftCount1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewStatus(giftCount1, giftCount5, giftCount10);
                selectedGiftCount = 1;
            }
        });
        giftCount5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewStatus(giftCount5, giftCount1, giftCount10);
                selectedGiftCount = 5;
            }
        });
        giftCount10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewStatus(giftCount10, giftCount1, giftCount5);
                selectedGiftCount = 10;
            }
        });
    }

    private void setViewStatus(View selectedView, View... unSelectedViews) {
        if (unSelectedViews != null) {
            for (View view : unSelectedViews) {
                view.setSelected(false);
            }
        }
        if (selectedView != null) {
            selectedView.setSelected(true);
        }
    }

    public PolyvCustomGiftBean getSendCustomGiftBean() {
        String giftType = giftListAdapter.getSelectedGiftBean().getGiftType();
        PolyvCustomGiftBean customGiftBean = new PolyvCustomGiftBean(giftType, PolyvCustomGiftBean.getGiftName(giftType), "", selectedGiftCount);
        return customGiftBean;
    }

    public void setOnClickSendGiftButtonListener(OnClickListener onClickListener) {
        this.onClickSendGiftButtonListener = onClickListener;
    }

    public void show(View parentView) {
        popupWindow = new PopupWindow(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setAnimationStyle(R.style.PopupWindowAnim);
        popupWindow.setOnDismissListener(onDismissListener);
        popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
    }
    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener){
        this.onDismissListener=onDismissListener;
    }

    public void hide() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hide();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hide();
    }
}
