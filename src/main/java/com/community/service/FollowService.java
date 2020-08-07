package com.community.service;

import com.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 关注功能的业务
 * @author flunggg
 * @date 2020/8/6 13:56
 * @Email: chaste86@163.com
 */
@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

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
                String followee = RedisUtil.getFolloweeKey(userId, entityType);
                // 实体的关注者（粉丝）
                String follower = RedisUtil.getFollowerKey(entityType, entityId);

                // 事务
                operations.multi();

                // 用户userId关注的实体, 需要传入是哪一个实体
                operations.opsForZSet().add(followee, entityId, System.currentTimeMillis());
                // 实体的关注者 需要传入是哪一个粉丝
                operations.opsForZSet().add(follower, userId, System.currentTimeMillis());

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
                String followee = RedisUtil.getFolloweeKey(userId, entityType);
                // 实体的关注者（粉丝）
                String follower = RedisUtil.getFollowerKey(entityType, entityId);

                // 事务
                operations.multi();

                // 用户userId关注的实体, 需要传入是哪一个实体
                operations.opsForZSet().remove(followee, entityId);
                // 实体的关注者 需要传入是哪一个粉丝
                operations.opsForZSet().remove(follower, userId);

                return operations.exec();
            }
        });
    }

    /**
     * 如果以后要查关注帖子数量最好单独再写一个
     * @param userId
     * @param entityType
     * @return 查询某个用户关注的实体数量，这里只是查人
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followee = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followee);
    }

    /**
     *
     * @param entityType
     * @param entityId
     * @return 查询某个实体的粉丝数量，这里只是查人
     */
    public long findFollowerCount(int entityType, int entityId) {
        String follower = RedisUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(follower);
    }

    /**
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return 查询当前用户是否关注该实体
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followee = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followee, entityId) != null;
    }
}
