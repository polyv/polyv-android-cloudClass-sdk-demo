/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import com.easefun.polyv.commonui.R;

public class PolyvMediaCheckView extends View {

    public static final int UNCHECKED = Integer.MIN_VALUE;
    private static final float STROKE_WIDTH = 4f; // dp
    private static final float SHADOW_WIDTH = 6.0f; // dp
    private static final int SIZE = 48; // dp
    private static final float STROKE_RADIUS = 11.5f; // dp
    private static final float BG_RADIUS = 11.0f; // dp
    private static final int CONTENT_SIZE = 16; // dp
    private boolean mCountable;
    private boolean mChecked;
    private Paint mStrokePaint;
    private Paint mBackgroundPaint;
    private float mDensity;
    private Rect mCheckRect;
    private boolean mEnabled = true;

    public PolyvMediaCheckView(Context context) {
        super(context);
        init(context, null);
    }

    public PolyvMediaCheckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PolyvMediaCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // fixed size 48dp x 48dp
        int sizeSpec = MeasureSpec.makeMeasureSpec((int) (SIZE * mDensity), MeasureSpec.EXACTLY);
        super.onMeasure(sizeSpec, sizeSpec);
    }

    private void init(Context context, AttributeSet attrs) {
        mDensity = context.getResources().getDisplayMetrics().density;

        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        mStrokePaint.setStrokeWidth(STROKE_WIDTH * mDensity);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PolyvMediaCheckView);

        int defaultColor = ResourcesCompat.getColor(
                getResources(), R.color.white,
                getContext().getTheme());
        int color = ta.getColor(R.styleable.PolyvMediaCheckView_borderColor, defaultColor);
        mStrokePaint.setColor(color);

        // draw content
        if (mBackgroundPaint == null) {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setAntiAlias(true);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            int defaultColorPaint = ResourcesCompat.getColor(
                    getResources(), R.color.item_checkCircle_backgroundColor,
                    getContext().getTheme());
            int colorPaint = ta.getColor(R.styleable.PolyvMediaCheckView_mainColor, defaultColorPaint);
            mBackgroundPaint.setColor(colorPaint);
        }

        ta.recycle();
    }

    public void setChecked(boolean checked) {
        if (mCountable) {
            throw new IllegalStateException("CheckView is countable, call setCheckedNum() instead.");
        }
        mChecked = checked;
        invalidate();
    }

    public void setCountable(boolean countable) {
        mCountable = countable;
    }

    public void setCheckedNum(int checkedNum) {
        if (!mCountable) {
            throw new IllegalStateException("CheckView is not countable, call setChecked() instead.");
        }
        if (checkedNum != UNCHECKED && checkedNum <= 0) {
            throw new IllegalArgumentException("checked num can't be negative.");
        }
        invalidate();
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw white stroke

        if (mChecked) {
            canvas.drawCircle((float) SIZE * mDensity / 2, (float) SIZE * mDensity / 2,
                    STROKE_RADIUS * mDensity, mStrokePaint);


//                mCheckDrawable.setBounds(getCheckRect());
//                mCheckDrawable.draw(canvas);
        }
        canvas.drawCircle((float) SIZE * mDensity / 2, (float) SIZE * mDensity / 2,
                BG_RADIUS * mDensity, mBackgroundPaint);
    }

    public String getBackgroundColor(){
        if(mBackgroundPaint != null){
            String value = Integer.toHexString(mBackgroundPaint.getColor());
            return "#"+value.substring(2);
        }
        return "";
    }
}
