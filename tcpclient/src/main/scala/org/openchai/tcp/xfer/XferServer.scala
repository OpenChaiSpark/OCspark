package org.openchai.tcp.xfer

import java.nio.file.{Files, Paths}
import java.util.concurrent.atomic.AtomicInteger

import org.openchai.tcp.rpc._

object XferServer {

  var server: TcpServer = _

  def apply(tcpParams: TcpParams) = {
    server = TcpServer(tcpParams.server, tcpParams.port,
      new XferServerIf(tcpParams))
    server
  }

  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1).toInt
    val server = apply(TcpParams(host, port))
    server.start
    Thread.currentThread.join
  }
}

class XferServerIf(tcpParams: TcpParams) extends ServerIF {

  private val nReqs = new AtomicInteger(0)

  import java.security.MessageDigest

  val md = MessageDigest.getInstance("MD5")

  def md5(arr: Array[Byte]) = {
    md.update(arr)
    md.digest
  }

  def writeNio(path: DataPtr, data: RawData) = {
    // Allocating nio is not really necessary but just here
    // to simulate whatever rdma done down the road
    val buf = java.nio.ByteBuffer.allocate(data.length)
    buf.put(data)
    val _md5 = md5(buf.array.slice(0,buf.position))
    val out = Files.write(Paths.get(path), buf.array.slice(0,buf.position))
    (buf, buf.position, _md5)
  }

  def readNio(path: DataPtr) = {
    val len = Files.size(Paths.get(path))
    println(s"Reading path $path of len=$len ..")
    val buf = java.nio.ByteBuffer.allocate(len.toInt)
    buf.put(Files.readAllBytes(Paths.get(path)))
    buf.mark
    val _md5 = md5(buf.array.slice(0,buf.position))
    (buf, len, _md5)
  }

  override def service(req: P2pReq[_]): P2pResp[_] = {
    req match {
      case o: XferWriteReq =>
        val (path,data) = o.value
        val start = System.currentTimeMillis
        val (buf, len, md5) = writeNio(path.toString, data)
        val elapsed = System.currentTimeMillis - start
        XferWriteResp("abc", len, elapsed, md5)

      case o: XferReadReq =>
        val data = o.value
        val start = System.currentTimeMillis
        val (buf, len, md5) = readNio(o.value)
        val elapsed = System.currentTimeMillis - start
        XferReadResp("abc", data.length, elapsed, md5, buf.array)
      case _ => throw new IllegalArgumentException(s"Unknown service type ${req.getClass.getName}")
    }
  }

}