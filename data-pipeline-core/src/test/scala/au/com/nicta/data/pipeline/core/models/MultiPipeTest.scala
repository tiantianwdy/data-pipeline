package au.com.nicta.data.pipeline.core.models

import au.com.nicta.data.pipeline.core.examples.SumReducer
import au.com.nicta.data.pipeline.core.examples.WordCountMapper
import au.com.nicta.data.pipeline.core.examples.WordCountSparkProc

import org.junit.Test

import scala.collection.mutable

/**
 * Created by tiantian on 30/07/15.
 */
class MultiPipeTest {

  @Test
  def testMutiPipeConnection: Unit ={
    val pipe1 = new SparkPipe(name = "datapipeline-examples", version ="0.0.1",
      exec = new WordCountSparkProc,
      inputPath =  mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
    )

    val pipe2 = new MRPipe(name = "mr-pipe", version = "0.0.1",
      mapper = new WordCountMapper,
      reducer = new SumReducer,
      combiner = new SumReducer
    )

    val pipe3 = new ShellPipe(name = "shell-pipe", version = "0.0.1",
      script = "hdfs dfs -tail /user/tiantian/data-pipeline/mr-pipe/0.0.1/part-r-00000")


    // submit dependency before execution
    PipelineContext.explain(pipe1 :-> pipe3 <-: pipe2)
  }

}
