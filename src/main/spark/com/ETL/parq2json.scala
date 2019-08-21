package com.ETL

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object parq2json {
  def main(args: Array[String]): Unit = {
//    //判断路径是否正确
//    if(args.length != 2){
//      println("目录参数不正确，退出程序")
//      sys.exit()
//    }
//    // 创建一个集合保存输入和输出目录
//    val Array(inputPath,outputPath) = args
    val conf = new SparkConf().setAppName(this.getClass.getName).setMaster("local[*]")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    //读取parquet文件数据
    val data: DataFrame = spark.read.parquet("d:/outpath")
    //获取需要的属性值
    val cols: DataFrame = data.select("provincename","cityname")
    cols.createTempView("provin_city")
    //统计计算
    val df: DataFrame = spark.sql("select provincename,cityname,count(1) cnt from provin_city group by provincename,cityname")

    //将结果数据保存到mysql
    //1.存到MySQL 记载配置文件才需要
    val load: Config = ConfigFactory.load()
    val pro: Properties = new Properties()
    pro.setProperty("user",load.getString("jdbc.user"))
    pro.setProperty("password",load.getString("jdbc.password"))
    //df.write.mode("append").jdbc(load.getString("jdbc.url"),load.getString("jdbc.TableName"),pro)
//    val props = getProperties()
//    df.write.mode(SaveMode.Overwrite).jdbc(props._2,"pro_city_cnt",props._1)

    //2.将结果保存到hdfs上
    //df.write.mode(SaveMode.Overwrite).partitionBy("provincename","cityname").format("json").save("hdfs://hadoop01:9000/advertisment")
    //df.write.mode(SaveMode.Overwrite).partitionBy("provincename","cityname").json("hdfs://hadoop01:8020/advertisment")

    //3.将结果以json格式保存到本地
    df.coalesce(1).write.json("d:/output/advertisment")

    spark.stop()
  }
//  /**
//    * 请求数据库的配置信息
//    *
//    * @return
//    */
//  def getProperties() = {
//    val prop = new Properties()
//    prop.put("user", "root")
//    prop.put("password", "123456")
//    val url = "jdbc:mysql://localhost:3306/advertisment?useUnicode=true&characterEncoding=utf8"
//    (prop, url)
//  }
}
