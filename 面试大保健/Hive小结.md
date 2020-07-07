# Hive总结
## 一，Hive的组成
### 引擎
1. mr基于磁盘  -->(统计周，月，年等数据大的场景)
2. tez基于内存 --> 数据量小的场景（易发生ORM）
3. spark基于磁盘（suffle）+内存
+ 企业绝大多数场景使用spark（主要处理当天数据）
---
## 二，Hive和Mysql与HBase的区别
||Hive|MySQL|
|---|----|----|
|`数据量`|大|相对小|
|`速度`|大数据场景快|小数据场景快|
|`使用场景`|查询|增删改查|
+ Hive仅仅擅长查询
---
## 三，内部表与外部表的区别
### 内部表包含元数据原始元素，外部表只是关系映射
1. 内部表删数据-->元数据和原始数据
2. 外部表删数据-->只删元数据
3. 企业中何时使用内部表何时使用外部表
+ 生产环境中绝大多数使用外部表，内部表仅自己使用（测试时）
---
## 四，4个by
1. order by     -->排序(全局)
2. sort by      -->排序（局部）
3. distribute by-->分区（分区内排序）
4. class by -->字段相同，分区+排序
---
## 五，函数
1. 系统函数（日，周，月，年，等......）
2. 自定义函数
> 自定义UDF（一进一出）

  + 定义类继承UDF重写evaluate方法

> 自定义UDTF（一进多出）

  + 定义类继承UDTF重写3个方法（初始化，procress，关闭）

>  在项目中的作用
3. 窗口函数
  + rank
  + over
  + 要求-->能查询topN
4. `优化`
> Mapjoin（默认开启，无需关闭）

>行列过滤--给定一个join+where的查询

  + 优化：先where减小范围再join
>创建分区表（防止扫描全部数据）

>分桶（对数据量大的数据进行采样）

>合理设置map个数和reduce个数

  + 128mb数据-->1个maptask-->1g内存
  + 切片(max(0,min块大小,long的最大值))
  + 对reduce的需求测试
5. 处理小文件
> combineHiveInputformat

> Jvm重用

>merge

  + maponly任务默认开启
  + 将小于16m的文件合并到256m
  + MapReduce任务需手动开启
6. 压缩,减小磁盘空间,减小网络传输
7. 列式存储
  + 查询速度快(parquet,orc)
  + 企业最低十元orc(增加压缩比例，查询快)
8. 开启map端conbiner
  + 提前合并(在不影响业务逻辑前提下)
  + set hive.map.aggr=true
***
## 六，数据倾斜
1. 数据倾斜的产生
  + 不同数据类型相关联（join两表时将string类型id按int型处理join到同一Reduce中）

> 解决方法-->

  + 1 强转
  + 2 group by代替distinct group
  + 3 开启mapjoin 
  + 4 开启数据倾斜时负载均衡-->set hive.groupby.skwindate=true
	

`负载均衡细节`：Map--随机数-->Reduce(打散)-->Map-->Reduce(最终聚合)