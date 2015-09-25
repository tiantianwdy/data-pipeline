package au.com.nicta.datapipeline.examples

import java.nio.file.{Files, Paths}

import au.com.nicta.data.pipeline.core.executor.{PipeExecutionContext, AkkaCallbackEntry}
import au.com.nicta.data.pipeline.core.manager.DependencyManager
import au.com.nicta.data.pipeline.core.messages.PipeSubmitMsg
import au.com.nicta.data.pipeline.core.models.{SparkPipe, MRPipe, Pipe}

/**
 * Created by tiantian on 25/09/15.
 */
object DependencySubmit {

  def submitPipe(name:String, version:String, file:String): Unit ={
    val depFile = Paths.get(file)
    val depBytes:Array[Byte] = Files.readAllBytes(depFile)

    val msg = PipeSubmitMsg(name, version, depBytes)
    println(AkkaCallbackEntry.sendCallBack(PipeExecutionContext.DEFAULT_PIPELINE_SERVER, msg))
  }

  def submitAllDependency(pipeList:Pipe[_,_]*): Unit ={
    pipeList.foreach { pipe => pipe match {
      case p: MRPipe =>
        submitPipe(p.name, p.version,
          "/home/tiantian/Dev/workspace/data-pipeline/data-pipeline-examples/target/data-pipeline-examples.jar" )
      case p: SparkPipe[_,_] =>
        submitPipe(p.name, p.version,
          "/home/tiantian/Dev/workspace/data-pipeline/data-pipeline-examples/target/data-pipeline-examples-0.0.1.jar" )
      }
    }
  }
}
