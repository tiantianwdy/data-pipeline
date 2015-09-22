package au.com.nicta.data.pipeline.core.manager

import java.util.concurrent.{CopyOnWriteArrayList, ConcurrentHashMap}
import scala.collection.JavaConversions._

/**
 * Created by tiantian on 20/08/15.
 */
class HistoryManager {

  /**
   * ${pipeName}#{$version} -> taskId(executionTrace id)
   */
  private val pipeExecutionHistory = new ConcurrentHashMap[String, CopyOnWriteArrayList[String]]()

  /**
   * executionTag -> taskId(executionTrace id)
   */
  private val executionHistory = new ConcurrentHashMap[String, CopyOnWriteArrayList[String]]

  /**
   * pipelineName -> Seq[executionTag]
   */
  private val pipelineExecutionInstances = new ConcurrentHashMap[String, CopyOnWriteArrayList[String]]

  /**
   * taskId -> executionTrace
   */
  private val executionTraceMap = new ConcurrentHashMap[String, ExecutionTrace]()

  /**
   * ${pipeName}#{$version} -> PipeTrace
   */
  private val pipeHistoryMap = new ConcurrentHashMap[String, PipeTrace]()


  
  def pipeId(name:String, version:String) = {
    s"$name#$version"
  }

  def addExecTrace(eTrace: ExecutionTrace): Unit ={
    //update execution history of pipes
    val historySeq =  pipeExecutionHistory.getOrElseUpdate(pipeId(eTrace.pipeName, eTrace.version), new CopyOnWriteArrayList[String])
    historySeq.add(eTrace.taskId)
    // save execution trace in execution history  
    executionHistory.getOrElseUpdate(eTrace.execTag, new CopyOnWriteArrayList[String])
    executionTraceMap.put(eTrace.taskId, eTrace)
  }
  
  def addPipeTrace(pTrace:PipeTrace): Unit = {
    val id = pipeId(pTrace.pipeName, pTrace.version)
    pipeHistoryMap.put(id, pTrace)
  }

  def addUpdatePipe(pTrace:PipeTrace): Unit = {
    val id = pipeId(pTrace.pipeName, pTrace.version)
    val updated = if(pipeHistoryMap.contains(id)){
      val old = pipeHistoryMap.get(id)
      val depSeq = (old.dep ++ pTrace.dep).toSet[String]
      old.copy(lastUpdatedTime = pTrace.lastUpdatedTime, dep = depSeq.toSeq)
    } else pTrace
    pipeHistoryMap.put(id, updated)
  }

  def addPipelineInstance(pipelineName:String, executionTag:String) = {
    val instanceMap = pipelineExecutionInstances.getOrElseUpdate(pipelineName, new CopyOnWriteArrayList[String])
    instanceMap.add(executionTag)
  }

  def getPipeExecTrace(pipeName:String, version:String):Seq[ExecutionTrace] = {
    val id = pipeId(pipeName, version)
    if(pipeExecutionHistory.contains(id)) {
      pipeExecutionHistory.get(id).map{
        tid => executionTraceMap.get(tid)
      }
    } else Seq.empty[ExecutionTrace]
  }

  def getPipeTrace(pipeName:String, version:String):PipeTrace = {
    val id = pipeId(pipeName, version)
    pipeHistoryMap.get(id)
  }
  
  def getPipeTrace(pipeName:String):Seq[PipeTrace] = {
    val ids = pipeHistoryMap.keySet().filter(_.startsWith(pipeName+"#")).toSeq
    ids.map(id => pipeHistoryMap.get(id))
  }


  def getExecutionTrace(taskId:String):ExecutionTrace = {
    executionTraceMap.get(taskId)  
  }
  
  def getExecutionTraces(execTag:String):Seq[ExecutionTrace] = {
    if(executionHistory.contains(execTag)){
      val ids  = executionHistory.get(execTag)
      ids.map(id => executionTraceMap.get(id))
    }
    else Seq.empty[ExecutionTrace]
  }

  def getPipelineExecutions(pipelineName:String) = {
    pipelineExecutionInstances.get(pipelineName)
  }

}

object HistoryManager{

  lazy val historyManager = new HistoryManager()

  def apply():HistoryManager = {
    historyManager
  }


  /**
   *
   * @param pipelineName
   * @return
   */
  def getPipelineProvenance(pipelineName:String):Seq[(String,Seq[String])] = {
    historyManager.getPipelineExecutions(pipelineName).map { exeId =>
      val traceIds = historyManager.getExecutionTraces(exeId).map(t => historyManager.pipeId(t.pipeName, t.version))
      exeId -> (traceIds)
    }
  }

  /**
   *
   * @param pipeName
   */
  def getPipeProvenance(pipeName:String) = Seq[(String, PipeProvenance)] = {
    historyManager.getPipeTrace(pipeName).map{t =>
      val instances = historyManager.getPipeExecTrace(t.pipeName, t.version)
      t.version -> PipeProvenance(t.pipeName, t.version, t.author, t.dep, t.creatTime, t.lastUpdatedTime, instances)
    }
  }

  /**
   *
   * @param pipeName
   * @param version
   * @return
   */
  def getPipeProvenance(pipeName:String, version:String): PipeProvenance = {
    val t = historyManager.getPipeTrace(pipeName, version)
    val instances = historyManager.getPipeExecTrace(t.pipeName, t.version)
    PipeProvenance(t.pipeName, t.version, t.author, t.dep, t.creatTime, t.lastUpdatedTime, instances)
  }


}

case class PipeTrace(pipeName:String,
                     version:String,
                     author:String,
                     dep:Seq[String],
                     creatTime:Long,
                     lastUpdatedTime:Long) extends Serializable


case class ExecutionTrace(taskId:String,
                          pipeName:String,
                          version:String,
                          pipeClass:String,
                          execTag:String,
                          inputPath:Seq[String],
                          outputPath:String,
                          startTime:Long,
                          endTime:Long,
                          status:String) extends Serializable

case class PipelineTrace(pipelineName:String,
                         instances:Map[String, PipelineDAG]
                          ) extends Serializable

case class PipelineDAG(nodes:Seq[String], links:Seq[String]) extends Serializable



case class PipeProvenance(pipeName:String,
                          version:String,
                          author:String,
                          dep:Seq[String],
                          creatTime:Long,
                          lastUpdatedTime:Long,
                          instances:Seq[ExecutionTrace]) extends Serializable