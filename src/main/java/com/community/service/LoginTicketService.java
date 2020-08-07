package com.community.service;

import com.community.dao.LoginTicketMapper;
import com.community.dao.UserMapper;
import com.community.entity.LoginTicket;
import com.community.entity.User;
import com.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

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
        loginTicketMapper.insertLoginTicket(loginTicket);
        // 我们只需要ticket
        map.put("loginUser", user);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicketByTicket(String ticket) {
        return loginTicketMapper.selectLoginTicketByTicket(ticket);
    }
}
