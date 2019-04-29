package com.easefun.polyv.cloudclassdemo.watch.chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class PolyvChatFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    private PolyvChatFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public PolyvChatFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        this(fm);
        this.fragments = fragments;
    }

    public void add(Fragment fragment) {
        if (fragments != null) {
            fragments.add(fragment);
        }
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
