package project1.Tag

import project1.Util.{JedisConnectionPool, TagsUtil}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * 上下文标签
  */
object TagsContext2 {
  def main(args: Array[String]): Unit = {
//    // 判断路径是否正确
//    if (args.length != 2) {
//      println("目录参数不正确，退出程序")
//      sys.exit()
//    }
    // 创建一个集合保存输入和输出目录
    /*
    inputPath:代表的是parquet文件的路径
    outputpath:代表的是Ad广告打过标签存储的路径
    inpath:代表的是字典文件的路径
    inpath2:代表的是停用词库文件的路径
     */
//    val Array(inputPath, input2) = args
    // 创建sparksession
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    //读取停用词库stopword数据，并进行广播。
    val stopword: RDD[(String, Int)] = spark.sparkContext.textFile("D:/Program Files (x86)/spark2阶段/Spark用户画像分析/stopwords.txt").map((_, 0))
    //将返回数据进行广播D:\Program Files (x86)\spark2阶段\Spark用户画像分析
    val spbroadcast: Broadcast[Map[String, Int]] = spark.sparkContext.broadcast(stopword.collect().toMap)

    //读取数据
    val df: DataFrame = spark.read.parquet("d:/outpath")
    df.filter(TagsUtil.OneuserId)
      .rdd
      //接下来所有的标签都在内部实现
      .mapPartitions(row=>{
        val jedis = JedisConnectionPool.getConn()
        var list = List[(String,List[(String,Int)])]()
        row.map(row=>{
          // 取出用户Id
          val userId = TagsUtil.getUserId(row)
          // 接下来通过row数据 打上 所有标签（按照需求）
          val adList = TagAdver.makeTags(row)
          val appList = TagApp.makeTags(row,jedis)
          val keywordList = TagKeyWord.makeTags(row,spbroadcast)
          val dvList = TagsDevice.makeTags(row)
          val loactionList = TagArea.makeTags(row)
          list:+=(userId,adList++appList++keywordList++dvList++loactionList)
        })
        jedis.close()
        list.iterator
      })
      .reduceByKey((list1,list2)=>
        // List(("lN插屏",1),("LN全屏",1),("ZC沈阳",1),("ZP河北",1)....)
        (list1:::list2)
          // List(("APP爱奇艺",List()))
          .groupBy(_._1)
          .mapValues(_.foldLeft[Int](0)(_+_._2))
          .toList
      ).foreach(println)
//    result.collect().foreach(println)
    //将结果存入本地
//    result.coalesce(1).saveAsTextFile(outputpath)
    spark.close()
  }
}
