package com.pointr.tcp.imgclient

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

import com.pointr.tcp.rpc._
import com.pointr.tcp.util.Logger._
import com.pointr.tcp.util._
import com.pointr.tcp.xfer._

case class ImgServerConf(appName: String, key: String) // placeholder

// The main thing we need to override here is using XferQConServerIf inside the server object
class ImgServer(val imgServerConf: ImgServerConf, val outQ: BlockingQueue[TaggedEntry], val imgTcpParams: TcpParams,
  val tcpParams: TcpParams, val xtcpParams: TcpParams) {

  val ConnectWait = 3

  val xferServer = new QXferConServer(outQ,
    tcpParams, xtcpParams)
  info(s"*** ImgServer")
  val imgServer = new TcpServer(imgTcpParams.server, imgTcpParams.port, new ImgServerIf(imgServerConf, outQ, imgTcpParams.port))

  def start() = {
    xferServer.start
    imgServer.start
  }

}

object ImgServer {

  val cfile = s"${System.getProperty("pointr.imgserver.config.file")}"
  info(s"Configfile=$cfile")

  val yamlConf = readConfig(cfile)
//  val imagesDir = s"${yamlConf("tmpdir")}/images"
//  info(s"Writing images to tmpdir=$imagesDir")
//  val f = new java.io.File(imagesDir)
//  if (!f.exists() && !f.mkdirs) {
//    throw new IllegalStateException(s"Unable to create image dirs ${f.getAbsolutePath}")
//  }

  def apply(yamlConf: ImgServerConf, outQ: BlockingQueue[TaggedEntry], imgTcpParams: TcpParams, tcpParams: TcpParams,
    xtcpParams: TcpParams) = {
    val server = new ImgServer(yamlConf, outQ, imgTcpParams, tcpParams, xtcpParams)
    server.start
  }

  def os = System.getProperty("os.name") match {
    case "Mac OS X" => "osx"
    case x => x.toLowerCase
  }

  def readConfig(path: String): ImgServerConf = {
//    parseJsonToMap(FileUtils.readFileAsString(path))
    new ImgServerConf(path, os)
  }

  def main(args: Array[String]): Unit = {

    val iargs = if (args.nonEmpty && args(0) == getClass.getName) {
      args.slice(1, args.length)
    } else args

    val q = new ArrayBlockingQueue[TaggedEntry](1000)
    val (host, port, xhost, xport, ahost, aport) = if (args.length == 0) {
      val cont = XferConCommon.TestControllers
      (cont.conHost, cont.conPort, cont.dataHost, cont.dataPort, cont.appHost, cont.appPort)
    } else {
      XferConServer.makeXferConnections(iargs)
    }
    val server = apply(yamlConf, q, TcpParams(ahost, aport), TcpParams(host, port), TcpParams(xhost, xport))
  }

}


class ImgServerIf(val imgServerConf: ImgServerConf, val q: BlockingQueue[TaggedEntry], port: Int = 0) extends ServerIf("ImgServerIf") {

  val pathsMap = new java.util.concurrent.ConcurrentHashMap[String, TcpXferConfig]()

  private val nReqs = new AtomicInteger(0)

  val LabelImageTag = "tensorflow-labelimage"
  val DarknetYoloTag = "darknet-yolo"
  val DefaultApp = DarknetYoloTag

  case class LabelImgExecStruct(istruct: LabelImgStruct, cmdline: String, appName: String, runDir: String, tmpDir: String)

  def labelImg(estruct: LabelImgExecStruct): LabelImgRespStruct = {
    info(s"LabelImg: processing $estruct ..")
    val istruct = estruct.istruct
//    if (istruct.data.isEmpty) {
//      throw new IllegalStateException(s"Non empty md5 for empty data on $istruct")
//    } else {
//      FileUtils.checkMd5(istruct.fpath, istruct.data, istruct.md5)
//    }

    val e = QXferConServer.findInQ(q, istruct.tag)
//    info(s"LabelImg: Found entry ${e.getOrElse("[empty]")}")
    val tEntry = TcpCommon.deserializeObject(e.get.data).asInstanceOf[TaggedEntry]
    val data = tEntry.data
    val dir = s"${estruct.tmpDir}" // /${istruct.fpath.substring(istruct.fpath.lastIndexOf("/") + 1)}"
    FileUtils.mkdirs(dir)
    val path = "%s/%s".format(dir,istruct.fpath.substring(istruct.fpath.lastIndexOf("/") + 1))
    FileUtils.writeBytes(path, data)
    val exe = estruct.cmdline.substring(0, estruct.cmdline.indexOf(" "))
    val exeResult = ProcessUtils.exec(ExecParams(estruct.appName, s"${exe}",
      Option(estruct.cmdline.replace("${1}",path).replace("${2}",istruct.tag).split(" ").tail), Some(Seq(estruct.runDir)), estruct.runDir))
    info(s"Result: $exeResult")
    LabelImgRespStruct(istruct.tag, istruct.fpath, istruct.outPath, exeResult)
  }

  override def service(req: P2pReq[_]): P2pResp[_] = {
    req match {
      case o: LabelImgReq =>
        val struct = o.value
        val app = struct.imgApp
        info(s"Service: Invoking LabelImg: struct=$struct")

        val estruct = LabelImgExecStruct(struct, "a"/*ImgServerConf( app, "cmdline") */, app, "b" /*imgServerConf(app, "rundir")*/, "c" /*imgServerConf(app, "tmpdir") */)
        val resp = labelImg(estruct)
        LabelImgResp(resp)
      case _ =>
        throw new IllegalArgumentException(s"Unknown service type ${req.getClass.getName} on port ${port}")
    }
  }

}
