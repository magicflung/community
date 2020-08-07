package com.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录拦截
 * @author flunggg
 * @date 2020/7/25 10:24
 * @Email: chaste86@163.com
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    // 因为一些网页不能直接访问，得登录才能访问
    // 该注解只是一个标记，看看哪些方法需要拦截
    // 不做任何事
}
