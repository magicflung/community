package com.community.service;

import com.community.dao.MessageMapper;
import com.community.entity.Message;
import com.community.filter.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author flunggg
 * @date 2020/8/3 10:27
 * @Email: chaste86@163.com
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int insertMessage(Message message) {
        // 转义
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        // 过滤敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 已读信息，修改status为1
     * @param ids
     * @return
     */
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }
}
