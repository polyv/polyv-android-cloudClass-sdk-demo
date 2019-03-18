package com.easefun.polyv.cloudclassdemo.watch.chat.modle;

import java.util.List;

/**
 * @author df
 * @create 2019/2/19
 * @Describe
 */
public class PolyvClassDetail {

    /**
     * channelId : 0
     * userId :
     * name : text
     * publisher :
     * likes : 5
     * pageView : 2
     * coverImage :
     * status : Y
     * desc :
     * startTime : text
     * stream :
     * roomId : text
     * scene : ppt
     * splashEnabled : N
     * splashImg : text
     * warmUpImg : text
     * warmUpFlv : text
     * playBackEnabled : N
     * sessionId : text
     * hasPlayback : false
     * watchThemeModel : {"pageSkin":"white","defaultTeacherImage":"","watchLayout":""}
     * recordFileSimpleModel : {"fileId":"","channelId":6,"name":"","flv":"text","mp4":"text","m3u8":"","startTime":"text","endTime":"text","fileSize":6,"duration":1,"bitrate":"","width":3,"height":10,"channelSessionId":"text","liveType":"","daysLeft":3}
     * authSettings : [{"qcodeTips":"text","qcodeImg":"","authTips":"text","watchEndTime":0,"customUri":"text","payAuthTips":"","price":10,"channelId":4,"userId":"","trialWatchEnabled":"N","rank":8,"validTimePeriod":4,"trialWatchTime":7,"globalSettingEnabled":"N","codeAuthTips":"text","externalKey":"","authType":"none","infoAuthTips":"","enabled":"N","customKey":"","trialWatchEndTime":10,"externalRedirectUri":"text","authCode":"","externalUri":"text"}]
     * channelMenus : [{"menuId":"text","content":"","ordered":7,"menuType":"chat","name":""},{"menuId":"text","content":"","ordered":9,"menuType":"text","name":"text"},{"menuId":"text","content":"text","ordered":8,"menuType":"iframe","name":""}]
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
    private String roomId;
    private String scene;
    private String splashEnabled;
    private String splashImg;
    private String warmUpImg;
    private String warmUpFlv;
    private String playBackEnabled;
    private String sessionId;
    private boolean hasPlayback;
    private WatchThemeModelBean watchThemeModel;
    private RecordFileSimpleModelBean recordFileSimpleModel;
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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

    public String getPlayBackEnabled() {
        return playBackEnabled;
    }

    public void setPlayBackEnabled(String playBackEnabled) {
        this.playBackEnabled = playBackEnabled;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isHasPlayback() {
        return hasPlayback;
    }

    public void setHasPlayback(boolean hasPlayback) {
        this.hasPlayback = hasPlayback;
    }

    public WatchThemeModelBean getWatchThemeModel() {
        return watchThemeModel;
    }

    public void setWatchThemeModel(WatchThemeModelBean watchThemeModel) {
        this.watchThemeModel = watchThemeModel;
    }

    public RecordFileSimpleModelBean getRecordFileSimpleModel() {
        return recordFileSimpleModel;
    }

    public void setRecordFileSimpleModel(RecordFileSimpleModelBean recordFileSimpleModel) {
        this.recordFileSimpleModel = recordFileSimpleModel;
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

    public static class WatchThemeModelBean {
        /**
         * pageSkin : white
         * defaultTeacherImage :
         * watchLayout :
         */

        private String pageSkin;
        private String defaultTeacherImage;
        private String watchLayout;

        public String getPageSkin() {
            return pageSkin;
        }

        public void setPageSkin(String pageSkin) {
            this.pageSkin = pageSkin;
        }

        public String getDefaultTeacherImage() {
            return defaultTeacherImage;
        }

        public void setDefaultTeacherImage(String defaultTeacherImage) {
            this.defaultTeacherImage = defaultTeacherImage;
        }

        public String getWatchLayout() {
            return watchLayout;
        }

        public void setWatchLayout(String watchLayout) {
            this.watchLayout = watchLayout;
        }
    }

    public static class RecordFileSimpleModelBean {
        /**
         * fileId :
         * channelId : 6
         * name :
         * flv : text
         * mp4 : text
         * m3u8 :
         * startTime : text
         * endTime : text
         * fileSize : 6
         * duration : 1
         * bitrate :
         * width : 3
         * height : 10
         * channelSessionId : text
         * liveType :
         * daysLeft : 3
         */

        private String fileId;
        private int channelId;
        private String name;
        private String flv;
        private String mp4;
        private String m3u8;
        private String startTime;
        private String endTime;
        private int fileSize;
        private int duration;
        private String bitrate;
        private int width;
        private int height;
        private String channelSessionId;
        private String liveType;
        private int daysLeft;

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public int getChannelId() {
            return channelId;
        }

        public void setChannelId(int channelId) {
            this.channelId = channelId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFlv() {
            return flv;
        }

        public void setFlv(String flv) {
            this.flv = flv;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getM3u8() {
            return m3u8;
        }

        public void setM3u8(String m3u8) {
            this.m3u8 = m3u8;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public int getFileSize() {
            return fileSize;
        }

        public void setFileSize(int fileSize) {
            this.fileSize = fileSize;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getBitrate() {
            return bitrate;
        }

        public void setBitrate(String bitrate) {
            this.bitrate = bitrate;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getChannelSessionId() {
            return channelSessionId;
        }

        public void setChannelSessionId(String channelSessionId) {
            this.channelSessionId = channelSessionId;
        }

        public String getLiveType() {
            return liveType;
        }

        public void setLiveType(String liveType) {
            this.liveType = liveType;
        }

        public int getDaysLeft() {
            return daysLeft;
        }

        public void setDaysLeft(int daysLeft) {
            this.daysLeft = daysLeft;
        }
    }

    public static class AuthSettingsBean {
        /**
         * qcodeTips : text
         * qcodeImg :
         * authTips : text
         * watchEndTime : 0
         * customUri : text
         * payAuthTips :
         * price : 10
         * channelId : 4
         * userId :
         * trialWatchEnabled : N
         * rank : 8
         * validTimePeriod : 4
         * trialWatchTime : 7
         * globalSettingEnabled : N
         * codeAuthTips : text
         * externalKey :
         * authType : none
         * infoAuthTips :
         * enabled : N
         * customKey :
         * trialWatchEndTime : 10
         * externalRedirectUri : text
         * authCode :
         * externalUri : text
         */

        private String qcodeTips;
        private String qcodeImg;
        private String authTips;
        private int watchEndTime;
        private String customUri;
        private String payAuthTips;
        private int price;
        private int channelId;
        private String userId;
        private String trialWatchEnabled;
        private int rank;
        private int validTimePeriod;
        private int trialWatchTime;
        private String globalSettingEnabled;
        private String codeAuthTips;
        private String externalKey;
        private String authType;
        private String infoAuthTips;
        private String enabled;
        private String customKey;
        private int trialWatchEndTime;
        private String externalRedirectUri;
        private String authCode;
        private String externalUri;

        public String getQcodeTips() {
            return qcodeTips;
        }

        public void setQcodeTips(String qcodeTips) {
            this.qcodeTips = qcodeTips;
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

        public String getTrialWatchEnabled() {
            return trialWatchEnabled;
        }

        public void setTrialWatchEnabled(String trialWatchEnabled) {
            this.trialWatchEnabled = trialWatchEnabled;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getValidTimePeriod() {
            return validTimePeriod;
        }

        public void setValidTimePeriod(int validTimePeriod) {
            this.validTimePeriod = validTimePeriod;
        }

        public int getTrialWatchTime() {
            return trialWatchTime;
        }

        public void setTrialWatchTime(int trialWatchTime) {
            this.trialWatchTime = trialWatchTime;
        }

        public String getGlobalSettingEnabled() {
            return globalSettingEnabled;
        }

        public void setGlobalSettingEnabled(String globalSettingEnabled) {
            this.globalSettingEnabled = globalSettingEnabled;
        }

        public String getCodeAuthTips() {
            return codeAuthTips;
        }

        public void setCodeAuthTips(String codeAuthTips) {
            this.codeAuthTips = codeAuthTips;
        }

        public String getExternalKey() {
            return externalKey;
        }

        public void setExternalKey(String externalKey) {
            this.externalKey = externalKey;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getInfoAuthTips() {
            return infoAuthTips;
        }

        public void setInfoAuthTips(String infoAuthTips) {
            this.infoAuthTips = infoAuthTips;
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String getCustomKey() {
            return customKey;
        }

        public void setCustomKey(String customKey) {
            this.customKey = customKey;
        }

        public int getTrialWatchEndTime() {
            return trialWatchEndTime;
        }

        public void setTrialWatchEndTime(int trialWatchEndTime) {
            this.trialWatchEndTime = trialWatchEndTime;
        }

        public String getExternalRedirectUri() {
            return externalRedirectUri;
        }

        public void setExternalRedirectUri(String externalRedirectUri) {
            this.externalRedirectUri = externalRedirectUri;
        }

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public String getExternalUri() {
            return externalUri;
        }

        public void setExternalUri(String externalUri) {
            this.externalUri = externalUri;
        }
    }

    public static class ChannelMenusBean {
        /**
         * menuId : text
         * content :
         * ordered : 7
         * menuType : chat
         * name :
         */

        private String menuId;
        private String content;
        private int ordered;
        private String menuType;
        private String name;

        public String getMenuId() {
            return menuId;
        }

        public void setMenuId(String menuId) {
            this.menuId = menuId;
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
    }
}
