package com.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flunggg
 * @date 2020/7/18 14:14
 * @Email: chaste86@163.com
 */
@Controller
public class MyController {

    @RequestMapping(path = "/test1", method = RequestMethod.GET)
    @ResponseBody
    public String test1(@RequestParam(name = "id") int id,
                        @RequestParam(name = "name", required = false, defaultValue ="张三") String name) {
        System.out.println(id);
        System.out.println(name);
        return "success";
    }

    @RequestMapping(path = "/test2/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String test2(@PathVariable("id") int id) {
        System.out.println(id);
        return "success";
    }


    /**
     * 不加@ResponseBody返回html，加了@ResponseBody返回的是字符串
     * @param name
     * @param password
     * @return
     */
    @RequestMapping(path = "/test3", method = RequestMethod.POST)
    @ResponseBody
    public String test3(String name, String password) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", name);
        modelAndView.addObject("password", password);
        modelAndView.setViewName("/my");
        return "success";
    }


    /*------------------------响应HTML-------------------------------*/

    @RequestMapping(path = "/test4", method = RequestMethod.GET)
    public ModelAndView test4() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "xiao");
        modelAndView.addObject("password", "1234");
        modelAndView.setViewName("/my"); // 需要配置视图路径
        return modelAndView;
    }

    /**
     *
     * @param model DispatcherServlet会发现这个类然后自动实例化，会把传过来的数据封装进Model，我们也可以通过这个对象把数据传给前端
     * @return 可以直接跳转，比上面简单
     *
     * ModelAndView需要自己实例化
     */
    @RequestMapping(path = "/test5", method = RequestMethod.GET)
    public String test5(Model model) {
        model.addAttribute("name", "周呃呃呃");
        model.addAttribute("password", "12344");
        return "/my";
    }

    /**
     * 响应JSON数据，通常是在异步请求当中
     * 比如：注册判断用户名是否存在
     * java对象 ---》JSON字符串 ---》 JS对象
     * @return
     */
    @RequestMapping(path = "/test6", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> test6() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("password", "12344455");
        map.put("gae", 12);
        return map;
    }

    @RequestMapping(path = "/test7", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> test7() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("password", "12344455");
        map.put("gae", 12);
        list.add(map);

        map = new HashMap<>();
        map.put("name1", "张三1");
        map.put("password", "2112344455");
        map.put("gae", 112);
        list.add(map);

        return list;
    }
}
