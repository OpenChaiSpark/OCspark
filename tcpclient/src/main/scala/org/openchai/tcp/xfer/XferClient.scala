package org.openchai.tcp.xfer

import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

import org.openchai.tcp.rpc.{ServiceIF, TcpClient, TcpParams}
import org.openchai.tcp.util.Logger.debug

class XferIf(config: XferConfig) extends ServiceIF("Xfer") {

  private val nReqs = new AtomicInteger(0)

  def write(xferParams: XferWriteParams) = {
    // while (keepGoing(n).value) {
    debug(s"XferIf: Sending request: $xferParams ..")
    val resp = getRpc().request(XferWriteReq(Paths.get(xferParams.config.finalPath).toString,
      xferParams.dataPtr)).asInstanceOf[XferWriteResp]
    debug(s"XferIf: Result is $resp")
    resp
  }

  def read(xferParams: XferReadParams) = {
    // while (keepGoing(n).value) {
    debug(s"Sending request: $xferParams ..")
    val resp = getRpc().request(XferReadReq(xferParams.dataPtr)).asInstanceOf[XferWriteResp]
    debug(s"Result is $resp")
    resp
  }

}

class XferIfClient(tcpParams: TcpParams, config: XferConfig)
  extends TcpClient(tcpParams, new XferIf(config)) {

  val xferIf = serviceIf.asInstanceOf[XferIf]

  def read(params: XferReadParams) = xferIf.read(params)

  def write(params: XferWriteParams) = xferIf.write(params)


}


case class XferParams(configFile: String, data: Array[Byte]) {

  override def toString: String = s"XferParams using $configFile and with datasize=${data.length}"

}
