package com.pointr.tcp.rpc

import com.pointr.tcp.util.{StringUtils, TcpUtils}
import org.scalatest.FunSuite

class SolverTest extends FunSuite {

  test("SolverBasic") {
    System.setProperty("logger.level","3")
    val serverConfPath = "src/main/resources/solver-server.yaml"
    val confPath = "src/main/resources/solver.yaml"
    val maxLoops = ConfParser.parseServiceConf(confPath).props("MaxLoops").asInstanceOf[Int]
    val servers = ServerFactory.create(serverConfPath)
    Thread.sleep(200)
    val client = TcpClient.createClientFromArgs(Array(TcpUtils.getLocalHostname,"" + TcpServer.DefaultPort,confPath))
    val out  = client.serviceIf.run().asInstanceOf[String]
    assert(StringUtils.countOccurrences(out,"EpochResult") == maxLoops, s"SolverTest check1 failed")
  }

}
