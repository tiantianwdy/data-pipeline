package au.com.nicta.data.pipeline.core

import au.com.nicta.data.pipeline.core.executor.SimplePipeExecutor
import au.com.nicta.data.pipeline.core.manager.HistoryManager
import au.com.nicta.data.pipeline.core.messages._

/**
 * Created by tiantian on 27/07/15.
 */
trait PipelineServerBackend {

  def pipeCompleted(msg:PipeCompleteMsg)

  def pipeSubmit(msg:PipeSubmitMsg)

  def getExecutionHistory(msg:QueryExecutionHistory): PipelineMsg

  def getPipeHistory(msg:QueryPipeHistory): PipelineMsg

}

class PipelineServerBackendImpl extends PipelineServerBackend {

  override def pipeCompleted(msg: PipeCompleteMsg): Unit = {
    SimplePipeExecutor.taskCompleted(msg)
  }

  override def pipeSubmit(msg: PipeSubmitMsg): Unit = {

  }

  override def getExecutionHistory(msg: QueryExecutionHistory): PipelineMsg = {
    val res = HistoryManager().getExecutionTrace(msg.execTag)
    QueryExecutionHistoryResp(msg.execTag, res)
  }

  override def getPipeHistory(msg: QueryPipeHistory): PipelineMsg = {
    require(msg.name ne null)
    val res = if(msg.version != null && msg.version.nonEmpty)
    {
      HistoryManager().getPipeTrace(msg.name, msg.version)
    } else {
      HistoryManager().getPipeTrace(msg.name).toSeq.flatMap(_._2)
    }
    QueryPipeHistoryResp(msg.name, msg.version, res)
  }
}
