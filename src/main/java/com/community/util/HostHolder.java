package com.community.util;

import com.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替sessison对象
 * @author flunggg
 * @date 2020/7/23 16:50
 * @Email: chaste86@163.com
 */
@Component
public class HostHolder {

    private ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public void setUser(User user) {
        threadLocal.set(user);
    }
    public User getUser() {
        return threadLocal.get();
    }

    public void clear() {
        threadLocal.remove();
    }
}
