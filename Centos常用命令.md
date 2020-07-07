# Centos配置
>更改本机ip
```bash
vim /etc/sysconfig/network-scripts/ifcfg-ens33
```
>更改主机名称
```bash
vim /etc/hostname
```
>更改host
```bash
vim /etc/hosts
```
>用户管理
+ 创建用户
```bash
useradd hadoop42
```
+ 添加密码
```bash
passwd hadoop42
```
+ 查看用户是否存在
```bash
id hadoop42
```
+ 查看创建了哪些用户
```bash
cat  /etc/passwd 
```
+ su 切换用户
```bash
su 用户名称 
```
+ userdel 删除用户
```bash
userdel-r  用户名	
```
+ who 查看登录用户信息
```bash
whoami
who am i
```
+ usermod 修改用户(-g 修改用户的初始登录组)
```bash
usermod -g 用户组 用户名
```
>关闭防火墙
```bash
systemctl disable firewalld 
#开启systemctl enable firewalld 
```
>chmod 改变权限 
+ chmod 权限 文件 
```bash
#文件读写执行权限
chmod 777 file.txt
#文件夹读写执行权限
chmod 777 file/
```
>chown 改变拥有者 
+ chown 用户 文件 
```bash
#文件所有者
chown atguigu file.txt 
#递归改变文件夹所有者
chown -R atguigu:atguigu file/
```
>chgrp 更改组
>
>+ 	chgrp [最终用户组] [文件或目录]
```bash
chgrp atguigu file.txt
```
### 过滤
> 查找
```bash
#查找atguigu下txt文件
find atguigu/ -name *.txt
#查找module下所有关于hadoop的jar文件
find /opt/module -name *.jar | grep hadoop
```
>多级查找
```bash
#查找当前有关python的进程过滤掉过滤进程（grep）
ps -ef | grep python | grep -v grep  
```
> 查看进程
```bash
#查看ssh进程
ps -aux | grep ssh	
#查看java进程
ps -ef | grep java	
```
### 压缩解压
>压缩文件
```bash
tar -zcvf houma.tar.gz 233.txt 123.txt 
```
>压缩文件夹
```bash
tar -zcvf 233.tar.gz /data/logs/
```
>解压
```bash
#不加-C /opt/module即解压到当前
tar -zxvf 233.tar.gz -C /opt/module
```
>查看端口占用应用信息
```bash
lsof -i:端口号
```
### 1 > /dev/null 2>&1 语句含义
+ 标准输出重定向到空设备文件(就是不显示任何信息)
```bash
1 > /dev/null
```
+ 标准错误输出重定向（把2追加1后面）
```bash
2 >&1 
```
### 停止任意任务
```bash
#通过grep过滤，awk空格分行，xargs拿到第一行参数 将参数传给 kill
ps -ef | grep 任务名称 | grep -v grep | grep -v stopapp.sh | awk '{print $2}' | xargs -n1 kill -9
```
### shell常用工具
awk 、sed 、 sort 、 cut
### 单引号与双引号的区别
+ 单引号:引号内部的变量不能取出内部变量对应的值
```bash
'$do_date'
```
+ 双引号:引号内部的变量能够取出内部变量对应的值
```bash
"$do_date"
```
+ 嵌套: 由最外层的决定 
```bash
"'$do_date'" 

'"$do_date"'
```