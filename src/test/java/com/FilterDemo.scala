package com

object FilterDemo {
  def main(args: Array[String]): Unit = {
    /*选出首字母为 A 的元素
    * */
    val names = List("Alice", "Bob", "Nick")
    val names2 = names.filter(startA)
    println("names=" + names)
    println("names2=" + names2)
  }
  def startA(str:String): Boolean = {
    str.startsWith("A")
  }
}
