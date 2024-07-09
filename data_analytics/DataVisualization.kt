package data_analytics

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession

object DataVisualization {
    private val spark = SparkSession.builder()
        .appName("DataVisualization")
        .master("local")
        .getOrCreate()

    fun visualizeData(df: Dataset<Row>) {
        df.groupBy("categoryColumn").count().show()
    }
}
