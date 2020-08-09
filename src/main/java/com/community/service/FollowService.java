package com.community.service;

import com.community.entity.User;
import com.community.util.CommunityConstant;
import com.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 关注功能的业务
 * @author flunggg
 * @date 2020/8/6 13:56
 * @Email: chaste86@163.com
 */
@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;
    /**
     * 关注
     * 因为两个操作（关注和被关注）得都成功，要不就都失败。所以使用事务
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 用户userId关注的实体
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                // 实体的关注者（粉丝）
                String followerKey = RedisUtil.getFollowerKey(entityType, entityId);

                // 事务
                operations.multi();

                // 用户userId关注的实体, 需要传入是哪一个实体
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // 实体的关注者 需要传入是哪一个粉丝
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 用户userId关注的实体
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                // 实体的关注者（粉丝）
                String followerKey = RedisUtil.getFollowerKey(entityType, entityId);

                // 事务
                operations.multi();

                // 用户userId关注的实体, 需要传入是哪一个实体
                operations.opsForZSet().remove(followeeKey, entityId);
                // 实体的关注者 需要传入是哪一个粉丝
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    /**
     * @param userId
     * @param entityType
     * @return 查询某个用户关注的实体数量，这里只是查人
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     *
     * @param entityType
     * @param entityId
     * @return 查询某个实体的粉丝数量，这里只是查人
     */
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return 查询当前用户是否关注该实体
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     *
     * @param userId
     * @param offset
     * @param limit
     * @return 查询某个用户的关注列表
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        // 这里的实体可以明确指定为 人
        String followeeKey = RedisUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        // 通过key查询该用户 关注的所有用户的id, 并且是逆序的（因为是按关注时间来排序）
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if(targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // 并把哪时候关注这个用户的时间拿出来
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));

            list.add(map);
        }

        return list;
    }

    /**
     *
     * @param userId
     * @param offset
     * @param limit
     * @return 查询某个用户的粉丝列表
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        // 这里的实体可以明确指定为 人
        String followerKey = RedisUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        // 通过key查询该用户 的所有粉丝的id, 并且是逆序的（因为是按关注时间来排序）
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if(targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            // 并把哪时候关注这个用户的时间拿出来
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));

            list.add(map);
        }

        return list;
    }
}
