package com.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author flunggg
 * @date 2020/7/23 14:58
 * @Email: chaste86@163.com
 */
public class CookieUtil {
    /**
     *
     * @param request
     * @param name
     * @return 从cookie获取名为name的值
     */
    public static Cookie getValue(HttpServletRequest request, String name) {
        if(request == null || name == null) {
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 设置一个cookie的持续时间
     * @param response HttpServletResponse
     * @param maxAge cookie的持续时间
     */
    public static void set(HttpServletResponse response,
                           Cookie cookie,
                           int maxAge) {
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
