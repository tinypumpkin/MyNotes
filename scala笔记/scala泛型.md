# scala泛型
### 泛型模板
>不可变性:声明类型,创建时只能放声明类型的数据
```scala
class 类名[T]
```
>协变:除了放本身类型,还可以放子类型
```scala
class 类名[+T]
```
>逆变：除了放本身类型,还可放父类型
```scala
class 类名[-T]
```

### 泛型通配符
>上界 -- 当前模板类型T必须是指定类型(这里是test)自身或test的子类
```scala
def down[T<:test](t:Class[T]): Unit ={}
```
>下界 -- 当前模板类型T必须是指定类型(这里是test)自身或test的父类
```scala
def up[T>:girl](t:Class[T]): Unit ={}
```
+ 示例代码
```scala
class Person{}
class girl extends Person{}
class girlfriend extends girl{}

//泛型模板
//不可变性:声明类型,创建时只能放声明类型的数据
class wife[T]{}
//协变:除了放本身类型,还可以放子类型
class wife_1[+T]{}
//逆变：除了放本身类型,还可放父类型
class wife_2[-T]{}
/*-------------------------------------------------------------------------*/ 
object Generic {
  def main(args: Array[String]): Unit = {
    //不可变性
    val wife:wife[girlfriend] = new wife[girlfriend]

    //协变
    val wifi1:wife_1[girl] = new wife_1[girlfriend]
    //逆变
    val wife2:wife_2[girl] =new wife_2[Person]

    //下界
    down(classOf[girl])
    down(classOf[Person])
    //上界
    up(classOf[girl])
    up(classOf[girlfriend])
  }
    //泛型通配符
    // 下界(当前模板类型T必须是girl或girl的父类)
  def down[T>:girl](t:Class[T]): Unit ={}
    // 上界(当前模板类型T必须是girl或girl的子类)
  def up[T<:girl](t:Class[T]): Unit ={}
}
```