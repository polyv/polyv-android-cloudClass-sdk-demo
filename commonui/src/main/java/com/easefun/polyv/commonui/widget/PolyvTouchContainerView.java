package com.easefun.polyv.commonui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.easefun.polyv.thirdpart.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;

/**
 * @author df
 * @create 2018/8/11
 * @Describe
 */
public class PolyvTouchContainerView extends FrameLayout {
    private static final String TAG = "PolyvTounchContainer";

    // 点击的位置
    private float lastX, lastY;

    // 竖屏下的位置
    private int originLeft,portraitLeft = 0;
    private int originTop,portraitTop = 0;

    //键盘弹起前得位置
    private int beforeSoftLeft = 0;
    private int beforeSoftTop = 0;
    private RotateTask rotateTask ;

    private boolean canMove;//是否能移动

    public PolyvTouchContainerView(Context context) {
        this(context,null);
    }

    public PolyvTouchContainerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PolyvTouchContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialConfig();
    }

    private void initialConfig() {
        rotateTask = new RotateTask();
    }

    public void initParam(int left ,int top){
        originLeft = left;
        originTop = top;
        PolyvCommonLog.d(TAG,"left:"+left+"  top:"+top);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!canMove){
            return super.onTouchEvent(event);
        }
        //子view为invisible时(即非visible)不要拦截点击事件
        boolean firstChildIsVisible = getChildAt(0) == null || (getChildAt(0).getVisibility() == View.VISIBLE);
        if (/*getVisibility() != View.VISIBLE || */!firstChildIsVisible)
            return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastX = event.getX();
            lastY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // 计算移动的距离
            float x = event.getX();
            float y = event.getY();
            // 偏移量
            int offX = (int) (x - lastX);
            int offY = (int) (y - lastY);
            View view = this;
            int left = view.getLeft() + offX;
            int top = view.getTop() + offY;
            int parentWidth = ((View) view.getParent()).getMeasuredWidth();
            int parentHeight = ((View) view.getParent()).getMeasuredHeight();
            if (offX < 0 && left < 0)
                left = 0;
            if (offY < 0 && top < 0)
                top = 0;
            if (offX > 0 && view.getRight() + offX > parentWidth)
                left = view.getLeft() + (parentWidth - view.getRight());
            if (offY > 0 && view.getBottom() + offY > parentHeight)
                top = view.getTop() + (parentHeight - view.getBottom());

            ViewGroup.MarginLayoutParams rlp = null;
            if (getParent() instanceof RelativeLayout) {
                rlp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            } else if (getParent() instanceof LinearLayout) {
                rlp = (LinearLayout.LayoutParams) view.getLayoutParams();
            } else if (getParent() instanceof FrameLayout) {
                rlp = (FrameLayout.LayoutParams) view.getLayoutParams();
            } else {
                return true;
            }
            rlp.leftMargin = left;
            rlp.topMargin = top;
            view.setLayoutParams(rlp);
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            lastX = 0;
            lastY = 0;
        }
        return true;
    }

    public void resetFloatViewLand() {
//        canMove = true;
        ViewGroup.MarginLayoutParams layoutParams = null;
        if (getParent() instanceof RelativeLayout) {
            layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        } else {
            return;
        }
        PolyvCommonLog.d(TAG,"left ;"+layoutParams.leftMargin+"  width :"+getMeasuredWidth()+"  width :"+ScreenUtils.getScreenWidth());
        portraitLeft = layoutParams.leftMargin;
        portraitTop = layoutParams.topMargin;

        Log.d(TAG, "resetFloatViewLand: portraitLeft :" + portraitLeft + " portraitTop :"
                + portraitTop+ "   width :" + getMeasuredWidth());

        layoutParams.leftMargin = 0;
        layoutParams.topMargin = 0;
        setLayoutParams(layoutParams);

    }

    public void resetFloatViewPort() {
//        canMove = false;
        MarginLayoutParams rlp = null;
        if (getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            rlp = (LinearLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) getLayoutParams();
        } else {
            return;
        }
        Log.d(TAG, "resetFloatViewPort: portraitLeft :" +portraitLeft+ " parent portraitTop :"
                + portraitTop + "   width :" + getMeasuredWidth());
        if(portraitLeft + getMeasuredWidth() >= ScreenUtils.getScreenWidth()){
            rlp.leftMargin = originLeft;
            rlp.topMargin = originTop;
        }else{
            rlp.leftMargin = portraitLeft;
            rlp.topMargin = portraitTop;
        }
        setLayoutParams(rlp);

    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rotateTask.buildConfig(newConfig);
        if(getHandler() != null){
            getHandler().removeCallbacks(rotateTask);
        }
        post(rotateTask);

    }

    public boolean isCanMove() {
        return canMove;
    }

    class RotateTask implements Runnable {
        public Configuration newConfig;

        public void buildConfig(Configuration configuration){
            newConfig = configuration;
        }
        @Override
        public void run() {

            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                resetFloatViewPort();
            } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                resetFloatViewLand();
            }
        }
    }

    public void topSubviewTo(final int top) {
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams rlp = getLayoutParamsLayout();
                if (rlp == null) {
                    return;
                }
                beforeSoftLeft = rlp.leftMargin;
                beforeSoftTop = rlp.topMargin;
                if(rlp.topMargin+rlp.height < top){
                    return;
                }

                PolyvCommonLog.d(TAG, "topSubviewTo left :" + beforeSoftLeft + "   top " + top);
                rlp.topMargin = top - rlp.height;
                setLayoutParams(rlp);
            }
        });

    }

    public void resetSoftTo() {
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.MarginLayoutParams rlp = getLayoutParamsLayout();
                if (rlp == null) {
                    return;
                }
                PolyvCommonLog.d(TAG, "resetSoftTo left :" + beforeSoftLeft + "   top " + beforeSoftTop);
                rlp.leftMargin = beforeSoftLeft;
                rlp.topMargin = beforeSoftTop;
                setLayoutParams(rlp);
            }
        });

    }

    private ViewGroup.MarginLayoutParams getLayoutParamsLayout() {
        ViewGroup.MarginLayoutParams rlp = null;
        if (getParent() instanceof RelativeLayout) {
            rlp = (RelativeLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof LinearLayout) {
            rlp = (LinearLayout.LayoutParams) getLayoutParams();
        } else if (getParent() instanceof FrameLayout) {
            rlp = (FrameLayout.LayoutParams) getLayoutParams();
        }

        return rlp;
    }

    public void setOriginLeft(int originLeft) {
        this.originLeft = originLeft;
    }

    public void setOriginTop(int originTop) {
        this.originTop = originTop;
    }

    public void setContainerMove(boolean canMove){
        this.canMove = canMove;
    }
}
