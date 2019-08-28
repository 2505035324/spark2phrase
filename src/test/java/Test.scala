import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import redis.clients.jedis.JedisPool

object Test {
  def main(args: Array[String]): Unit = {
    //创建pool对象
    val pool = new JedisPool("192.168.80.141", 6379)
    //通过pool获取jedis实例
    val jedis = pool.getResource
    jedis.set("k1","v1")
    val str: String = jedis.get("k1")
    println(str)
    //关闭jedis
    jedis.close()
    //关闭jedisPool
    pool.close()
  }
}
