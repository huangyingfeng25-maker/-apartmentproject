package com.atguigu.lease.common.interceptor;

import com.atguigu.lease.common.context.LoginUser;
import com.atguigu.lease.common.context.LoginUserContext;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
/**
 * 干活的人
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //TODO 进行是否进行登录判断
        //1 获取请求头token  access-token和前端一致
        String token = request.

                getHeader("access-token");

        //2 判断token是否为空，如果空，提示用户
        if(token == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }else {
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId",Long.class);
            String username=claims.get("username",String.class);
            //TODO 解析token成功之后，根据userId查询数据库，用户是否正常

            //把userId放到ThreadLocal里面
            LoginUser loginUser=new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setUsername(username);
            LoginUserContext.setLoginUser(loginUser);
        }
        return true;
    }
    //清除ThreadLocal值，防止内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        LoginUserContext.clear();
    }
}
