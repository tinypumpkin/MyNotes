# Azkaban的使用
## 启动
### 启动web服务器
```bash
cd /opt/module/azkaban/server
bin/azkaban-web-start.sh
```
### 启动executor服务器
```bash
cd /opt/module/azkaban/executor
bin/azkaban-executor-start.sh
```
### 登录
>输入 https://hadoop100:8443 打开浏览器，输入帐号密码登录
## 使用
### Command类型
> 单job
+ 创建job描述文件(注意描述文件不应有空格)
```bash
vim command.job
```
```job
#command.job
type=command
mkdir -p /home/atguigu/datas/dick
command=echo 'hello azkaban'
```
+ 将job资源文件打包成zip文件
+ 通过azkaban的web管理平台创建project并上传job压缩包
1. 创建project
2. 上传zip包
+ 启动执行该job
> 多job
+ 创建有依赖关系的多个job描述
1. 第一个job：f1.job
```job
# f1.job
type=command
command=mkdir -p /home/atguigu/datas/f1
```
2. 第二个job：f2.job依赖f1.job
```job
# f2.job
type=command
dependencies=f1
command=mkdir -p /home/atguigu/datas/f1/f2
```
+ 将所有job资源文件打到一个zip包中
+ 创建project -->上传zip包 -->启动执行该job
### 脚本任务
+ 创建job描述文件和shell脚本
1. 创建shell脚本 233.sh
```bash
#!/bin/bash
mkdir -p /home/atguigu/datas/file
```
2. Job描述文件：test.job
```bash
# test.job
type=command
command=bash 233.sh
```
+ 将所有job资源文件打到一个zip包中
+ 创建project -->上传zip包 -->启动执行该job
### Hive脚本任务
+ 创建job描述文件和hive脚本
1. Hive脚本： t1.sql
```sql
use vaild;
create table azk(
    `hive` string,
    `job` string
)
PARTITIONED BY (`dt` string)
row format delimited fields terminated by '\t'
STORED AS
  INPUTFORMAT 'com.hadoop.mapred.DeprecatedLzoTextInputFormat'
  OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
location '/warehouse/mytest/azkaban';
```
2. Job描述文件：dis.job
```job
# dis.job
type=command
command=/opt/module/hive/bin/hive -f 't1.sql'
```
+ 将所有job资源文件打到一个zip包中
+ 创建project -->上传zip包 -->启动执行该job