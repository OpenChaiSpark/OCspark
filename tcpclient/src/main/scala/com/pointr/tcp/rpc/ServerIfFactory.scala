package com.pointr.tcp.rpc

import com.pointr.tcp.rpc.SolverIf.{DefaultHyperParams, DefaultModel, ModelParams}
import com.pointr.tcp.util.{Logger, ReflectUtils}

object ServerIfFactory extends Logger {

  def createIf(confPath: String): SolverServerIf = {
    val conf = ConfParser.parseServerConf(confPath)
    createIf(conf.serverServiceIfs.head._1, conf.serverServiceIfs.head._2)
  }

  def createIf(serviceName: String, serviceConf: ServerIfConf): SolverServerIf = {
    val className = serviceConf.className
    info(s"Creating $className for ServiceIF $serviceName ..")
    className.substring(className.lastIndexOf(".")+1) match {
      case "SolverServerIf" =>
        val solver = ReflectUtils.instantiate(className)(serviceConf).asInstanceOf[SolverServerIf]
        solver
      case _ => throw new UnsupportedOperationException(s"Unsupported ServerServiceIf $className")
    }
  }
}
