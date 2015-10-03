package au.com.nicta.datapipeline.examples

import au.com.nicta.data.pipeline.core.models.{SparkPipe, MRPipe, Pipe}
import au.com.nicta.data.pipeline.core.server.DependencyClient

/**
 * Created by tiantian on 2/10/15.
 */
object DependencySubmit {

  def submitAllDependency(pipeList: Pipe[_, _]*): Unit = {
    pipeList.foreach { pipe => pipe match {
      case p: MRPipe =>
        DependencyClient.submitPipe(p.name, p.version,
          "/home/tiantian/Dev/workspace/data-pipeline/data-pipeline-examples/target/data-pipeline-examples-0.0.1.jar")
      case p: SparkPipe[_, _] =>
        DependencyClient.submitPipe(p.name, p.version,
          "/home/tiantian/Dev/workspace/data-pipeline/data-pipeline-examples/target/data-pipeline-examples-0.0.1.jar")
    }
    }
  }
}
