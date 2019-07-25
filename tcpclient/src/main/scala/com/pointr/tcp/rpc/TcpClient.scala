package com.pointr.tcp.rpc

import com.pointr.tcp.util.Logger._
import com.pointr.tcp.util.TcpCommon._
import com.pointr.tcp.util.TcpUtils

case class TcpParams(server: String, port: Int) extends P2pConnectionParams

//class BinaryTcpClient(connParams: TcpParams) extends TcpClient(connParams, new BinaryIf)

class TcpClient(val connParams: TcpParams, val serviceIf: ServiceIf)
  extends Thread with P2pRpc with P2pBinding {

  import java.io._
  import java.net._

  import reflect.runtime.universe._

  private var sock: Socket = _
  private var os: DataOutputStream = _
  private var is: DataInputStream = _

  val MaxTcpWaitSecs = 2

  override def isConnected: Boolean = sock.isConnected && is != null && os != null

  override def connect(connParam: P2pConnectionParams): Boolean = {
    try {
      savedConnParam = connParam
      val tconn = connParams.asInstanceOf[TcpParams]
      info(s"TcpClient: Connecting ${serviceIf.name} to ${tconn.server}:${tconn.port} ..")
      sock = new Socket(tconn.server, tconn.port)
      os = new DataOutputStream(sock.getOutputStream)
      is = new DataInputStream(sock.getInputStream)
      bind(this, serviceIf)
      info(s"TcpClient: Bound ${serviceIf.name} to ${tconn.server}:${tconn.port}")
      is != null && os != null
    } catch {
      case e: Exception =>
        error(s"Error Connecting to $connParam", e)
        throw e
    }
  }


  override def run(): Unit = {
    connect(connParams)
    Thread.currentThread.join()
  }

  private var savedConnParam: P2pConnectionParams = _

  val buf = new Array[Byte](BufSize)
  override def request[U: TypeTag, V: TypeTag](req: P2pReq[U]): P2pResp[V] = {
    // TODO: determine how to properly size the bos
    if (!isConnected) {
      connect(savedConnParam)
    }
    val serreq = serializeStream(req.path, pack(req.path, req))
    os.writeInt(serreq.length)
    os.write(serreq)
    val sent = serreq.length
    os.flush
    debug(s"Wrote $sent bytes to output")

    var totalRead = 0
    //      val available = dis.available
    //      if (available <= 0) {
    //        Thread.sleep(200)
    //      } else {
    val bytesToRead = is.readInt
    debug(s"Client BytesToRead = $bytesToRead")
    do {
      val nread = is.read(buf, totalRead, buf.length - totalRead)
      totalRead += nread
      debug(s"${getClass.getSimpleName}: in loop: nread=$nread totalRead=$totalRead bytesToRead=$bytesToRead")
      Thread.sleep(20)
    } while (totalRead < bytesToRead)
    //      }
    debug(s"TcpClient.Request: totalSent=$sent totalRcvd=$totalRead")
    val o = unpack("/tmp/clientReq.out", buf.slice(0, totalRead))
    //    val (path, o, md5) = unpack(buf.slice(0,nread))
    val out = o.asInstanceOf[P2pResp[V]]
    if (reconnectEveryRequest) {
      sock.close
      sock = null
      os = null
      is = null
    }
    out
  }

}

object TcpClient {
  val TestPort = 8989

  def runClient(server: String, port: Int, serviceIf: ServiceIf) = {
    val client = new TcpClient(TcpParams(server, port), serviceIf)
    client.start()
    client
  }

  def serviceFromConf(serviceConfPath: String) = {
    println(s"Creating service from $serviceConfPath..")
      // YamlStruct ..
    new SolverIf
  }

  def main(args: Array[String]) {
    val server = if (args.length >= 1) args(0) else TcpUtils.getLocalHostname
    val port = if (args.length >= 2) args(1).toInt else TestPort
    val serviceConf = if (args.length >= 3) args(2) else "solver.yml"
    val serviceIf = serviceFromConf(serviceConf)
    val client = runClient(server, port, serviceIf)
  }
}
