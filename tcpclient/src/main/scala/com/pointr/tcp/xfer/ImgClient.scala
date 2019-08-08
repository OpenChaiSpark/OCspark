package com.pointr.tcp.xfer

import java.net.SocketException

import com.pointr.tcp.rpc._
import com.pointr.tcp.util.Logger._
import com.pointr.tcp.util.{ExecResult, FileUtils, Logger, TcpCommon, YamlUtils}
import com.pointr.tcp.xfer.DataXferConClient.DataXferControllers
import com.pointr.tcp.xfer.XferConCommon._
import com.pointr.tcp.xfer._

case class ImgAppConfig()

object ImgConfig {
  def getHostName = FileUtils.readFileAsString("/shared/conf/hostname").trim

  def getAppConfig(fpath: String): ImgAppConfig = {
    val yml = FileUtils.readFileAsString(fpath)
    YamlUtils.toScala[ImgAppConfig](yml)
  }
}

object ImgClient extends Logger {

  def apply(conf: ImgAppConfig, controllers: DataXferControllers, tcpParams: TcpParams) = {

    val client = new ImgClient(tcpParams, ImgSimpleConfig("ImagesApp"), controllers.client)
    client
  }

  def apply(conf: ImgAppConfig): ImgClient = {
    apply(conf,DataXferConClient.makeDataXferControllers(TestControllers), AppTcpArgs)
  }

  def apply(conf: ImgAppConfig, server: String, port: Int = 0): ImgClient = {
    val base = if (port > 0) port else 61234
    val controllers = DataXferConClient.makeDataXferControllers(remoteControllers(server, base))
    apply(conf, controllers, remoteTcpArgs(server,base+2))
  }

  def defaultConf() = ImgConfig.getAppConfig("submitter.yml")

  def testClient() = {
//    val testImg = "/images/pilatus800.jpg"
    val testImg = "/images/JohnNolteAndDad.jpg"

    val is = this.getClass().getResourceAsStream(testImg)
    val buf = new Array[Byte](16 * 1024 *1024)

//    val imgBytes = this.getClass().getResourceAsStream(testImg) //.getBytes("ISO-8859-1")
    if (is == null) {
      throw new IllegalArgumentException(s"Unable to access $testImg")
    }
    val n = is.read(buf)
    val rbuf = buf.slice(0,n)
    val imgClient = ImgClient(defaultConf())
    val md5 = FileUtils.md5(rbuf.slice(0,n))
    val label = imgClient.labelImg(LabelImgStruct("funnyPic","TestImgClient", s"${System.getProperty("user.dir")}/img/src/main/resources/$testImg",
      "/tmp", rbuf, md5))
    info(s"Received label result: $label")
    label
  }

  def main(args: Array[String]): Unit = {
    val resp = testClient()
    info(resp.value.toString)
    info("We're done!")
  }

}

class ImgClient(val tcpParams: TcpParams, val config: ImgSimpleConfig, val xferClient: DataXferConClient)
  extends TcpClient(tcpParams, ImgClientIf(tcpParams, config, xferClient)) {
  val imgIf = serviceIf.asInstanceOf[ImgClientIf]

  def labelImg(struct: LabelImgStruct) = imgIf.labelImg(struct)
}

case class ImgSimpleConfig(name: String/*, imgDir: String, outDir: String*/) // placeholder

case class LabelImgStruct(tag: String, imgApp: String, fpath: String, outPath: String,
  data: Array[Byte] = Array.empty[Byte], md5: Array[Byte] = Array.empty[Byte]) {

  override def toString: DataPtr = s"LabelImgStruct: tag=$tag path=$fpath " +
    s"datalen=${if (data!=null) data.length else -1} md5len=${if (md5!=null) md5.length else -1}"
}

case class LabelImgReq(value: LabelImgStruct) extends P2pReq[LabelImgStruct]

case class LabelImgRespStruct(tag: String, fpath: String, outDir: String, cmdResult: ExecResult, nImagesProcessed: Int=1) {
  override def toString() = s"LabelImgRespStruct: tag=$tag fpath=$fpath result=$cmdResult"
}

case class LabelImgResp(val value: LabelImgRespStruct) extends P2pResp[LabelImgRespStruct]

case class ImgClientIf(tcpParams: TcpParams, config: ImgSimpleConfig, imgClient: DataXferConClient) extends ServiceIf("ImgClient") {

  def labelImg(s: LabelImgStruct, retries: Int = 0): LabelImgResp = {
    FileUtils.mkdirs(s.outPath)
    info(s"ImgClientIf.LabelImg: $s")
    try {

      val wparams = XferWriteParams(s.tag, imgClient.config,
        TcpCommon.serializeObject(s.fpath, TaggedEntry("taggedPic", s.data)))
      val xferConf = TcpXferConfig(s.tag, s.fpath)
      debug(s"ImgClientIf.prepareWrite on ${wparams.tag} ..")
      imgClient.prepareWrite(xferConf)
      debug(s"ImgClientIf.write on ${wparams.tag} ..")
      val wres = imgClient.write(wparams)
      debug(s"ImgClientIf.completeWrite on ${wparams.tag} ..")
      imgClient.completeWrite(xferConf)
      val newLiReq = LabelImgReq(LabelImgStruct(s.tag, s.imgApp, s.fpath, s.outPath))
      debug(s"ImgClientIf.request on ${wparams.tag} ..")
      val resp = getRpc().request(newLiReq)
      info(s"LabelImg response: $resp")
      resp.asInstanceOf[LabelImgResp]
    } catch {
      case se: SocketException =>
        if (retries == 0) {
          error(s"SocketException on labelImg", se)
          labelImg(s, 1)
        } else {
          throw se
        }
      case e: Exception => throw e
    }

  }

}

