package com.easefun.polyv.cloudclassdemo.watch.chat.point_reward.effect;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.easefun.polyv.cloudclass.chat.event.PLVRewardEvent;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * date: 2019-12-05
 * author: hwj
 * description: 积分打赏事件队列
 */
public class PolyvPointRewardEffectQueue implements IPolyvPointRewardEventProducer {
    private static final String TAG = PolyvPointRewardEffectQueue.class.getSimpleName();

    //事件队列
    private Queue<PLVRewardEvent> eventQueue = new LinkedList<>();
    //锁
    private ReentrantLock lock = new ReentrantLock();
    private Condition eventQueueEmptyCondition = lock.newCondition();

    private HandlerThread handlerThread;
    private Handler workerThreadHandler;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void addEvent(PLVRewardEvent rewardEvent) {
        lock.lock();
        try {
            eventQueue.offer(rewardEvent);
            eventQueueEmptyCondition.signal();
        } catch (Exception e) {
            PolyvCommonLog.exception(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void fetchEvent(final IPLVOnFetchRewardEventListener onFetchRewardEventListener) {
        workerThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                PLVRewardEvent rewardEvent = null;
                lock.lock();
                try {
                    rewardEvent = eventQueue.poll();
                    while (rewardEvent == null) {
                        eventQueueEmptyCondition.await();
                        rewardEvent = eventQueue.poll();
                        LogUtils.d("eventQueue.size=" + eventQueue.size() + "  poll=" + rewardEvent);
                    }
                    LogUtils.d("从循环中跳出");

                    if (handlerThread.isInterrupted()) {
                        LogUtils.d("线程被中断了，返回");
                        return;
                    }
                    final PLVRewardEvent finalRewardEvent = rewardEvent;
                    LogUtils.d("发送event");
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onFetchRewardEventListener != null) {
                                onFetchRewardEventListener.onFetchSucceed(finalRewardEvent);
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    PolyvCommonLog.i(TAG, workerThreadHandler.toString() + "被中断");
                } catch (Exception e) {
                    PolyvCommonLog.exception(e);
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    @Override
    public void prepare(final OnPreparedListener onPreparedListener) {
        handlerThread = new HandlerThread("PolyvPointRewardEffectQueue-HandlerThread") {
            @Override
            protected void onLooperPrepared() {
                workerThreadHandler = new Handler(handlerThread.getLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onPreparedListener.onPrepared();
                    }
                });
            }
        };
        handlerThread.start();
    }

    @Override
    public void destroy() {
        handlerThread.quit();
        handlerThread.interrupt();
        lock.lock();
        try {
            LogUtils.d("destroy, 清空eventQueue, eventQueue.size=" + eventQueue.size() + " eventQueue.clear");
            eventQueue.clear();
            eventQueueEmptyCondition.signal();
        } catch (Exception e) {
            PolyvCommonLog.exception(e);
        } finally {
            lock.unlock();
        }
    }
}
