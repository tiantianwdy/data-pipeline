package au.com.nicta.datapipeline.example

import java.nio.charset.Charset
import java.nio.file.{StandardOpenOption, Paths, Path, Files}

import au.com.nicta.datapipeline.examples.Rankings
import com.fasterxml.jackson.databind.ObjectMapper

import scala.util.Random

/**
 * Created by tiantian on 10/08/15.
 */
object DataGenerator extends  App{

  val jsonMapper = new ObjectMapper()

  def generateRankingJsonData(n:Int, filePath:String): Unit ={
    val strLen = 16
    val file = Files.newBufferedWriter(Paths.get(filePath), Charset.forName("UTF-8"), StandardOpenOption.CREATE)
    for (i <- 1 to n) {
      val url =  Random.alphanumeric.take(strLen).mkString("")
      val record = new Rankings(url, Random.nextFloat() * 100)
      val outStr = jsonMapper.writeValueAsString(record)
//      println(outStr)
      file.write(outStr + "\r\n")
    }
    file.close()
  }


  def generateRankingCSVData(n:Int, filePath:String): Unit ={
    val strLen = 16
    val path = Paths.get(filePath)
    val writer = Files.newBufferedWriter(path,Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    for (i <- 1 to n) {
      val url =  Random.alphanumeric.take(strLen).mkString("")
      val record = new Rankings(url, Random.nextFloat() * 100)
      //      println(outStr)
      writer.write(s"${record.url}; ${record.ranking}\r\n")
    }
    writer.close()
  }

  def generateRankingTextData(n:Int, filePath:String): Unit ={
    val strLen = 16
    val path = Paths.get(filePath)
    val writer = Files.newBufferedWriter(path,Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    for (i <- 1 to n) {
      val url =  Random.alphanumeric.take(strLen).mkString("")
      val record = new Rankings(url, Random.nextFloat() * 100)
      //      println(outStr)
      writer.write(s"${record.url} ${record.ranking}\r\n")
    }
    writer.close()
  }

  generateRankingCSVData(10000, "/home/tiantian/Dev/test/data/csv/ranking-part-00002.csv")

}
