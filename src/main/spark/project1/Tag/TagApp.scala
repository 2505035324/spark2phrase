package project1.Tag

import project1.Util.{JedisTest, TagTriat}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.Row
import redis.clients.jedis.Jedis

/**
  * redis实现打标签
  */
object TagApp extends TagTriat {
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String, Int)]()

    //解析参数
    val row = args(0).asInstanceOf[Row]
    val jedis = args(1).asInstanceOf[Jedis]
    //获取Appname（String）
    val appname = row.getAs[String]("appname")
    //获取Appid（String）
    val appid = row.getAs[String]("appid")
    if (StringUtils.isNotBlank(appname)) {
      list :+= ("APP" + appname,1)
    }else if (StringUtils.isNotBlank(appid)) {
        list :+= ("APP" + jedis.get(appid), 1)
    }
    jedis.close()
    list
  }
}
