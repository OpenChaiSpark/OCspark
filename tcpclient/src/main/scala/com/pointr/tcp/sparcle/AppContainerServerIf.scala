package com.pointr.tcp.sparcle

import com.pointr.tcp.rpc.{P2pReq, P2pResp, ServerIf, ServerIfConf}
import com.pointr.tcp.util.Logger

class AppContainerServerIf(conf: ServerIfConf) extends ServerIf("AppContainerServer", Option(conf)) with Logger {

  import AppContainerService._

  override def service(req: P2pReq[_]): P2pResp[_] = {
    debug(s"Received request ${req.toString}")
    req match {
      case o: ExecuteAppCommandReq =>
        ExecuteAppCommandResp(s"YogiBearAndBooBooBearCommandResponse to ${o.toString}")
      case o: ExecuteAppCommand2Req =>
        ExecuteAppCommand2Resp(ExecuteAppCommand2Struct(s"YogiBearAndBooBooBearCommand2 response to ${o.toString}", "GPU123"))
      case o: GetAppStatusReq =>
        GetAppStatusResp(s"GPU123 response to ${o.toString}")
      case o: ReadNextFromSparcleReq =>
        ReadNextFromSparcleResp(s"GPU123 response to ${o.toString}")
      case o: WriteAppDataToSparcleReq =>
        WriteAppDataToSparcleResp(s"OK response to ${o.toString}")
      case o: WriteFeedDataToSparcleReq =>
        WriteFeedDataToSparcleResp(s"OK response to ${o.toString}")
      case o: WriteAppNativeDataToSparcleReq =>
        WriteAppNativeDataToSparcleResp(s"SUCCESS response to ${o.toString}")
      case _ => throw new IllegalArgumentException(s"Unknown service type ${req.getClass.getName}")
    }
  }
}
