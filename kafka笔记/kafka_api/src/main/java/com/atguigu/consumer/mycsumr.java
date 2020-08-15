package com.atguigu.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * 自定义消费者--自动提交offset
 */
public class mycsumr {
    public static void main(String[] args) {
//       1.创建配置信息
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop100:9092");
        //消费者组
        props.put("group.id", "g1");
        //自动提交offset(自动提交offset由broker保存)
        props.put("enable.auto.commit", "true");
        //提交offset周期
        props.put("auto.commit.interval.ms", "1000");
        //key ,value 的序列类型
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

//       2.创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer(props);
//       2.1订阅主题
        consumer.subscribe(Arrays.asList("aa1"));
//       3.拉取数据
        while (true) {
            //若拉取数据时没有数据则等待1s
            ConsumerRecords<String, String> records = consumer.poll(1000);
            //遍历数据
            for (ConsumerRecord<String, String> record : records)
                System.out.printf("分区 = %s,offset = %d, key = %s, value = %s%n",record.partition(), record.offset(), record.key(), record.value());
        }
    }
}
