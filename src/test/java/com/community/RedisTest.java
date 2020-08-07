package com.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

/**
 * @author flunggg
 * @date 2020/8/5 10:59
 * @Email: chaste86@163.com
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class) // 以CommunityApplication.class配置的启动测试
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";
        // 存
        redisTemplate.opsForValue().set(redisKey, 1);
        // 取
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        // 增加
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        // 减少
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";
        // 存
        redisTemplate.opsForHash().put(redisKey, "id", "1");
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");
        // 取
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testList() {
        String redisKey = "test:ids";
        // 存
        redisTemplate.opsForList().leftPush(redisKey, "101");
        redisTemplate.opsForList().leftPush(redisKey, "102");
        redisTemplate.opsForList().leftPush(redisKey, "103");
        // 取
        System.out.println("大小:" + redisTemplate.opsForList().size(redisKey));
        System.out.println("索引为0的value为:" + redisTemplate.opsForList().index(redisKey, 0));
        System.out.println("索引0到2范围内的value" + redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println("从左弹出：" + redisTemplate.opsForList().leftPop(redisKey));
        System.out.println("从左弹出：" + redisTemplate.opsForList().leftPop(redisKey));
        System.out.println("从左弹出：" + redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSet() {
        String redisKey = "test:teachers";
        // 存
        redisTemplate.opsForSet().add(redisKey, "刘备", "张飞", "关羽", "诸葛亮");
        // 取
        System.out.println("大小:" + redisTemplate.opsForSet().size(redisKey));
        System.out.println("随机弹出:" + redisTemplate.opsForSet().pop(redisKey));
        System.out.println("查看set中的全部元素：" + redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortSet() {
        String redisKey = "test:students";
        // 存
        redisTemplate.opsForZSet().add(redisKey, "小红", 100);
        redisTemplate.opsForZSet().add(redisKey, "小明", 30);
        redisTemplate.opsForZSet().add(redisKey, "小张", 90);
        redisTemplate.opsForZSet().add(redisKey, "小皮", 50);
        // 取
        System.out.println("大小:" + redisTemplate.opsForZSet().size(redisKey));
        System.out.println("统计有多少个元素：" + redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println("取出小明的分数：" + redisTemplate.opsForZSet().score(redisKey, "小明"));
        System.out.println("小张的排名：" + redisTemplate.opsForZSet().rank(redisKey, "小张"));
        System.out.println("小张的逆向排名：" + redisTemplate.opsForZSet().reverseRank(redisKey, "小张"));
        System.out.println("0-2范围内正序输出：" + redisTemplate.opsForZSet().range(redisKey, 0, 2));
        System.out.println("0-2范围内逆序输出：" + redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));

    }


    @Test
    public void testKeys() {
        // 删除
        redisTemplate.delete("test:user");
        // 判断某个key是否存在
        System.out.println(redisTemplate.hasKey("test:user"));
        // 设置某个key的过期时间，需要指定单位
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
        System.out.println(redisTemplate.hasKey("test:students"));
    }

    // 多次访问同一个key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        // 绑定
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        // 就不用带上key
        operations.increment();
    }


    // 编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                // 启动事务
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, "1");
                redisOperations.opsForSet().add(redisKey, "2");

                System.out.println(redisOperations.opsForSet().members(redisKey));
                return redisOperations.exec(); // 提交事务
            }
        });

        System.out.println(obj);
    }
}
