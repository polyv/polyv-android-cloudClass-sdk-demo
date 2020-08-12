package com.easefun.polyv.cloudclassdemo.watch.playback_cache.playback_list;

import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclass.model.playback.PolyvPlaybackListVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.cloudclassdemo.watch.playback_cache.cache_storage.PolyvPlaybackCacheDBManager;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.EncryptUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * date: 2019/8/13 0013
 *
 * @author hwj
 * description 回放列表数据仓库
 */
public class PolyvPlaybackListRepo implements IPolyvPlaybackListRepo {
    private Disposable getPlaybackListDisposable;
    private Disposable isVideoLiveIdExistDisposable;


    @Override
    public void getPlaybackList(String channelId, int pageNum, int pageSize, Consumer<PolyvPlaybackListVO> successCallback, Consumer<Throwable> errorCallback) {
        if (getPlaybackListDisposable != null) {
            getPlaybackListDisposable.dispose();
        }
        String appSecret = PolyvLiveSDKClient.getInstance().getAppSecret();
        String appId = PolyvLiveSDKClient.getInstance().getAppId();
        String timestamp = String.valueOf(System.currentTimeMillis());

        //生成sign
        Map<String, String> signTargetMap = new HashMap<>();
        signTargetMap.put("appId", appId);
        signTargetMap.put("timestamp", timestamp);
        signTargetMap.put("channelId", channelId);
        signTargetMap.put("page", String.valueOf(pageNum));
        signTargetMap.put("pageSize", String.valueOf(pageSize));
        String sign = createSign(appSecret, signTargetMap);

        getPlaybackListDisposable = PolyvResponseExcutor.excuteDataBean(PolyvApiManager.getPolyvLiveStatusApi()
                        .getPlaybackList(appId, timestamp, channelId, String.valueOf(pageNum), String.valueOf(pageSize), sign),
                PolyvPlaybackListVO.class, new PolyvrResponseCallback<PolyvPlaybackListVO>() {
                    @Override
                    public void onSuccess(PolyvPlaybackListVO polyvPlaybackListVO) {
                        notifySuccess(successCallback, polyvPlaybackListVO);
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvPlaybackListVO> responseBean) {
                        super.onFailure(responseBean);
                        notifyError(errorCallback, new Throwable(responseBean.getMessage()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        notifyError(errorCallback, e);
                    }
                });
    }

    @Override
    public void isVideoLiveIdExitInDB(String videoPoolId,Consumer<List<PolyvPlaybackCacheDBEntity>> callback) {
        if (isVideoLiveIdExistDisposable != null) {
            isVideoLiveIdExistDisposable.dispose();
        }
        isVideoLiveIdExistDisposable=PolyvPlaybackCacheDBManager.getDB()
                .asyncWithResult(iPolyvPlaybackCacheDB -> iPolyvPlaybackCacheDB.getByVideoPoolId(videoPoolId), callback);
    }


    private <T> void notifySuccess(Consumer<T> success, T t) {
        try {
            success.accept(t);
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    private void notifyError(Consumer<Throwable> error, Throwable throwable) {
        try {
            if (throwable instanceof HttpException) {
                throwable = new Throwable(((HttpException) throwable).response().errorBody().string());
            }
            error.accept(throwable);
            LogUtils.e(throwable);
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }


    @Override
    public void destroy() {
        if (getPlaybackListDisposable != null) {
            getPlaybackListDisposable.dispose();
        }
    }

    private String createSign(String appSecret, Map<String, String> signTargetMap) {
        String signResult = "";

        Map<String, String> paramMap = signTargetMap;
        String[] keyArray = paramMap.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);

        StringBuilder builder = new StringBuilder();
        builder.append(appSecret);
        for (String key : keyArray) {
            builder.append(key).append(paramMap.get(key));
        }
        builder.append(appSecret);

        String signSource = builder.toString();
        signResult = EncryptUtils.encryptMD5ToString(signSource).toUpperCase();
        return signResult;
    }

}
