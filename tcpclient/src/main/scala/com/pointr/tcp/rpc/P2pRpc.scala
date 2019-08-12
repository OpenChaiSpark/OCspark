package com.pointr.tcp.rpc

abstract class P2pRpc {

  import reflect.runtime.universe.TypeTag

  def connect(connParam: P2pConnectionParams): Boolean
  def isConnected: Boolean

  def request[U: TypeTag, V: TypeTag](req: P2pReq[U]): P2pResp[V] //  = _

  def requestJava[U, V](req: P2pReq[U]): P2pResp[V]  // = { null.asInstanceOf[P2pResp[V]] }

}
