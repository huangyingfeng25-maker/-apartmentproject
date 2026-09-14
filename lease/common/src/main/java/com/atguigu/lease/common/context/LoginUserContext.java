package com.atguigu.lease.common.context;

public class LoginUserContext {

    private static final ThreadLocal<LoginUser> userThreadLocal = new ThreadLocal<>();

    //放
    public static void setLoginUser(LoginUser loginUser) {
        userThreadLocal.set(loginUser);
    }

    //获取
    public static LoginUser getLoginUser() {
        return userThreadLocal.get();
    }

    //清除
    public static void clear() {
        userThreadLocal.remove();
    }
}
