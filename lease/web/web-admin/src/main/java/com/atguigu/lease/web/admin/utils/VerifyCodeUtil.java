package com.atguigu.lease.web.admin.utils;

import java.util.Random;

public class VerifyCodeUtil {
    public static String getVerifyCode(int length){
        StringBuilder stringBuilder = new StringBuilder();
        Random random=new Random();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }
}
