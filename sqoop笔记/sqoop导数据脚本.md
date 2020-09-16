# Sqoop导数据
## MySQL->Sqoop->HDFS
业务数据通过sqoop导入HDFS(ODS层)
```bash
#! /bin/bash

sqoop=/opt/module/sqoop/bin/sqoop

#do_date=`date +%F`
do_date=`date -d '-1 day' +%F`

if [[ -n "$2" ]]; then
    do_date=$2
fi

import_data(){
$sqoop import \
--connect jdbc:mysql://hadoop100:3306/gmall \
--username root \
--password myself \
--target-dir /origin_data/gmall/db/$1/$do_date \
--delete-target-dir \
--query "$2 and  \$CONDITIONS" \
--num-mappers 1 \
--fields-terminated-by '\t' \
--compress \
--compression-codec lzop \
--null-string '\\N' \
--null-non-string '\\N'

hadoop jar /opt/module/hadoop-3.1.3/share/hadoop/common/hadoop-lzo-0.4.20.jar com.hadoop.compression.lzo.DistributedLzoIndexer /origin_data/gmall/db/$1/$do_date
}



import_user_info(){
  import_data "user_info" "select 
                            id,
                            name,
                            gender,
                            create_time,
                            operate_time
                          from user_info 
                          where (DATE_FORMAT(create_time,'%Y-%m-%d')='$do_date' 
                          or DATE_FORMAT(operate_time,'%Y-%m-%d')='$do_date')"
}

	
case $1 in

  "user_info")
     import_user_info
;;
  "sku_info")
     import_sku_info
;;
#第一次导入
"first")
   import_sku_info
   import_user_info
;;
#导入所有
"all")
   import_sku_info
   import_user_info
;;
esac
```
## HDFS->sqoop->MySQL
Hive表(HDFS数据)通过sqoop导出MySQL(ADS层)
```bash
#!/bin/bash
# hdfs数据
hive_db_name=gmall
#Mysql数据
mysql_db_name=gmall_report

export_data() {
/opt/module/sqoop/bin/sqoop export \
--connect "jdbc:mysql://hadoop100:3306/${mysql_db_name}?useUnicode=true&characterEncoding=utf-8"  \
--username root \
--password myself \
--table $1 \
--num-mappers 1 \
#导出数据地址
--export-dir /warehouse/$hive_db_name/ads/$1 \
--input-fields-terminated-by "\t" \
#更新模式允许插入
--update-mode allowinsert \
#更新主键/唯一字段
--update-key $2 \
#null值处理
--input-null-string '\\N'    \
--input-null-non-string '\\N'
}

case $1 in
  "ads_uv_count")
     export_data "ads_uv_count" "dt"
;;
  "ads_user_action_convert_day") 
     export_data "ads_user_action_convert_day" "dt"
;;
  "ads_user_topic")
     export_data "ads_user_topic" "dt"
     #其余表省略未写
;;
esac
```