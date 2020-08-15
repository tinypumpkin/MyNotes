# 手写HQL
### 第一题：找出所有科目成绩都大于某一学科平均成绩的学生

|uid（学生ID）|subject_id（科目ID）|score|
|--|--|--|
|1001|01|90|
|1001|02|90|
|1001|03|90|
|1002|01|85|
|1002|02|85|
|1002|03|70|
|1003|01|70|
|1003|02|70|
|1003|03|85|

1. 建表语句
```sql
create table score(
    uid string,
    subject_id string,
    score int)
row format delimited fields terminated by '\t'; 
```
2. 求出每个学科平均成绩
```mysql 
(select 
uid,myavg，source
avg(source) over(partition by subject_id) myavg
from source)t1	
```
3. 根据是否大于平均成绩记录flag，大于则记为0否则记为1
```sql
(select
    uid,
    if(score>myavg,0,1) flag
from
    t1)t2
```
4. 根据学生id进行分组统计flag的和，和为0则是所有学科都大于平均成绩
```sql
select uid from t2 group by uid hiving sum(flag)=0
```
### 第二题：使用SQL统计出每个用户的累积访问次数
> 用户访问数据(原始表)

|userId|visitDate|visitCount|
|--|--|--|
|u01|2017/1/21|5|
|u02|2017/1/23|6|
|u03|2017/1/22|8|
|u04|2017/1/20|3|
|u01|2017/1/23|6|
|u01|2017/2/21|8|
|U02|2017/1/23|6|
|U01|2017/2/22|4|

> 要求使用SQL统计出每个用户的累积访问次数，如下表所示

|用户id|月份|小计|累积|
|--|--|--|--|
|u01|2017-01|11|11|
|u01|2017-02|12|23|
|u02|2017-01|12|12|
|u03|2017-01|8|8|
|u04|2017-01|3|3|

+ 创建表
```sql
create table action
(userId string,
visitDate string,
visitCount int) 
row format delimited fields terminated by "\t";
```
+ 修改表格式
```sql
(select
     userId,
     date_format(regexp_replace(visitDate,'/','-'),'yyyy-MM') mn,
     visitCount
from
     action)t1
```
+ 单人单月访问量
```sql
(select userid,sum(visitCount) vis from t1 group by userid)t2
```
+ 按月累计访问量
```sql
select 
userid,
vis,
visitDate
sum(v) over(partition by visitDate) v
from 
t2
```