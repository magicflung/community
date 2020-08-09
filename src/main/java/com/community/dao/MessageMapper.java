package com.community.dao;

import com.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author flunggg
 * @date 2020/8/1 11:58
 * @Email: chaste86@163.com
 */
@Mapper
public interface MessageMapper {

    // 查询当前用户的私信列表，针对每一条私信只返回一条最新的显示在网页上
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的所有私信数量
    int selectConversationCount(int userId);

    // 查询某个私信中的内容列表（私信详情）
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个私信所包含的信息总数量
    int selectLetterCount(String conversationId);

    // 查询未读的私信数量，
    // 如果单传入userId可查一个用户的所有私信未读的数量，
    // 如果传入userId和conversationId查一个私信内的未读的数量
    int selectUnreadLetterCount(int userId, String conversationId);

    // 新增私信
    int insertMessage(Message message);

    // 修改私信的状态，可以改为已读和删除
    int updateStatus(List<Integer> ids, int status);

    // 查询关于某个主题下最新的通知(目前就三个主题：点赞主题，关注主题，评论主题，所以还不用分页)
    Message selectLatestNotice(int userId, String topic);

    // 查询关于某个主题下的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询关于某个主题下未读的通知数量
    // 如果不传topic，那么就查所有未读的通知
    int selectUnreadNoticeCount(int userId, String topic);

    // 查询某个主题下所有的通知，需要分页
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
