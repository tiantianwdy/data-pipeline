package au.com.nicta.data.pipeline.core.examples

import au.com.nicta.data.pipeline.core.models.{SparkRDDProc, PipelineContext, SparkProc, SparkPipe}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

import scala.collection.mutable


/**
 * Created by tiantian on 20/07/15.
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
    val input = in.reduce(_ union _) // union of the input
    val res = input.map(_.split(",")).map(str => str(0)-> str(1).toInt)
    Left(res)
  }
}



