package com.community.controller;

import com.community.entity.DiscussPost;
import com.community.entity.User;
import com.community.service.DiscussPostService;
import com.community.service.LikeService;
import com.community.service.UserService;
import com.community.util.CommunityConstant;
import com.community.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flunggg
 * @date 2020/7/19 14:53
 * @Email: chaste86@163.com
 */
@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 查询论贴列表
     * @param model 前后端传递数据
     * @param page 分页
     * @param orderMode 代码重构：新增热榜模块
     * @return 返回到index.html
     */
    @GetMapping({"/index", "/"})
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        // 方法调用前，SpringMvc会自动实例化Model和Page，并将Page注入到Model
        // 所以，在thymeleaf可以直接访问Page对象中的数据
        // 所有实体类都会注入到Model吗
        page.setRows(discussPostService.findDiscussPostRows(0));
        // 代码重构：新增orderMode
        page.setPath("/index?orderMode=" + orderMode);

        // discussPostService代码重构，新增orderMode参数，0表示跟原先的排序模式是一样的
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffSet(), page.getLimit(), orderMode);
        // 因为还得根据DiscussPost中的userId去查出用户名，所以使用map将一个DiscussPost对应的用户名封装在一起。
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(discussPosts != null) {
            for(DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                // 查询帖子的赞
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        // 代码重构：
        model.addAttribute("orderMode", orderMode);

        return "/index";
    }

    // 虽然springboot遇到异常会自动跳到500
    // 但是现在统一异常处理，在出现异常时，先将异常加入日志，然后再重定向到500页面
    // 因为是人为跳转，所以得写一个路径可以跳转到500
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    // 拒绝访问时的提示页面
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}
