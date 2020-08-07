package com.community.controller.interceptor;

import com.community.annotation.LoginRequired;
import com.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author flunggg
 * @date 2020/7/25 10:26
 * @Email: chaste86@163.com
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记住就行，判断请求的是不是方法（HandlerMethod），因为可能还有其他请求，比如静态资源
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 通过反射获取注解
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 判断请求的url（方法）是否被标记上@LoginRequired，如果有，则判断是否登录
            if(loginRequired != null &&  hostHolder.getUser() == null) {
                // 如果没有登录，则重定向到登录页面
                // request.getContextPath():请求的项目名路径
                response.sendRedirect(request.getContextPath() + "/login");
                // 拦截
                return false;
            }

        }
        return true;
    }
}
