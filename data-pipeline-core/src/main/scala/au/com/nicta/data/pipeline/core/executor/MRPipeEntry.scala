package au.com.nicta.data.pipeline.core.executor

import java.util.UUID

import au.com.nicta.data.pipeline.core.manager.DependencyManager
import au.com.nicta.data.pipeline.core.messages.PipeCompleteMsg
import au.com.nicta.data.pipeline.core.models.{MRPipe, PipelineContext}
import au.com.nicta.data.pipeline.io.NameService
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.util.{Tool, ToolRunner}

/**
 * Created by tiantian on 13/07/15.
 */
object MRPipeEntry {


  val MR_JOB_TRACKER_ADRESS = "local"


  def launch(pipe: MRPipe[_, _, _, _], taskId:String, executionTag:String) = {
    val conf = new Configuration()
    conf.set("mapred.job.tracker", MR_JOB_TRACKER_ADRESS)
    conf.set("fs.default.name", "hdfs://127.0.0.1:9001")
//    conf.set("job.end.notification.url", pipe.pipelineServer)
//    conf.setInt("job.end.retry.attempts", 3)
//    conf.setInt("job.end.retry.interval", 1000)
//    val dependencyFiles = s"${PipelineContext.DEPENDENCY_HOME}/app/${pipe.name}/${pipe.version}/${pipe.name}-${pipe.version}.jar"

    val appJar = DependencyManager().getAppPath(pipe.name, pipe.version)
    val outputPath = NameService.getHDFSPath(pipe.name, pipe.version, executionTag)
    val inputPaths = if(pipe.parents != null && pipe.parents.nonEmpty){
      pipe.parents.map(p => NameService.getHDFSPath(p.name, p.version, executionTag))
    } else pipe.inputPath
    println("input paths:" + inputPaths.mkString(","))
    val job = Job.getInstance(conf, pipe.name)
    job.setJar(appJar)
//    job.setJarByClass(pipe.mapper.getClass)
    job.setMapperClass(pipe.mapper.getClass)
    if(pipe.reducer ne null){
      job.setReducerClass(pipe.reducer.getClass)
      job.setOutputKeyClass(pipe.outKType)
      job.setOutputValueClass(pipe.outVType)
    } else { // map only jobs
      job.setNumReduceTasks(0)
      job.setMapOutputKeyClass(pipe.outKType)
      job.setMapOutputValueClass(pipe.outVType)
    }
    if(pipe.combiner ne null){
      job.setCombinerClass(pipe.combiner.getClass)
    }

    inputPaths.foreach (p =>
      FileInputFormat.addInputPath(job, new Path(p))
    )
    FileOutputFormat.setOutputPath(job, new Path(outputPath))
    println("output path:" + outputPath)
//    job.submit()
    job.waitForCompletion(true)
    println(job.getStatus)
    val state = job.getStatus.getState
    if(state == org.apache.hadoop.mapreduce.JobStatus.State.SUCCEEDED){
      AkkaCallbackEntry.sendCallBack(pipe.pipelineServer, PipeCompleteMsg(pipe.name, pipe.version, taskId, "success", executionTag))
    } else {
      AkkaCallbackEntry.sendCallBack(pipe.pipelineServer, PipeCompleteMsg(pipe.name, pipe.version, taskId, state.toString, executionTag))
    }
    job.getTrackingURL
  }

}

abstract class ScalaHadoopTool extends  Configured with Tool {



  def main (args: Array[String]) {
    ToolRunner.run(this, args)
  }
}

