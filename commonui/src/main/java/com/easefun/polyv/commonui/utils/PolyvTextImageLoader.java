package com.easefun.polyv.commonui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.easefun.polyv.businesssdk.sub.gif.GifImageSpan;
import com.easefun.polyv.businesssdk.sub.gif.RelativeImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;

public class PolyvTextImageLoader {

    public static CharSequence messageToSpan(CharSequence charSequence, int size, boolean useGif, Context context) {
        return messageToSpan(charSequence, size, useGif, context, false);
    }

    public static CharSequence messageToSpan(CharSequence charSequence, int size, boolean useGif, Context context, boolean isToHtmlFormat) {
        if (charSequence instanceof String && isToHtmlFormat) {
            charSequence = Html.fromHtml((String) charSequence);//html转义，<>会转为""
        }
        int reqWidth;
        int reqHeight;
        reqWidth = reqHeight = size;
        SpannableStringBuilder span = new SpannableStringBuilder(charSequence);
        int start;
        int end;
        Pattern pattern = Pattern.compile("\\[[^\\[]{1,5}\\]");
        Matcher matcher = pattern.matcher(charSequence);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            String group = matcher.group();
            Drawable drawable;
            ImageSpan imageSpan;
            try {
                if (useGif) {
                    //由于gif会有卡顿的问题，如果需要，请替换为gif表情图片
                    //异步加载需view的高宽是固定的
                    drawable = new GifDrawable(context.getResources(), PolyvFaceManager.getInstance().getFaceId(group));
                    imageSpan = new GifImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
                } else {
                    drawable = context.getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(group));
                    imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
                }
            } catch (Exception e) {
                try {
                    drawable = context.getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(group));
                    imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
                } catch (Exception e1) {
                    continue;
                }
            }
            drawable.setBounds(0, 0, (int) (reqWidth * 1.6), (int) (reqHeight * 1.6));
            span.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    /**
     * 显示带本地表情图片的图文混排
     */
    public static void displayTextImage(CharSequence charSequence, TextView textView, boolean useGif) {
        textView.setText(messageToSpan(charSequence, (int) textView.getTextSize(), useGif, textView.getContext()));
    }
}
