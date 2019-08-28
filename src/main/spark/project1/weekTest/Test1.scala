package project1.weekTest

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import com.alibaba.fastjson._
import scala.collection.mutable
/**
  * json数据归纳格式（考虑获取到数据的成功因素 status=1成功 starts=0 失败）：
  *  1、按照pois，分类businessarea，并统计每个businessarea的总数。
  *  2、按照pois，分类type，为每一个Type类型打上标签，统计各标签的数量
  */
object Test1 {
    def main(args: Array[String]): Unit = {

      val spark: SparkSession = SparkSession.builder()
        .appName(this.getClass.getName)
        .master("local[*]")
        .enableHiveSupport()
        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .getOrCreate()
      //读取json数据
      val jsondata: RDD[String] = spark.sparkContext.textFile("d:/json.txt")

      //将数据转换为缓冲数组
      val buf: mutable.Buffer[String] = jsondata.collect().toBuffer

      //定义一个空list集合用于添加集合
      var list: List[List[String]] = List()// 创建集合 保存数据
      val buffer = collection.mutable.ListBuffer[String]()
      //循环遍历数组
      for (str <- buf) {

//        val str: String = i
//        Thread.sleep(1000)
//        println(str)
        //将遍历的json格式数据传入
        val jsonparse: JSONObject = JSON.parseObject(str)

        // 判断状态是否成功
        val status = jsonparse.getIntValue("status")
        if(status == 0) return ""
        // 接下来解析内部json串，判断每个key的value都不能为空
        val regeocodeJson = jsonparse.getJSONObject("regeocode")
        if(regeocodeJson == null || regeocodeJson.keySet().isEmpty) return ""

        val poisArray: JSONArray = regeocodeJson.getJSONArray("pois")
        //判断是否为空
        if (poisArray == null || poisArray.isEmpty) return null

        // 循环输出
        for(item <- poisArray.toArray){
          item match {
            case json: JSONObject =>
              buffer.append(json.getString("businessarea"))
            //buffer1.append(json.getString("Type"))
            case _ => ""
          }
        }
        val lst: List[String] = buffer.toList
        list :+= lst
      }
      //List[(String, Int)]
      list.flatMap(d=>d)
        .filter(_ != "[]")
        .map(x => (x, 1))
        .groupBy(x => x._1)
        .mapValues(x => x.size)
        .toList.sortWith((x,y) => x._2>y._2)
        .foreach(println)

    }
}
