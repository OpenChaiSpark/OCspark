package com.pointr.tensorflow.api;

import org.junit.*;

import static com.pointr.tensorflow.api.JsonUtils.toJson;
import static com.pointr.tensorflow.api.Logger.p;

public class PcieDMAServerTest {

  static PcieDMAServer server = null;
  @BeforeClass
  public static void setUp() throws Exception {
    server = new PcieDMAServer();
  }

  @AfterClass
  public static void tearDown() throws Exception {
//    server.shutdownChannel("blah");
    server = null;
  }

  @Test
  public void battery() throws Exception {
    Logger.info("Starting Server battery ..");
    setUp();
    setupChannel();
    prepareWrite();
    write();
    completeWrite();
    prepareRead();
    read();
    completeRead();
    shutdownChannel();
    tearDown();
  }

  public void setupChannel() throws Exception {
    String res = server.setupChannel(toJson("blah"));
    Logger.p("setupChannel result: %s", res);
  }

  public void register() throws Exception {
    String res = server.register(new TensorFlowIf.DMACallback() {
      @Override
      public DMAStructures.WriteResultStruct dataSent() {
        return null;
      }

      @Override
      public DMAStructures.ReadResultStruct dataReceived() {
        return null;
      }
    });
    Logger.p("register result: %s", res);
  }

  public void prepareWrite() throws Exception {
    String x = server.prepareWrite(toJson("PrepareSend"));
    Logger.p("prepareWrite result: %s", x);

  }

  public void write() throws Exception {
    DMAStructures.WriteResultStruct x = server.write(toJson("blah"),
            "hello there".getBytes());
    Logger.p("write result: %s", x);
  }

  public void completeWrite() throws Exception {
    String x = server.prepareWrite(toJson("completeSend"));
    Logger.p("completeWrite  result: %s", x);

  }

  public void prepareRead() throws Exception {
    String x = server.prepareWrite(toJson("PrepareRcv"));
    Logger.p("prepareRead result: %s", x);

  }

  public void read() throws Exception {
    DMAStructures.ReadResultStruct x = server.read(toJson("read"));
    Logger.p("read result: %s", x);

  }

  public void completeRead() throws Exception {
    DMAStructures.ReadResultStruct x = server.read(toJson("completeRcv"));
    Logger.p("completeRead result: %s", x);

  }

  public void shutdownChannel() throws Exception {
    String x = server.shutdownChannel(toJson("blah"));
    Logger.p("shutdownChannel result: %s", x);

  }

  public void readLocal() throws Exception {
    byte[] dataPtr = "I am a dataPointer".getBytes();
    byte[] x = server.readLocal(dataPtr);
    Logger.p("readLocal result: %s", x);

  }

}