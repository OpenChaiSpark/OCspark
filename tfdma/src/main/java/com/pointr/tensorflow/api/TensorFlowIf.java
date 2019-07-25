package com.pointr.tensorflow.api;

public interface TensorFlowIf {
// public static interface TensorFlowClientIf {

  // import DMAStructures.*;
// DMACallback Interface 
  public static interface DMACallback {
    DMAStructures.WriteResultStruct dataSent();

    DMAStructures.ReadResultStruct dataReceived();
  }

  // DMA Interface
  public static interface DMAServer {
    String setupChannel(String setupJson);

    String register(DMACallback callbackIf);

    String prepareWrite(String configJson);  // Sends "prepare data xfer operation" command to server side

    DMAStructures.WriteResultStruct write(String configJson, byte[] dataPtr);

    // TODO: I'm unclear if we neeed the completeWrite  or not .. but keeping it for now
    DMAStructures.WriteResultStruct completeWrite(String configJson);  // Sends "data xfer completed" notification to server

    String prepareRead(String configJson);  // Sends "prepare data xfer operation" command to server side

    DMAStructures.ReadResultStruct read(String configJson);

    // TODO: I'm unclear if we neeed the completeRead or not .. but keeping it for now
    DMAStructures.ReadResultStruct completeRead(String configJson);  // Sends "data xfer completed" notification to server

    String shutdownChannel(String shutdownJson);

    byte[] readLocal(byte[] dataptr);// Retrieve *locally* from dma shared memory location
  }

  // DMA Client API
  public static interface DMAClient {
    String setupChannel(String setupJson);

    String prepareWrite(String configJson);  // Sends "prepare data xfer operation" command to server side

    DMAStructures.WriteResultStruct write(String configJson, byte[] data, byte[] md5);  // invoke on DMA channel

    DMAStructures.WriteResultStruct completeWrite(String configJson);  // Sends "data xfer completed" notification to server

    String prepareRead(String configJson);  // Sends "prepare data xfer operation" command to server side

    DMAStructures.ReadResultStruct read(String configJson);  // invoke on DMA channel

    String completeRead(String configJson);  // Sends "data xfer completed" notification to server

    String shutdownChannel(String setupJson);

    byte[] readLocal(byte[] dataptr); // Retrieve *locally* from dma shared memory location
  }
}

