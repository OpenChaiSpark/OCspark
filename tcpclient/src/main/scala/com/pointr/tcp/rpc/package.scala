package com.pointr.tcp

import com.pointr.tcp.util.{FileUtils, Logger, TcpUtils}
import com.pointr.tcp.xfer.{DataPtr, RawData}
import com.pointr.tcp.util.Logger._

package object rpc {

  case class ServiceConf(serviceName: String, className: String, props: Map[String, Any], host: String = "localhost", port: Int = 4561, logLevel: Int = 2) {
    if (logLevel != 2) System.setProperty("logger.level", logLevel.toString)
  }

  // Use an abstract class instead of trait to permit interoperability with Java
  abstract class P2pConnectionParams

  val BufSize = (24 * Math.pow(2, 20) - 1).toInt // TODO: change back to 64MB when xgene available
  abstract class ServiceIf(val optConf: Option[ServiceConf]) {

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

  abstract class ServerIf(serverIfName: String, val confOpt: Option[ServerIfConf] = None) {

    //    def name: String = confOpt.map( _.serviceName).getOrElse(_name)
    //    private var _name: String = "emptyName"
    //
    //    confOpt.map { conf =>
    //      this._name
    //    }
    //    @deprecated
    //    def this(_name: String) = {
    //      this(None)
    //      this._name = _name
    //    }

    def name() = confOpt.map { conf =>
      conf.serviceName
    }.getOrElse(this.serverIfName)

    def service(req: P2pReq[_]): P2pResp[_]
  }

  abstract class P2pMessage[T] extends _root_.java.io.Serializable {
    def path(): DataPtr = getClass.getName

    def value(): T
  }

  abstract class P2pReq[T] extends P2pMessage[T] with _root_.java.io.Serializable

  abstract class P2pResp[T] extends P2pMessage[T] with _root_.java.io.Serializable

  val MagicNumber: String = "MyNameIsFozzieBear"

  trait ArrayData[V] {
    def tag: String

    def dims: Seq[Int]

    def toArray: V
  }

  import reflect.runtime.universe._

  abstract class XferReq[T: TypeTag](val value: T) extends P2pReq[T]

  abstract class XferResp[T: TypeTag](val value: T) extends P2pResp[T]

  type DArray = Array[Double]

  case class MData(override val tag: String, override val dims: Seq[Int], override val toArray: DArray) extends ArrayData[DArray]

  type AnyData = MData

  case class TData(label: Double, data: Vector[Double])

}
