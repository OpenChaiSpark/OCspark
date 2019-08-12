package com.pointr.tcp.rpc

import reflect.runtime.universe.TypeTag
abstract class XferReq[T: TypeTag](val value: T) extends P2pReq[T]
