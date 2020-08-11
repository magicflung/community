package com.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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

    // HyperLogLog
    // 统计40w个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redisKey = "test:h11:01";

        for(int i = 1; i < 200000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for(int i = 1; i < 200000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, (int)(Math.random() * 200000) + 1);
        }

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    // 合并：将3组数据合并，在统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:h11:02";


        for(int i = 1; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:h11:02";
        for(int i = 5001; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:h11:03";
        for(int i = 10001; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:h11:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    // 统一一组布尔值
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);


        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));;
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));;
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));;

        // 统计
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(execute);
    }

    // 统计三组数据的布尔值，并进行OR运算
    @Test
    public void testBigMapOperation() {
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        redisTemplate.opsForValue().setBit(redisKey3, 5, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String redisKey = "test:bm:or";
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                /*只有设置后重复的才会进行OR*/
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(execute);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey2, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey2, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey2, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey3, 4));
    }
}
