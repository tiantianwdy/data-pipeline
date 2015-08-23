package au.com.nicta.data.pipeline.core.io

import java.util.Properties

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

/**
 * Created by tiantian on 31/07/15.
 */
trait DataAdapter[dm] {

  def read(inputProtocol:String, inputPath:String):dm

  def write(outputProtocol:String, outputPath:String, data:dm)

}



class SparkDataAdapter(val sc:SparkContext) extends DataAdapter[Either[RDD[_], DataFrame]] {


  def read(inputProtocol:String, inputPath:String):Either[RDD[_], DataFrame] = {

    val data = inputProtocol match {
      case "text" => sc.textFile(inputPath)
      case "json" => sc.textFile(inputPath)
      case x => sc.textFile(inputPath)
    }
    data match {
      case rdd:RDD[_] => Left(rdd)
      case df:DataFrame => Right(df)
    }
  }


  def write(outputFormat:String, outputPath:String, data:Either[RDD[_], DataFrame]  ): Unit ={
    val path = new Path(outputPath)

    data match {
      case Left(rdd) =>
        outputFormat match {
          case "text" =>
            rdd.saveAsTextFile(outputPath)
          case "json" =>
            rdd.saveAsTextFile(outputPath)
          case x => rdd.saveAsTextFile(outputPath)
        }

      case Right(df) =>
        outputFormat match {
          case "json" => df.write.json(outputPath)
          case "parquet" => df.write.parquet(outputPath)
          case "jdbc" => df.write.jdbc(outputPath, s"${path.getName}", new Properties())
          case "csv" | "text" => df.map(r=> r.mkString(",")).saveAsTextFile(outputPath)
          case x =>
            df.write.save(outputPath)
        }

    }
  }

}
