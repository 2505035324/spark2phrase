package project1.Tag

import project1.Util.TagTriat
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.Row

object TagChannel extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {

    var list = List[(String, Int)]()

    val row = args(0).asInstanceOf[Row]

    val adChannel: String = row.getAs[Int]("adplatformproviderid").toString

    if(StringUtils.isNotBlank(adChannel)){
      list:+=("CN" + adChannel,1)
    }
    list
  }
}
