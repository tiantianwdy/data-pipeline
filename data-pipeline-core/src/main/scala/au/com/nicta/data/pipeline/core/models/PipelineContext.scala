package au.com.nicta.data.pipeline.core.models

import java.io.{InputStreamReader, BufferedReader}
import java.util.concurrent.atomic.AtomicBoolean

import au.com.nicta.data.pipeline.core.executor._

import scala.concurrent.Lock


/**
 * Created by tiantian on 15/07/15.
 */
object PipelineContext {
  


  val isInit = new AtomicBoolean(false)
  val initLock = new Lock

  def initLocalServer(): Unit ={
    // for testing init a local server
    if(!isInit.get()) {
      new Thread{

        override def run(): Unit = {
          SimplePipelineServer.start(0)
          isInit.set(true)
        }
      }.start()
    }
    while(!isInit.get()) Thread.sleep(100)
  }

  def test(pipe:Pipe[_,_]) = {
    initLocalServer()
    SimplePipeExecutor.execute(pipe, ExecutionOption.Test)
  }

  def test(pipes:Seq[Pipe[_,_]]):Unit = {
    initLocalServer()
    SimplePipeExecutor.execute(pipes, ExecutionOption.Test, SimplePipeExecutor.getHexTimestamp())
  }

  def pseudoRun(pipe:Pipe[_,_]) = {
    initLocalServer()
    SimplePipeExecutor.execute(pipe, ExecutionOption.PseudoRun)
  }

  def pseudoRun(pipes:Seq[Pipe[_,_]]):Unit = {
    initLocalServer()
    SimplePipeExecutor.execute(pipes, ExecutionOption.PseudoRun, SimplePipeExecutor.getHexTimestamp())
  }

  def exec(pipe:Pipe[_,_]) = {
    //todo submit to a remote server
    SimplePipeExecutor.execute(pipe, ExecutionOption.Run)
  }

  def exec(pipes:Seq[Pipe[_,_]]) = {
    SimplePipeExecutor.execute(pipes, ExecutionOption.Run, SimplePipeExecutor.getHexTimestamp())
  }

  def explain(pipe:Pipe[_,_]) = {
    SimplePipeExecutor.explain(pipe)
  }

}

