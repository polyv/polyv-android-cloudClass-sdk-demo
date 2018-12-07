package com.easefun.polyv.commonui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.utils.PolyvBezierEvaluator;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import java.lang.ref.WeakReference;
import java.util.Random;

public class PolyvLikeIconView extends RelativeLayout {

    private int width, height;
    private int iconWidth, iconHeight;

    private Interpolator[] interpolators;
    private Random random = new Random();

    public PolyvLikeIconView(Context context) {
        this(context, null);
    }

    public PolyvLikeIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvLikeIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initInterpolator();
    }

    private void initInterpolator() {
        interpolators = new Interpolator[]{
                new LinearInterpolator(),
                new AccelerateDecelerateInterpolator(),
                new AccelerateInterpolator(),
                new DecelerateInterpolator(),
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (getChildCount() == 0) {
//            final FloatingActionButton view = new FloatingActionButton(getContext());
//            view.setImageResource(R.drawable.polyv_icon_like);
//            view.setBackgroundTintList(ColorStateList.valueOf(color[2]));
//            view.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (onButtonClickListener != null)
//                        onButtonClickListener.onClick(view);
//                }
//            });
//            int width = PolyvScreenUtils.px2dip(getContext(),128);
//            LayoutParams rlp = new LayoutParams(width, width);
//            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            rlp.bottomMargin = PolyvScreenUtils.dip2px(getContext(), 16);
//            rlp.rightMargin = PolyvScreenUtils.dip2px(getContext(),16);
//            view.setLayoutParams(rlp);
//
//            addView(view);
        }
    }

    private OnClickListener onButtonClickListener;

    public void setOnButtonClickListener(@Nullable OnClickListener l) {
        onButtonClickListener = l;
    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        super.onDetachedFromWindow();
    }

    private void startAnimator(ImageView view) {
        //曲线的两个顶点
        PointF pointF1 = new PointF(
                random.nextInt(width),
                random.nextInt(height / 2) + height / 2.5f);
        PointF pointF2 = new PointF(
                random.nextInt(width),
                random.nextInt(height / 2));
        PointF pointStart = new PointF((width - iconWidth) / 1.6f,
                height - iconHeight * 3 - PolyvScreenUtils.dip2px(getContext(), 16));
        PointF pointEnd = new PointF(random.nextInt(width), random.nextInt(height / 2));

        //贝塞尔估值器
        PolyvBezierEvaluator evaluator = new PolyvBezierEvaluator(pointF1, pointF2);
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, pointStart, pointEnd);
        animator.setTarget(view);
        animator.setDuration(2000);
        animator.addUpdateListener(new UpdateListener(view));
        animator.addListener(new AnimatorListener(view, this));
        animator.setInterpolator(interpolators[random.nextInt(4)]);

        animator.start();
    }

    public void addLoveIcon(int resId) {
        if (height <= 0 || width <= 0)
            return;
        ImageView view = new ImageView(getContext());
        view.setImageResource(resId);
        iconWidth = view.getDrawable().getIntrinsicWidth();
        iconHeight = view.getDrawable().getIntrinsicHeight();

        addView(view);
        startAnimator(view);
    }

    private int[] color = new int[]{0xFF9D86D2, 0xFFF25268, 0xFF5890FF, 0xFFFCBC71};
    private Random randomColor = new Random();

    public void addLoveIcon() {
        if (height <= 0 || width <= 0)
            return;
        post(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton view = new FloatingActionButton(getContext());
                view.setImageResource(R.drawable.polyv_icon_like);
                view.setBackgroundTintList(ColorStateList.valueOf(color[randomColor.nextInt(color.length)]));
                iconWidth = view.getDrawable().getIntrinsicWidth();
                iconHeight = view.getDrawable().getIntrinsicHeight();

                addView(view);
                startAnimator(view);
            }
        });

    }

    public static class UpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private WeakReference<View> iv;

        public UpdateListener(View iv) {
            this.iv = new WeakReference<>(iv);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            PointF pointF = (PointF) animation.getAnimatedValue();
            View view = iv.get();
            if (null != view) {
                view.setX(pointF.x);
                view.setY(pointF.y);
                view.setAlpha(1 - animation.getAnimatedFraction() + 0.1f);
            }
        }
    }

    public static class AnimatorListener extends AnimatorListenerAdapter {

        private WeakReference<View> iv;
        private WeakReference<PolyvLikeIconView> parent;

        public AnimatorListener(View iv, PolyvLikeIconView parent) {
            this.iv = new WeakReference<>(iv);
            this.parent = new WeakReference<>(parent);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            View view = iv.get();
            PolyvLikeIconView parent = this.parent.get();
            if (null != view
                    && null != parent) {
                parent.removeView(view);
            }
        }
    }
}
