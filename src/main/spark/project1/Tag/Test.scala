package project1.Tag

import org.apache.spark.sql.{DataFrame, SparkSession}

object Test {
  def main(args: Array[String]): Unit = {
    // 创建sparksession
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getName)
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    val df: DataFrame = spark.read.parquet("d:/outpath")
    df.rdd.map(row=>{
      val businesss = TagBusinesss.makeTags(row)
      businesss
    }).foreach(println)
  }
}
