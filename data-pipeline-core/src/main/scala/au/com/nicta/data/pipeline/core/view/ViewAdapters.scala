package au.com.nicta.data.pipeline.core.view

import java.nio.file.{Paths, Path}
import java.util.Date

import au.com.nicta.data.pipeline.core.manager.{ExecutionTrace, PipeProvenance}
import au.com.nicta.data.pipeline.view.models.{Link, PipeNode, GraphVO, TreeVO}

import scala.collection.mutable

/**
 * Created by tiantian on 25/09/15.
 */
object ViewAdapters {



  def pipelineProvenanceToTreeVO(pipelineName:String, provenance:Seq[(String, Seq[(String, Int)])]):TreeVO = {
    val treeRoot = new TreeVO(pipelineName)
    provenance.foreach{execIns =>
      val insNode = new TreeVO(execIns._1)
      treeRoot.addChild(insNode)
      execIns._2 foreach{trace =>
        insNode.addChild(new TreeVO(trace._1, trace._2))
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
        insNode.addChild(new TreeVO("in: " + ins.inputPath.mkString("(", " , ", ")")))
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


    def indexOfNode(pipeName:String, version:String, nodes:Seq[PipeNode]):Int = {
      nodes.find(_.getName == s"$pipeName#$version") match {
        case Some(n) => nodes.indexOf(n)
        case None => -1
      }
    }

    def containsNode(pipeName:String, version:String, nodes:Seq[PipeNode]):Boolean = {
      nodes.exists(_.getName == s"$pipeName#$version")
    }

    val graph = new GraphVO
    val nodes = mutable.Buffer.empty[PipeNode]
    res.foreach{trace =>
      val node = new PipeNode(trace.pipeName,
        trace.version,
        "Dongyao",
        trace.pipeClass,
        new Date(trace.startTime),
        new Date(trace.endTime),
        trace.status,
        1
      )
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
        val source = indexOfNode(parentName, version, nodes)
        if(source >= 0){
          val target = indexOfNode(trace.pipeName, trace.version, nodes)
          graph.addLink(new Link(source, target))
        }
      }
    }
    graph
  }

  def getName(path:Path):String = {
    path.toFile.getName
  }


  def executionHistoryToArray(exeTag:String, res:Seq[ExecutionTrace]):Array[Array[String]] = {

    def containsNode(pipeName:String, version:String, nodes:Seq[Array[String]]):Boolean = {
      nodes.exists( n => (n(1) == pipeName) && n(2) == version)
    }

    val data = mutable.Buffer.empty[Array[String]]
    res.foreach{trace =>
      val item = new Array[String](6)
      item(0) = res.indexOf(trace).toString
      item(1) = trace.pipeName
      item(2) = trace.version
      val parents = trace.inputPath.map{str =>
        if(str != null){
          val path = Paths.get(str)
          val version = getName(path)
          val parentName = getName(path.getParent)
          parentName + "#" + version
        } else Seq.empty[String]
      }
      item(3) = parents.mkString(" , ")
      item(4) = (trace.endTime - trace.startTime).toString
      item(5) = trace.status
      if(!containsNode(trace.pipeName, trace.version, data)){
        data += item
      }
    }
    data.toArray
  }

}
