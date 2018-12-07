package com.easefun.polyv.commonui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PolyvPictureUtils {

    public static int[] getPictureWh(String pictureFilePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(pictureFilePath);
        if (bitmap == null) {
            return new int[]{-1, -1};
        }
        return new int[]{bitmap.getWidth(), bitmap.getHeight()};
    }
}
