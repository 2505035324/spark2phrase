package project1.meida

import project1.Util.RptUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object MediaAnalyze {
  def main(args: Array[String]): Unit = {
    // 判断路径是否正确
    if (args.length != 2) {
      println("目录参数不正确，退出程序")
      sys.exit()
    }
    // 创建一个集合保存输入和输出目录
    val Array(inputPath, outputpath) = args
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .enableHiveSupport()
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    //获取数据
    val data: RDD[String] = spark.sparkContext.textFile("D:/Program Files (x86)/spark2阶段/Spark用户画像分析/app_dict.txt")
    //数据清洗
    val tups: RDD[(String, String)] = data.map(_.split("\\s")) //("\\t")切割也可以
      .filter(_.length >= 5)
      .map(arr => (arr(4), arr(1)))

    //将元组（appid,appname）广播出去
    val broadcastInfo: Broadcast[Map[String, String]] = spark.sparkContext.broadcast(tups.collect().toMap)

    // val sQLContext = new SQLContext(sc)
    // 获取数据
    val df: DataFrame = spark.read.parquet(inputPath)
    // 将数据进行处理，统计各个指标
    val restmp: RDD[(String, List[Double])] = df.rdd.map(row => {
      // 把需要的字段全部取到
      val appid = row.getAs[String]("appid")
      val appname = row.getAs[String]("appname")
      val requestmode = row.getAs[Int]("requestmode")
      val processnode = row.getAs[Int]("processnode")
      val iseffective = row.getAs[Int]("iseffective")
      val isbilling = row.getAs[Int]("isbilling")
      val isbid = row.getAs[Int]("isbid")
      val iswin = row.getAs[Int]("iswin")
      val adorderid = row.getAs[Int]("adorderid")
      val WinPrice = row.getAs[Double]("winprice")
      val adpayment = row.getAs[Double]("adpayment")
      // 创建三个对应的方法处理九个指标
      val reqlist = RptUtils.request(requestmode, processnode)
      val clicklist = RptUtils.click(requestmode, iseffective)
      val adlist = RptUtils.Ad(iseffective, isbilling, isbid, iswin, adorderid, WinPrice, adpayment)
      //定义一个临时变量
      var app_name = ""
      if(appname.equals("未知") || appname.equals("其他")){
        app_name = broadcastInfo.value.getOrElse(appid,null)
      }else{
        app_name = appname
      }
      (app_name,reqlist ++ clicklist ++ adlist)
    })
    //过滤Appname的空值字段
    val rest: RDD[(String, List[Double])] = restmp.filter(_._1 != null)
    val res: RDD[String] = rest.reduceByKey((list1, list2) => {
      list1.zip(list2).map(t => t._1 + t._2)
    }).map(t => {
      t._1 + "," + t._2.mkString(",")
    })
    res.coalesce(1).saveAsTextFile(outputpath)
  }
}
