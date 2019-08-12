package com.pointr.tcp.rpc

import reflect.runtime.universe.TypeTag
abstract class XferResp[T: TypeTag](val value: T) extends P2pResp[T]
