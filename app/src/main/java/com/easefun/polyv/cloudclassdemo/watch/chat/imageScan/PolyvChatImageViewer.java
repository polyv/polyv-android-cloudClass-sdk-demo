package com.easefun.polyv.cloudclassdemo.watch.chat.imageScan;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.bumptech.glide.Glide;
import com.easefun.polyv.cloudclass.chat.event.PolyvChatImgEvent;
import com.easefun.polyv.cloudclass.chat.history.PolyvChatImgHistory;
import com.easefun.polyv.cloudclass.chat.playback.PolyvChatPlaybackImg;
import com.easefun.polyv.cloudclass.chat.send.img.PolyvSendLocalImgEvent;
import com.easefun.polyv.cloudclassdemo.watch.chat.adapter.PolyvChatListAdapter;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.utils.PolyvToast;
import com.easefun.polyv.foundationsdk.permission.PolyvOnGrantedListener;
import com.easefun.polyv.foundationsdk.permission.PolyvPermissionManager;
import com.easefun.polyv.foundationsdk.utils.PolyvSDCardUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PolyvChatImageViewer extends FrameLayout {
    private View view;
    private TextView tvPage;
    private ImageView ivDownload;
    private ViewPager vpImageViewer;
    private List<PolyvChatListAdapter.ChatTypeItem> chatTypeItems;
    private int currentPosition = -1;
    private PolyvPermissionManager permissionManager;
    private CompositeDisposable compositeDisposable;
    private PolyvToast toast = new PolyvToast();

    public PolyvChatImageViewer(@NonNull Context context) {
        this(context, null);
    }

    public PolyvChatImageViewer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvChatImageViewer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.polyv_image_viewpager, this);
        vpImageViewer = view.findViewById(R.id.vp_image_viewer);
        tvPage = view.findViewById(R.id.tv_page);
        ivDownload = view.findViewById(R.id.iv_download);
        ivDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionManager == null) {
                    downloadImg();
                } else {
                    boolean result = permissionManager.permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .opstrs(-1)
                            .meanings("存储权限")
                            .setOnGrantedListener(new PolyvOnGrantedListener() {
                                @Override
                                public void afterPermissionsOnGranted() {
                                    downloadImg();
                                }
                            })
                            .request();
                    if (!result) {
                        toast("请允许存储权限后再保存图片");
                    }
                }
            }
        });
    }

    private void downloadImg() {
        if (currentPosition > -1) {
            final String imgUrl = getImgUrl(chatTypeItems.get(currentPosition));
            if (imgUrl == null) {
                toast("图片保存失败(null)");
                return;
            }
            final String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
            final String savePath = PolyvSDCardUtils.createPath(getContext(), "PolyvImg");
            compositeDisposable.add(
                    Observable.just(1)
                            .map(new Function<Integer, File>() {
                                @Override
                                public File apply(Integer integer) throws Exception {
                                    try {
                                        File file = new File(imgUrl);//对于本地gif图片的情况
                                        if (file.isFile() && file.exists()) {
                                            return file;
                                        }
                                    } catch (Exception e) {
                                    }
                                    return Glide.with(getContext())
                                            .asFile()
                                            .load(imgUrl)
                                            .submit()
                                            .get();
                                }
                            })
                            .map(new Function<File, Boolean>() {
                                @Override
                                public Boolean apply(File file) throws Exception {
                                    if (file.getAbsolutePath().equals(new File(savePath, fileName).getAbsolutePath()))
                                        return true;
                                    return FileUtils.copyFile(file, new File(savePath, fileName),//同path时，复制失败
                                            new FileUtils.OnReplaceListener() {
                                                @Override
                                                public boolean onReplace() {
                                                    return true;
                                                }
                                            });
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    toast(aBoolean ? "图片保存在：" + new File(savePath, fileName).getAbsolutePath() : "图片保存失败(saveFailed)");
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    toast("图片保存失败(loadFailed)");
                                }
                            })
            );
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            compositeDisposable.dispose();
        } else {
            compositeDisposable = new CompositeDisposable();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        compositeDisposable.dispose();
        toast.destroy();
    }

    private void toast(String message) {
        toast.makeText(getContext(), message, PolyvToast.LENGTH_SHORT).show();
    }

    public static String getImgUrl(PolyvChatListAdapter.ChatTypeItem chatTypeItem) {
        String chatImgUrl = null;
        if (chatTypeItem.object instanceof PolyvChatImgEvent) {
            PolyvChatImgEvent chatImgEvent = (PolyvChatImgEvent) chatTypeItem.object;
            chatImgUrl = chatImgEvent.getValues().get(0).getUploadImgUrl();
        } else if (chatTypeItem.object instanceof PolyvChatImgHistory) {
            PolyvChatImgHistory chatImgHistory = (PolyvChatImgHistory) chatTypeItem.object;
            chatImgUrl = chatImgHistory.getContent().getUploadImgUrl();
        } else if (chatTypeItem.object instanceof PolyvSendLocalImgEvent) {
            PolyvSendLocalImgEvent sendLocalImgEvent = (PolyvSendLocalImgEvent) chatTypeItem.object;
            chatImgUrl = sendLocalImgEvent.getImageFilePath();
        } else if (chatTypeItem.object instanceof PolyvChatPlaybackImg) {
            PolyvChatPlaybackImg chatPlaybackImg = (PolyvChatPlaybackImg) chatTypeItem.object;
            if (chatPlaybackImg.getContent() != null) {
                chatImgUrl = chatPlaybackImg.getContent().getUploadImgUrl();
            }
        }
        return chatImgUrl;
    }

    public void setPermissionManager(PolyvPermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setData(final List<PolyvChatListAdapter.ChatTypeItem> items, int curPosition) {
        if (items != null && items.size() > 0) {
            chatTypeItems = items;
            PolyvChatImgFragmentStatePagerAdapter fragmentAdapter = new PolyvChatImgFragmentStatePagerAdapter(((AppCompatActivity) getContext()).getSupportFragmentManager(), chatTypeItems);
            fragmentAdapter.setOnClickImgListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getParent() != null && ((ViewGroup) getParent()).getVisibility() == View.VISIBLE) {
                        ((ViewGroup) getParent()).setVisibility(View.GONE);
                    }
                }
            });
            vpImageViewer.setAdapter(fragmentAdapter);
            vpImageViewer.clearOnPageChangeListeners();
            vpImageViewer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    currentPosition = position;
                    tvPage.setText(position + 1 + " / " + chatTypeItems.size());
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            vpImageViewer.setCurrentItem(curPosition, false);
            currentPosition = curPosition;
            tvPage.setText(curPosition + 1 + " / " + chatTypeItems.size());
        }
    }

    public void attachRootView(ViewGroup rootView) {
        if (rootView == null || chatTypeItems == null || chatTypeItems.size() == 0)
            return;
        if (rootView == getParent()) {
            if (rootView.getVisibility() != View.VISIBLE)
                rootView.setVisibility(View.VISIBLE);
            return;
        }
        rootView.removeAllViews();
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null)
            viewGroup.removeView(this);
        rootView.addView(this);
        if (rootView.getVisibility() != View.VISIBLE)
            rootView.setVisibility(View.VISIBLE);
    }

    public void detachRootView() {
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null) {
            viewGroup.removeView(this);
        }
    }
}
