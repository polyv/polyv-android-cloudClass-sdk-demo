package com.easefun.polyv.commonui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class PolyvFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    private PolyvFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public PolyvFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        this(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
