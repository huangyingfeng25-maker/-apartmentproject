package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//异常处理类
@ControllerAdvice
public class GlobalExceptionHandler {

    //出现异常执行的方法
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }
}
