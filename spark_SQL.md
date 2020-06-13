# Spark SQL
+ 将Spark SQL转换成RDD，然后提交到集群执行，执行效率非常快！
## 提供2个抽象,类似SparkCore中的RDD
### DataFrame-->一种以RDD为基础的分布式数据集，类似于传统数据库中的二维表格。
+ DataFrame与RDD的区别-->前者带有schema元信息，即DataFrame所表示的二维表数据集的每一列都带有名称和类型。
+ DataFrame也支持嵌套数据类型（struct、array和map）
+ DataFrame是为数据提供了Schema的视图。可以把它当做数据库中的一张表来对待
+ DataFrame是懒执行的，但性能上比RDD要高
### DataSet-->DataFrame的一个扩展。
+ DataSet是强类型的。可以有DataSet[Car]，DataSet[Person]。 
+ 支持使用强大的lambda函数的能力
+ DataSet可以使用功能性的转换（操作map，flatMap，filter等）
+ DataFrame是DataSet的特例--->DataFrame=DataSet[Row] (Row是一个类型，跟Car、Person这些的类型一样，所有的表结构信息都用Row来表示)
+ 可以通过as方法将DataFrame转换为DataSet。
## SQL风格语法
+ 查询数据时使用SQL语句来查询(通过临时视图或者全局视图)
+ 创建一个DataFrame
```shell
val df = spark.read.json("/opt/module/spark-local/people.json")
```
+ 对DataFrame创建一个临时表
```shell
df.createOrReplaceTempView("表名1")
```
+ 查询临时表
```shell
val sqlDF = spark.sql("SELECT * FROM 表名1")
```
+ 对于DataFrame创建一个全局表
```shell
df.createGlobalTempView("表名2")
```
+ 通过SQL语句实现查询全局表
```shell
spark.sql("SELECT * FROM global_temp.表名2").show()
```
## DSL风格语法--去管理结构化数据(domain-specific language)
+ 可以在 Scala, Java, Python 和 R 中使用 DSL，使用 DSL 语法风格不必去创建临时视图了
#### 查询语法
+ 创建一个DataFrame
```scala
val df = spark.read.json("/opt/module/spark-local /people.json")
```
+ 查看DataFrame的Schema信息
```scala
df.printSchema
```
+ 只查看”name”列数据
```scala
df.select($"name",$"age" + 1).show
```
+ 查看所有列
```scala
df.select("*").show()
```
+ 查看”name”列数据以及”age+1”数据  (涉及到运算的时候, 每列都必须使用<font color=green>$</font>)
```scala
df.select($"name",$"age" + 1).show
```
+ 查看”age”大于”19”的数据
```scala
df.filter($"age">19).show
```
+ 按照”age”分组，查看数据条数
```scala
df.groupBy("age").count.show
```
## DataFrame
### 创建DataFrame
+ 通过JSON数据源进行创建
```shell
val df = spark.read.json("/opt/module/spark-local/people.json")
df.show
```
+ 通过集合创建
```scala
val df = List((1, "长泽雅美", "大姐姐"),(2, "新恒结衣","小姐姐")).toDF("id", "姓名", "属性")
df.show()
```
+ 通过RDD转化
```scala
val rdd = spark.sparkContext.parallelize(List(1,3,5,7,9))
val df = rdd.map(x=>(x,x+1)).toDF("奇数","偶数")
df.show()
```
注意：从内存中获取数据，spark知道数据类型具体是什么，如果是数字，默认作为Int处理；但是从文件中读取的数字，不能确定是什么类型，所以用bigint接收，可以和Long类型转换，但是和Int不能进行转换
+ 从一个存在的RDD进行转换
+ 从Hive Table进行查询返回。
### RDD与DataFrame间的转化
+ RDD与DF或者DS之间操作，那么都需要引入 import spark.implicits.(spark不是包名，而是sparkSession对象的名称，所以必须先创建SparkSession对象再导入. implicits是一个内部object)
```scala
import spark.implicits._
```
+ RDD转换为DataFrame--toDF
```scala
var rdd=sc.textFile("opt/module/sparl/people.txt")
//映射
rdd.map{str=>{var fields=str.split(",");(fields(0),fields(1),fields(2))}}
```
1. RDD手动转换为DataFrame
```scala
rdd.map{x=> val fields=x.split(",");(fields(0),fields(1).trim.toInt)}.toDF("name","age").show
```
2. 通过样例类反射转换（常用）字符串转成people对象（rdd-->df）
```scala
case class People(name:String,age:Int)
rdd.map{x=> var fields=x.split(",");
People(fields(0),fields(1).toInt)}.toDF.show
```
+ DataFrame转换为RDD   --得到的RDD存储类型为Row
```scala
val dfToRDD = df.rdd

//打印RDD
dfToRDD.collect
```
## DataSet--强类型的数据集合，需提供对应的类型信息。
### 创建DataSet
+ 使用样例类序列创建DataSet
```scala
case class Person(name: String, age: Long)

val caseClassDS = Seq(Person("凌波丽",23)).toDS()
```
+ 使用基本类型的序列创建DataSet

(注意：在实际使用的时候，很少用到把序列转换成DataSet，更多是通过RDD来得到DataSet)
```scala
val ds = Seq(1,2,3,4,5,6).toDS
```
### RDD与DataSet间的转化
+ SparkSQL能够自动将包含有样例类的RDD转换成DataSet，样例类定义了table的结构，样例类属性通过反射变成了表的列名。样例类可以包含诸如Seq或者Array等复杂的结构。
+ RDD转化为DataFrame
1. 创建rdd,样例类
```scala
val rdd = sc.textFile("/opt/module/spark-local/people.txt")
case class Person(name:String,age:Int)
```
2. 将RDD转化为DataSet（通过反射）
```scala
rdd.map(line => {val fields = line.split(",");Person(fields(0),fields(1). toInt)}).toDS
```
+ DataSet转换为RDD
1. 创建DataSet
```scala
 val DS = Seq(Person("凌波丽", 23)).toDS()
```
2. 转化成RDD
```scala
DS.rdd
```
## DataFrame与DataSet的互操作
![转化.PNG](https://i.loli.net/2020/05/18/PiAZVXmla2xyQqC.png)
###  DataFrame转为DataSet（需导入包spark.implicits._）
+ 创建一个DateFrame
```scala
val df = spark.read.json("/opt/module/spark-local/people.json")
```
+ 创建一个样例类
```scala
case class Person(name: String,age: Long)
```
+ 将DataFrame转化为DataSet 
```scala
df.as[Person]
```
### Dataset转为DataFrame
+ 创建一个样例类
```scala
case class Person(name: String,age: Long)
```
+ 创建DataSet
```scala
 val ds = Seq(Person("凌波丽",23)).toDS()
```
+ 将DataSet转化为DataFrame
```scala
var df = ds.toDF
```
## RDD、DataFrame和DataSet之间的关系
+ 同样的数据都给到这三个数据结构，他们分别计算之后，都会给出相同的结果。不同是的他们的执行效率和执行方式。
### 三者的共性
+ RDD、DataFrame、DataSet全都是spark平台下的分布式弹性数据集，为处理超大型数据提供便利;
+ 都有惰性机制，在进行创建、转换，如map方法时，不会立即执行，只有在遇到Action如foreach时，三者才会开始遍历运算
+ 有许多共同的函数，如filter，排序等;
+ 对DataFrame和Dataset进行操作许多操作都需要这个包:import spark.implicits._（在创建好SparkSession对象后尽量直接导入）
+ 根据 Spark 的内存情况自动缓存运算，这样即使数据量很大，也不用担心会内存溢出
+ 都有partition的概念
+ DataFrame和Dataset均可使用模式匹配获取各个字段的值和类型
### 三者的区别
+ RDD								
1. RDD一般和Spark MLib同时使用
2. RDD不支SparkSQL操作
+ DataFrame
1. 与RDD和Dataset不同，DataFrame每一行的类型固定为Row，每一列的值没法直接访问，只有通过解析才能获取各个字段的值
2. DataFrame与DataSet一般不与 Spark MLib 同时使用
3. DataFrame与DataSet均支持 SparkSQL 的操作,比如select,groupby之类,还能注册临时表/视窗,进行sql语句操作
4. DataFrame与DataSet支持一些特别方便的保存方式,比如保存成csv,可以带上表头,这样每一列的字段名一目了然
+ DataSet
1. Dataset和DataFrame拥有完全相同的成员函数，区别只是每一行的数据类型不同。 DataFrame其实就是DataSet的一个特例  
    ```scala
    type DataFrame = Dataset[Row]
    ```
2. DataFrame也可以叫Dataset[Row],每一行的类型是Row，不解析，每一行究竟有哪些字段，各个字段又是什么类型都无从得知，只能用上面提到的getAS方法或者模式匹配拿出特定字段。而Dataset中，每一行是什么类型是不一定的，在自定义了case class之后可以很自由的获得每一行的信息
## 数据的加载与保存
+ spark.read.load 是加载数据的通用方法
+ df.write.save 是保存数据的通用方法
### 加载数据
+ read直接加载数据spark.read

例：直接加载Json数据
```scala
spark.read.json("/opt/module/spark-local/people.json").show
```
+ format指定加载数据类型
```scala
spark.read.format("数据类型")[.option("jdbc参数")].load("路径")
```
+ 在文件上直接运行SQL
```scala
spark.sql("select * from 格式.`路径`").show
```
### 保存数据
+ write直接保存数据

例：直接将df中数据保存到指定目录
```scala
//默认保存格式为parquet
scala> df.write.save("/opt/module/spark-local/output")
//可以指定为保存格式，直接保存，不需要再调用save了
scala> df.write.json("/opt/module/spark-local/output")
```
+ format指定保存数据类型
```scala
df.write.format("数据类型")[.option("jdbc参数")].save("路径")
```
+ 保存选项

|Scala/Java|Any Language|Meaning|
|-----------|-----------|-----------|
|SaveMode.ErrorIfExists(default)|error(default)|如果文件已经存在则抛出异常|
|SaveMode.Append|append|如果文件已经存在则追加|
|SaveMode.Overwrite|overwrite|如果文件已经存在则覆盖|
|SaveMode.Ignore|ignore|如果文件已经存在则忽略|
+ 使用指定format指定保存类型进行保存

例,追加保存
```scala
df.write.mode("append").json("/opt/module/spark-local/output")
```
### 默认数据源
+ Spark SQL的默认数据源为Parquet格式。数据源为Parquet文件时，Spark SQL可以方便的执行所有的操作，不需要使用format。
+ 修改配置项spark.sql.sources.default，可修改默认数据源格式。
### JSON文件查询流程
1. 导入隐式转换

    ```scala
    import spark.implicits._
    ```
2. 加载JSON文件

    ```scala
    val path = "/opt/module/spark/people.json"
    val df = spark.read.json(path)
    ```
3. 创建临时表

    ```scala
    df.createOrReplaceTempView("people")
    ```
4. 数据查询

    ```scala
    val sel = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19")

    sel.show()
    ```
### MySQL
+ 通过JDBC对Mysql进行操作
#### 1. 从JDBC读数据
方式1
```scala
val df1 = spark.read.format("jdbc")
.option("url", "jdbc:mysql://hadoop100:3306/test")
.option("driver", "com.mysql.jdbc.Driver")
.option("user", "root")
.option("password", "myself")
.option("dbtable", "user")
.load()
df1.show()
```
方式2
```scala
val df2 = spark.read.format("jdbc")
.options(
    Map(
    "url" -> "jdbc:mysql://hadoop100:3306/test?user=root&password=myself",
    "driver" -> "com.mysql.jdbc.Driver",
    "dbtable" -> "user"
    )
).load()
df2.show()
```
方式3
```scala
val pro = new Properties()
pro.setProperty("user","root")
pro.setProperty("password","myself")
val df3 = spark.read.jdbc("jdbc:mysql://hadoop100/test", "user", pro)
df3.show()
```
#### 2. 向JDBC写数据
```scala
val rdd = spark.sparkContext.makeRDD(List(person("桥本环奈", 21), person("新恒结衣", 25)))
    import spark.implicits._
//转成df
val df = rdd.toDF()
//转成ds
val ds = rdd.toDS()
ds.write.format("jdbc")
    .option("url", "jdbc:mysql://hadoop100:3306/test")
    .option("driver", "com.mysql.jdbc.Driver")
    .option("user", "root")
    .option("password", "myself")
    .option("dbtable", "user")
    .mode(SaveMode.Append)//追加写入
    .save()
```
### Hive
+  Hive 是 Hadoop 上的 SQL 引擎，Spark SQL编译时可以包含 Hive 支持，也可以不包含。（默认支持）
+ 包含 Hive 支持的 Spark SQL 可以支持 Hive 表访问、UDF (用户自定义函数)以及 Hive 查询语言(HiveQL/HQL)等
+ 内嵌Spark SQL 会在当前的工作目录中创建出自己的 Hive 元数据仓库，叫作 metastore_db。
+ 已安装Hive使用HQL创建表,这些表会根据 hdfs-site.xml 配置存放（默认/user/hive/warehouse） 
#### 使用内嵌Hive 
1. 直接使用无需配置
2. Hive 的元数据存储在 derby 中, 仓库地址:$SPARK_HOME/spark-warehouse
3. 内嵌Spark SQL 会在当前的工作目录中创建出自己的 Hive 元数据仓库metastore_db。
#### 外部Hive应用
1. 确保Hive正常工作
2. 把hive-site.xml拷贝到spark的conf/目录下
3. 把Mysql的驱动copy到Spark的jars/目录下
4. 如果访问不到hdfs，则需要把core-site.xml和hdfs-site.xml拷贝到conf/目录下
#### Spark-Hive在IDEA中运行

添加Hive支持--enableHiveSupport()
```scala
//创建sparkconf配置文件对象
val conf:SparkConf = new SparkConf().setAppName("HIVE").setMaster("local[*]")
//创建Spark入口点对象 SparkSession
val spark:SparkSession = SparkSession.builder().enableHiveSupport().config(conf).getOrCreate()

spark.sql("show tables").show()

//释放资源
spark.stop()
```
### 自定义查询函数
##### 初始化(函数定义使用都应在sparksession范围内)
+ 创建sparkconf配置文件对象
```scala
val conf:SparkConf = new SparkConf().setAppName("test1").setMaster("local[*]")
```
+ 创建Spark入口点对象 SparkSession
```scala
val spark:SparkSession = SparkSession.builder().config(conf).getOrCreate()
```
+ 释放资源
```scala
spark.stop()
```
#### UDF,UDAF,UDTF函数
+ UDF  ----->  1 条数据输入映射1条输出    （一进一出）
+ UDAF   ---> 多条数据输入映射 1 条输出  （多进一出）
+ UDTF -----> 1 条数据输入映射多条输出   （一进多出）
+ SQl语法风格(DataFrame) --需通过df创建临时表
```scala
df.createOrReplaceTempView("视图名")
spark.sql("select * from 视图名").show()
```
+ DSL风格(DataSet)--无需创建临时表
```scala
df.select("属性1","属性2").show()
```
#### 自定义UDF
+ 自定义UDF函数addword(给字符串前添加字符串)，在SQL语句中使用
```scala
val conf:SparkConf = new SparkConf().setAppName("udf").setMaster("local[*]")
val spark:SparkSession = SparkSession.builder().config(conf).getOrCreate()
//创建DF
val df = spark.read.json("F:\\Mywork\\spark\\day08\\datas\\233.json")
//创建临时视图
df.createOrReplaceTempView("user")

// 自定义UDF函数 一条数据一条返回

spark.udf.register("addWord",(name:String)=>{"我是"+name})
//通过sql从视图查询数据`
spark.sql("select addWord(name),age from user").show
//释放资源
spark.stop()
```
#### 自定义UDAF函数
+ 自定义弱类型UDAF函数--方便的应用于Spark SQL
1. StructType--用以指定类型
2. 继承UserDefinedAggregateFunction实现弱类型UDAF

弱类型UDAF函数的定义(求平均值)
```scala
//自定义UDAF函数（弱类型）
class weakavg extends UserDefinedAggregateFunction{
  //聚合函数输入数据类型  -->（输入类型）
  override def inputSchema: StructType = {
    StructType(Array(StructField("age",IntegerType)))
  }
  //缓存数据类型        -->（缓存类型）
  override def bufferSchema: StructType = {
    StructType(Array(StructField("sum",IntegerType),StructField("count",IntegerType)))
  }
  //聚合函数返回的数据类型  -->(输出类型)
    override def dataType: DataType = DoubleType

  //稳定性，默认不处理返回true(相同输入是否会得到相同输出)
  override def deterministic: Boolean = true
  //初始化,缓存设置到初始状态
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    //让缓存中年龄总和归零
   buffer(0)= 0
    //让缓存中总人数总归零
    buffer(1)= 0

  }
  //更新缓存数据
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    if (!buffer.isNullAt(0))
      buffer(0)=buffer.getInt(0)+input.getInt(0)
      buffer(1)=buffer.getInt(1)+1
  }
  //分区间合并
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit ={
    buffer1(0)=buffer1.getInt(0)+buffer2.getInt(0)
    buffer1(1)=buffer1.getInt(1)+buffer2.getInt(1)
  }
  //计算逻辑
  override def evaluate(buffer: Row): Any = {
      buffer.getInt(0).toDouble/buffer.getInt(1)
  }
}
```
弱类型UDAF函数的使用(求平均值)
```scala
//创建sparkconf配置文件对象
val conf:SparkConf = new SparkConf().setAppName("udaf1").setMaster("local[*]")
//创建Spark入口点对象 SparkSession
val spark:SparkSession = SparkSession.builder().config(conf).getOrCreate()

//读取json文件创建DF
val df = spark.read.json("F:\\Mywork\\spark\\day08\\datas\\233.json")
//弱类型:SQL风格--创建临时视图
df.createOrReplaceTempView("user")

//创建自定义函数对象
val av = new weakavg
//注册自定义函数
spark.udf.register("myavg",av)
//使用聚合函数查询
spark.sql("select myavg(age) from user").show()

//释放资源
spark.stop()
```
+ 自定义强类型UDAF函数--主要应用在 DSL
1. 继承Aggregator[-IN, BUF, OUT]实现弱类型UDAF函数
2. IN--输入数据类型 BUF--缓存数据类型 OUT--输出结果数据类型
3. 需要提供样例类
4. 注意：如果从内存中获取数据spark可以知道具体数据类型数字默认int，但从文件中读取的数字无法确定类型只能用bigint接收，bigint可与Long转换但不能和int转换

定义样例类
```scala
//输入类型样例类
case class user(name:String,age:Long)
//缓存类型
case class AgeBuffer(var sum:Long,var count:Long)
```
定义UDAF函数（强类型）实现平均年龄计算 -->需自己封装样例类指定类型
```scala
class strage extends Aggregator[user,AgeBuffer,Double]{
  //缓存数据初始化
  override def zero: AgeBuffer = {
    AgeBuffer(0,0)
  }

//分区内数据聚合             (缓存    查询记录)
  override def reduce(b: AgeBuffer, a: user): AgeBuffer = {
    b.sum=b.sum+a.age
    b.count=b.count+1
    b
  }
  //分区间合并
  override def merge(b1: AgeBuffer, b2: AgeBuffer): AgeBuffer = {
    b1.sum+=b2.sum
    b1.count+=b2.count
    b1
  }
  //返回计算结果
  override def finish(reduction: AgeBuffer): Double = {
    reduction.sum.toDouble/reduction.count
  }

  //DataSet编码及解码器，用于序列化（固定写法）
  //类型     用户自定义ref类型product      
  //        系统值类型,根据具体类型选择
  override def bufferEncoder: Encoder[AgeBuffer] = {
    Encoders.product

  }
  override def outputEncoder: Encoder[Double] = {
    Encoders.scalaDouble
  }
}
```
使用UDAF函数(强类型)求平均值
```scala
//创建sparkconf配置文件对象
val conf:SparkConf = new SparkConf().setAppName("udaf2").setMaster("local[*]")
//创建Spark入口点对象 SparkSession
val spark:SparkSession = SparkSession.builder().config(conf).getOrCreate()
import spark.implicits._
//创建自定义函数对象
val myavg = new strage

//从json创建df,通过样例类转化ds
val df = spark.read.json("F:\\Mywork\\spark\\day08\\datas\\233.json")
val ds = df.as[user]

//自定义函数对象转换成查询列
val col:TypedColumn[user,Double] = myavg.toColumn

//进行查询时将查询出的记录交给自定义函数处理
ds.select(col).show()

//释放资源
spark.stop()
```
