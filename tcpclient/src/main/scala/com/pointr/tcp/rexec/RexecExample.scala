package com.pointr.tcp.rexec

import com.pointr.tcp.rpc.{TcpClient, TcpParams, TcpServer}

object RexecExample {
  def main(args: Array[String]) = {
    val tcpParams = TcpParams("localhost", TcpServer.DefaultPort)
    val rexecServer = RexecServer(tcpParams, "ls -lrta /git")
    rexecServer.start
    val client = TcpClient.createClientFromArgs(Array("localhost", "8989", "src/main/resources/rexec-client.yaml"))
    client.serviceIf.run()
    Thread.currentThread().join
  }
}
