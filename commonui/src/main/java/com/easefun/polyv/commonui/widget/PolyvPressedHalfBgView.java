package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class PolyvPressedHalfBgView extends AppCompatTextView {

    public PolyvPressedHalfBgView(Context context) {
        super(context);
    }

    public PolyvPressedHalfBgView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvPressedHalfBgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int solidColor = getSolidColor();
        int color = Color.argb(99, Color.red(solidColor), Color.green(solidColor), Color.blue(solidColor));
        if (isPressed())
            canvas.drawColor(color);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        super.dispatchSetPressed(pressed);
        invalidate();
    }
}
