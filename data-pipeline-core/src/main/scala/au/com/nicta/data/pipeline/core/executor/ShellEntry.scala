package au.com.nicta.data.pipeline.core.executor

import java.io.File
import java.util.UUID

import au.com.nicta.data.pipeline.core.messages.PipeCompleteMsg
import au.com.nicta.data.pipeline.core.models.{PipelineContext, ShellPipe}

/**
 * Created by tiantian on 22/07/15.
 */
object ShellEntry {

  import scala.sys.process._

  def launch(pipe: ShellPipe, taskId:String, execTag:String) ={

    if(pipe.script == null){
      val dependencyFiles = s"${PipeExecutionContext.DEPENDENCY_HOME}/app/${pipe.name}/${pipe.version}/${pipe.name}-${pipe.version}.sh"
      val process = Process("sh", Seq("-c", dependencyFiles))
      process.!!
    } else {
      val scripts = pipe.script.split("\\|")
      val processChain = scripts.map{ s=>
        Process("sh", Seq("-c", s))
      }.reduceLeft(_ #| _)
      processChain.!!
      //todo send call back in a channel process
      AkkaCallbackEntry.sendCallBack(pipe.pipelineServer, PipeCompleteMsg(pipe.name, pipe.version, taskId, "success", SimplePipeExecutor.getHexTimestamp()))
    }
  }

}
