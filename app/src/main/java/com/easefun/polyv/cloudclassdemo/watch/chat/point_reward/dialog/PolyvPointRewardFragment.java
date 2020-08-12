package com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.point_reward.PolyvPointRewardSettingVO;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;

import java.util.List;

/**
 * date: 2019-12-03
 * author: hwj
 * description: 积分打赏的ViewPager中对应的Fragment。
 */
public class PolyvPointRewardFragment extends Fragment {

    //  0 < size <= 3
    private List<PolyvPointRewardSettingVO.GoodsBean> goodsBeanList;

    //View
    private ImageView ivPointRewardFragment1;
    private TextView tvPointRewardFragmentGoodName1;
    private TextView tvPointRewardFragmentGoodPrice1;
    private ImageView ivPointRewardFragment2;
    private TextView tvPointRewardFragmentGoodName2;
    private TextView tvPointRewardFragmentGoodPrice2;
    private ImageView ivPointRewardFragment3;
    private TextView tvPointRewardFragmentGoodName3;
    private TextView tvPointRewardFragmentGoodPrice3;
    private FrameLayout flPointRewardItem1;
    private FrameLayout flPointRewardItem2;
    private FrameLayout flPointRewardItem3;
    private PolyvPointRewardCheckItem checkboxPointReward1;
    private PolyvPointRewardCheckItem checkboxPointReward2;
    private PolyvPointRewardCheckItem checkboxPointReward3;

    //Listener
    private OnAddItemView onAddItemViewListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.plv_fragment_point_reward, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        flPointRewardItem1 = view.findViewById(R.id.fl_point_reward_item_1);
        flPointRewardItem2 = view.findViewById(R.id.fl_point_reward_item_2);
        flPointRewardItem3 = view.findViewById(R.id.fl_point_reward_item_3);
        checkboxPointReward1 = view.findViewById(R.id.checkbox_point_reward_1);
        checkboxPointReward2 = view.findViewById(R.id.checkbox_point_reward_2);
        checkboxPointReward3 = view.findViewById(R.id.checkbox_point_reward_3);
        ivPointRewardFragment1 = view.findViewById(R.id.iv_point_reward_fragment_1);
        tvPointRewardFragmentGoodName1 = view.findViewById(R.id.tv_point_reward_fragment_good_name_1);
        tvPointRewardFragmentGoodPrice1 = view.findViewById(R.id.tv_point_reward_fragment_good_price_1);
        ivPointRewardFragment2 = view.findViewById(R.id.iv_point_reward_fragment_2);
        tvPointRewardFragmentGoodName2 = view.findViewById(R.id.tv_point_reward_fragment_good_name_2);
        tvPointRewardFragmentGoodPrice2 = view.findViewById(R.id.tv_point_reward_fragment_good_price_2);
        ivPointRewardFragment3 = view.findViewById(R.id.iv_point_reward_fragment_3);
        tvPointRewardFragmentGoodName3 = view.findViewById(R.id.tv_point_reward_fragment_good_name_3);
        tvPointRewardFragmentGoodPrice3 = view.findViewById(R.id.tv_point_reward_fragment_good_price_3);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setDataForView();
    }

    /**
     * 初始化。请在Fragment一创建，还未添加到FragmentManager就调用。
     */
    public void init(List<PolyvPointRewardSettingVO.GoodsBean> goodsBeanList, OnAddItemView onAddItemViewListener) {
        this.goodsBeanList = goodsBeanList;
        this.onAddItemViewListener = onAddItemViewListener;
    }

    // <editor-fold defaultstate="collapsed" desc="将数据设置到View">
    private void setDataForView() {
        for (int i = 0; i < goodsBeanList.size(); i++) {
            PolyvPointRewardSettingVO.GoodsBean goodsBean = goodsBeanList.get(i);
            if (i == 0) {
                setDataForEachItem(goodsBean, flPointRewardItem1, checkboxPointReward1,
                        ivPointRewardFragment1, tvPointRewardFragmentGoodName1, tvPointRewardFragmentGoodPrice1);
                //初始化默认选中第一个道具
                if (goodsBean.getGoodId()==1){
                    checkboxPointReward1.setChecked(true);
                }
            } else if (i == 1) {
                setDataForEachItem(goodsBean, flPointRewardItem2, checkboxPointReward2,
                        ivPointRewardFragment2, tvPointRewardFragmentGoodName2, tvPointRewardFragmentGoodPrice2);
            } else {
                setDataForEachItem(goodsBean, flPointRewardItem3, checkboxPointReward3,
                        ivPointRewardFragment3, tvPointRewardFragmentGoodName3, tvPointRewardFragmentGoodPrice3);
            }
        }
    }

    private void setDataForEachItem
            (PolyvPointRewardSettingVO.GoodsBean goodsBean, ViewGroup itemView,
             PolyvPointRewardCheckItem checkItem, ImageView iv, TextView goodNameTv, TextView goodPriceTv) {
        String goodImg = goodsBean.getGoodImg();
        String goodName = goodsBean.getGoodName();
        int goodPrice = goodsBean.getGoodPrice();

        itemView.setVisibility(View.VISIBLE);
        String pointUnit = PolyvPointRewardDialog.sPointUnit;
        String goodPriceText = goodPrice + pointUnit;

        loadImage(goodImg, iv);
        goodNameTv.setText(goodName);
        goodPriceTv.setText(goodPriceText);

        if (onAddItemViewListener != null) {
            onAddItemViewListener.onAddItem(checkItem);
        }
        checkItem.setGoodBean(goodsBean);
    }
    // </editor-fold>

    private void loadImage(String url, ImageView iv) {
        if (!url.startsWith("http")) {
            url = "https:/" + url;
        }
        PolyvImageLoader.getInstance().loadImage(getActivity(),url,iv);
    }

    /**
     * 当前Fragment添加了一个打赏道具ItemView的回调
     */
    interface OnAddItemView {
        void onAddItem(PolyvPointRewardCheckItem checkItem);
    }

}
