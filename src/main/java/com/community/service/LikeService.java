package com.community.service;

import com.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 点赞的业务
 * @author flunggg
 * @date 2020/8/6 9:18
 * @Email: chaste86@163.com
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 某个用户点赞或取消赞
     * 如果想让所有人（游客）点赞，那么就不需要userId，并且每一个实体只需要一个整型变量来记录点赞次数
     * @param userId 谁点的赞
     * @param entityType 在哪一个类型点赞，比如帖子，评论，评论中的评论
     * @param entityId 哪一张帖子或者哪一个评论
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        // // 判断该用户是否点赞
        // Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        // if(member) {
        //     // 如果已经点过赞，此时就取消点赞
        //     redisTemplate.opsForSet().remove(entityLikeKey, userId);
        // } else {
        //     // 如果还没点过赞，此时就点赞
        //     // 这里传入userId是为了以后可以知道是谁点的赞，如果只是记录一个整型变量也行。
        //     redisTemplate.opsForSet().add(entityLikeKey, userId);
        // }
        // 新增功能：某个用户点赞或取消赞，也顺便记录目标用户收到的赞，需要用到redis事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
                // 目标用户
                String userLikeKey = RedisUtil.getEntityLikeKey(entityUserId);
                // 当前用户有没有对实体点赞
                boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                // 先去查，查完再开启事务，如果把查询放在事务中，那么不会立即得到结果，因为在事务中的命令会被放到队列中，当提交事务时再统一提交。
                operations.multi();
                if(member) {
                    // 如果当前用户已经点过赞，此时就取消点赞
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    // 并且对目标用户获得的赞-1
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    // 如果当前用户还没点过赞，此时就点赞
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    // 并且对目标用户获得的赞+1
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    /**
     *
     * @param entityType
     * @param entityId
     * @return 查询某实体的点赞数量
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return 查询用户的对某实体的点赞状态，为了方便以后扩展使用int，比如未来可能会扩展 踩
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     *
     * @param userId 用户id
     * @return 用户所获得的赞
     */
    public int findEntityUserLikeCount(int userId) {
        String userLikeKey = RedisUtil.getEntityLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
