# scala集合事例集
## 集合三大类--seq（序列）set（集）map（映射）
+ 在scala中如果运算符中包含冒号，且冒号在运算符后，运算顺序为从右到左
### 数组
+ 操作集合时不可变用符号可变用方法
+ 不可变数组
```scala
val a:Array[String] = new Array[String](5)    //方式1 通过new创建
val a1 = Array(1, 2, 3, 4, 5)                 //方式2 通过apply方法创建
//访问数组元素
println(a1(0))
//遍历
//1) 普通for
for(a<-0 to a1.length)
    println(a1(0))
//2) 普通for
for (elem <- a1)
    println(elem)
//3) 迭代器
val it = a1.iterator
while (it.hasNext)
    println(it.next())
for (elem <- it)
    println(elem)
//4) 增强for 传入一个匿名函数
a1.foreach((a:Int)=>{println(a)})
//简化后
a1.foreach(println(_))
a1.foreach(println)
//5) 用指定字符串连接数组元素
println(a1.mkString("--"))
//不可变添加元素
val a2 = Array(1, 2, 3)
println(a2.mkString("-"))
//数组前添加元素
val _a2 = a2.+:(4)
println(a2.mkString("-"))
println(_a2.mkString("-"))
//数组后添加元素
val a2_ = a2.:+(4)
println(a2.mkString("-"))
println(a2_.mkString("-"))
//简化--在scala中如果运算符中包含冒号，且冒号在运算符后，运算顺序为从右到左
val s_a2 = 4 +:a2
println(s_a2.mkString("-"))
val sa2_ = a2:+ 4
println(sa2_.mkString("-"))
```
+ 可变数组
```scala
//可变数组,在执行添加或者删除操作时不会创建新的数组对象，直接对原数组进行操作
//操作集合时不可变用符号可变用方法
val a_ = new ArrayBuffer[Int]()
val a3:ArrayBuffer[Int] = ArrayBuffer(1, 2, 3)
//修改数组中元素
a3.update(1,5)
println(a3(1))
//向数组添加元素
val a3_ = a3.+=(4) //添加方法不同于不可变数组 -->+=方法源数组添加
println(a3)
println(a3_)
val _a3 = a3.+:(20)   //                 -->+:创建新数组
println(a3)
println(_a3)
a3.append(6)
println(a3)
//删除元素
a3.remove(1,2)
println(a3)
```
+ 可变不可变转化
```scala
//可变转为不可变
val arr = ArrayBuffer(1, 2, 3)
val narr = arr.toArray
//不可变转为可变
val ba:mutable.Buffer[Int] = narr.toBuffer
```
### 序列--Seq集合（List） 
+ 不可变seq集合
1. 向集合中添加元素 -> ::
2. Nil表示空集合
3. 扁平化 -> ::: 添加的同时整体拆分为个体
```scala
 //不可变集合
//创建--由于List是抽象的所以只能用apply方法创建
val li = List(1, 2, 3, 4, 5)
//向集合中添加元素
//（1）
val _li = li.+:(20)
val li_ = li.:+(20)
println(_li)
println(li_)
//(2)  向集合中添加元素 ::
// Nil表示空集合
val nli:List[Int] = Nil.::(30)
println(nli)
val li3 = 30 :: 40 :: 50 :: Nil
println(li3)
//(3)扁平化 ::: 添加的同时整体拆分为个体
val tem = List(20, 30)
val ll = tem :: li
println(ll)
val ll_ = tem ::: li
println(ll_)
```
+ 可变seq集合
```scala
//可变seq集合
//创建可变集合对象
val lib = ListBuffer(1, 2, 3, 4, 5)
//添加元素
lib.append(10)
println(lib)
//指定位置添加
lib.insert(2,15)
println(lib)
//修改
lib.update(1,2)
//删除
lib.remove(2,1)
println(lib)
```
### set集合（无序号不重复）
+ 不可变set
```scala
//创建不可变集合
val s1 = Set(1, 2, 3)
//添加元素
val ls = s1.+(20)
println(s1)
println(ls)
```
+ 可变set
```scala
//创建可变集合
val s2 = mutable.Set(1, 2, 3, 4, 5)
val ls2 = s2.+(10)
//添加元素
val s3 = s2.add(20)    //set的添加，删除返回布尔类型
println(ls2)
println(s2)
```
### Map映射
+ 不可变Map集合
+ 注意
1. 通过map直接get(key)会得到some--避免空指针异常,需要再get得到值
2. 利用getOrElse若有值返回得到的val若没有返回()内指定的值
3. option有2个子类分别为some和None
4. 不可变Map不能通过key直接修改value 
```scala
//创建不可变Map集合
val m1:Map[String,Int] = Map("a" -> 1, "b" -> 2, "c" -> 3)
//遍历map集合中元素
m1.foreach((kv:(String,Int))=>{println(kv)})
//简化==>
m1.foreach(println)
//获取key
for (key <- m1.keys) {print(s"${key} ")}
println()
//获取不存在的kye的value避免空指针异常返回some若没有返回None
println(m1.get("d"))
//利用getOrElse若有值返回得到的val若没有返回（）内指定的
println(m1.get("d").getOrElse(0))
for (k <- m1.keys) {println(s"${k}->${m1.get(k)}")}
```
+ 可变Map集合
```scala
//创建可变Map集合
val m2 = mutable.Map("a" -> 1, "b" -> 2, "c" -> 3)
//添加
m2.put("d",4)
println(m2)
//删除元素
m2.remove("d")
println(m2)
//修改集合中元素
m2.update("a",20) //不可变Map不能通过key直接修改value
println(m2)
m2("a")=1
m2.foreach(println)
```
### 元组（tuple）
+ 元组是一个容器存放各种相同或不同类型的数据,即将多个无关数据封装成的一个整体称为元组
+ Map集合中键值对就是一种特殊的元组，元组元素为2我们称之为对偶
```scala
//创建元组对象
//数据类型 :Int \String \(数据类型)=>返回值类型\ =>(返回值类型) \(类型...)
val tuple:(String,String,Int) = ("王子晨", "小姐姐", 23)
//下标访问元组数据
println(tuple._1)
println(tuple._2)
println(tuple._3)
//索引访问
println(tuple.productElement(1))
//迭代器访问
for (elem <- tuple.productIterator) {
    println(elem)
}
//Map集合中键值对就是一种特殊的元组，元组元素为2我们称之为对偶
//元组创建Map
val m1 = Map(("木田彩水", 23), ("新恒结衣", 23))
for (e<- m1.keys) println(s"${e}--${m1.get(e)getOrElse(0)}")
```
## 集合基本属性和常用操作
### 基本属性和常用操作
+ 创建集合样例 li
```scala
val li = List(1, 2, 3, 4, 5)
val li2 = List(5,6,7,8)
```
+ 获取集合长度-->length 元素个数
```scala
li.length
```
+ 获取集合大小-->size  容器大小
```scala
li.size
```
+ 循环遍历-->foreach
```scala
li.foreach(println)
```
+ 迭代器-->iterator
```scala
for (elem <- li.iterator) {
    println(elem)
}
```
+ 生成字符串-->mkString
```scala
println(li.mkString("-")) 
```
+ 是否包含-->contains
```scala
println(li.contains(3))
```
### 衍生集合
+ 获取集合的头--> head
```scala
li.head
```
+ 获取集合的尾（不是头的就是尾）--> tail
```scala
li.tail //除了头以外的其他元素
```
+ 集合最后一个数据 --> last
```scala
li.last
```
+ 集合初始数据（不包含最后一个）--> init
```scala
li.init
```
+ 反转 --> reverse
```scala
li.reverse
```
+ 取前（后）n个元素 --> take|takeRight
```scala
li.take(3)
li.takeRight(3)
```
+ 去掉前（后）n个元素 --> drop|dropRight
```scala
li.drop(2)
li.dropRight(2)
```
+ 并集 --> union
```scala
li.union(li2)
```
+ 交集 --> intersect
```scala
li.intersect(li2)
```
+ 差集 --> diff
```scala
li.diff(li2)  //li去掉于li2的交集
li2.diff(li)  //li2去掉于li的交集
```
+ 拉链 --> zip
```scala
li.zip(li2) //li与li2按顺序配对形成新的元组，若数据不一致丢弃超额的
```
+ 滑窗 --> sliding
1. 集合.sliding(a,b)
2. a-->cover的窗口大小     
3. b-->每次移动的步长
```scala
li2.sliding(3,3)
val s = li.sliding(2, 3)
s.foreach(println)
```
### 集合计算初级函数
+ 求和 --> sum
```scala
li.sum
```
+ 求乘积 --> product
```scala
li.product
```
+ 最大值 --> max
```scala
li.max
```
+ 最小值 --> min
```scala
li.min
```
+ 排序 --> sorded|sortBy|sortWith
```scala
li.sorted
li.sortBy(e => e) //填入比较规则此处填入自身
//按照绝对值排序
val test1 = List(-2, 4, 23, 6, 8, 5, 1)
test1.sortBy(e=>e.abs)
//自定义比较规则
li.sortWith((a:Int,b:Int)=>{a<b}) //升序
```
### 集合计算高级函数
+ 过滤 filter(函数：指定过滤条件) --> 遍历一个集合并从中获取满足指定条件的元素组成一个新的集合
```scala
li.filter((e)=>{println(e%2==0)}) //过滤出偶数
//简化==>
li.filter(_%2==0)
```
+ 转换/映射  map
```scala
//集合元素集体*2
li.map(_*2)
```
+ 扁平化 flatten  ::: --> 将集合中元素由整体转换为个体的过程
```scala
val cacs = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))
println(cacs)
val f = cacs.flatten
println(f)
```
+ 扁平映射  flatMap  --> 先映射再进行扁平化处理（传入映射规则--表达式）
```scala
//例所使用符串对空格进行分割将分割后的单词放到一个小新的集合中
val str = List("时代变了 大凡大","王子晨 可爱的","大疆","不足 萌妹子","王子晨 萌妹子","昭和时代 木田彩水")
//先Map 映射 后 flatten 扁平化
val ss:List[String] = str.flatMap(_.split(" "))
println(ss.groupBy(_.charAt(0)))
```
+ 分组	gruopBy --> 按照一定的分组规则，将集合中的元素放到不同的组中 （传入映射规则--表达式）
```scala
li.groupBy(a=>{a%2==0}) //分奇偶组
```
>简化|规约
+ 对集合内部元素之间进行聚合 
+ reduce   聚合的数据类型一致
+ reduceLeft|reduceRight 	聚合的数据类型可以不一致
+ 聚合函数底层由递归实现
```scala
li.reduceRight((a:Int,b:Int)=>{b-a})  //右启开始聚合
//聚合函数传入聚合规则  
i1.reduce(_+_)                       //左启开始聚合
```
>折叠
+ 对外部元素和集合内部元素之间进行聚合
+ fold 	聚合的数据类型一致
+ foldLeft|foldRight		聚合的数据类型可以不一致
```scala
li.fold(10)(_+_)      //集合外10与集合内元素聚合
```
+ 例，合并2个Map集合
```scala
//合并2个Map集合
val m1 = mutable.Map("a" -> 1, "b" -> 2, "c" -> 3)
val m2 = mutable.Map("a" -> 4, "b" -> 5, "d" -> 6)
val res:mutable.Map[String,Int] = m1.foldLeft(m2) { 
    //m2e表示m2，kv表示m1中的每一个元素
    (m2e, kv) => {
    val k = kv._1
    val v = kv._2
    //根据m1中的key到m2获取val
    m2e(k) = m2e.getOrElse(k, 0) + v
    m2e
}}
println(res)
```
## WordCount样例
###  需求：将集合中出现的相同单词进行计数，取排名前三的单词
```scala
val stringList = List("Hello Scala Hbase kafka", "Hello Scala Hbase", "Hello Scala", "Hello")
val wordlist = stringList.flatMap(_.split(" "))
//指定分组规则，按自身字符串名称分组
wordlist.groupBy(e => e)
//转换map的结构-->元组为后面排序做准备
val str = wordlist.groupBy(e => e).map(e => {
    (e._1, e._2.size)
})
//指定排序规则
val sorts = str.toList.sortWith(_._2 > _._2)
//去前三
sorts.take(3)
```