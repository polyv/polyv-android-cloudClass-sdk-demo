package com.easefun.polyv.cloudclassdemo.utils;

import com.blankj.utilcode.util.EncryptUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @date: 2019/2/22 0022
 * @author: hwj
 * @description 加密签名生成器
 */
public class PolyvSignCreator {

    /**
     * 用appSecret作为前后缀加密的签名生成方法。
     * see <a href="http://dev.polyv.net/2018/liveproduct/l-api/rule/sign/">
     *
     * @param appSecret     appSecret
     * @param signTargetMap 所有需要加密的参数。
     * @return MD5加密过的String
     */
    public static String createSign(String appSecret, Map<String, String> signTargetMap) {
        String signResult = "";

        Map<String, String> paramMap = signTargetMap;
        String[] keyArray = paramMap.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);

        StringBuilder builder = new StringBuilder();
        builder.append(appSecret);
        for (String key : keyArray) {
            builder.append(key).append(paramMap.get(key));
        }
        builder.append(appSecret);

        String signSource = builder.toString();
        signResult = EncryptUtils.encryptMD5ToString(signSource).toUpperCase();
        return signResult;
    }
}
