package com.community.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志是系统需求，为了不让业务耦合系统需求，使用AOP统一日志处理
 * @author flunggg
 * @date 2020/8/4 15:05
 * @Email: chaste86@163.com
 */
@Component
@Aspect
public class ServiceLogAspect {

    // 日志处理
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceLogAspect.class);

    // 切点
    @Pointcut("execution(* com.community.service.*.*(..))")
    public void pointcut() {

    }

    /**
     * 让每一个用户访问到业务代码时，日志打印出每一个用户的ip，访问时间，访问什么方法
     * @param joinPoint 可以获取切点的信息
     */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 获取ip
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(servletRequestAttributes == null) {
            // 原先所有的业务层都是通过Conrtoller去访问的
            // 特殊情况，就是引入kafka后，消费者也会去调用业务，所以这次调用就没有request
            return ;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringType() + "." + joinPoint.getSignature().getName();

        LOGGER.info(String.format("用户[%s], 在[%s]，访问了[%s].", ip, now, target));

    }
}
