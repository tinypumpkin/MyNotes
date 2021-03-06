# Hive习题练习
#### 例1： 以此表格为准求那些顾客连续两天来过此店
```sql
create external table business
(name string,orderdate date,cost int)
row format delimited fields terminated by ','
load data local inpath "/opt/module/datas/busin.txt" into table business;

jack,2017-01-01,10
tony,2017-01-02,15
jack,2017-02-03,23
tony,2017-01-04,29
jack,2017-01-05,46
jack,2017-04-06,42
tony,2017-01-07,50
jack,2017-01-08,55
mart,2017-04-08,62
mart,2017-04-09,68
neil,2017-05-10,12
mart,2017-04-11,75
neil,2017-06-12,80
mart,2017-04-13,94
```
+ 以name分区分割组内按日期排序，先排号
```sql
select *,
--用到 row_numer函数
row_number over(partition by name order by orderdate) nu 
from business 
```
+ 以日期减去分组后的编号，求日期相减
```sql
select *,
date_sub(current_date(),nu) dat
from t1
```
+ 将其按姓名，日期 分组过滤 计数=2天的输出
```sql
select name,count(*) c
from t2
group by name,dat having c>=2;
```
### 谷粒影音习题
![1.png](https://i.loli.net/2020/06/12/rFwbWyZSfOGUACR.png)
1. 统计视频观看数Top10
```sql
select * from gulivideo_orc order by views desc limit 10; 
```
2. 统计视频类别热度Top10 
```sql
--没有用子查询
select video_id,sum(views)  hots
from gulivideo_orc 
later view 
explode(category) cas as ca  group by ca
order by hots
desc limit 10;

--利用子查询
(select category,views
from gulivideo_orc
later view 
explode(category) cas as ca)t1

(select category, sum(views) hot from t1 group by t1.ca)t2

select category,hot from t2 order by t2.hot desc limit 10

```
3.  统计出视频观看数最高的20个视频的所属类别以及类别包含Top20视频的个数
```sql
--1 先找到观看数最高的20个视频所属条目的所有信息，降序排列
--2 把这20条信息中的category分裂出来(列转行)
--3 最后查询视频分类名称和该分类下有多少个Top20的视频

(select * from gulivideo_orc order by views desc limit 20;)t1
select 
category count(*)
from t1 
later view 
explode(category) cas as ca
group by ca 
```
4.  统计视频观看数Top50所关联视频的所属类别排序
```sql
--前50观看的视频
(select * from gulivideo_orc 
order by views desc limit 50)t1

--炸开关联,join视频id
(select 
distinct r_id,
category
explode(related ids) r_id
from t1.related_ids
join
(select videoid from t1)
on videoid=r_id)t2
--炸开类别
(select explode(category) cat from t2)t3
--join类别热度表排序
(select 
cat,sum(views) hot
from gulivideo_orc
explode(category)cat
where views group by cat)t4
--最终表
select distinct t3.cat, hot from t3 join t4 
on t3.cat=t4.cat
order by hot desc

```
5.  统计每个类别中的视频热度Top10(Music为例)
```sql
--创建中间表
create table video_cat 
stored as orc tblproperties("orc.compress"="lzo") 
as select 
 videoid,
 views, 
 cat,
 ratings
from gulivideo_orc 
lateral viev
explode(category) cate as cat;
--类别热度表
select videoid,views hot from video_cat where cat=music order by hot desc limit 10; 
```
6.  统计每个类别中视频流量Top10(music为例)
```sql
select ratings rat from video_cat where cat=music order by rat desc limit 10; 
```
7.  统计上传视频最多的用户Top10以及他们上传的观看次数在前20的视频
```sql
--统计上传视频最多的用户Top10
(select uploader,num from gulivideo_user_orc order by videos desc limit 10)t1
--最多的用户上传的观看次数在前20的视频
select videoid,views top 
Rank() over(Partition by t1.uploader order by views desc) top
from gulivideo_orc join t1 
on t1.uploader=gulivideo_orc.uploader;
```
8.  统计每个类别视频观看数Top10
    8.1 video_cat查出每个类别视频观看排名
    
    ```sql
    (select
    cat,
    videoid,
    views
    rank()over(partition by cat order by views desc)hot
    from 
    video_cat)t1
    ```
      8.2 取每个类别Top10
    ```sql
    select cat,videoid,views
    from t1 where hot<=10;
    ```
