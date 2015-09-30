package au.com.nicta.data.pipeline.core.utils

import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by tiantian on 30/09/15.
 */
trait Logging {

  @transient
  private var _log: Logger = null

  protected def log: Logger = {
    if(_log eq null){
      var className = this.getClass.getName
      if(className.endsWith("$")){
        className = className.substring(0,className.length -1)
      }
      _log = LoggerFactory.getLogger(className)
    }
    _log
  }
}
