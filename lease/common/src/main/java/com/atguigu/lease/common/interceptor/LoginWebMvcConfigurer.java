package com.atguigu.lease.common.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * 安排活的人
 */
public class LoginWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;//，负责注册拦截器并指定拦截范围。

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(authenticationInterceptor)
                    .addPathPatterns("/admin/**")//拦截所有 /admin/ 开头的请求
                    .excludePathPatterns("/admin/login/**");//排除登录接口，发起的登录请求不需要 token
    }
}
