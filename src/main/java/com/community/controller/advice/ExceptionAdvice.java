package com.community.controller.advice;

import com.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @author flunggg
 * @date 2020/8/4 11:14
 * @Email: chaste86@163.com
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * @ExceptionHandler传入Exception.class表示捕获所有Exception以及Exception子类的异常
     * 可以有其他参数，但是常用以下三个
     * @param e
     * @param request
     * @param response
     */
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.error("服务器发送异常：" + e.getMessage());
        // 详细异常信息
        for(StackTraceElement element : e.getStackTrace()) {
            LOGGER.error(element.toString());
        }
        // 重定向
        // 需要注意，可能是同步请求，可能是异步请求
        // 同步请求就返回html，但是异步请求返回json
        // 以下是固定代码
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)) {
            // 异步请求
            // response.setContentType("application/json"); // 如果写字符串，浏览器自动转为json对象
            response.setContentType("application/plain;charset=utf-8"); // 也可以向浏览器返回普通字符串，也可以是json格式，但是需要人为的转为json对象
            PrintWriter writer = response.getWriter();
            // 人为转为json
            writer.write(CommunityUtil.getJSONString(500, "服务器异常！"));
        } else {
            // 同步请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
