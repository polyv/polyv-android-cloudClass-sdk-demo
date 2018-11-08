package com.easefun.polyv.commonui.player.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PolyvDotView extends View {
    private Paint mPaint;
    private int x, y, r;

    public PolyvDotView(Context context) {
        this(context, null);
    }

    public PolyvDotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStrokeWidth(1);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.parseColor("#14a2f4"));
    }

    public void init(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.r != 0)
            canvas.drawCircle(x, y, r, mPaint);
    }
}
