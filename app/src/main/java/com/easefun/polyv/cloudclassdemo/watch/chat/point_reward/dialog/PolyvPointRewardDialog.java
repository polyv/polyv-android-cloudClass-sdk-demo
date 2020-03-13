package com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.dialog;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.point_reward.PolyvPointRewardSettingVO;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.widget.PolyvBeadWidget;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ToastUtils;

import java.util.List;

/**
 * date: 2019-12-02
 * author: hwj
 * description: 积分打赏弹窗
 */
public class PolyvPointRewardDialog {
    //static
    //积分打赏单位
    public static String sPointUnit = "点";

    private Activity activity;

    //View
    private View rootView;
    private ImageView plvIvPointRewardClose;
    private ViewPager plvVpPointReward;
    private PolyvBeadWidget plvBeadPointReward;
    private TextView plvTvPointRewardRemainingPoint;
    private RelativeLayout plvRlPointRewardBottom;

    //显示弹窗钱的焦点View，隐藏弹窗后，要归还焦点
    private View lastFocusedView;

    //后台设置的积分打赏数据
    private List<PolyvPointRewardSettingVO.GoodsBean> goodsBeanList;

    //adapter
    private PolyvPointRewardVPFragmentAdapter vpFragmentAdapter;

    //FragmentManager of Activity
    private FragmentManager fragmentManager;

    //listener
    private OnMakePointRewardListener onMakePointRewardListener;
    private OnShowListener onShowListener;
    private Button plvBtnPointRewardMakeReward;
    private RadioGroup plvRgPointRewardSendCount;

    //状态变量
    private int goodNumToSend = -1;

    // <editor-fold defaultstate="collapsed" desc="初始化">
    public PolyvPointRewardDialog(AppCompatActivity activity, OnMakePointRewardListener listener, OnShowListener onShowListener) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();

        this.onShowListener = onShowListener;
        this.onMakePointRewardListener = listener;

        ViewGroup activityParentView = activity.findViewById(android.R.id.content);

        rootView = LayoutInflater.from(activity).inflate(R.layout.plv_window_point_reward, activityParentView, false);
        rootView.setVisibility(View.GONE);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);

        activityParentView.addView(rootView);

        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        hide();
                        return true;
                    }
                }
                return false;
            }
        });
        initView(rootView);
    }

    private void initView(View rootView) {
        View plvVTopTransparent = rootView.findViewById(R.id.plv_v_top_transparent);
        plvVpPointReward = rootView.findViewById(R.id.plv_vp_point_reward);
        plvBeadPointReward = rootView.findViewById(R.id.plv_bead_point_reward);
        plvTvPointRewardRemainingPoint = rootView.findViewById(R.id.plv_tv_point_reward_remaining_point);
        plvBtnPointRewardMakeReward = rootView.findViewById(R.id.plv_btn_point_reward_make_reward);
        plvRgPointRewardSendCount = rootView.findViewById(R.id.plv_rg_point_reward_send_count);
        plvIvPointRewardClose = rootView.findViewById(R.id.plv_iv_point_reward_close);
        plvRlPointRewardBottom = rootView.findViewById(R.id.plv_rl_point_reward_bottom);

        plvVTopTransparent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        plvIvPointRewardClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        plvRgPointRewardSendCount.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.plv_rb_point_reward_reward_1) {
                    goodNumToSend = 1;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_5) {
                    goodNumToSend = 5;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_10) {
                    goodNumToSend = 10;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_66) {
                    goodNumToSend = 66;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_88) {
                    goodNumToSend = 88;
                } else if (checkedId == R.id.plv_rb_point_reward_reward_666) {
                    goodNumToSend = 666;
                }
            }
        });
        //默认check 数量1
        plvRgPointRewardSendCount.check(R.id.plv_rb_point_reward_reward_1);

        plvBtnPointRewardMakeReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (goodNumToSend < 0) {
                    ToastUtils.showShort("请选择打赏数量");
                    return;
                }
                PolyvPointRewardSettingVO.GoodsBean goodsBean = vpFragmentAdapter.getCurrentCheckedItemGood();
                if (goodsBean == null) {
                    ToastUtils.showShort("请选择打赏道具");
                    return;
                }
//                if (goodNumToSend * goodsBean.getGoodPrice() > remainingPoint) {
//                    ToastUtils.showShort("积分不足");
//                    return;
//                }
                if (onMakePointRewardListener != null) {
                    onMakePointRewardListener.onMakeReward(goodNumToSend, goodsBean.getGoodId());
                }
                hide();
            }
        });

        plvVpPointReward.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {/**/}

            @Override
            public void onPageSelected(int position) {
                plvBeadPointReward.setCurrentSelectedIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {/**/}
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="外部更新积分打赏弹窗数据">

    public void updateRemainingPoint(int remainingPoint) {
        String remainingPointToShow = "我的积分：" + remainingPoint +" "+ sPointUnit;
        plvTvPointRewardRemainingPoint.setText(remainingPointToShow);
    }

    public void setPointRewardSettingVO(PolyvPointRewardSettingVO polyvPointRewardSettingVO) {
        goodsBeanList = polyvPointRewardSettingVO.getGoods();
        sPointUnit = polyvPointRewardSettingVO.getPointUnit();

        //根据数据，创建ViewPagerAdapter
        vpFragmentAdapter = new PolyvPointRewardVPFragmentAdapter(fragmentManager, goodsBeanList);
        plvVpPointReward.setAdapter(vpFragmentAdapter);
        plvVpPointReward.setOffscreenPageLimit(goodsBeanList.size());

        //设置底部珠子数量
        plvBeadPointReward.setBeadCount(vpFragmentAdapter.getCount());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="控制显示、隐藏">
    public void show() {
        PolyvScreenUtils.setPortrait(activity);
        PolyvScreenUtils.lockOrientation();
        rootView.setVisibility(View.VISIBLE);
        lastFocusedView = activity.getCurrentFocus();
        rootView.requestFocus();

        Animation enterAnim = AnimationUtils.loadAnimation(activity, R.anim.plv_point_reward_enter);
        plvRlPointRewardBottom.startAnimation(enterAnim);

        onShowListener.onShow();
    }

    public void hide() {
        Animation exitAnim = AnimationUtils.loadAnimation(activity, R.anim.plv_point_reward_exit);
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rootView.setVisibility(View.GONE);
                if (lastFocusedView != null) {
                    lastFocusedView.requestFocus();
                }
                PolyvScreenUtils.unlockOrientation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        plvRlPointRewardBottom.startAnimation(exitAnim);
    }
    // </editor-fold>


    /**
     * 观众点击积分打赏回调。
     * 客户端应使用该回调发送积分打赏请求。
     */
    public interface OnMakePointRewardListener {
        void onMakeReward(int goodNum, int goodId);
    }

    /**
     * 第一次显示积分打赏弹窗回调
     */
    public interface OnShowListener {
        void onShow();
    }
}
