package com.pointr.tcp.rexec

import java.util.concurrent.atomic.AtomicInteger

import com.pointr.tcp.rpc.{ServiceConf, ServiceIf}
import com.pointr.tcp.util.{ExecParams, Logger}

class RexecIf(serviceConf: ServiceConf) extends ServiceIf(Option(serviceConf)) with Logger {

  private val nReqs = new AtomicInteger(0)

  def rexec(execParams: ExecParams): RexecResp = {
    val resp = getRpc().request(RexecReq(Rexec(execParams)))
    resp.asInstanceOf[RexecResp]
  }

  override def run(args: Seq[Any]) = {
    val Seq(execParams: ExecParams, nLoops: Int) = args
    run(execParams, nLoops)
  }

  override def run() = {
    def c(f: String) = conf.props.get(f).map{ _.toString}.getOrElse("")

    val execParams = new ExecParams(c("tag"), c("cmdLine"), if (c("dir").length>0) c("dir") else ".",
      Option(c("env").split(" ").toSeq))

    run(execParams, c("loops").toInt)
  }

    def run(execParams: ExecParams, nLoops: Int) = {
      var lastResult: String = null
      for (n <- 0 until nLoops) {
        // while (keepGoing(n).value) {
        debug(s"Loop $n: Sending request: $execParams ..")
        lastResult = rexec(execParams).toString
        debug(s"Loop #$n: Result is $lastResult")
      }
      lastResult
    }

}
