# Hive复习
## HSQL 语句执行顺序（优先级）
+  from
+  where
+  group by
+  select
+  having
+  over()
+  order by
+  limit
## 表
Hive中的表是存在MySql的原数据和存在HDFS上的数据组成的。

### 内部表与外部表

删除外部表仅仅删除元数据，无关HDFS上的数据`相当于视图`

删除内部表删除元数据的同时会同时删除HDFS上的数据（标记为不可用）
#### 外部表
当建表时加入external关键字就会建立一张外部表
>1. 新建外部表
```sql
create external table test
(id int,name string)
row format delimited fields terminated by '\t'
location '/inner.in';
```
>2. 插入数据
```sql
load data local inpath "/opt/module/datas/student.txt" into table test;
```

#### 外部表与内部表的转化
>内部表转化为外部表
```sql
alter table test set ablproperties("EXTERNAL"="TRUE");
```
>转回内部表
```sql
alter table test set ablproperties("EXTERNAL"="FALSE");
```
###  分区表

1. 建立分区表
```sql
-- 分区依据
partitioned by (class string)
```
2. 向表中插入数据
```sql
load data local inpath '/opt/module/datas/student.txt' into table stu_par partition(class='01');

load data local inpath '/opt/module/datas/student.txt' into table stu_par partition(class='02');
```
3. 分区表查询 
>查表时选择分区可减少扫描量
```sql
select * from stu_par where class="01";
select * from stu_par where id=1001;
```
>查询分区表的分区情况
```sql
show partitions stu_par;
```
>hdfs上仅有数据没提供元数据，修复方式
>
+ 1 添加分区
```sql
alter table stu_par add partition(class="03");
```
+ 2 直接修复(修复所有分区)
```sql
msck repair table stu_par;
```
4. 二级分区 (套娃)
>建立二级分区表 `按class，gread两类进行分区，gread在class文件夹内`
```sql
create table stu_par2(id int, name string)
-- 以class，gread进行分区
partitioned by (gread string,class string)
row format delimited fields terminated by '\t'
```
>插入数据到指定二级分区表
```sql
load data local inpath '/opt/module/datas/student.txt' into table stu_par2 partition(gread="01",class="03");
```
### 分区的增删改查
1. 增加父亲
>增加一个
```sql
alter table stu_par add partition(class="05");
```
>一次增加多个
```sql
alter table stu_par add partition(class="06") partition(class="07");
```

2. 删除分区
>删除单个分区
```sql
alter table stu_par2 drop partition(class="05");
```
>删除多个分区
```sql
alter table stu_par2 drop partition(class="06"),partition(class="07");
```
## 排序（4个by）
>全局排序`order by`(将数据汇总到1个reduce里)  
```sql
select * from emp order by desc;
--求前10%，Map取前10%
select * from emp order by sal desc limit 10;
```
>多条件排序(先按部门在按工资)
```sql
--部门按升序，薪水按降序
select * from emp order by deptno asc sal desc;
```
>局部排序`sort by`
```sql
select * from emp sort by deptno desc;
--设置mapreduce个数 
set mapreduce.job.reduces=3
select * from emp sort by deptno desc;
```
>分区排序`distribute by`(指定局部排序的分区字段)
```sql
--先按部门分成若干份，每份再按薪水降序分
select * from emp distribute by deptno sort by sal desc;
```
+ `cluster by` 当distribute by 和 sort by 字段相同时使用
```sql
select * from emp cluster by empno;
```
## 常用函数
>空字段赋值
```sql
--把员工查询到为null的用-1代替（null转换为-1）
select comm,nvl(comm,-1)from emp;
```
>case...when（类似 java 中的switch...case）
```sql
--显示部门的男生总量和女生总量（按部门分组后按男女分类累计）
select 
dept_id,
sum(case sex when '男' then 1 else 0 end) meal,
sum(case sex when '女' then 1 else 0 end) femeal
from emp_sex
group by
dept_id;
```
## 分区与分桶
>hive无索引可利用分区减少数据扫描量（起索引作用）
分桶针对某一区的数据将其整合成多个文件(取余按哈希)
```sql
--建立分桶表
create table  stu_buck(id int,name string)
clustered by(id)
into 4 buckets
row format delimited fields terminated by '\t';
location '/xxx';
```

+ 分桶通过哈希取余把`指定属性`数据变成多个，分区将`相同属性`数据分成多个文件夹管理

> 抽样

查询结果按id分成4分从中取第一份

```
select * from student tablesample (bucket 1 out of 4 on id); 
```
## 行列互转
>聚合函数 ->将一列数据捏在一起
+ collect_list -> 返回不去重数组
+ collect_set  -> 返回去重数组
>拼接 
+ concat(a,",",b)
+ concat_ws("分隔符",数组(需拼接内容))
### 行转列(将`多行一列`变为`一行一列`)
+ 例：把相同星座血型的人分一组
```sql
select 
concat(constellation,",",blood_type) group ,
--用到了聚合函数collect_list
concate_ws("|",collect_list(name)) name 
from person_info 
group by constellation,blood_type;
```
### 列转行（将`多列`转成`一行`）
>UDTF函数（一行输入，多行输出）
>
+ explode(炸开) ->将<font color=red>数组</font>里的元素变成多行单列，将map里的元素（k-v）变成多行2列
+ split ->分割列（字符串）变成数组，用法：split(字符串，分隔符)

>lateral viev（配合UDTF函数使用，将一列数据拆分成多行数据）
+ lateral viev用于粘合原始表和UDTF表里的列元素
##### 例:将下列表格转化
|name|frideds|children|address|
|----|----|----|----|
|songsong|[bingbing，lili]|{xiaosong：18，xiaoxiao：19}|{street：hui long guan，city：Beijing}|
|yangyang|[chenchen，susu]|{shuishui：18，mutian：19}|{street：nanjing road，city：Tianjin}|

---
|name| cname    |age|
|----|----|----|
|songsong|xiaoxiao|19|
|yangyang|shuishui|18|
|yangyang|mutian|19|
```sql
select 
t.name,
cn.cname,
cn.age
from table t
later view 
explode(children) cn as cname,age --将map炸成2列
```
## 窗口函数（开窗函数）

>over-->为聚合函数<font color="red">指定操作范围</font>，仅仅是指定操作范围，具体操作由前面的聚合函数实现。
+ unbounded preceding--->第一行
+ unbounded following--->最后一行
+ current row ---->当前行
+ preceding------->前一行
+ following-------->后一行
1. 窗口函数over()里面无参数时，范围是全表。
2. 按姓名分区，data排序
```sql
over(partition by name order by date ) as smp3
--此句与smp3相同，smp3自动会转化成此段执行
over(partition by name order by date rows between  preceding andcurrent row) 
```
3. 按姓名分区，data排序，范围从data<font color=red>当前</font>自身的<font color="#00dddd">上 1 行</font>到<font color="#00dddd">下 1 行</font>
```sql
over(partition by name order by date rows between 1 preceding and 1 following)  
```
4. 范围从<font color="#00dddd">第一行</font>到<font color="#00dddd">最后一行</font>
```sql
over(partition by name order by date rows between unbounded preceding and unbounded following)
```
### 窗口函数配合其他函数使用
+ lag（col，n，default_val）：往前第n行数据-->(对有序窗口使用)
+ lead（col，n，default_val）:往后第n行数据-->(对有序窗口使用)
+  ntile（n）：把有序窗口的行分发到指定数据的组中，各个组员编号（从1开始）对每一行ntile返回此行所属组的编号（n必须为int型）
```sql
select 
name,orderdate,cost
--显示某一列数据的上一行
lag(orderdate,1,"1970-01-01")--->对有序窗口使用
over(partition by name order by orderdate) last_order
--显示某一列数据的下一行
lead(orderdate,1,"1970-01-01")--->对有序窗口使用
over(partition by name order by orderdate) next_order
--把数据按行数分成5组并返回序号
ntile(5)over(order by orderdate)
from 
business 
```
+ percent_rank() 百分比占位，处于数据位置的百分比-->对窗口函数使用
```sql
select
name,orderdate,cost,
percent_rank() over(order by orderdate) pr
from 
business
```
>排名
+ rank（）--->编号相同，下一个编号会跳过
+ dense_rank（）--->编号相同，下一个编号连续
+ row_number（）--->不管Colum想不想同，连续编号
|row_number|rank|dense_rank|
|------|------|------|
|1|1|1|
|2|2|2|
|3|3|3|
|4|3|3|
|5|5|3|
|6|6|4|
```sql
select *,
rank() over(partition by subject order by score desc) r,
dense_rank() over(partition by subject order by score desc) dr,
row_number() over(partition by subject order by score desc) rn
from
score; 
```
>日期函数
+ 当前日期
```sql
--current_date() 返回当前日期
select current_date();
```
+ 日期的加减
```sql
--从今天开始90天以后的日期
select date_add(current_date(),90);
--从今天开始90天之前的日期
select date_sub(current_date(),90);
```
+ 日期差
```sql
--日期差
select datediff(current_date(),"1990-06-04"); 
```