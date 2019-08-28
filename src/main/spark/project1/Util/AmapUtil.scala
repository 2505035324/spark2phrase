package project1.Util

import com.alibaba.fastjson._
/**
  * 商圈解析工具
  * json数据归纳格式（考虑获取到数据的成功因素 status=1成功 starts=0 失败）：
  * 1、按照pois，分类businessarea，并统计每个businessarea的总数。
  * 2、按照pois，分类type，为每一个Type类型打上标签，统计各标签的数量
  */
object AmapUtil {

  // 获取高德地图商圈信息
  def getBusinessFromAmap(long:Double,lat:Double):String= {
    //https://restapi.amap.com/v3/geocode
    // /regeo?output=xml&location=116.310003,39.991957&key=<用户的key>&radius=1000&extensions=all
    //	05aa2e716c1b49137975f6e1779f5506
    val location = long+","+lat
    val urlStr = "https://restapi.amap.com/v3/geocode/regeo?location="+location+"&key=05aa2e716c1b49137975f6e1779f5506"
    // 调用请求
    val jsonstr = HttpUtil.get(urlStr)
    // 解析json串
    val jsonparse = JSON.parseObject(jsonstr)
    // 判断状态是否成功
    val status = jsonparse.getIntValue("status")
    if(status == 0) return ""
    // 接下来解析内部json串，判断每个key的value都不能为空
    val regeocodeJson = jsonparse.getJSONObject("regeocode")
    if(regeocodeJson == null || regeocodeJson.keySet().isEmpty) return ""

    val addressComponentJson = regeocodeJson.getJSONObject("addressComponent")
    if(addressComponentJson == null || addressComponentJson.keySet().isEmpty) return ""

    val businessAreasArray = addressComponentJson.getJSONArray("businessAreas")
    if(businessAreasArray == null || businessAreasArray.isEmpty) return null
    // 创建集合 保存数据
    val buffer = collection.mutable.ListBuffer[String]()
    // 循环输出
    for(item <- businessAreasArray.toArray){
      item match {
        case json: JSONObject =>
          buffer.append(json.getString("name"))
        case _ =>
      }
    }
//    for(item <- businessAreasArray.toArray){
//      if(item.isInstanceOf[JSONObject]){
//        val json = item.asInstanceOf[JSONObject]
//        buffer.append(json.getString("name"))
//      }
//    }
    buffer.mkString(",")

  }
}
