# SparkCore
## cache缓存
+ RDD通过cache或者persist方法将前面的计算结果缓存,
+ 默认情况下会把数据以序列化形式存在JVM堆内存中,
+ 缓存的使用并非调用立即缓存而是触发action时,RDD将会缓存在计算节点内存内以供后面使用
#### 设置缓存
```scala
rdd.cache()
```
#### 释放缓存
```scala
rdd.unpersist()
```
例：通过查看血缘关系缓存是否被使用，rdd的来源
```scala
val rdd = sc.makeRDD(List(1, 2, 3), 2)
val r1 = rdd.map(
num => {
println("*********************")
(num, 1)
}
)
//打印血缘关系
println(r1.toDebugString)
//触发行动操作
r1.collect()
println("=========================================================")
//缓存r1 当程序执行结束后缓存目录会被删除
r1.cache()
//接收参数,指定缓存位置(内存/磁盘)程序执行结束缓存目录会被删除
//    r1.persist()  //默认缓存在内存中
//打印血缘关系
println(r1.toDebugString)
//触发行动操作
r1.collect()
```
## Chack Point 检查点
+ 检查点-->通过将RDD中间结果写入磁盘(HDFS)
+ 做检查点目的-->血缘依赖过长会造成容错成本过高，这样就不如在中间阶段做检查点容错，如果检查点之后有节点出现问题，可以从检查点开始重做血缘，减少了开销。
+ 检查点存储路径：Checkpoint的数据通常是存储在HDFS等容错、高可用的文件系统
+ 检查点数据存储格式为：二进制的文件
+ 检查点切断血缘：在Checkpoint的过程中，该RDD的所有依赖于父RDD中的信息将全部被移除。
+ 检查点触发时间：对RDD进行checkpoint操作并不会马上被执行，必须执行Action操作才能触发。
+ 检查点为了数据安全，会从血缘关系的最开始执行一遍。（第一次从头开始执行，后面的从检查点里获取）
### 设置检查点步骤
#### 设置检查点数据存储路径
+ 本地：
```scala
sc.setCheckpointDir("绝对路径")
```
+ HDFS：
```scala
System.setProperty("HADOOP_USER_NAME","atguigu")           
sc.setCheckpointDir("hdfs://hadoop100:8020/cp")
```
#### 调用检查点方法：--待补充
### 缓存和检查点区别
+ Cache(缓存)只是将数据保存起来，不切断血缘依赖。Checkpoint(检查点)切断血缘依赖。
+ Cache(缓存)的数据通常存储在磁盘、内存等地方，可靠性低。Checkpoint的数据通常存储在HDFS等容错、高可用的文件系统，可靠性高。
+ 缓存-检查点组合使用：对checkpoint()的RDD使用Cache缓存，这样checkpoint的job只需从Cache缓存中读取数据即可，否则需要再从头计算一次RDD。
+ 使用完了缓存，可以通过unpersist（）方法释放缓存
```scala
val conf = new SparkConf().setAppName("ck").setMaster("local[*]")
val sc = new SparkContext(conf)
/** 检查点目录 应将目录设置在hdfs上
System.setProperty("HADOOP_USER_NAME","atguigu")
sc.setCheckpointDir("hdfs://hadoop100:8020/cp")
*/
//检查点设置在本地
sc.setCheckpointDir("F:\\BaiduNetdiskDownload\\大数据\\spark\\input")
val rdd = sc.makeRDD(List("长泽雅美 木田彩水", "凌波丽 葛城明里", "新恒结衣 明日香"),2)
//扁平映射
val r1 = rdd.flatMap(_.split(" "))
//结构化
val r2 = r1.map {
words => {(words, System.currentTimeMillis())}
}
val r3 = r2.groupByKey()
val wc = r3.map(wc => (wc._1, wc._2.sum))
//打印血缘关系
println(wc.toDebugString)
//设置缓存
wc.cache()
//设置检查点
wc.checkpoint()
//触发行动操作
wc.collect().foreach(println)
//打印血缘关系
println(wc.toDebugString)
//释放缓存
wc.unpersist()
//触发行动操作
wc.collect().foreach(println)
//wc.collect()
sc.stop()
```
## 累加器--分布式共享只写变量
通常在向Spark传递函数时，比如使用map()函数或者用 filter()传条件时，可以使用驱动器程序中定义的变量，但是集群中运行的每个任务都会得到这些变量的一份新的副本，更新这些副本的值也不会影响驱动器中的对应变量。如果我们想实现<font color=blue>`所有分片处理时更新共享变量`</font>的功能，那么累加器可以实现我们想要的效果。
+ 累加器将不同节点的Excuter结果收集到Driver端汇总
+ TASK与TASK之间不能互读数据
+  累加器用来对信息聚合（Reduce功能）
+  实现了所有分片处理时更新共享变量的功能
#### 注意事项
+ 工作节点上的任务不能相互访问累加器的值。(任务的角度来看，累加器是一个只写变量）
+ 必须放在foreach()这样的行动操作中。转化操作中累加器可能会发生不止一次更新（Spark只会把每个任务对各累加器的修改应用一次）。
#### 系统累加器--WordCount
```scala
//扁平映射
val rdd = sc.makeRDD(List("桥本环奈 木田彩水", "凌波丽 葛城明里 桃乃木香奈", "新恒结衣 明日香"),2)
val r1 = rdd.flatMap(_.split(" "))
//结构化
val r2 = r1.map {
words => {(words, 1)}
}
//系统累加器使用--创建累加器
val ac = sc.longAccumulator("wc")
r2.foreach{
//不针对key,仅对value做累加
case (word,datas)=> {ac.add(datas)}
}
println(ac.value)
```
#### 自定义累加器
自定义累加器  统计单词中带“木“的词,以及出现次数
```scala
//AccumulatorV2泛型表示累加器输入,输出的类型[In,Out]
class myaccumulat extends AccumulatorV2[String,mutable.Map[String,Int]]{
//定义集合，集合单词及出现次数 (通过Apply方法创建对象)
var map=mutable.Map[String,Int]()
//判断是否是初始状态
override def isZero: Boolean = map.isEmpty
//拷贝（通过map集合记录累加的值）
override def copy(): AccumulatorV2[String, mutable.Map[String, Int]] = {
val myac = new myaccumulat
myac.map=this.map
myac
}
//重置
override def reset(): Unit = map.clear()
//向累加器添加元素
override def add(v: String): Unit = {
//输入单词包含木则map中key对应的val+1
if (v.contains("木")){
map(v)=map.getOrElse(v,0)+1
}
}
//合并
override def merge(other: AccumulatorV2[String, mutable.Map[String, Int]]): Unit = {
//当前Excuter的map
val map1=map
//另一个Excuter的map
val map2=other.value
//将两个集合的合并结果赋值给map,返回累加结果
map=map1.foldLeft(map2){
//mm表示map2,kv表示map1中的每一个元素
(mm,kv)=>{
//指定合并规则
val k:String=kv._1
val v:Int=kv._2
//根据map1中的元素key,到map2中获取value
mm(k)=mm.getOrElse(k,0)+v
mm
}
}
}
//获取累加器的值
override def value: mutable.Map[String, Int] = map
}
```
自定义累加器的使用
```scala
val conf = new SparkConf().setAppName("adder").setMaster("local[*]")
val sc = new SparkContext(conf)
val r3 = sc.makeRDD(List("桥本环奈", "木田彩水", "凌波丽","葛城明里", "桃乃木香奈", "新恒结衣","明日香"),2)
//--创建累加器
val myac = new myaccumulat
//--注册累加器
sc.register(myac)
//--使用累加器
r3.foreach {
word => myac.add(word)
}
//输出累加器结果
println(myac.value)
sc.stop()
```
## 广播变量--分布式共享只读变量
多个并行操作中（Excuter）`使用同一个变量`spark默认为每个任务(Task)分别发送
所以共享较大的对象会占用较大的工作节点内存

+ 广播变量用来高效分发较大对象,向所有工作节点发一个较大的只读值,供一个或多个spark操作使用
+ 场景：应用需向所有节点发送较大的只读查询表(机器学习算法较大的特征向量)

### 使用步骤：
 * 1.对类型 T 的对象调用SparkContext.broadcast创建出一个Broadcast[T] 对象任何可序列化类型都能这样实现
 * 2.通过value属性访问该对象的值
 * 3.变量分发到各个节点(只发送一次)作为只读值(修改单个节点不影响其他)

案例
```scala
val conf = new SparkConf().setAppName("bordcast").setMaster("local[*]")
val sc = new SparkContext(conf)
val li = sc.makeRDD(List(("桥本环奈", 1), ("新恒结衣", 3), ("木田彩水", 5)))
val li2 = List(("桥本环奈", 23), ("新恒结衣", 25), ("木田彩水", 27))

//1.创建广播变量
val bc = sc.broadcast(li2)
//实现类型join效果

val resRDD = li.map {
    case (k1, v1) => {
    var v3 = 0
    //2. 遍历广播变量,通过value属性访问广播变量的值
    for ((k2, v2) <- bc.value) {
        if (k1 == k2) {
        v3 = v2
        }
    }
    (k1, (v1, v3))
    }
}
resRDD.collect().foreach(println)
sc.stop()
```