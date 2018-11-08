package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.easefun.polyv.commonui.R;

public class PolyvPressedImageView extends AppCompatImageView {
    private int pressedColor;
    private int selectedColor;

    public PolyvPressedImageView(Context context) {
        this(context, null);
    }

    public PolyvPressedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPressedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PolyvPressedImageView);
        pressedColor = typedArray.getColor(R.styleable.PolyvPressedImageView_pressed_color, 0);//0，无效
        selectedColor = typedArray.getColor(R.styleable.PolyvPressedImageView_selected_color, 0);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPressed())
            // canvas.drawColor(0x33000000);
            setColorFilter(pressedColor);
        else if (isSelected() && selectedColor != 0)
            setColorFilter(selectedColor);
        else
            clearColorFilter();
    }

    @Override
    protected void dispatchSetSelected(boolean selected) {
        super.dispatchSetSelected(selected);
        if (selectedColor != 0)
            invalidate();
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        super.dispatchSetPressed(pressed);
        invalidate();
    }
}
