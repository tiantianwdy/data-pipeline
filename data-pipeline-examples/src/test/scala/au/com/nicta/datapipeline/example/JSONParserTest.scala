package au.com.nicta.datapipeline.example

import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import au.com.nicta.datapipeline.examples.Rankings
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test

/**
 * Created by tiantian on 11/08/15.
 */
class JSONParserTest {

  val mapper = new ObjectMapper()
//  mapper.registerModule(new DefaultScalaModule)

  @Test
  def testReadJsonObject(): Unit ={
    val path = Paths.get("/home/tiantian/Dev/test/data/json/ranking-part-00001.json")
    val reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))
    var line = reader.readLine()
    while(line ne null){
      val obj =  mapper.readValue(line, classOf[Rankings])
      println(obj)
      line = reader.readLine()
    }
    reader.close()
  }

}
