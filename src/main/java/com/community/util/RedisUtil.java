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
    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户信息
    private static final String PREFIX_USER = "user";

    /**
     * 某一个实体的赞
     * @param entityType
     * @param entityId
     * @return 某一个实体的赞
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }


    /**
     * 某一个用户获得的赞
     * @param userId 某个用户
     * @return 某一个用户获得的赞
     */
    public static String getEntityLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体（可以为人，帖子等）
     * 格式：followee：userId：entityType -》 zset（entityId，time）
     * @param userId 某个用户
     * @param entityType 实体类型
     * @return 某个用户关注的实体的key
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

    /**
     * 登录验证码
     * 每一个用户的验证码都是不一样的，需要识别，但是没登录之前也不知道是哪一个用户，所有用一个临时凭证来识别用户
     * @param owner 临时凭证
     * @return 登录验证码key
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录凭证
     * @param ticket
     * @return 登录凭证的key
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户信息
     * @param userId
     * @return
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}