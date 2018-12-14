package com.easefun.polyv.commonui.utils;

import android.graphics.BitmapFactory;

public class PolyvPictureUtils {

    public static int[] getPictureWh(String pictureFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不分配内存
        BitmapFactory.decodeFile(pictureFilePath, options);//返回null
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        return new int[]{imageWidth, imageHeight};
    }
}
