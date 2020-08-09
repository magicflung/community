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
 * 虽然springboot会自动跳转到error目录下的网页
 * 但是不会加到日志中，所以这理可以加入到日志。
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
        // 默认异常重定向到error目录中的网页
        // 但是需要注意，请求可能是同步请求，可能是异步请求
        // 同步请求就返回html（500页面，404页面），异步请求返回json（提示）
        // 以下是固定代码
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)) {
            // 异步请求
            // 告诉请求的接收者，body体的数据格式是符合json格式的，接受者拿到这些数据后可以直接使用相应的格式化方法转换成处理语言识别的数据对象或者框架拦截器自动进行转换，能更早发现数据传递上的错误
            // response.setContentType("application/json");
            // 接收者需要自己执行判断怎么处理这个数据。
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            // 人为转为json
            writer.write(CommunityUtil.getJSONString(500, "服务器异常！"));
        } else {
            // 同步请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
