package com.pointr.tcp

import com.pointr.tcp.rpc.{SolverServerIf, TcpClient, TcpServer}
import com.pointr.tcp.util.TcpUtils
import org.scalatest.FunSuite

class BasicTcp extends FunSuite {

  test("basic") {
    val weightsMergePolicy: String = "best"
    val TestPort = TcpServer.DefaultPort

    System.setProperty("logger.level", "3")
//    val server = TcpServer(TcpUtils.getLocalHostname, TestPort, new SolverServerIf(weightsMergePolicy))
    TcpExample.run(Array.empty[String])
  }
}
