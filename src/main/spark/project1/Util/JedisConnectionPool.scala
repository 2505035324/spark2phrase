package project1.Util

import redis.clients.jedis.{Jedis, JedisPool}

object JedisConnectionPool {
    //创建pool对象
    val pool = new JedisPool("192.168.80.141", 6379)
    //通过pool获取jedis实例
    def getConn():Jedis ={
      pool.getResource
    }
//    //关闭jedisPool
//    pool.close()
}
