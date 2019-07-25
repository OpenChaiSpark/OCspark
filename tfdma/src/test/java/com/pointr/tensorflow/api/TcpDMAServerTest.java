package com.pointr.tensorflow.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.pointr.tensorflow.api.Logger.p;

public class TcpDMAServerTest {
  TcpDMAServer server = null;
  @Before
  public void setUp() throws Exception {
    server = new TcpDMAServer();
  }

  @After
  public void tearDown() throws Exception {
    server.shutdownChannel("blah");
    server = null;
  }

  @Test
  public void setupChannel() throws Exception {
    String res = server.setupChannel("blah");
    Logger.p("setupChannel result: %s", res);
  }

  @Test
  public void register() throws Exception {
    server.register(new TensorFlowIf.DMACallback() {
      @Override
      public DMAStructures.WriteResultStruct dataSent() {
        return null;
      }

      @Override
      public DMAStructures.ReadResultStruct dataReceived() {
        return null;
      }
    });
  }

  @Test
  public void write() throws Exception {
    DMAStructures.WriteResultStruct x = server.write("blah", "hello there".getBytes());
    Logger.p("sendata result: %s", x);
  }

  @Test
  public void read() throws Exception {
    DMAStructures.ReadResultStruct x = server.read("blah");
    Logger.p("rcvata result: %s", x);

  }

  @Test
  public void shutdownChannel() throws Exception {
    String x = server.shutdownChannel("blah");
    Logger.p("shutdownChannel result: %s", x);

  }

  @Test
  public void readLocal() throws Exception {
    byte[] dataPtr = "I am a dataPointer".getBytes();
    byte[] x = server.readLocal(dataPtr);
    Logger.p("setupChannel result: %s", x);

  }

}