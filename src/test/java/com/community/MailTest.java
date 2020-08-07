package com.community;

import com.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author flunggg
 * @date 2020/7/21 9:28
 * @Email: chaste86@163.com
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
public class MailTest {
    @Autowired
    public MailClient mailClient;

    // 可以主动调用
    @Autowired
    public TemplateEngine templateEngine;

    @Test
    public void testMail() {
        mailClient.sendMail("chaste86@163.com", "Test", "Welcome");
    }

    @Test
    public void testHtmlMail() {
        // 可以设置键值
        Context context = new Context();
        context.setVariable("username", "张三");
        // 主动处理
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("chaste86@163.com", "Test", content);
    }
}
