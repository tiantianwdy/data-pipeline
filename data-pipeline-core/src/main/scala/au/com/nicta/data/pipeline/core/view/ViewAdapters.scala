package au.com.nicta.data.pipeline.core.view

import java.nio.file.{Paths, Path}

import au.com.nicta.data.pipeline.core.manager.{ExecutionTrace, PipeProvenance}
import au.com.nicta.data.pipeline.view.models.{Link, PipeNode, GraphVO, TreeVO}

import scala.collection.mutable

/**
 * Created by tiantian on 25/09/15.
 */
object ViewAdapters {

  def pipelineProvenanceToTreeVO(pipelineName:String, provenance:Seq[(String, Seq[String])]):TreeVO = {
    val treeRoot = new TreeVO(pipelineName)
    provenance.foreach{execIns =>
      val insNode = new TreeVO(execIns._1)
      treeRoot.addChild(insNode)
      execIns._2 foreach{pipeId =>
        insNode.addChild(new TreeVO(pipeId))
      }
    }
    treeRoot
  }


  def pipeProvenanceToTreeVO(pipeName:String, provenance:Seq[(String, PipeProvenance)]):TreeVO = {

    val treeRoot = new TreeVO(pipeName)
    provenance.foreach{version =>
      treeRoot.addChild(pipeToTreeVO(version._2))
    }
    treeRoot
  }


  def pipeToTreeVO(data:PipeProvenance):TreeVO = {
    val treeRoot = new TreeVO(data.version)
    val author = new TreeVO("author")
    author.addChild(new TreeVO(data.author))
    val dep = new TreeVO("dep")
    data.dep.foreach(d => dep.addChild(new TreeVO(d)))
    val updateTime = new TreeVO("updateTime")
    updateTime.addChild(new TreeVO(data.lastUpdatedTime.toString))
    val instances = new TreeVO("instances")
    data.instances.foreach{ ins =>
        val insNode = new TreeVO(ins.execTag)
        insNode.addChild(new TreeVO("in: " + ins.inputPath.mkString("(", ",", ")")))
        insNode.addChild(new TreeVO("out: "+ ins.outputPath))
        insNode.addChild(new TreeVO("state: " + ins.status))
        instances.addChild(insNode)
    }

    treeRoot.addChild(author).addChild(dep).addChild(updateTime).addChild(instances)
  }


  def pipelineListToTreeVO(data: Seq[(String, Seq[String])]):TreeVO = {
    val treeRoot = new TreeVO("All Pipelines")
    data.foreach{
      p =>
        val pipeline = new TreeVO(p._1)
        treeRoot.addChild(pipeline)
        p._2.foreach(exeId => pipeline.addChild(new TreeVO(exeId)))
    }
    treeRoot
  }

  def pipeListToTreeVO(data:Seq[String]):TreeVO = {
    val treeRoot = new TreeVO("All Pipes")
    data.foreach(p=> treeRoot.addChild(new TreeVO(p)))
    treeRoot
  }


  def executionHistoryToGraphVO(exeTag:String, res:Seq[ExecutionTrace]):GraphVO ={
    val graph = new GraphVO
    val nodes = mutable.Buffer.empty[PipeNode]
    res.foreach{trace =>
      val node = new PipeNode(trace.pipeName, trace.version)
      if(!containsNode(trace.pipeName, trace.version, nodes)){
        nodes += node
        graph.addNodes(node)
      }
    }
    res.foreach{trace =>
      trace.inputPath.foreach { str =>
        val path = Paths.get(str)
        val version = getName(path)
        val parentName = getName(path.getParent)
        val target = indexOfNode(parentName, version, nodes)
        if(target >= 0){
          val source = indexOfNode(trace.pipeName, trace.version, nodes)
          graph.addLink(new Link(source, target))
        }
      }
    }
    graph
  }

  def getName(path:Path):String = {
    path.toFile.getName
  }

  def indexOfNode(pipeName:String, version:String, nodes:Seq[PipeNode]):Int = {
    nodes.find(_.getName == s"$pipeName#$version") match {
      case Some(n) => nodes.indexOf(n)
      case None => -1
    }
  }

  def containsNode(pipeName:String, version:String, nodes:Seq[PipeNode]):Boolean = {
    nodes.exists(_.getName == s"$pipeName#$version")
  }

}
