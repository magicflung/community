package com.community.controller;

import com.community.entity.User;
import com.community.service.LoginTicketService;
import com.community.service.UserService;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.CookieUtil;
import com.community.util.RedisUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author flunggg
 * @date 2020/7/21 10:05
 * @Email: chaste86@163.com
 */
@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * @return 注册页面
     */
    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * @return 登录页面
     */
    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * @param model
     * @param user 只要表单的name对应的值相匹配就会封装成为User
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);

        if(map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经像您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }



    /**
     * http://localhost:8080/community/activation/101/uuid
     * @param model 传递数据
     * @param userId 用户id
     * @param activationCode 激活码
     * @return 激活
     */
    @GetMapping("/activation/{userId}/{activationCode}")
    public String activation(Model model,
                             @PathVariable("userId") int userId, @PathVariable("activationCode") String activationCode) {
        int activation = userService.activation(userId, activationCode);
        if(activation == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功");
            model.addAttribute("target", "/login");
        } else if(activation == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，你的账号已经激活了");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 验证码图片
     * @param response 需要把图片推送到浏览器，得借助rsponse
     * 之前：session  需要保存图片的验证码文本，可以验证传过来的验证码与生成的验证码的文本是否正确，因为是重要信息，所以得保存在session
     * 原先把验证码存到session中，现在重构，存入Redis
     */
    @GetMapping("/kaptcha")
    public void kaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha", text);

        // 重构：把验证码存入Redis
        // 先创建一个临时凭证
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60); // 60s
        cookie.setPath(contextPath); // 有效路径
        response.addCookie(cookie);
        // 将验证码存入Redis
        String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出到浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
            // 这个流可以不用关闭，SpringMvc会自动关
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }

    }

    /**
     * 登录
     * @param username 用户名
     * @param password 用户密码
     * @param rememberme 是否记住当前的登录信息，记住的话延迟登录凭证
     * @param code 前端传来的验证码
     * @param model 传递数据
     * @param response 保存cookie
     * @return 成功就跳转到首页，否则跳转到登录页重新登录
     */
    @PostMapping("/login")
    public String login(String username, String password, boolean rememberme, String code,
                        Model model, HttpServletRequest request, HttpServletResponse response) {
        // 之前是从session中取验证码
        // 先判断验证码信息
        // String kaptcha = (String) session.getAttribute("kaptcha");


        // 现在从Redis中取
        String kaptcha = null;
        // 获取临时凭证，通过临时凭证来获取校验码。
        // 需要注意，因为是临时的，所以如果过去就取不到，如果使用@CookieValue注解取获取kaptchaOwner，获取不到就报错，所以采用下面的方式。
        String kaptchaOwner = CookieUtil.getValue(request, "kaptchaOwner");
        // 如果没有失效
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        } else {
            model.addAttribute("codeMsg", "验证码已失效，请刷新验证码");
            return "/site/login";
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }

        // 检查账号和密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = loginTicketService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }


    /**
     * 登出
     * @param ticket
     * @return
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket")String ticket) {
        loginTicketService.logout(ticket);

        // 清理认证结果
        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }
}