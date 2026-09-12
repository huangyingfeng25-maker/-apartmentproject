package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.controller.constant.RedisConstant;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SystemUserMapper systemUserMapper;

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130,48,4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String code = specCaptcha.text().toLowerCase();
        //根据生成验证码图片，把图片转换字符串
        String image = specCaptcha.toBase64();

        //把验证码放到redis里面
        // redis key: 生成唯一的值 ， value：验证码四位值 code
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        //放到redis里面
        //为了测试，过期时间设置长一点
        redisTemplate.opsForValue().set(key,code,60, TimeUnit.HOURS);

        //封装vo对象
        CaptchaVo captchaVo = new CaptchaVo();
        captchaVo.setKey(key);
        captchaVo.setImage(image);
        return captchaVo;
    }

    @Override
    public String login(LoginVo loginVo) {
        //1 从loginVo获取验证码
        String input_captchaCode = loginVo.getCaptchaCode();
        //2 判断验证码是否为空，如果为空，提示用户
        if (!StringUtils.hasText(input_captchaCode)){
            throw new RuntimeException();
        }
        //3 如果验证码不为空，从redis根据loginVo里面key获取redis存储验证码
        String captchaKey = loginVo.getCaptchaKey();
        String redis_captchaCode = redisTemplate.opsForValue().get(captchaKey);
        //4 如果根据key获取redis验证码为空，提示用户
        if(!StringUtils.hasText(redis_captchaCode)) {
            throw new RuntimeException();
        }
        //5 如果根据key获取redis验证码不为空，校验验证码
        // 把redis的验证码 和 输入的验证码比对，如果不同，提示用户
        if(!redis_captchaCode.equals(input_captchaCode)){
            throw new RuntimeException();
        }
        //6 根据loginVo里面用户名查询数据库，如果查询结果为空，提示用户
        String input_username = loginVo.getUsername();
        LambdaQueryWrapper<SystemUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SystemUser::getUsername,input_username);
        SystemUser systemUser = systemUserMapper.selectOne(wrapper);
        if(systemUser == null) {
            throw new RuntimeException();
        }
        //7 如果查询结果不为空，判断用户是否被禁用，如果被禁用，提示用户
        if(systemUser.getStatus()== BaseStatus.DISABLE){
            throw new RuntimeException();
        }
        //8 如果用户没有禁用，比较密码
        // 把数据库存储密码 和输入的密码比对，输入密码进行加密之后再比对
        String database_password = systemUser.getPassword();
        String input_password = loginVo.getPassword();
        //输入密码加密
        String input_password_md5 = DigestUtils.md5Hex(input_password);
        if(!database_password.equals(input_password_md5)){
            throw new RuntimeException();
        }
        //10 使用jwt生成token，返回token
        String token = JwtUtil.createToken(systemUser.getId(), systemUser.getUsername());
        return token;
    }
}
