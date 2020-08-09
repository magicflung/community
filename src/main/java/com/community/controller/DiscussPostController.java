package com.community.controller;

import com.community.entity.Comment;
import com.community.entity.DiscussPost;
import com.community.entity.Event;
import com.community.entity.User;
import com.community.event.EventProducer;
import com.community.service.CommentService;
import com.community.service.DiscussPostService;
import com.community.service.LikeService;
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

import java.util.*;

/**
 * @author flunggg
 * @date 2020/7/28 11:40
 * @Email: chaste86@163.com
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 异步添加帖子
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "您还未登录噢！");
        }
        if(StringUtils.isBlank(title)) {
            return CommunityUtil.getJSONString(403, "标题不能为空");
        }
        if(StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(403, "内容不能为空");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 引入ES
        // 触发发帖子事件，把事件存入ES
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST) // 这里肯定是帖子，所有直接传
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);


        return CommunityUtil.getJSONString(200, "发布成功！");
    }

    /**
     * 本来可以使用联合查询，把帖子和用户信息一起查出来，速度快点，但是缺点就是有耦合
     * 另一种就是：先查帖子，再根据帖子的用户id去查用户信息，后面会使用redis来提高速度
     * @param discussPostId 帖子id
     * @param model
     * @param page 要分页就需要用，然后封装进model
     * @return 帖子的详细页面
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 用户
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 当前用户的点赞状态，如果赞过就前端显示已赞，但是这里还得处理没登陆的状态，因为没登录也可以看点赞数量但不能改变状态。
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        // 评论
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        // 帖子在的评论总数，不计评论楼中楼
        page.setRows(post.getCommentCount());
        // 分页查询帖子中的评论
        List<Comment> postComment = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPostId, page.getOffSet(), page.getLimit());
        // 一个评论还需要包含用户信息，所以用Map来存储，但是有多个评论
        List<Map<String, Object>> commentAndUserList = new ArrayList<>();
        if(postComment != null) {
            for(Comment comment : postComment) {
                Map<String, Object> commentAndUser = new HashMap<>();
                // 评论
                commentAndUser.put("comment", comment);
                // 评论的用户
                commentAndUser.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentAndUser.put("likeCount", likeCount);
                // 当前用户的点赞状态，如果赞过就前端显示已赞，但是这里还得处理没登陆的状态，因为没登录也可以看点赞数量但不能改变状态。
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentAndUser.put("likeStatus", likeStatus);
                // 还有楼中楼评论，需要的是评论的id, 不需要分页，全部查出
                List<Comment> replyComment = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 楼中楼评论还包含用户，有多个楼中楼
                List<Map<String, Object>> replyAndUserList = new ArrayList<>();
                if(replyComment != null) {
                    for(Comment reply : replyComment) {
                        Map<String, Object> replyAndUser = new HashMap<>();
                        // 回复
                        replyAndUser.put("reply", reply);
                        // 回复的用户
                        replyAndUser.put("replyUser", userService.findUserById(reply.getUserId()));
                        // 但是回复的时候，可以回复层主或者回复层中的用户，需要判断回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyAndUser.put("replyTargetUser", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyAndUser.put("likeCount", likeCount);
                        // 当前用户的点赞状态，如果赞过就前端显示已赞，但是这里还得处理没登陆的状态，因为没登录也可以看点赞数量但不能改变状态。
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyAndUser.put("likeStatus", likeStatus);

                        replyAndUserList.add(replyAndUser);
                    }
                }
                commentAndUser.put("replies", replyAndUserList);
                // 每一个小评论的回复的数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentAndUser.put("replyCount", replyCount);

                // 最后，把评论添加回评论列表
                commentAndUserList.add(commentAndUser);
            }
        }
        model.addAttribute("comments", commentAndUserList);

        return "/site/discuss-detail";
    }

    /**
     * 置顶
     * @param id 帖子id
     */
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 引入ES
        // 触发发帖子事件，把事件存入ES
        // 更新
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST) // 这里肯定是帖子，所有直接传
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(200);
    }

    /**
     * 加精
     * @param id 帖子id
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 引入ES
        // 触发发帖子事件，把事件存入ES
        // 更新
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST) // 这里肯定是帖子，所有直接传
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(200);
    }

    /**
     * 删除
     * @param id 帖子id
     */
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 引入ES
        // 触发删除帖子事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST) // 这里肯定是帖子，所有直接传
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(200);
    }
}
