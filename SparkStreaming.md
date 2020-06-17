# SparkStreaming-主要用于实时计算
### 数据处理的延时
+ 离线计算

就是在计算开始前已知所有输入数据，输入数据不会产生变化，一般计算量级较大，计算时间也较长。例如今天早上一点，把昨天累积的日志，计算出所需结果。最经典的就是Hadoop的MapReduce方式；
+ 实时计算

输入数据是可以以序列化的方式一个个输入并进行处理的，也就是说在开始的时候并不需要知道所有的输入数据。与离线计算相比，运行时间短，计算量级相对较小。强调计算过程的时间要短，即所查当下给出结果。

### 批量和流式概念
#### 数据处理的方式
+ 批：处理离线数据，冷数据。单个处理数据量大，处理速度比流慢。
+ 流：在线，实时产生的数据。单次处理的数据量小，但处理速度更快。
#### 流数据具有如下特征：
+ 数据快速持续到达，潜在大小也许是无穷无尽的；
+ 数据来源众多，格式复杂；
+ 数据量大，但是不十分关注存储，一旦经过处理，要么被丢弃，要么被归档存储；
+ 注重数据的整体价值，不过分关注个别数据；
## Spark Streaming --<font color=red>用于流式数据的处理</font>
+ 支持多数据源输入，例如：Kafka、Flume和简单的TCP套接字等。
+ 数据输入后可以用算子如：map、reduce、join、window等进行运算。结果能保存在很多地方，如HDFS，数据库等。
### 微批次处理及批处理间隔
+ Spark Streaming 中，处理数据的单位是一批(微批次)而不是单条，而数据采集却是逐条进行的，因此 Spark Streaming 系统需要设置间隔使得数据汇总到一定的量后再一并操作，这个间隔就是批处理间隔。
+ 批处理间隔是Spark Streaming的核心概念和关键参数，它决定了Spark Streaming提交作业的频率和数据处理的延迟，同时也影响着数据处理的吞吐量和性能。
### DStream（离散化流）--随时间推移而收到的数据的序列
+ Spark Streaming使用了一个高级抽象离散化流(discretized stream)，叫作DStreams。DStreams是随时间推移而收到的数据的序列。
+ 在Streaming内部，每个时间区间收到的数据都作为RDD存在，而DStreams是由这些RDD所组成的序列(因此得名“离散化”)。
+ DStreams可以由来自数据源的输入数据流来创建, 也可以通过在其他的 DStreams上应用一些高阶操作来得到。

原理图
![streaming原理.PNG](https://i.loli.net/2020/05/20/fCzOdVQ1sjYeZSw.png)
### 背压机制（Spark Streaming Backpressure）
根据JobScheduler反馈作业的执行信息来动态调整Receiver数据接收率。
+ 解决数据采集速率与数据处理速率不同步问题
+ 通过属性“spark.streaming.backpressure.enabled”来控制是否启用backpressure机制，默认值false，即不启用。

### 原理解析
+ Discretized Stream是Spark Streaming的基础抽象，代表持续性的数据流和经过各种Spark算子操作后的结果数据流。
+ 内部实现上，DStream是一系列连续的RDD来表示,每个RDD含有一段时间间隔内的数据，对这些 RDD的转换是由Spark引擎来计算的 
+ DStream的操作隐藏的大多数的细节, 给开发者提供了方便使用的高级 API

### 注意
+ 一旦StreamingContext已经启动, 则不能再添加新的 streaming computations
+ 一旦一个StreamingContext已经停止(StreamingContext.stop()), 他也不能再重启
+ 在一个 JVM 内, 同一时间只能启动一个StreamingContext
+ stop() 的方式停止StreamingContext, 也会把SparkContext停掉. 如果仅仅想停止StreamingContext, 则应该这样: stop(false)
+ 一个SparkContext可以重用去创建多个StreamingContext, 前提是以前的StreamingContext已经停掉,并且SparkContext没有被停掉
### DStream创建
#### 直接指定
+ 案例1通过指定端口获取数据
```bash
nc -lk 2333
```
```scala
//创建streaming程序执行入口对象
val ssc:StreamingContext = new StreamingContext(conf, Seconds(3))
//指定端口获取数据
val socketDS = ssc.socketTextStream("hadoop100", 2333)
//扁平化
val fmDS = socketDS.flatMap(_.split(" "))
//结构转换
val mapDS = fmDS.map((_, 1))
//聚合
val rdDS = mapDS.reduceByKey(_ + _)
rdDS.print

//启动采集器
ssc.start()
//默认不关闭，等待采集程序结束终止程序
ssc.awaitTermination()
```
+ 案例2通过队列获取
```scala
val ssc =new StreamingContext(conf, Seconds(3))
//创建队列里面放rdd
val rddque = new mutable.Queue[RDD[Int]]()
//队列采集数据获取DS
val queDS = ssc.queueStream(rddque)
//处理采集到的数据
val DStr = queDS.map((_, 1)).reduceByKey(_ + _)
//打印结果
DStr.print
ssc.start()
//循环创建rdd 将创建的rdd放入队列
for(i<-1 to 20){
    rddque.enqueue(ssc.sparkContext.makeRDD(6 to 10))
    Thread.sleep(2000)
}
ssc.awaitTermination()
```
+ 案例3 自定义数据源指定读取方式路径
```scala
//Receiver[T]泛型表示读取数据类型
class MyReciver(host:String,port:Int) extends Receiver[String](StorageLevel.MEMORY_ONLY){
private var socket: Socket = _

override def onStart():Unit={
//守护线程负责运行程序
new Thread("Socket Receiver") {
    setDaemon(true)
    override def run() { receive() }
}.start()
}
def receive(){
try {
    //创建连接
    socket=new Socket(host,port)
    //字节数据byte需转换成字节流
    val inputStream = socket.getInputStream
    //转换流
    val reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
    //定义变量读取一行数据
    var input:String=null
    while ((input=reader.readLine())!=null){
    store(input)
    }
} catch {
case e: ConnectException =>
    restart(s"Error connecting to $host:$port", e)
    return
} finally {
    onStop()
}
}
override def onStop():Unit={
synchronized {
    if (socket != null) {
    socket.close()
    socket = null
    }}}
}
```
使用自定义方法加载数据
```scala
val sc = new StreamingContext(conf, Seconds(3))
//自定义数据源创建Dstream
//    sc.socketTextStream()
val myDS = sc.receiverStream(new MyReciver("hadoop100", 2333))
//扁平化
val fmDS = myDS.flatMap(_.split(" "))
//结构转换
val mapDS = fmDS.map((_, 1))
//聚合
val rdDS = mapDS.reduceByKey(_ + _)
//打印输出
rdDS.print
sc.start()
sc.awaitTermination()
```
### kafka数据源
#### stream-Kafka-0-8版本 
+ Kafka-Receive-API:需要Excuter接收数据再发送给其他Excuter做计算，有可能发生内存溢出(接收Excuter速度>计算Excuter速度)--已过时不建议使用
+ Kafka-DirectAPI：由计算Excuter主动消费Kafka数据，速度由自身决定
##### DirectApi连接kafka获取数据
```bash
//创建topic
bin/kafka-topics.sh --create --bootstrap-server hadoop100:9092 --topic tp1 --partitions 2 --replication-factor 2
//生产消息
bin/kafka-console-producer.sh --broker-list hadoop100:9092 --topic tp1
```
 > 方式1 -- 自动维护偏移量 维护在checkpoint中
 ```scala
//创建streaming上下文环境对象
val sc = new StreamingContext(conf, Seconds(6))

//准备kafka参数
val kfparams = Map[String, String](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop100:9092,hadoop101:9092,hadoop102:9092",
    ConsumerConfig.GROUP_ID_CONFIG -> "bigdata"
)

//创建时指定序列化类型
val kfDS = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
    sc,
    kfparams,
    Set("tp1") //消费主题名称
)
val liDS = kfDS.map(_._2)
//扁平化
val fmDS = liDS.flatMap(_.split(" "))
//结构转换
val mapDS = fmDS.map((_, 1))
//聚合
val rdDS = mapDS.reduceByKey(_ + _)
rdDS.print

//启动采集器
sc.start()

//默认不关闭，等待采集程序结束终止程序
sc.awaitTermination()
 ```
> 方式2 -- 修改StreamingContext对象获取方式
+ 先从检查点获取数据，若检查点没有则函数创建保证数据不丢失
+ 缺点 -->1 生成小文件       2 checkpoint中只记录最后一次offset的时间戳再次启动时会从这个时间到当前时间把所有周期全部执行

```scala
def main(args: Array[String]): Unit = {
//创建streaming上下文环境对象
val scc: StreamingContext = StreamingContext.getActiveOrCreate("F:\\BaiduNetdiskDownload\\大数据\\spark\\datas", () => getstreamingcontext)

scc.start()
scc.awaitTermination()
}
def getstreamingcontext():StreamingContext={

    val conf = new SparkConf().setAppName("dirct2").setMaster("local[*]")
    //创建streaming上下文环境对象
    val sc = new StreamingContext(conf, Seconds(3))
    //设置检查点目录，偏移量维护在checkpoint中
    sc.checkpoint("F:\\BaiduNetdiskDownload\\大数据\\spark\\datas")
    //准备kafka参数
    val kfparams = Map[String, String](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop100:9092,hadoop101:9092,hadoop102:9092",
    ConsumerConfig.GROUP_ID_CONFIG -> "bigdata"
    )

    //创建时指定序列化类型
    val kfDS = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
    sc,
    kfparams,
    Set("tp1") //消费主题名称
    )
    val liDS = kfDS.map(_._2)
    //扁平化
    val fmDS = liDS.flatMap(_.split(" "))
    //结构转换
    val mapDS = fmDS.map((_, 1))
    //聚合
    val rdDS = mapDS.reduceByKey(_ + _)
    rdDS.print
    sc
}
```
>方式3--手动维护offset
```scala
//创建上下文
val sc = new StreamingContext(conf, Seconds(3))

//准备kafkf参数
val kafparam = Map[String, String](
  ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "hadoop100:9092,hadoop101:9092,hadoop102:9092",
  ConsumerConfig.GROUP_ID_CONFIG -> "bigdata"
)
//获取上一次消费位置（偏移量）
//实际项目中，为保证数据精准一次性对数据消费处理后，将偏移量保存在有事务的存储中如Mysql（消费成功写入，不成功回滚）
var fromoffset:Map[TopicAndPartition,Long]= Map[TopicAndPartition,Long](
  //tp1会话0号分区偏移量为1
  TopicAndPartition("tp1",0)->10L,
  TopicAndPartition("tp1",1)->10L
)

//指定的offset读取数据进行消费
val kafDS = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, String](
  sc,
  kafparam,
  fromoffset,
  (m: MessageAndMetadata[String, String]) => m.message()
)
//消费完毕后对偏移量进行更新
var offsetrange = Array.empty[OffsetRange]
kafDS.transform{
  rdd=>{
    offsetrange = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
    rdd
  }
}.foreachRDD{
  rdd=>{for(o <-offsetrange){
    println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
  }}
}
sc.start()
sc.awaitTermination()
```
#### *stream-Kafka-0-10版本（常用）
```scala
val conf = new SparkConf().setAppName("trans").setMaster("local[*]")
val sc = new StreamingContext(conf, Seconds(3))
val tpic = "hadoop100:9092,hadoop101:9092"
val group = "bigdata"
val key = "org.apache.kafka.common.serialization.StringDeserializer"
val value = classOf[StringDeserializer]
//参数配置
val kafpar = Map[String,Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> tpic,
    ConsumerConfig.GROUP_ID_CONFIG -> group,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> key,
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> value )
val kafDS = KafkaUtils.createDirectStream(
    sc,
    LocationStrategies.PreferConsistent,
    ConsumerStrategies.Subscribe[String,String](Set("tp1"), kafpar)
)
//功能实现代码块 ......{}
sc.start()
sc.awaitTermination()
```
#### *stream-Kafka-0-8版本（不常用）
```scala
val conf = new SparkConf().setAppName("demo2").setMaster("local[*]")
val sc = new StreamingContext(conf, Seconds(3))
//参数
val topic="my-ads-0105"
val broker = "hadoop100:9092,hadoop101:9092"
val group = "bigdata"
val seral = "org.apache.kafka.common.serialization.StringDeserializer"
//参数配置
val kafarg = Map(
ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> broker,
ConsumerConfig.GROUP_ID_CONFIG -> group,
ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> seral,
ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> seral
)
//创建kfakaDS
val kafDS = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
sc, kafarg, Set(topic)
)
//实现功能代码块......{}
sc.start()
sc.awaitTermination()
```
## DStream转换
DStream上的操作与RDD的类似，分为Transformations（转换）和Output Operations（输出）两种，此外转换操作中还有一些比较特殊的算子，如：updateStateByKey()、transform()以及各种Window相关的算子。


### 无状态转化操作 
+ 后面执行的结果与之前的无关不关联历史结果
#### 无状态转换函数:
```scala
Map() 
flatMap() 
filter()
repartition()
reduceByKey()
groupByKey()
```
尽管这些函数看起来像作用在整个流上一样，但事实上每个DStream在内部是由许多RDD（批次）组成，且无状态转化操作是分别应用到每个RDD上的(Dsteram是RDD的封装)。
####  Transform算子-- 转化DStream中的每一个RDD 
+ 允许DStream上执行任意的RDD函数。(即使这些函数并没有在DStream的API中暴露出来)
+ DStream转换成RDD再进行操作 
+ 通过该函数可以方便的扩展Spark API
```scala
//创建sparkconf配置文件对象
val conf:SparkConf = new SparkConf().setAppName("tra1").setMaster("local[*]")
val sc = new StreamingContext(conf, Seconds(3))
//指定端口获取数据
val sk = sc.socketTextStream("hadoop100", 2333)
val trrd = sk.transform {
  rdd => {
    val fm = rdd.flatMap(_.split(""))
    val maps = fm.map((_, 1))
    val rdds = maps.reduceByKey(_ + _)
    rdds.sortByKey()
  }
}
trrd.print()

sc.start()
sc.awaitTermination()
```
### 有状态转化操作
+  将历史结果应用到当前批次(如当前值与历史值做累加)
#### UpdateStateByKey算子
+ 定义状态，状态可以是一个任意的数据类型。
+ 定义状态更新函数，用此函数阐明如何使用之前的状态和来自输入流的新值对状态进行更新。
+ 使用updateStateByKey需要对检查点目录进行配置，会使用检查点来保存状态。
>2个参数 UpdateStateByKey（Seq，Option）
+ 第一个参数Seq：相同的key对应value的集合
+ 第二个参数option：相同的key对应的状态(上一个采集周期的计算结果)
```scala
val conf = new SparkConf().setAppName("tra2").setMaster("local[*]")
val sc = new StreamingContext(conf, Seconds(3))

val tpic = "hadoop100:9092,hadoop101:9092"
val group = "bigdata"
val key = "org.apache.kafka.common.serialization.StringDeserializer"
val value = classOf[StringDeserializer]

//设置检查点路径
sc.checkpoint("F:\\BaiduNetdiskDownload\\大数据\\spark\\datas\\cp")

val kafpar = Map(
  ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> tpic,
  ConsumerConfig.GROUP_ID_CONFIG -> group,
  ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> key,
  ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> value
)
val kafDS = KafkaUtils.createDirectStream(
  sc,
  LocationStrategies.PreferConsistent,
  ConsumerStrategies.Subscribe[String,String](Set("tp1"), kafpar)
)
val temDS:DStream[(String,Int)] = kafDS.map(_.value()).flatMap(_.split(" ")).map((_, 1))
val res = temDS.updateStateByKey(
  //第一个参数：表示相同点key对应value组成的数据集合
  //第二个参数：表示相同的key缓冲区数据
  (seq: Seq[Int], state: Option[Int]) => {
    //对当前key对应value求和
    seq.sum
    //获取缓冲区数据
    state.getOrElse(0)
    Option(seq.sum + state.getOrElse(0))
  }
)
res.print()

sc.start()
sc.awaitTermination()
```
#### Window Operations(窗口操作) 
+ 把计算应用到一个指定的窗口内的所有RDD上
+ 通过整合多个批次的结果，计算出整个窗口的结果。
>2个参数（这两者都必须为采集周期的整数倍）需要检查点保存数据
+ 窗口时长：计算内容的时间范围
+ 滑动步长：隔多久触发一次计算。
```scala
val conf = new SparkConf().setAppName("window").setMaster("local[*]")
val sc = new StreamingContext(conf, Seconds(5))

//设置检查点路径  用于保存状态
sc.checkpoint("F:\\BaiduNetdiskDownload\\大数据\\spark\\datas\\cp")
//创建DStream
val lineDStream: ReceiverInputDStream[String] = sc.socketTextStream("hadoop100", 2333)
//扁平映射
val flatMapDS: DStream[String] = lineDStream.flatMap(_.split(" "))
//设置窗口大小，滑动的步长
val windowDS: DStream[String] = flatMapDS.window(Seconds(10),Seconds(5))
//结构转换
val mapDS: DStream[(String, Int)] = windowDS.map((_,1))
//聚合
val reduceDS: DStream[(String, Int)] = mapDS.reduceByKey(_+_)
reduceDS.print()

sc.start()
sc.awaitTermination()
```
## DStream输出
#### 打印 print()
+ 打印每一批最开始的10个元素
```scala
DS.print()
```
#### 保存 saveAsTextFiles--保存到指定目录 DS.saveAsTextFiles(参数1，参数2)
+ 参数1 --> 指定保存文件夹目录+前缀
+ 参数2 --> 后缀
```scala
//保存文件
kafDS.saveAsTextFiles("F:\\BaiduNetdiskDownload\\大数据\\spark\\datas\\save\\子晨","小姐姐")
```
#### foreachRDD(func) 迭代 -- 传入函数 
+ 最通用的输出操作，函数 func 用于stream的每一个RDD。其中参数传入的函数func应该实现将每一个RDD中数据推送到外部系统。
+ 应用：向Mysql写入数据
1. 连接不能写在driver层面（序列化）；
2.	如果写在foreach则每个RDD中的每一条数据都创建，得不偿失；
3.	增加foreachPartition，在分区创建（获取）。
### DStream 编程进阶
####	累加器和广播变量
#### 缓存（caching）/检查点（checkpoint）
+ 缓存（caching） -- 对DStream
```scala
//直接使用
DS.cache()
//指定级别
DS.persist(StorageLevel.MEMORY_ONLY)
```
+ 检查点 （checkpoint） --对StreamingContext
```scala
sc.checkpoint("地址")
```
#### DataFrame和SQL Operations
+ 使用SparkSQL采集周期中的数据
+ kafkaDS用foreachRDD得到rdd
+ 将RDD转化成DataFrame
```scala
//将kafkaDS转化成RDD
kafkaDS.foreachRDD{
rdd=>{
  rdd
  //rdd转换DataFrame
  val df = rdd.toDF("word", "count")
  //创建临时视图
  df.creatOrReplaceTempView("word")
  //执行sql
  spark.sql("select * from word").show
}}
```
#### 优雅关闭 -- 使用标记位
+ 通过在HDFS上创建文件（标记状态，广播变量）
```scala
val conf = new SparkConf().setAppName("close").setMaster("local[*]")
val sc = new StreamingContext(conf, Seconds(3))

//设置检查点路径  用于保存状态
sc.checkpoint("F:\\BaiduNetdiskDownload\\大数据\\spark\\datas\\cp")
//创建DStream
val lineDStream: ReceiverInputDStream[String] = sc.socketTextStream("hadoop100", 2333)
//扁平映射
val flatMapDS: DStream[String] = lineDStream.flatMap(_.split(" "))
//结构转换
val mapDS: DStream[(String, Int)] = flatMapDS.map((_,1))
//聚合
val reduceDS: DStream[(String, Int)] = mapDS.reduceByKey(_+_)
reduceDS.print()

conf.set("spark.streaming.stopGracefullyOnShutdown", "true")

// 启动新的线程，希望在特殊的场合关闭SparkStreaming
new Thread(new Runnable {
  override def run(): Unit = {

    while ( true ) {
      try {
        Thread.sleep(5000)
      } catch {
        case ex : Exception => println(ex)
      }

      // 监控HDFS文件的变化
      val fs: FileSystem = FileSystem.get(new URI("hdfs://hadoop100:8020"), new Configuration(), "atguigu")

      val state: StreamingContextState = sc.getState()
      // 如果环境对象处于活动状态，可以进行关闭操作
      if ( state == StreamingContextState.ACTIVE ) {

        // 判断路径是否存在
        val flg: Boolean = fs.exists(new Path("hdfs://hadoop100:8020/stopSpark2"))
        if ( flg ) {
          sc.stop(true, true)
          System.exit(0)
        }
      }
    }

  }
}).start()

sc.start()
sc.awaitTermination()
```
### 程序执行入口
|Core|Sql|Streaming|
|---|---|---|
|SparkContext|SparkSession|StreamingContext|
|RDD|DataFrame|DStream|