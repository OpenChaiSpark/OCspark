package org.openchai.tensorflow

import java.net.ConnectException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

import org.openchai.tcp.rpc._
import org.openchai.tcp.util.Logger._
import org.openchai.tcp.util._
import org.openchai.tcp.xfer._
import org.openchai.util.{AppConfig, TfConfig}

// For my java tfengine test:
import org.openchai.tfengine.TfEngine

// For my file watcher test:
import better.files.{File, FileMonitor}
import scala.concurrent._
//import ExecutionContext.Implicits.global
import java.util.concurrent.Executors


// The main thing we need to override here is using XferQConServerIf inside the server object
class TfServer(val appConfig: AppConfig, val outQ: BlockingQueue[TaggedEntry], val tfTcpParams: TcpParams,
  val tcpParams: TcpParams, val xtcpParams: TcpParams) {

  val ConnectWait = 3

  val xferServer = new QXferConServer(outQ/*.asInstanceOf[BlockingQueue[TaggedEntry]]*/,
    tcpParams, xtcpParams)
  info(s"*** TfServer")
  val tfServer = new TcpServer(tfTcpParams.server, tfTcpParams.port, new TfServerIf(appConfig, outQ, tfTcpParams.port))

  def start() = {
    var connected = false
    xferServer.start
    tfServer.start
    while (!connected) {
      try {
        GpuRegistry.registerGpuAlternate(appConfig("connections.gpuRegistryHost"), appConfig("connections.gpuRegistryPort").toInt, TfConfig.getHostName, tcpParams.port)
        connected = true
        error(s"Connected to Gpu Registry")
      } catch {
        case ce: ConnectException =>
          error(s"Unable to connect to GpuRegistry - will try again in $ConnectWait seconds ..")
          Thread.sleep(1000 * ConnectWait)
      }
    }
    Thread.sleep(100)
  }

}

object TfServer {


  val cfile = s"${System.getProperty("openchai.tfserver.config.file")}"
  info(s"Configfile=$cfile")

  val yamlConf = readConfig(cfile)
//  val imagesDir = s"${yamlConf("tmpdir")}/images"
//  info(s"Writing images to tmpdir=$imagesDir")
//  val f = new java.io.File(imagesDir)
//  if (!f.exists() && !f.mkdirs) {
//    throw new IllegalStateException(s"Unable to create image dirs ${f.getAbsolutePath}")
//  }

  def apply(yamlConf: AppConfig, outQ: BlockingQueue[TaggedEntry], tfTcpParams: TcpParams, tcpParams: TcpParams,
    xtcpParams: TcpParams) = {
    val server = new TfServer(yamlConf, outQ, tfTcpParams, tcpParams, xtcpParams)
    server.start
  }

  def os = System.getProperty("os.name") match {
    case "Mac OS X" => "osx"
    case x => x.toLowerCase
  }

  def readConfig(path: String): AppConfig = {
//    parseJsonToMap(FileUtils.readFileAsString(path))
    new AppConfig(path, os)
  }

  def main(args: Array[String]): Unit = {

    val iargs = if (args.nonEmpty && args(0) == getClass.getName) {
      args.slice(1, args.length)
    } else args

    // BEGIN TEST: let's have the watcher here so it doesn't die in a child thread.
    // Implies that we won't actually return exeResult objects from labelImg.  So
    // instead of pasing the exeResult all the way to the output data stream via
    // TcpServer.serve() we should somehow tie this callback to that stream...
    val watcher = new FileMonitor(File("/Users/mike/tmp/special"), recursive = true) {
      override def onCreate(file: File, count: Int) = info(s"$file got created")
    }
    info("****** starting watcher")
    val executorService = Executors.newFixedThreadPool(1)
    val executionContext = ExecutionContext.fromExecutorService(executorService)
    watcher.start()(executionContext)
    info("*********** watcher finished")
    // END TEST

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


class TfServerIf(val appConfig: AppConfig, val q: BlockingQueue[TaggedEntry], port: Int = 0) extends ServerIf("TfServerIf") {

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
    info("tfengine test: " + TfEngine.processImage("bogus"))
    // Here instead of exec'ing the command we should just wait for the result file to appear (or time out if it doesn't appear).
    val exeResult = if (estruct.appName == "test-local") {
      info("Doing a test-local app")
      if (false) {
        val resultPath = path + ".out"
        val watcher = new FileMonitor(File("/Users/mike/tmp/special"), recursive = true) {
          override def onCreate(file: File, count: Int) = info(s"$file got created")
        }
        info("****** starting watcher")
        val executorService = Executors.newFixedThreadPool(1)
        val executionContext = ExecutionContext.fromExecutorService(executorService)
        watcher.start()(executionContext)
        info("*********** watcher finished")
      }
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
      exeResult
    } else {
      val exe = estruct.cmdline.substring(0, estruct.cmdline.indexOf(" "))
      val exeResult = ProcessUtils.exec(ExecParams(estruct.appName, s"${exe}",
        Option(estruct.cmdline.replace("${1}", path).replace("${2}", istruct.tag).split(" ").tail), Some(Seq(estruct.runDir)), estruct.runDir))
      exeResult
    }
    info(s"Result: $exeResult")
    LabelImgRespStruct(istruct.tag, istruct.fpath, istruct.outPath, exeResult)
  }

  //  val isLinux = os == "linux"
  override def service(req: P2pReq[_]): P2pResp[_] = {
    req match {
      case o: LabelImgReq =>
        val struct = o.value
        val app = struct.imgApp
        info(s"Service: Invoking LabelImg: struct=$struct")

        val estruct = LabelImgExecStruct(struct, appConfig(app, "cmdline"), app, appConfig(app, "rundir"), appConfig(app, "tmpdir"))
        val resp = labelImg(estruct)
        LabelImgResp(resp)
      case _ =>
        throw new IllegalArgumentException(s"Unknown service type ${req.getClass.getName} on port ${port}")
    }
  }

}
