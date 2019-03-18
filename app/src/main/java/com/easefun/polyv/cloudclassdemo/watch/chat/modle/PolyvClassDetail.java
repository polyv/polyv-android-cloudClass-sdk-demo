package com.easefun.polyv.cloudclassdemo.watch.chat.modle;

import java.util.List;

/**
 * @author df
 * @create 2019/2/19
 * @Describe
 */
public class PolyvClassDetail {

    /**
     * channelId : 1
     * userId :
     * name : 摄影教学
     * publisher : 主持人
     * likes : 140000
     * pageView : 10400
     * coverImage : http://livestatic.videocc.net/uploaded/images/2018/05/f18prsbs3t.jpg
     * status : N
     * desc : text
     * startTime :
     * stream : text
     * splashEnabled : Y
     * splashImg : text
     * warmUpImg :
     * warmUpFlv : text
     * authSettings : [{"qcodeTips":"text","authType":"none","qcodeImg":"","authTips":"text","externalRedirectUri":"","watchEndTime":4,"customUri":"","payAuthTips":"","price":5,"channelId":9,"userId":"text","rank":4,"customKey":"text","validTimePeriod":3,"externalUri":"","globalSettingEnabled":"Y","authCode":"","externalKey":"","enabled":"N"},{"qcodeTips":"","authType":"none","qcodeImg":"text","authTips":"","externalRedirectUri":"","watchEndTime":4,"customUri":"","payAuthTips":"text","price":8,"channelId":5,"userId":"text","rank":0,"customKey":"text","validTimePeriod":5,"externalUri":"text","globalSettingEnabled":"Y","authCode":"text","externalKey":"text","enabled":"N"},{"qcodeTips":"text","authType":"custom","qcodeImg":"text","authTips":"","externalRedirectUri":"text","watchEndTime":8,"customUri":"text","payAuthTips":"text","price":3,"channelId":7,"userId":"","rank":2,"customKey":"","validTimePeriod":6,"externalUri":"","globalSettingEnabled":"Y","authCode":"","externalKey":"text","enabled":"N"}]
     * channelMenus : [{"menuId":"","menuType":"chat","name":"聊天室","content":"聊天室-- 内容","ordered":2},{"menuId":"","menuType":"quiz","name":"咨询提问","content":"咨询提问-- 内容","ordered":3},{"menuId":"","menuType":"desc","name":"课程介绍","content":"<h1><font size=\"3\">春风十里，不及入门前端的你<\/font><\/h1><h3><font color=\"#008080\">认识你，从揭开你的神秘面纱开始<\/font><\/h3><p>Web前端开发是从网页制作演变而来，其中包括三个要素：HTML、CSS和JavaScript；<br>Web前端用人数量已经远远超过主流编程语言的开发人员的数量。据统计，我国对于Web前端工程师人员的缺口将达到12万。在2017年的岗位全年共招聘136848人，平均每月招聘人数需求11412人<\/p><h3><font color=\"#008080\">熟悉你，从了解你的方方面面入手<\/font><\/h3><p>实现Web端界面和移动端的界面；<br>比如，网站设计、网页界面开发、前台数据的获取和绑定、以及动态特效等等<\/p><h3><font color=\"#008080\">选择你，并着手规划与你的未来<\/font><\/h3><p>据职友集数据显示，2017年Web前端岗位月薪最高达到2-3万元，月平均薪资8190元，预计在2018年待遇将再一次水涨船高<\/p><p><u><font color=\"#880000\">职位参考：\n<\/font><\/u><\/p><p>Web前端工程师<\/p><p>前端架构师<\/p><p>网站重构工程师<\/p><p>网页制作工程师<\/p><p>H5开发工程师<\/p><p>移动端开发工程师<\/p><p><br><\/p>","ordered":4},{"menuId":"","menuType":"iframe","name":"推广外链","content":"https://www.baidu.com/","ordered":5},{"menuId":"","menuType":"text","name":"图文菜单1","content":"图文菜单1 -- 内容","ordered":6},{"menuId":"","menuType":"text","name":"图文菜单2","content":"图文菜单2 -- 内容","ordered":7},{"menuId":"","menuType":"text","name":"图文菜单3","content":"图文菜单3 -- 内容","ordered":8}]
     */

    private int channelId;
    private String userId;
    private String name;
    private String publisher;
    private int likes;
    private int pageView;
    private String coverImage;
    private String status;
    private String desc;
    private String startTime;
    private String stream;
    private String splashEnabled;
    private String splashImg;
    private String warmUpImg;
    private String warmUpFlv;
    private List<AuthSettingsBean> authSettings;
    private List<ChannelMenusBean> channelMenus;

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getPageView() {
        return pageView;
    }

    public void setPageView(int pageView) {
        this.pageView = pageView;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getSplashEnabled() {
        return splashEnabled;
    }

    public void setSplashEnabled(String splashEnabled) {
        this.splashEnabled = splashEnabled;
    }

    public String getSplashImg() {
        return splashImg;
    }

    public void setSplashImg(String splashImg) {
        this.splashImg = splashImg;
    }

    public String getWarmUpImg() {
        return warmUpImg;
    }

    public void setWarmUpImg(String warmUpImg) {
        this.warmUpImg = warmUpImg;
    }

    public String getWarmUpFlv() {
        return warmUpFlv;
    }

    public void setWarmUpFlv(String warmUpFlv) {
        this.warmUpFlv = warmUpFlv;
    }

    public List<AuthSettingsBean> getAuthSettings() {
        return authSettings;
    }

    public void setAuthSettings(List<AuthSettingsBean> authSettings) {
        this.authSettings = authSettings;
    }

    public List<ChannelMenusBean> getChannelMenus() {
        return channelMenus;
    }

    public void setChannelMenus(List<ChannelMenusBean> channelMenus) {
        this.channelMenus = channelMenus;
    }

    public static class AuthSettingsBean {
        /**
         * qcodeTips : text
         * authType : none
         * qcodeImg :
         * authTips : text
         * externalRedirectUri :
         * watchEndTime : 4
         * customUri :
         * payAuthTips :
         * price : 5
         * channelId : 9
         * userId : text
         * rank : 4
         * customKey : text
         * validTimePeriod : 3
         * externalUri :
         * globalSettingEnabled : Y
         * authCode :
         * externalKey :
         * enabled : N
         */

        private String qcodeTips;
        private String authType;
        private String qcodeImg;
        private String authTips;
        private String externalRedirectUri;
        private int watchEndTime;
        private String customUri;
        private String payAuthTips;
        private int price;
        private int channelId;
        private String userId;
        private int rank;
        private String customKey;
        private int validTimePeriod;
        private String externalUri;
        private String globalSettingEnabled;
        private String authCode;
        private String externalKey;
        private String enabled;

        public String getQcodeTips() {
            return qcodeTips;
        }

        public void setQcodeTips(String qcodeTips) {
            this.qcodeTips = qcodeTips;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getQcodeImg() {
            return qcodeImg;
        }

        public void setQcodeImg(String qcodeImg) {
            this.qcodeImg = qcodeImg;
        }

        public String getAuthTips() {
            return authTips;
        }

        public void setAuthTips(String authTips) {
            this.authTips = authTips;
        }

        public String getExternalRedirectUri() {
            return externalRedirectUri;
        }

        public void setExternalRedirectUri(String externalRedirectUri) {
            this.externalRedirectUri = externalRedirectUri;
        }

        public int getWatchEndTime() {
            return watchEndTime;
        }

        public void setWatchEndTime(int watchEndTime) {
            this.watchEndTime = watchEndTime;
        }

        public String getCustomUri() {
            return customUri;
        }

        public void setCustomUri(String customUri) {
            this.customUri = customUri;
        }

        public String getPayAuthTips() {
            return payAuthTips;
        }

        public void setPayAuthTips(String payAuthTips) {
            this.payAuthTips = payAuthTips;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getChannelId() {
            return channelId;
        }

        public void setChannelId(int channelId) {
            this.channelId = channelId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getCustomKey() {
            return customKey;
        }

        public void setCustomKey(String customKey) {
            this.customKey = customKey;
        }

        public int getValidTimePeriod() {
            return validTimePeriod;
        }

        public void setValidTimePeriod(int validTimePeriod) {
            this.validTimePeriod = validTimePeriod;
        }

        public String getExternalUri() {
            return externalUri;
        }

        public void setExternalUri(String externalUri) {
            this.externalUri = externalUri;
        }

        public String getGlobalSettingEnabled() {
            return globalSettingEnabled;
        }

        public void setGlobalSettingEnabled(String globalSettingEnabled) {
            this.globalSettingEnabled = globalSettingEnabled;
        }

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public String getExternalKey() {
            return externalKey;
        }

        public void setExternalKey(String externalKey) {
            this.externalKey = externalKey;
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }
    }

    public static class ChannelMenusBean {
        /**
         * menuId :
         * menuType : chat
         * name : 聊天室
         * content : 聊天室-- 内容
         * ordered : 2
         */

        private String menuId;
        private String menuType;
        private String name;
        private String content;
        private int ordered;

        public String getMenuId() {
            return menuId;
        }

        public void setMenuId(String menuId) {
            this.menuId = menuId;
        }

        public String getMenuType() {
            return menuType;
        }

        public void setMenuType(String menuType) {
            this.menuType = menuType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getOrdered() {
            return ordered;
        }

        public void setOrdered(int ordered) {
            this.ordered = ordered;
        }
    }
}
