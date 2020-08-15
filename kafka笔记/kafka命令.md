# 	kafka 命令
> 查看topic
```bash
kafka-topics.sh --zookeeper hadoop100:2181 --list
```
> 创建topic
```bash
kafka-topics.sh --zookeeper hadoop100:2181 --create --replication-factor 3 --partitions 1 --topic first
```
>删除topic
```bash
kafka-topics.sh --zookeeper hadoop100:2181 --delete --topic first
```
>发送消息
```bash
kafka-console-producer.sh --broker-list hadoop100:9092 --topic first
```
>消费消息
```bash
kafka-console-consumer.sh --bootstrap-server hadoop100:9092 --topic first
```
>查看topic
```bash
kafka-topics.sh --zookeeper hadoop100:2181  --describe --topic first
```
>修改分区数
```bash
kafka-topics.sh --zookeeper hadoop100:2181 --alter --topic first --partitions 6
```
>指定消费分区(from-beginng指的是从头开始消费)
```bash
kafka-console-consumer.sh --bootstrap-server hadoop100:9092 --topic first --from-beginning --partition 1
```