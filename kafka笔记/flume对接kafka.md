# Flume对接Kafka
> ### 例1配置flume(flume-kafka.conf)
```conf
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# source
a1.sources.r1.type = netcat
a1.sources.r1.bind=hadoop100
a1.sources.r1.port =11111

# 配置kafka sink
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.kafka.bootstrap.servers = hadoop100:9092,hadoop101:9092,hadoop102:9092
a1.sinks.k1.kafka.topic = aa1
a1.sinks.k1.kafka.flumeBatchSize = 20
a1.sinks.k1.kafka.producer.acks = 1
a1.sinks.k1.kafka.producer.linger.ms = 1

# channel
a1.channels.c1.type = memory
# bind
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```
> ### 启动flume
```bash
flume-ng agent -c conf/ -n a1 -f datas/flume-kafka.conf -Dflume.root.logger=INFO,console
```
> ### 例2配置flume(flume-kafka2.conf)
```conf
# define
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# source
a1.sources.r1.type = exec
a1.sources.r1.command = tail -F -c +0 /opt/module/datas/flume.log
a1.sources.r1.shell = /bin/bash -c

# sink
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.kafka.bootstrap.servers = hadoop100:9092,hadoop101:9092,hadoop102:9092
a1.sinks.k1.kafka.topic = first
a1.sinks.k1.kafka.flumeBatchSize = 20
a1.sinks.k1.kafka.producer.acks = 1
a1.sinks.k1.kafka.producer.linger.ms = 1

# channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100

# bind
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```
>### 启动kafkaIDEA消费者

>### 进入flume根目录下，启动flume
```bash
flume-ng agent -c conf/ -n a1 -f datas/flume-kafka2.conf
``` 
>### 向 /opt/module/datas/flume.log里追加数据，查看kafka消费者消费情况
```bash
echo hello >> /opt/module/datas/flume.log
```

