package au.com.nicta.data.pipeline.core.executor

import org.junit.Test

/**
 * Created by tiantian on 13/08/15.
 */
class PipeExecutionContextTest {

  @Test
  def testConfigLoading(): Unit ={
    println(PipeExecutionContext.DEPENDENCY_HOME)
    println(PipeExecutionContext.DEFAULT_PIPELINE_SERVER)
    println(PipeExecutionContext.SPARK_EXECUTION_CONTEXT)
    println(PipeExecutionContext.HADOOP_EXECUTION_CONTEXT)
    println(PipeExecutionContext.SHELL_CONTEXT)
  }

}
