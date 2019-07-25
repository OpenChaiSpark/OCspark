package com.pointr.tensorflow.api;

public class TcpDMAServer extends DMAServerBase implements TensorFlowIf.DMAServer {

  @Override
  public String setupChannel(String setupJson) {
    return super.setupChannel(setupJson);
  }

  @Override
  public String register(TensorFlowIf.DMACallback callbackIf) {
    return super.register(callbackIf);
  }

  @Override
  public DMAStructures.WriteResultStruct write(String configJson, byte[] dataPtr) {
    return super.write(configJson, dataPtr);
  }

  @Override
  public DMAStructures.ReadResultStruct read(String configJson) {
    return super.read(configJson);
  }

  @Override
  public String shutdownChannel(String shutdownJson) {
    return super.shutdownChannel(shutdownJson);
  }

  @Override
  public byte[] readLocal(byte[] dataPtr) {
    return super.readLocal(dataPtr);
  }
}

