package com.pointr.tcp.rpc

import com.pointr.tcp.util.{Logger, ReflectUtils}

object ServerFactory extends Logger {

  def create(confPath: String) = {
    val conf = ConfParser.parseServerConf(confPath)

    val servers = for ((serverIfName, serverIfConf) <- conf.serverServiceIfs) yield {
      info(s"ServerFactory: creating serverIf=$serverIfName..")
      val serverIf = ServerIfFactory.createIf(serverIfName,serverIfConf)
      val server = TcpServer(conf.host, conf.port, serverIf)
      server.start()
    }
    servers
  }
}
