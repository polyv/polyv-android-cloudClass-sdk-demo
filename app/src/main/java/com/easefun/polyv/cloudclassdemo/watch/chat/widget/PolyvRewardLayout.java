package com.easefun.polyv.cloudclassdemo.watch.chat.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvEmoListAdapter;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvRewardListAdapter;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.model.PolyvCustomRewardBean;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PolyvRewardLayout extends FrameLayout {
    private PopupWindow popupWindow;
    private EditText inputMoney;
    private TextView showMoney;
    private OnClickListener onClickSendRewardButtonListener;
    private PopupWindow.OnDismissListener onDismissListener;
    private PolyvRewardListAdapter rewardAdapter;

    public PolyvRewardLayout(@NonNull Context context) {
        this(context, null);
    }

    public PolyvRewardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvRewardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.polyv_chat_reward_layout, this);
        init();
    }

    private void init() {
        View rewardLayout = this;
        //金额列表
        RecyclerView rewardList = rewardLayout.findViewById(R.id.reward_money);
        rewardList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        rewardList.setLayoutManager(gridLayoutManager);
        rewardList.addItemDecoration(new PolyvEmoListAdapter.GridSpacingItemDecoration(3, ConvertUtils.dp2px(6), false));
        rewardAdapter = new PolyvRewardListAdapter(rewardList);
        rewardAdapter.setItemOnClickListener(new PolyvBaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ClickableViewHolder holder) {
                if (!TextUtils.isEmpty(inputMoney.getText())) {
                    inputMoney.setText("");
                }
                showMoney.setText(rewardAdapter.rewardLists.get(position).getMoney() + "");
            }
        });
        rewardAdapter.rewardLists = new ArrayList<>();
        rewardAdapter.rewardLists.add(new PolyvRewardListAdapter.RewardBean(0.88f));
        rewardAdapter.rewardLists.add(new PolyvRewardListAdapter.RewardBean(1.68f));
        rewardAdapter.rewardLists.add(new PolyvRewardListAdapter.RewardBean(3.88f));
        rewardAdapter.rewardLists.add(new PolyvRewardListAdapter.RewardBean(8.88f));
        rewardAdapter.rewardLists.add(new PolyvRewardListAdapter.RewardBean(16.8f));
        rewardAdapter.rewardLists.add(new PolyvRewardListAdapter.RewardBean(66.8f));

        rewardList.setAdapter(rewardAdapter);

        View closeRewardButton = rewardLayout.findViewById(R.id.close_reward_button);
        closeRewardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        TextView sendRewardButton = rewardLayout.findViewById(R.id.send_reward_button);
        sendRewardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickSendRewardButtonListener != null)
                    onClickSendRewardButtonListener.onClick(v);
            }
        });

        inputMoney = rewardLayout.findViewById(R.id.input_money);
        inputMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().matches("\\..*")) {
                    inputMoney.setText("0" + s);
                    inputMoney.setSelection(before == 0 ? inputMoney.getText().length() : 1);
                } else if (s.toString().matches(".+\\.\\d{3,}")) {
                    inputMoney.setText(s.toString().substring(0, s.length() - 1));
                    inputMoney.setSelection(inputMoney.getText().length());
                }

                if (s.toString().contains(".")) {
                    inputMoney.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
                } else {
                    inputMoney.setKeyListener(DigitsKeyListener.getInstance("1234567890."));
                }

                if (!TextUtils.isEmpty(inputMoney.getText())) {
                    showMoney.setText(new DecimalFormat("0.00").format(Double.parseDouble(inputMoney.getText().toString())));
                } else {
                    if (rewardAdapter.getSelectedRewardBean() != null) {
                        showMoney.setText(rewardAdapter.getSelectedRewardBean().getMoney() + "");
                    } else {
                        showMoney.setText(rewardAdapter.rewardLists.get(0).getMoney() + "");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        showMoney = rewardLayout.findViewById(R.id.show_money);
        showMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().endsWith("0.00")) {
                    sendRewardButton.setEnabled(false);
                    sendRewardButton.setBackground(getResources().getDrawable(R.drawable.polyv_tv_corner_gray));
                } else {
                    sendRewardButton.setEnabled(true);
                    sendRewardButton.setBackground(getResources().getDrawable(R.drawable.polyv_tv_corner_orange));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        showMoney.setText(rewardAdapter.rewardLists.get(0).getMoney() + "");
    }

    public PolyvCustomRewardBean getSendCustomRewardBean() {
        PolyvCustomRewardBean customGiftBean = new PolyvCustomRewardBean();
        customGiftBean.setPrice(showMoney.getText().toString());
        customGiftBean.setUnit("元");
        return customGiftBean;
    }

    public void setOnClickSendRewardButtonListener(OnClickListener onClickListener) {
        this.onClickSendRewardButtonListener = onClickListener;
    }

    public void setOnPopupWindowDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void show(View parentView) {
        popupWindow = new PopupWindow(this, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                inputMoney.setText("");
                KeyboardUtils.hideSoftInput(inputMoney);//homeActivity stateAlwaysHidden
                if (onDismissListener != null)
                    onDismissListener.onDismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setAnimationStyle(R.style.PopupWindowAnim);
        popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
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
