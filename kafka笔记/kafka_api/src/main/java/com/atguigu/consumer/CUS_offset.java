package com.atguigu.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.io.*;
import java.util.*;

/**
 * 自定义offset存储
 * 让offset存储在consumer本地,消费一个存一个offset,中途若宕机重启后可根据本地offset接下去消费
 */
public class CUS_offset {
    private static Map<TopicPartition, Long> offset = new HashMap<TopicPartition, Long>();
    private static String file = "d:/offset";

    public static void main(String[] args) throws IOException {
        //1. 实例化Consumer对象
        Properties properties = new Properties();
        properties.load(Consumer.class.getClassLoader().getResourceAsStream("consumer1.properties"));
        final KafkaConsumer<String, String> consumer =
                new KafkaConsumer<String, String>(properties);

        //2. 订阅话题，拉取消息
        consumer.subscribe(Collections.singleton("aa1"),

                new ConsumerRebalanceListener() {
                    //分区分配之前做的事情
                    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                        //提交旧的offset
                        commit();
                    }

                    //分区分配之后做的事情
                    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                        //获取新的offset
                        readOffset(partitions);
                        for (TopicPartition partition : partitions) {
                            Long os = offset.get(partition);
                            if (os == null) {
                                consumer.seek(partition,0);
                            } else {
                                consumer.seek(partition, os);
                            }
                        }
                    }
                });
        //拉取数据
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(2000);
            //原子绑定 (每消费一个,就存储一个)
            {
                for (ConsumerRecord<String, String> record : records) {
                    //消费
                    System.out.println(record);
                    //保存每个分区对应的offset值
                    offset.put(
                            new TopicPartition(record.topic(), record.partition()),
                            record.offset());
                }
                //存储commit方法是自定义的
                commit();
            }
        }
    }


    /**
     * 从自定义介质读取offset到缓存
     * @param partitions
     */
    private static void readOffset(Collection<TopicPartition> partitions) {
        Map<TopicPartition, Long> temp = readFile();
        //从全部分区offset中读取我们分配到的分区的offset
        for (TopicPartition partition : partitions) {
            offset.put(partition, temp.get(partition));
        }

    }

    /**
     * 将缓存中的Offset提交到自定义介质中
     * 将内存中的数据序列化到本地磁盘
     */
    private static void commit() {
        //1. 先从文件中读取旧的所哟Offset
        Map<TopicPartition, Long> temp = readFile();
        //2. 合并我们的Offset
        temp.putAll(offset);

        //3. 将新的Offset写出去
        writeFile(temp);
    }

    private static Map<TopicPartition, Long> readFile() {
        ObjectInputStream objectInputStream = null;
        Map<TopicPartition, Long> temp;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(file));
            temp = (Map<TopicPartition, Long>) objectInputStream.readObject();
        } catch (Exception e) {
            temp = new HashMap<TopicPartition, Long>();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return temp;
    }

    private static void writeFile(Map<TopicPartition, Long> temp) {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
