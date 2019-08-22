package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;
import com.easefun.polyv.businesssdk.model.video.PolyvBitrateVO;
import com.easefun.polyv.businesssdk.model.video.PolyvDefinitionVO;
import com.easefun.polyv.businesssdk.model.video.PolyvLiveLinesVO;
import com.easefun.polyv.businesssdk.model.video.PolyvMediaPlayMode;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import java.util.List;

/**
 * date: 2019/6/10 0010
 *
 * @author hwj
 * description PolyvCloudClassMediaController右上角的“更多”布局
 */
public class PolyvCloudClassMoreLayout {
    private View anchor;
    private PopupWindow window;
    private Activity activity;

    //View
    private FrameLayout flMoreRoot;
    private OrientationSensibleLinearLayout llMoreVertical;
    private RecyclerView rvBitrate,rvLines;
    private RvMoreAdapter rvAdapter;
    private RvLinesAdapter linesAdapter;
    private TextView tvOnlyAudioSwitch;
    private ImageView ivCloseMore;
    private FrameLayout llBitrate,linesContainer;

    //callback
    private ShowMediaControllerFunction showMediaControllerFunction;
    private ShowGradientBarFunction showGradientBarFunction;
    private OnBitrateSelectedListener onBitrateSelectedListener;
    private OnLinesSelectedListener onLinesSelectedListener;
    private OnOnlyAudioSwitchListener onOnlyAudioSwitchListener;

    //constant
    private final static int WIDTH_PORT = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
    private final static int WIDTH_LAND = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) / 2;
    private final static int HEIGHT_LAND = WIDTH_PORT;
    private int HEIGHT_PORT = PolyvScreenUtils.getHeight();
    private final static int MARGIN_TOP_PORTRAIT = PolyvScreenUtils.dip2px(Utils.getApp(), 35);
    private final static String TEXT_MODE_AUDIO = "仅听声音";
    private final static String TEXT_MODE_VIDEO = "播放画面";

    //flag
    private boolean isSupportBitrate = false;


    public PolyvCloudClassMoreLayout(Activity activity, View anchor) {
        this.anchor = anchor;
        this.activity = activity;
        this.window = new PopupWindow(activity);
        View root = LayoutInflater.from(activity).inflate(R.layout.polyv_cloudclass_controller_more, null);
        window.setContentView(root);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setBackgroundDrawable(null);
        window.setOnDismissListener(() -> {
            if (showGradientBarFunction!=null){
                showGradientBarFunction.showGradientBar(true);
            }
        });

        window.setWidth(WIDTH_PORT);
        window.setHeight(HEIGHT_PORT);

        initView(root);
    }

    public void hide() {
        window.dismiss();
    }


    public void showWhenPortrait() {
        onPortrait();
        show();
        showMediaController();
    }


    public void showWhenLandscape() {
        onLandscape();
        show();
        showMediaController();
    }

    public void injectShowMediaControllerFunction(ShowMediaControllerFunction function) {
        this.showMediaControllerFunction = function;
    }
    public void injectShowGradientBarFunction(ShowGradientBarFunction function){
        this.showGradientBarFunction=function;
    }

    public void setOnBitrateSelectedListener(OnBitrateSelectedListener onBitrateSelectedListener) {
        this.onBitrateSelectedListener = onBitrateSelectedListener;
    }

    public void setOnOnlyAudioSwitchListener(OnOnlyAudioSwitchListener onOnlyAudioSwitchListener) {
        this.onOnlyAudioSwitchListener = onOnlyAudioSwitchListener;
    }

    public void setOnLinesSelectedListener(OnLinesSelectedListener onLinesSelectedListener) {
        this.onLinesSelectedListener = onLinesSelectedListener;
    }

    public void initBitrate(PolyvBitrateVO bitrateVO) {
        isSupportBitrate = !bitrateVO.getDefinitions().isEmpty();
        showBitrate(true);
        rvAdapter.updateBitrateListData(bitrateVO);
    }

    public void initLines(List<PolyvLiveLinesVO> lines) {
        linesAdapter.updateLinesDatas(lines);
        showLines(true);
    }

    public void updateLinesStatus(int pos) {
        linesAdapter.updateLinesStatus(pos);
    }


    //从别处而不是从MoreLayout里切换模式，所以改变当前MoreLayout的状态。
    public void onChangeAudioOrVideoMode(@PolyvMediaPlayMode.Mode int mediaPlayMode) {
        boolean isAudioNow = mediaPlayMode == PolyvMediaPlayMode.MODE_AUDIO;
        if (isAudioNow) {
            showBitrate(false);
            showLines(false);
            tvOnlyAudioSwitch.setText(TEXT_MODE_VIDEO);
        } else {
            showBitrate(true);
            showLines(true);
            tvOnlyAudioSwitch.setText(TEXT_MODE_AUDIO);
        }
        tvOnlyAudioSwitch.setSelected(isAudioNow);
    }

    ////////////////////private below///////////////////////

    private void onLandscape() {
        window.setWidth(WIDTH_LAND);
        window.setHeight(HEIGHT_LAND);
        if (window.isShowing()) {
            window.update();
        }
        llMoreVertical.post(() -> {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) llMoreVertical.getLayoutParams();
            lp.width = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()) / 2;
            lp.topMargin = MARGIN_TOP_PORTRAIT;
            lp.gravity = Gravity.TOP;
            llMoreVertical.setLayoutParams(lp);
            ivCloseMore.setVisibility(View.GONE);
        });
    }

    private void onPortrait() {
        window.setWidth(WIDTH_PORT);
        window.setHeight(HEIGHT_PORT);
        if (window.isShowing()) {
            window.update();
        }
        llMoreVertical.post(() -> {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) llMoreVertical.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.topMargin = 0;
            lp.gravity = Gravity.CENTER;
            llMoreVertical.setLayoutParams(lp);
            ivCloseMore.setVisibility(View.VISIBLE);
        });
    }


    private void initView(View root) {
        flMoreRoot = (FrameLayout) root.findViewById(R.id.fl_more_root);
        flMoreRoot.setOnClickListener(v -> hide());

        llMoreVertical = (OrientationSensibleLinearLayout) root.findViewById(R.id.ll_more_vertical);
        llMoreVertical.onLandscape = this::onLandscape;
        llMoreVertical.onPortrait = this::onPortrait;

        rvBitrate = (RecyclerView) root.findViewById(R.id.rv_more_bitrate);
        rvAdapter = new RvMoreAdapter();
        rvBitrate.setAdapter(rvAdapter);
        rvBitrate.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,false));

        tvOnlyAudioSwitch = (TextView) root.findViewById(R.id.cb_only_audio_switch);
        //多綫路
        linesAdapter = new RvLinesAdapter();
        rvLines = root.findViewById(R.id.rv_more_lines);
        rvLines.setAdapter(linesAdapter);
        rvLines.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,false));
        linesContainer = root.findViewById(R.id.fl_lines);

        tvOnlyAudioSwitch = root.findViewById(R.id.cb_only_audio_switch);
        tvOnlyAudioSwitch.setSelected(false);
        tvOnlyAudioSwitch.setOnClickListener(v -> {
            //点击后，将要生效的模式
            boolean isAudioNow = !tvOnlyAudioSwitch.isSelected();
            //是否成功切换模式
            boolean isChangeModeSucceed = false;

            if (onOnlyAudioSwitchListener != null) {
                isChangeModeSucceed = onOnlyAudioSwitchListener.onOnlyAudioSelect(isAudioNow);
            }
            if (!isChangeModeSucceed) {
                hide();
                return;
            }
            if (isAudioNow) {
                showBitrate(false);
                showLines(false);
                tvOnlyAudioSwitch.setText(TEXT_MODE_VIDEO);
            } else {
                showBitrate(true);
                showLines(true);
                tvOnlyAudioSwitch.setText(TEXT_MODE_AUDIO);
            }
            tvOnlyAudioSwitch.setSelected(isAudioNow);
            hide();
        });

        ivCloseMore = (ImageView) root.findViewById(R.id.iv_close_more);
        ivCloseMore.setOnClickListener(v -> hide());

        llBitrate = (FrameLayout) root.findViewById(R.id.fl_bitrate);
    }

    private void showMediaController() {
        if (showMediaControllerFunction != null) {
            showMediaControllerFunction.showMediaController();
        }
    }

    private void show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.showAsDropDown(anchor, 0, -window.getHeight(), Gravity.RIGHT);
        } else {
            window.showAtLocation(anchor, Gravity.RIGHT, 0, 0);
        }
        if (showGradientBarFunction!=null){
            showGradientBarFunction.showGradientBar(false);
        }
    }

    private void showBitrate(boolean show) {
        if (show && isSupportBitrate) {
            llBitrate.setVisibility(View.VISIBLE);
        } else {
            llBitrate.setVisibility(View.GONE);
        }
    }

    private void showLines(boolean show) {
        linesContainer.setVisibility(show && linesAdapter.getItemCount() >1?View.VISIBLE:View.GONE);
    }


    /////////////////////
    //function
    /////////////////////
    public interface ShowMediaControllerFunction {
        void showMediaController();
    }

    public interface ShowGradientBarFunction {
        void showGradientBar(boolean show);
    }

    /////////////////////
    //listener
    /////////////////////
    public interface OnBitrateSelectedListener {
        void onBitrateSelected(PolyvDefinitionVO definitionVO, int pos);
    }

    public interface OnLinesSelectedListener {
        void onLineSelected(PolyvLiveLinesVO linesVO, int bitratePos);
    }

    public interface OnOnlyAudioSwitchListener {
        boolean onOnlyAudioSelect(boolean onlyAudio);
    }

    //////////////////////
    //RecyclerView适配器
    //////////////////////
    private class RvMoreAdapter extends RecyclerView.Adapter<RvMoreAdapter.RvMoreViewHolder> {


        private int curSelectPos = -1;
        private boolean isInitDefaultDefinition = false;
        private PolyvBitrateVO bitrateVO;

        void updateBitrateListData(PolyvBitrateVO bitrateVO) {
            this.bitrateVO = bitrateVO;
            if (!isInitDefaultDefinition) {
                for (int i = 0; i < bitrateVO.getDefinitions().size(); i++) {
                    if (bitrateVO.getDefinitions().get(i).definition.equals(bitrateVO.getDefaultDefinition())) {
                        curSelectPos = i;
                        break;
                    }
                }
                isInitDefaultDefinition = true;
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RvMoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.polyv_cloud_class_item_bitrate, parent, false);
            return new RvMoreViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RvMoreViewHolder holder, int position) {
            List<PolyvDefinitionVO> definitionVOList = bitrateVO.getDefinitions();
            String defenition = definitionVOList.get(position).definition;
            holder.tvBitrate.setText(defenition);

            if (position == curSelectPos) {
                holder.tvBitrate.setSelected(true);
            } else {
                holder.tvBitrate.setSelected(false);
            }

            holder.itemView.setOnClickListener((itemView) -> {
                curSelectPos = holder.getAdapterPosition();
                notifyDataSetChanged();
                if (onBitrateSelectedListener != null) {
                    onBitrateSelectedListener.onBitrateSelected(definitionVOList.get(curSelectPos), curSelectPos);
                }
                rvBitrate.post(PolyvCloudClassMoreLayout.this::hide);
            });
        }

        @Override
        public int getItemCount() {
            if (bitrateVO != null && bitrateVO.getDefinitions() != null) {
                return bitrateVO.getDefinitions().size();
            } else {
                return 0;
            }
        }

        public int getCurSelectPos() {
            return curSelectPos;
        }

        class RvMoreViewHolder extends RecyclerView.ViewHolder {
            TextView tvBitrate;

            RvMoreViewHolder(View itemView) {
                super(itemView);
                tvBitrate = (TextView) itemView.findViewById(R.id.tv_bitrate);
            }
        }

    }

    //////////////////////
    //RecyclerView适配器
    //////////////////////
    private class RvLinesAdapter extends RecyclerView.Adapter<RvLinesAdapter.RvLinesViewHolder> {
        private int curSelectPos = 0;
        private PolyvLiveLinesVO lastSelectedLine;
        private List<PolyvLiveLinesVO> lines;
        private boolean isInitDefaultDefinition = false;


        @NonNull
        @Override
        public RvLinesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.polyv_cloud_class_item_bitrate, parent, false);
            return new RvLinesViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RvLinesViewHolder holder, int position) {
           PolyvLiveLinesVO linesVO = lines.get(position);
            holder.tvBitrate.setText("线路"+(position+1));

//            if (position == curSelectPos) {
//                holder.tvBitrate.setSelected(true);
//            } else {
//                holder.tvBitrate.setSelected(false);
//            }
            holder.tvBitrate.setSelected(position == curSelectPos);

            holder.itemView.setOnClickListener((itemView) -> {
                if(holder.getAdapterPosition() == curSelectPos){
                    return;
                }
                curSelectPos = holder.getAdapterPosition();
                if(lastSelectedLine != null){
                    lastSelectedLine.setSelected(false);
                }
                linesVO.setSelected(true);
                lastSelectedLine = linesVO;

                notifyDataSetChanged();
                if (onLinesSelectedListener != null) {
                    onLinesSelectedListener.onLineSelected(linesVO, position);
                }
                rvBitrate.post(PolyvCloudClassMoreLayout.this::hide);
            });
        }

        @Override
        public int getItemCount() {
            if (lines != null ) {
                return lines.size();
            } else {
                return 0;
            }
        }

        public void updateLinesDatas(List<PolyvLiveLinesVO> lines) {
            this.lines = lines;
            notifyDataSetChanged();
        }

        public void updateLinesStatus(int pos) {
            this.curSelectPos = pos;
        }

        class RvLinesViewHolder extends RecyclerView.ViewHolder {
            TextView tvBitrate;

            RvLinesViewHolder(View itemView) {
                super(itemView);
                tvBitrate = itemView.findViewById(R.id.tv_bitrate);
            }
        }

    }
}
