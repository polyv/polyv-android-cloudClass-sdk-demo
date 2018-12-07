package com.easefun.polyv.commonui.player.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.easefun.polyv.commonui.R;


public class PolyvSlideSwitchView extends View implements OnTouchListener {
    private boolean nowChoose = false;// 记录当前按钮是否打开,true为打开,flase为关闭

    private boolean isChecked;

    private boolean onSlip = false;// 记录用户是否在滑动的变量

    private float down_x, now_x;// 按下时的x,当前的x

    private Rect btn_off, btn_on;// 打开和关闭状态下,游标的Rect .

    private boolean isChangeOn = false;

    private boolean isInterceptOn = false;

    private OnChangedListener onChangedListener;

    private Bitmap bg_on, bg_off, slip_btn;

    public PolyvSlideSwitchView(Context context) {
        super(context);
        init();
    }

    public PolyvSlideSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PolyvSlideSwitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {// 初始化
        bg_on = BitmapFactory.decodeResource(getResources(), R.drawable.polyv_btn_platform_switch_open);
        bg_off = BitmapFactory.decodeResource(getResources(), R.drawable.polyv_btn_platform_switch_close);
        slip_btn = BitmapFactory.decodeResource(getResources(), R.drawable.polyv_btn_platform_switch_handle);
        btn_off = new Rect(0, 0, slip_btn.getWidth(), slip_btn.getHeight());
        btn_on = new Rect(bg_off.getWidth() - slip_btn.getWidth(), 0, bg_off.getWidth(), slip_btn.getHeight());
        setOnTouchListener(this);// 设置监听器,也可以直接复写OnTouchEvent
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {// 绘图函数

        super.onDraw(canvas);

        Matrix matrix = new Matrix();
        Paint paint = new Paint();
        float x;
        if (now_x < (bg_on.getWidth() / 2))// 滑动到前半段与后半段的背景不同,在此做判断
        {
            x = now_x - slip_btn.getWidth() / 2;
            canvas.drawBitmap(bg_off, matrix, paint);// 画出关闭时的背景
        } else {
            x = bg_on.getWidth() - slip_btn.getWidth() / 2;
            canvas.drawBitmap(bg_on, matrix, paint);// 画出打开时的背景
        }

        if (onSlip)// 是否是在滑动状态,
        {
            if (now_x >= bg_on.getWidth())// 是否划出指定范围,不能让游标跑到外头,必须做这个判断

                x = bg_on.getWidth() - slip_btn.getWidth() / 2;// 减去游标1/2的长度...

            else if (now_x < 0) {
                x = 0;
            } else {
                x = now_x - slip_btn.getWidth() / 2;
            }
        } else {// 非滑动状态
            if (nowChoose)// 根据现在的开关状态设置画游标的位置
            {
                x = btn_on.left;
                canvas.drawBitmap(bg_on, matrix, paint);// 初始状态为true时应该画出打开状态图片
            } else
                x = btn_off.left;
        }
        if (isChecked) {
            canvas.drawBitmap(bg_on, matrix, paint);
            x = btn_on.left;
            isChecked = !isChecked;
        }

        if (x < 0)// 对游标位置进行异常判断...
            x = 0;
        else if (x > bg_on.getWidth() - slip_btn.getWidth())
            x = bg_on.getWidth() - slip_btn.getWidth();
        canvas.drawBitmap(slip_btn, x, 0, paint);// 画出游标.

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        getParent().requestDisallowInterceptTouchEvent(true);
        boolean old = nowChoose;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:// 滑动
                now_x = event.getX();
                break;
            case MotionEvent.ACTION_DOWN:// 按下
                if (event.getX() > bg_on.getWidth() || event.getY() > bg_on.getHeight())
                    return false;
                onSlip = true;
                down_x = event.getX();
                now_x = down_x;
                break;
            case MotionEvent.ACTION_CANCEL: // 移到控件外部
                onSlip = false;
                boolean choose = nowChoose;
                if (now_x >= (bg_on.getWidth() / 2)) {
                    now_x = bg_on.getWidth() - slip_btn.getWidth() / 2;
                    nowChoose = true;
                } else {
                    now_x = now_x - slip_btn.getWidth() / 2;
                    nowChoose = false;
                }
                if (isChangeOn && (choose != nowChoose)) // 如果设置了监听器,就调用其方法..
                    onChangedListener.OnChanged(this, nowChoose);
                break;
            case MotionEvent.ACTION_UP:// 松开
                onSlip = false;
                boolean lastChoose = nowChoose;
                if (event.getX() >= (bg_on.getWidth() / 2)) {
                    now_x = bg_on.getWidth() - slip_btn.getWidth() / 2;
                    nowChoose = true;
                } else {
                    now_x = now_x - slip_btn.getWidth() / 2;
                    nowChoose = false;
                }
                if (isChangeOn && (lastChoose != nowChoose)) // 如果设置了监听器,就调用其方法..
                    onChangedListener.OnChanged(this, nowChoose);
                break;
            default:
        }
        if (!old && isInterceptOn) {

        } else {
            invalidate();// 重画控件
        }
        return true;
    }

    public void setOnChangedListener(OnChangedListener listener) {// 设置监听器,当状态修改的时候
        isChangeOn = true;
        onChangedListener = listener;
    }

    public void toggle() {
        setCheck(!nowChoose);
    }

    public interface OnChangedListener {
        abstract void OnChanged(View v, boolean checkState);
    }

    public void setCheck(boolean isChecked) {
        this.isChecked = isChecked;
        nowChoose = isChecked;
        if (isChecked == false) {
            now_x = 0;
        }
        invalidate();
    }

    public void setInterceptState(boolean isIntercept) {// 设置监听器,是否在重画钱拦截事件,状态由false变true时 拦截事件
        isInterceptOn = isIntercept;
        //onInterceptListener = listener;
    }
}
