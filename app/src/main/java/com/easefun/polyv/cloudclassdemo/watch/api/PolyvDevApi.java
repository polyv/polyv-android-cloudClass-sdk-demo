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

    /**
     * 获取
     * @param channelId
     * @param timestamp
     * @param sign
     * @param appId
     * @return
     */
    @GET("/live/v3/applet/sdk/get-channel-detail")
    Observable<PolyvResponseApiBean> getClassDetail(@Query("channelId") int channelId,
                                                    @Query("timestamp") long timestamp,
                                                    @Query("sign") String sign,
                                                    @Query("appId") String appId
    );
}
