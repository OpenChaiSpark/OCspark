package com.pointr.spark.util

import Logger._

object ProcessUtils {

case class ExecParams(process: String, args: Option[Seq[String]] = None,
  env: Option[Array[String]] = None, dir: String = ".") {

  override def toString: String = dir + "/" + process + args.flatMap( arr => Some(arr.mkString(" "))).getOrElse("")

}

  case class ExecResult(params: ExecParams, elapsed: Long, rc: Int, stdout: String, stderr: String)

  def exec(params: ExecParams) = {
    val startt = System.currentTimeMillis()

    val pb = new ProcessBuilder ( (params.process +: params.args.getOrElse(Seq.empty[String])):_*)
    pb.directory(new java.io.File(params.dir))

    val proc = pb.start()
    proc.waitFor()

    val exit = proc.exitValue()
    val stdout = scala.io.Source.fromInputStream(proc.getInputStream).getLines.toList.mkString("\n")
    val stderr = scala.io.Source.fromInputStream(proc.getErrorStream).getLines.toList.mkString("\n")

    val elapsed = System.currentTimeMillis() - startt

    val res = ExecResult(params, elapsed, exit, stdout, stderr)
    info(s"Process [${params}] completed in $elapsed with rc=$exit stdoutLen=${stdout.length} stderrLen=${stderr.length}")
    res

  }

}
