package project1.Rpt

import project1.Util.RptUtils
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext, SparkSession}

/**
  * 地域分布指标
  */
object LocationRpt {
  def main(args: Array[String]): Unit = {
    //System.setProperty("hadoop.home.dir", "D:\\Huohu\\下载\\hadoop-common-2.2.0-bin-master")
    // 判断路径是否正确
    if(args.length != 1){
      println("目录参数不正确，退出程序")
      sys.exit()
    }
    // 创建一个集合保存输入和输出目录
//    val Array(inputPath,outputpath) = args
    val Array(inputPath) = args
    // 创建sparksession
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .enableHiveSupport()
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    // 获取数据
    val df: DataFrame = spark.read.parquet(inputPath)
    //    val df: DataFrame = sQLContext.read.parquet("d:/outpath")
        // 将数据进行处理，统计各个指标
    val res: RDD[((String, String), List[Double])] = df.rdd.map(row => {
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
      // key 值  是地域的省市
      val pro = row.getAs[String]("provincename")
      val city = row.getAs[String]("cityname")
      // 创建三个对应的方法处理九个指标
      val reqlist = RptUtils.request(requestmode, processnode)
      val clicklist = RptUtils.click(requestmode, iseffective)
      val adlist = RptUtils.Ad(iseffective, isbilling, isbid, iswin, adorderid, WinPrice, adpayment)
      ((pro, city), reqlist ++ clicklist ++ adlist)
    }).reduceByKey((list1, list2) => {
      list1.zip(list2).map(t => t._1 + t._2)
    })

    // .map(t => {
    //   t._1 + "," + t._2.mkString(",")
    // })
    //res.foreach(println)
    //存储到本地磁盘
    // res.coalesce(1).saveAsTextFile(outputpath)
    res.foreachPartition(data2MySql)
  }

  def data2MySql = (it:Iterator[((String,String),List[Double])]) => {
    //创建连接池核心工具类
    val dataSource = new ComboPooledDataSource()
    //第二步：连接池，url，驱动，账号，密码，初始连接数，最大连接数
    dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/advertisment?characterEncoding=utf-8");//设置url
    dataSource.setDriverClass("com.mysql.jdbc.Driver");//设置驱动
    dataSource.setUser("root");//mysql的账号
    dataSource.setPassword("123456");//mysql的密码
    dataSource.setInitialPoolSize(6);//初始连接数，即初始化6个连接
    dataSource.setMaxPoolSize(50);//最大连接数，即最大的连接数是50
    dataSource.setMaxIdleTime(60);//最大空闲时间

    //第三步：从连接池对象中获取数据库连接
    val con=dataSource.getConnection()
    val sql = "insert into LocationRpt(provincename,cityname,requestcnt ," +
      "iseffecnt ,adrequest ,isbillingcnt ," +
      "successbid ,showcnt,clickcnt,adcost ,adpay) " +
      "values(?,?,?,?,?,?,?,?,?,?,?)"
    it.foreach( tup => {
      val ps=con.prepareStatement(sql)
      ps.setString(1,tup._1._1)
      ps.setString(2,tup._1._2)
      ps.setDouble(3,tup._2.head)
      ps.setDouble(4,tup._2(1))
      ps.setDouble(5,tup._2(2))
      ps.setDouble(6,tup._2(3))
      ps.setDouble(7,tup._2(4))
      ps.setDouble(8,tup._2(5))
      ps.setDouble(9,tup._2(6))
      ps.setDouble(10,tup._2(7))
      ps.setDouble(11,tup._2(8))
      /**
        * 当分区中的数据很大时（可以更具数据预估）， ps.addBatch() 缓存可能会不够，
        * 所有在添加到缓存中时，可以让他 5万或者（估计值）执行一次 ps.executeBatch()。
        * 只需要在添加变量 var count = 0 计数就行
        */
      ps.addBatch()
      ps.executeUpdate()
      ps.executeBatch()
      ps.close()
    })
    con.close()
  }
}