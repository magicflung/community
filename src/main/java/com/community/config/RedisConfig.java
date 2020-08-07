package com.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author flunggg
 * @date 2020/8/5 10:48
 * @Email: chaste86@163.com
 */
@Configuration
public class RedisConfig {

    /**
     *
     * @param factory springboot会自动注入
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 访问数据库之前，得先连接
        template.setConnectionFactory(factory);

        // 因为这是java类型数据，跟redis类型不一定，所以需要指定序列化的方式
        // 设置普通的key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置普通value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        // redis的hash比较特殊，因为它的value还有key，回忆下指令：hset key field value
        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        // 当设置完，为了让template中的参数生效，使用下面方法触发一下，表示生效。
        template.afterPropertiesSet();

        return template;
    }
}
