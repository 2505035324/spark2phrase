package project1.Tag

import project1.Util.TagTriat
import org.apache.commons.lang3.StringUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row

object TagAppBroad extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String,Int)]()

    //解析参数
    val row = args(0).asInstanceOf[Row]
    val appmap = args(1).asInstanceOf[Broadcast[Map[String, String]]]

    //获取所需数据
    val appname = row.getAs[String]("appname")
    val appid = row.getAs[String]("appid")
    // 空值判断
    if(StringUtils.isNotBlank(appname)){
      list:+=("APP"+appname,1)
    }else if(StringUtils.isNoneBlank(appid)){
      list:+=("APP"+appmap.value.getOrElse(appid,appid),1)
    }
    list
  }
}
