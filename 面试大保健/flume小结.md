# flume的三件事
## 一，组成（source，channel，sink，2个事务）
### source
1. Taildir Source支持断点续传多目录（阿帕奇1.7后）
2. 自定义source断点续传（API）
3. taildir source挂掉不会丢失可能出现数据重复
4. 处理重复数据
+ 不处理
+ 处理
>在下一段处理

hive数仓里（dwd层数据清洗ETL），stream里处理
>去重手段：groupby（id）开窗id取第一条
5. 是否支持递归读取文件
不支持，需自定义tail source(递归+读写)消费kafka数据上传至hdfs
### channel
1. File channel

基于磁盘效率低可靠性高
2. memory channel

基于内存效率高可靠性低
3. kafka channel

数据存储在kafka基于磁盘效率高于File channel+kafka sink(省去sink)
注意：flume1.6版本topic+内存无效1.7以后版本解决
4. 生产环境
+ 如果下一级kafka果断kafka channel
+ 不是kafka关心性能选memory,关系可靠性选File
### sink
1. HDFS sink
> 控制文件块大小
+ 时间(1~2h),大小(128MB),event个数(0，禁用)
>压缩
+ 开启压缩流，指定压缩编码（LZOP/snappy）
## 二，3个器
### 拦截器
1. ETL(判断json完整性)
2. 分类器（启动日志,事件日志）
事件日志包含很多不同业务，kafka要满足下一级所有消费者，最好1张表1个topic
3. 自定义拦截器
+ 定义1个类实现Interceptor接口
+ 重写4个方法（初始化，关闭，单event，多event）
4. 拦截器的选择视不同场景选定(对速度要求高可以不用，到下一级处理)
### 选择器
+ rep(默认)，mul(选择性发送到下一通道)
### 监控器
1. ganglia
1. 发现尝试提交的次数 >> 实际提交的次数则flume性能有问题
2. 解决
+ 提高内存4~6G（flume_env.sh）
+ 增加flume台数
## 三，优化
1. File Channel   能多目录就多目录（要求在不同的磁盘），提高吞吐量
2. 控制小文件
+ 时间（1-2小时）、大小（128m）、event个数(0禁止)
3. 监控器



