# kafka的API
## 生产者api发数据
### 生产者api发数据(异步)
```java
public class Myproducer {
    public static void main(String[] args) {
//      1.自定义配置文件
        Properties props = new Properties();
        props.put("bootstrap.servers","hadoop100:9092");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"hadoop100:9092");
        props.put("acks","all");
        props.put("retries", 1);//重试次数
        props.put("batch.size", 16384);//批次大小  缓冲区到达16384向缓冲区发生一次
        props.put("linger.ms", 1);//等待时间      缓冲区尚未到达批次大小但已过1ms发生一次
        props.put("buffer.memory", 33554432);//RecordAccumulator缓冲区大小
//       发送ProducerRecord序列化key value
         props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//      2.创建生产者对象
        Producer<String, String> producer = new KafkaProducer(props);
        for (int i = 0; i < 10; i++) {
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
```
+ 消费者消费
```bash
kafka-console-consumer.sh --bootstrap-server hadoop100:9092 --topic aa1
```
### 生产者api发数据(同步)
+ 异步回调函数后+.get()
+ get()会阻塞当前线程，等待分线程执行完毕
## 消费者api收数据
### 消费者收数据（自动） 
```java
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
```
+ 生产者生产
```bash
 kafka-console-producer.sh --broker-list hadoop100:9092 --topic aa1
```
### 消费者收数据（手动提交）
```java

```