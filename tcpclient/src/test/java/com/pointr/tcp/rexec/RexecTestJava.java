package com.pointr.tcp.rexec;
import com.pointr.tcp.rpc.TcpParams;
import com.pointr.tcp.rpc.TcpServer;
import com.pointr.tcp.rpc.TcpServer$;
import com.pointr.tcp.util.ExecParams;
import scala.*;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class RexecTestJava {
  public static void  main(String[] args)  {
    String server = args[0];
    TcpParams tcpParams = new TcpParams(server, TcpServer$.MODULE$.DefaultPort());
    TcpServer rexecServer = RexecServer$.MODULE$.apply(tcpParams);
    rexecServer.start();
    RexecTcpClient rexecClient = new RexecTcpClient(tcpParams);
    String res = rexecClient.run(new RexecParams(new ExecParams("ls","ls",
        Option.<Seq<String>>apply(JavaConverters.<String>asScalaBufferConverter(
                Arrays.<String>asList("-lrta .".split(" "))).asScala().toSeq()),
        Option.apply(null),"/etc/pam.d")), 5);
    System.out.println("Result: %s".format(res));
  }
}

