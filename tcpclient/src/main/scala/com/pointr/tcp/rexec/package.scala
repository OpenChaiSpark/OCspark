package com.pointr.tcp

import java.util.concurrent.atomic.AtomicInteger

import com.pointr.tcp.rpc.{P2pReq, P2pResp, ServiceIf}
import com.pointr.tcp.util.{ExecParams, ExecResult, Logger}

package object rexec {


  case class RexecParams(execParams: ExecParams)

  case class Rexec(execParams: ExecParams)

  case class RexecReq(rexec: Rexec) extends P2pReq[Rexec] {
    override def value(): Rexec = rexec
  }

  case class RexecResp(res: ExecResult) extends P2pResp[ExecResult] {
    override def value(): ExecResult = res
  }

}
