package au.com.nicta.data.pipeline.core.executor

import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import au.com.nicta.data.pipeline.core.{PipelineServerBackendImpl, PipelineServerBackend}
import au.com.nicta.data.pipeline.core.messages._
import com.typesafe.config.ConfigFactory

/**
 * Created by tiantian on 18/07/15.
 */
class SimplePipelineServer(val backend:PipelineServerBackend = new PipelineServerBackendImpl()) extends Actor with ActorLogging{


  override def receive: Receive = {
    case pMsg:PipelineMsg => handlePipelineMsg(pMsg)
    case x => log.error(s"Unknown msg $x")
  }


  def handlePipelineMsg(msg:PipelineMsg): Unit = msg match {
    case m:PipeCompleteMsg =>
      backend.pipeCompleted(m)
      sender() ! CompleteRevMsg(m.name, m.version, null)
    case m:PipeSubmitMsg =>
      backend.pipeSubmit(m)
      sender() ! SubmitRevMsg(m.name, m.version)
    case msg:QueryPipeHistory =>
      sender() ! backend.getPipeHistory(msg)
    case msg:QueryExecutionHistory =>
      sender() ! backend.getExecutionHistory(msg)
    case x => log.error(s"Unknown msg $x")
  }
}


object SimplePipelineServer {


  def start(port:Int): Unit ={
    val config = if (port > 0) ConfigFactory.parseString(s"""akka.remote.netty.tcp.port="${port}" """).withFallback(ConfigFactory.load())
    else ConfigFactory.load()

    val system = ActorSystem("pipeline-master", config)
    val server = system.actorOf(Props(new SimplePipelineServer()),"pipeline-server")
    println(server.path.toString)
  }
}