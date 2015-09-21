package au.com.nicta.data.pipeline.core.server

import java.nio.file.{Paths, Path, Files}

import au.com.nicta.data.pipeline.core.examples.{WordCountSparkProc, SumReducer, WordCountMapper}
import au.com.nicta.data.pipeline.core.models.{ShellPipe, SparkPipe, MRPipe}
import org.junit.Test

import au.com.nicta.data.pipeline.core.executor.{SimplePipeExecutor, AkkaCallbackEntry, PipeExecutionContext}
import au.com.nicta.data.pipeline.core.messages.{PipelineJobMsg, PipeSubmitMsg, PipeCompleteMsg}

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
    val msg = PipeSubmitMsg("analysisSpark2", "0.0.1", depBytes)
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

}
