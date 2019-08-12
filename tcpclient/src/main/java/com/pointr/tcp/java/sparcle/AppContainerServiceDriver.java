package com.pointr.tcp.java.sparcle;

import com.pointr.tcp.rpc.ServerFactory;
import com.pointr.tcp.rpc.TcpClient;
import com.pointr.tcp.rpc.TcpServer;
import com.pointr.tcp.util.TcpUtils;
import scala.collection.immutable.Iterable;

public class AppContainerServiceDriver {

  public static String run() throws InterruptedException {
    System.setProperty("logger.level","3");
    String serverConfPath = "src/main/resources/appContainer-server.java.yaml";
    String  confPath = "src/main/resources/appContainer.java.yaml";
    Iterable<TcpServer> servers = ServerFactory.create(serverConfPath);
    Thread.sleep(200);
    TcpClient client = TcpClient.createClientFromArgs(
        new String[]{TcpUtils.getLocalHostname(),"" + TcpServer.DefaultPort(),confPath});
    String out = (String)client.serviceIf().run();
    System.out.println(out);
    return out;
  }

  public static void main(String[] args) throws InterruptedException {
    run();
  }

}
