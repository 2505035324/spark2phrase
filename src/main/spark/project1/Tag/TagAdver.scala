package project1.Tag

import project1.Util.TagTriat
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.Row

/**
  * 广告标签
  */
object TagAdver extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String,Int)]()

    //解析参数
    val row = args(0).asInstanceOf[Row]

    //获取广告类型（int）。广告名称（String）
    val adType = row.getAs[Int]("adspacetype")
    adType match {
      case v if v > 9 => list:+= ("LC"+v,1)
      case v if v<= 9 => list:+=("LC0"+v,1)
    }
    val adName = row.getAs[String]("adspacetypename")
    if(StringUtils.isNotBlank(adName)){
      list :+=("LN"+adName,1)
    }
    list
  }

}
