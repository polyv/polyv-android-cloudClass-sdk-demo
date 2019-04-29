package com.easefun.polyv.commonui.adapter.itemview;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author df
 * @create 2019/1/16
 * @Describe
 */
public abstract class IPolyvCustomMessageBaseItemView<T> extends FrameLayout {
    public IPolyvCustomMessageBaseItemView(@NonNull Context context) {
        this(context,null);
    }

    public IPolyvCustomMessageBaseItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IPolyvCustomMessageBaseItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    //处理对应的数据信息，展示内容
    public abstract void processMessage(T data, int pos);

    //初始化view
    public abstract void initView();

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
