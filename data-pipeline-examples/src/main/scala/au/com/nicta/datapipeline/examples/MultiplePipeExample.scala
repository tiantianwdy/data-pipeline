package au.com.nicta.datapipeline.examples

import au.com.nicta.data.pipeline.core.executor.ShellEntry
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import au.com.nicta.data.pipeline.core.models._

import scala.collection.mutable

/**
 * Created by tiantian on 17/07/15.
 */
object MultiplePipeExample extends App{

  val pipe1 = new SparkPipe(name = "spark-pipe-1.0.0",
    exec = new WordCountSparkProc,
    inputPath = mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/partial/rankings")
  )

  val pipe2 = MRPipe("MR-pipe-1.0.0",
    new WordCountMapper,
    new SumReducer,
    new SumReducer
  )


  val pipe3 = new ShellPipe(name = "shell-pipe", version = "0.0.1",
    script = "ps -ef | grep spark")
/*
   val pipe3 = new PyPipe("python-pipe-1.0.0", "/user/dev/pipeline.py")

   val pipe4 = new BashPipe("bash-pipe-1.0.0", /user/dev/bh-pipe.sh)
*/

  // submit dependency before execution
  PipelineContext.test(pipe1.connect(pipe2).connect(pipe3))
}


