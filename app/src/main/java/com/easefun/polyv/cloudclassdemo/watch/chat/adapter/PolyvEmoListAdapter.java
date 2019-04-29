package com.easefun.polyv.cloudclassdemo.watch.chat.adapter;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.adapter.viewholder.ClickableViewHolder;
import com.easefun.polyv.commonui.utils.PolyvFaceManager;

import java.util.ArrayList;
import java.util.List;

public class PolyvEmoListAdapter extends PolyvBaseRecyclerViewAdapter {
    public List<String> emoLists;

    public PolyvEmoListAdapter(RecyclerView recyclerView) {
        super(recyclerView);
        emoLists = new ArrayList<>(PolyvFaceManager.getInstance().getFaceMap().keySet());
    }

    public class EmoItemViewHolder extends ClickableViewHolder<Object,PolyvEmoListAdapter> {
        private ImageView emo;

        public EmoItemViewHolder(View itemView,PolyvEmoListAdapter adapter) {
            super(itemView, adapter);
            emo = $(R.id.iv_emo);
        }

        @Override
        public void processNormalMessage(Object item, int position) {
            EmoItemViewHolder emoItemViewHolder = this;
            int id = PolyvFaceManager.getInstance().getFaceId(emoLists.get(position));
            Drawable drawable = getContext().getResources().getDrawable(id);
            emoItemViewHolder.emo.setImageDrawable(drawable);
        }

        @Override
        public  void processCustomMessage(PolyvCustomEvent item, int position) {

        }

        @Override
        public <T> IPolyvCustomMessageBaseItemView createItemView(PolyvCustomEvent<T> baseCustomEvent) {
            return null;
        }
    }

    @NonNull
    @Override
    public ClickableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new EmoItemViewHolder
                (LayoutInflater.from(getContext()).inflate(R.layout.polyv_chat_emo_item, parent, false),this);
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof EmoItemViewHolder) {
            holder.processNormalMessage(null,position);
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return emoLists.size();
    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
