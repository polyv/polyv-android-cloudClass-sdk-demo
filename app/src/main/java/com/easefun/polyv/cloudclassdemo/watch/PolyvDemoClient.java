package com.easefun.polyv.cloudclassdemo.watch;

import com.easefun.polyv.businesssdk.model.link.PolyvJoinInfoEvent;

/**
 * @author df
 * @create 2019/1/18
 * @Describe
 */
public class PolyvDemoClient {
    private static final PolyvDemoClient ourInstance = new PolyvDemoClient();

    public static PolyvDemoClient getInstance() {
        return ourInstance;
    }

    private PolyvDemoClient() {
    }

    private PolyvJoinInfoEvent teacher;

    public PolyvJoinInfoEvent getTeacher() {
        return teacher;
    }

    public void setTeacher(PolyvJoinInfoEvent teacher) {
        this.teacher = teacher;
    }

    public void onDestory(){
        this.teacher = null;
    }
}
