package project1.JDBCConnecionPool

import java.sql.{Connection, DriverManager}
import java.util
import java.util.Properties

object Constants{
  val JDBC_DRIVER = "jdbc.driver"
  val JDBC_URL = "jdbc.url"
  val JDBC_USER = "jdbc.user"
  val JDBC_PASSWORD = "jdbc.password"
  val JDBC_MAX_ACTIVE = "jdbc.max.active"
}
object ConnectionPools {
  val pool = new util.LinkedList[Connection]() //连接池
  //注册驱动
  try {
    val properties = new Properties()
    properties.load(ConnectionPools.getClass.getClassLoader().getResourceAsStream("db.properties"))
    Class.forName(properties.getProperty(Constants.JDBC_DRIVER))
    val maxActive = Integer.valueOf(properties.getProperty(Constants.JDBC_MAX_ACTIVE))
    val url = properties.getProperty(Constants.JDBC_URL)
    val password = properties.getProperty(Constants.JDBC_PASSWORD)
    val user = properties.getProperty(Constants.JDBC_USER)
    // 创建连接对象
    var i = 0
    while(i < maxActive) {
      pool.push(DriverManager.getConnection(url, user, password))
      i+=1
    }
  } catch {
    case _: Exception => "初始化异常~"
  }
  // 获得连接池对象
  def getConnection: Connection = {
    while (pool.isEmpty) {
      try {
        println("线程池为空，请稍后再来~~~~~")
        Thread.sleep(2000)
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
      pool.poll()
  }
    // spark写入数据库线程结束后，释放连接。也就是连接池收回连接
  def release(connection: Connection) = {
      pool.push(connection)
    }
}