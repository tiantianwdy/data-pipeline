package au.com.nicta.data.pipeline.core.executor

import java.io.File
import java.net.URLClassLoader
import java.util.{Properties, UUID}

import au.com.nicta.data.pipeline.core.io.SparkDataAdapter
import au.com.nicta.data.pipeline.core.manager.DependencyManager
import au.com.nicta.data.pipeline.core.messages.PipeCompleteMsg
import au.com.nicta.data.pipeline.core.models.{PipelineContext, SparkPipe, SparkProc}
import au.com.nicta.data.pipeline.io.{IOContext, NameService}
import org.apache.hadoop.conf.Configuration
import org.apache.spark.launcher.SparkLauncher
import org.apache.spark.rdd.RDD
import org.apache.spark.scheduler.InputFormatInfo
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

/**
 * Created by tiantian on 13/07/15.
 */
object SparkPipeEntry extends Serializable{

  val SPARK_HOME = "/home/tiantian/Dev/lib/spark-1.4.1-bin-hadoop2.6"
  val SPARK_ENTRY_CLASS = "au.com.nicta.data.pipeline.core.executor.SparkPipeEntry"


  def launch(pipe:SparkPipe[_,_], taskId:String, executionTag:String): Process = launch(pipe.name,
    pipe.version, pipe.app.getName, pipe.pipelineServer, pipe.executionContext, pipe.inputAddress(executionTag), taskId, executionTag)

  /**
   *  launch spark app driver as a sub-process to execute the spark pipe
   *  called when a Spark pipe is scheduled
   *
   */
  def launch(name:String, version:String, pipeClass:String, pipelineServer:String, master:String, inputPath:Seq[String], taskId:String, exceTag:String): Process ={
    //run spark job
//    val mainJar = s"${PipelineContext.DEPENDENCY_HOME}/dep/${name}/${version}/data-pipeline-core-0.0.1.jar"
//    val dependencyFiles = new File(s"${PipelineContext.DEPENDENCY_HOME}/app/${name}/${version}")

//    val dependencyJars = if(dependencyFiles.isDirectory) {
//      dependencyFiles.listFiles()
//        .map(_.getAbsolutePath)
//        .filter(_.endsWith(".jar"))
//    } else null

    val mainJar = DependencyManager().getSharedDep()+"/data-pipeline-core-0.0.1.jar"
    val appJar = DependencyManager().getAppPath(name, version)
    val dependencyFiles = new File(DependencyManager().getDepPath(name, version))

    val dependencyJars = if (dependencyFiles.isDirectory) {
      dependencyFiles.listFiles()
        .map(_.getAbsolutePath)
        .filter(_.endsWith(".jar"))
    } else null

//        Runtime.getRuntime.exec(s"$SPARK_HOME/bin/spark-submit --class $cls --master spark://$master --num-executors 4 --executor-memory 6G $dependencyJar")
    val launcher = new SparkLauncher()
    launcher.setSparkHome(SPARK_HOME)
      .setAppResource(mainJar)
      .setMainClass(SPARK_ENTRY_CLASS)
      .setMaster(master)
      .addAppArgs(name, version, pipeClass, pipelineServer, master, inputPath.mkString(";"), taskId, exceTag)

    if(appJar != null){
      launcher.addJar(appJar)
    }
    if(dependencyJars != null && dependencyJars.size > 0){
      dependencyJars.foreach(launcher.addJar(_))
    }

    launcher.launch()
  }

  /**
   * the generic execution logic of a managed spark pipe
   * @param pipe
   * @tparam I
   * @tparam O
   */
  def exec[I, O, T >: SparkProc[I,O]](pipe:SparkPipe[I, O]): Unit = {

    if (pipe eq null) System.exit(1)
    val start = System.currentTimeMillis()
    val inputPath = IOContext.getServerAddress(pipe.inputFormat)
    val sparkConf = new SparkConf().setAppName(pipe.name).setMaster(pipe.executionContext)
    val conf = new Configuration()
//    val sc = new SparkContext(sparkConf,
//      InputFormatInfo.computePreferredLocations(
//        Seq(new InputFormatInfo(conf, classOf[org.apache.hadoop.mapred.TextInputFormat], pipe.inputPath.head))
//      ))
    val sc = new SparkContext(sparkConf)
    val dataAdapter = new SparkDataAdapter(sc)
    val data = pipe.inputPath.map{ in =>
      dataAdapter.read(pipe.inputFormat, in)
    }

    val sparkApp = pipe.exec.asInstanceOf[SparkProc[I,O]]
    val rdds = mutable.Buffer.empty[RDD[I]]
    val dfs = mutable.Buffer.empty[DataFrame]
    data.foreach{ d => d match {
      case Left(rdd) => rdds += rdd.asInstanceOf[RDD[I]]
      case Right(df) => dfs += df
     }
    }
    val output = if(rdds.nonEmpty) sparkApp.process(Left(rdds), sc)
    else sparkApp.process(Right(dfs), sc)

    dataAdapter.write(pipe.outputFormat, pipe.outputPath, output)


    sc.stop()

  }


  /**
   *  the main entrance for execution of a spark job
   *  this is the  entrance class that loaded by the launcher of pipeline server to execute a Spark pipe
   * @param args
   */
  def main (args: Array[String]) = try {
    if (args.length < 8) System.exit(1)

    val start = System.currentTimeMillis()
    val pipeName = args(0)
    val version = args(1)

//    val appJar = new File(s"${PipelineContext.DEPENDENCY_HOME}/app/${pipeName}/${version}/${pipeName}-${version}.jar")
//    val dependencyFiles = new File(s"${PipelineContext.DEPENDENCY_HOME}/dep/${pipeName}/${version}").listFiles()
    val appJar = new File(DependencyManager().getAppPath(pipeName, version))
    val dependencyFiles = DependencyManager().getAllDepFiles(pipeName, version)
    val jarURls = if(dependencyFiles != null && dependencyFiles.nonEmpty){
      dependencyFiles.map(_.toURL)
    } else {
      Array.empty[java.net.URL]
    } :+ appJar.toURL

    val parentLoader = Thread.currentThread().getContextClassLoader
    val classLoader = new URLClassLoader(jarURls, parentLoader)
    val appCls = classLoader.loadClass(args(2))
    val appIns =  appCls.getConstructor().newInstance().asInstanceOf[SparkProc[_,_]]
    val pipelineServer = args(3)
    val executionContext = args(4)
    val inputPath = args(5).split(";").toBuffer
    val taskId = args(6)
    val executionTag = args(7)

    val inputFormat = "text"
    val outputPath = NameService.getHDFSPath(pipeName, version, executionTag)
    val outputFormat = "text"


    val pipe = new SparkPipe(pipeName, appIns, version,
      pipelineServer, executionContext,  inputPath, inputFormat, outputPath, outputFormat)
    pipe.outputPath = outputPath
    println(pipe.toString)
    exec(pipe)
    // send call back to pipe.pipeServer
    AkkaCallbackEntry.sendCallBack(pipelineServer, PipeCompleteMsg(pipeName, version, taskId, "success", executionTag))
    System.exit(0)

  } catch {
    case ex:Throwable =>
      ex.printStackTrace()
//      AkkaCallbackEntry.sendCallBack(pipelineServer, PipeCompleteMsg(pipeName, version, taskId, "Failed", executionTag))
      System.exit(0)
  }

}
