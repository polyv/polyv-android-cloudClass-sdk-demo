package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyv.thirdpart.blankj.utilcode.util.KeyboardUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.cloudclassdemo.R;

/**
 * date: 2019/6/6 0006
 *
 * @author hwj
 * description 横屏发送弹幕输入框
 */
public class PolyvLandscapeDanmuSendPanel implements IPolyvLandscapeDanmuSender {
    private PopupWindow window;
    private AppCompatActivity activity;

    private OrientationSensibleLinearLayout llSendDanmu;
    private EditText etSendDanmu;
    private TextView tvSendDanmu;
    private ImageView ivSendDanmuClose;
    private View anchor;

    private IPolyvLandscapeDanmuSender.OnSendDanmuListener onSendDanmuListener;

    public PolyvLandscapeDanmuSendPanel(AppCompatActivity appCompatActivity,View anchor) {
        this.anchor=anchor;
        this.activity = appCompatActivity;
        this.window = new PopupWindow(activity);
        View root = LayoutInflater.from(activity).inflate(R.layout.polyv_cloudclass_send_danmu, null);
        window.setContentView(root);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        window.setBackgroundDrawable(null);

        int width = Math.max(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        int height = Math.min(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());

        window.setWidth(width);
        window.setHeight(height);

//        activity.getLifecycle().addObserver(new GenericLifecycleObserver() {
//            @Override
//            public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
//                switch (event) {
//                    case ON_DESTROY:
//                        window.dismiss();
//                        break;
//                }
//            }
//        });
        initView(root);
    }

    @Override
    public void dismiss() {
        if (window != null) {
            window.dismiss();
        }
    }

    @Override
    public void setOnSendDanmuListener(OnSendDanmuListener listener) {
        this.onSendDanmuListener = listener;
    }

    @Override
    public void openDanmuSender() {
        window.showAtLocation(anchor, Gravity.CENTER,0,0);
        llSendDanmu.post(new Runnable() {
            @Override
            public void run() {
                KeyboardUtils.showSoftInput(etSendDanmu);
            }
        });
    }

    private void initView(View root) {
        llSendDanmu = root.findViewById(R.id.ll_send_danmu);
        tvSendDanmu = root.findViewById(R.id.tv_send_danmu);
        etSendDanmu = root.findViewById(R.id.et_send_danmu);
        ivSendDanmuClose = root.findViewById(R.id.iv_send_danmu_close);
        llSendDanmu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                PolyvLandscapeDanmuSendPanel.this.onClick(view2);
            }
        });
        tvSendDanmu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                PolyvLandscapeDanmuSendPanel.this.onClick(view1);
            }
        });
        ivSendDanmuClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PolyvLandscapeDanmuSendPanel.this.onClick(view);
            }
        });

        etSendDanmu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {/**/}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {/**/}

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etSendDanmu.getText())) {
                    tvSendDanmu.setEnabled(false);
                } else {
                    tvSendDanmu.setEnabled(true);
                }
            }
        });
        llSendDanmu.onPortrait = new Runnable() {
            @Override
            public void run() {
                PolyvLandscapeDanmuSendPanel.this.hide();
            }
        };
    }

    private void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ll_send_danmu) {
            hide();

        } else if (i == R.id.tv_send_danmu) {
            sendDanmuAndChatMsg();

        } else if (i == R.id.iv_send_danmu_close) {
            hide();

        }
    }

    private void sendDanmuAndChatMsg() {
        String danmuMsg = etSendDanmu.getText().toString();
        if (TextUtils.isEmpty(danmuMsg)) {
            return;
        }
        etSendDanmu.setText("");
        //发送聊天室消息
        if (onSendDanmuListener != null) {
            onSendDanmuListener.onSendDanmu(danmuMsg);
        }
        KeyboardUtils.hideSoftInput(etSendDanmu);
        hide();
    }

    private void hide() {
        KeyboardUtils.hideSoftInput(etSendDanmu);
        window.dismiss();
    }

}
