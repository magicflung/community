package com.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
class CommunityApplicationTests implements ApplicationContextAware { // 获取IOC容器

    private ApplicationContext applicationContext;

    // Spring在扫描组件时会检查ApplicationContextAware然后会调用下面方法，然后把IOC容器传进来
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Test
    void contextLoads() {
        System.out.println(applicationContext);
        // ADao bean = applicationContext.getBean(ADao.class);
        // System.out.println(bean.test());;
        //
        // ADao aaa = applicationContext.getBean("aaa", ADao.class);
        // System.out.println(aaa.test());
    }

    @Test
    void text1() {

        // AService bean = applicationContext.getBean(AService.class);
        // System.out.println(bean);
        //
        // bean = applicationContext.getBean(AService.class);
        // System.out.println(bean);
    }

    @Test
    void textFormat() {

        // SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        // System.out.println(simpleDateFormat.format(new Date()));

    }
}
