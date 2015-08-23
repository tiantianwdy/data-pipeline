package au.com.nicta.datapipeline.examples

import au.com.nicta.data.pipeline.core.executor.SparkPipeEntry
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import au.com.nicta.data.pipeline.core.models.{SparkRDDProc, PipelineContext, SparkPipe, SparkProc}
import org.apache.spark.sql.{SQLContext, DataFrame}

import scala.collection.mutable

/**
 * Created by tiantian on 15/07/15.
 */
object SparkPipeExample  extends App{


  val pipe1 = new SparkPipe(name = "datapipeline-examples", version = "0.0.1",
    exec = new WordCountSparkProc,
    inputPath =  mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/partial/rankings")
  )
  PipelineContext.test(pipe1)

}

class WordCountSparkProc extends  SparkRDDProc[String, (String, Int)] {



  //  override def process: (RDD[String]) => RDD[(String, Int)] = (in:RDD[String]) => in.map(_.split(",")).map(str => str(0)-> str(1).toInt)
  override def process(in: Seq[RDD[String]], sc: SparkContext): Either[RDD[(String, Int)], DataFrame] = {
    val res = in.head.map(_.split(",")).map(str => str(0)-> str(1).toInt)
    Left(res)
  }
}


class SparkSQLProc extends SparkRDDProc[String, Any] {

  import org.apache.spark.sql._

  case class Rankings(url:String, ranking:Float)

  override def process(in: Seq[RDD[String]], sc: SparkContext): Either[RDD[Any], DataFrame] = {
    val sqlContext = new  SQLContext(sc)

    import sqlContext.implicits._

    val rankings  = in.head.map(_.split(" ")).map(r => Rankings(r(0), r(1).trim.toFloat)).toDF
    rankings.registerTempTable("rankings")

    val res = sqlContext.sql("select * from rankings where ranking > 10")
    Right(res)
  }
}