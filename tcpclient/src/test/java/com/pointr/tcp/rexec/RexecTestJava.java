package com.pointr.tcp.rexec;

import com.pointr.tcp.rpc.TcpClient$;
import com.pointr.tcp.rpc.TcpClient;
import com.pointr.tcp.rpc.TcpParams;
import com.pointr.tcp.rpc.TcpServer;
import com.pointr.tcp.rpc.TcpServer$;
import junit.framework.TestCase;

public class RexecTestJava extends TestCase {

  public void testBasic() throws InterruptedException {
    TcpParams tcpParams = new TcpParams("localhost", TcpServer$.MODULE$.DefaultPort());
    TcpServer rexecServer = RexecServer$.MODULE$.apply(tcpParams, "ls -lrta /tmp");
    rexecServer.start();
    TcpClient client = TcpClient$.MODULE$.createClientFromArgs(new String[]{"localhost", "8989", "src/main/resources/rexec-client.yaml"});
    String output = (String) client.serviceIf().run();
    assert output.contains("RexecResp(ExecResult(ls  -lrta /shared") : "ExecResults failed Loop message check1";
    assert output.contains("drwxr-xr-x") : "ExecResults failed Loop message check2";
//    Thread.currentThread().join();
  }
}

