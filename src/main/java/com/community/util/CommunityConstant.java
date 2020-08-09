package com.community.util;

/**
 * @author flunggg
 * @date 2020/7/21 18:10
 * @Email: chaste86@163.com
 */
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认的登录凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600; // 一小时

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 12; // 一天

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：回复
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 引入ES，新增发帖事件
     * 主题：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 引入ES，删除发帖事件
     * 主题：删除
     */
    String TOPIC_DELETE = "delete";


    /**
     * 系统用户ID：1
     */
    int SYSTEM_USER_ID = 1;


    /**
     * 权限：普通用户
     */
    String AUTHOURTTY_USER = "user";

    /**
     * 权限：管理员
     */
    String AUTHOURTTY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AUTHOURTTY_MODERATOR = "moderator";
}
