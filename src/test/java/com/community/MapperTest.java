package com.community;

import com.community.dao.DiscussPostMapper;
import com.community.dao.UserMapper;
import com.community.entity.DiscussPost;
import com.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @author flunggg
 * @date 2020/7/18 20:40
 * @Email: chaste86@163.com
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(11);
        System.out.println(user);

        User liubei = userMapper.selectByName("liubei");
        System.out.println(liubei);

        User user1 = userMapper.selectByEmail("nowcoder113@sina.com");
        System.out.println(user1);

    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setEmail("123@163.com");
        user.setSalt("abc");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int i = userMapper.insertUser(user);
        System.out.println(i);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
        int i = userMapper.updateStatus(150, 1);
        System.out.println(i);
        int i1 = userMapper.updatePassword(150, "654321");
        System.out.println(i1);
        int i2 = userMapper.updateHeader(150, "http://www.nowcoder.com/100.png");
        System.out.println(i2);
    }

    @Test
    public void testSelectPost() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 5);
        for(DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }

        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }
}
