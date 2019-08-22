package com.easefun.polyv.cloudclassdemo.watch.player.live.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;

/**
 * @author df
 * @create 2019/7/26
 * @Describe
 */
public class PolyvChatPullLayout extends LinearLayout {

    private ChatPullLayoutCallback chatPullLayoutCallback;
    GestureDetector mDetector;
    protected static final float FLIP_DISTANCE = 30;
    private LinearLayout chatTopPullLayout;
    private ImageView pullChat;

    public PolyvChatPullLayout(Context context) {
        this(context, null);
    }

    public PolyvChatPullLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PolyvChatPullLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.chat_pull_layout, this);
        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向上滑...");
                    if (chatPullLayoutCallback != null) {
                        chatPullLayoutCallback.pullUp();
                    }


                    return true;
                }
                if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向下滑...");
                    chatPullLayoutCallback.pullDown();
                    return true;
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getY() - e2.getY() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向上滑...");
                    if (chatPullLayoutCallback != null) {
                        chatPullLayoutCallback.pullUp();
                    }


                    return true;
                }
                if (e2.getY() - e1.getY() > FLIP_DISTANCE) {
                    Log.i("MYTAG", "向下滑...");
                    chatPullLayoutCallback.pullDown();
                    return true;
                }

                return false;
            }
        });
        chatTopPullLayout = (LinearLayout) findViewById(R.id.chat_top_pull_layout);
        pullChat = (ImageView) findViewById(R.id.pull_chat);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PolyvCommonLog.d("touch","ontounchevent"+event.getAction());
        mDetector.onTouchEvent(event);
        return true;
    }

    public ChatPullLayoutCallback getChatPullLayoutCallback() {
        return chatPullLayoutCallback;
    }

    public void setChatPullLayoutCallback(ChatPullLayoutCallback chatPullLayoutCallback) {
        this.chatPullLayoutCallback = chatPullLayoutCallback;
    }

    public interface ChatPullLayoutCallback {
        void pullUp();

        void pullDown();
    }
}
