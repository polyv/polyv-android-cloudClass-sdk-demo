package com.easefun.polyv.commonui.utils;

import com.jakewharton.rxrelay2.Relay;
import com.jakewharton.rxrelay2.ReplayRelay;

import io.reactivex.Observable;

/**
 * date: 2019/11/12 0012
 *
 * @author hwj
 * description 单次使用的relay bus
 */
public class PolyvSingleRelayBus {
    // <editor-fold defaultstate="collapsed" desc="单例">
    private volatile static PolyvSingleRelayBus INSTANCE = null;

    private PolyvSingleRelayBus() {
        init();
    }

    public static PolyvSingleRelayBus get() {
        if (INSTANCE == null) {
            synchronized (PolyvSingleRelayBus.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PolyvSingleRelayBus();
                }
            }
        }
        return INSTANCE;
    }

    public static void clear() {
        synchronized (PolyvSingleRelayBus.class) {
            INSTANCE = null;
        }
    }
// </editor-fold>

    private void init() {
        relay = ReplayRelay.createWithSize(1).toSerialized();
    }

    private Relay<Object> relay;

    public void post(Object object) {
        relay.accept(object);
    }

    public <T> Observable<T> toObservable(Class<T> tClass) {
        return relay.ofType(tClass);
    }
}
