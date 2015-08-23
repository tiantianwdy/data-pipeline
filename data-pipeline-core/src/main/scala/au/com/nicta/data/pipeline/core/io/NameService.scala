package au.com.nicta.data.pipeline.io

/**
 * Created by tiantian on 17/07/15.
 */
object NameService {

  def getPath(pipeline:String, version:String):String = {
    s"/data-pipeline/$pipeline/$version/"
  }

  def getPath(pipeline:String, version:String, execTag:String):String = {
    s"/data-pipeline/$pipeline/$version/$execTag/"
  }

  def getURL(serverAddress:String, pipeline:String, version:String):String = {
    s"$serverAddress/${getPath(pipeline,version)}"
  }

  def getHDFSPath(pipeline:String, version:String):String = {
    s"hdfs://127.0.0.1:9001/user/${IOContext.username}/data-pipeline/$pipeline/$version/"
  }

  def getHDFSPath(pipeline:String, version:String, execTag:String):String = {
    s"hdfs://127.0.0.1:9001/user/${IOContext.username}/data-pipeline/$pipeline/$version/$execTag/"
  }

}

object IOContext {

  val HDFS_HOST = "hdfs://127.0.0.1:9001"

  val DB_HOST = ""

  val CACHE_HOST = ""

  val username = "tiantian"


  def getServerAddress(protocol:String): String = {
    protocol match {
      case "hdfs" => HDFS_HOST
      case "sql" => DB_HOST
      case "cache" => CACHE_HOST
      case x => x
    }
  }
}
