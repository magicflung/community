package com.community;

import com.community.dao.CommentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author flunggg
 * @date 2020/8/3 10:15
 * @Email: chaste86@163.com
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
public class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    public void textComment() {
        int i = commentMapper.selectCountByEntity(1, 228);
        System.out.println(i);
    }
}
