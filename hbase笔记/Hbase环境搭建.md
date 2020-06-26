# Hbase的安装配置
在保证zookeeper，hadoop正常安装配置好,启动好的情况下安装
> HBase的解压安装
```bash
tar -zxvf hbase-xxx.tar.gz 
mv hbase-xxx hbase 
mv hbase /opt/module
```
> 修改HBase的配置文件
1. hbase-env.sh修改内容：
```bash
vim /opt/module/hbase/conf/hbase-env.sh
```
```bash
export HBASE_MANAGES_ZK=false
```
2.	hbase-site.xml修改内容：
```bash
vim /opt/module/hbase/conf/hbase-site.xml
```
```xml
<configuration>
  <property>
    <name>hbase.rootdir</name>
    <value>hdfs://hadoop100:8020/hbase</value>
  </property>
  <property>
    <name>hbase.cluster.distributed</name>
    <value>true</value>
  </property>
  <property>
    <name>hbase.zookeeper.quorum</name>
    <value>hadoop100,hadoop101,hadoop102</value>
  </property>
  <property>
    <name>hbase.unsafe.stream.capability.enforce</name>
    <value>false</value>
  </property>
<property>
<name>hbase.wal.provider</name>
<value>filesystem</value>
</property>
</configuration>
```
3. regionservers：
```bash
hadoop100
hadoop101
hadoop102
```
> 集群同步
```bash
cd /opt/module
xsync hbase
```
> HBase服务的启动
```bash
#方式1
hbase-daemon.sh start master
hbase-daemon.sh start regionserver
#方式2
#启动
start-hbase.sh
#停止
stop-hbase.sh
```
+ 注意:集群之间的节点时间不同步，会导致regionserver无法启动，抛出ClockOutOfSyncException异常。
+ 修复时间同步问题
```bash
vim /opt/module/hbase/conf/hbase-site.xml
```
```xml
<property>
        <name>hbase.master.maxclockskew</name>
        <value>180000</value>
        <description>Time difference of regionserver from master</description>
</property>
```
## Phoneix的安装配置
+ 解压安装
```bash
tar -zxvf apache-phoenix-xxx.tar.gz
mv apache-phoenix-xxx phoenix
mv phoenix /opt/module
```
+ 复制server包并拷贝到各个节点的hbase/lib
```bash
cd /opt/module/phoenix
cp phoenix-5.0.0-HBase-2.0-server.jar /opt/module/hbase/lib/
scp phoenix-5.0.0-HBase-2.0-server.jar hadoop101:/opt/module/hbase/lib/
scp phoenix-5.0.0-HBase-2.0-server.jar hadoop102:/opt/module/hbase/lib/
```
+ 复制client包并拷贝到各个节点的hbase/lib
```bash
cd /opt/module/phoenix/
cp phoenix-5.0.0-HBase-2.0-client.jar /opt/module/hbase/lib/
scp phoenix-5.0.0-HBase-2.0-client.jar hadoop101:/opt/module/hbase/lib/
scp phoenix-5.0.0-HBase-2.0-client.jar hadoop102:/opt/module/hbase/lib/
```
+ 配置环境变量
```bash
sudo vim /etc/profile.d/my_env.sh
```
+ 添加内容
```bash
#phoenix
export PHOENIX_HOME=/opt/module/phoenix
export PHOENIX_CLASSPATH=$PHOENIX_HOME
export PATH=$PATH:$PHOENIX_HOME/bin
```
+ 重启使环境变量生效
```bash
sudo vim /etc/profile.d/my_env.sh
```
+ 启动phoenix
```bash
sqlline.py hadoop100,hadoop101,hadoop102:2181
```
>Phoenix JDBC操作
+ 创建工程导入依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.apache.phoenix</groupId>
        <artifactId>phoenix-core</artifactId>
        <version>5.0.0-HBase-2.0</version>
</dependency>
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <version>3.3.6</version>
</dependency>
</dependencies>
```
+ 测试jdbc
```java
public class PhoenixTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        //1.定义参数
        String driver = "org.apache.phoenix.jdbc.PhoenixDriver";
        String url = "jdbc:phoenix:hadoop100,hadoop101,hadoop102:2181";

        //2.加载驱动
        Class.forName(driver);

        //3.创建连接
        Connection connection = DriverManager.getConnection(url);

        //4.预编译SQL
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM test");

        //5.查询获取返回值
        ResultSet resultSet = preparedStatement.executeQuery();

        //6.打印结果
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1) + resultSet.getString(2));
        }

        //7.关闭资源
        resultSet.close();
        preparedStatement.close();
        connection.close();
    }
}
```