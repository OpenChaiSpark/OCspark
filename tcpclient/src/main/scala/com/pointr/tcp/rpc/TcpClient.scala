package com.pointr.tcp.rpc

import com.pointr.tcp.rexec.RexecIf
import com.pointr.tcp.util.Logger._
import com.pointr.tcp.util.TcpCommon._
import com.pointr.tcp.util.{FileUtils, ReflectUtils, TcpUtils, YamlUtils}

case class TcpParams(server: String, port: Int) extends P2pConnectionParams

//class BinaryTcpClient(connParams: TcpParams) extends TcpClient(connParams, new BinaryIf)

class TcpClient(val connParams: TcpParams, val serviceIf: ServiceIf)
  extends P2pRpc with P2pBinding {

  import java.io._
  import java.net._

  import reflect.runtime.universe._

  private var sock: Socket = _
  private var os: DataOutputStream = _
  private var is: DataInputStream = _

  val MaxTcpWaitSecs = 2

  {
    connect(connParams)
  }

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
  private var savedConnParam: P2pConnectionParams = _

  val buf = new Array[Byte](BufSize)
  override def request[U: TypeTag, V: TypeTag](req: P2pReq[U]): P2pResp[V] = {
    // TODO: determine how to properly size the bos
    if (!isConnected) {
      connect(savedConnParam)
    }
    val serreq = serializeStream(req.path, pack(req.path, req))
    val b1 = MagicNumber.getBytes("ISO-8859-1")
    os.write(b1)
    os.writeInt(serreq.length)
    os.write(serreq)
    val sent = serreq.length
    os.flush
    debug(s"Wrote $sent bytes to output")

    val b = new Array[Byte](MagicNumber.length)
    val magicNumberLen = is.read(b)
    val magicNumber = new String(b,"ISO-8859-1")
    if (magicNumberLen != MagicNumber.length || magicNumber != MagicNumber) {
      throw new IllegalStateException(s"MagicNumber mismatch: len=$magicNumberLen and magicNumber=$magicNumber")
    }

    var totalRead = 0
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

  System.setProperty("java.net.preferIPv4Stack","true")

  def createClient(host: String, port: Int, serviceIf: ServiceIf) = {
    val client = new TcpClient(TcpParams(host, port), serviceIf)
    client
  }

  def runClient(client: TcpClient) = {
    val ret = client.serviceIf.run()
    ret
  }

  def serviceConfFromPath(serviceConfPath: String) = {
    println(s"Creating service from $serviceConfPath..")
    val service = YamlUtils.toScala[ServiceConf](FileUtils.readFileAsString(serviceConfPath))
    service
  }

  def serviceFromConf(serviceConf: ServiceConf) = {
    val className = serviceConf.className
    val serviceName = serviceConf.serviceName
    info(s"Creating $className for ServiceIF $serviceName ..")
    val service = serviceName match {
      case "SolverIf" =>
        val service = ReflectUtils.instantiate(className)(serviceConf).asInstanceOf[SolverIf]
        service
      case "Rexec" =>
        val service = ReflectUtils.instantiate(className)(serviceConf).asInstanceOf[RexecIf]
        service
      case _ => throw new UnsupportedOperationException(s"Unsupported ServiceIf $className")
    }
    service.asInstanceOf[ServiceIf]
  }

  def createClientFromArgs(args: Array[String]) = {
    val Array(host, sPort, serviceConfPath) = args
    val port = sPort.toInt
    val serviceConf = serviceConfFromPath(serviceConfPath)
    val service = serviceFromConf(serviceConf)
    val client = createClient(host, port, service)
    client
  }

  def main(args: Array[String]) {
    val client = createClientFromArgs(args)
    val ret = runClient(client)
    Thread.currentThread.join
  }
}
