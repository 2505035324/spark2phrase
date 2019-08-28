package project1.Tag

import project1.Util.TagTriat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.Row

object TagKeyWord extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String,Int)]()

    //解析参数
    val row = args(0).asInstanceOf[Row]
    val stopword = args(1).asInstanceOf[Broadcast[Map[String, Int]]]

    // 获取关键字，打标签
  val kwds: Array[String] = row.getAs[String]("keywords").split("\\|")
    // 按照过滤条件进行过滤数据
    kwds.filter(word=>{
      word.length>=3 && word.length <=8 && !stopword.value.contains(word)
    })
      .foreach(word=>list:+=("K"+word,1))
    list
  }
}
