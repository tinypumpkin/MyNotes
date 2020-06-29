# clickhouse的笔记
## 一、clickhouse的简介
>cickhouse是什么
+ 开源的列式存储数据库（DBMS），使用C++语言编写，主要用于在线分析处理查询（OLAP），能够使用SQL查询实时生成分析数据报告。
>Clickhouse的特点
+ 列式存储

以下面的表为例：

|Id|Name|Age|
|---|---|---|
|1|张三|18|
|2|李四|22|
|3|王五|34|
+ 采用行式存储时，数据在磁盘上的组织结构为：

|ID|name|age|ID|name|age|ID|name|age|
|--|--|--|--|--|--|--|--|--|
|1|张三|18|2|李四|22|3|王五|34|

好处是想查某个人所有的属性时，可以通过一次磁盘查找加顺序读取就可以。但是当想查所有人的年龄时，需要不停的查找，或者全表扫描才行，遍历的很多数据都是不需要的。
+ 采用列式存储时，数据在磁盘上的组织结构为：

|ID|ID|ID|name|name|name|age|age|age|
|--|--|--|--|--|--|--|--|--|
|1|2|3|张三|李四|王五|18|22|34|

这时想查所有人的年龄只需把年龄那一列拿出来就可以了

+ 列式储存的好处：
1. 对于列的聚合，计数，求和等统计操作原因优于行式存储。
2. 由于某一列的数据类型都是相同的，针对于数据存储更容易进行数据压缩，每一列选择更优的数据压缩算法，大大提高了数据的压缩比重。
3. 由于数据压缩比更好，一方面节省了磁盘空间，另一方面对于cache也有了更大的发挥空间。

+ DBMS的功能
1. 几乎覆盖了标准SQL的大部分语法，包括 DDL和 DML ,以及配套的各种函数（不支持开窗）。
2. 用户管理及权限管理
3. 数据的备份与恢复

+ 多样化引擎 

clickhouse和mysql类似，把表级的存储引擎插件化，根据表的不同需求可以设定不同的存储引擎。目前包括合并树、内存、文件、接口和其他六大类20多种引擎。

+ 高吞吐写入能力

ClickHouse采用类LSM Tree的结构，数据写入后定期在后台Compaction。通过类LSM tree的结构，ClickHouse在数据导入时全部是顺序append写，写入后数据段不可更改，在后台compaction时也是多个段merge sort后顺序写回磁盘。顺序写的特性，充分利用了磁盘的吞吐能力，即便在HDD上也有着优异的写入性能。

+ 数据分区与线程级并行

ClickHouse将数据划分为多个partition，每个partition再进一步划分为多个index granularity，然后通过多个CPU核心分别处理其中的一部分来实现并行数据处理。

在这种设计下，单条Query就能利用整机所有CPU。极致的并行处理能力，极大的降低了查询延时。

所以，clickhouse即使对于大量数据的查询也能够化整为零平行处理。但是有一个弊端就是对于单条查询使用多cpu，就不利于同时并发多条查询。所以对于高qps的查询业务，clickhouse并不是强项（适用于低频复杂度高的大数据量查询）。

clickhouse与很多OLAP数据库一样，单表查询速度优于关联查询

### 数据类型
#### 整型
>固定长度的整型，包括有符号整型或无符号整型。

整型范围（-2n-1~2n-1-1）：
+ Int8 - [-128 : 127]
+ Int16 - [-32768 : 32767]
+ Int32 - [-2147483648 : 2147483647]
+ Int64 - [-9223372036854775808 : 9223372036854775807]
>无符号整型范围（0~2n-1）：
+ UInt8 - [0 : 255]
+ UInt16 - [0 : 65535]
+ UInt32 - [0 : 4294967295]
+ UInt64 - [0 : 18446744073709551615]

使用场景： 个数、数量、也可以存储型id。
#### 浮点型
+ Float32 - float
+ Float64 – double

建议尽可能以整数形式存储数据。例如，将固定精度的数字转换为整数值，如时间用毫秒为单位表示，因为浮点型进行计算时可能引起四舍五入的误差。

使用场景：一般数据值比较小，不涉及大量的统计计算，精度要求不高的时候。比如保存商品的重量。

#### 布尔型

没有单独的类型来存储布尔值。可以使用 UInt8 类型，取值限制为 0 或 1。

#### Decimal 型

有符号的浮点点数，可在加、减和乘法运算过程中保持精度。对于除法，最低有效数字会被丢弃（不舍入）。
+ 三种声明(s标识小数位)：
1. Decimal32(s)，相当于Decimal(9-s,s)
2. Decimal64(s)，相当于Decimal(18-s,s)
3. Decimal128(s)，相当于Decimal(38-s,s)

使用场景： 一般金额字段、汇率、利率等字段为了保证小数点精度，都使用Decimal进行存储。 
#### 字符串
+ String

字符串可以任意长度的。它可以包含任意的字节集，包含空字节。
+ FixedString(N)

固定长度 N 的字符串，N 必须是严格的正自然数。当服务端读取长度小于 N 的字符串时候，通过在字符串末尾添加空字节来达到 N 字节长度。 当服务端读取长度大于 N 的字符串时候，将返回错误消息。

与String相比，极少会使用FixedString，因为使用起来不是很方便。
使用场景：名称、文字描述、字符型编码。 固定长度的可以保存一些定长的内容，比如一些编码，性别等但是考虑到一定的变化风险，带来收益不够明显，所以定长字符串使用意义有限。
#### 枚举类型

包括 Enum8 和 Enum16 类型。Enum 保存 'string'= integer 的对应关系。
+ Enum8 用 'String'= Int8 对描述。
+ Enum16 用 'String'= Int16 对描述。

用法演示：--> 创建一个带有一个枚举 Enum8('hello' = 1, 'world' = 2) 类型的列：
```sql
CREATE TABLE t_enum
(
    x Enum8('hello' = 1, 'world' = 2)
)
ENGINE = TinyLog
```
注意：这个 `x 列`只能存储类型定义中列出的值：'hello'或'world'。如果尝试保存任何其他值，ClickHouse 抛出异常。

+ 从表中查询数据时，ClickHouse 从 Enum 中输出字符串值。
```sql
SELECT * FROM t_enum
```
+ 如果需要看到对应行的数值，则必须将 Enum 值转换为整数类型。
```sql
SELECT CAST(x, 'Int8') FROM t_enum
```
使用场景：对一些状态、类型的字段算是一种空间优化，也算是一种数据约束。但是实际使用中往往因为一些数据内容的变化增加一定的维护成本，甚至是数据丢失问题。所以谨慎使用。


#### 时间类型

目前clickhouse 有三种时间类型
+ Date 接受 年-月-日 的字符串比如 ‘2019-12-16’
+ Datetime 接受 年-月-日 时:分:秒 的字符串比如 ‘2019-12-16 20:50:10’
+ Datetime64 接受 年-月-日 时:分:秒.亚秒 的字符串比如 ‘2019-12-16 20:50:10.66’

+ 日期类型，用两个字节存储，表示从 1970-01-01 (无符号) 到当前的日期值。

更多数据结构，可以参考文档：<https://clickhouse.yandex/docs/zh/data_types>

#### 数组
+ Array(T)：由 T 类型元素组成的数组。

T 可以是任意类型，包含数组类型。 但不推荐使用多维数组，ClickHouse 对多维数组的支持有限。(`不能在 MergeTree表中存储多维数组`)
+ 使用array函数来创建数组：
```sql
-- array(T)
SELECT array(1, 2) AS x, toTypeName(x)
```
+ 使用方括号：
```sql
-- []
SELECT [1, 2] AS x, toTypeName(x)
```
## 二、表引擎
>表引擎的使用

表引擎是clickhouse的一大特色。可以说， 表引擎决定了如何存储标的数据。包括：
1. 数据的存储方式和位置，写到哪里以及从哪里读取数据
2. 支持哪些查询以及如何支持。
3. 并发数据访问。
4. 索引的使用（如果存在）。
5. 是否可以执行多线程请求。
6. 数据复制参数。

表引擎的使用方式就是`必须定义创建表时定义该表所使用的引擎，以及引擎使用的相关参数`。
```sql
create table t_tinylog ( id String, name String) engine=TinyLog;
```
特别注意：引擎的名称大小写敏感
> TinyLog

以列文件的形式保存在磁盘上，不支持索引，没有并发控制。一般保存少量数据的小表，生产环境上作用有限。可以用于平时练习测试用。
> Memory

内存引擎，数据以未压缩的原始形式直接保存在内存当中，服务器重启数据就会消失。读写操作不会相互阻塞，不支持索引。简单查询下有非常非常高的性能表现（超过10G/s）。
一般用到它的地方不多，除了用来测试，就是在需要非常高的性能，同时数据量又不太大（上限大概 1 亿行）的场景。

 >`MergeTree`

 Clickhouse 中`最强大的表引擎`当属 MergeTree （合并树）引擎及该系列（*MergeTree）中的其他引擎。地位可以相当于innodb之于Mysql。 而且基于MergeTree，还衍生除了很多小弟，也是非常有特色的引擎。
 + 建表语句
```sql
create table t_Merge(
    id UInt32,
    sku_id String,
    total_amount Decimal(16,2),
    create_time  Datetime
 ) engine =MergeTree --必填参数-1
 partition by toYYYYMMDD(create_time)
   primary key (id) --必填参数-2
   order by (id,sku_id) --必填参数-3
```
```sql
insert into  t_Merge
values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,
(102,'sku_002',2000.00,'2020-06-01 11:00:00'),
(102,'sku_004',2500.00,'2020-06-01 12:00:00'),
(102,'sku_002',2000.00,'2020-06-01 13:00:00')
(102,'sku_002',12000.00,'2020-06-01 13:00:00')
(102,'sku_002',600.00,'2020-06-02 12:00:00')
```
MergeTree有很多参数(绝大多数用默认值),但是三个参数(engine,primary key,order by)必填
> ReplacingMergeTree -- 去重

MergeTree的一个变种，它存储特性完全继承MergeTree，只是多了一个去重的功能。

尽管MergeTree可以设置主键，但是primary key只作为一级索引,没有唯一约束的功能。如果要处理掉重复的数据，可以借助这个ReplacingMergeTree。
+ 支持分区（单机，按物理目录分区）
+ 去重时机

数据的去重只会在合并的过程中出现。合并会在未知的时间在后台进行，所以无法预先作出计划。有一些数据可能仍未被处理。

+ 去重范围

如果表经过了分区，则去重只会在分区内部进行去重，不能执行跨分区的去重。

所以ReplacingMergeTree能力有限， ReplacingMergeTree 适用于在后台清除重复的数据以节省空间，但是它`不保证没有重复的数据出现`。
+ ReplacingMergeTree() 中的参数为`版本字段`,重复数据保留版本保留最大的值,如果不填版本字段，默认保留最后一条

```sql
create table t_rep(
id UInt32,
sku_id String,
total_amount Decimal(16,2) ,
create_time  Datetime ) 
engine =ReplacingMergeTree(create_time)
partition by toYYYYMMDD(create_time)
primary key (id)
order by (id, sku_id)
```
```sql
insert into  t_rep
values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,
(102,'sku_002',2000.00,'2020-06-01 11:00:00'),
(102,'sku_004',2500.00,'2020-06-01 12:00:00'),
(102,'sku_002',2000.00,'2020-06-01 13:00:00')
(102,'sku_002',12000.00,'2020-06-01 13:00:00')
(102,'sku_002',600.00,'2020-06-02 12:00:00')
```
```sql
OPTIMIZE TABLE t_rep FINAL
```
+ 结论
1. 使用order by 字段作为唯一键。
2. 只有合并分区才会进行去重。
3. 认定重复的数据保留，版本字段值最大的。
4. 如果版本字段相同则保留最后一笔。
>SummingMergeTree -- 预聚合

对于`不查询明细，只关心以维度进行汇总聚合结果`的场景。如果只使用普通的MergeTree的话，无论是存储空间的开销，还是查询时临时聚合的开销都比较大。
Clickhouse 为了这种场景，提供了一种能够`预聚合`的引擎，SummingMergeTree.
+ 表定义
```sql
create table t_smt(
    id UInt32,
    sku_id String,
    total_amount Decimal(16,2) ,
    create_time  Datetime 
 ) engine =SummingMergeTree(total_amount)
 partition by toYYYYMMDD(create_time)
   primary key (id)
   order by (id,sku_id )
```
```sql
insert into  t_smt
values(101,'sku_001',1000.00,'2020-06-01 12:00:00') ,
(102,'sku_002',2000.00,'2020-06-01 11:00:00'),
(102,'sku_004',2500.00,'2020-06-01 12:00:00'),
(102,'sku_002',2000.00,'2020-06-01 13:00:00')
(102,'sku_002',12000.00,'2020-06-01 13:00:00')
(102,'sku_002',600.00,'2020-06-02 12:00:00')
```
手动触发合并
```sql
optimize table t_smt final;
```
+ 结论
1. 以SummingMergeTree（）中指定的列作为汇总数据列,可填写多列但必须是数字列，不填则以所有非维度列且为数字列的字段为汇总数据列。
2. 以order by 的列为准，作为维度列。
3. 其他的列保留最后一行。
4. 不在一个分区的数据不会被聚合。


#### 表引擎的相关参数:
>partition by 分区 （可选项）
+ 作用--降低扫描的范围，优化查询速度。(不填只会使用一个分区)
+ 分区目录--MergeTree 以`列文件+索引文件+表定义文件`组成的，但是如果设定了分区那么这些文件就会保存到不同的分区目录中。
+ 并行：分区后，面对涉及跨分区的查询统计，clickhouse会以分区为单位并行处理。
  
+ 数据写入与分区合并：

任何一个批次的数据写入都会产生一个临时分区，不会纳入任何一个已有的分区。写入后的某个时刻（大概10-15分钟后），clickhouse会自动执行合并操作（等不及也可以手动通过optimize执行），把临时分区的数据，合并到已有分区中。
```sql
optimize table xxxx [final]
```
>primary key主键(可选)

+ clickhouse中的主键

与其他数据库不太一样，它`只提供了数据的一级索引，但是却不是唯一约束`。这就意味着是可以存在相同primary key的数据的。

+ 主键的设定主要依据是查询语句中的where 条件

根据条件通过对主键进行某种形式的二分查找，能够定位到对应的index granularity,避免了全包扫描。

+ index granularity(索引粒度)

指在稀疏索引中两个相邻索引对应数据的间隔。clickhouse中的MergeTree默认是8192。官方不建议修改这个值，除非该列存在大量重复值，比如在一个分区中几万行才有一个不同数据。

+  稀疏索引：

![1.png](https://i.loli.net/2020/06/28/NR2vjDQustkl3Jm.png)

用很少的索引数据，定位更多的数据，代价就是只能定位到索引粒度的第一行，然后再进行进行一点扫描。

> order by （必选）

order by是MergeTree中唯一一个必填项，甚至比primary key 还重要，因为当用户不设置主键的情况，很多处理会依照order by的字段进行处理（去重和汇总）。
+ 要求：主键必须是order by字段的前缀字段。

如order by 字段是 (id,sku_id)  那么主键必须是id 或者(id,sku_id)

>二级索引

目前在clickhouse的官网上二级索引的功能是被标注为实验性的。
+ 所以使用二级索引前需要增加设置
```sql
set allow_experimental_data_skipping_indices=1;
```
```sql
 create table t_index_2(
    id UInt32,
    sku_id String,
    total_amount Decimal(16,2),
    create_time  Datetime,
	INDEX a total_amount TYPE minmax GRANULARITY 5
 ) engine =MergeTree
 partition by toYYYYMMDD(create_time)
   primary key (id)
   order by (id, sku_id)
```
+ 其中GRANULARITY N 是设定二级索引对于一级索引粒度的粒度。 

+ 通过shell(客户端执行sql)可以查看效果-->二级索引能够为非主键字段的查询发挥作用。
```bash
clickhouse-client  --send_logs_level=trace <<< 'select * from dbname.t_index_2  where total_amount > 1000'
```
>数据TTL

TTL即Time To Live，MergeTree提供了可以管理数据或者列的生命周期的功能。
+ 列级别TTL(创建与导入)
```sql
create table t_ttl(
id UInt32,
sku_id String,
total_amount Decimal(16,2)  TTL create_time+interval 10 SECOND,
create_time  Datetime 
) engine =MergeTree
partition by toYYYYMMDD(create_time)
primary key (id)
order by (id, sku_id)
```
```sql
insert into  t_ttl
values(106,'sku_001',1000.00,'2020-06-12 22:52:30') ,
(107,'sku_002',2000.00,'2020-06-12 22:52:30'),
(110,'sku_003',600.00,'2020-06-13 12:00:00')
```
+ 表级TTL--针对整张表

例-下面的这条语句是数据会在create_time 之后10秒丢失
```sql
alter table t_ttl MODIFY TTL create_time + INTERVAL 10 SECOND;
```
涉及判断的字段必须是Date或者Datetime类型，推荐使用分区的日期字段。
能够使用的时间周期：

SECOND，MINUTE，HOUR，DAY，WEEK，MONTH，QUARTER，YEAR 

## 三、SQL操作
> Insert 
+ 插入操作--基本与标准SQL一致
```sql
--标准插入
insert into [表名]values(...),(...)
--表到表
insert into  [表1] select a,b,c from [表2]
```
> Update 和 Delete 
+ Mutation查询 -- 可以看做Alter 的一种。

虽然可以实现修改和删除，但是和一般的OLTP数据库不一样，Mutation语句是一种很`重`的操作，而且`不支持事务`。

`重`的原因主要是每次修改或者删除都会导致放弃目标数据的原有分区，重建新分区。所以尽量做批量的变更，不要进行频繁小数据的操作（`修改数据结构`）。

+ 删除操作
```sql
alter table t_smt delete where sku_id ='sku_001';
```
+ 修改操作
```sql
alter table t_smt 
update total_amount=toDecimal32(2000.00,2) 
where id =102;
```
由于操作比较`重`，所以 Mutation语句分两步执行，
1. 标记-->新增数据新增分区和并把旧分区打上逻辑上的失效标记。
2. 执行-->触发分区合并时，删除标记失效的旧数据释放磁盘空间。
>select -- 查询操作
+ 支持子查询
+ 支持CTE(with 子句) 
+ 支持各种JOIN， 但是JOIN操作无法使用缓存,所以即使是两次相同的JOIN语句,Clickhouse也会视为两条新SQL。
+ 不支持窗口函数。
+ 不支持自定义函数。
+ GROUP BY 操作增加了 `with rollup\with cube\with total` 用来计算小计和总计。

with rollup : `从右至左`去掉维度进行小计
```sql
select id , sku_id,sum(total_amount) from  t_smt group by id,sku_id with rollup;
```
 with cube : 先`从右至左去掉维度`进行小计，再从`左至右去掉维度`进行小计
 ```sql
 select id , sku_id,sum(total_amount) from  t_smt group by id,sku_id with cube;
 ```
 with totals: 只计算合计
 ```sql
 select id,sku_id,sum(total_amount) from  t_smt group by id,sku_id with totals;
 ```
>Alter -- 同mysql的修改字段基本一致

新增字段
```sql
alter table 表名  add column  列名  String after col1
```
修改字段类型
```sql
alter table 表名  modify column  列名  String    ；
```
删除字段
```sql
alter table 表名  drop column  列名   ;
```
>导出数据--客户端导出
```bash
clickhouse-client  --query    "select Hour(create_time) hr  ,count(*) from 库名.order_wide where dt='2020-06-23'  group by hr" --format CSVWithNames> ~/out.csv
```
## 副本
保障数据的高可用性，即使一台clickhouse节点宕机，那么也可以从其他服务器获得相同的数据。
>副本写入流程

client -- 写入数据-->clickhouse1-->zookeeper-->clickhouse2
