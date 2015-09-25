package au.com.nicta.data.pipeline.core.messages

import au.com.nicta.data.pipeline.core.manager.{PipeProvenance, ExecutionTrace}
import au.com.nicta.data.pipeline.core.models.Pipe

/**
 * Created by tiantian on 27/07/15.
 */
trait PipelineMsg extends Serializable



case class PipeCompleteMsg(name:String, version:String, taskId:String, status:String, executionTag:String) extends PipelineMsg

case class CompleteRevMsg(name:String, version:String, taskId:String) extends PipelineMsg

case class PipeSubmitMsg(name:String, version:String, depBytes:Array[Byte]) extends PipelineMsg

case class SubmitRevMsg(name:String, version:String) extends PipelineMsg

case class PipelineJobMsg(piplineName:String, pipeDag:Seq[Pipe[_,_]]) extends PipelineMsg

case class JobRevMsg(piplineName:String, exeId:String, status:String) extends PipelineMsg

case class PipeFailureMsg(cause:Throwable) extends PipelineMsg


case class QueryPipeHistory(name:String, version:String) extends PipelineMsg

case class QueryPipeHistoryResp(name:String, version:String, results:Seq[(String, PipeProvenance)]) extends PipelineMsg

case class QueryExecutionHistory(execTag:String) extends PipelineMsg

case class QueryExecutionHistoryResp(execTag:String, results:Seq[ExecutionTrace]) extends PipelineMsg

case class QueryPipelineHistory(pipelineName:String, executionTag:String) extends PipelineMsg

case class QueryPipelineHistoryResp(pipelineName:String, executionTag:String, results:Seq[(String, Seq[String])]) extends PipelineMsg

case class QueryPipeList() extends PipelineMsg

case class QueryPipeListResp(results:Seq[String]) extends PipelineMsg

case class QueryPipelineList() extends PipelineMsg

case class QueryPipelineListResp(results:Seq[(String, Seq[String])]) extends PipelineMsg

