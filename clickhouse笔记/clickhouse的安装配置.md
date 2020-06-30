# clickhouse的安装
>准备工作
+ CentOS取消打开文件数限制
```bash
vim /etc/security/limits.conf
```
```bash
# 在文件末尾添加
* soft nofile 65536 
* hard nofile 65536 
* soft nproc 131072 
* hard nproc 131072
```
```bash
vim /etc/security/limits.d/90-nproc.conf
```
```bash
# 在文件末尾添加
* soft nofile 65536 
* hard nofile 65536 
* soft nproc 131072 
* hard nproc 131072
```
+ CentOS取消SELINUX 
```bash
vim /etc/selinux/config
```
```config
<!-- 修改SELINUX为disabled -->
SELINUX=disabled
```
+ 重启
+ 关闭防火墙 
+ 安装依赖
```bash
yum install -y libtool
yum install -y *unixODBC*
```
>单机安装
+ 下载安装rpm包 ---> 官网：<https://clickhouse.yandex/>
+ 上传rpm服务器/opt/software/rpm
+ 进入/opt/softwarerpm执行
```bash
# 安装所有rpm包
sudo rpm -ivh *.rpm
```
+ 修改配置文件 -- 允许外网访问
```bash
sudo vim /etc/clickhouse-server/config.xml
```
```xml
<!-- 让clickhouse被除本机以外的服务器访问 -->
<listen_host>::</listen_host>
```
>运行启动
+ 启动ClickServer
```bash
sudo systemctl start clickhouse-server
```
+ 使用client连接server
```bash
clickhouse-client -m
```
+ 关闭开机自启
```bash
sudo systemctl disable clickhouse-server 
```
>集群安装--副本
+ 需要启动zookeeper集群 和另外一台clickhouse服务器
+ 两台服务器的/etc/clickhouse-server/config.d目录下创建一个名为metrika.xml的配置文件
```bash
vim /etc/clickhouse-server/config.d/metrika.xml
```
```xml
<?xml version="1.0"?>
<yandex>
  <zookeeper-servers>
     <node index="1">
	     <host>hadoop100</host>
		 <port>2181</port>
     </node>
	 <node index="2">
	     <host>hadoop101</host>
		 <port>2181</port>
     </node>
  </zookeeper-servers>
</yandex>
```
+ 修改/etc/clickhouse-server/config.xml 
```bash
vim /etc/clickhouse-server/config.xml 
```
```xml
<zookeeper incl="zookeeper-servers" optional="true" />
<include_from>/etc/clickhouse-server/config.d/metrika.xml</include_from>
```
+ 两台服务器分别建表
1. Server-A
```sql
create table rep_t_order_mt(
    id UInt32,
    sku_id String,
    total_amount Decimal(16,2),
    create_time  Datetime
 ) engine =ReplicatedMergeTree('/clickhouse/tables/01/rep_t_order_mt','rep_hadoop')
 partition by toYYYYMMDD(create_time)
   primary key (id)
   order by (id,sku_id);
```
2. Server-B
```sql
 create table rep_t_order_mt(
    id UInt32,
    sku_id String,
    total_amount Decimal(16,2),
    create_time  Datetime
 ) engine =ReplicatedMergeTree('/clickhouse/tables/01/rep_t_order_mt','rep_hadoop')
 partition by toYYYYMMDD(create_time)
   primary key (id)
   order by (id,sku_id);
```
此时修改一个,另一个也会变化，通过zookeeper同步互为副本（高可用）

>集群安装--分片集群
```bash
vim /etc/clickhouse-server/config.d/metrika.xml
```
+ 6节点版本--完全体

1. 优先选择出错少的，默认选择随机
2. 配置文件指定集群名是gmall_cluster
```xml
<yandex>
<clickhouse_remote_servers>
<gmall_cluster> <!-- 集群名称--> 
  <shard>         <!--集群的第一个分片-->
<internal_replication>true</internal_replication>
     <replica>    <!--该分片的第一个副本-->
          <host>hadoop100</host>
          <port>9000</port>
     </replica>
     <replica>    <!--该分片的第二个副本-->
          <host>hadoop101</host>
          <port>9000</port>
     </replica>
  </shard>

  <shard>  <!--集群的第二个分片-->
     <internal_replication>true</internal_replication>
     <replica>    <!--该分片的第一个副本-->
          <host>hadoop102</host>
          <port>9000</port>
     </replica>
     <replica>    <!--该分片的第二个副本-->
          <host>hadoop103</host>
          <port>9000</port>
     </replica>
  </shard>

  <shard>  <!--集群的第三个分片-->
     <internal_replication>true</internal_replication>
     <replica>    <!--该分片的第一个副本-->
          <host>hadoop104</host>
          <port>9000</port>
     </replica>
     <replica>    <!--该分片的第二个副本-->
          <host>hadoop105</host>
          <port>9000</port>
     </replica>
  </shard>

</gmall_cluster>

</clickhouse_remote_servers>
</yandex> 
```
+ 3 节点版本配置
1. 修改/etc/clickhouse-server/config.xml 指定分区配置文件metrika-shard
```bash
vim /etc/clickhouse-server/config.xml 
```
```xml
<zookeeper incl="zookeeper-servers" optional="true" />
<include_from>/etc/clickhouse-server/config.d/metrika-shard.xml</include_from>
```
2. 修改配置metrika-shard.xml（对不同节点做分片或副本仅通过更改`宏`实现）
```bash
vim /etc/clickhouse-server/config.d/metrika-shard.xml
```
```xml
<yandex>
<clickhouse_remote_servers>
<gmall_cluster> <!-- 集群名称--> 
  <shard>         <!--集群的第一个分片-->
<internal_replication>true</internal_replication>
     <replica>    <!--该分片的第一个副本-->
          <host>hadoop100</host>
          <port>9000</port>
     </replica>
     <replica>    <!--该分片的第二个副本-->
          <host>hadoop101</host>
          <port>9000</port>
     </replica>
  </shard>

  <shard>  <!--集群的第二个分片-->
     <internal_replication>true</internal_replication>
     <replica>    <!--该分片的第一个副本-->
          <host>hadoop102</host>
          <port>9000</port>
     </replica>
</shard>

</gmall_cluster>
</clickhouse_remote_servers>

<zookeeper-servers>
  <node index="1">
    <host>hadoop100</host>
    <port>2181</port>
  </node>
  <node index="2">
    <host>hadoop101</host>
    <port>2181</port>
  </node>
  <node index="3">
    <host>hadoop102</host>
    <port>2181</port>
  </node>
</zookeeper-servers>
<!-- 配置宏 -->
<macros>
<shard>01</shard>   <!--不同机器放的分片数不一样-->
<replica>rep_1_1</replica>  <!--不同机器放的副本数不一样-->
</macros>
</yandex> 
```
![4.png](https://i.loli.net/2020/06/30/iNOVqLuDnIw5WX8.png)
配置宏
|hadoop100|hadoop101|hadoop102|
|---|---|---|
|分区-01,副本-rep_1_1|分区-01,副本-rep_1_2|分区-02,副本-rep_2_1|

+ 集群同步分发（复制）
```sql
create table my_table on cluster gmall_cluster (
id UInt32,
sku_id String,
total_amount Decimal(16,2),
create_time  Datetime
) engine =ReplicatedMergeTree('/clickhouse/tables/{shard}/my_table','{replica}')
partition by toYYYYMMDD(create_time)
primary key (id)
order by (id,sku_id);
```
+ 分布式建表

Distributed( 集群名称，库名，本地表名，分片键)

分片键(分区依据)必须是整型数字,也可以是随机数rand()

```sql
create table clust_table on cluster gmall_cluster(
    id UInt32,
    sku_id String,
    total_amount Decimal(16,2),
    create_time  Datetime)
engine = Distributed(gmall_cluster,mydb,clust_table,hiveHash(sku_id))
```
插入数据
```sql
insert into clust_table 
values(201,'sku_001',1000.00,'2020-06-01 12:00:00') ,
(202,'sku_002',2000.00,'2020-06-01 12:00:00'),
(203,'sku_004',2500.00,'2020-06-01 12:00:00'),
(204,'sku_002',2000.00,'2020-06-01 12:00:00')
(205,'sku_003',600.00,'2020-06-02 12:00:00')
```
通过查询分布式表语句
```sql
SELECT *  FROM clust_table
```