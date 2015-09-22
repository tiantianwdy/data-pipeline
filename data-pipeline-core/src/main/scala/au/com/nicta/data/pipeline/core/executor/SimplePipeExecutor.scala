package au.com.nicta.data.pipeline.core.executor

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{ConcurrentHashMap, Executors}

import au.com.nicta.data.pipeline.core.executor.ExecutionOption.{PseudoRun, Test, Run}
import au.com.nicta.data.pipeline.core.manager.{ExecutionTrace, HistoryManager}
import au.com.nicta.data.pipeline.core.messages.{PipeCompleteMsg, PipelineMsg}
import au.com.nicta.data.pipeline.core.models.Pipe
import au.com.nicta.data.pipeline.io.NameService

import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * A simple executor for scheduling and execution of pipelines
 * Created by tiantian on 22/07/15.
 */
object SimplePipeExecutor {

  val executor = Executors.newFixedThreadPool(4)
  implicit val executionContext = ExecutionContext.fromExecutor(executor)

  val  promiseMap:java.util.Map[String, Promise[PipelineMsg]] = new ConcurrentHashMap[String, Promise[PipelineMsg]]()

  def getHexTimestamp():String = {
    val hexTag = java.lang.Long.toHexString(System.currentTimeMillis())
    hexTag
  }

  def getPipeId(pipeName:String, version:String, instanceTag:String)={
    s"${pipeName}-${version}-${instanceTag}"
  }


  def checkPromiseSync(id:String): Boolean = synchronized{
    promiseMap.containsKey(id)
  }
  /**
   * run a single pipe task
   * @param pipe
   * @param option
   * @return
   */
  def run(pipe: Pipe[_, _], option: ExecutionOption = Run, executionId: String): Future[PipelineMsg] = {
    println(s"Running Task:${pipe}")

    val id = getPipeId(pipe.name, pipe.version, executionId)
    val outputPath = NameService.getHDFSPath(pipe.name, pipe.version, executionId)
    val timestamp = System.currentTimeMillis()

    HistoryManager().addExecTrace(ExecutionTrace(id,
      pipe.name,
      pipe.version,
      pipe.getClass.toString,
      executionId,
      pipe.inputPath,
      outputPath,
      timestamp,
      timestamp,
      "Running"))

    val task = PipeTask(id, pipe, executionId)
    //    val promise =
    val promise = if (promiseMap.containsKey(id)) {
      promiseMap.get(id)
    } else {
      val newP = Promise[PipelineMsg]
      promiseMap.put(task.id, newP)
      newP
    }
    Future {
      option match {
        case Test => 
          task.test()
        case Run =>
          task.run()
        case PseudoRun =>
          task.sudoRun()
      }
    }
    promise.future
  }

  /**
   * finalize a task and trigger related promise
   * @param msg
   * @return
   */
  def taskCompleted(msg:PipeCompleteMsg): Boolean = {
    println(s"Pipe Task:${msg.taskId} completed successfully.")
    val outputPath = NameService.getHDFSPath(msg.name, msg.version, msg.executionTag)
    val timestamp = System.currentTimeMillis()
    val traces = HistoryManager().getExecutionTraces(msg.executionTag).filter(_.taskId == msg.taskId)
    val trace = if(traces nonEmpty) {
      traces.head.copy(endTime = timestamp, status = msg.status)
    } else {
      ExecutionTrace(msg.taskId,
        msg.name,
        msg.version,
        msg.getClass.toString,
        msg.executionTag,
        Seq.empty[String],
        outputPath,
        timestamp,
        timestamp,
        msg.status)
    }

    HistoryManager().addExecTrace(trace)

    val promise = promiseMap.remove(msg.taskId)
    if(promise ne null){
      if(!promise.isCompleted)
        promise.success(msg)
      true
    } else false
  }

  /**
   * execute a pipeline which may contain multiple pipes
   * @param pipe
   * @param option
   * @return
   */
  def execute(pipe:Pipe[_,_], option:ExecutionOption = Run, executionId:String = getHexTimestamp()):Future[_] = {

    val promiseId = getPipeId(pipe.name, pipe.version, executionId)
    val future = if(promiseMap.containsKey(promiseId)) {
      println(s"Reuse the previous promise: $promiseId")
      promiseMap.get(promiseId).future
    } else if(pipe.parents.isEmpty){
      val promise = Promise[PipelineMsg]
      promiseMap.put(promiseId, promise)
      run(pipe, option, executionId)
    } else {
      val promise = Promise[PipelineMsg]
      val preSteps = execute(pipe.parents, option, executionId)
      promiseMap.put(promiseId, promise)

      preSteps andThen {
        case Success(arr) =>
          run(pipe, option, executionId)
        case Failure(e) =>
          promise.failure(e)
      }
      promise.future
    }
    future
  }


  def execute(pipes:Seq[Pipe[_,_]], option:ExecutionOption, executionId:String ):Future[_] = {
    val preFutures = pipes.map{ p =>
      execute(p, option, executionId)
    }
    Future.sequence(preFutures)
  }

  /**
   * explain and print out the execution plan of a pipeline.
   * @param pipe
   */
  def explain(pipe:Pipe[_,_]):Unit = {
    if(pipe.parents.isEmpty){

      println(pipe.toString)
    } else {
      pipe.parents.foreach{ p =>
        explain(p)
      }
      println("=======================")
      println(pipe)
    }
  }

}
