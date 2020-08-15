package com.atguigu.自定义拦截器;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * 用来统计发送成功失败数量
 */
public class myinterceptor2 implements ProducerInterceptor<String,String> {
    int succ;
    int fail;
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        return producerRecord;
    }

    /**
     * 判断exception是否为null
     * @param recordMetadata
     * @param e
     */
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {
        if (e==null){ //无异常
            succ++;
        }
        else { //有异常
            fail++;
        }
    }
    //producer关闭时输出
    public void close() {
        System.out.println("成功的数量"+succ);
        System.out.println("失败的数量"+fail);
    }

    public void configure(Map<String, ?> map) {

    }
}
