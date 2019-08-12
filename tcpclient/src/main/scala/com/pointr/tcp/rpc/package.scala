package com.pointr.tcp

import com.pointr.tcp.rpc.{P2pReq, P2pResp}
import com.pointr.tcp.xfer.DataPtr




package object rpc {


  // Use an abstract class instead of trait to permit interoperability with Java
  abstract class P2pConnectionParams


  val MagicNumber: String = "MyNameIsFozzieBear"

  trait ArrayData[V] {
    def tag: String

    def dims: Seq[Int]

    def toArray: V
  }

  import reflect.runtime.universe._


  type DArray = Array[Double]

  case class MData(override val tag: String, override val dims: Seq[Int], override val toArray: DArray) extends ArrayData[DArray]

  type AnyData = MData

  case class TData(label: Double, data: Vector[Double])

}
