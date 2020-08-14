package com.easefun.polyv.cloudclassdemo.watch;

import com.easefun.polyv.businesssdk.PolyvChatDomainManager;
import com.easefun.polyv.businesssdk.model.chat.PolyvChatDomain;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodSDKClient;
import com.easefun.polyv.cloudclass.chat.PolyvChatApiRequestHelper;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclass.model.PolyvLiveClassDetailVO;
import com.easefun.polyv.cloudclass.model.PolyvLiveStatusVO;
import com.easefun.polyv.cloudclass.net.PolyvApiManager;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvResponseExcutor;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * 直播登录管理器，用于根据当前直播状态进入直播页面/回放页面的场景
 */
public class PolyvLiveLoginManager {
    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private Disposable liveDetailDisposable;
    private Disposable checkTokenDisposable;
    private Disposable liveStatusDisposable;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="单例">
    private static volatile PolyvLiveLoginManager singleton = null;

    private PolyvLiveLoginManager() {
    }

    public static PolyvLiveLoginManager getInstance() {
        if (singleton == null) {
            synchronized (PolyvLiveLoginManager.class) {
                if (singleton == null) {
                    singleton = new PolyvLiveLoginManager();
                }
            }
        }
        return singleton;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录">

    /**
     * 登录直播间
     *
     * @param channelId 频道号
     * @param userId    直播账号userId
     * @param appId     直播账号appId
     * @param appSecret 直播账号appSecret
     */
    public void loginLiveRoom(final String channelId, final String userId, final String appId, final String appSecret, final OnRequestListener listener) {
        //先取消请求
        if (checkTokenDisposable != null) {
            checkTokenDisposable.dispose();
        }
        if (liveDetailDisposable != null) {
            liveDetailDisposable.dispose();
        }
        if (liveStatusDisposable != null) {
            liveStatusDisposable.dispose();
        }
        //直播详情接口回调
        final Consumer<PolyvLiveClassDetailVO> liveDetailNextConsumer = new Consumer<PolyvLiveClassDetailVO>() {
            @Override
            public void accept(PolyvLiveClassDetailVO liveClassDetailVO) throws Exception {
                boolean isPlaybackStatus = liveClassDetailVO.getData().isPlaybackStatus();
                final String rtcType = liveClassDetailVO.getData().getRtcType();
                if (isPlaybackStatus) {
                    if (listener != null) {
                        listener.onPlaybackCallback(channelId, userId, rtcType);
                    }
                } else {
                    //直播状态接口回调
                    PolyvrResponseCallback<PolyvLiveStatusVO> responseCallback = new PolyvrResponseCallback<PolyvLiveStatusVO>() {
                        @Override
                        public void onSuccess(PolyvLiveStatusVO polyvLiveStatusVO) {
                            boolean isAlone = polyvLiveStatusVO.isAlone();//是否有ppt
                            if (listener != null) {
                                listener.onLiveCallback(channelId, userId, rtcType, isAlone);
                            }
                        }

                        @Override
                        public void onFailure(PolyvResponseBean<PolyvLiveStatusVO> responseBean) {
                            super.onFailure(responseBean);
                            if (listener != null) {
                                listener.onFailed(responseBean.getMessage(), new Exception(responseBean.getMessage()));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);
                            if (listener != null) {
                                listener.onFailed(getErrorMsg(e), e);
                            }
                        }
                    };
                    requestLiveStatus(channelId, responseCallback);
                }
            }
        };
        final Consumer<Throwable> liveDetailErrorConsumer = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (listener != null) {
                    listener.onFailed(getErrorMsg(throwable), throwable);
                }
            }
        };
        //checkToken接口回调
        PolyvrResponseCallback<PolyvChatDomain> responseCallback = new PolyvrResponseCallback<PolyvChatDomain>() {
            @Override
            public void onSuccess(PolyvChatDomain responseBean) {
                PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecret);
                PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecret);
                PolyvVodSDKClient.getInstance().initConfig(appId, appSecret);
                PolyvChatDomainManager.getInstance().setChatDomain(responseBean);

                requestLiveDetail(channelId, liveDetailNextConsumer, liveDetailErrorConsumer);
            }

            @Override
            public void onFailure(PolyvResponseBean<PolyvChatDomain> responseBean) {
                super.onFailure(responseBean);
                if (listener != null) {
                    listener.onFailed(responseBean.getMessage(), new Exception(responseBean.getMessage()));
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                if (listener != null) {
                    listener.onFailed(getErrorMsg(e), e);
                }
            }
        };
        requestCheckToken(userId, appSecret, channelId, null, appId, responseCallback);
    }

    private void requestCheckToken(final String userId, final String appSecret, String channel, final String vid, final String appId, PolyvrResponseCallback<PolyvChatDomain> responseCallback) {
        if (checkTokenDisposable != null) {
            checkTokenDisposable.dispose();
        }
        checkTokenDisposable = PolyvLoginManager.checkLoginToken(userId, appSecret, appId, channel, vid, responseCallback);
    }

    private void requestLiveDetail(String channelId, final Consumer<PolyvLiveClassDetailVO> onNext, final Consumer<Throwable> onError) {
        if (liveDetailDisposable != null) {
            liveDetailDisposable.dispose();
        }
        liveDetailDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvChatApiRequestHelper.getInstance()
                .requestLiveClassDetailApi(channelId), new PolyvrResponseCallback<PolyvLiveClassDetailVO>() {
            @Override
            public void onSuccess(PolyvLiveClassDetailVO polyvLiveClassDetailVO) {
                try {
                    onNext.accept(polyvLiveClassDetailVO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                try {
                    onError.accept(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void requestLiveStatus(String channelId, PolyvrResponseCallback<PolyvLiveStatusVO> responseCallback) {
        if (liveStatusDisposable != null) {
            liveStatusDisposable.dispose();
        }
        liveStatusDisposable = PolyvResponseExcutor.excuteUndefinData(PolyvApiManager.getPolyvLiveStatusApi().geLiveStatusJson(channelId), responseCallback);
    }

    private String getErrorMsg(Throwable e) {
        String errorMsg = e.getMessage();
        if (e instanceof HttpException) {
            try {
                errorMsg = ((HttpException) e).response().errorBody().string();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return errorMsg;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="销毁">
    public void destroy() {
        if (checkTokenDisposable != null) {
            checkTokenDisposable.dispose();
            checkTokenDisposable = null;
        }
        if (liveDetailDisposable != null) {
            liveDetailDisposable.dispose();
            liveDetailDisposable = null;
        }
        if (liveStatusDisposable != null) {
            liveStatusDisposable.dispose();
            liveStatusDisposable = null;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="请求监听器">
    public interface OnRequestListener {
        /**
         * 请求成功，直播回调
         *
         * @param channelId    频道号
         * @param userId       直播账号userId
         * @param rtcType      rtc类型
         * @param isNormalLive 是否是普通直播(非三分屏直播)
         */
        void onLiveCallback(String channelId, String userId, String rtcType, boolean isNormalLive);

        /**
         * 请求成功，回放回调
         *
         * @param channelId 频道号
         * @param userId    直播账号userId
         * @param rtcType   rct类型
         */
        void onPlaybackCallback(String channelId, String userId, String rtcType);

        /**
         * 请求失败
         */
        void onFailed(String errorMsg, Throwable throwable);
    }
    // </editor-fold>
}
