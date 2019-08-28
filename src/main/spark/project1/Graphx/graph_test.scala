package project1.Graphx

import org.apache.spark.graphx.{Edge, Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object graph_test {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(this.getClass.getName).setMaster("local[*]")
    val sc = new SparkContext(conf)
    //构造点的集合
    val vertRDD: RDD[(VertexId, (String, Int))] = sc.makeRDD(Seq(
      (1L, ("詹姆斯", 35)),
      (2L, ("霍华德", 34)),
      (6L, ("杜兰特", 31)),
      (9L, ("哈登", 31)),
      (133L, ("欧文", 30)),
      (138L, ("席娃尔", 36)),
      (16L, ("法尔考", 35)),
      (44L, ("内马尔", 27)),
      (21L, ("C罗", 28)),
      (5L, ("高斯林", 28)),
      (7L, ("奥德斯基", 28)),
      (158L, ("码云", 28))
    ))
    //构造边的集合
    val egde: RDD[Edge[Int]] = sc.makeRDD(Seq(
      Edge(1L, 133L, 0),
      Edge(2L, 133L, 0),
      Edge(6L, 133L, 0),
      Edge(9L, 133L, 0),
      Edge(6L, 138L, 0),
      Edge(16L, 138L, 0),
      Edge(44L, 138L, 0),
      Edge(21L, 138L, 0),
      Edge(5L, 158L, 0),
      Edge(7L, 158L, 0)
    ))
    //构建图
    val graph = Graph(vertRDD,egde)
    //取出每个边上的最大顶点
    val vertices: VertexRDD[VertexId] = graph.connectedComponents().vertices
    vertices.join(vertRDD).map {
      case (userId, (conId, (name, age))) => {
        (conId, List(name, age))
        }
    }.reduceByKey(_++_).foreach(println)
  }
}
