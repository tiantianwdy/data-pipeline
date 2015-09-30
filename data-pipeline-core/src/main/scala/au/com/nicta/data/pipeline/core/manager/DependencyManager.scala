package au.com.nicta.data.pipeline.core.manager

import java.io.File
import java.nio.file.attribute.{BasicFileAttributes, FileAttribute}
import java.nio.file.{StandardOpenOption, StandardCopyOption, Paths, Files}
import au.com.nicta.data.pipeline.core.executor.PipeExecutionContext
import au.com.nicta.data.pipeline.core.utils.Logging

/**
 * Created by tiantian on 10/08/15.
 */
class DependencyManager(val dependencyBasePath:String, val historyManager: HistoryManager) extends Serializable with Logging{

  def getAppPath(appName:String, version:String):String =  s"$dependencyBasePath/app/$appName/${version}/${appName}-${version}.jar"

  def getDepPath(appName:String, version:String):String = s"$dependencyBasePath/dep/$appName/${version}"

  def getSharedDep():String = s"$dependencyBasePath/shared"

  def getAllDepFiles(appName:String, version:String):Array[File] = {
    val depPath = new File(getDepPath(appName, version))
      if(depPath.exists() && depPath.isDirectory){
        depPath.listFiles() ++ new File(getSharedDep).listFiles()
      } else new File(getSharedDep).listFiles()
  }

  //add dependency lib to a job from local
  def addDepFromLocal(appName:String, version:String, srcFile:String, author:String = "defultUser"): Unit = {
    val src = Paths.get(srcFile)
    val target = Paths.get(getDepPath(appName, version))
    if(Files.notExists(target)){
      Files.createDirectories(target.getParent)
      Files.createFile(target)
    }
    Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING)
    val current =  System.currentTimeMillis()
    val trace = PipeTrace(appName, version, author, Seq(srcFile), current, current)
    historyManager.addPipeTrace(trace)
  }

  //add dependency lib to a lib
  def addDep(appName:String, version:String, depName:String, depBytes:Array[Byte], author:String = "defultUser"): Unit = {
    val target = Paths.get(getDepPath(appName, version)+ "/" + depName)
    if(Files.notExists(target)){
      Files.createDirectories(target.getParent)
      Files.createFile(target)
    }
    Files.write(target, depBytes, StandardOpenOption.CREATE)
    val current =  System.currentTimeMillis()
    val trace = PipeTrace(appName, version, author, Seq(target.toString), current, current)
    historyManager.addPipeTrace(trace)
  }

  // submit a job with version from local file
  def submitFromLocal(appName:String, version:String, srcFile:String, author:String = "defultUser"): Unit = {
    val src = Paths.get(srcFile)
    val target = Paths.get(getAppPath(appName, version))
    if(Files.notExists(target)){
      Files.createDirectories(target.getParent)
      Files.createFile(target)
    }
    Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING)
    val current =  System.currentTimeMillis()
    val trace = PipeTrace(appName, version, author, Seq(srcFile), current, current)
    historyManager.addPipeTrace(trace)
  }

  // submit a job with bytes
  def submit(appName:String, version:String, depBytes:Array[Byte], author:String = "defultUser")= {
    val target = Paths.get(getAppPath(appName, version))
    if(Files.exists(target)){
      Files.write(target, depBytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
    } else {
      if(Files.notExists(target.getParent))
        Files.createDirectories(target.getParent)
      Files.write(target, depBytes, StandardOpenOption.CREATE)
    }
    val current =  System.currentTimeMillis()
    val trace = PipeTrace(appName, version, author, Seq(target.toString), current, current)
    historyManager.addPipeTrace(trace)
  }
}

object DependencyManager extends Serializable{

  lazy val dependencyManager = new DependencyManager(PipeExecutionContext.DEPENDENCY_HOME, HistoryManager())

  def apply() ={
    dependencyManager
  }
}
