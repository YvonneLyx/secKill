package com.miaosha.mq;

//import io.lettuce.core.dynamic.annotation.Value;
import com.alibaba.fastjson.JSON;
import com.miaosha.dao.ItemDOMapper;
import com.miaosha.dao.ItemStockDOMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
//@ConfigurationProperties(prefix="mq",ignoreInvalidFields=true, ignoreUnknownFields=true)
//@PropertySource("classpath:application.yml")
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    //导包错了
    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topic.name}")
    private String topicName;


    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(topicName,"*");

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                Message msgs = list.get(0);
                String jsonString = new String(msgs.getBody());
                Map<String, Object> map = JSON.parseObject(jsonString);

                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");

                itemStockDOMapper.decreaseStock(itemId, amount);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

    }
}
