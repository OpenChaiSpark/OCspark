package com.pointr.tcp

import com.pointr.tcp.rpc.{SolverServerIf, TcpClient, TcpServer}
import com.pointr.tcp.util.TcpUtils

object TcpExample {

  val weightsMergePolicy: String = "best"
  val TestPort = TcpServer.DefaultPort

  def main(args: Array[String]) {
    System.setProperty("logger.level","3")
    val server = TcpServer(TcpUtils.getLocalHostname, TestPort, new SolverServerIf(weightsMergePolicy))
    server.start
    TcpClient.main(Array(TcpUtils.getLocalHostname,"" + TestPort))
    Thread.currentThread.join
  }
}
