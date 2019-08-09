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
    private TextView tvOnlyAudioSwitch;
    private ImageView ivCloseMore;
    private FrameLayout llBitrate;

    //callback
    private ShowMediaControllerFunction showMediaControllerFunction;
    private ShowGradientBarFunction showGradientBarFunction;
    private OnBitrateSelectedListener onBitrateSelectedListener;
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

    public void initBitrate(PolyvBitrateVO bitrateVO) {
        isSupportBitrate = !bitrateVO.getDefinitions().isEmpty();
        showBitrate(true);
        rvAdapter.updateBitrateListData(bitrateVO);
    }

    //从别处而不是从MoreLayout里切换模式，所以改变当前MoreLayout的状态。
    public void onChangeAudioOrVideoMode(@PolyvMediaPlayMode.Mode int mediaPlayMode) {
        boolean isAudioNow = mediaPlayMode == PolyvMediaPlayMode.MODE_AUDIO;
        if (isAudioNow) {
            showBitrate(false);
            tvOnlyAudioSwitch.setText(TEXT_MODE_VIDEO);
        } else {
            showBitrate(true);
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
        flMoreRoot = root.findViewById(R.id.fl_more_root);
        flMoreRoot.setOnClickListener(v -> hide());

        llMoreVertical = root.findViewById(R.id.ll_more_vertical);
        llMoreVertical.onLandscape = this::onLandscape;
        llMoreVertical.onPortrait = this::onPortrait;

        rvBitrate = root.findViewById(R.id.rv_more_bitrate);
        rvAdapter = new RvMoreAdapter();
        rvBitrate.setAdapter(rvAdapter);
        rvBitrate.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,false));

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
                tvOnlyAudioSwitch.setText(TEXT_MODE_VIDEO);
            } else {
                showBitrate(true);
                tvOnlyAudioSwitch.setText(TEXT_MODE_AUDIO);
            }
            tvOnlyAudioSwitch.setSelected(isAudioNow);
            hide();
        });

        ivCloseMore = root.findViewById(R.id.iv_close_more);
        ivCloseMore.setOnClickListener(v -> hide());

        llBitrate = root.findViewById(R.id.fl_bitrate);
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

        class RvMoreViewHolder extends RecyclerView.ViewHolder {
            TextView tvBitrate;

            RvMoreViewHolder(View itemView) {
                super(itemView);
                tvBitrate = itemView.findViewById(R.id.tv_bitrate);
            }
        }

    }
}
