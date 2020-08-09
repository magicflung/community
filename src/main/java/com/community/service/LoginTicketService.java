package com.community.service;

import com.community.dao.UserMapper;
import com.community.entity.LoginTicket;
import com.community.entity.User;
import com.community.util.CommunityUtil;
import com.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录凭证，使用数据库来代替Session
 * @author flunggg
 * @date 2020/7/23 9:40
 * @Email: chaste86@163.com
 */
@Service
public class LoginTicketService {

    // 废弃，使用Redis来代替存储
    // @Autowired
    // private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录
     *
     * 重构：使用Redis来代替MySQL存储登录凭证
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        // 验证用户是否存在
        User user = userMapper.selectUserByName(username);
        if(user == null) {
            map.put("usernameMsg", "您输入的用户不存在");
            return map;
        }
        // 否则，判断密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(! user.getPassword().equals(password)) {
            map.put("passwordMsg", "您输入的密码有误");
            return map;
        }
        // 判断是否激活账号
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "您的账号未激活");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // 重构：使用 Redis 代替
        // loginTicketMapper.insertLoginTicket(loginTicket);
        // 存入 Redis
        String loginTicketKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        // 虽然 loginTicket 是一个对象，而opsForValue存入的是字符串，但是在Redis配置类已经设置了，自动序列化为JSON字符串
        redisTemplate.opsForValue().set(loginTicketKey, loginTicket);

        // 我们只需要ticket
        map.put("loginUser", user);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 登出
     * 但是不用删，可能未来可以利用它扩展功能，比如用户登录天数，每一年登录多少天
     * @param ticket 需要无效的登录凭证
     */
    public void logout(String ticket) {
        // 重构
        // loginTicketMapper.updateStatus(ticket, 1);

        // 从 Redis 删掉 ticket
        String loginTicketKey = RedisUtil.getTicketKey(ticket);
        // 先从 Redis取出来
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
        // 设置为1，表示无效
        loginTicket.setStatus(1);
        // 更新
        redisTemplate.opsForValue().set(loginTicketKey, loginTicket);
    }

    public LoginTicket findLoginTicketByTicket(String ticket) {
        // 重构
        // return loginTicketMapper.selectLoginTicketByTicket(ticket);
        // 从 Redis 查
        String loginTicketKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
    }
}
