package project1.Rpt

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}

object SqlLocationRpt {
  def main(args: Array[String]): Unit = {
//    // 判断路径是否正确
//    if(args.length != 2){
//      println("目录参数不正确，退出程序")
//      sys.exit()
//    }
    val conf = new SparkConf().setAppName(this.getClass.getName).setMaster("local[*]")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._
    // 创建一个集合保存输入和输出目录
//    val Array(inputPath,outputpath) = args
    val df: DataFrame = spark.read.parquet("d:/outpath")
    df.createTempView("locationrpt")

    val res: DataFrame = spark.sql(
        "select provincename," +
      "cityname," +
      "sum(if(requestmode = 1 and processnode >=1,1,0))  `原始请求`," +
      "sum(if(requestmode = 1 and processnode >=2,1,0))  `有效请求`," +
      "sum(if(requestmode = 1 and processnode =3,1,0))  `广告请求`," +
      "sum(if(iseffective = 1 and isbilling=1 and isbid=1,1,0)) `参与竞价的次数`," +
      "sum(if(iseffective = 1 and isbilling=1 and iswin=1 and adorderid != 0,1,0)) `竞价成功数`," +
      "sum(if(requestmode = 2 and iseffective = 1,1,0)) `展示量`," +
      "sum(if(requestmode = 3 and iseffective = 1,1,0)) `点击量`," +
      "sum(if(iseffective = 1 and isbilling=1 and iswin=1,adpayment,0)) / 1000.0 `DSP广告成本`," +
      "sum(if(iseffective = 1 and isbilling=1 and iswin=1,winprice,0)) / 1000.0 `DSP广告消费`" +
      "from locationrpt group by provincename,cityname")
      res.show()
//    res.coalesce(1).write.save("d:/output/areal_distribution2")
    spark.close()
  }
}
