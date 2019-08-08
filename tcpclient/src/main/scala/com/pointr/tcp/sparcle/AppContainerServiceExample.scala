package com.pointr.tcp.sparcle

import com.pointr.tcp.rpc.{ServerFactory, ServiceConf, ServiceIf, TcpClient, TcpServer}
import com.pointr.tcp.util.{Logger, TcpUtils}

object AppContainerServiceExample {

  def run(args: Array[String]) {
    System.setProperty("logger.level","3")
    val serverConfPath = "src/main/resources/appContainer-server.yaml"
    val confPath = "src/main/resources/appContainer.yaml"
    val servers = ServerFactory.create(serverConfPath)
    Thread.sleep(200)
    val client = TcpClient.createClientFromArgs(Array(TcpUtils.getLocalHostname,"" + TcpServer.DefaultPort,confPath))
    val iter = client.serviceIf.run()
    Thread.sleep(20*1000)
  }

  def main(args: Array[String]): Unit = {
    run(args)
  }

}
