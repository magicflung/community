package com.community.util;

/**
 * @author flunggg
 * @date 2020/8/6 9:15
 * @Email: chaste86@163.com
 */
public class RedisUtil {

    private static final String SPLIT = ":";
    // 赞
    private static final String PREFIX_ENTITY_LIKE = "like:count";
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注
    private static final String PREFIX_FOLLOWEE = "followee"; // 被关注者
    private static final String PREFIX_FOLLOWER = "follower"; // 关注者（粉丝）

    /**
     * 某一个实体的赞
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }


    /**
     * 某一个用户获得的赞
     * @param userId
     * @return
     */
    public static String getEntityLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体（可以为人，帖子等）
     * 格式：followee：userId：entityType -》 zset（entityId，time）
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体的关注者
     * 格式：follower：entityType：entityId -》 zset（userId，time）
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
