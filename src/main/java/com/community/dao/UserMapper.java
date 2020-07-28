package com.community.dao;

import com.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author flunggg
 * @date 2020/7/18 20:00
 * @Email: chaste86@163.com
 */
// 也可以用@Repository，如果不加这个，可能在@Autowise时会红色，但是可以允许，如果不想红色，可以加上去，同时有两个注解
// 因为@Mapper是Mybatis的注解，他的作用是表明该类是Mapper，Reponsity是Spring的注解，他的作用的是标明该类是个bean
@Mapper
public interface UserMapper {

    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);
}
