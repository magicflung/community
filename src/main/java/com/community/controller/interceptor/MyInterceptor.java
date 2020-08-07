package com.community.controller.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author flunggg
 * @date 2020/7/23 14:32
 * @Email: chaste86@163.com
 */
@Component
public class MyInterceptor implements HandlerInterceptor {

    // 在Controller前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("preHandle:" + handler);
        // 如果返回false，则取消这个请求，否则返回true，处理该请求
        return true;
    }

    // 在Controller后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle:" + handler);
    }

    // 在TemplateEngine之后调用
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion:" + handler);
    }
}
