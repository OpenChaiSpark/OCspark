package com.pointr.tcp.rpc

case class ServiceConf(serviceName: String, className: String, props: Map[String, Any], host: String = "localhost", port: Int = 4561, logLevel: Int = 2) {
//    if (logLevel != 2) {
    System.setProperty("logger.level", logLevel.toString)
//    }
}
