package com.community.controller;

import com.community.entity.DiscussPost;
import com.community.entity.User;
import com.community.service.DiscussPostService;
import com.community.service.UserService;
import com.community.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    /**
     * 查询论贴列表
     * @param model 前后端传递数据
     * @return 返回到index.html
     */
    @GetMapping({"/index", "/"})
    public String getIndexPage(Model model, Page page) {
        // 方法调用前，SpringMvc会自动实例化Model和Page，并将Page注入到Model
        // 所以，在thymeleaf可以直接访问Page对象中的数据
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffSet(), page.getLimit());
        // 因为还得根据DiscussPost中的userId去查出用户名，所以使用map将一个DiscussPost对应的用户名封装在一起。
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(discussPosts != null) {
            for(DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }
}
