package au.com.nicta.data.pipeline.core.server

/**
 * Created by tiantian on 20/09/15.
 */
object PipelineServerMain extends App {

  SimplePipelineServer.start(port = 8999)

}
