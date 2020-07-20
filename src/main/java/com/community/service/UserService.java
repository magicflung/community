package com.community.service;

import com.community.dao.UserMapper;
import com.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author flunggg
 * @date 2020/7/19 14:46
 * @Email: chaste86@163.com
 */
@Service
public class UserService {

    @Autowired
    public UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
}
