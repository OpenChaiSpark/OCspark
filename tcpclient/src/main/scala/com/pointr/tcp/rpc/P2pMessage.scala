package com.pointr.tcp.rpc

import com.pointr.tcp.xfer.DataPtr

abstract class P2pMessage[T] extends _root_.java.io.Serializable {
  def path(): DataPtr = getClass.getName

  def value(): T
}
