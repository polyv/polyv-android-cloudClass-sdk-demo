package com.easefun.polyv.commonui.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.thirdpart.blankj.utilcode.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PolyvPictureUtils {

    public static int[] getPictureWh(String pictureFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不分配内存
        BitmapFactory.decodeFile(pictureFilePath, options);//返回null
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        return new int[]{imageWidth, imageHeight};
    }

    /**
     * 插入图片信息到MediaStore，还需要对Uri写入实际流才算保存完毕
     * @param imgName
     * @return
     */
    public static Uri insertImageToMediaStore(String imgName){
        if(TextUtils.isEmpty(imgName)){
            return null;
        }

        String type = imgName.substring(imgName.lastIndexOf(".")+1);

        Uri insertUri = null;
        ContentResolver resolver = Utils.getApp().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imgName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + type);
        //相对路径仅在Android Q开始才支持
//        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +"/"+ relativePath);

        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try {
            insertUri = resolver.insert(external, values);
        } catch (Exception e){
            e.printStackTrace();
            PolyvCommonLog.e("insertImageToMediaStore", e.getMessage());
        }

        return insertUri;
    }

    public static boolean copyImageToExternal(String sourcePath, Uri insertUri){
        if(insertUri == null){
            return false;
        }

        ContentResolver resolver = Utils.getApp().getContentResolver();
        InputStream is = null;
        OutputStream os = null;

        try {
            os = resolver.openOutputStream(insertUri);
            if(os == null){
                return false;
            }

            File sourceFile = new File(sourcePath);
            if(!sourceFile.exists()){
                return false;
            }

            is = new FileInputStream(sourceFile);

            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer);
            }
            os.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
