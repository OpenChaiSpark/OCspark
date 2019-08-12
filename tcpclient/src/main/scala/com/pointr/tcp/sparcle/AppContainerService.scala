package com.pointr.tcp.sparcle

import java.nio.ByteBuffer

import com.pointr.tcp.rpc.{P2pReq, P2pResp, ServiceConf, ServiceIf}
import com.pointr.tcp.util.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Container service manages the life cycle of the app and
  * exposes communication interface from Sparcle I/O bus
  * Its a facade layer sitting on top of Sparcle client
  */

object AppContainerService {

  case class ExecuteAppCommandReq(value: String) extends P2pReq[String] {
    val Command: String = value
  }

  case class ExecuteAppCommandResp(value: String) extends P2pResp[String]

  case class ExecuteAppCommand2Struct(Command: String, GPURefID: String)

  case class ExecuteAppCommand2Req(value: ExecuteAppCommand2Struct) extends P2pReq[ExecuteAppCommand2Struct] {
    val Command: String = value.Command
    val GPURefID: String = value.GPURefID
  }

  case class ExecuteAppCommand2Resp(value: ExecuteAppCommand2Struct) extends P2pResp[ExecuteAppCommand2Struct]

  case class GetAppStatusReq(value: String) extends P2pReq[String] {
    val GPURefID: String = value
  }

  case class GetAppStatusResp(value: String) extends P2pResp[String]

  case class IsAppRunningReq(value: String) extends P2pReq[String] {
    val GPURefID: String = value
  }

  case class IsAppRunningResp(value: Boolean) extends P2pResp[Boolean]

  case class ReadNextFromSparcleReq(value: String) extends P2pReq[String] {
    val GPURefID: String = value
  }

  case class ReadNextFromSparcleResp(value: String) extends P2pResp[String]

  case class WriteAppDataToSparcleStruct(GPURefID: String, output: Array[Byte]) {
    override def toString: String = s"$GPURefID  ${new String(output, "ISO-8859-1")}"
  }

  case class WriteAppDataToSparcleReq(value: WriteAppDataToSparcleStruct) extends P2pReq[WriteAppDataToSparcleStruct] {
    val GPURefID: String = value.GPURefID
    val output: Array[Byte] = value.output
  }

  case class WriteAppDataToSparcleResp(value: String) extends P2pResp[String]

  case class WriteFeedDataToSparcleStruct(GPURefID: String, output: Array[Byte]) {
    override def toString: String = s"$GPURefID  ${new String(output, "ISO-8859-1")}"
  }

  case class WriteFeedDataToSparcleReq(value: WriteFeedDataToSparcleStruct) extends P2pReq[WriteFeedDataToSparcleStruct] {
    val GPURefID: String = value.GPURefID
    val output: Array[Byte] = value.output
  }

  case class WriteFeedDataToSparcleResp(value: String) extends P2pResp[String]

  case class WriteAppNativeDataToSparcleStruct(GPURefID: String, output: Array[Byte]) {
    override def toString: String = s"$GPURefID  ${new String(output,"ISO-8859-1")}"
  }

  case class WriteAppNativeDataToSparcleReq(val value: WriteAppNativeDataToSparcleStruct) extends P2pReq[WriteAppNativeDataToSparcleStruct] {
    val GPURefID: String = value.GPURefID
    val output: Array[Byte] = value.output
  }

  case class WriteAppNativeDataToSparcleResp(value: String) extends P2pResp[String]


}


class AppContainerService(conf: ServiceConf) extends ServiceIf(Option(conf)) with Logger {

  import AppContainerService._

  def executeAppCommand(req: ExecuteAppCommandReq): ExecuteAppCommandResp = {
    getRpc().request(req).asInstanceOf[ExecuteAppCommandResp]
  }

  def executeAppCommand2(req: ExecuteAppCommand2Req): ExecuteAppCommand2Resp = {
    getRpc().request(req).asInstanceOf[ExecuteAppCommand2Resp]
  }

  def getAppStatus(req: GetAppStatusReq): GetAppStatusResp = {
    getRpc().request(req).asInstanceOf[GetAppStatusResp]
  }

  def readNextFromSparcle(req: ReadNextFromSparcleReq): ReadNextFromSparcleResp = {
    getRpc().request(req).asInstanceOf[ReadNextFromSparcleResp]
  }

  def writeAppDataToSparcle(req: WriteAppDataToSparcleReq): WriteAppDataToSparcleResp = {
    getRpc().request(req).asInstanceOf[WriteAppDataToSparcleResp]
  }

  def writeFeedDataToSparcle(req: WriteFeedDataToSparcleReq): WriteFeedDataToSparcleResp = {
    getRpc().request(req).asInstanceOf[WriteFeedDataToSparcleResp]
  }

  def writeAppNativeDataToSparcle(req: WriteAppNativeDataToSparcleReq): WriteAppNativeDataToSparcleResp = {
    getRpc().request(req).asInstanceOf[WriteAppNativeDataToSparcleResp]
  }

  def isAppRunning(req: IsAppRunningReq): IsAppRunningResp = {
    getRpc().request(req).asInstanceOf[IsAppRunningResp]
  }

  override def run(): Any = {
    val outs = mutable.ArrayBuffer[String]()
    outs += executeAppCommand(ExecuteAppCommandReq("YogiBear - run!!")).toString
    outs += executeAppCommand2(ExecuteAppCommand2Req(ExecuteAppCommand2Struct("YogiBear - run more!!", "Gpu123"))).toString
    outs += getAppStatus(GetAppStatusReq("Here's My Status!")).toString
    outs += readNextFromSparcle(ReadNextFromSparcleReq("Here's My Status!")).toString
    outs += writeAppDataToSparcle(WriteAppDataToSparcleReq(WriteAppDataToSparcleStruct("GpuID", "WriteApp buffer data here".getBytes("ISO-8859-1")))).toString
    outs += writeFeedDataToSparcle(WriteFeedDataToSparcleReq(WriteFeedDataToSparcleStruct("GpuID", "WriteFeed buffer data here".getBytes("ISO-8859-1")))).toString
    outs += writeAppNativeDataToSparcle(WriteAppNativeDataToSparcleReq(WriteAppNativeDataToSparcleStruct("GpuID", "WriteAppNative buffer data here".getBytes("ISO-8859-1")))).toString
    outs.mkString("\n")
  }

  //  def setSparcleContext(sc: SparcleContext): Unit
  //  def executeAppCommand(Command: String): String /* Enum[_] */
  /*
   def executeAppCommand(Command: String): String /* Enum[_] */
   def getAppStatus(GPURefID: String): String /* Enum[_] */
   def readNextfromSparcle(GPURefID: String): Array[Byte] /* ByteBuffer */
   def writeAppDatatoSparcle(GPURefID: String, output: ByteBuffer): Unit
   def writeFeedDatatoSparcle(GPURefID: String, output: Array[Byte]): Unit
   def writeAppNativeDatatoSparcle(GPURefID: String,
                                   nBuffer: NativeBuffer): Unit
   def excuteAppCommand(Command: String, GPURefID: String): String
 //  def getAppSdkServiceInstance(): AppSdkService
   def isAppRunning(gpuRefId: String): Boolean
   */

}

case class SparcleContext()