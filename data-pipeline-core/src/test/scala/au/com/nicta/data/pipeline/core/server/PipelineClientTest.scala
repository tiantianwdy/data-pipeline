package au.com.nicta.data.pipeline.core.server

import java.nio.file.{Paths, Path, Files}

import au.com.nicta.data.pipeline.core.examples.{WordCountSparkProc, SumReducer, WordCountMapper}
import au.com.nicta.data.pipeline.core.models.{ShellPipe, SparkPipe, MRPipe}
import org.junit.Test

import au.com.nicta.data.pipeline.core.executor.{SimplePipeExecutor, AkkaCallbackEntry, PipeExecutionContext}
import au.com.nicta.data.pipeline.core.messages._

import scala.collection.mutable


/**
 * Created by tiantian on 20/09/15.
 */
class PipelineClientTest {

  val pipelineServer = PipeExecutionContext.DEFAULT_PIPELINE_SERVER

  @Test
  def testPipeDepSubmit(): Unit ={
    val depFile = Paths.get("/home/tiantian/Dev/workspace/datapipeline-examples/target/datapipeline-examples-0.0.1.jar")
    val depBytes:Array[Byte] = Files.readAllBytes(depFile)
    val msg = PipeSubmitMsg("test-spark-pipe", "0.0.1", depBytes)
    val res = AkkaCallbackEntry.sendCallBack(pipelineServer, msg)
    println(res)
  }

  @Test
  def testPipelineJobSubmission(): Unit ={
    import au.com.nicta.data.pipeline.core.models._

    val mrPipe = MRPipe(name = "mr-pipe", version = "0.0.1",
      mapper = new WordCountMapper,
      reducer = new SumReducer,
      combiner = new SumReducer,
      inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
    )

    val sparkPipe = new SparkPipe(name = "datapipeline-examples", version = "0.0.1",
      exec = new WordCountSparkProc,
      inputPath =  mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
    )

    val pipe3 = new ShellPipe(name = "shell-pipe", version = "0.0.1",
      script = "hdfs dfs -tail /user/tiantian/data-pipeline/mr-pipe/0.0.1/part-r-00000")

    val pipeline = (mrPipe, sparkPipe) ->: pipe3

    val msg = PipelineJobMsg("testedPipeline", Seq(pipeline))
    val res = AkkaCallbackEntry.sendCallBack(pipelineServer, msg)
    println(res)
  }

  @Test
  def testQueryMsgs(): Unit ={
    import au.com.nicta.data.pipeline.core.models._

    val mrPipe = MRPipe(name = "test-mr-pipe", version = "0.0.1",
      mapper = new WordCountMapper,
      reducer = new SumReducer,
      combiner = new SumReducer,
      inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
    )

    val sparkPipe = new SparkPipe(name = "test-spark-pipe", version = "0.0.1",
      exec = new WordCountSparkProc,
      inputPath =  mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
    )

    val pipe3 = new ShellPipe(name = "test-shell-pipe", version = "0.0.1",
      script = "hdfs dfs -tail /user/tiantian/data-pipeline/mr-pipe/0.0.1/part-r-00000")

    val depFile = Paths.get("/home/tiantian/Dev/workspace/datapipeline-examples/target/datapipeline-examples-0.0.1.jar")
    val depBytes:Array[Byte] = Files.readAllBytes(depFile)

    val msg = PipeSubmitMsg("test-mr-pipe", "0.0.1", depBytes)
    val msg2 = PipeSubmitMsg("test-spark-pipe", "0.0.1", depBytes)
    val msg3 = PipeSubmitMsg("test-shell-pipe", "0.0.1", depBytes)

    println(AkkaCallbackEntry.sendCallBack(pipelineServer, msg))
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, msg2))
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, msg3))

    val query = QueryPipeHistory(name = "test-spark-pipe", version = "0.0.1")
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, query))

    // update with more dependencies
    val msg4 = PipeSubmitMsg("test-spark-pipe", "0.0.2", depBytes)
    val msg5 = PipeSubmitMsg("test-spark-pipe", "0.0.3", depBytes)
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, msg4))
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, msg5))

    val query2 = QueryPipeHistory(name = "test-spark-pipe", version = "")
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, query2))

    //execution
    val pipeline = (mrPipe, sparkPipe) ->: pipe3
    //
    val job = PipelineJobMsg("testedPipeline", Seq(pipeline))
    val res = AkkaCallbackEntry.sendCallBack(pipelineServer, job)

  }

  @Test
  def testQueryExecutionHistory(): Unit ={
    val query2 = QueryPipeHistory(name = "test-spark-pipe", version = "")
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, query2))

    val query3 = QueryPipelineHistory(pipelineName = "testedPipeline", executionTag = "")
    println(AkkaCallbackEntry.sendCallBack(pipelineServer, query3))
  }

}
