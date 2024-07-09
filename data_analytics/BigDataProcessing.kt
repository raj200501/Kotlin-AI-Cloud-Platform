package data_analytics

import org.apache.spark.sql.SparkSession

object BigDataProcessing {
    private val spark = SparkSession.builder()
        .appName("BigDataProcessing")
        .master("yarn")
        .getOrCreate()

    fun processBigData(filePath: String) {
        val df = spark.read().option("header", "true").csv(filePath)
        df.groupBy("categoryColumn").count().show()
    }
}
