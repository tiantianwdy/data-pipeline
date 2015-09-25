package au.com.nicta.data.pipeline.core.server

import au.com.nicta.data.pipeline.core.executor.SimplePipeExecutor
import au.com.nicta.data.pipeline.core.manager.{DependencyManager, HistoryManager}
import au.com.nicta.data.pipeline.core.messages._
import au.com.nicta.data.pipeline.core.models.PipelineContext

/**
 * Created by tiantian on 27/07/15.
 */
trait PipelineServerBackend {

  def pipeCompleted(msg:PipeCompleteMsg)

  def pipeDepSubmit(msg:PipeSubmitMsg): PipelineMsg

  def pipelineJobSubmit(msg:PipelineJobMsg): PipelineMsg

  def getExecutionHistory(msg:QueryExecutionHistory): PipelineMsg

  def getPipelineHistory(msg:QueryPipelineHistory):PipelineMsg

  def getPipeHistory(msg:QueryPipeHistory): PipelineMsg

  def getAllPipelines():PipelineMsg

  def getAllPipes():PipelineMsg

}

class PipelineServerBackendImpl extends PipelineServerBackend {

  override def pipeCompleted(msg: PipeCompleteMsg): Unit = {
    SimplePipeExecutor.taskCompleted(msg)
  }

  override def pipeDepSubmit(msg: PipeSubmitMsg): PipelineMsg = {
    DependencyManager().submit(msg.name, msg.version, msg.depBytes)
    SubmitRevMsg(msg.name, msg.version)
  }


  override def pipelineJobSubmit(msg: PipelineJobMsg): PipelineMsg = {
    val exeId = PipelineContext.exec(msg.piplineName, msg.pipeDag, "localhost")
    JobRevMsg(msg.piplineName, exeId, "Running")
  }

  override def getExecutionHistory(msg: QueryExecutionHistory): PipelineMsg = {
    val res = HistoryManager().getExecutionTraces(msg.execTag)
    QueryExecutionHistoryResp(msg.execTag, res)
  }

  override def getPipeHistory(msg: QueryPipeHistory): PipelineMsg = {
    require(msg.name ne null)
    val res = if(msg.version != null && msg.version.nonEmpty) {
      Seq(msg.version -> HistoryManager.getPipeProvenance(msg.name, msg.version))
    } else {
      HistoryManager.getPipeProvenance(msg.name).toSeq
    }
    QueryPipeHistoryResp(msg.name, msg.version, res)
  }

  override def getPipelineHistory(msg: QueryPipelineHistory): PipelineMsg = {
    val res = HistoryManager.getPipelineProvenance(msg.pipelineName)
    QueryPipelineHistoryResp(msg.pipelineName, msg.executionTag, res)

  }

  override def getAllPipelines(): PipelineMsg = {
    val res = HistoryManager.getPipelineList()
    QueryPipelineListResp(res)
  }

  override def getAllPipes(): PipelineMsg = {
    val res = HistoryManager.getPipeList()
    QueryPipeListResp(res)
  }
}
