package com.community.controller;

import com.community.annotation.LoginRequired;
import com.community.entity.User;
import com.community.service.FollowService;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author flunggg
 * @date 2020/8/6 14:03
 * @Email: chaste86@163.com
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/follow")
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(200, "已关注！");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(200, "已取消关注！");
    }

}
