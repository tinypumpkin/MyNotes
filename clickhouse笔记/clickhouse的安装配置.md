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
