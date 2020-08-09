package com.community.controller;

import com.community.entity.Event;
import com.community.entity.User;
import com.community.event.EventProducer;
import com.community.service.LikeService;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flunggg
 * @date 2020/8/6 9:31
 * @Email: chaste86@163.com
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    /**
     * 异步请求
     * @param entityType 点赞的是帖子还是帖子的评论
     * @param entityId 该帖子id或者帖子的评论id
     * @param entityUserId 该帖子或评论的作者id
     *
     * 新增：因为创建事件需要使用到是哪一篇帖子
     * 虽然entityId也会带上帖子id，但是如果点赞的是帖子中的评论，那么entityId带上的是评论的id
     * 这里去查询也行不过太麻烦了，让前端在传的时候顺便把帖子id传过来
     * 所以得单独传入postId
     * @param postId
     * @return
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        // 当前登录的用户
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);

        // 然后返回点赞状态
        // 更新点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 查询点赞状态，然后前端可以根据状态变成已赞或者赞
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        if(likeStatus == 1) {
            // 点赞后触发事件，通知目标用户
            // 需要带上帖子id
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(200, null, map);
    }
}
