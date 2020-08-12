package com.easefun.polyv.cloudclassdemo.watch.chat.menu;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.tuwen.PolyvCreateImageText;
import com.easefun.polyv.cloudclass.chat.event.tuwen.PolyvDeleteImageText;
import com.easefun.polyv.cloudclass.chat.event.tuwen.PolyvSetTopImageText;
import com.easefun.polyv.cloudclass.model.PolyvSocketMessageVO;
import com.easefun.polyv.cloudclass.video.PolyvTuWenWebView;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.base.PolyvBaseFragment;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBaseTransformer;
import com.easefun.polyv.foundationsdk.rx.PolyvRxBus;
import com.easefun.polyv.foundationsdk.utils.PolyvGsonUtil;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class PolyvTuWenMenuFragment extends PolyvBaseFragment {
    private PolyvTuWenWebView tuWenWebView;
    private LinearLayout parentView;
    private String channelId;
    private Gson gson;

    @Override
    public int layoutId() {
        return R.layout.polyv_fragment_custommenu;
    }

    @Override
    public void loadDataDelay(boolean isFirst) {
    }

    @Override
    public void loadDataAhead() {
        parentView = findViewById(R.id.ll_parent);
        tuWenWebView = new PolyvTuWenWebView(getContext());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -1);
        tuWenWebView.setLayoutParams(llp);
        parentView.addView(tuWenWebView);

        tuWenWebView.loadWeb();
        channelId = getArguments().getString("channelId");
        disposables.add(Observable.just(1).delay(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        tuWenWebView.callInit(channelId);
                    }
                }));

        disposables.add(PolyvRxBus.get().toObservable(PolyvSocketMessageVO.class)
                .compose(new PolyvRxBaseTransformer<PolyvSocketMessageVO, PolyvSocketMessageVO>())
                .subscribe(new Consumer<PolyvSocketMessageVO>() {
                    @Override
                    public void accept(final PolyvSocketMessageVO polyvSocketMessage) throws Exception {
                        String event = polyvSocketMessage.getEvent();
                        processSocketMessage(polyvSocketMessage, event);
                    }
                }));

        disposables.add(PolyvRxBus.get().toObservable(BUS_EVENT.class)
                .compose(new PolyvRxBaseTransformer<BUS_EVENT, BUS_EVENT>())
                .subscribe(new Consumer<BUS_EVENT>() {
                    @Override
                    public void accept(BUS_EVENT event) throws Exception {
                        if (event.type == BUS_EVENT.TYPE_REFRESH) {
                            tuWenWebView.callRefresh(channelId);
                        }
                    }
                }));
    }

    public void processSocketMessage(PolyvSocketMessageVO polyvSocketMessage, final String event) {
        final String msg = polyvSocketMessage.getMessage();
        if (msg == null || event == null) {
            return;
        }
        if (gson == null) {
            gson = new Gson();
        }
        LogUtils.json(msg);
        switch (event) {
            case PolyvChatManager.EVENT_CREATE_IMAGE_TEXT:
                tuWenWebView.callCreate(gson.toJson(PolyvGsonUtil.fromJson(PolyvCreateImageText.class, msg)));
                break;
            case PolyvChatManager.EVENT_DELETE_IMAGE_TEXT:
                tuWenWebView.callDelete(gson.toJson(PolyvGsonUtil.fromJson(PolyvDeleteImageText.class, msg)));
                break;
            case PolyvChatManager.EVENT_SET_TOP_IMAGE_TEXT:
                tuWenWebView.callSetTop(gson.toJson(PolyvGsonUtil.fromJson(PolyvSetTopImageText.class, msg)));
                break;
            case PolyvChatManager.EVENT_SET_IMAGE_TEXT_MSG:
                tuWenWebView.callUpdate(msg.replaceAll("'", "\\\\u0027"));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tuWenWebView != null) {
            tuWenWebView.removeAllViews();
            tuWenWebView.destroy();
            ((ViewGroup) tuWenWebView.getParent()).removeView(tuWenWebView);
            tuWenWebView = null;
        }
    }

    //RxBus的事件
    public static class BUS_EVENT {
        //刷新图文
        public static final int TYPE_REFRESH = 1;

        int type;

        public BUS_EVENT(int type) {
            this.type = type;
        }
    }
}
