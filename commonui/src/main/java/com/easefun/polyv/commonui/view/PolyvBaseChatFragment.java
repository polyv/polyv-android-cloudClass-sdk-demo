package com.easefun.polyv.commonui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.easefun.polyv.businesssdk.sub.gif.GifImageSpan;
import com.easefun.polyv.businesssdk.sub.gif.RelativeImageSpan;
import com.easefun.polyv.cloudclass.chat.PolyvChatManager;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.adapter.PolyvEmoListAdapter;
import com.easefun.polyv.commonui.base.PolyvBaseFragment;
import com.easefun.polyv.commonui.presenter.IPolyvHomePresnter;
import com.easefun.polyv.commonui.utils.PolyvFaceManager;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.commonui.widget.PolyvChatRecyclerView;
import com.easefun.polyv.commonui.widget.PolyvImageViewer;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

public abstract class PolyvBaseChatFragment extends PolyvBaseFragment {
    protected PolyvChatManager chatManager;
    //问答列表
    protected PolyvChatRecyclerView chatMessageList;
    protected PolyvChatListAdapter chatListAdapter;
    //所有的item，只看讲师时不是取这个数据
    protected List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems = new ArrayList<>();
    //信息编辑布局
    protected LinearLayout talkLayout;
    protected FrameLayout talkParentLayout;
    //信息编辑框
    protected EditText talk;
    //表情
    protected ImageView emoji;
    protected boolean isShowEmoji;
    //表情
    protected RelativeLayout emoListLayout;
    protected RecyclerView emoList;
    protected PolyvEmoListAdapter emoListAdapter;
    //信息删除按钮
    protected ImageView ivMsgDelete;
    protected TextView tvSendMsg;
    // 表情的文本长度
    private int emoLength;
    //查看更多信息
    protected TextView unread;
    protected IPolyvHomePresnter homePresnter;
    protected PolyvImageViewer imageViewer;
    protected boolean isKeyboardVisible;
    protected int keyboardHeight;

    @Override
    public void loadDataAhead() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IPolyvHomePresnter) {
            this.homePresnter = (IPolyvHomePresnter) context;
        }
    }

    protected void sendDanmu(CharSequence content) {
        if (homePresnter != null) {
            homePresnter.sendDanmu(content);
        }
    }

    protected ViewGroup getImageViewerContainer() {
        if (homePresnter != null) {
            return homePresnter.getImageViewerContainer();
        }
        return null;
    }

    protected ViewGroup getChatEditContainer() {
        if (homePresnter != null) {
            return homePresnter.getChatEditContainer();
        }
        return null;
    }

    public PolyvBaseChatFragment setChatManager(PolyvChatManager chatManager) {
        this.chatManager = chatManager;
        return this;
    }

    @Override
    public void loadDataDelay(boolean isFirst) {
        final ViewGroup chatEditLayout = getChatEditContainer();
        if (chatEditLayout != null) {
            chatEditLayout.setOnTouchListener(new View.OnTouchListener() {//切换fragment时重新设置
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && getUserVisibleHint()) {
                        if (unread.getVisibility() == View.VISIBLE) {//判断是否点击了查看更多信息
                            float rx = event.getRawX();
                            float ry = event.getRawY();
                            int[] location = new int[2];
                            unread.getLocationOnScreen(location);
                            if (rx >= location[0] && rx <= location[0] + unread.getWidth() &&
                                    ry >= location[1] && ry <= location[1] + unread.getHeight()) {
                                unread.performClick();
                            } else {
                                hideSoftInputAndEmoList();
                            }
                        } else {
                            hideSoftInputAndEmoList();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    protected void initCommonView() {
        //问答列表
        chatMessageList = findViewById(R.id.chat_message_list);
        chatMessageList.initRecyclerView(chatMessageList, getContext());
        chatListAdapter = new PolyvChatListAdapter(chatMessageList, chatTypeItems);
        chatListAdapter.setOnChatImgViewClickListener(new PolyvChatListAdapter.OnChatImgViewClickListener() {
            @Override
            public void onClick(ImageView iv, int position) {
                ViewGroup vp = getImageViewerContainer();
                if (vp != null) {
                    if (imageViewer == null) {
                        imageViewer = new PolyvImageViewer(getContext());
                        imageViewer.setPermissionManager(permissionManager);
                    }
                    List<PolyvChatListAdapter.ChatTypeItem> imgItemList = new ArrayList<>();
                    for (PolyvChatListAdapter.ChatTypeItem chatTypeItem : chatListAdapter.getChatTypeItems()) {
                        if (chatTypeItem.object instanceof PolyvChatImgEvent
                                || chatTypeItem.object instanceof PolyvChatImgHistory) {
                            imgItemList.add(chatTypeItem);
                        }
                    }
                    for (int i = 0; i < imgItemList.size(); i++) {
                        if (imgItemList.get(i) == chatListAdapter.getChatTypeItems().get(position)) {
                            position = i;
                            break;
                        }
                    }
                    imageViewer.setData(imgItemList, position);
                    imageViewer.attachRootView(vp);
                }
            }
        });
        chatMessageList.setAdapter(chatListAdapter);
        chatMessageList.addOnScrollListener();
        //发送布局
        talkLayout = findViewById(R.id.ll_bottom);
        talkParentLayout = findViewById(R.id.fl_bottom);
        final ViewGroup chatEditLayout = getChatEditContainer();
        //监听布局变化
        if (chatEditLayout != null) {
            chatEditLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (!getUserVisibleHint())
                        return;
                    if (bottom > 0 && oldBottom > 0 && right == oldRight) {
                        if ((keyboardHeight = Math.abs(bottom - oldBottom)) > PolyvScreenUtils.getNormalWH(getActivity())[1] * 0.3)
                            // 键盘关闭
                            if (bottom > oldBottom) {
                                isKeyboardVisible = false;
                                acceptKeyboardCloseEvent(chatEditLayout);
                            }// 键盘弹出
                            else if (bottom < oldBottom) {
                                isKeyboardVisible = true;
                            }
                    } else if (right > 0 && oldRight > 0 && right != oldRight && isKeyboardVisible) {//键盘显示状态时切换到了横屏
                        if (bottom != oldBottom) {//键盘关闭
                            isKeyboardVisible = false;
                            acceptKeyboardCloseEvent(chatEditLayout);
                        }
                    }
                }
            });
        }
        //发送框
        talk = findViewById(R.id.et_talk);
        talk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    tvSendMsg.setEnabled(true);
                    tvSendMsg.setSelected(true);
                } else {
                    tvSendMsg.setSelected(false);
                    tvSendMsg.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        talk.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        talk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isShowEmoji = false;
                    resetViewLayoutParams();
                    replaceChatEditContainer(chatEditLayout);
                    KeyboardUtils.showSoftInput(talk);//避免点击编辑框可能不弹出，替换后再显示
                    emoListLayout.setVisibility(View.GONE);
                    emoji.setSelected(isShowEmoji);
                }
                return false;
            }
        });
        //表情列表
        emoListLayout = findViewById(R.id.ic_chat_emo_list_layout);
        emoList = findViewById(R.id.rv_emo_list);
        emoList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 6, GridLayoutManager.VERTICAL, false);
        emoList.setLayoutManager(gridLayoutManager);
        emoList.addItemDecoration(new PolyvEmoListAdapter.GridSpacingItemDecoration(6, ConvertUtils.dp2px(4), true));
        emoListAdapter = new PolyvEmoListAdapter(emoList);
        emoListAdapter.setOnItemClickListener(new PolyvBaseRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, PolyvBaseRecyclerViewAdapter.ClickableViewHolder holder) {
                appendEmo(emoListAdapter.emoLists.get(position), false);
            }
        });
        emoList.setAdapter(emoListAdapter);
        //表情打开按钮
        emoji = findViewById(R.id.iv_emoji);
        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowEmoji = emoListLayout.getVisibility() != View.VISIBLE;//willShow
                KeyboardUtils.hideSoftInput(talk);//隐藏后再替换
                if (isShowEmoji) {
                    if (!isKeyboardVisible) {//键盘隐藏时才替换
                        replaceChatEditContainer(chatEditLayout);//放在监听器那里替换
                        emoListLayout.setVisibility(View.VISIBLE);//放在监听器那里替换
                        changeViewLayoutParams();//放在监听器那里替换
                    }
                    talk.requestFocus();
                } else {
                    resetChatEditContainer();
                    emoListLayout.setVisibility(View.GONE);
                    resetViewLayoutParams();
                }
                emoji.setSelected(isShowEmoji);
            }
        });
        //查看更多信息
        unread = findViewById(R.id.tv_unread);
        chatMessageList.setUnreadView(unread);
        //信息删除按钮
        ivMsgDelete = findViewById(R.id.iv_delete);
        ivMsgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEmoText();
            }
        });
        tvSendMsg = findViewById(R.id.tv_send);
        tvSendMsg.setEnabled(false);
        tvSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void resetViewLayoutParams() {
        if (view != null) {
            final ViewGroup viewParent = (ViewGroup) view.getParent();
            if (viewParent == null)
                return;
            ViewGroup.LayoutParams vlp = viewParent.getLayoutParams();//viewpager中改变view的高度无效
            vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            viewParent.setLayoutParams(vlp);
        }
    }

    private void changeViewLayoutParams() {
        if (emoListLayout == null)
            return;
        if (emoListLayout.getHeight() == 0) {
            emoListLayout.post(new Runnable() {
                @Override
                public void run() {
                    setViewLayoutParams(emoListLayout.getHeight());
                }
            });
        } else {
            setViewLayoutParams(emoListLayout.getHeight());
        }
    }

    private void setViewLayoutParams(int height) {//和adjustResize对应
        final ViewGroup viewParent = (ViewGroup) view.getParent();
        if (viewParent == null)
            return;
        ViewGroup.LayoutParams vlp = viewParent.getLayoutParams();//viewpager中改变view的高度无效
        if (viewParent.getHeight() > height) {
            vlp.height = viewParent.getHeight() - height;
            viewParent.setLayoutParams(vlp);
        }
    }

    private void resetChatEditContainer() {
        if (talkLayout != null && talkLayout.getParent() != talkParentLayout) {
            if (talkLayout.getParent() != null) {
                ((ViewGroup) talkLayout.getParent()).setVisibility(View.GONE);
                ((ViewGroup) talkLayout.getParent()).removeView(talkLayout);
            }
            talkParentLayout.removeAllViews();
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            talkLayout.setLayoutParams(flp);
            talkParentLayout.addView(talkLayout);
        }
    }

    private void replaceChatEditContainer(ViewGroup chatEditLayout) {
        if (chatEditLayout != null && talkLayout.getParent() != chatEditLayout && chatEditLayout instanceof FrameLayout) {
            chatEditLayout.setVisibility(View.VISIBLE);
            if (talkLayout.getParent() != null) {
                ((ViewGroup) talkLayout.getParent()).removeView(talkLayout);
            }
            chatEditLayout.removeAllViews();
            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            flp.gravity = Gravity.BOTTOM;
            talkLayout.setLayoutParams(flp);
            chatEditLayout.addView(talkLayout);
        }
    }

    // 删除表情
    private void deleteEmoText() {
        int start = talk.getSelectionStart();
        int end = talk.getSelectionEnd();
        if (end > 0) {
            if (start != end) {
                talk.getText().delete(start, end);
            } else if (isEmo(end)) {
                talk.getText().delete(end - emoLength, end);
            } else {
                talk.getText().delete(end - 1, end);
            }
        }
    }

    //判断是否是表情
    private boolean isEmo(int end) {
        String preMsg = talk.getText().subSequence(0, end).toString();
        int regEnd = preMsg.lastIndexOf("]");
        int regStart = preMsg.lastIndexOf("[");
        if (regEnd == end - 1 && regEnd - regStart >= 2) {
            String regex = preMsg.substring(regStart);
            emoLength = regex.length();
            if (PolyvFaceManager.getInstance().getFaceId(regex) != -1)
                return true;
        }
        return false;
    }

    //添加表情
    private void appendEmo(String emoKey, boolean useGif) {
        SpannableStringBuilder span = new SpannableStringBuilder(emoKey);
        int textSize = (int) talk.getTextSize();
        Drawable drawable;
        ImageSpan imageSpan;
        try {
            if (useGif) {
                //如需使用，请自行替换表情图片
                drawable = new GifDrawable(getResources(), PolyvFaceManager.getInstance().getFaceId(emoKey));
                imageSpan = new GifImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            } else {
                drawable = getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(emoKey));
                imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            }
        } catch (Exception e) {
            try {
                drawable = getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(emoKey));
                imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            } catch (Exception e1) {
                toast.makeText(getContext(), "添加表情失败！", PolyvToast.LENGTH_SHORT).show(true);
                return;
            }
        }
        drawable.setBounds(0, 0, (int) (textSize * 1.5), (int) (textSize * 1.5));
        span.setSpan(imageSpan, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int selectionStart = talk.getSelectionStart();
        int selectionEnd = talk.getSelectionEnd();
        if (selectionStart != selectionEnd)
            talk.getText().replace(selectionStart, selectionEnd, span);
        else
            talk.getText().insert(selectionStart, span);
    }

    private void acceptKeyboardCloseEvent(ViewGroup chatEditLayout) {
        if (!isShowEmoji) {
            resetChatEditContainer();
        } else {
            replaceChatEditContainer(chatEditLayout);//放在这里替换，避免布局抖动
            emoListLayout.setVisibility(View.VISIBLE);//放在这里替换，避免布局抖动
            changeViewLayoutParams();//放在这里替换，避免布局抖动
        }
    }

    protected void hideSoftInputAndEmoList() {
        if (talk != null) {
            KeyboardUtils.hideSoftInput(talk);
        }
        if (emoListLayout != null) {
            emoListLayout.setVisibility(View.GONE);
            emoji.setSelected(false);
        }
        if (isShowEmoji) {//isShowEmoji->键盘已隐藏
            resetChatEditContainer();//如果键盘还没隐藏，需要交给监听器重置，不然会有问题
            resetViewLayoutParams();
        }
        isShowEmoji = false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            hideSoftInputAndEmoList();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getUserVisibleHint()) {
            hideSoftInputAndEmoList();
        }
    }

    public boolean onBackPressed() {
        if (getImageViewerContainer() != null && getImageViewerContainer().getVisibility() == View.VISIBLE) {
            getImageViewerContainer().setVisibility(View.GONE);
            return true;
        } else if (emoListLayout != null && emoListLayout.getVisibility() == View.VISIBLE) {
            hideSoftInputAndEmoList();
            return true;
        }
        return false;
    }

    public abstract void sendMessage();

    public static class ConnectStatus {
        public int status;
        public Throwable t;

        public ConnectStatus(int status, Throwable t) {
            this.status = status;
            this.t = t;
        }
    }

    public static class EventMessage {
        public String message;
        public String event;

        public EventMessage(String message, String event) {
            this.message = message;
            this.event = event;
        }
    }
}
