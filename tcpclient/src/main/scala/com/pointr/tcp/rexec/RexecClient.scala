package com.pointr.tcp.rexec

import com.pointr.tcp.rpc.TcpClient

object RexecClient {
  def main(args: Array[String]): Unit = {
    System.setProperty("java.net.preferIPv4Stack","true")
    val client = TcpClient.createClientFromArgs(if (args.length>0) args else Array("localhost","8989","rexec-client.yaml"))
    client.serviceIf.run()
  }
}
