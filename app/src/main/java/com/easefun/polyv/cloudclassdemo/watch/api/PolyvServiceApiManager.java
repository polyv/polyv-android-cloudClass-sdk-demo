package com.easefun.polyv.cloudclassdemo.watch.api;

import com.easefun.polyv.businesssdk.PolyvSDKClient;
import com.easefun.polyv.businesssdk.net.PolyvCommonApiConstant;
import com.easefun.polyv.businesssdk.net.api.PolyvVodApi;
import com.easefun.polyv.foundationsdk.net.PolyvRetrofitHelper;

import okhttp3.OkHttpClient;

/**
 * @author df
 * @create 2019/2/19
 * @Describe
 */
public class PolyvServiceApiManager {
    private static OkHttpClient createOkHttpClient() {
        return PolyvRetrofitHelper.userAgentOkHttpClient(PolyvSDKClient.getUserAgent());
    }

    public static PolyvDevApi getPolyvDevApi(){
        return PolyvRetrofitHelper.createApi(PolyvDevApi.class,
                PolyvCommonApiConstant.API_POLYV_NET,createOkHttpClient());
    }
}
