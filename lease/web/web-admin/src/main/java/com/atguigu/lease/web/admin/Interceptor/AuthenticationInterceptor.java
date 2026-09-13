package com.atguigu.lease.web.admin.Interceptor;


import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private SystemUserMapper systemUserMapper;

    //之前执行
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        //进行是否登录判断
        //1 获取请求头token  access-token和前端一致
        String token = request.getHeader("access-token");

        //2 判断token是否为空，如果空，提示用户
        if(token == null) {
            throw new RuntimeException();
        } else { //3 如果不为空，解析token
            Claims claims = JwtUtil.parseToken(token);

            //4 解析token成功之后，根据userId查询数据库，用户是否正常
            Long userId = ((Number) claims.get("userId")).longValue();
            LambdaQueryWrapper<SystemUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemUser::getId, userId);
            SystemUser systemUser = systemUserMapper.selectOne(wrapper);
            //用户不存在或者被禁用，提示用户
            if (systemUser == null || systemUser.getStatus() == BaseStatus.DISABLE) {
                throw new RuntimeException();
            }
        }
        return true;
    }
}
