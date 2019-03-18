package com.easefun.polyv.cloudclassdemo.watch.request;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclassdemo.utils.HttpExceptionConverter;
import com.easefun.polyv.cloudclassdemo.utils.PolyvSignCreator;
import com.easefun.polyv.cloudclassdemo.watch.api.PolyvServiceApiManager;
import com.easefun.polyv.cloudclassdemo.watch.chat.modle.PolyvClassDetail;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;

/**
 * @date: 2019/3/11 0011
 * @author: hwj
 * @description PolyvClassDetail 实体的请求封装类
 */
public class PolyvChannelDetailRequest {
    /**
     * 发起请求
     *
     * @param failCallback 请求错误的回调
     * @return LiveData
     */
    public LiveData<PolyvClassDetail> get(String channelId, @NonNull Action failCallback) {
        MutableLiveData<PolyvClassDetail> liveData = new MutableLiveData<>();

        long ts = System.currentTimeMillis();
        Map<String, String> signMap = new HashMap<>();
        signMap.put("channelId", channelId);
        signMap.put("timestamp", Long.toString(ts));
        signMap.put("appId", PolyvLiveSDKClient.getInstance().getAppId());
        String sign = PolyvSignCreator.createSign(PolyvLiveSDKClient.getInstance().getAppSecret(), signMap);
        PolyvResponseExcutor.excuteDataBean(
                PolyvServiceApiManager.getPolyvDevApi().getClassDetail(Integer.parseInt(channelId), ts, sign, PolyvLiveSDKClient.getInstance().getAppId()),
                PolyvClassDetail.class,
                new PolyvrResponseCallback<PolyvClassDetail>() {
                    @Override
                    public void onSuccess(PolyvClassDetail data) {
                        if (data != null) {
                            liveData.postValue(data);
                        } else {
                            fail(failCallback);
                        }
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvClassDetail> responseBean) {
                        super.onFailure(responseBean);
                        fail(failCallback);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        LogUtils.e(HttpExceptionConverter.convert(e));
                        fail(failCallback);
                    }
                });
        return liveData;
    }

    //请求失败
    private void fail(Action failCallback) {
        LogUtils.d("请求失败");
        try {
            failCallback.run();
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }
}
