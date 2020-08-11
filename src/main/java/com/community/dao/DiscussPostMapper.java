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
     * // 代码重构
     * @param orderMode 0:表示按type倒序然后再轮到create_time倒序；1：表示按照按type倒序然后score倒序最后再轮到create_time倒序
     * @return 返回一个论贴列表
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);


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

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);

}
