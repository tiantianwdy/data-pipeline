package au.com.nicta.data.pipeline.core.server

import java.nio.file.{Files, Paths}

import au.com.nicta.data.pipeline.core.executor.{AkkaCallbackEntry, PipeExecutionContext}
import au.com.nicta.data.pipeline.core.messages.PipeSubmitMsg
import au.com.nicta.data.pipeline.core.models.{MRPipe, Pipe, SparkPipe}

/**
 * Created by tiantian on 25/09/15.
 */
object DependencyClient {

  def submitPipe(name:String, version:String, file:String): Unit ={
    val depFile = Paths.get(file)
    val depBytes:Array[Byte] = Files.readAllBytes(depFile)

    val msg = PipeSubmitMsg(name, version, depBytes)
    println(AkkaCallbackEntry.sendCallBack(PipeExecutionContext.DEFAULT_PIPELINE_SERVER, msg))
  }


}
