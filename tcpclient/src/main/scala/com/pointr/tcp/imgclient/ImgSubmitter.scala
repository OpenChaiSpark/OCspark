package com.pointr.tcp.imgclient

import java.util.Base64

import com.pointr.tcp.util.FileUtils._
import com.pointr.tcp.util.Logger._
import com.pointr.tcp.xfer.{ImgAppConfig, ImgClient, ImgConfig, LabelImgStruct}

import scala.collection.mutable.{ArrayBuffer => AB}

case class LabelImgRest(restHostAndPort: Option[String], imgServerHostAndPort: String, workerName: String,
  imgApp: String, path: String, outPath: String, outputTag: String, contents: Array[Byte]) {
}

object LabelImgRest {
  def apply(liwreq: LabelImgWebRest) = {
    if (liwreq.contentsBase64 != null) {
      val bytes = if (liwreq.restHostAndPort.startsWith("/")) {
        readFileBytes(liwreq.restHostAndPort)
      } else {
        Base64.getDecoder.decode(liwreq.contentsBase64)
      }
      new LabelImgRest(Option(liwreq.restHostAndPort), liwreq.imgServerHostAndPort, liwreq.workerName, liwreq.imgApp, liwreq.path, liwreq.outDir, liwreq.outputTag, bytes)
    } else {
      new LabelImgRest(Option(liwreq.restHostAndPort), liwreq.imgServerHostAndPort, liwreq.workerName, liwreq.imgApp, liwreq.path, liwreq.outDir, liwreq.outputTag, null)
    }
  }
}

case class LabelImgWebRest(restHostAndPort: String, imgServerHostAndPort: String, workerName: String, imgApp: String, path: String, outDir: String, outputTag: String, contentsBase64: String)

object LabelImgWebRest {
  def apply(lireq: LabelImgRest) =
    new LabelImgWebRest(lireq.restHostAndPort.get, lireq.imgServerHostAndPort, lireq.workerName, lireq.imgApp, lireq.path, lireq.outPath, lireq.outputTag,
      if (lireq.contents != null) {
        Base64.getEncoder.encodeToString(lireq.contents)
      } else "")
}

object ImagesMeta {
  def allowsExt(ext: String) = extWhiteList.isEmpty || extWhiteList.get.contains(ext.toLowerCase)

  private var _extWhiteList = Option("jpg jpeg png gif svg tiff".split(" ").toSeq)
  def extWhiteList = _extWhiteList

  def setExtWhiteList(exts: Option[Seq[String]])  = _extWhiteList = exts
}

object ImgSubmitter {
  val UseWeb = false

  def process(params: Map[String, String]) = {
    import com.pointr.tcp.util.JsonUtils._
    val UseSpark = true // TODO: make configurable with direct

    // tODO: conf from params..
    var conf: ImgAppConfig = null
    if (!params.contains("json")) {
      s"ERROR: no json in payload"
    } else {
      info(s"Received request ${params.map { p => val ps = p.toString; ps.substring(0, Math.min(ps.length, 300)) }.mkString(",")}")
      val json = params("json")
      val labelImgWebReq = parseJsonToCaseClass[LabelImgWebRest](json)
      val resp = if (UseSpark) {
        val msg = s"ImgWebServer: ImgSubmitter can not run Spark jobs: invoke DirectSubmitter instead.."
        error(msg)
        msg
      } else {
        debug(s"ImgWebServer: ImgSubmitter.labelImg..")
        val lresp = ImgSubmitter.labelImg(ImgClient(conf), LabelImgRest(labelImgWebReq))
        lresp.value.toString
      }
      info(s"ImgWebServer: result is $resp")
      resp
    }
  }

  @inline def getResetWatermarkFileName(dir: String) = s"$dir/watermark.reset.txt"
  @inline def getWatermarkFileName(dir: String) = s"$dir/watermark.txt"

  def labelImg(imgClient: ImgClient, lireq: LabelImgRest) = {
    val fmd5 = md5(lireq.contents)
    debug(s"invoking imgClient.labelImg on $lireq ..")
    val res = imgClient.labelImg(LabelImgStruct(lireq.outputTag, lireq.imgApp, lireq.path, lireq.outPath,
      lireq.contents, fmd5))
    info(s"ImgSubmitter.labelImg: LabelImgResp=$res")
    res
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      System.err.println("Usage: ImgSubmitter <submitter.yml file>")
      System.exit(-1)
    }
    val conf = ImgConfig.getAppConfig(args(0))
//    if (conf.isDirect) {
      val msg = s"ImgWebServer: ImgSubmitter can not run Spark jobs: invoke DirectSubmitter instead.."
      throw new IllegalArgumentException(msg)
//    } else {
//      val res = if (conf.isRest) {
//        labelImgViaRest(conf, LabelImgRest(Option(conf.restHostAndPort), conf.imgServerAndPort, conf.master, "IMGViaRest", conf.imgApp,
//          conf.inDir, conf.outDir, conf.outTag, null))
//      } else {
//        labelImg(ImgClient(conf), LabelImgRest(None, conf.master, conf.imgServerAndPort, "IMGCommandLine", conf.imgApp, conf.inDir, conf.outDir, conf.outTag, readFileBytes(conf.inDir)))
//      }
//      info(res.toString)
//    }
  }
}
