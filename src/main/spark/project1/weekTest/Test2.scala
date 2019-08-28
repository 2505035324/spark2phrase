package project1.weekTest

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql. SparkSession

/**
  * json数据归纳格式（考虑获取到数据的成功因素 status=1成功 starts=0 失败）：
  *  1、按照pois，分类businessarea，并统计每个businessarea的总数。
  *  2、按照pois，分类type，为每一个Type类型打上标签，统计各标签的数量
  */
object Test2 {
  def main(args: Array[String]): Unit = {
    //创建入口对象
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    //读取json数据
    val jsondata: RDD[String] = spark.sparkContext.textFile("d:/json.txt")
    val lst: List[String] = jsondata.collect().toList
//    val arr: mutable.Buffer[String] = jsondata.collect().toBuffer
    //创建空集合
    var list: List[String] = List()
    for(str <- lst) {
      //将遍历出的数据转换为字符串格式
//      val str: String = lst(i).toString

      //将字符串形式的json数据传参
      val jsonparse: JSONObject = JSON.parseObject(str)

      // 判断状态是否成功
      val status = jsonparse.getIntValue("status")
      if(status == 0) return ""
      // 接下来解析内部json串，判断每个key的value都不能为空
      val regeocodeJson = jsonparse.getJSONObject("regeocode")
      if(regeocodeJson == null || regeocodeJson.keySet().isEmpty) return ""
      //获取pois数组
      val poisArray = regeocodeJson.getJSONArray("pois")


      // 创建集合 保存数据
      val listbuf = collection.mutable.ListBuffer[String]()

      // 循环输出
      for (item <- poisArray.toArray) {
        item match {
          case json: JSONObject =>
            //将type对应的value添加到集合内
            listbuf.append(json.getString("type"))
          case _ =>
        }
      }
      //因为公司类型里是用;间隔的所以转换成字符串形式时要用;
      list:+=listbuf.mkString(";")
    }
    //val res: List[(String, Int)] =
    list.flatMap(str => str.split(";"))
      .map(str => ("type：" + str, 1))
      .groupBy(x => x._1)
      .mapValues(x => x.size)
      .toList
      .sortWith((x,y)=>x._2>y._2)
      .foreach(println)
  }
}
