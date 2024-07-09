package data_analytics

import org.apache.spark.sql.SparkSession

object DataIngestion {
    private val spark = SparkSession.builder()
        .appName("DataIngestion")
        .master("local")
        .getOrCreate()

    fun loadCsv(filePath: String) {
        val df = spark.read().option("header", "true").csv(filePath)
        df.show()
    }
}
