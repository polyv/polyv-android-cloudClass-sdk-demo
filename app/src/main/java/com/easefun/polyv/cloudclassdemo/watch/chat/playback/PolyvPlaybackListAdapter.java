package com.easefun.polyv.cloudclassdemo.watch.chat.playback;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyv.cloudclass.model.PolyvPlaybackListVO;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.utils.imageloader.PolyvImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 回放列表适配器
 */
public class PolyvPlaybackListAdapter extends RecyclerView.Adapter<PolyvPlaybackListAdapter.PlaybackListViewHolder> {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private List<PolyvPlaybackListVO.DataBean.ContentsBean> contentsBeanList;

    private int selPosition;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造方法">
    public PolyvPlaybackListAdapter() {
        contentsBeanList = new ArrayList<>();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="数据操作">
    public void addDataNotify(List<PolyvPlaybackListVO.DataBean.ContentsBean> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        int size = contentsBeanList.size();
        contentsBeanList.addAll(list);
        notifyItemRangeInserted(size, list.size());
    }

    //需在添加数据前设置
    public void setSelPosition(int position) {
        this.selPosition = position;
    }

    //需在添加数据后设置
    public void updateSelPosition(int position) {
        int oldSelPosition = selPosition;
        selPosition = position;
        notifyItemChanged(oldSelPosition, "payload");
        notifyItemChanged(selPosition, "payload");
    }

    public int getSelPosition() {
        return selPosition;
    }

    public List<PolyvPlaybackListVO.DataBean.ContentsBean> getContentsBeanList() {
        return contentsBeanList;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="view事件监听器">
    private ViewActionListener viewActionListener;

    public void setOnViewActionListener(ViewActionListener listener) {
        this.viewActionListener = listener;
    }

    public interface ViewActionListener {
        void onItemClick(int position, PolyvPlaybackListVO.DataBean.ContentsBean contentsBean);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现RecyclerView.Adapter方法">
    @NonNull
    @Override
    public PlaybackListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.polyv_playback_list_item, parent, false);
        return new PlaybackListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaybackListViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.updateSelPositionView(position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PlaybackListViewHolder holder, int position) {
        holder.processData(contentsBeanList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return contentsBeanList.size();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="viewHolder">
    public class PlaybackListViewHolder extends RecyclerView.ViewHolder {
        private ImageView coverIv;
        private TextView durationTv;
        private ViewGroup playStatusLy;
        private TextView titleTv;
        private PolyvPlaybackListVO.DataBean.ContentsBean contentsBean;

        public PlaybackListViewHolder(View itemView) {
            super(itemView);
            coverIv = itemView.findViewById(R.id.cover_iv);
            durationTv = itemView.findViewById(R.id.duration_tv);
            playStatusLy = itemView.findViewById(R.id.play_status_ly);
            titleTv = itemView.findViewById(R.id.title_tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int oldSelPosition = selPosition;
                    selPosition = getVHPosition();
                    if (viewActionListener != null) {
                        viewActionListener.onItemClick(selPosition, contentsBean);
                    }
                    notifyItemChanged(oldSelPosition, "payload");
                    notifyItemChanged(selPosition, "payload");
                }
            });
        }

        protected int getVHPosition() {//getAdapterPosition()
            int position = 0;//item 移动时 position 需更新
            for (int i = 0; i < contentsBeanList.size(); i++) {
                Object obj = contentsBeanList.get(i);
                if (obj == contentsBean) {
                    position = i;
                    break;
                }
            }
            return position;
        }

        public void updateSelPositionView(int position) {
            if (position == selPosition) {
                playStatusLy.setVisibility(View.VISIBLE);
                titleTv.setSelected(true);
            } else {
                playStatusLy.setVisibility(View.GONE);
                titleTv.setSelected(false);
            }
        }

        public void processData(PolyvPlaybackListVO.DataBean.ContentsBean contentsBean, int position) {
            this.contentsBean = contentsBean;

            PolyvImageLoader.getInstance().loadImage(itemView.getContext(), contentsBean.getFirstImage(), coverIv);

            durationTv.setText(contentsBean.getDuration());

            updateSelPositionView(position);

            titleTv.setText(contentsBean.getTitle());
        }
    }
    // </editor-fold>
}
