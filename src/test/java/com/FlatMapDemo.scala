package com

object FlatMapDemo {
  def main(args: Array[String]): Unit = {
    val names = List("Alice", "Bob", "Nick")
    val list = List(List("zhangsan",19),List("lisi",20),List("yajun",22))
    //需求是将 List 集合中的所有元素，进行扁平化操作，即把所有元素打散
    val list1: List[Any] = list.flatten
    val names2 = names.flatMap(upper)
    val name3 = names.flatten
    println(name3)
    println("list1="+list1)
    println("names2=" + names2)
  }

  def upper(s: String): String = {
    s.toUpperCase
  }
}
