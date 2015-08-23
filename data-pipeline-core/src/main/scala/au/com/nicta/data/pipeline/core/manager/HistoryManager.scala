package au.com.nicta.data.pipeline.core.manager

import java.util.concurrent.{CopyOnWriteArrayList, ConcurrentHashMap}
import scala.collection.JavaConversions._

/**
 * Created by tiantian on 20/08/15.
 */
class HistoryManager {

  private val pipelineHistoryMap = new ConcurrentHashMap[String, ConcurrentHashMap[String, CopyOnWriteArrayList[ExecutionTrace]]]()

  private val executionHistory = new ConcurrentHashMap[String, CopyOnWriteArrayList[ExecutionTrace]]

  def addTrace(eTrace: ExecutionTrace): Unit ={
    val historyMap =  pipelineHistoryMap.getOrElseUpdate(eTrace.pipeName, new ConcurrentHashMap[String, CopyOnWriteArrayList[ExecutionTrace]])
    val historySeq =  historyMap.getOrElseUpdate(eTrace.version, new CopyOnWriteArrayList[ExecutionTrace])
    historySeq.add(eTrace)

    executionHistory.getOrElseUpdate(eTrace.execTag, new CopyOnWriteArrayList[ExecutionTrace])
    .add(eTrace)
  }

  def getPipeTrace(pipeName:String, version:String):Seq[ExecutionTrace] = {
    if(pipelineHistoryMap.contains(pipeName) &&
      pipelineHistoryMap.get(pipeName).contains(version)){
      pipelineHistoryMap.get(pipeName).get(version).toSeq
    } else Seq.empty[ExecutionTrace]
  }

  def getPipeTrace(pipeName:String):Map[String,Seq[ExecutionTrace]] = ???


  def getExecutionTrace(execTag:String):Seq[ExecutionTrace] = {
    if(executionHistory.contains(execTag))
      executionHistory.get(execTag)
    else Seq.empty[ExecutionTrace]
  }

}

object HistoryManager{

  lazy val historyManager = new HistoryManager()

  def apply():HistoryManager = {
    historyManager
  }

}

case class PipeTrace(pipeName:String,
                     version:String,
                     execTag:String,
                     outputPath:String) extends Serializable

case class ExecutionTrace(pipeName:String,
                          version:String,
                          pipeClass:String,
                          execTag:String,
                          taskId:String,
                          outputPath:String,
                          status:String) extends Serializable