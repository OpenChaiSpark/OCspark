package com.pointr.tcp.rpc

import com.pointr.tcp.util.{Logger, TcpUtils}

abstract class ServiceIf(val optConf: Option[ServiceConf]) extends Logger {

  @deprecated
  def this(_name: String) = {
    this(None)
  }

  val conf = optConf.get

  import reflect.runtime.universe.TypeTag

  def request[U: TypeTag, V: TypeTag](req: P2pReq[U]): P2pResp[V] = {
    getRpc.request(req)
  }

  protected[tcp] var optRpc: Option[P2pRpc] = None

  protected def getRpc() = optRpc match {
    case None => throw new IllegalStateException("RPC mechanism has not been set")
    case _ => optRpc.get
  }

  val clientName = try {
    TcpUtils.getLocalHostname
  } catch {
    case e: Exception =>
      error(s"Failed getLocalHostname", e)
      "localhost"
  }

  def configure() = {}

  def init() = {}

  def run(): Any = {}

  def run(args: Seq[Any]): Any = {}

  def stop() = {}

  lazy val name: String = optConf.map(_.serviceName).getOrElse(_name)

  private[this] var _name: String = ""

}
