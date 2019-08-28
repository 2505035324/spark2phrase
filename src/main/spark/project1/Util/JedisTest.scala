package project1.Util

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object JedisTest {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    //读取字典数据存储到redis中
    val dict: RDD[String] = spark.sparkContext.textFile("D:/Program Files (x86)/spark2阶段/Spark用户画像分析/app_dict.txt")
    //返回APP的id哈APP的name
    // 读取字段文件
    dict.map(_.split("\\t",-1))
      .filter(_.length>=5)
      .foreachPartition(arr=>{
      val jedis = JedisConnectionPool.getConn()
      arr.foreach(arr=>{
        jedis.set(arr(4),arr(1))
      })
      jedis.close()
    })
    spark.close()
  }
}
