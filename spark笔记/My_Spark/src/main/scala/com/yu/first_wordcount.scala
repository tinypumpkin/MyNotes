package com.yu
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


object first_wordcount {
  def main(args: Array[String]): Unit = {
    //1.创建SparkConf并设置App名称
    val conf = new SparkConf().setAppName("wc").setMaster("local[*]")

    //2.创建SparkContext，该对象是提交Spark App的入口
    val sc = new SparkContext(conf)

    //3.读取指定位置文件:hello atguigu atguigu
//    val file = sc.textFile("F:\\BaiduNetdiskDownload\\大数据\\spark\\input\\wc.txt")
    val file = sc.textFile(args(0))
    //4. 扁平化映射结构转化
    val tr = file.flatMap(_.split(" ")).map((_,1))
    //5. 将转换结构后的数据进行聚合处理
    val wc:RDD[(String,Int)] = tr.reduceByKey(_ + _)
    //6. 将统计结果采集到控制台打印
//    val wcarr:Array[(String,Int)] = wc.collect()
//    wcarr.foreach(println)
    //6. 保存文件到指定位置
    wc.saveAsTextFile(args(1))

    //7.关闭连接
    sc.stop()
  }
}
