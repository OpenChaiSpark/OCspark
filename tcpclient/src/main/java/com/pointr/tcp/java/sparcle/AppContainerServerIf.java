package com.pointr.tcp.java.sparcle;
import com.pointr.tcp.rpc.*;
import com.pointr.tcp.rpc.ServerIf;

import com.pointr.tcp.java.sparcle.AppContainerService;
import com.pointr.tcp.java.sparcle.AppContainerService.*;
import com.pointr.tcp.sparcle.AppContainerService.WriteAppDataToSparcleStruct;
import com.pointr.tcp.sparcle.AppContainerService.WriteFeedDataToSparcleStruct;
import com.pointr.tcp.sparcle.AppContainerService.WriteAppNativeDataToSparcleStruct;
import scala.Option;

public class AppContainerServerIf extends ServerIf {

  public AppContainerServerIf(String serverIfName, Option<ServerIfConf> confOpt) {

    super(serverIfName, confOpt);
  }

  @Override
  public P2pResp<?> service(P2pReq<?> req) {
    if (req instanceof AppContainerService.ExecuteAppCommandReq) {
      return new ExecuteAppCommandResp(String.format("YogiBearAndBooBooBearCommandResponse to %s",req.toString()));
    } else if (req instanceof AppContainerService.ExecuteAppCommand2Req) {
      return new ExecuteAppCommand2Resp(new ExecuteAppCommand2JavaStruct(String.format("YogiBearAndBooBooBearCommand2Response to %s",req.toString()),"GPU123"));
    }  else if (req instanceof AppContainerService.GetAppStatusReq) {
      return new GetAppStatusResp(String.format("GPU123 Response to %s",req.toString()));
    }   else if (req instanceof AppContainerService.IsAppRunningReq) {
      return new IsAppRunningResp(true);
    } else if (req instanceof AppContainerService.ReadNextFromSparcleReq) {
      return new ReadNextFromSparcleResp(String.format("ReadNext Resp to %s",req.toString()));
    } else if (req instanceof AppContainerService.WriteAppDataToSparcleReq) {
      return new WriteAppDataToSparcleResp(new WriteAppDataToSparcleStruct("GPURefid",String.format("AppData Response to %s",req.toString()).getBytes()));
    } else if (req instanceof AppContainerService.WriteFeedDataToSparcleReq) {
      return new WriteFeedDataToSparcleResp(new WriteFeedDataToSparcleStruct("GPURefid",String.format("Feed Response to %s",req.toString()).getBytes()));
    } else if (req instanceof AppContainerService.WriteAppNativeDataToSparcleReq) {
      return new WriteAppNativeDataToSparcleResp(new WriteAppNativeDataToSparcleStruct("GPURefid",
          String.format("AppNativeData Response to %s",req.toString()).getBytes()));
    } else {
      throw new UnsupportedOperationException("What type of request is this: %s".format(req.toString()));
    }
  }
}
