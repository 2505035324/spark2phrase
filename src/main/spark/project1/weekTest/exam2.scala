package project1.weekTest

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SparkSession}

import scala.collection.mutable

object exam2 {
  def main(args: Array[String]): Unit = {

    var list: List[String] = List()
    //创建入口对象
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .enableHiveSupport()
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val log: RDD[String] = spark.sparkContext.textFile("d:/json.txt")

    val logs: mutable.Buffer[String] = log.collect().toBuffer

    for(i <- 0 until logs.length) {
      val str: String = logs(i).toString

      val jsonparse: JSONObject = JSON.parseObject(str)

      val status = jsonparse.getIntValue("status")
      if (status == 0) return ""

      val regeocodeJson = jsonparse.getJSONObject("regeocode")
      if (regeocodeJson == null || regeocodeJson.keySet().isEmpty) return ""

      val poisArray = regeocodeJson.getJSONArray("pois")
      if (poisArray == null || poisArray.isEmpty) return null

      // 创建集合 保存数据
      val buffer = collection.mutable.ListBuffer[String]()
      // 循环输出
      for (item <- poisArray.toArray) {
        if (item.isInstanceOf[JSONObject]) {
          val json = item.asInstanceOf[JSONObject]
          buffer.append(json.getString("type"))
        }
      }

      list:+=buffer.mkString(";")
    }
   val res2: List[(String, Int)] = list.flatMap(x => x.split(";"))
      .map(x => ("type：" + x, 1))
      .groupBy(x => x._1)
      .mapValues(x => x.size).toList

    res2.foreach(x => println(x))

  }
}
