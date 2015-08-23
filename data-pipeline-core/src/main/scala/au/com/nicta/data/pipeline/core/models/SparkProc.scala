package au.com.nicta.data.pipeline.core.models

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

import scala.reflect.ClassTag

/**
 * Created by tiantian on 17/07/15.
 */
@SerialVersionUID(1L)
abstract class SparkProc[I:ClassTag, O:ClassTag]() extends Serializable{


  def process(in:Either[Seq[RDD[I]], Seq[DataFrame]], sc:SparkContext):Either[RDD[O], DataFrame]
}


abstract class SparkRDDProc[I:ClassTag, O:ClassTag]() extends SparkProc[I,O]{

  final override def process(in:Either[Seq[RDD[I]], Seq[DataFrame]], sc:SparkContext):Either[RDD[O], DataFrame] = {
    in match {
      case Left(rdd) => process(rdd,sc)
      case Right(df) => throw new RuntimeException("unsupported data format")
    }
  }


  def process(in:Seq[RDD[I]], sc:SparkContext):Either[RDD[O], DataFrame]

}

abstract class SparkSQLProc[I:ClassTag, O:ClassTag]() extends SparkProc[I,O]{

  final override def process(in:Either[Seq[RDD[I]], Seq[DataFrame]], sc:SparkContext):Either[RDD[O], DataFrame] = {
    in match {
      case Left(rdd) => throw new RuntimeException("unsupported data format")
      case Right(df) => process(df,sc)
    }
  }

  def process(in:Seq[DataFrame], sc:SparkContext):Either[RDD[O], DataFrame]
}


object SparkProc{

  def apply[I:ClassTag, O:ClassTag](exec :(Either[Seq[RDD[I]], Seq[DataFrame]], SparkContext) => Either[RDD[O], DataFrame]) = new SparkProc[I,O](){


    def process (in:Either[Seq[RDD[I]], Seq[DataFrame]], sc:SparkContext) = exec(in, sc)
  }
}

