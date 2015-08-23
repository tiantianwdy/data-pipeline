package au.com.nicta.data.pipeline.core.executor

/**
 * Created by tiantian on 17/08/15.
 */
trait ExecutionOption extends Serializable{
  
}

object ExecutionOption{
  
  case object Test extends ExecutionOption

  case object Run extends ExecutionOption

  case object PseudoRun extends ExecutionOption
}

