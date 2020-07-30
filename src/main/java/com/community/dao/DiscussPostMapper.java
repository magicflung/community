package com.community.dao;

import com.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author flunggg
 * @date 2020/7/19 14:16
 * @Email: chaste86@163.com
 */
@Mapper
public interface DiscussPostMapper {

    /**
     * @param userId 用户id，如果为0则表示全部用户
     * @param offset 行数
     * @param limit 一页多少条
     * @return 返回一个论贴列表
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);


    /**
     * @Param注解用于给参数取别名，如果只有一个参数，并且在<if>里面使用，则必须取别名
     *
     * @param userId 用户id，如果为0则表示全部用户
     * @return 返回论贴一共有多少条
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    DiscussPost selectDiscussPostById(int id);

    int insertDiscussPost(DiscussPost discussPost);

    int updateCommentCount(int id, int commentCount);
}
