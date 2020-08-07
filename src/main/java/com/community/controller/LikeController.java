package com.community.controller;

import com.community.entity.User;
import com.community.service.LikeService;
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
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 异步请求
     * @param entityType
     * @param entityId
     * @return
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId) {
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

        return CommunityUtil.getJSONString(200, null, map);
    }
}
