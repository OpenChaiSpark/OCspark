package com.pointr.tensorflow.api;

import static com.pointr.tensorflow.api.JsonUtils.toJson;

public class DMAServerBase implements TensorFlowIf.DMAServer {
  public static class Result {
    public Result(String fn, int rc, String msg) {
      this.fn = fn;
      this.rc = rc;
      this.msg = msg;
    }

    public String fn;
    public int rc;
    public String msg;
  }

  public String setupChannel(String setupJson) {
    Logger.info(Logger.f("SetupChannel for %s", setupJson));
    return toJson(new Result("SetupChannel", 1, "Foo"));
  }

  public String register(TensorFlowIf.DMACallback callbackIf) {
    Logger.info(Logger.f("register DMACallback invoked for ", callbackIf.getClass().getName()));
    return toJson("register not implemented");
  }

  @Override
  public String prepareWrite(String configJson) {
    Logger.info(Logger.f("prepareWrite for %s", configJson));
    return toJson(Logger.f("prepareWrite completed for %s", configJson));
  }

  @Override
  public DMAStructures.WriteResultStruct completeWrite(String configJson) {
    Logger.info(Logger.f("completeWrite  for %s", configJson));
    return new DMAStructures.WriteResultStruct();
  }

  @Override
  public DMAStructures.WriteResultStruct write(String configJson, byte[] dataPtr) {
    Logger.info(Logger.f("write for %s and dataLen=%d", configJson, dataPtr.length));
    return new DMAStructures.WriteResultStruct();
  }

  @Override
  public String prepareRead(String configJson) {
    Logger.info(Logger.f("prepareRead for %s", configJson));
    return toJson(Logger.f("prepareRead completed for %s", configJson));
  }

  public DMAStructures.ReadResultStruct read(String configJson) {
    Logger.info(Logger.f("read for %s", configJson));
    return new DMAStructures.ReadResultStruct();
  }

  @Override
  public DMAStructures.ReadResultStruct completeRead(String configJson) {
    Logger.info(Logger.f("completeRead for %s", configJson));
    return new DMAStructures.ReadResultStruct();
  }

  public String shutdownChannel(String shutdownJson) {
    Logger.info(Logger.f("ShutdownCannel for %s", shutdownJson));
    return toJson(Logger.f("ShutdownChannel completed for %s", shutdownJson));
  }

  public byte[] readLocal(byte[] dataPtr) {
    Logger.info(Logger.f("readLocal for datalen=%d", dataPtr.length));
    return toJson("readLocal not implemented").getBytes();
  }
}
