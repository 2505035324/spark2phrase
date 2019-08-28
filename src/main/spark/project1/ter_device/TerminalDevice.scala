package project1.ter_device

import project1.Util.RptUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}

object TerminalDevice {
  def main(args: Array[String]): Unit = {
    // 判断路径是否正确
    if(args.length != 2){
      println("目录参数不正确，退出程序")
      sys.exit()
    }
    // 创建一个集合保存输入和输出目录
    val Array(inputPath,outputpath) = args
    val conf = new SparkConf().setAppName(this.getClass.getName).setMaster("local[*]")
      // 设置序列化方式 采用Kyro序列化方式，比默认序列化方式性能高
      .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
    // 创建执行入口
    val sc = new SparkContext(conf)
    val sQLContext = new SQLContext(sc)
    // 获取数据
    import sQLContext.implicits._
    val df: DataFrame = sQLContext.read.parquet(inputPath)
    // 将数据进行处理，统计各个指标
    val res: RDD[String] = df.rdd.map(row => {
      // 把需要的字段全部取到
      val requestmode = row.getAs[Int]("requestmode")
      val processnode = row.getAs[Int]("processnode")
      val iseffective = row.getAs[Int]("iseffective")
      val isbilling = row.getAs[Int]("isbilling")
      val isbid = row.getAs[Int]("isbid")
      val iswin = row.getAs[Int]("iswin")
      val adorderid = row.getAs[Int]("adorderid")
      val WinPrice = row.getAs[Double]("winprice")
      val adpayment = row.getAs[Double]("adpayment")
      // key 值 运营商名称
      // val ispname = row.getAs[String]("ispname")
      // key 值 网络类型 networkmannername
      //val networkmannername = row.getAs[String]("networkmannername")
      //key 值 client int 设备类型 （1：android 2：ios 3：wp
      //val client = row.getAs[Int]("client")
      //key 值 devicetype int 设备类型（1：手机 2：平板）
      val devicetype = row.getAs[Int]("devicetype")
      // 创建三个对应的方法处理九个指标
      val reqlist = RptUtils.request(requestmode, processnode)
      val clicklist = RptUtils.click(requestmode, iseffective)
      val adlist = RptUtils.Ad(iseffective, isbilling, isbid, iswin, adorderid, WinPrice, adpayment)
      ((devicetype), reqlist ++ clicklist ++ adlist)
    }).reduceByKey((list1, list2) => {
      list1.zip(list2).map(t => t._1 + t._2)
    }).map(t => {
      t._1 + "," + t._2.mkString(",")
    })
    res.coalesce(1).saveAsTextFile(outputpath)
  }
}
