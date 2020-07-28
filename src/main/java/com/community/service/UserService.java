package com.community.service;

import com.community.dao.UserMapper;
import com.community.entity.User;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author flunggg
 * @date 2020/7/19 14:46
 * @Email: chaste86@163.com
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    public UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectUserById(id);
    }

    /**
     * 使用Map可以封装多种情况的返回结果
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值判断
        if(user == null) {
            throw new IllegalArgumentException("User为空");
        }
        if(StringUtils.isBlank(user.getUsername())) { // isBlank包含了isEmpty，也包括空格
            map.put("usernameMsg", "用户名不允许为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不允许为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不允许为空");
            return map;
        }

        // 判断是否存在
        User u = userMapper.selectUserByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg", "用户名已存在");
            return map;
        }
        u = userMapper.selectUserByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg", "邮箱已被注册");
            return map;
        }

        // 设置
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setCreateTime(new Date());
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/uuid
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String activationCode) {
        User user = userMapper.selectUserById(userId);
        if(user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if(user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    public int updatePassword(int useId, String password) {
        return userMapper.updatePassword(useId, password);
    }
}
