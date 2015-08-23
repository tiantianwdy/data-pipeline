package au.com.nicta.data.pipeline.core.executor

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

/**
 * Created by tiantian on 29/07/15.
 */
object CallbackServerMain  extends App{
  val port = 8999
  val config = if (port > 0) ConfigFactory.parseString(s"""akka.remote.netty.tcp.port="${port}" """).withFallback(ConfigFactory.load())
  else ConfigFactory.load()

  val system = ActorSystem("pipeline-master", config)
  val server = system.actorOf(Props(new SimplePipelineServer()),"pipeline-server")
  println(server.path.toString)
}
