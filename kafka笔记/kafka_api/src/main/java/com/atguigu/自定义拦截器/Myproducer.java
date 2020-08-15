package com.atguigu.自定义拦截器;

import org.apache.kafka.clients.producer.*;

import java.util.ArrayList;
import java.util.Properties;

public class Myproducer {
    public static void main(String[] args) {
//      1.自定义配置文件
        Properties props = new Properties();
        //props.put("bootstrap.servers","hadoop100:9092");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop100:9092");
        props.put("acks","all");
        props.put("retries", 1);//重试次数
        //自定义拦截器(拦截器会按照配置顺序调用)
        ArrayList<String> interceptor = new ArrayList<String>();
        interceptor.add("com.atguigu.自定义拦截器.myinterceptor");
        interceptor.add("com.atguigu.自定义拦截器.myinterceptor2");
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,interceptor);

        props.put("batch.size", 16384);//批次大小  缓冲区到达16384向缓冲区发生一次
        props.put("linger.ms", 1);//等待时间      缓冲区尚未到达批次大小但已过1ms发生一次
        props.put("buffer.memory", 33554432);//RecordAccumulator缓冲区大小
//       发送ProducerRecord序列化key value
         props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//      2.创建生产者对象
        Producer<String, String> producer = new KafkaProducer(props);
        for (int i = 0; i < 50; i++) {
//        3.封装数据
            ProducerRecord<String, String> pro = new ProducerRecord("aa1", Integer.toString(i), "test"+i);
//        4.发送数据 --异步
            producer.send(pro, new Callback() {
                //回调函数
                public void onCompletion(RecordMetadata recor, Exception e) {
                    System.out.println("分区"+recor.partition()+"=====>topic-->"
                                +recor.topic()+".offset=====>"+recor.offset());
                }
            });
            System.out.println("-------------验证异步性-------------");
        }
//        5.关闭资源
        producer.close();
    }
}
