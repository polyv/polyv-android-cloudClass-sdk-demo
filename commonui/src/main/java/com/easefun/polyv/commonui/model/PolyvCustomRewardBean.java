package com.easefun.polyv.commonui.model;

import com.easefun.polyv.businesssdk.model.PolyvBaseVO;

/**
 * @author df
 * @create 2019/1/18
 * @Describe 打赏
 */
//自定义信息类，由于涉及到gson的解析，所以当项目使用混淆时注意要keep住
public class PolyvCustomRewardBean implements PolyvBaseVO {
    private String price;//价格
    private String discount;//折扣
    private String unit;//价格单位
    private String acceptPersonName;//接收礼物人

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAcceptPersonName() {
        return acceptPersonName;
    }

    public void setAcceptPersonName(String acceptPersonName) {
        this.acceptPersonName = acceptPersonName;
    }
}
