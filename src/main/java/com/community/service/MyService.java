package com.community.service;

import com.community.entity.User;
import com.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

/**
 * @author flunggg
 * @date 2020/7/29 9:40
 * @Email: chaste86@163.com
 */
@Service
public class MyService {

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * propagation:事务交叉一起的问题，以哪一个事务为准
     * Propagation.REQUIRED:支持外部事务（A调B，A就是当前事务，而A对于B来说就是外部事务），如果没有外部事务，则创建，按照B的事务为准
     * Propagation.REQUIRES_NEW: 创建一个新事务，并暂停当前事务（A调B，无论A有没有事务，都按照B的事务来）
     * Propagation.NESTED:如果存在外部事务，则嵌套在该事务中执行（有独立的提交和回滚），如果外部事务不存在，则跟REQUIRED一样
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object va1() {
        User user = new User();
        user.setUsername("fff");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5).toString());
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("123@163.com");
        user.setCreateTime(new Date());
        userService.register(user);

        discussPostService.addDiscussPost(user.getId(), "hello", "hahaha");

        Integer.valueOf("abc");

        return "ok";
    }

    /**
     * 可以局部添加事务
     */
    @Autowired
    TransactionTemplate transactionTemplate;

    public Object val2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute((transactionStatus)-> {
            User user = new User();
            user.setUsername("fff");
            user.setSalt(CommunityUtil.generateUUID().substring(0,5).toString());
            user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
            user.setEmail("123@163.com");
            user.setCreateTime(new Date());
            userService.register(user);

            discussPostService.addDiscussPost(user.getId(), "hello", "hahaha");

            Integer.valueOf("abc");

            return "ok";
        });

        // return transactionTemplate.execute(new TransactionCallback<Object>() {
        //     @Override
        //     public Object doInTransaction(TransactionStatus transactionStatus) {
        //         User user = new User();
        //         user.setUsername("fff");
        //         user.setSalt(CommunityUtil.generateUUID().substring(0,5).toString());
        //         user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        //         user.setEmail("123@163.com");
        //         user.setCreateTime(new Date());
        //         userService.register(user);
        //
        //         discussPostService.addDiscussPost(user.getId(), "hello", "hahaha");
        //
        //         Integer.valueOf("abc");
        //
        //         return "ok";
        //     }
        // });
    }
}
