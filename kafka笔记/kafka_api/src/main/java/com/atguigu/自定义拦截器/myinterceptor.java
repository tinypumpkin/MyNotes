package com.atguigu.自定义拦截器;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * 作用: value前加一个时间戳
 */
public class myinterceptor implements ProducerInterceptor<String,String> {
    /**
     * 核心方法 处理生产者发来的数据
     */
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        //String topic, Integer partition, Long timestamp, K key, V value, Iterable<Header> headers
        return new ProducerRecord<String, String>(record.topic(),record.partition(), record.timestamp(), record.key(),
                System.currentTimeMillis()+record.value(),record.headers());
    }

    /**
     * broker --ack(调用拦截器方法onAcknowledgement)-->生产者
     * @param recordMetadata
     * @param e
     */
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    /**
     * 调用producer.close()方法时会调用此方法
     */
    public void close() {

    }

    /**
     * 读取配置文件的内容
     * @param map
     */
    public void configure(Map<String, ?> map) {

    }
}
