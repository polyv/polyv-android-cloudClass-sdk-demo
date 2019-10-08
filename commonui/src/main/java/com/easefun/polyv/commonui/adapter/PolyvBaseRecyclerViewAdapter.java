package com.easefun.polyv.commonui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.utils.imageloader.glide.progress.PolyvMyProgressManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PolyvBaseRecyclerViewAdapter extends
        RecyclerView.Adapter<ClickableViewHolder> {

    protected Context context;

    protected RecyclerView mRecyclerView;

    private List<RecyclerView.OnScrollListener> mListeners = new ArrayList<>();

    protected Map<String, List<Integer>> loadImgMap = new HashMap<>();

    public PolyvBaseRecyclerViewAdapter(RecyclerView recyclerView) {

        this.mRecyclerView = recyclerView;
        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView rv, int newState) {

                for (RecyclerView.OnScrollListener listener : mListeners) {
                    listener.onScrollStateChanged(rv, newState);
                }
            }


            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {

                for (RecyclerView.OnScrollListener listener : mListeners) {
                    listener.onScrolled(rv, dx, dy);
                }
            }
        });
    }


    public void addOnScrollListener(RecyclerView.OnScrollListener listener) {

        mListeners.add(listener);
    }


    public Map<String, List<Integer>> getLoadImgMap() {
        return loadImgMap;
    }

    public  void onDestory(){
        if (getLoadImgMap() != null) {
            for (String key : getLoadImgMap().keySet()) {
                for (int value : getLoadImgMap().get(key)) {
                    PolyvMyProgressManager.removeListener(key, value);
                }
            }
        }
    };


    public interface OnItemClickListener {

        void onItemClick(int position, ClickableViewHolder holder);
    }

    interface OnItemLongClickListener {

        boolean onItemLongClick(int position, ClickableViewHolder holder);
    }

    private OnItemClickListener itemClickListener;

    private OnItemLongClickListener itemLongClickListener;


    public void setOnItemClickListener(OnItemClickListener listener) {

        this.itemClickListener = listener;
    }


    public void setOnItemLongClickListener(OnItemLongClickListener listener) {

        this.itemLongClickListener = listener;
    }


    public void bindContext(Context context) {
        this.context = context;
    }


    public Context getContext() {

        return this.context;
    }


    @Override
    public void onBindViewHolder(final ClickableViewHolder holder, final int position) {

        holder.getParentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position, holder);
                }
            }
        });
        holder.getParentView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return itemLongClickListener != null
                        && itemLongClickListener.onItemLongClick(position, holder);
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ClickableViewHolder holder) {
        super.onViewRecycled(holder);
    }


}