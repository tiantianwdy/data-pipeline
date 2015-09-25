package au.com.nicta.datapipeline.examples

import java.lang
import java.lang.Iterable
import java.util.StringTokenizer

import au.com.nicta.data.pipeline.core.models.{PipelineContext, MRPipe}
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}

import scala.collection.mutable

/**
 * Created by tiantian on 17/07/15.
 */
object MRPipeExample extends App{


  val pipe = MRPipe(name = "mr-pipe", version = "0.0.1",
    mapper = new WordCountMapper,
    reducer = new SumReducer,
    combiner = new SumReducer,
    inputProtocol = mutable.Buffer("hdfs://127.0.0.1:9001/user/spark/benchmark/micro/rankings")
  )

  PipelineContext.test(pipe)


}

class WordCountMapper extends Mapper[Any, Text, Text, IntWritable] {

  private val one = new IntWritable(1)
  private val word = new Text()

  override def map(key: Any, value: Text, context: Mapper[Any, Text, Text, IntWritable]#Context): Unit = {
    val itr =  new StringTokenizer(value.toString())
    while(itr.hasMoreTokens){
      word.set(itr.nextToken())
      context.write(word, one)
    }
  }

}


/**
 * reducer of a MR job
 */
class SumReducer extends Reducer[Text, IntWritable, Text, IntWritable] {

  private val result = new IntWritable()



  override def reduce(key: Text, values: lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit = {
    var sum = 0
    val itr = values.iterator()
    while(itr.hasNext){
      sum += itr.next().get()
    }
    result.set(sum)
    context.write(key, result)
  }


}