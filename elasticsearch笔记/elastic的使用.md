# elasticsearch的使用

elasticsearch的基本概念
|es组件|es组件所表示的含义|
|---|---|
|cluster|整个elasticsearch 默认就是集群状态，整个集群是一份完整、互备的数据|
|node|集群中的一个节点，一般只一个进程就是一个node|
|shard|分片，即使是一个节点中的数据也会通过hash算法，分成多个片存放，默认是5片。（7.0默认改为1片）|
|index|相当于rdbms的database(5.x), 对于用户来说是一个逻辑数据库，虽然物理上会被分多个shard存放，也可能存放在多个node中。   6.x 7.x index相当于table|
|type|类似于rdbms的table，但是与其说像table，其实更像面向对象中的class , 同一Json的格式的数据集合。（6.x只允许建一个，7.0被废弃，造成index实际相当于table级）|
|document|类似于rdbms的 row、面向对象里的object|
|field|相当于字段、属性|

>新建数据
```sql
PUT customer0105/_doc/1
{
  "id":"0101",
  "name":"zhaoyu"
}
```
>查询各个节点状态
```sql
GET _cat/nodes?v  
```
>查询各个索引(健康)状态 
1. green只是一个分布式副本
2. yello没有副本,主数据完整
3. red主数据不完整
```sql
GET _cat/indices?v
```
>查询索引customer0105的分片情况 
```sql
GET _cat/shards/customer0105         
```
## elasticsearch restful api (DSL)
DSL全称 Domain Specific language，即特定领域专用语言。
>es中保存的数据结构
+ 创建两个对象若在关系型数据库保存，会被拆成2张表，但是es用一个json来表示一个document。
```java
public class  Movie {
	 String id;
     String name;
     Double doubanScore;
     List<Actor> actorList;
}

public class Actor{
String id;
String name;
}
```
+ 保存到es中应该是：
```json
{  "id":"1",
  "name":"operation red sea",
  "doubanScore":"8.5",
  "actorList":[  
{"id":"1","name":"zhangyi"},
{"id":"2","name":"haiqing"},
{"id":"3","name":"zhanghanyu"}] }
```
>对数据的操作
+ 查看es中有哪些索引(es 中会默认存在一个名为.kibana的索引)
```sql
GET /_cat/indices?v
```
表头的含义
|表头|表头代表内容|
|---|---|
|health|green(集群完整) yellow(单点正常、集群不完整) red(单点不正常)|
|status|是否能使用|
|index|索引名|
|uuid|索引统一编号|
|pri|主节点几个|
|rep|从节点几个|
|docs.count|文档数|
|docs.deleted|文档被删了多少|
|store.size|整体占空间大小|
|pri.store.size|主节点占|

+ 增加一个索引
```sql
PUT /movie_index
```
+ 删除一个索引（ ES不删除也不修改任何数据，只是增加版本号）
```sql
DELETE /movie_index
```
+  新增文档 
1. 格式 PUT /index/type/id（之前没建过index或者type，es 会自动创建。）
```sql
PUT /movie_index/movie/1
{ "id":1,
  "name":"operation red sea",
  "doubanScore":8.5,
  "actorList":[  
{"id":1,"name":"zhang yi"},
{"id":2,"name":"hai qing"},
{"id":3,"name":"zhang han yu"}
]
}
PUT /movie_index/movie/2
{
  "id":2,
  "name":"operation meigong river",
  "doubanScore":8.0,
  "actorList":[  
{"id":3,"name":"zhang han yu"}
]
}

PUT /movie_index/movie/3
{
  "id":3,
  "name":"incident red sea",
  "doubanScore":5.0,
  "actorList":[  
{"id":4,"name":"zhang chen"}
]}
```
+ 直接用id查找
```sql
GET movie_index/movie/1
```
+ 修改—整体替换  格式:PUT /index/type/id{内容} (和新增没有区别  要求：必须包括全部字段)
```sql
PUT /movie_index/movie/3 --幂等性
{
  "id":"3",
  "name":"incident red sea",
  "doubanScore":"5.0",
  "actorList":[  
{"id":"1","name":"zhang chen"}
]}
```
+ 修改—某个字段 格式：POST index/type/id/_update {修改内容}
```sql
POST movie_index/movie/3/_update --非幂等性
{ 
  "doc": {
    "doubanScore":"7.0"
  } }
```
+ 删除一个document 格式: DELETE/index/type/id
```sql
DELETE movie_index/movie/3
```
+ 搜索type全部数据
```sql
GET movie_index/movie/_search
```
>查询操作

|关键字|匹配模式|说明|
|---|---|---|
|match|分词匹配|使用分词，text类型是专用分词字段|
|term|值等匹配|使用keyword，不分词字段|
|watch_phrase|短语匹配|将查询字段周围短语匹配(不进行分词)|
|fuzzy|容错匹配|查询输出相似度最高的结果|
|match...post_filter|混合查询-1|先匹配后过滤|
|bool:{must:[match...]，filter}|混合查询-2|同时匹配+过滤|
|from,size|分页匹配|每页显示多少行(from:从第几行开始,size:每页显示多少个)|
|range|按范围匹配|gte：大于，lte：小于按区间匹配|
|sort|排序|desc/asc（指定排序后默认相关度排序失效）|
|aggs:{name:{agg_type}}|聚合|<font color=blue>聚合-->aggs</font>，<font color=blue>分组-->terms</font>，   常用方法(sum min max avg)  ，stats--聚合全家桶|

+ 按条件查询(全部)
```sql
GET movie_index/movie/_search
{
  "query":{
    "match_all": {}
  }
}
```
+ 按分词查询 
```sql
GET movie_index/movie/_search
{
  "query":{
    "match": {"name":"red"}
  }
}
```
+ 按分词子属性查询 
```sql
GET movie_index/movie/_search
{
  "query":{
    "match": {"actorList.name":"zhang"}
  }
}
```
+  match phrase(按短语查询，不再利用分词技术，直接用短语在原始数据中匹配)
```sql
GET movie_index/movie/_search
{
    "query":{
      "match_phrase": {"name":"operation red"}
}}
```
+ fuzzy查询
1. 校正匹配分词--当一个单词都无法准确匹配，es通过一种算法对非常接近的单词也给与一定的评分，能够查询出来，但是消耗更多的性能。
```sql
GET movie_index/movie/_search
{
    "query":{
      "fuzzy": {"name":"rad"}
    }
}
```
+ 过滤--查询后过滤
```sql
GET movie_index/movie/_search
{
    "query":{
      "match": {"name":"red"}
    },
    "post_filter":{
      "term": {
        "actorList.id": 3
      }
    }
}
```
+ 过滤--查询前过滤（推荐使用）
```sql
GET movie_index/movie/_search
{ 
    "query":{
        "bool":{
          "filter":[ {"term": {  "actorList.id": "1"  }},
                     {"term": {  "actorList.id": "3"  }}
           ], 
           "must":{"match":{"name":"red"}}
         }
    }
}
```
+ 过滤--按范围过滤
```sql
GET movie_index/movie/_search
{
   "query": {
     "bool": {
       "filter": {
         "range": {
            "doubanScore": {"gte": 8}
         }
       }
     }
   }
}
```
关于范围操作符：
|操作符|含义|
|--|--|
|gt|大于|
|lt|小于|
|gte|大于等于 great than or equals|
|lte|小于等于 less than or equals|
+ 排序
```sql
GET movie_index/movie/_search
{
  "query":{
    "match": {"name":"red sea"}
  }
  , "sort": [
    {
      "doubanScore": {
        "order": "desc"
      }
    }
  ]
}
```
+ 分页查询
```sql
GET movie_index/movie/_search
{
  "query": { "match_all": {} },
  "from": 1,
  "size": 1
}
```
+ 指定查询的字段
```sql
GET movie_index/movie/_search
{
  "query": { "match_all": {} },
  "_source": ["name", "doubanScore"]
}
```
+ 高亮
```sql
GET movie_index/movie/_search
{
    "query":{
      "match": {"name":"red sea"}
    },
    "highlight": {
      "fields": {"name":{} }
    }
    
}
```
+ 聚合(取出每个演员共参演了多少部电影)
```sql
GET movie_index/movie/_search
{ 
  "aggs": {
    "groupby_actor": {
      "terms": {
        "field": "actorList.name.keyword"  
      }
    }
  }
}
```
+ 每个演员参演电影的平均分是多少，并按评分排序
```sql
GET movie_index/movie/_search
{ 
  "aggs": {
    "groupby_actor_id": {
      "terms": {
        "field": "actorList.name.keyword" ,
        "order": {
          "avg_score": "desc"
          }
      },
      "aggs": {
        "avg_score":{
          "avg": {
            "field": "doubanScore" 
          }
        }
       }
    } 
  }
}
```
+ 聚合时为何要加 .keyword后缀？

.keyword 是某个字符串字段，专门储存不分词格式的副本 ，在某些场景中只允许只用不分词的格式，比如过滤filter 比如 聚合aggs, 所以字段要加上.keyword的后缀。
### 中文分词
elasticsearch本身自带的中文分词，就是单纯把中文一个字一个字的分开，根本没有词汇的概念。
+ 安装
1. 下载elasticsearch-analysis-ik的zip包
2. 解压到./elasticsearch/plugins/ik
3. 安装后重启es
```bash
cd /opt/module/elasticsearch/plugins

mkdir ik

mv elasticsearch-analysis-ik-xxx.zip /opt/module/elasticsearch/plugins/ik

cd /opt/module/elasticsearch/plugins/ik

unzip elasticsearch-analysis-ik-xxx.zip  

rm elasticsearch-analysis-ik-xxx.zip  

sh 启动/es.sh stop

sh 启动/es.sh start

cd /opt/module/kibana/bin
./kibana
```
+ 测试使用
1. 默认
```sql
GET movie_index/_analyze
{  
  "text": "我是地球人"
}
```
2. 分词器1
```sql
GET movie_index/_analyze
{  "analyzer": "ik_smart", 
  "text": "我是火星人"
}
```
3. 分词器2
```sql
GET movie_index/_analyze
{  "analyzer": "ik_max_word", 
  "text": "我是三体人"
}
```
能够看出不同的分词器，分词有明显的区别，所以以后定义一个type不能再使用默认的mapping了，要手工建立mapping, 因为要选择分词器。
+ 自定义词库
```bash
vim /opt/module/elasticsearch/plugins/ik/config/IKAnalyzer.cfg.xml
```
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
        <comment>IK Analyzer 扩展配置</comment>
        <!--用户可以在这里配置自己的扩展字典 -->
        <entry key="ext_dict"></entry>
         <!--用户可以在这里配置自己的扩展停止词字典-->
        <entry key="ext_stopwords"></entry>
        <!--用户可以在这里配置远程扩展字典 -->
         <entry key="remote_ext_dict">hadoop100/fenci/myword.txt</entry>
        <!--用户可以在这里配置远程扩展停止词字典-->
        <!-- <entry key="remote_ext_stopwords">words_location</entry> -->
</properties>
```
+ 远程拓展字典路径利用nginx发布静态资源
```bash
vim /opt/module/nginx/conf/nginx.conf
```
```conf
  server {
        listen  80;
        server_name  hadoop100;
        location /fenci/ {
           root es;
    }
   }
```
在/nginx/下建/es/fenci/目录，目录下加myword.txt
myword.txt中编写关键词，每一行代表一个词。

### 关于mapping
+ 查看mapping
```sql
GET movie_index/_mapping/movie
```
每个type中的字段的数据类型，由mapping定义。若没有设定mapping会自动，根据一条数据的格式来推断出应该的数据格式。
+ true/false → boolean
+ 1020  →  long
+ 20.1 → double
+ “2018-02-01” → date
+ “hello world” → text +keyword

#### 注意：
1. 默认只有text会进行分词，keyword是不会分词的字符串。
2. mapping除了自动定义，还可以手动定义，但是只能对新加的、没有数据的字段进行定义。一旦有了数据就无法再做修改了。
3. 虽然每个Field的数据放在不同的type下,但同一个名的Field在一个index下只能有一种mapping定义。

#### 基于中文分词搭建索引 
+ 建立mapping
```sql
PUT movie_chn
{
  "mappings": {
    "movie":{
      "properties": {
        "id":{
          "type": "long"
        },
        "name":{
          "type": "text"
          , "analyzer": "ik_smart"
        },
        "doubanScore":{
          "type": "double"
        },
        "actorList":{
          "properties": {
            "id":{
              "type":"long"
            },
            "name":{
              "type":"keyword"
}}}}}}}
```
+ 插入数据
```sql
PUT /movie_chn/movie/1
{ "id":1,
  "name":"红海行动",
  "doubanScore":8.5,
  "actorList":[  
  {"id":1,"name":"张译"},
  {"id":2,"name":"海清"},
  {"id":3,"name":"张涵予"}
 ]
}
PUT /movie_chn/movie/2
{
  "id":2,
  "name":"湄公河行动",
  "doubanScore":8.0,
  "actorList":[  
{"id":3,"name":"张涵予"}
]
}

PUT /movie_chn/movie/3
{
  "id":3,
  "name":"红海事件",
  "doubanScore":5.0,
  "actorList":[  
{"id":4,"name":"张晨"}
]}
```
+ 查询测试
```sql
GET /movie_chn/movie/_search
{
  "query": {
    "match": {
      "name": "红海战役"
    }
  }
}

GET /movie_chn/movie/_search
{
  "query": {
    "term": {
      "actorList.name": "张译"
    }}}
```
### 索引别名 _aliases
索引别名就像一个快捷方式或软连接，可以指向一个或多个索引，也可以给任何一个需要索引名的API来使用。别名 带给我们极大的灵活性，允许我们做下面这些：
1.	给多个索引分组 (例如， last_three_months)
2.	给索引的一个子集创建视图
3.	在运行的集群中可以无缝的从一个索引切换到另一个索引
+ 创建索引别名（建表时直接声明）
```sql
PUT movie_chn_2020
{  "aliases": {
      "movie_chn_2020-query": {}
  }, 
  "mappings": {
    "movie":{
      "properties": {
        "id":{
          "type": "long"
        },
        "name":{
          "type": "text"
          , "analyzer": "ik_smart"
        },
        "doubanScore":{
          "type": "double"
        },
        "actorList":{
          "properties": {
            "id":{
              "type":"long"
            },
            "name":{
              "type":"keyword"
    }}}}}}}
```
+ 为已存在的索引增加别名
```sql
POST  _aliases
{
    "actions": [
        { "add":    { "index": "movie_chn_xxxx", "alias": "movie_chn_2020-query" }}
    ]
}
```
+ 通过加过滤条件缩小查询范围，建立一个子集视图
```sql
POST  _aliases
{
    "actions": [
        { "add":    
{ "index": "movie_chn_xxxx", 
"alias": "movie_chn0919-query-zhhy",
            "filter": {
                "term": {  "actorList.id": "3"
}}}}]}
```
+ 查询别名（使用与使用普通索引没有区别）
```sql
GET movie_chn_2020-query/_search
```
+ 删除某个索引的别名
```sql
POST  _aliases
{
    "actions": [
        { "remove":    { "index": "movie_chn_xxxx", "alias": "movie_chn_2020-query" }}
    ]
}
```
+ 为某个别名进行无缝切换
```sql
POST /_aliases
{
    "actions": [
        { "remove": { "index": "movie_chn_xxxx", "alias": "movie_chn_2020-query" }},
        { "add":    { "index": "movie_chn_yyyy", "alias": "movie_chn_2020-query" }}
    ]
}
```
+ 查询别名列表
```sql
GET  _cat/aliases?v
```
### 索引模板 
创建索引的模具,可定义一系列规则构建符合特定业务需求的索引的 mappings 和 settings，通过使用 Index Template(索引模板)可以让我们的索引具备可预知的一致性。
> 分割索引（常见场景）
+ 分割索引就是根据时间间隔把一个业务索引切分成多个索引。
+ 例如 把order_info  变成 order_info_20200101,order_info_20200102 …..

+ 优点:
1. 结构变化的灵活性：

es不允许对数据结构进行修改。但实际使用中索引的结构和配置难免变化，只要对下一个间隔的索引进行修改，原来的索引维持原状。这样就有了一定的灵活性。

2. 查询范围优化：

一般情况并不会查询全部时间周期的数据，那么通过切分索引，物理上减少了扫描数据的范围，也是对性能的优化
+ 创建模板
```SQL
PUT _template/template_movie2020
{
  "index_patterns": ["movie_test*"],                  
  "settings": {                                               
    "number_of_shards": 1
  },
  "aliases" : { 
    "{index}-query": {},
    "movie_test-query":{}
  },
  "mappings": {                                          
"_doc": {
      "properties": {
        "id": {
          "type": "keyword"
        },
        "movie_name": {
          "type": "text",
          "analyzer": "ik_smart"
}}}}}
```
其中 "index_patterns": ["movie_test*"],  的含义就是凡是往movie_test开头的索引写入数据时，如果索引不存在，那么es会根据此模板自动建立索引。
在 "aliases" 中用{index}表示，获得真正的创建的索引名。
+ 测试
```SQL
POST movie_test_2020xxxx/_doc
{
  "id":"333",
  "name":"zhang3"
}
```
+ 查看系统中已有的模板清单
```SQL
GET  _cat/templates
```
+ 查看某个模板详情
```SQL
GET  _template/template_movie2020
或者
GET  _template/template_movie*
```
