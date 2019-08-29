package com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_management;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019/8/14 0014
 *
 * @author hwj
 * description 回放缓存管理Activity
 */
public class PolyvPlaybackCacheManageActivity extends PolyvBaseActivity {
    private static final String EXTRA_CHANNEL_ID = "channel_id";

    //View
    private ImageView iv_finish;
    private TextView tv_downloaded;
    private TextView tv_downloading;
    private View v_tabline;
    private RelativeLayout rlTop;
    private View v_line;
    private ViewPager vp_download;

    //Fragment
    PolyvPlaybackCacheFragment finishedFragment;
    PolyvPlaybackCacheFragment unfinishedFragment;
    private List<Fragment> fragmentList = new ArrayList<>(2);

    private String channelId;

    public static void launch(Activity activity, String channelId) {
        Intent intent = new Intent(activity, PolyvPlaybackCacheManageActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_playback_cache_manage);

        getIntentExtra();
        findView();
        initView();
    }

    private void getIntentExtra() {
        channelId = getIntent().getStringExtra(EXTRA_CHANNEL_ID);
    }

    private void findView() {
        iv_finish = findView(R.id.iv_finish);
        tv_downloaded = findView(R.id.tv_downloaded);
        tv_downloading = findView(R.id.tv_downloading);
        v_tabline = findView(R.id.v_tabline);
        rlTop = findView(R.id.rl_top);
        v_line = findView(R.id.v_line);
        vp_download = findView(R.id.vp_download);
    }

    private void initView() {
        finishedFragment = PolyvPlaybackCacheFragment.create(true);
        unfinishedFragment = PolyvPlaybackCacheFragment.create(false);

        fragmentList.add(finishedFragment);
        fragmentList.add(unfinishedFragment);

        PolyvPlayerFragmentAdapter vpAdapter = new PolyvPlayerFragmentAdapter(getSupportFragmentManager(), fragmentList);

        vp_download.setAdapter(vpAdapter);
        vp_download.setOffscreenPageLimit(1);
        vp_download.setPageMargin(30);
        vp_download.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tv_downloading.setSelected(false);
                tv_downloaded.setSelected(false);
                if (position == 0) {
                    tv_downloaded.setSelected(true);
                } else if (position == 1) {
                    tv_downloading.setSelected(true);
                }
                setLineLocation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        iv_finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_downloaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp_download.setCurrentItem(0);
            }
        });
        tv_downloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vp_download.setCurrentItem(1);
            }
        });

        final boolean isStarting = getIntent().getBooleanExtra("isStarting", false);
        if (isStarting) {
            tv_downloading.setSelected(true);
        } else {
            tv_downloaded.setSelected(true);
        }
        vp_download.setCurrentItem(isStarting ? 1 : 0);
        v_tabline.post(new Runnable() {
            @Override
            public void run() {
                setLineLocation(isStarting ? 1 : 0);
            }
        });
    }

    private void setLineLocation(int position) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v_tabline.getLayoutParams();
        lp.width = tv_downloaded.getWidth();
        int[] wh = new int[2];
        if (position == 0) {
            tv_downloaded.getLocationInWindow(wh);
        } else if (position == 1) {
            tv_downloading.getLocationInWindow(wh);
        }
        lp.leftMargin = wh[0];
        v_tabline.setLayoutParams(lp);
    }

    public PolyvPlaybackCacheFragment getDownloadedFragment() {
        return finishedFragment;
    }

    public static class PolyvPlayerFragmentAdapter extends FragmentPagerAdapter {
        List<Fragment> fragmentList;

        PolyvPlayerFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

    }

}
