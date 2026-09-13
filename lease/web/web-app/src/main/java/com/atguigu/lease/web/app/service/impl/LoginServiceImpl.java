package com.atguigu.lease.web.app.service.impl;


import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.utils.HttpUtils;
import com.atguigu.lease.web.app.utils.VerifyCodeUtil;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import org.apache.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public String loginUser(LoginVo loginVo) {
        String verifyCode = VerifyCodeUtil.getVerifyCode(4);
        return "";
    }

    @Override
    public void getCode(String phone) {
        //1 生成4位数字验证码
        String verifyCode = VerifyCodeUtil.getVerifyCode(4);

        //2 把验证码和手机号传递阿里云进行发送
        this.sendMessage(phone,verifyCode);

        //3 验证码发送成功了，把验证码放到redis里面，设置过期时间
        // redis key：手机号    value：验证码
        redisTemplate.opsForValue().set(phone,verifyCode,60, TimeUnit.MINUTES);
    }

    private void sendMessage(String phone, String verifyCode){
        String host = "https://slytext.market.alicloudapi.com";
        String path = "/smssend";
        String method = "GET";
        String appcode = "9d8f9a2348fe4a639df2a26e9b38710c";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("templateId", "templateId");
        querys.put("receive", phone);
        querys.put("tag", "tag:"+verifyCode);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            throw new LeaseException("验证码发送失败",201);
        }
    }
}
