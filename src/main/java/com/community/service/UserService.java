package com.community.service;

import com.community.dao.UserMapper;
import com.community.entity.User;
import com.community.util.CommunityConstant;
import com.community.util.CommunityUtil;
import com.community.util.MailClient;
import com.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    // 模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;




    /**
     * 原先是一直从MySQL查
     * 引入 Redis
     * 现在查询一个用户信息的步骤：
     * 1. 先从 Redis 查
     * 2. 查不到就到 MySQL查，并缓存到 Redis
     *
     * @param id
     * @return
     */
    public User findUserById(int id) {
        // return userMapper.selectUserById(id);
        // 重构
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }


    /**
     * 使用Map可以封装多种情况的返回结果
     *
     * @param user
     * @return 注册
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值判断
        if (user == null) {
            throw new IllegalArgumentException("User为空");
        }
        if (StringUtils.isBlank(user.getUsername())) { // isBlank包含了isEmpty，也包括空格
            map.put("usernameMsg", "用户名不允许为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不允许为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不允许为空");
            return map;
        }

        // 判断是否存在
        User u = userMapper.selectUserByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "用户名已存在");
            return map;
        }
        u = userMapper.selectUserByEmail(user.getEmail());
        if (u != null) {
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

    /**
     * @param userId
     * @param activationCode
     * @return 激活状态
     */
    public int activation(int userId, String activationCode) {
        User user = userMapper.selectUserById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            // 这里更新了数据，那么让该 User从 Redis中清除
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * @param userId
     * @param headerUrl
     * @return 更新头像
     */
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        // 只要更新就清空缓存
        clearCache(userId);
        return rows;
    }

    /**
     * @param userId
     * @param password
     * @return 更新密码
     */
    public int updatePassword(int userId, String password) {
        int rows = userMapper.updatePassword(userId, password);
        // 只要更新就清空缓存
        clearCache(userId);
        return rows;
    }

    public int updatePassword(String email, String password) {

        User user = userMapper.selectUserByEmail(email);

        password = CommunityUtil.md5(password + user.getSalt());

        int rows = userMapper.forgetPassword(email, password);
        return rows;
    }

    /**
     * @param username
     * @return 根据用户名查用户
     */
    public User findUserByName(String username) {
        return userMapper.selectUserByName(username);
    }

    /*---------------使用 Redis 来缓存用户信息-----------------*/

    /**
     * 先用缓存从取用户信息
     *
     * @param userId
     * @return
     */
    public User getCache(int userId) {
        String userKey = RedisUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * 如果缓存中没有该用户，那么先去数据库查出来缓存到 Redis
     *
     * @param userId
     * @return
     */
    public User initCache(int userId) {
        // 先从MySQL查User
        User user = userMapper.selectUserById(userId);
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 数据更改时从缓存中清除，所以上面有哪些调用update的都需要清空
     * 虽然可以从缓存中更新但可能会有并发问题
     *
     * @param userId
     */
    public void clearCache(int userId) {
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    /*---------------------引入Spring Security需要的业务逻辑--------------------------*/

    /**
     * 根据用户userId来查询它的权限
     *
     * @param userId
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHOURTTY_ADMIN;
                    case 2:
                        return AUTHOURTTY_MODERATOR;
                    default:
                        return AUTHOURTTY_USER;
                }
            }
        });

        return list;
    }


    /**
     * 忘记密码：发送验证码邮件
     *
     * @param email
     * @return 注册
     */
    public void verifycode(String email, String verifycode) {
        // 发送验证码邮件
        // 不用管邮箱是否存在
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verifycode", verifycode);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "忘记密码", content);

    }


}
