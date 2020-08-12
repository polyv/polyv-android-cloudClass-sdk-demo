package com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.dialog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.CompoundButton;

import com.easefun.polyv.cloudclass.model.point_reward.PolyvPointRewardSettingVO;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019-12-03
 * author: hwj
 * description: 积分打赏弹窗中的fragment 的 Adapter
 */
class PolyvPointRewardVPFragmentAdapter extends FragmentPagerAdapter {

    private List<PolyvPointRewardSettingVO.GoodsBean> goodsBeanList;
    private int fragmentCount = 0;

    //维护所有的打赏道具CheckItem。
    private List<PolyvPointRewardCheckItem> checkItemList = new ArrayList<>();
    private PolyvPointRewardCheckItem curCheckedItem;


    PolyvPointRewardVPFragmentAdapter(FragmentManager fm, List<PolyvPointRewardSettingVO.GoodsBean> goodsBeans) {
        super(fm);
        goodsBeanList = goodsBeans;

        //计算要创建的fragment的数量
        int goodsSize = goodsBeanList.size();
        if (goodsSize % 3 == 0) {
            fragmentCount = goodsSize / 3;
        } else {
            fragmentCount = goodsSize / 3 + 1;
        }
    }

    @Override
    public Fragment getItem(int position) {
        PolyvPointRewardFragment fragment = new PolyvPointRewardFragment();
        int[] indexArray = getGoodsBeanIndexFromFragmentItemIndex(position);
        int start = indexArray[0];
        int end = indexArray[1];
        if (end > goodsBeanList.size() - 1) {
            end = goodsBeanList.size() - 1;
        }
        List<PolyvPointRewardSettingVO.GoodsBean> subList = goodsBeanList.subList(start, end + 1);
        fragment.init(subList, new PolyvPointRewardFragment.OnAddItemView() {
            @Override
            public void onAddItem(PolyvPointRewardCheckItem checkItem) {
                checkItemList.add(checkItem);

                checkItem.addOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //标记当前选中的道具
                            if (buttonView instanceof PolyvPointRewardCheckItem) {
                                curCheckedItem = (PolyvPointRewardCheckItem) buttonView;
                            }

                            //uncheck 其他道具
                            for (PolyvPointRewardCheckItem item : checkItemList) {
                                if (buttonView != item) {
                                    item.setChecked(false);
                                }
                            }
                        }
                    }
                });
            }
        });
        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentCount;
    }

    /**
     * 获取当前check到的item对应的道具的实体对象
     *
     * @return 可能会为null，表示还没选择，要给出提示：请选择打赏道具
     */
    PolyvPointRewardSettingVO.GoodsBean getCurrentCheckedItemGood() {
        if (curCheckedItem != null) {
            return curCheckedItem.getGoodBean();
        } else return null;
    }

    /**
     * 根据fragment下标取出对应的数据的下标，例如fragment index =0 ,那么道具就是0,1,2。即每个Fragment展示3个道具
     * [3n, 3n+2]
     *
     * @return 数组，index 0 是开头。index 1是结尾。
     */
    private int[] getGoodsBeanIndexFromFragmentItemIndex(int fragmentItemIndex) {
        int[] result = new int[2];
        result[0] = fragmentItemIndex * 3;
        result[1] = fragmentItemIndex * 3 + 2;
        return result;
    }
}
