package au.com.nicta.data.pipeline.core.models


/**
 * Created by tiantian on 3/08/15.
 */
abstract class ComposedPipes(name:String,
                             version:String,
                             pipelineServer: String,
                             executionContext: String) extends Pipe[Any, Any](name, version, pipelineServer, executionContext, null, "", "", "")


abstract class ParallelPipes(name:String,
                             version:String,
                             pipelineServer: String,
                             executionContext: String,
                             val pipes:Seq[Pipe[_, _]]) extends ComposedPipes(name, version, pipelineServer, executionContext) {




override def connect(that: Pipe[_, _]): Pipe[_, _] = {
    pipes.foreach(_.connect(that))
    that
  }

  override private[pipeline] def execute(): Unit = ???

  override private[pipeline] def notify(status: String): Unit = ???

}
