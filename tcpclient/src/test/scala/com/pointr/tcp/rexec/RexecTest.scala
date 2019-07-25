package com.pointr.tcp.rexec

import com.pointr.tcp.rpc.{TcpParams, TcpServer}
import com.pointr.tcp.util.ExecParams
import com.pointr.tcp.util.Logger._

object RexecTest {
  def main(args: Array[String]) = {
    val server = args(0)
    val tcpParams = TcpParams(server, TcpServer.DefaultPort)
    val rexecServer = RexecServer(tcpParams)
    rexecServer.start
    val rexecClient = RexecTcpClient(tcpParams)
      val res = rexecClient.run(RexecParams(ExecParams("ls","ls",Some("-lrta .".split(" ")),None,"/etc/pam.d")), 5)
    info(s"Result: $res")
  }
}
