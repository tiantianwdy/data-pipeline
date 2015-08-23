package au.com.nicta.data.pipeline.core.executor

import java.io.{InputStreamReader, BufferedReader}

import scala.util.Try

import au.com.nicta.data.pipeline.core.models.{ShellPipe, MRPipe, SparkPipe, Pipe}
import com.typesafe.config.ConfigFactory

/**
 * Created by tiantian on 17/07/15.
 */
object PipeExecutionContext {

  lazy val defaultConf = ConfigFactory.load("data-pipeline.conf")

  val DEFAULT_PIPELINE_SERVER = Try {defaultConf.getString("data.pipeline.server.address")} getOrElse ("akka.tcp://pipeline-master@127.0.1.1:8999/user/pipeline-server")

  val DEPENDENCY_HOME = Try {defaultConf.getString("data.pipeline.server.dependency.home")} getOrElse ("/home/data/data-pipeline/")

  val SPARK_EXECUTION_CONTEXT = Try {defaultConf.getString("data.pipeline.execution.spark.address")} getOrElse ("spark://127.0.0.1:7077")

  val HADOOP_EXECUTION_CONTEXT = Try {defaultConf.getString("data.pipeline.execution.hadoop.job-tracker.address")} getOrElse ("127.0.0.1:9001")

  val SHELL_CONTEXT = Try {defaultConf.getString("data.pipeline.execution.shell.bin")} getOrElse ("/bin/bash")


  def launch(pipe: Pipe[_,_], taskId:String, execTag:String) = {
    pipe match {
      case sp:SparkPipe[_,_] =>
        SparkPipeEntry.launch(sp, taskId, execTag).waitFor()

      case mp:MRPipe[_,_,_,_] =>
        MRPipeEntry.launch(mp, taskId, execTag)

      case shellPipe:ShellPipe =>
        ShellEntry.launch(shellPipe, taskId, execTag)
    }
  }

  def test(pipe:Pipe[_,_], taskId:String = "", execTag:String) = {

    pipe match {
      case sp:SparkPipe[_,_] =>
        println(sp)
        val process = SparkPipeEntry.launch(sp, taskId, execTag)
        val in = new BufferedReader(new InputStreamReader(process.getErrorStream))
        var line:String = in.readLine()
        while(line!= null){
          println(line)
          line = in.readLine()
        }
        val status = process.waitFor()
        println(s"process exit with $status ..")

      case mp:MRPipe[_,_,_,_] =>
        val res = MRPipeEntry.launch(mp,taskId, execTag)
        println(s"job history at: {$res}")

      case shellPipe:ShellPipe =>
        ShellEntry.launch(shellPipe,taskId, execTag)
    }
  }

}
