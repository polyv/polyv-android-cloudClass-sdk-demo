package com.easefun.polyv.cloudclassdemo.watch;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvLoginEvent;
import com.easefun.polyv.cloudclass.chat.event.PolyvTeacherInfo;

/**
 * @author df
 * @create 2019/1/18
 * @Describe  demo层的 用户信息管理
 */
public class PolyvDemoClient {
    private static final PolyvDemoClient ourInstance = new PolyvDemoClient();

    public static PolyvDemoClient getInstance() {
        return ourInstance;
    }

    private PolyvDemoClient() {
    }

    private PolyvTeacherInfo teacher;
    private PolyvLoginEvent loginEvent;

    public PolyvTeacherInfo getTeacher() {
        return teacher;
    }

    public void setTeacher(PolyvTeacherInfo teacher) {
        this.teacher = teacher;
    }

    public void onDestory(){
        this.teacher = null;
    }

    public PolyvLoginEvent getLoginEvent() {
        return loginEvent;
    }

    public void setLoginEvent(PolyvLoginEvent loginEvent) {
        this.loginEvent = loginEvent;
    }
}
