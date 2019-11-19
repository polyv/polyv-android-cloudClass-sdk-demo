package com.easefun.polyv.commonui.utils;

import com.jakewharton.rxrelay2.Relay;
import com.jakewharton.rxrelay2.ReplayRelay;

import io.reactivex.Observable;

/**
 * 发送聊天室状态请用{@link PolyvSingleRelayBus}
 * 发送聊天消息请用{@link com.easefun.polyv.foundationsdk.rx.PolyvRxBus}
 */
@Deprecated
public class PolyvChatEventBus {
    //xxxRelay的xxx等同于xxxSuject/xxxProcessor
    private final Relay<Object> mBus;
    private static PolyvChatEventBus rxBusReplay;

    private PolyvChatEventBus() {
        // toSerialized method made bus thread safe
        mBus = ReplayRelay.create().toSerialized();
    }

    public static PolyvChatEventBus get() {
        if (rxBusReplay == null) {
            synchronized (PolyvChatEventBus.class) {
                if (rxBusReplay == null)
                    rxBusReplay = new PolyvChatEventBus();
            }
        }
        return rxBusReplay;
    }

    public static void clear() {
        synchronized (PolyvChatEventBus.class) {
            rxBusReplay = null;
        }
    }

    public void post(Object obj) {
        mBus.accept(obj);
    }

    public <T> Observable<T> toObservable(Class<T> tClass) {
        return mBus.ofType(tClass);
    }

    public Observable<Object> toObservable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }
}
