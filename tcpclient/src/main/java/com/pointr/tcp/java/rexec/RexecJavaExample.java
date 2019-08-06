package com.pointr.tcp.java.rexec;

import com.pointr.tcp.rexec.RexecServer$;
import com.pointr.tcp.rpc.TcpClient;
import com.pointr.tcp.rpc.TcpParams;
import com.pointr.tcp.rpc.TcpServer;
import com.pointr.tcp.rpc.TcpServer$;

public class RexecJavaExample {
  public static void  main(String[] args) throws InterruptedException {
    TcpParams tcpParams = new TcpParams("localhost", TcpServer$.MODULE$.DefaultPort());
    TcpServer rexecServer = RexecServer$.MODULE$.apply(tcpParams, "ls -lrta /tmp");
    rexecServer.start();
    TcpClient client = TcpClient.createClientFromArgs(new String[]{"localhost", "8989", "src/main/resources/rexec-client.yaml"});
    String ret = (String) client.serviceIf().run();
    Thread.currentThread().join();
  }
}

