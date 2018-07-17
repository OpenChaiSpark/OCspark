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
package org.openchai.tcp.rpc

import java.io.{BufferedOutputStream, DataInputStream, DataOutputStream}
import java.net._
import java.util
import java.util.concurrent.Executors

import org.openchai.tcp.util.Logger._
import org.openchai.tcp.util.TcpCommon

import scala.collection.mutable

// For my file watcher test:
import better.files.{File, FileMonitor}
import scala.concurrent._

object TcpServer {
  val DefaultPort = 8989
}

case class TcpServer(host: String, port: Int, serverIf: ServerIf) extends P2pServer with P2pBinding {

  import TcpServer._

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

  import TcpCommon._

  val MaxTcpWaitSecs = 1

  val buf = new Array[Byte](BufSize)
  def serve(socket: Socket): Unit = {
    val sockaddr = socket.getRemoteSocketAddress.asInstanceOf[InetSocketAddress]
    info(s"$port: Received connection request from ${sockaddr.getHostName}@${sockaddr.getAddress.getHostAddress} on socket ${socket.getPort}")
    var msgPrinted = false
    var msgCounter = 0

    val dis = new DataInputStream(socket.getInputStream)
    val os = new DataOutputStream(socket.getOutputStream)
    var nEmpty = 0
    val nWaitCycles = Option(System.getProperty("tcpserver.wait.cycles")).getOrElse("500").toInt

    // BEGIN TEST: let's have the watcher here so it doesn't die in a child thread.
    // Implies that we won't actually return exeResult objects from labelImg.  So
    // instead of pasing the exeResult all the way to the output data stream via
    // TcpServer.serve() we should somehow tie this callback to that stream...
    val watcher = new FileMonitor(File("/Users/mike/tmp/special"), recursive = true) {
//      override def onCreate(file: File, count: Int) = info(s"$file ALSO got created")
      override def onCreate(file: File, count: Int) = {
        val exeResult = new ExecResult(
          new ExecParams(
            "test-local",
            "/Users/mike/bin/testoc",
            Some(Array("/Users/mike/tmp/ocspark/tmp/cat.jpg")),
            Some(List("/Users/mike/tmp/ocspark/run")),
            "/Users/mike/tmp/ocspark/run"),
          6206,
          0,
          "904\\t/Users/mike/tmp/ocspark/tmp/cat4.jpg",
          "",
          false)
        info(s"$file REALLY got created")
//        val istruct = ??
//        val resp = LabelImgRespStruct(istruct.tag, istruct.fpath,
//          istruct.outPath, exeResult)
//        val resp2 = LabelImgResp(resp)
//        val ser = serializeStream("/tmp/serverResp.out",
//          pack("/tmp/serverResp.pack.out", resp))
//        debug(s"serialized stream length is ${ser.length}")
//        os.writeInt(ser.length)
//        os.write(ser)
//        os.flush
      }
    }
    info("****** starting watcher")
    val executorService = Executors.newFixedThreadPool(1)
    val executionContext = ExecutionContext.fromExecutorService(executorService)
    watcher.start()(executionContext)
    info("*********** watcher finished")
    // END TEST

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
      val bytesToRead = dis.readInt
      debug(s"Server BytesToRead = $bytesToRead")
      do {
        val nread = dis.read(buf, totalRead, buf.length - totalRead)
        totalRead += nread
        debug(s"in loop: nread=$nread totalRead=$totalRead bytesToRead=$bytesToRead")
        nEmpty = 0
        Thread.sleep(20)
      } while (totalRead < bytesToRead)
      //      }
      if (totalRead > 0) {
        debug(s"Serve: totalRead=$totalRead")
        val unpacked = unpack("/tmp/serverReq.out", buf.slice(0, totalRead))
        val req = unpacked.asInstanceOf[P2pReq[_]]
        //          val req = unpacked._2.asInstanceOf[P2pReq[_]]
        //          debug(s"Message received: ${req.toString}")
        val resp = serverIf.service(req)
        //          debug(s"Sending response:  ${resp.toString}")
        val ser = serializeStream("/tmp/serverResp.out", pack("/tmp/serverResp.pack.out", resp))
        debug(s"serialized stream length is ${ser.length}")
        os.writeInt(ser.length)
        os.write(ser)
        os.flush
        Thread.sleep(20)
      } else {
        nEmpty += 1
      }
      debug(s"outer outer: $nEmpty")
    } while (nEmpty <= nWaitCycles)
    info(s"Killing thread")
  }

  override def stop() = {
    serverThread.stop
    true
  }
}

