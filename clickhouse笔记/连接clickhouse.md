# 通过API访问ClickHouse
>JDBC
+ 依赖
```xml
<dependency>
    <groupId>ru.yandex.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.1.55</version>
</dependency>

<dependency>
<groupId>org.apache.logging.log4j</groupId>
<artifactId>log4j-slf4j-impl</artifactId>
<version>2.11.0</version>
</dependency>
```
```java
public class ClickHouseJDBC {

public static void main(String[] args) {
    String sqlDB = "show databases";//查询数据库
    String sqlTab = "show tables";//查看表
    String sqlCount = "select count(*) count from ontime";//查询ontime数据量
    String crsql="create table "
    exeSql(sqlDB);
    exeSql(sqlTab);
    exeSql(sqlCount);
}
public static void exeSql(String sql){
String address = "jdbc:clickhouse://hadoop100:8123/default";
Connection connection = null;
Statement statement = null;
ResultSet results = null;
try {
    Class.forName("ru.yandex.clickhouse.ClickHouseDriver");
    connection = DriverManager.getConnection(address);
    statement = connection.createStatement();
    long begin = System.currentTimeMillis();
    results = statement.executeQuery(sql);
    long end = System.currentTimeMillis();
    System.out.println("执行（"+sql+"）耗时："+(end-begin)+"ms");
    ResultSetMetaData rsmd = results.getMetaData();
    List<Map> list = new ArrayList();
    while(results.next()){
    Map map = new HashMap();
    for(int i = 1;i<=rsmd.getColumnCount();i++){
    map.put(rsmd.getColumnName(i),results.getString(rsmd.getColumnName(i)));
    }
    list.add(map);
    }
    for(Map map : list){
    System.err.println(map);}
 } 
catch (Exception e) {
    e.printStackTrace();
}
finally {//关闭连接
    try {
    if(results!=null){
    results.close();
    }
    if(statement!=null){
    statement.close();
    }
    if(connection!=null){
    connection.close();}
    } 
    catch (SQLException e) {
    e.printStackTrace();
 }}
}}
```
>Python_Api
```bash
pip install clickhouse-driver
```
```python
from clickhouse_driver import Client

client = Client(host="hadoop100", port="9000", databses="db",user='ch' ,password='myself')
sql = "show databases"

print(sql)
try:
    client.execute(sql, types_check=True)
except Exception as e:
    print(e)
```