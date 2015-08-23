package au.com.nicta.data.pipeline.core.executor

import au.com.nicta.data.pipeline.core.messages.PipeCompleteMsg
import au.com.nicta.data.pipeline.core.models.Pipe

/**
 * Created by tiantian on 22/07/15.
 */
case class PipeTask(id:String, pipe:Pipe[_,_], execTag:String) extends Runnable{

  override def run(): Unit = {
    PipeExecutionContext.launch(pipe, id, execTag)
  }


  def test(): Unit = {
    PipeExecutionContext.test(pipe, id, execTag)
  }

  def sudoRun(): Unit ={
    AkkaCallbackEntry.sendCallBack(pipe.pipelineServer, PipeCompleteMsg(pipe.name, pipe.version, id, "success", SimplePipeExecutor.getHexTimestamp()))
  }
}
