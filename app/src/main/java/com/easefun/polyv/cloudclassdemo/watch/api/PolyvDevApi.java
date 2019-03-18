package com.easefun.polyv.cloudclassdemo.watch.api;


import com.easefun.polyv.cloudclassdemo.watch.chat.modle.PolyvClassDetail;
import com.easefun.polyv.foundationsdk.net.PolyvResponseApiBean;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author df
 * @create 2019/2/19
 * @Describe
 */
public interface PolyvDevApi {

    /**
     * 获取点赞数api
     */
    @GET("live/v2/channels/live-likes")
    Observable<ResponseBody> getLikeInfo(@Query("appId") String appId, @Query("timestamp") String timestamp,
                                         @Query("channelIds") String channelIds, @Query("sign") String sign);

    @GET("live/inner/v3/applet/get-class-detail")
    Observable<PolyvResponseApiBean> getClassDetail(@Query("timestamp") String timestamp,
                                                                      @Query("channelId") String channelIds, @Query("sign") String sign);
}
