package com.community.controller;

import com.community.entity.Comment;
import com.community.service.CommentService;
import com.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author flunggg
 * @date 2020/7/30 9:15
 * @Email: chaste86@163.com
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;
    /**
     * 添加帖子评论
     * @param discussPostId 重定向需要的id
     * @param comment 需要自己补充useId，date，status
     * @return 评论后重定向到当前帖子，所以需要discussPostId
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment) {
        if(hostHolder.getUser() == null) {
            return "redirect:/login";
        }
        // 这里如果没有登录会出错，后面会统一处理
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);

        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
