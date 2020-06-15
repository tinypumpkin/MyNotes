# elasticsearch安装
+ 下载tar包，拷贝elasticsearch-xxx.tar.gz重命名 拷贝到/opt/software 目录下
```bash
tar -zxvf elasticsearch-xxx.tar.gz
mv elasticsearch-xxx elasticsearch
mv elasticsearch /opt/module
```
+ 修改配置文件
```bash
vim /opt/module/elasticsearch/config/elasticsearch.yml
```
+ 修改yml配置的注意事项:
1. 每行必须顶格，不能有空格   
2. “：”后面必须有一个空格

+ 集群名称，同一集群名称必须相同
```yml
# Use a descriptive name for your cluster:
#
cluster.name: my-es
```
+ 每个节点名称（不同节点节点名不同 node-1，node-2，node-3）
```yml
# Use a descriptive name for the node:
#
node.name: node-1
```
+ 网络部分  改为当前的ip地址  ，端口号保持默认9200
```yml
# Set the bind address to a specific IP (IPv4 or IPv6):
#
network.host: hadoop100
#
# Set a custom port for HTTP:
#
#http.port: 9200
```
+ 把bootstrap自检程序关掉
```yml
# ----------------------------------- Memory --------------------------------
# Lock the memory on startup:
#
bootstrap.memory_lock: false
bootstrap.system_call_filter: false
```
+ 自发现配置：新节点向集群报到的主机名
```yml
# Pass an initial list of hosts to perform discovery when new node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
discovery.zen.ping.unicast.hosts: ["hadoop100", "hadoop101","hadoop102"]
```
#### 修改linux配置(默认es是单机模式,打开服务器限制，支持多并发)
>max file descriptors [4096] for elasticsearch process likely too low, increase to at least [65536] elasticsearch
+ 原因：系统允许 ES 打开的最大文件数需要修改成65536
+ 解决：
```bash
vim /etc/security/limits.conf
```
添加内容：
```conf
* soft nofile 65536
* hard nofile 131072
* soft nproc 2048
* hard nproc 65536
#注意：“*” 不要省略掉
```

>max number of threads [1024] for user [judy2] likely too low, increase to at least [4096] 
+ 原因：允许最大进程数修该成4096
+ 解决：
```bash
vim /etc/security/limits.d/90-nproc.conf   
```
修改如下内容：
```conf
* soft nproc 1024
#修改为
* soft nproc 4096
```
>max virtual memory areas vm.max_map_count [65530] likely too low, increase to at least [262144]
+ 原因：一个进程可以拥有的虚拟内存区域的数量。
+ 解决： 
```bash
vim /etc/sysctl.conf  
```
文件最后添加一行
```conf
vm.max_map_count=262144
#可永久修改虚拟内存
```
#### 环境启动优化
+ ES在Java虚拟机运行，虚拟机默认启动占2G内存。测试用可以改小一点内存
```bash
vim  /opt/module/elasticsearch/config/jvm.options
```
```options
# Xms represents the initial size of total heap space
# Xmx represents the maximum size of total heap space

-Xms256m
-Xmx256m
```
+ 重启linux
### 集群模式
>es天然就是集群状态。
1. 把ES的安装包分发给其他两台机器
2. 根据第一台机器的linux系统配置，修改其他两台机子
3. 在三台机器能够独立启动的情况下，修改/etc/elasticsearch/elasticsearch.yml
>设置新主机的“报道中心”  
1. node-xx
2. network-host hadoop1 
+ 还要记得把 三台主机的node.name改成各自的
> 测试
```bash
curl http://hadoop100:9200/_cat/nodes?v
```
>启动失败看日志
```bash
vim /opt/module/elasticsearch/logs/my-es.log
```
## 安装kibana
+ 下载，解压,进入kibana目录下
```bash
tar -zxvf kibana-xxx-linux-x86_64.tar.gz 
mv kibana-xxx kibana
mv kibana /opt/module
cd /opt/module/kibana
```
+ 配置
```bash
vim /opt/module/config/kibana.yml
```
```yml
# To allow connections from remote users, set this parameter to a non-loopback address.
server.host: "0.0.0.0"

# The URLs of the Elasticsearch instances to use for all your queries.
elasticsearch.hosts: ["http://hadoop100:9200"]
```
+ 启动,测试访问<http://hadoop100:5601/>
