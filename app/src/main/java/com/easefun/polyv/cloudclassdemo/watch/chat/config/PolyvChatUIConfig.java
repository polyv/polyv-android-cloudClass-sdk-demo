package com.easefun.polyv.cloudclassdemo.watch.chat.config;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * date: 2019/5/30 0030
 *
 * @author hwj
 * description 聊天室可选ui配置
 */
public class PolyvChatUIConfig {

    /**
     * 聊天室成员字体颜色
     */
    public static class FontColor {
        @Retention(RetentionPolicy.SOURCE)
        @StringDef({
                FontColor.USER_TEACHER,
                FontColor.USER_MANAGER,
                FontColor.USER_ASSISTANT,
                FontColor.USER_STUDENT
        })
        @interface UserType {
        }

        private static final int DEFAULT_COLOR= Color.parseColor("#546e7a");

        public static final String USER_TEACHER = "user_teacher";
        public static final String USER_MANAGER = "user_manager";
        public static final String USER_ASSISTANT = "user_assistant";
        public static final String USER_STUDENT = "user_student";

        public static int color_teacher = DEFAULT_COLOR;
        public static int color_manager = DEFAULT_COLOR;
        public static int color_assistant = DEFAULT_COLOR;
        public static int color_student = DEFAULT_COLOR;

        public static void set(@UserType String userType, @ColorInt int color) {
            switch (userType) {
                case USER_TEACHER:
                    color_teacher = color;
                    break;
                case USER_MANAGER:
                    color_manager = color;
                    break;
                case USER_STUDENT:
                    color_student = color;
                    break;
                case USER_ASSISTANT:
                    color_assistant = color;
                    break;
            }
        }
    }
}
