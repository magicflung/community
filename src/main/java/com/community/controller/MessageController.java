package com.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.community.annotation.LoginRequired;
import com.community.entity.Message;
import com.community.entity.User;
import com.community.service.MessageService;
import com.community.service.UserService;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import com.community.util.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author flunggg
 * @date 2020/8/3 10:59
 * @Email: chaste86@163.com
 */
@Controller
@LoginRequired
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    /**
     * 私信列表
     *
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/letter/list")
    public String getLetters(Model model, Page page) {
        // 需要判断user
        User user = hostHolder.getUser();
        // 分页
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 查询私信列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffSet(), page.getLimit());
        // 还需要查询出 私信列表的未读数量，每个私信详情的未读数量，私信详情中的数量
        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Message conversation : conversationList) {
            Map<String, Object> map = new HashMap<>();
            map.put("conversation", conversation); // 私信/会话
            // 私信详情的未读数量
            map.put("unreadCount", messageService.findUnreadLetterCount(user.getId(), conversation.getConversationId()));
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
        model.addAttribute("unreadLetterCount", messageService.findUnreadLetterCount(user.getId(), null));
        // 现在系统通知功能写好了，补上查询系统通知的未读数量
        model.addAttribute("unreadNoticeCount", messageService.findUnreadNoticeCount(user.getId(), null));
        return "/site/letter";
    }

    /**
     * 私信详情列表
     * 并把未读的改为已读
     *
     * @param conversationId
     * @param model
     * @param page
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getDetailLetters(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        // 分页
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setPath("/letter/detail/" + conversationId);
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
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    /**
     * @param letterList
     * @return 获得未读私信的id
     */
    private List<Integer> getUnreadLettersId(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message letter : letterList) {
                if (letter.getToId() == hostHolder.getUser().getId() && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 得到私信的对象用户
     *
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        int targetId = hostHolder.getUser().getId() == id0 ? id1 : id0;
        return userService.findUserById(targetId);
    }

    /**
     * 异步发送私信
     *
     * @param toName    发送给谁
     * @param toContent 发送内容
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String toContent) {
        if (StringUtils.isBlank(toName)) {
            return CommunityUtil.getJSONString(403, "目标用户不能为空");
        }
        if (StringUtils.isBlank(toContent)) {
            return CommunityUtil.getJSONString(403, "内容不能为空");
        }
        // 根据username查询接收用户
        User toUser = userService.findUserByName(toName);
        if (toUser == null) {
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
        if (fromUser.getId() < toUser.getId()) {
            message.setConversationId(fromUser.getId() + "_" + toUser.getId());
        } else {
            message.setConversationId(toUser.getId() + "_" + fromUser.getId());
        }
        messageService.insertMessage(message);

        return CommunityUtil.getJSONString(200, "发送成功！");
    }


    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论主题的通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            Map<String, Object> commentNotice = new HashMap<>();
            commentNotice.put("message", message);
            // 从数据库看content需要转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 然后转为对象
            HashMap<String, Object> map = JSONObject.parseObject(content, HashMap.class);
            // 然后把这个map中的东西一一加入到messageVO
            commentNotice.put("user", userService.findUserById((Integer) map.get("userId")));
            commentNotice.put("entityType", map.get("entityType"));
            commentNotice.put("entityId", map.get("entityId"));
            commentNotice.put("postId", map.get("postId"));

            // 查询评论的通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            commentNotice.put("count", count);
            // 查询评论的未读通知数量
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT);
            commentNotice.put("unreadCount", unreadCount);

            model.addAttribute("commentNotice", commentNotice);
        }
        // 查询点赞主题的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> likeNotice = new HashMap<>();
            likeNotice.put("message", message);
            // 从数据库看content需要转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 然后转为对象
            HashMap<String, Object> map = JSONObject.parseObject(content, HashMap.class);
            // 然后把这个map中的东西一一加入到messageVO
            likeNotice.put("user", userService.findUserById((Integer) map.get("userId")));
            likeNotice.put("entityType", map.get("entityType"));
            likeNotice.put("entityId", map.get("entityId"));
            likeNotice.put("postId", map.get("postId"));

            // 查询评论的通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            likeNotice.put("count", count);
            // 查询评论的未读通知数量
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_LIKE);
            likeNotice.put("unreadCount", unreadCount);

            model.addAttribute("likeNotice", likeNotice);
        }
        // 查询关注主题的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> followNotice = new HashMap<>();
            followNotice.put("message", message);
            // 从数据库看content需要转义
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 然后转为对象
            HashMap<String, Object> map = JSONObject.parseObject(content, HashMap.class);
            // 然后把这个map中的东西一一加入到messageVO
            followNotice.put("user", userService.findUserById((Integer) map.get("userId")));
            followNotice.put("entityType", map.get("entityType"));
            followNotice.put("entityId", map.get("entityId"));

            // 查询评论的通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            followNotice.put("count", count);
            // 查询评论的未读通知数量
            int unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_FOLLOW);
            followNotice.put("unreadCount", unreadCount);

            model.addAttribute("followNotice", followNotice);
        }

        // 查询未读私信数量
        int unreadLetterCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        // 查询未读系统通知数量
        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);

        return "/site/notice";
    }

    /**
     * 通知的详细情况
     *
     * @param topic
     * @param model
     * @param page
     * @return 通知的详细情况页面
     */
    @GetMapping("/notice/detail/{topic}")
    public String getDetailNotices(@PathVariable("topic") String topic, Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffSet(), page.getLimit());

        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (notices != null) {
            for (Message notice : notices) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                // 下面这个对于关注列表来说是没有的
                map.put("postId", data.get("postId"));
                // 通知的作者(这里其实是系统)
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);
        // 设置为已读
        List<Integer> unreadLettersIds = getUnreadLettersId(notices);
        if(!unreadLettersIds.isEmpty()) {
            messageService.readMessage(unreadLettersIds);
        }

        return "/site/notice-detail";
    }
}
