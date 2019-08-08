package com.pointr.tcp.sparcle

import com.pointr.tcp.rpc.{P2pReq, P2pResp, ServerIf, ServerIfConf}
import com.pointr.tcp.util.Logger

class AppContainerServerIf(conf: ServerIfConf) extends ServerIf("AppContainerServer", Option(conf)) with Logger {
  val weightsMergePolicy = conf.props("weightsMergePolicy")

  import AppContainerService._

  override def service(req: P2pReq[_]): P2pResp[_] = {
    debug(s"Received request ${req.toString}")
    req match {
      case o: ExecuteAppCommandReq =>
        ExecuteAppCommandResp("YogiBearAndBooBooBearCommand")
      case o: ExecuteAppCommand2Req =>
        ExecuteAppCommand2Resp(ExecuteAppCommand2Struct("YogiBearAndBooBooBearCommand2", "GPU123"))
      case o: GetAppStatusReq =>
        GetAppStatusResp("GPU123")
      case o: ReadNextFromSparcleReq =>
        ReadNextFromSparcleResp("GPU123")
      case o: WriteAppDataToSparcleReq =>
        WriteAppDataToSparcleResp("OK")
      case o: WriteFeedDataToSparcleReq =>
        WriteFeedDataToSparcleResp("OK")
      case o: WriteAppNativeDataToSparcleReq =>
        WriteAppNativeDataToSparcleResp("SUCCESS")
      case _ => throw new IllegalArgumentException(s"Unknown service type ${req.getClass.getName}")
    }
  }
}
