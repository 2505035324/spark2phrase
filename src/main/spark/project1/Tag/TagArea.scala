package project1.Tag

import project1.Util.TagTriat
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.Row

/**
  * 6)地域标签（省标签格式：ZPxxx->1, 地市标签格式: ZCxxx->1）xxx 为省或市名称
  */
object TagArea extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String,Int)]()

    //解析参数
    val row = args(0).asInstanceOf[Row]
    //获取数据
    val pro = row.getAs[String]("provincename")
    val city = row.getAs[String]("cityname")
    if(StringUtils.isNotBlank(pro)){
      list:+=("ZP"+pro,1)
    }
    if(StringUtils.isNotBlank(city)){
      list:+=("ZC"+pro,1)
    }
    list
  }
}
