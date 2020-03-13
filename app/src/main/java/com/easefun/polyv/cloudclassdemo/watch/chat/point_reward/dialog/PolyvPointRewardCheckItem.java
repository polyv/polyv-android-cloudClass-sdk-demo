package com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.easefun.polyv.cloudclass.model.point_reward.PolyvPointRewardSettingVO;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019-12-04
 * author: hwj
 * description: 每个Fragment中保存3个（默认）道具，每个道具都有check的点击效果
 */
class PolyvPointRewardCheckItem extends android.support.v7.widget.AppCompatCheckBox {
    private List<OnCheckedChangeListener> onCheckedChangeListeners = new ArrayList<>();
    private PolyvPointRewardSettingVO.GoodsBean goodsBean;

    public PolyvPointRewardCheckItem(Context context) {
        this(context, null);
    }

    public PolyvPointRewardCheckItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPointRewardCheckItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setClickable(true);
        //先每一个Item都停用
        setEnabled(false);
        setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //如果已经被checked了，那么不再允许点击
                setClickable(!isChecked);

                for (OnCheckedChangeListener onCheckedChangeListener : onCheckedChangeListeners) {
                    onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
    }

    /**
     * 绑定道具实体对象
     *
     * @param goodBean 道具实体对象
     */
    public void setGoodBean(PolyvPointRewardSettingVO.GoodsBean goodBean) {
        this.goodsBean = goodBean;
        //当绑定了道具对象后，才启用这个item。
        setEnabled(true);
    }

    /**
     * 获取道具实体对象
     *
     * @return 道具实体对象
     */
    public PolyvPointRewardSettingVO.GoodsBean getGoodBean() {
        return goodsBean;
    }

    /**
     * 添加check 监听器
     *
     * @param li
     */
    public void addOnCheckedChangeListener(OnCheckedChangeListener li) {
        onCheckedChangeListeners.add(li);
    }
}
