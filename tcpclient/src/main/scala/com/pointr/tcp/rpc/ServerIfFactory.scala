package com.pointr.tcp.rpc

import com.pointr.tcp.rpc.SolverIf.{DefaultHyperParams, DefaultModel, ModelParams}
import com.pointr.tcp.util.{Logger, ReflectUtils}

object ServerIfFactory extends Logger {

  def createIf(confPath: String): SolverServerIf = {
    val conf = ConfParser.parseServerConf(confPath)
    createIf(conf.serverServices.head._1, conf.serverServices.head._2)
  }

  def createIf(serviceName: String, serviceConf: ServerIfConf): SolverServerIf = {
    val className = serviceConf.className
    info(s"Creating $className for ServiceIF $serviceName ..")
    serviceName match {
      case "SolverServer" =>
        val solver = ReflectUtils.instantiate(className)(serviceConf).asInstanceOf[SolverServerIf]
        solver
      case _ => throw new UnsupportedOperationException(s"Unsupported ServerServiceIf $className")
    }
  }
}
