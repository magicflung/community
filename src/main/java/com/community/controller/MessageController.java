package com.community.controller;

import com.community.annotation.LoginRequired;
import com.community.entity.Message;
import com.community.entity.User;
import com.community.service.MessageService;
import com.community.service.UserService;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import com.community.util.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author flunggg
 * @date 2020/8/3 10:59
 * @Email: chaste86@163.com
 */
@Controller
@RequestMapping("/message")
@LoginRequired
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    @GetMapping("/list")
    public String getConversations(Model model, Page page) {
        // 需要判断user
        User user = hostHolder.getUser();
        // 分页
        page.setLimit(5);
        page.setPath("/message/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 查询私信列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffSet(), page.getLimit());
        // 还需要查询出 私信列表的未读数量，每个私信详情的未读数量，私信详情中的数量
        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Message conversation : conversationList) {
            Map<String, Object> map = new HashMap<>();
            map.put("conversation", conversation); // 私信/会话
            // 私信详情的未读数量
            map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), conversation.getConversationId()));
            // 私信详情中的总数量
            map.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
            // 获得对方信息
            int targetId = user.getId() == conversation.getToId() ? conversation.getFromId() : conversation.getToId();
            map.put("target", userService.findUserById(targetId));
            // 每一个私信添加进list
            conversations.add(map);
        }
        model.addAttribute("conversations", conversations);
        // 未读私信的总数量
        model.addAttribute("unreadAllCount", messageService.findLetterUnreadCount(user.getId(), null));

        return "/site/letter";
    }

    /**
     * 私信详情
     * 并把未读的改为已读
     * @param conversationId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/detail/{conversationId}")
    public String getLetters(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        // 分页
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setPath("/message/detail/" + conversationId);
        // 查询私信详情列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffSet(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        for (Message letter : letterList) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter", letter);
            User fromUser = userService.findUserById(letter.getFromId());
            map.put("fromUser", fromUser);
            letters.add(map);
        }

        model.addAttribute("letters", letters);
        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getUnreadLettersId(letterList);
        if(!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    private List<Integer> getUnreadLettersId(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if(letterList != null) {
            for(Message letter : letterList) {
                if(letter.getToId() == hostHolder.getUser().getId() && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        int targetId = hostHolder.getUser().getId() == id0 ? id1 : id0;
        return userService.findUserById(targetId);
    }

    /**
     * 异步发送私信
     * @param toName 发送给谁
     * @param toContent 发送内容
     * @return
     */
    @PostMapping("/send")
    @ResponseBody
    public String sendLetter(String toName, String toContent) {
        if(StringUtils.isBlank(toName)) {
            return CommunityUtil.getJSONString(403, "目标用户不能为空");
        }
        if(StringUtils.isBlank(toContent)) {
            return CommunityUtil.getJSONString(403, "内容不能为空");
        }
        // 根据username查询接收用户
        User toUser = userService.findUserByName(toName);
        if(toUser == null) {
            return CommunityUtil.getJSONString(403, "目标用户不存在");
        }
        // 当前用户的
        User fromUser = hostHolder.getUser();

        Message message = new Message();
        message.setFromId(fromUser.getId());
        message.setToId(toUser.getId());
        message.setStatus(1);
        message.setCreateTime(new Date());
        message.setContent(toContent);
        // 拼接conversationId, 以id小的拼在前面
        if(fromUser.getId() < toUser.getId()) {
            message.setConversationId(fromUser.getId() + "_" + toUser.getId());
        } else {
            message.setConversationId(toUser.getId() + "_" + fromUser.getId());
        }
        messageService.insertMessage(message);

        return CommunityUtil.getJSONString(200, "发送成功！");
    }
}
