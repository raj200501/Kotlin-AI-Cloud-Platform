package data_analytics

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession

object DataTransformation {
    private val spark = SparkSession.builder()
        .appName("DataTransformation")
        .master("local")
        .getOrCreate()

    fun transformData(df: Dataset<Row>): Dataset<Row> {
        return df.withColumnRenamed("oldColumnName", "newColumnName")
            .filter(df.col("filterColumn") === "filterValue")
    }
}
