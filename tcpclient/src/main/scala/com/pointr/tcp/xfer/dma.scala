package com.pointr.tcp.xfer

import com.pointr.tcp.rpc.TcpParams
import com.pointr.tcp.util.Logger

// placeholder: Burcak will inform what more needed
case class DataXferConf(name: String)

class DataXferConClient(val dataConfig: DataXferConf, val tcpParams: TcpParams, val xferTcpParams: TcpParams, val config: XferConfig) extends XferConIf with XferIfClient {
  val controller = XferConClient(tcpParams, xferTcpParams, config)
  val tcpXferIf = controller.tcpXferIf
//  val dataXferIf = new DataXferIfClient(new DataParams(dataConfig.name), dataConfig, dataXferIfTcpParams, config)
  controller.xferIf = tcpXferIf  // TODO: decide how to do switching tcp/data

  override def read(params: XferReadParams): XferReadResp =
    controller.xferIf.read(params)

  override def write(params: XferWriteParams): XferWriteResp = {
     controller.xferIf.write(params)
  }

  override def prepareWrite(config: XferConfig): PrepResp = controller.xferConIf.prepareWrite(config)

  override def completeWrite(config: XferConfig): CompletedResp = controller.xferConIf.completeWrite(config)

  override def prepareRead(params: XferConfig): PrepResp = controller.xferConIf.prepareRead(params)

  override def completeRead(config: XferConfig): CompletedResp = controller.xferConIf.completeRead(config)

}

object DataXferConClient extends Logger {

  import XferConCommon._
  case class DataXferControllers(client: DataXferConClient, xferConf: XferConfig,
    wparams: XferWriteParams, rparams: XferReadParams)

  def makeDataXferControllers(args: XferControllerArgs): DataXferControllers = {
    debug(s"makeDataXferControllers: Connecting controllers on $args")
    try {
      val tcpParams = TcpParams(args.conHost, args.conPort)
      val xtcpParams = TcpParams(args.dataHost, args.dataPort)
      val xferConf = TcpXferConfig(args.outboundDataPaths._1, args.outboundDataPaths._2)
      val client = new DataXferConClient(DataXferConf("tbd"), tcpParams, xtcpParams, xferConf)
      val wparams = XferWriteParams("WriteParams", xferConf, args.data)
      val rparams = XferReadParams("ReadParams", xferConf, args.inboundDataPath)
      DataXferControllers(client, xferConf, wparams, rparams)
    } catch {
      case e: Exception =>
        error(s"Connection error in makeDataXferControllers for $args", e)
        throw e
    }
  }

}

