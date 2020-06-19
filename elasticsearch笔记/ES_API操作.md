# 提供API操作ElasticSearch
>通过scala的API对es数据进行操作
+ 导入jest依赖
```xml
<dependencies>
<dependency>
    <groupId>io.searchbox</groupId>
    <artifactId>jest</artifactId>
    <version>5.3.3</version>

</dependency>

<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna</artifactId>
    <version>4.5.2</version>
</dependency>

<dependency>
    <groupId>org.codehaus.janino</groupId>
    <artifactId>commons-compiler</artifactId>
    <version>2.7.8</version>
</dependency>

<!-- https://mvnrepository.com/artifact/org.elasticsearch/elasticsearch -->
<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>2.4.6</version>
</dependency>
</dependencies>
``` 
+ 编写添加和查询功能
```scala
object MyEsUtil {
  var factory:JestClientFactory=null
  def getClient:JestClient ={
    if(factory==null)build();
    factory.getObject

  }

  def  build(): Unit ={
    factory=new JestClientFactory
    factory.setHttpClientConfig(new HttpClientConfig.Builder("http://hadoop100:9200" )
      //是否运行多线程
      .multiThreaded(true)
      //最大并发（取决于消费端cpu个数）
      .maxTotalConnection(20)
      //连接超时，读取超时
      .connTimeout(10000).readTimeout(10000).build())
  }

  //写入操作
  //插入单条数据
  def addDoc():Unit= {
    val jest = getClient
    //build设计模式
    //可转化为json对象    hashMap  或者样例类(此处通过样例类添加数据)
    val index = new Index.Builder(Movie("0104", "龙岭迷窟", "鬼吹灯")).index("movie0105").`type`("_doc").id("0104").build()
    val msg:String = jest.execute(index).getErrorMessage
    if(msg!=null)
      println(msg)
    jest.close()
  }
  //查询操作
  //查询片名中带red的演员是zhang han yu 的字段结果安douban排名降序排列
  def queryDoc(): Unit ={
    var query=""

    val jest:JestClient = getClient
    val ssb = new SearchSourceBuilder()
    val bqb = new BoolQueryBuilder
    bqb.must(new MatchQueryBuilder("name","red"))
    bqb.filter(new TermQueryBuilder("actorList.name.keyword","zhang han yu"))
    ssb.query(bqb)
    ssb.from(0).size(20)
    ssb.sort("doubanScore",SortOrder.DESC)
    ssb.highlight(new HighlightBuilder().field("name"))
    query=ssb.toString
    println(query)


    val search = new Search.Builder(query).addIndex("movie_index").addType("movie")build()
    val res = jest.execute(search)
    val rsList: util.List[SearchResult#Hit[util.Map[String,Any],Void]] = res.getHits(classOf[util.Map[String, Any]])


    import scala.collection.JavaConversions._
    for (rl <- rsList ) {
      println(rl.source.mkString(","))
    }
    jest.close()
  }

  def main(args: Array[String]): Unit = {
//    addDoc()
    queryDoc()
  }

case class Movie(id:String,movie_name:String,name:String)
}
```