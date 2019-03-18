package com.easefun.polyv.cloudclassdemo.utils;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * @date: 2019/3/12 0012
 * @author: hwj
 * @description 转换器
 */
public class HttpExceptionConverter {

    /**
     * 将RxJava抛出的，Retrofit生成的，http异常信息转换出来
     * @param throwable throwable
     * @return 一般是json
     */
    public static String convert(Throwable throwable) {
        String result = "";
        if (throwable instanceof retrofit2.HttpException) {
            ResponseBody erroBody = ((retrofit2.HttpException) throwable).response().errorBody();
            if (erroBody == null) {
                return result;
            }
            try {
                result = erroBody.string();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                erroBody.close();
            }
        } else {
            result = "throwable 不是 retrofit2.HttpException 的子类型!";
        }
        return result;
    }
}
