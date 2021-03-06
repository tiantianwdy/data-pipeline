package au.com.nicta.data.pipeline.core.models

import au.com.nicta.data.pipeline.core.executor.{PipeExecutionContext, SparkPipeEntry, MRPipeEntry}
import au.com.nicta.data.pipeline.io.NameService
import org.apache.hadoop.mapreduce.{Mapper, Reducer}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.{ClassTag, classTag}

/**
 * the generic pipe object
 * @param name
 * @param version
 * @param pipelineServer
 * @param executionContext
 * @param inputPath
 * @param inputFormat
 * @param outputPath
 * @param outputFormat
 * @tparam I
 * @tparam O
 */
abstract class Pipe[I: ClassTag, O: ClassTag](val name: String,
                                              val version:String,
                                              val pipelineServer: String,
                                              val executionContext: String,
                                              val inputPath: mutable.Buffer[String],
                                              var inputFormat: String,
                                              var outputPath: String,
                                              var outputFormat: String) extends Serializable {


  val parents = ArrayBuffer.empty[Pipe[_,_]]

  outputPath = NameService.getHDFSPath(name, version)

  private[pipeline] def outputAddress(executionTag:String):String = {
    outputPath+ s"/$executionTag"
  }

  private[pipeline] def inputAddress(executionTag:String):Seq[String] = {
    if(parents nonEmpty)
      inputPath.map(p => p + s"/$executionTag")
    else inputPath
  }

  private[pipeline] def execute()

  private[pipeline] def notify(status:String)

  def root():Seq[Pipe[_,_]] = {
    if(parents == null || parents.isEmpty) Seq(this)
    else {
      parents.flatMap(_.root())
    }
  }

  def connect(that:Pipe[_,_]) = {
    that.inputPath += this.outputPath
    that.inputFormat = this.outputFormat
    that.parents += this
    that
  }

  def join(these:Seq[Pipe[_,_]]) = {
    these.foreach(_.connect(this))
    this
  }

  def fork(those:Seq[Pipe[_,_]]) = {
    those.flatMap(_.root()).map{ that =>
      this.connect(that)
    }
    those
  }



  def :-> (that:Pipe[_,_]) = this.connect(that)

  def ->: (that:Pipe[_,_]) = that.connect(this)

  def <-:(that:Pipe[_,_]) = this.connect(that)


  def >-: (these: Pipe[_,_]*) = this.join(these)

  def ->: (these:Product) = {
    require(these.productIterator.forall(_.isInstanceOf[Pipe[_,_]]))
    this.join(these.productIterator.map(_.asInstanceOf[Pipe[_,_]]).toSeq)
  }

  def ->: (these:Seq[Pipe[_,_]]) = this.join(these)


  def :-<(those:Pipe[_,_]*)  = this.fork(those)

  def :->(those:Pipe[_,_]*)  = this.fork(those)

  def :-> (those:Product) = {
    require(those.productIterator.forall(_.isInstanceOf[Pipe[_,_]]))
    this.fork(those.productIterator.map(_.asInstanceOf[Pipe[_,_]]).toSeq)
  }

  def <-:(those:Pipe[_,_]*)  = those.map(that => connect(that))

  override def toString: String = {
    s"""name: $name
        pipelineServer: $pipelineServer
        executionContext: $executionContext
        inputProtocol: $inputPath
        outputProtocol: $outputPath
        parents:${parents.map(_.name).mkString(",")}"""
  }
}


/**
 * A Spark pipe object
 * @param name
 * @param exec
 * @param version
 * @param pipelineServer
 * @param executionContext
 * @param inputPath
 * @param inputFormat
 * @param output
 * @param outputFormat
 * @tparam I
 * @tparam O
 */
class SparkPipe[I:ClassTag, O:ClassTag]( name: String,
                                         @transient
                                         val exec:SparkProc[I, O] ,
                                         version:String = "",
                                         pipelineServer: String = PipeExecutionContext.DEFAULT_PIPELINE_SERVER,
                                         executionContext: String = PipeExecutionContext.SPARK_EXECUTION_CONTEXT,
                                         inputPath: mutable.Buffer[String] = ArrayBuffer.empty[String],
                                         inputFormat: String = "text",
                                         output: String = "hdfs",
                                         outputFormat: String = "text"
                                         ) extends Pipe[I, O](name, version, pipelineServer, executionContext, inputPath, inputFormat , output, outputFormat) {

 
  val appClass = exec.getClass.getCanonicalName

  val inType = classTag[I]

  val outType = classTag[O]

//  outputPath = NameService.getPath(name, version)

  override private[pipeline] def execute(): Unit = PipelineContext.exec(this)

  override private[pipeline] def notify(status: String): Unit = {
    println(s"send call back to  $pipelineServer")
  }

  override def toString: String = {
    super.toString + "\n" +
    s"exec: $exec \n"
  }
}

/**
 *  a MapReduce pipe object
 * @param name
 * @param mapper
 * @param combiner
 * @param reducer
 * @param version
 * @param pipelineServer
 * @param executionContext
 * @param inputProtocol
 * @param inputFormat
 * @param outputProtocol
 * @param outputFormat
 * @tparam KIN
 * @tparam VIN
 * @tparam KOUT
 * @tparam VOUT
 */
class MRPipe_old[KIN:ClassTag, VIN:ClassTag, KOUT:ClassTag, VOUT:ClassTag] ( name: String,
                                     mapper:Mapper[KIN, VIN, KOUT, VOUT],
                                     combiner:Reducer[KOUT, VOUT, KOUT, VOUT] = null,
                                     reducer:Reducer[KOUT, VOUT, KOUT, VOUT] = null,
                                     version:String = "",
                                     pipelineServer: String = PipeExecutionContext.DEFAULT_PIPELINE_SERVER,
                                     executionContext: String = PipeExecutionContext.HADOOP_EXECUTION_CONTEXT,
                                     inputProtocol: mutable.Buffer[String] = ArrayBuffer.empty[String],
                                     inputFormat: String = "text",
                                     outputProtocol: String = "hdfs",
                                     outputFormat: String = "text") extends Pipe[Any, Any](name, version, pipelineServer, executionContext, inputProtocol, inputFormat, outputProtocol, outputFormat) {


  val mapperClassName = if(mapper ne null) mapper.getClass.getName else null

  val reducerClassName = if(reducer ne null) reducer.getClass.getName else null

  val combinatorClassName = if(combiner ne null) combiner.getClass.getName else null

  val mapKType = classTag[KIN].runtimeClass.getCanonicalName

  val mapVType = classTag[VIN].runtimeClass.getCanonicalName

  val outKType = classTag[KOUT].runtimeClass.getCanonicalName

  val outVType = classTag[VOUT].runtimeClass.getCanonicalName


  override private[pipeline] def notify(status: String): Unit = ???

  override private[pipeline] def execute(): Unit = PipelineContext.exec(this)

  override def toString: String = {
    super.toString + "\n" +
      s"mapper: $mapper \n" +
      s"reducer: $reducer \n" +
      s"combiner: $combiner \n"
  }
}

class MRPipe(name: String,
             val mapperClassName: String,
             val reducerClassName: String = null,
             val combinatorClassName: String = null,
             val mapKType:String,
             val mapVType:String,
             val outKType:String,
             val outVType:String,
             version: String = "",
             pipelineServer: String = PipeExecutionContext.DEFAULT_PIPELINE_SERVER,
             executionContext: String = PipeExecutionContext.HADOOP_EXECUTION_CONTEXT,
             inputProtocol: mutable.Buffer[String] = ArrayBuffer.empty[String],
             inputFormat: String = "text",
             outputProtocol: String = "hdfs",
             outputFormat: String = "text") extends Pipe[Any, Any](name, version, pipelineServer, executionContext, inputProtocol, inputFormat, outputProtocol, outputFormat) {


  override private[pipeline] def notify(status: String): Unit = ???

  override private[pipeline] def execute(): Unit = PipelineContext.exec(this)

  override def toString: String = {
    super.toString + "\n" +
      s"mapper: $mapperClassName \n" +
      s"reducer: $reducerClassName \n" +
      s"combiner: $combinatorClassName \n"
  }
}

object MRPipe {

  def apply[KIN:ClassTag, VIN:ClassTag, KOUT:ClassTag, VOUT:ClassTag] ( name: String,
                                                                        mapper:Mapper[KIN, VIN, KOUT, VOUT],
                                                                        combiner:Reducer[KOUT, VOUT, KOUT, VOUT] = null,
                                                                        reducer:Reducer[KOUT, VOUT, KOUT, VOUT] = null,
                                                                        version:String = "",
                                                                        pipelineServer: String = PipeExecutionContext.DEFAULT_PIPELINE_SERVER,
                                                                        executionContext: String = PipeExecutionContext.HADOOP_EXECUTION_CONTEXT,
                                                                        inputProtocol: mutable.Buffer[String] = ArrayBuffer.empty[String],
                                                                        inputFormat: String = "text",
                                                                        outputProtocol: String = "hdfs",
                                                                        outputFormat: String = "text"):MRPipe = {
    val mapperClassName = if(mapper ne null) mapper.getClass.getCanonicalName else null
    val reducerClassName = if(reducer ne null) reducer.getClass.getCanonicalName else null
    val combinatorClassName = if(combiner ne null) combiner.getClass.getCanonicalName else null

    val mapKType = classTag[KIN].runtimeClass.getCanonicalName
    val mapVType = classTag[VIN].runtimeClass.getCanonicalName
    val outKType = classTag[KOUT].runtimeClass.getCanonicalName
    val outVType = classTag[VOUT].runtimeClass.getCanonicalName
    new MRPipe(name,
      mapperClassName,
      reducerClassName,
      combinatorClassName,
      mapKType, mapVType,
      outKType, outVType,
      version,
      pipelineServer, executionContext, inputProtocol, inputFormat, outputProtocol, outputProtocol)
  }
}


/**
 * A shell pipe object
 *
 * @param name
 * @param version
 * @param script
 * @param pipelineServer
 * @param executionContext
 */
class ShellPipe(name: String,
                version:String,
                val script:String = null,
                inputProtocol: ArrayBuffer[String] = ArrayBuffer.empty[String],
                pipelineServer: String = PipeExecutionContext.DEFAULT_PIPELINE_SERVER,
                executionContext: String = PipeExecutionContext.SHELL_CONTEXT
             ) extends Pipe[String, String](name, version, pipelineServer, executionContext, inputProtocol, "text" ,"sh", "text") {

  override private[pipeline] def execute(): Unit = PipelineContext.exec(this)

  override private[pipeline] def notify(status: String): Unit = ???

  override def toString: String = {
    super.toString + "\n" +
      s"script: $script \n"
  }
}
