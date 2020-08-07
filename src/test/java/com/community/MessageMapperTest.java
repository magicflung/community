package com.community;

import com.community.dao.MessageMapper;
import com.community.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

/**
 * @author flunggg
 * @date 2020/8/3 10:15
 * @Email: chaste86@163.com
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
public class MessageMapperTest {
    @Autowired
    private MessageMapper messageMapper;

    /**
     * 查询一个用户中所有最新的私信列表
     */
    @Test
    public void testSelectConversations() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 5);
        for(Message message : messages) {
            System.out.println(message);
        }
    }

    /**
     * 查询一个用户中所有最新的私信列表的数量
     */
    @Test
    public void testSelectConversationCount() {
        int i = messageMapper.selectConversationCount(111);
        System.out.println(i);
    }

    /**
     * 查询一个私信的全部内容
     */
    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 10);
        for(Message message : messages) {
            System.out.println(message);
        }
    }

    /**
     * 查询一个私信内的全部数量
     */
    @Test
    public void testSelectLetterCount() {
        int i = messageMapper.selectLetterCount("111_112");
        System.out.println(i);
    }

    /**
     * 一个用户的所有私信未读的数量，也可以查一个私信内的未读的数量
     */
    @Test
    public void testSelectLetterUnreadCount() {
        int i = messageMapper.selectLetterUnreadCount(111, null);
        System.out.println(i);
    }
}
