package com.community.dao;

import com.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * 如果要写上动态代码，比如if
 * 得在sql外面加上<script></script>
 * 比如：
 *     @Update({
 *             "<script>",
 *             "update login_ticket set status = #{status} where ticket = #{ticket}"
 *             "<if test=\"ticket!=null\">",
 *             "...."
 *             "</if>",
 *             "</script>"
 *     })
 * @author flunggg
 * @date 2020/7/23 9:15
 * @Email: chaste86@163.com
 */
@Mapper
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    LoginTicket selectLoginTicketByTicket(String ticket);

    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(String ticket, int status);
}
