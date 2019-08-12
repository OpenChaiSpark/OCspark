package com.pointr.tcp.rpc

import com.pointr.tcp.rpc.SolverIf.{DefaultHyperParams, DefaultModel, ModelParams}
import com.pointr.tcp.util.{Logger, ReflectUtils}

object ServerIfFactory extends Logger {

  def createIf(confPath: String): ServerIf = {
    val conf = ConfParser.parseServerConf(confPath)
    createIf(conf.serverServices.head._1, conf.serverServices.head._2)
  }

  def createIf(serviceName: String, serviceConf: ServerIfConf): ServerIf = {
    val className = serviceConf.className
    info(s"Creating $className for ServiceIF $serviceName ..")
    serviceName match {
      case "SolverServer" =>
        val solver = ReflectUtils.instantiate(className)(serviceConf).asInstanceOf[SolverServerIf]
        solver
      case _ =>
        val solver = ReflectUtils.instantiate(className)(serviceName, Option(serviceConf)).asInstanceOf[ServerIf]
        solver
//        throw new UnsupportedOperationException(s"Unsupported ServerServiceIf $className")
    }
  }
}
