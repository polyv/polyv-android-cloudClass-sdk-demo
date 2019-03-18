package com.easefun.polyv.commonui.model;

import com.easefun.polyv.businesssdk.model.PolyvBaseVO;

/**
 * @author df
 * @create 2019/1/17
 * @Describe
 */
//自定义信息类，由于涉及到gson的解析，所以当项目使用混淆时注意要keep住
public class PolyvCustomQBean implements PolyvBaseVO{
    private int contentType;//数字

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }
}
