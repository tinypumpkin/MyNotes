package com.yu

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SetRDD {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkCore")
    val sc: SparkContext = new SparkContext(conf)

    val rdd: RDD[Int] = sc.makeRDD(Array(1,2,3,4))

    rdd.saveAsTextFile("output")
    sc.stop()
  }
}
