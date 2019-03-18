package com.easefun.polyv.commonui.adapter.itemview;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author df
 * @create 2019/1/16
 * @Describe
 */
public abstract class IPolyvCustomMessageBaseItemView<T> extends FrameLayout {
    protected TextView defaultView = null;
    protected RequestOptions requestOptions_s;
    public IPolyvCustomMessageBaseItemView(@NonNull Context context) {
        this(context,null);
    }

    public IPolyvCustomMessageBaseItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IPolyvCustomMessageBaseItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initView();
    }

    //初始化公共数据
    protected void init(){
        requestOptions_s = new RequestOptions()
                .placeholder(com.easefun.polyv.commonui.R.drawable.polyv_missing_face)
                .error(com.easefun.polyv.commonui.R.drawable.polyv_missing_face)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
    }

    //处理对应的数据信息，展示内容
    public abstract void processMessage(T data, int pos);

    //初始化view
    public abstract void initView();

    //播放动画
    public abstract void playAnimation();

    /**
     * 根据图片的名称获取对应的资源id
     * @param resourceName
     * @return
     */
    public int getDrawResourceID(String resourceName) {
        Resources res=getResources();
        int picid = res.getIdentifier(resourceName,"drawable",getContext().getPackageName());
        return picid;
    }

}
