package com.pointr.tcp

import com.pointr.tcp.rpc.{ServerFactory, SolverServerIf, TcpClient, TcpServer}
import com.pointr.tcp.util.TcpUtils

object TcpExample {

  def run(args: Array[String]) {
    System.setProperty("logger.level","3")
    val confPath = "src/main/resources/solver-server.yaml"
    val servers = ServerFactory.create(confPath)
    Thread.sleep(200)
    val client = TcpClient.runClientFromArgs(Array(TcpUtils.getLocalHostname,"" + TcpServer.DefaultPort))
    val iter = client.serviceIf.run(Seq.empty[Any])
    Thread.sleep(20*1000)
  }

  def main(args: Array[String]): Unit = {
    run(args)
  }
}
