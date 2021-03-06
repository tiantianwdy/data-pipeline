package au.com.nicta.data.pipeline.core.examples

import au.com.nicta.data.pipeline.core.models.SparkRDDProc
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Mapper
import org.apache.spark.SparkContext
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, DataFrame}
import org.codehaus.jackson.map.ObjectMapper

import scala.beans.BeanProperty

/**
 * Created by tiantian on 25/09/15.
 */

class CSVMapper extends Mapper[Any, Text, Text, Text] {

  private val obj = new Text()
  private val prefix = new Text()

  override def map(key: Any, value: Text, context: Mapper[Any, Text, Text, Text]#Context): Unit = {
    val arr = value.toString.split(";")
    prefix.set(arr(0).substring(3))
    obj.set(arr(1))
    context.write(prefix, obj)
  }

}


class Rankings(@BeanProperty val url:String,
               @BeanProperty val ranking: Float) {

  def this(){
    this("", 0F)
  }

  override def toString: String = {
    s"$url $ranking"
  }
}

class JSONMapper extends Mapper[Any, Text, Text, Text] {

  private val outputValue = new Text()
  private val outputKey = new Text()
  val mapper = new ObjectMapper()
  //  mapper.registerModule(new DefaultScalaModule)

  override def map(key: Any, value: Text, context: Mapper[Any, Text, Text, Text]#Context): Unit = {
    val json =  mapper.readValue(value.toString, classOf[Rankings])
    outputKey.set(s"${json.url.substring(3)}")
    outputValue.set(s"${json.ranking}")
    context.write(outputKey, outputValue)
  }

}

class TextMapper extends Mapper[Any, Text, Text, Text] {

  private val obj = new Text()
  private val prefix = new Text()

  override def map(key: Any, value: Text, context: Mapper[Any, Text, Text, Text]#Context): Unit = {
    val arr = value.toString.split(" ")
    prefix.set(arr(0).substring(3))
    obj.set(arr(1))
    context.write(prefix, obj)
  }

}

class DataJoinerProc extends  SparkRDDProc[String, String] {


  override def process(in:  Seq[RDD[String]], sc: SparkContext): Either[RDD[String], DataFrame] = {
    require(in.length >=3)
    val data = in.map(rdd => rdd.map(_.split("\\s+")).map(arr => arr(0) -> arr(1).toFloat))
    val input1 = data(0)
    val input2 = data(1)
    val input3 = data(2)
    val joined = input1.cogroup(input2, input3).map( kv =>
      (kv._1, kv._2._1.sum, kv._2._2.sum, kv._2._3.sum)
    )
    val res = joined.map(d=> d.productIterator.mkString(";"))
    Left(res)
  }
}


case class JoinedData(url:String, f1:Float, f2:Float, f3:Float)

class FeatureExtractorPyProc extends SparkRDDProc[String, Any] {

  import org.apache.spark.sql._

  override def process(in:  Seq[RDD[String]], sc: SparkContext): Either[RDD[Any], DataFrame] = {
    val sqlContext = new  SQLContext(sc)

    import sqlContext.implicits._


    val data = in.head
    val rankings  = data.map(_.split(";")).map(r => JoinedData(r(0), r(1).trim.toFloat, r(2).trim.toFloat, r(3).trim.toFloat)).toDF
    rankings.registerTempTable("rankings")

    val res = sqlContext.sql("select f1, f2 from rankings where f1 > 10")
    Right(res)
  }
}

class FeatureExtractorSparkProcNew extends SparkRDDProc[String, Any] {

  import org.apache.spark.sql._


  override def process(in: Seq[RDD[String]], sc: SparkContext): Either[RDD[Any], DataFrame] = {
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._
    //    case class Rankings(url:String, f1:Float, f2:Float, f3:Float)

    val data = in.head
    val rankings = data.map(_.split(";")).map(r => JoinedData(r(0), r(1).trim.toFloat, r(2).trim.toFloat, r(3).trim.toFloat)).toDF
    rankings.registerTempTable("rankings")

    val res = sqlContext.sql("select f1, f2, f3 from rankings ")
    Right(res)
  }

}



class FeatureExtractorSparkProc extends SparkRDDProc[String, Any] {

  import org.apache.spark.sql._


  override def process(in:  Seq[RDD[String]], sc: SparkContext): Either[RDD[Any], DataFrame] = {
    val sqlContext = new  SQLContext(sc)
    import sqlContext.implicits._
    //    case class Rankings(url:String, f1:Float, f2:Float, f3:Float)

    val data = in.head
    val rankings  = data.map(_.split(";")).map(r => JoinedData(r(0), r(1).trim.toFloat, r(2).trim.toFloat, r(3).trim.toFloat)).toDF
    rankings.registerTempTable("rankings")

    val res = sqlContext.sql("select f2, f3 from rankings where f2 > 10")
    Right(res)
  }

}



class SparkAnalysisProc extends SparkRDDProc[String, Vector[Double]] {


  override def process(in: Seq[RDD[String]], sc: SparkContext): Either[RDD[Vector[Double]], DataFrame] = {
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val data = in.head

    val res = data.map(_.split(",")).map(str => LabeledPoint(str(0).toDouble, Vectors.dense(str(0).toDouble, str(1).toDouble))).toDF()

    val lr = new LinearRegression()
    lr.setMaxIter(3).setRegParam(0.01)
    val model = lr.fit(res).weights.toArray
    val output = sc.parallelize(Seq(model.toVector))
    Left(output)
  }
}

class SparkAnalysisProcNew extends SparkRDDProc[String, Vector[Double]] {


  override def process(in: Seq[RDD[String]], sc: SparkContext): Either[RDD[Vector[Double]], DataFrame] = {
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val data = in.head

    val res = data.map(_.split(",")).map(str => LabeledPoint(str(0).toDouble, Vectors.dense(str(1).toDouble, str(2).toDouble))).toDF()

    val lr = new LinearRegression()
    lr.setMaxIter(3).setRegParam(0.01)
    val model = lr.fit(res).weights.toArray
    val output = sc.parallelize(Seq(model.toVector))
    Left(output)
  }
}
