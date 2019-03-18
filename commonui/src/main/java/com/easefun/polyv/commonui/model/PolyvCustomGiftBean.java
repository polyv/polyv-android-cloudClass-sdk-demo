package com.easefun.polyv.commonui.model;

//自定义信息类，由于涉及到gson的解析，所以当项目使用混淆时注意要keep住
public class PolyvCustomGiftBean {
    public static final String GIFTTYPE_FLOWER = "flower";
    public static final String GIFTTYPE_TEA = "tea";
    public static final String GIFTTYPE_CLAP = "clap";
    private String giftType;
    private String giftName;
    private String giftImgUrl;
    private int giftCount;

    public static String getGiftName(String giftType){
        String giftName = "";
        switch (giftType){
            case GIFTTYPE_FLOWER:
                giftName = "花";
                break;
            case GIFTTYPE_TEA:
                giftName = "茶壶";
                break;
            case GIFTTYPE_CLAP:
                giftName = "掌声";
                break;
        }
        return giftName;
    }

    public PolyvCustomGiftBean(String giftType, String giftName, String giftImgUrl, int giftCount) {
        this.giftType = giftType;
        this.giftName = giftName;
        this.giftImgUrl = giftImgUrl;
        this.giftCount = giftCount;
    }

    public String getGiftType() {
        return giftType;
    }

    public void setGiftType(String giftType) {
        this.giftType = giftType;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getGiftImgUrl() {
        return giftImgUrl;
    }

    public void setGiftImgUrl(String giftImgUrl) {
        this.giftImgUrl = giftImgUrl;
    }

    public int getGiftCount() {
        return giftCount;
    }

    public void setGiftCount(int giftCount) {
        this.giftCount = giftCount;
    }

    @Override
    public String toString() {
        return "PolyvCustomGiftBean{" +
                "giftType='" + giftType + '\'' +
                ", giftName='" + giftName + '\'' +
                ", giftImgUrl='" + giftImgUrl + '\'' +
                ", giftCount=" + giftCount +
                '}';
    }
}