/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pointr.tcp.rpc

import java.io.{DataInputStream, DataOutputStream}
import java.net._
import java.util.concurrent.Executors

import com.pointr.tcp.util.Logger

case class ServerIfConf(serviceName: String, className: String, props: Map[String, Any],optServiceConf: Option[ServiceConf] = None, host: String = "localhost", port: Int = 4561) {

  lazy val serviceConf = optServiceConf.get
}

case class TcpServerConf(serverServiceIfs: Map[String,ServerIfConf], host: String = "localhost", port: Int = 4561)

case class TcpServer(host: String, port: Int, serverIf: ServerIf) extends P2pServer with P2pBinding with Logger {

  private var serverThread: Thread = _
  private var serverSocket: ServerSocket = _
  private var stopRequested: Boolean = _
  val MaxListeners = 3
  val pool = Executors.newFixedThreadPool(MaxListeners)

  type ServerType = TcpServer

  def CheckPort = false

  def checkPort(port: Int) = {
    import scala.concurrent.duration._
    val socketTimeout = 200
    val duration: Duration = 10 seconds
  }

  override def start() = {
    System.setProperty("java.net.preferIPv4Stack", "true")
    serverSocket = new ServerSocket()
    info(s"Starting ${serverIf.name} on $host:$port ..")
    try {
      if (host == "*" || host == "all" || host == "localhost") {
        serverSocket.bind(new InetSocketAddress(port))
      } else {
        serverSocket.bind(new InetSocketAddress(host, port))
      }
    } catch {
      case e: Exception => throw new Exception(s"BindException on $host:$port", e)
    }
    serverThread = new Thread() {
      override def run() {
        while (!stopRequested) {
          val sock = serverSocket.accept()
          pool.submit(new Runnable() {
            override def run() = {
              serve(sock)
            }
          })
        }
      }
    }
    serverThread.start
    this
  }

  import com.pointr.tcp.util.TcpCommon._

  val MaxTcpWaitSecs = 1

  val buf = new Array[Byte](BufSize)

  val MaxBytesAllowed = 64 * 1e6

  def serve(socket: Socket): Unit = {
    try {
      val sockaddr = socket.getRemoteSocketAddress.asInstanceOf[InetSocketAddress]

      info(s"$port: Received connection request from ${sockaddr.getHostName}@${sockaddr.getAddress.getHostAddress} on socket ${socket.getPort}")
      var msgPrinted = false
      var msgCounter = 0

      val dis = new DataInputStream(socket.getInputStream)
      val os = new DataOutputStream(socket.getOutputStream)
      var nEmpty = 0
      val nWaitCycles = Option(System.getProperty("tcpserver.wait.cycles")).getOrElse("500").toInt
      do {
        if (!msgPrinted) {
          debug("Listening for messages..")
          msgPrinted = true
        }
        var nNone = 0
        var totalRead = 0
        //      val available = dis.available
        //      if (available <= 0) {
        //        Thread.sleep(200)
        //      } else {
        val b = new Array[Byte](MagicNumber.length)
        val magicNumberLen = dis.read(b)
        val magicNumber = new String(b,"ISO-8859-1")
        if (magicNumberLen != MagicNumber.length || magicNumber != MagicNumber) {
          throw new IllegalStateException(s"MagicNumber mismatch: len=$magicNumberLen and magicNumber=$magicNumber")
        }

        val bytesToRead = dis.readInt
        debug(s"Server BytesToRead = $bytesToRead")
        if (bytesToRead> MaxBytesAllowed) {
          throw new UnsupportedOperationException(s"Why are we trying to read $bytesToRead bytes?")
        }
        do {
          val nread = dis.read(buf, totalRead, buf.length - totalRead)
          totalRead += nread
          debug(s"${getClass.getSimpleName}: in loop: nread=$nread totalRead=$totalRead bytesToRead=$bytesToRead")
          nEmpty = 0
          Thread.sleep(20)
        } while (totalRead < bytesToRead)
        //      }
        if (totalRead > 0) {
          debug(s"Serve: totalRead=$totalRead")
          val unpacked = unpack("/tmp/serverReq.out", buf.slice(0, totalRead))
          val req = unpacked.asInstanceOf[P2pReq[_]]
          debug(s"Received ${req.path()} request")
          //          val req = unpacked._2.asInstanceOf[P2pReq[_]]
          //          debug(s"Message received: ${req.toString}")
          val resp = serverIf.service(req)
          //          debug(s"Sending response:  ${resp.toString}")
          val ser = serializeStream("/tmp/serverResp.out", pack("/tmp/serverResp.pack.out", resp))
          debug(s"serialized stream length is ${ser.length}")
          val b1 = MagicNumber.getBytes("ISO-8859-1")
          os.write(b1)
          os.writeInt(ser.length)
          os.write(ser)
          os.flush
          Thread.sleep(20)
        } else {
          nEmpty += 1
        }
        //      debug(s"outer outer: $nEmpty")
      } while (nEmpty <= nWaitCycles)
      info(s"Killing thread")
    } catch {
      case e: Exception =>
        error(s"TcpServer.server() Error on socket=$socket: ${Logger.toString(e)}")
    }
  }

  override def stop() = {
    serverThread.stop
    true
  }
}


object TcpServer {

  val DefaultPort = 8989

  def createDefaultServer(confPath: String)= {
    val server = TcpServer("localhost",DefaultPort,
      new SolverServerIf(ConfParser.parseServerConf(DefaultConfPath).serverServiceIfs("Solver")))
    server
  }
  var DefaultConfPath = "/git/OCspark/tcpclient/src/main/resources/solver-server.yaml"

}