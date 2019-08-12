package com.pointr.tcp.java.sparcle;

import com.pointr.tcp.rpc.P2pReq;
import com.pointr.tcp.rpc.P2pResp;
import com.pointr.tcp.rpc.*;
import com.pointr.tcp.sparcle.AppContainerService.ExecuteAppCommand2Struct;
import com.pointr.tcp.sparcle.AppContainerService.WriteAppDataToSparcleStruct;
import com.pointr.tcp.sparcle.AppContainerService.WriteFeedDataToSparcleStruct;
import com.pointr.tcp.sparcle.AppContainerService.WriteAppNativeDataToSparcleStruct;
import scala.Option;
import scala.collection.Seq;
import scala.reflect.api.TypeTags;

import java.io.Serializable;
import java.util.ArrayList;

public class AppContainerService extends ServiceIf {

  ServiceConf conf;

  public AppContainerService(ServiceConf conf) {
    super(Option.apply(conf));
    this.conf = conf;
  }

  public static class ExecuteAppCommand2JavaStruct implements Serializable {
    String command;
    public String GPURefId;

    public ExecuteAppCommand2JavaStruct(String command, String GPURefId) {
      this.command = command;
      this.GPURefId = GPURefId;
    }
  }

  public ExecuteAppCommandResp executeAppCommand(ExecuteAppCommandReq req) {
    return (ExecuteAppCommandResp) getRpc().<String,String>requestJava (req);
  }

  public ExecuteAppCommand2Resp executeAppCommand2(ExecuteAppCommand2Req req) {
    return (ExecuteAppCommand2Resp) getRpc().<ExecuteAppCommand2JavaStruct,ExecuteAppCommand2JavaStruct>requestJava (req);
  }

  public GetAppStatusResp getAppStatus(GetAppStatusReq req) {
    return (GetAppStatusResp) getRpc().<String,String>requestJava (req);
  }

  public IsAppRunningResp isAppRunning(IsAppRunningReq req) {
    return (IsAppRunningResp) getRpc().<Boolean,Boolean>requestJava (req);
  }

  public ReadNextFromSparcleResp readNextFromSparcle(ReadNextFromSparcleReq req) {
    return (ReadNextFromSparcleResp) getRpc().<String,String>requestJava (req);
  }

  public WriteAppDataToSparcleResp writeAppDataToSparcle(WriteAppDataToSparcleReq req) {
    return (WriteAppDataToSparcleResp) getRpc().<WriteAppDataToSparcleStruct,WriteAppDataToSparcleStruct>requestJava (req);
  }

  public WriteFeedDataToSparcleResp writeFeedDataToSparcle(WriteFeedDataToSparcleReq req) {
    return (WriteFeedDataToSparcleResp) getRpc().<com.pointr.tcp.sparcle.AppContainerService.WriteFeedDataToSparcleStruct, com.pointr.tcp.sparcle.AppContainerService.WriteFeedDataToSparcleStruct>requestJava (req);
  }

  public WriteAppNativeDataToSparcleResp writeAppNativeDataToSparcle(WriteAppNativeDataToSparcleReq req) {
    return (WriteAppNativeDataToSparcleResp) getRpc().<com.pointr.tcp.sparcle.AppContainerService.WriteAppNativeDataToSparcleStruct, com.pointr.tcp.sparcle.AppContainerService.WriteAppNativeDataToSparcleStruct>requestJava (req);
  }

  public static class ExecuteAppCommandReq extends P2pReq<String> {
    public String value;

    @Override
    public String value() {
      return value;
    }

    public ExecuteAppCommandReq(String value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class ExecuteAppCommandResp extends P2pResp<String> {
    private  String value;

    @Override
    public String value() {
      return value;
    }

    public ExecuteAppCommandResp(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + ": " + value;
    }
  }

  public static class ExecuteAppCommand2Req extends P2pReq<ExecuteAppCommand2JavaStruct> {
    private ExecuteAppCommand2JavaStruct  value;

    @Override
    public ExecuteAppCommand2JavaStruct value() {
      return value;
    }

    public ExecuteAppCommand2Req(ExecuteAppCommand2JavaStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class ExecuteAppCommand2Resp extends P2pResp<ExecuteAppCommand2JavaStruct> {
    public ExecuteAppCommand2JavaStruct value;

    @Override
    public ExecuteAppCommand2JavaStruct value() {
      return value;
    }

    public ExecuteAppCommand2Resp(ExecuteAppCommand2JavaStruct value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + ": " + value;
    }

  }

  public static class GetAppStatusReq extends P2pReq<String> {
    public String value;

    @Override
    public String value() {
      return value;
    }

    public GetAppStatusReq(String value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class GetAppStatusResp extends P2pResp<String> {
    private  String value;

    @Override
    public String value() {
      return value;
    }

    public GetAppStatusResp(String value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }
  }

  public static class IsAppRunningReq extends P2pReq<Boolean> {
    public Boolean value;

    @Override
    public Boolean value() {
      return value;
    }

    public IsAppRunningReq(Boolean value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class IsAppRunningResp extends P2pResp<Boolean> {
    private  Boolean value;

    @Override
    public Boolean value() {
      return value;
    }

    public IsAppRunningResp(Boolean value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }
  }

  public static class ReadNextFromSparcleReq extends P2pReq<String> {
    private  String value;

    @Override
    public String value() {
      return value;
    }

    public ReadNextFromSparcleReq(String value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }
  }

  public static class ReadNextFromSparcleResp extends P2pResp<String> {
    public String value;

    @Override
    public String value() {
      return value;
    }

    public ReadNextFromSparcleResp(String value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class WriteAppDataToSparcleReq extends P2pReq<WriteAppDataToSparcleStruct> {
    private WriteAppDataToSparcleStruct  value;

    @Override
    public WriteAppDataToSparcleStruct value() {
      return value;
    }

    public WriteAppDataToSparcleReq(WriteAppDataToSparcleStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class WriteAppDataToSparcleResp extends P2pResp<WriteAppDataToSparcleStruct> {
    public WriteAppDataToSparcleStruct value;

    @Override
    public WriteAppDataToSparcleStruct value() {
      return value;
    }

    public WriteAppDataToSparcleResp(WriteAppDataToSparcleStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class WriteFeedDataToSparcleReq extends P2pReq<WriteFeedDataToSparcleStruct> {
    private WriteFeedDataToSparcleStruct value;

    @Override
    public WriteFeedDataToSparcleStruct value() {
      return value;
    }

    public WriteFeedDataToSparcleReq(WriteFeedDataToSparcleStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class WriteFeedDataToSparcleResp extends P2pResp<WriteFeedDataToSparcleStruct> {
    public WriteFeedDataToSparcleStruct value;

    @Override
    public com.pointr.tcp.sparcle.AppContainerService.WriteFeedDataToSparcleStruct value() {
      return value;
    }

    public WriteFeedDataToSparcleResp(WriteFeedDataToSparcleStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class WriteAppNativeDataToSparcleReq extends P2pReq<WriteAppNativeDataToSparcleStruct> {
    private WriteAppNativeDataToSparcleStruct value;

    @Override
    public WriteAppNativeDataToSparcleStruct value() {
      return value;
    }

    public WriteAppNativeDataToSparcleReq(WriteAppNativeDataToSparcleStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  public static class WriteAppNativeDataToSparcleResp extends P2pResp<WriteAppNativeDataToSparcleStruct> {
    public WriteAppNativeDataToSparcleStruct value;

    @Override
    public WriteAppNativeDataToSparcleStruct value() {
      return value;
    }

    public WriteAppNativeDataToSparcleResp(WriteAppNativeDataToSparcleStruct value) {
      this.value = value;
    }

    @Override
    public String toString() { return getClass().getSimpleName() + ": " + value; }

  }

  @Override
  public Object run(Seq<Object> args) {
    return run();
  }

  @Override
  public Object run() {
    ArrayList<String> outs = new ArrayList();
    outs.add(executeAppCommand(new ExecuteAppCommandReq("YogiBear - run!!")).toString());
    outs.add(executeAppCommand2(new ExecuteAppCommand2Req(new ExecuteAppCommand2JavaStruct("YogiBear - run more!!", "Gpu123"))).toString());
    outs.add(getAppStatus(new GetAppStatusReq("Here's My Status!")).toString());
    outs.add(isAppRunning(new IsAppRunningReq(true)).toString());
    outs.add(readNextFromSparcle(new ReadNextFromSparcleReq("Next one from sparcle!")).toString());
    outs.add(writeAppDataToSparcle(new WriteAppDataToSparcleReq(new WriteAppDataToSparcleStruct("Gpu123", "Write app Data buffer stuff".getBytes() ))).toString());
    outs.add(writeFeedDataToSparcle(new WriteFeedDataToSparcleReq(new WriteFeedDataToSparcleStruct("Gpu123", "Write feed data buffer stuff".getBytes() ))).toString());
    outs.add(writeAppNativeDataToSparcle(new WriteAppNativeDataToSparcleReq(new WriteAppNativeDataToSparcleStruct("Gpu123", "Write appNative Data buffer stuff".getBytes() ))).toString());
    return String.join("\n",outs);
  }

}
