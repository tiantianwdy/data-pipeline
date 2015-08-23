package au.com.nicta.data.pipeline.core.messages

import au.com.nicta.data.pipeline.core.manager.ExecutionTrace

/**
 * Created by tiantian on 27/07/15.
 */
trait PipelineMsg {

}


case class PipeCompleteMsg(name:String, version:String, taskId:String, status:String, executionTag:String) extends PipelineMsg

case class CompleteRevMsg(name:String, version:String, taskId:String) extends PipelineMsg

case class PipeSubmitMsg(name:String, version:String, dependency:String) extends PipelineMsg

case class SubmitRevMsg(name:String, version:String) extends PipelineMsg

case class QueryPipeHistory(name:String, version:String) extends PipelineMsg

case class QueryPipeHistoryResp(name:String, version:String, results:Seq[ExecutionTrace]) extends PipelineMsg

case class QueryExecutionHistory(execTag:String) extends PipelineMsg

case class QueryExecutionHistoryResp(execTag:String, results:Seq[ExecutionTrace]) extends PipelineMsg

case class PipeFailureMsg(cause:Throwable) extends PipelineMsg