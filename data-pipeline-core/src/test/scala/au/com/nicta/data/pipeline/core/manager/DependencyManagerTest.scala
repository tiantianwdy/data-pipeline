package au.com.nicta.data.pipeline.core.manager

import org.junit.Test
/**
 * Created by tiantian on 11/08/15.
 */
class DependencyManagerTest {

  @Test
  def testSubmitSparkPipeFromLocal(): Unit ={
    DependencyManager().submitFromLocal("analysisSpark2", "0.0.1",
      "/home/tiantian/Dev/workspace/datapipeline-examples/target/datapipeline-examples-0.0.1.jar" )
  }

  @Test
  def testSubmitMRPipeFromLocal(): Unit ={
    DependencyManager().submitFromLocal("mr-pipe", "0.0.1",
      "/home/tiantian/Dev/workspace/datapipeline-examples/target/scala-2.10/datapipeline-examples-assembly-0.0.1.jar" )
  }

}
