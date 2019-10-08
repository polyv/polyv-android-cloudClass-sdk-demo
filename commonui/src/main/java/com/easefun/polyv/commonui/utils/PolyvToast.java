package com.easefun.polyv.commonui.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyv.thirdpart.blankj.utilcode.util.ConvertUtils;
import com.easefun.polyv.commonui.R;

import java.util.ArrayList;
import java.util.List;

public class PolyvToast {
    private static final int REMOVE = 1;
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;
    private static final int LONG_DELAY = 2750;
    private static final int SHORT_DELAY = 1250;
    private List<Toast> toasts = new ArrayList<>();
    private Toast toast;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (toasts.size() > 0) {
                toasts.remove(0).cancel();
                if (toasts.size() > 0)
                    handler.sendEmptyMessageDelayed(REMOVE, toasts.get(0).getDuration() == LENGTH_SHORT ? SHORT_DELAY : LONG_DELAY);
            }
        }
    };

    public PolyvToast makeText(Context context, CharSequence charSequence, int duration) {
        if (toasts == null)
            toasts = new ArrayList<>();
        toast = Toast.makeText(context, charSequence, duration);
        View view = toast.getView();
        if (view != null) {
            view.setBackgroundColor(Color.parseColor("#00000000"));
            TextView message = view.findViewById(android.R.id.message);
            message.setBackgroundResource(R.drawable.polyv_tv_status);
            message.setGravity(Gravity.CENTER);
            message.setPadding(ConvertUtils.dp2px(12), message.getPaddingTop(), ConvertUtils.dp2px(12), message.getPaddingBottom());
            message.setTextColor(Color.WHITE);
        }
        return this;
    }

    public void show() {
        show(false);
    }

    public void show(boolean isCancelAll) {
        if (toast == null)
            return;
        if (isCancelAll)
            cancelAll();
        toasts.add(toast);
        if (toasts.size() == 1)
            handler.sendEmptyMessageDelayed(REMOVE, toast.getDuration() == LENGTH_SHORT ? SHORT_DELAY : LONG_DELAY);
        toast.show();
    }

    public void cancelAll() {
        handler.removeMessages(REMOVE);
        for (int i = toasts.size() - 1; i >= 0; i--)
            toasts.get(i).cancel();
        toasts.clear();
    }

    public void destroy() {
        if (toasts == null)
            return;
        cancelAll();
        toasts = null;
        toast = null;
    }
}
