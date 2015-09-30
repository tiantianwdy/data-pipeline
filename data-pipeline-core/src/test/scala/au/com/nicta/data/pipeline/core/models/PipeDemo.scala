package au.com.nicta.data.pipeline.core.models

import au.com.nicta.data.pipeline.core.examples.{SumReducer, WordCountMapper, WordCountSparkProc}
import au.com.nicta.data.pipeline.core.executor.ShellEntry
import org.junit.Test

import scala.collection.mutable


/**
  * Created by tiantian on 14/07/15.
  */
class PipeDemo {



   @Test
   def testConstructingSparkPipe(): Unit ={

     val pipe1 = new SparkPipe(name = "datapipeline-examples", version = "0.0.1",
       exec = new  WordCountSparkProc,
       inputPath =  mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/partial/rankings")
     )

     println(pipe1.appClass)
     val cls = Thread.currentThread().getContextClassLoader.loadClass(pipe1.appClass)
     println(cls)
//     val constrcutors = cls.getConstructors()
//     constrcutors.foreach(println(_))
//     val ins = cls.newInstance()
//     println(ins)
//     new DataPipelineContext("127.0.0.1:8999").test(pipe1)
 //    SparkPipeEntry.pipe = pipe1
 //    SparkPipeEntry.main(null)
   }


  @Test
  def testConstructingMRPipe(): Unit ={
    val pipe = MRPipe(name = "mr-pipe", version = "0.0.1",
      mapper = new WordCountMapper,
      reducer = new SumReducer,
      combiner = new SumReducer,
      inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
    )
    println(pipe.mapKType + " " + pipe.mapVType)
    println(pipe.outKType + " " + pipe.outVType)
    println(pipe.mapperClassName)
    println(pipe.reducerClassName)
    println(pipe.combinatorClassName)
//    MRPipeEntry.launch(pipe)
  }

  @Test
  def testBashPipe(): Unit ={
    val pipe = new ShellPipe(name = "shell-pipe", version = "0.0.1",
      script = "ps -ef | grep java")
    println(ShellEntry.launch(pipe,"taskid",  "exec-0"))
  }

  @Test
  def testBashInScala(): Unit ={
    import scala.sys.process._
//    println (Seq("hdfs", "dfs", "-rm", "-r", "/data-pipeline/").!!)
//    Process("ps -ef") #| Process("grep java")!
      val process = Process("sh", Seq("-c", "/home/tiantian/Dev/lib/data-pipeline/app/shell-pipe/shell-pipe.sh"))
      println(process.!!)
//      Seq("sh","-c", "echo password") #| Seq("sh","-c", "/home/tiantian/Dev/env/local/sbin/hdfs-start.sh","namenode").!!

//    val cmd = Array("sh", "-c", "/home/tiantian/Dev/env/local/sbin/hdfs-start.sh", "namenode")
//    Runtime.getRuntime.exec(cmd)
  }


 }



