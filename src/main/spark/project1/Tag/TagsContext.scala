package project1.Tag

import java.util

import project1.Util.{JedisConnectionPool, JedisTest, TagsUtil}
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.hbase.{HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import redis.clients.jedis.{Jedis, JedisPool}

/**
  * 上下文标签
  */
object TagsContext {
  def main(args: Array[String]): Unit = {
    // 判断路径是否正确
//    if (args.length != 4) {
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
//    val Array(inputPath, input, input2,days) = args
    // 创建sparksession
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    //    //读取字典文件数据，进行广播
    val dirt: RDD[String] = spark.sparkContext.textFile("D:/Program Files (x86)/spark2阶段/Spark用户画像分析/app_dict.txt")
    //数据清洗
    val tups: RDD[(String, String)] = dirt.map(_.split("\\s", -1)) //("\\t")切割也可以
      .filter(_.length >= 5)
      .map(arr => (arr(4), arr(1)))

    //将过滤之后返回的数据进行广播（转成map）
    val broadcast: Broadcast[Map[String, String]] = spark.sparkContext.broadcast(tups.collect().toMap)
    //读取停用词库stopword数据，并进行广播。
    val stopword: RDD[(String, Int)] = spark.sparkContext.textFile("D:/Program Files (x86)/spark2阶段/Spark用户画像分析/stopwords.txt").map((_, 0))
    //将返回数据进行广播
    val spbroadcast: Broadcast[Map[String, Int]] = spark.sparkContext.broadcast(stopword.collect().toMap)
    // todo 调用Hbase API
    // 加载配置文件
    val load = ConfigFactory.load()
    val hbaseTableName = load.getString("hbase.TableName")
    // 创建Hadoop任务
    val configuration = spark.sparkContext.hadoopConfiguration
    configuration.set("hbase.zookeeper.quorum",load.getString("hbase.host"))
    // 创建HbaseConnection
    val hbconn = ConnectionFactory.createConnection(configuration)
    val hbadmin = hbconn.getAdmin
    // 判断表是否可用
    if(!hbadmin.tableExists(TableName.valueOf(hbaseTableName))){
      // 创建表操作
      val tableDescriptor = new HTableDescriptor(TableName.valueOf(hbaseTableName))
      val descriptor = new HColumnDescriptor("tags")
      tableDescriptor.addFamily(descriptor)
      hbadmin.createTable(tableDescriptor)
      hbadmin.close()
      hbconn.close()
    }
    // 创建JobConf
    val jobconf = new JobConf(configuration)
    // 指定输出类型和表
    jobconf.setOutputFormat(classOf[TableOutputFormat])
    jobconf.set(TableOutputFormat.OUTPUT_TABLE,hbaseTableName)
    //读取数据
    val df: DataFrame = spark.read.parquet("d:/outpath")
    df.filter(TagsUtil.OneuserId)
      .rdd
      //接下来所有的标签都在内部实现
      .map(row => {
        val userid = TagsUtil.getUserId(row)
        //接下来通过row数据 打上所有标签。(按照需求)
        //广告标签
        val adList = TagAdver.makeTags(row)
        //app标签广播变量完成
        val appList = TagAppBroad.makeTags(row, broadcast)
        // 渠道
        val channellist: List[(String, Int)] = TagChannel.makeTags(row)
        //关键字标签
        val keywordlist = TagKeyWord.makeTags(row, spbroadcast)
        //设备
        val devicelist: List[(String, Int)] = TagsDevice.makeTags(row)
        //地域
        val areaList: List[(String, Int)] = TagArea.makeTags(row)
        (userid, adList++appList++channellist++keywordlist++devicelist++areaList)
      }).reduceByKey((list1,list2)=>{
      // List(("lN插屏",1),("LN全屏",1),("ZC沈阳",1),("ZP河北",1)....)
        //List[(String,Int)]
        list1 ::: list2
        // List(("APP爱奇艺",List()))
        .groupBy(_._1)
        .mapValues(_.foldLeft[Int](0)(_ + _._2))
        .toList
    }).map{
      case(userid,userTag)=>{

        val put = new Put(Bytes.toBytes(userid))
        // 处理下标签
        val tags = userTag.map(t=>t._1+","+t._2).mkString(",")
       put.addImmutable(Bytes.toBytes("tags"),Bytes.toBytes("20190827"),Bytes.toBytes(tags))
        (new ImmutableBytesWritable(),put)
      }
    }
      // 保存到对应表中
      .saveAsHadoopDataset(jobconf)
    spark.close()
  }
}
