package au.com.nicta.data.pipeline.core.executor

import java.util.concurrent.{TimeUnit, ConcurrentHashMap}

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

import akka.actor.{ActorSystem, ActorContext}
import akka.pattern._
import akka.util.Timeout
import au.com.nicta.data.pipeline.core.messages.{PipeFailureMsg, CompleteRevMsg, PipelineMsg}

import scala.concurrent.{Await, Promise}
import scala.util.Random

/**
 * Created by tiantian on 27/07/15.
 */
trait CallbackEntry {

  def sendCallBack(server:String, msg:PipelineMsg):PipelineMsg

}


object AkkaCallbackEntry extends CallbackEntry {

  implicit val timeout = Timeout(5 seconds)
  implicit val maxWaitResponseTime = Duration(20, TimeUnit.SECONDS)


  val port:Int = (10001 + Random.nextFloat()*5000).toInt

  lazy val config = ConfigFactory.parseString(s"""akka.remote.netty.tcp.port = "$port" """).withFallback(ConfigFactory.load())


  lazy val actorContext:ActorSystem = ActorSystem("pipeline-client", config)

  val callBackMap:java.util.Map[String, Promise[_]] = new ConcurrentHashMap[String, Promise[_]]()


  override def sendCallBack(server: String, msg:PipelineMsg): PipelineMsg = {
    val rev = actorContext.actorSelection(server) ? msg
    val res = try {
      Await.result(rev, maxWaitResponseTime) match {
        case msg:PipelineMsg => msg
        case x => PipeFailureMsg(new RuntimeException(s"Unknown msg $x"))
      }
    } catch {
      case t =>
        System.err.println(t.getMessage)
        PipeFailureMsg(t)
    }
//    Runtime.getRuntime.addShutdownHook(new Threads)
//    actorContext.shutdown()
    res
  }

}
