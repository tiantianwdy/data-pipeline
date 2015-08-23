package au.com.nicta.data.pipeline.core.executor

import akka.actor.{Props, ActorSystem}
import au.com.nicta.data.pipeline.core.messages.{QueryExecutionHistory, QueryPipeHistory, PipeCompleteMsg}
import com.typesafe.config.ConfigFactory
import org.junit.Test

/**
 * Created by tiantian on 29/07/15.
 */
class CallbackEntryTest {

  @Test
  def testConfigLoading(): Unit ={
    val conf = ConfigFactory.parseString(s"""akka.remote.netty.tcp.port="15001" """).withFallback(ConfigFactory.load())
    println(conf.getInt("akka.remote.netty.tcp.port"))
  }

  @Test
  def testSendCallBackMsg(): Unit ={
    val serverAddr = "akka.tcp://pipeline-master@127.0.1.1:8999/user/pipeline-server"
    val res = AkkaCallbackEntry.sendCallBack(serverAddr, new PipeCompleteMsg("SparkPipe", "0.0.1", "123456", "success", SimplePipeExecutor.getHexTimestamp()))
    println(res)
  }

  @Test
  def testSendQueryMsg(): Unit ={
    val serverAddr = "akka.tcp://pipeline-master@127.0.1.1:8999/user/pipeline-server"
//    val res = AkkaCallbackEntry.sendCallBack(serverAddr, QueryPipeHistory("analysisSpark", "0.0.1"))
    val res = AkkaCallbackEntry.sendCallBack(serverAddr, QueryExecutionHistory("14f4db0b92f"))
    println(res)
  }

}

