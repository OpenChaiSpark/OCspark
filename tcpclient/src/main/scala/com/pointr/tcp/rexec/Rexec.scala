package com.pointr.tcp.rexec

import java.util.concurrent.atomic.AtomicInteger

import com.pointr.tcp.rpc._
import com.pointr.tcp.util.Logger._
import com.pointr.tcp.util.{ExecParams, ExecResult, ProcessUtils}

class RexecServerIf(conf: ServerIfConf) extends ServerIf("RexecServerIf", Option(conf)) {

//  val rexecIf = new RexecIf

  private val nReqs = new AtomicInteger(0)

  override def service(req: P2pReq[_]): P2pResp[_] = {
    req match {
      case o: RexecReq => {
        val eres = ProcessUtils.exec(o.rexec.execParams)
        RexecResp(eres)
      }
      case _ => throw new IllegalArgumentException(s"Unknown service type ${req.getClass.getName}")
    }
  }

}

object RexecServer {

  val template = """host: $$HOST
port: $$PORT
serviceName: Rexec
className : com.pointr.tcp.rpc.RexecServerIf
props:
    cmdline: $$CMDLINE
"""

  def apply(tcpParams: TcpParams, cmdLine: String) = {
    System.setProperty("java.net.preferIPv4Stack","true")
    val confStr = template.replace("$$HOST",tcpParams.server).replace("$$PORT",tcpParams.port.toString).replace("$$CMDLINE",cmdLine)
    val conf = ConfParser.parseServerIfConf(confStr)
    TcpServer(tcpParams.server, tcpParams.port, new RexecServerIf(conf))
  }

  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1).toInt
    val server = apply(TcpParams(host, port), "ls -lrta /git")
    server.start
    Thread.currentThread.join
  }
}

