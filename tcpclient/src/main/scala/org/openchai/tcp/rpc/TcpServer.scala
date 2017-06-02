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

import java.io.{BufferedInputStream, BufferedOutputStream}
import java.net._

import org.openchai.tcp.util.Logger._
import org.openchai.tcp.util.TcpCommon

import scala.collection.mutable
import scala.util.Try

object TcpServer {
  val DefaultPort = 8989
  val BufSize = (Math.pow(2, 20) - 1).toInt
}

case class TcpServer(host: String, port: Int, serverIf: ServerIf) extends P2pServer with P2pBinding {

  import TcpServer._

  private var serverThread: Thread = _
  private var serverSocket: ServerSocket = _
  private var stopRequested: Boolean = _
  val threads = mutable.ArrayBuffer[Thread]()

  type ServerType = TcpServer

  def CheckPort = false

  def checkPort(port: Int) = {
    import scala.concurrent.duration._
    val socketTimeout = 200
    val duration: Duration = 10 seconds
//    if (CheckPort) {
//      val result =
//        Future {
//          Try {
//            val socket = new java.net.Socket()
//            socket.connect(new InetSocketAddress("localhost", port), socketTimeout)
//            socket.close()
//            port
//          } toOption
//        }
//      Try {
//        Await.result(result, duration)
//      }.toOption.getOrElse(Nil)
//    }
  }

  override def start() = {
	Console.println("setting preferIpv4Stack")
    System.setProperty("java.net.preferIPv4Stack" , "true");
    serverSocket = new ServerSocket()
    println(s"Starting ${serverIf.name} on $host:$port ..")
    try {
      if (host=="*") {
	serverSocket.bind(new InetSocketAddress(port))
	} else {
          serverSocket.bind(new InetSocketAddress(host, port))
      }
    } catch {
      case e: Exception => throw new Exception(s"BindException on $host:$port", e)
    }
//    checkPort(port) match {
//      case m: Exception => error(s"Server already running on port $port"); false
//      case _ => */ serverSocket.bind(new InetSocketAddress(host, port))
//    }
    serverThread = new Thread() {
      override def run() {
        while (!stopRequested) {
          val t = serve(serverSocket.accept())
          t.start
          threads += t
        }
      }
    }
    serverThread.start
    this
  }

  import TcpCommon._
  def serve(socket: Socket): Thread = {
    val sockaddr = socket.getRemoteSocketAddress.asInstanceOf[InetSocketAddress]
    info(s"Received connection request from ${sockaddr.getHostName}@${sockaddr.getAddress.getHostAddress} on socket ${socket.getPort}")
    val t = new Thread() {
      var msgPrinted = false
      var msgCounter = 0
      override def run() = {
        val is = new BufferedInputStream(socket.getInputStream)
        val os = new BufferedOutputStream(socket.getOutputStream)
        do {
          val buf = new Array[Byte](BufSize)
          if (!msgPrinted) { debug("Listening for messages.."); msgPrinted = true }
          val nread = is.read(buf)
          println(s"Nread=$nread")
          val unpacked = unpack("/tmp/serverReq.out",buf.slice(0,nread))
          val req = unpacked.asInstanceOf[P2pReq[_]]
//          val req = unpacked._2.asInstanceOf[P2pReq[_]]
          debug(s"Message received: ${req.toString}")
          val resp = serverIf.service(req)
          debug(s"Sending response:  ${resp.toString}")
          val ser = serializeStream("/tmp/serverResp.out",pack("/tmp/serverResp.pack.out",resp))
          os.write(ser)
          os.flush
        } while (!reconnectEveryRequest)
        Thread.sleep(5000)
      }
    }
    t
  }

  override def stop(): Boolean = ???
}

