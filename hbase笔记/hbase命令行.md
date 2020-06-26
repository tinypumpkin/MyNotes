# HBase命令行
## 常用操作
>### 1. 进入
```bash
hbase shell
```
>### 2.查看表
```sql
list
```
>### 3.建表
```sql
--create 表名 ,  列族
create 'student','info'
```
>### 4.插入数据
```sql
--put '表名','行键','列族:列限定名','value'

put 'student','1001','info:sex','male'
put 'student','1001','info:age','18'
put 'student','1002','info:name','Janna'
put 'student','1002','info:sex','female'
put 'student','1002','info:age','20'
```
>### 5.扫描查看表数据
```sql
scan 'student'
--startrow 从 1001行到 stoprow 1001行
scan 'student',{STARTROW => '1001', STOPROW  => '1001'}
scan 'student',{STARTROW => '1001'}
```
>### 6.查看表结构
```sql
describe 'student'
```
>### 7.更新指定字段
```sql
put 'student','1001','info:name','Nick'
put 'student','1001','info:age','100'
```
>### 8.查看“指定行”或“指定`列族`:列”的数据
```sql
get 'student','1001'
get 'student','1001','info:name'
```
>### 9.统计表数据行数
```sql
count 'student'
```
>### 10.删除数据
+ 删除某rowkey的全部数据：
```sql
deleteall 'student','1001'
```
+ 删除某rowkey的某一列数据：
```sql
delete 'student','1002','info:sex'
```
>### 11.清空表数据
+ 清空表的操作顺序为先disable，然后再truncate(自动化执行)。
```sql
truncate 'student'
```
>### 12.删除表
+ 先disable表
```sql
disable 'student'
```
+ 然后才能drop这个表(直接drop表，会报错)
```sql
drop 'student'
```
>### 13.变更表信息
+ 将info列族中的数据存放3个版本：
```sql
alter 'student',{NAME=>'info',VERSIONS=>3}

get 'student','1001',{COLUMN=>'info:name',VERSIONS=>3}
```

