package com.community.controller;

import com.community.annotation.LoginRequired;
import com.community.entity.Event;
import com.community.entity.User;
import com.community.event.EventProducer;
import com.community.service.FollowService;
import com.community.service.UserService;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.HostHolder;
import com.community.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author flunggg
 * @date 2020/8/6 14:03
 * @Email: chaste86@163.com
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        // 当前登录的用户
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        // 关注后，触发事件:通知关注的用户
        // 目前只有关注人，未来可以扩展为关注帖子，那么需要修改setEntityId
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 因为关注的只是人，所以entityId是人，就可以直接用
        eventProducer.fireEvent(event);

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

    /**
     *
     * @param userId 某个用户
     * @param page 分页
     * @param model
     * @return 某个用户的关注列表
     */
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        // 获取该用户的关注列表
        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffSet(), page.getLimit());
        // 然后还得看看我们自己有没有关注该用户的关注列表的用户
        if(followees != null) {
            for(Map<String, Object> followee : followees) {
                User targetUser = (User) followee.get("user");
                followee.put("hasFollowed", hasFollowed(targetUser.getId()));
            }
        }
        model.addAttribute("followees", followees);

        return "/site/followee";
    }

    /**
     *
     * @param userId 某个用户
     * @param page 分页
     * @param model
     * @return 某个用户的关注列表
     */
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        // 获取该用户的关注列表
        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffSet(), page.getLimit());
        // 然后还得看看我们自己有没有关注该用户的关注列表的用户
        if(followers != null) {
            for(Map<String, Object> followee : followers) {
                User targetUser = (User) followee.get("user");
                followee.put("hasFollowed", hasFollowed(targetUser.getId()));
            }
        }
        model.addAttribute("followers", followers);

        return "/site/follower";
    }

    /**
     *
     * @param entityId 目标用户
     * @return 当前登录用户是否关注目标用户
     */
    private boolean hasFollowed(int entityId) {
        if(hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, entityId);
    }
}
