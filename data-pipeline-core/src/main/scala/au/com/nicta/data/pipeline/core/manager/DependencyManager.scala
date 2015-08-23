package au.com.nicta.data.pipeline.core.manager

import java.io.File
import java.nio.file.{StandardOpenOption, StandardCopyOption, Paths, Files}

import au.com.nicta.data.pipeline.core.executor.PipeExecutionContext

/**
 * Created by tiantian on 10/08/15.
 */
class DependencyManager(val dependencyBasePath:String) extends Serializable {

  def getAppPath(appName:String, version:String):String =  s"$dependencyBasePath/app/$appName/${version}/${appName}-${version}.jar"

  def getDepPath(appName:String, version:String):String = s"$dependencyBasePath/dep/$appName/${version}"

  def getSharedDep():String = s"$dependencyBasePath/shared"

  def getAllDepFiles(appName:String, version:String):Array[File] = {
    val depPath = new File(getDepPath(appName, version))
      if(depPath.exists() && depPath.isDirectory){
        depPath.listFiles() ++ new File(getSharedDep).listFiles()
      } else new File(getSharedDep).listFiles()
  }

  def addDepFromLocal(appName:String, version:String, srcFile:String): Unit = {
    val src = Paths.get(srcFile)
    val target = Paths.get(getDepPath(appName, version))
    if(Files.notExists(target)){
      Files.createDirectories(target.getParent)
      Files.createFile(target)
    }
    Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING)
  }

  def addDep(appName:String, version:String, depName:String, depBytes:Array[Byte]): Unit = {
    val target = Paths.get(getDepPath(appName, version)+ "/" + depName)
    Files.write(target, depBytes, StandardOpenOption.CREATE)
  }

  def submitFromLocal(appName:String, version:String, srcFile:String): Unit = {
    val src = Paths.get(srcFile)
    val target = Paths.get(getAppPath(appName, version))
    if(Files.notExists(target)){
      Files.createDirectories(target.getParent)
      Files.createFile(target)
    }
    Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING)
  }

  def submit(appName:String, version:String, depBytes:Array[Byte])= {
    val target = Paths.get(getAppPath(appName, version))
    Files.write(target, depBytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
  }
}

object DependencyManager extends Serializable{

  lazy val dependencyManager = new DependencyManager(PipeExecutionContext.DEPENDENCY_HOME)

  def apply() ={
    dependencyManager
  }
}
