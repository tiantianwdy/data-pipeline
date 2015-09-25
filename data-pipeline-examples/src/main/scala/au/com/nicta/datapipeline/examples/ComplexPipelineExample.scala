package au.com.nicta.datapipeline.examples


import au.com.nicta.data.pipeline.core.examples._
import au.com.nicta.data.pipeline.core.executor.{AkkaCallbackEntry, PipeExecutionContext}
import au.com.nicta.data.pipeline.core.messages.PipelineJobMsg
import au.com.nicta.data.pipeline.core.models._

import scala.collection.mutable

/**
 * Created by tiantian on 2/08/15.
 */
object ComplexPipelineExample extends App {


  val csvMapper = MRPipe(name = "csvMapper", version = "0.0.1",
    mapper = new CSVMapper,
    inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/tiantian/org1/csv/")
  )

  val jsonMapper = MRPipe(name = "jsonMapper", version = "0.0.1",
    mapper = new JSONMapper,
    inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/tiantian/org1/json/")
  )

  val textMapper = MRPipe(name = "textMapper", version = "0.0.1",
    mapper = new TextMapper,
    inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/tiantian/org1/text/")
  )

  val dataJoiner = new SparkPipe(name = "dataJoiner", version ="0.0.1",
    exec = new DataJoinerProc
  )


  val featureExtractorNew = new SparkPipe(name = "extractorNew", version = "0.0.2",
    exec = new FeatureExtractorSparkProcNew
  )

  val featureExtractorSpark = new SparkPipe(name = "extractorSpark", version ="0.0.1",
    exec = new FeatureExtractorSparkProc
  )

  val analysisSpark = new SparkPipe(name = "analysisSpark", version = "0.0.1",
   exec = new SparkAnalysisProc)


  val analysisSparkNew = new SparkPipe(name = "analysisSparkNew", version = "0.0.1",
    exec = new SparkAnalysisProcNew)

//  val pipeline =(csvMapper, jsonMapper, textMapper) ->: dataJoiner

  val pipeline = ((csvMapper, jsonMapper, textMapper) ->: dataJoiner) :-> (featureExtractorNew :-> analysisSparkNew,
                                                                           featureExtractorSpark :-> analysisSpark)


  DependencySubmit.submitAllDependency(csvMapper,
    jsonMapper,
    textMapper,
    dataJoiner,
    featureExtractorSpark,
    featureExtractorNew,
    analysisSpark,
    analysisSparkNew)

  val msg = PipelineContext.exec("ComplexPipeLine", pipeline, PipeExecutionContext.DEFAULT_PIPELINE_SERVER)
  println("execution Id: " + msg)

  System.exit(0)

// other options:
//    PipelineContext.pseudoRun(pipeline)
//    PipelineContext.explain(pipeline)
//    PipelineContext.test(pipeline)



}


