package project1.Tag

import project1.Util.TagTriat
import org.apache.spark.sql.Row

object TagsDevice extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*):List[(String, Int)] = {

    var list = List[(String, Int)]()

    val row = args(0).asInstanceOf[Row]
    //client是设备类型
    val client: Int = row.getAs("client")
    client match {
      case 1 => list:+=(" Andriod D00010001", 1)
      case 2 => list:+=("ios D00010002", 1)
      case 3 => list:+=("wp D00010003", 1)
      case _ => list:+=("其他 D00010004", 1)
    }

    val networkmannername: String = row.getAs("networkmannername")
    networkmannername match {
      case "Wifi" => list:+=("D00020001", 1)
      case "4G" => list:+=("D00020002", 1)
      case "3G" => list:+=("D00020003", 1)
      case "2G" => list:+=("D00020004", 1)
      case _ => list:+=("D00020005", 1)
    }

    val ispname: String = row.getAs("ispname")
    ispname match {
      case "移动" => list:+=("D00030001", 1)
      case "联通" => list:+=("D00030002", 1)
      case "电信" => list:+=("D00030003", 1)
      case _ => list:+=("D00030004", 1)
    }
    list
  }
}
