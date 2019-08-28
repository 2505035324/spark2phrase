package project1.Tag

import ch.hsr.geohash.GeoHash
import project1.Util.{AmapUtil, JedisConnectionPool, TagTriat, Utils2Type}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.Row
import redis.clients.jedis.JedisPool

object TagBusinesss extends TagTriat{
  /**
    * 打标签的统一接口
    */
  override def makeTags(args: Any*): List[(String, Int)] = {
    var list = List[(String,Int)]()
    //参数解析
    val row = args(0).asInstanceOf[Row]

    //获取经纬度数据信息
    val long = row.getAs[String]("long")//经度
    val lat = row.getAs[String]("lat") //纬度
    //过滤经纬度
    if(Utils2Type.toDouble(long)>=73.0 &&
    Utils2Type.toDouble(long)<=135.0 &&
    Utils2Type.toDouble(lat)>=3.0 &&
    Utils2Type.toDouble(lat)<=154.0){
    //先去数据库获取商圈
    val business = getBusiness(long.toDouble,lat.toDouble)
    //判断缓存中是否有此商圈
    if(StringUtils.isNotBlank(business)){
      val lines = business.split(",")
      lines.foreach(f=>list:+=(f,1))
      }
    }
    list
  }

  /**
    * 获取商圈信息
    * @param long //经度
    * @param lat  //纬度
    * @return
    */
  def getBusiness(long:Double,lat:Double):String={
    //转换GeoHash字符串
    val geohash = GeoHash.geoHashStringWithCharacterPrecision(lat,long,8)
    //去数据库查询
    var business = redis_queryBusiness(geohash)
    //判断商圈是否为空
    if(business == null || business.length == 0){
      //通过经纬度获取商圈
      business = AmapUtil.getBusinessFromAmap(long.toDouble,lat.toDouble)
      redis_insertBusiness(geohash,business)
    }
    business
  }
  /**
    * 获取商圈信息
    * @param geohash
    * @return
    */
  def redis_queryBusiness(geohash:String):String={

    //获取jedis对象
    val jedis = JedisConnectionPool.getConn()
    val business = jedis.get(geohash)
    jedis.close()
    business
  }

  /**
    * 存储商圈到redis
    * @param geoHash
    * @param business 商圈信息
    * @return
    */
  def redis_insertBusiness(geoHash:String,business:String)={
   val jedis =  JedisConnectionPool.getConn()
    jedis.set(geoHash,business)
  }
}
