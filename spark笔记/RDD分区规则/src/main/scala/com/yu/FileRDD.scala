package com.yu

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FileRDD {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("SC").setMaster("local[*]")
    val sc: SparkContext = new SparkContext(conf)

    val rdd: RDD[String] = sc.textFile("F:\\BaiduNetdiskDownload\\大数据\\spark\\input\\input\\wc.txt",3)

    rdd.saveAsTextFile("F:\\BaiduNetdiskDownload\\大数据\\spark\\input\\output")

    sc.stop()
  }
}
