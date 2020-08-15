# kafka概念
定义：一个分布式，发布/订阅模式消息队列应用于大数据实时处理。

>优点：解耦，可恢复性，缓冲，峰值处理，异步通信

### 基础架构
1. Producer ：消息生产者，向kafka broker发消息的客户端
2. Consumer ：消息消费者，向kafka broker取消息的客户端
3. Consumer Group （CG）：消费者组，由多个consumer组成。消费者组内每个消费者负责消费不同分区的数据，`一个分区只能由一个消费者消费`；消费者组之间互不影响。`所有`的消费者`都`属于某个`消费者组`，即消费者组是逻辑上的一个订阅者。

4. Broker ：一台kafka服务器就是一个`broker`。一个集群由`多个`broker组成。`一个broker`可以容纳`多个topic`。

5. Topic(主题)：一个消息队列，生产者和消费者面向的都是一个topic；
6. partition(分区)：`topic`可以分布到`多个broker`（即服务器）上，一个`topic`可以分为多个`partition`，`每个`partition是一个`有序`的`队列`,且每个partition都有leader
7. Replica(副本)：保证集群中节点发生`故障`时，该节点上的`partition数据不丢失`，且kafka仍然能够继续工作，kafka提供了副本机制，一个topic的`每个分区`都有`若干副本`，一个`leader`和若干个`follower`。
8. leader：每个分区多个副本的“主”，`生产者发送数据的对象`，以及`消费者消费数据的对象`都是leader。
9. follower：每个分区多个副本中的“从”，`实时从leader中同步数据`，保持和leader数据的同步。leader发生`故障`时，某个`follower会成为新的leader`。

