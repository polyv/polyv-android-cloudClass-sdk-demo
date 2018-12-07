package com.easefun.polyv.cloudclassdemo.watch.chat.imageScan;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;

import java.util.List;

public class PolyvChatImgFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems;
    private View.OnClickListener onClickListener;

    private PolyvChatImgFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public PolyvChatImgFragmentStatePagerAdapter(FragmentManager fm, List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems) {
        this(fm);
        this.chatTypeItems = chatTypeItems;
    }

    public void setOnClickImgListener(View.OnClickListener l) {
        this.onClickListener = l;
    }

    @Override
    public Fragment getItem(int position) {
        return PolyvChatImageFragment.newInstance(chatTypeItems.get(position), position).setOnImgClickListener(onClickListener);
    }

    @Override
    public int getCount() {
        return chatTypeItems.size();
    }
}
