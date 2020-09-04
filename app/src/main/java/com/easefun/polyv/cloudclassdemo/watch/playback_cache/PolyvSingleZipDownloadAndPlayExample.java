package com.easefun.polyv.cloudclassdemo.watch.playback_cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.easefun.polyv.businesssdk.model.video.PolyvPlaybackVideoParams;
import com.easefun.polyv.cloudclassdemo.watch.player.playback.PolyvPlaybackVideoHelper;
import com.easefun.polyv.foundationsdk.download.listener.IPolyvDownloaderSDKListener;
import com.easefun.polyv.foundationsdk.download.zip.IPolyvDownloaderUnzipListener;
import com.easefun.polyv.foundationsdk.download.zip.PolyvZipDownloader;
import com.easefun.polyv.foundationsdk.download.zip.PolyvZipMultimedia;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.LogUtils;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.functions.Consumer;

/**
 * date: 2019/9/18 0018
 *
 * @author hwj
 * description 单个压缩文件下载并播放示例
 */
public class PolyvSingleZipDownloadAndPlayExample {

    public static void testDownloadAndPlay(View attachedView, PolyvPlaybackVideoHelper helper, PolyvPlaybackVideoParams playbackVideoParams) {
        String url = "http://liveimages.videocc.net/ppt/d5c168fc26c3a1d5a33fe592b5ca41f5.zip?run=2234";
        PolyvSingleZipDownloadAndPlayExample.testDownloadZip(url, file -> {
            attachedView.post(() -> {
                helper.startLocal(file, playbackVideoParams);
            });
        });
    }

    private static void testDownloadZip(String url, Consumer<File> callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String appCacheDir = Utils.getApp().getCacheDir().getAbsolutePath();
                String fileName = url.substring(url.lastIndexOf("/") + 1);

                //解压后的文件名
                String unzipFileName = fileName.substring(0, fileName.lastIndexOf("."));
                File unzipFile = new File(appCacheDir, unzipFileName);
                if (unzipFile.exists()) {
                    try {
                        callback.accept(unzipFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                PolyvZipMultimedia multimedia = new PolyvZipMultimedia(1, url, appCacheDir, unzipFileName + ".zip");
                PolyvZipDownloader zipDownloader = new PolyvZipDownloader(multimedia);
                zipDownloader.addDownloadListener(new IPolyvDownloaderSDKListener() {
                    @Override
                    public void onDownloadSuccess() {
                        LogUtils.d("下载成功");
                    }

                    @Override
                    public void onDownloadError(@NonNull String event, int errorReason, @Nullable ArrayList<String> exceptionList, @Nullable ArrayList<String> logList) {
                        LogUtils.e("下载失败" + event + " " + errorReason + "\n" + exceptionList + "\n" + logList);
                    }

                    @Override
                    public void onDownloadProgress(long downloaded, long total) {

                    }

                    @Override
                    public void onByte(int num) {

                    }
                });

                zipDownloader.addUnzipListener(new IPolyvDownloaderUnzipListener() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onDone() {
                        LogUtils.d("解压成功");
                        LogUtils.d(appCacheDir);
                        try {
                            callback.accept(unzipFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                zipDownloader.start();
            }
        };
        new Thread(runnable).start();
    }

    public static File getDownloadedFile() {
        String fileName = "8205ac89d37e05513c5cb49dcb5f2705";
        String fileParentName = Utils.getApp().getCacheDir().getAbsolutePath();

        File file = new File(fileParentName, fileName);
        return file;
    }
}
