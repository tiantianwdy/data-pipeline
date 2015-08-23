package au.com.nicta.data.pipeline.core.executor

import java.io.{BufferedReader, InputStreamReader}

import org.junit.Test

/**
 * Created by tiantian on 19/07/15.
 */
class SparkPipeEntryTest {

  @Test
  def testLaunchPipe(){

    SparkPipeEntry.launch("datapipeline-examples",
      "0.0.1",
      "au.com.nicta.data.pipeline.examples.WordCountSparkPipe",
      "akka.tcp://pipeline-master@127.0.1.1:8999/pipeline-server",
      "spark://tiantian-HP-EliteBook-Folio-9470m:7077",
      Seq("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings"),
      "taskId", "exec-0")


  }

  @Test
  def testPipeExecMain(): Unit ={
    SparkPipeEntry.main(Array("datapipeline-examples",
      "0.0.1",
      "au.com.nicta.data.pipeline.examples.WordCountSparkPipe",
      "akka.tcp://pipeline-master@127.0.1.1:8999/pipeline-server",
      "spark://tiantian-HP-EliteBook-Folio-9470m:7077",
      "hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings",
      "taskId", "exec-0"))
  }

}

object SparkPipeEntryTest extends App {

  val process = SparkPipeEntry.launch("datapipeline-examples",
    "0.0.1",
    "au.com.nicta.data.pipeline.examples.WordCountSparkPipe",
    "akka.tcp://pipeline-master@127.0.1.1:8999/pipeline-server",
    "spark://tiantian-HP-EliteBook-Folio-9470m:7077",
    Seq("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings"),
    "taskId", "exec-0")

  val in = new BufferedReader(new InputStreamReader(process.getErrorStream))
  var line:String = in.readLine()
  while(line!= null){
    println(line)
    line = in.readLine()
  }

  val status = process.waitFor()
  println(s"process finished in state of ${if(status == 1) "Success" else "Failed"} ..")

}
