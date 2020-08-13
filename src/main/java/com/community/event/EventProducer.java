package com.community.event;

import com.alibaba.fastjson.JSONObject;
import com.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息队列---生产者
 * @author flunggg
 * @date 2020/8/7 16:53
 * @Email: chaste86@163.com
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件（本质上就是发消息）
     * @param event 传过来的事件
     */
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        // 把对象转为JSON字符串
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
