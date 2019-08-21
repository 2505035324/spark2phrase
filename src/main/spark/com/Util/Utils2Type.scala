package com.Util

object Utils2Type {

  //字符串转换为整型
  def toInt(str:String):Int ={
      try {
        str.toInt
      }catch {
        case _:Exception => 0
      }
  }
  //字符串转换为Double型
  def toDouble(str:String):Double = {
    try{
      str.toDouble
    }catch {
      case _:Exception => 0.0
    }
  }
}
