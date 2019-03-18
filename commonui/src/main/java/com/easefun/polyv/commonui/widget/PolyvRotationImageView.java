package com.easefun.polyv.commonui.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class PolyvRotationImageView extends AppCompatImageView {

    public PolyvRotationImageView(Context context) {
        super(context);
    }

    public PolyvRotationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvRotationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSelected(boolean selected) {
        if (isSelected() != selected) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "rotation",
                    selected ? 45f : 90f, selected ? 90f : 45f);
            animator.setDuration(300);
            animator.start();
        }
        super.setSelected(selected);
    }
}
