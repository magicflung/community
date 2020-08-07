package com.community;

import com.community.filter.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author flunggg
 * @date 2020/7/27 15:28
 * @Email: chaste86@163.com
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
public class FilterTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitive() {
        /**
         * 敏感词测试用例
         * fabcd
         * abc
         * 如果处理不好，比如fabcc还是照样输出，原因就是position越界后跳出循环，而返回结果时，begin后面的字符都会被添加到结果集中，
         *      这是错误的，因为begin+1可能还是敏感字符
         */
        String text = "fabcc";
        String filter = sensitiveFilter.filter(text);
        System.out.println(filter);

        text = "☆f☆a☆b☆c☆";
        filter = sensitiveFilter.filter(text);
        System.out.println(filter);
    }
}
