package com.easefun.polyv.commonui.modle.db;

/**
 * @author df
 * @create 2019/8/19
 * @Describe
 */
public class PolyvRxOption<T> {
    public PolyvRxOption(T data) {
        this.data = data;
    }

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
