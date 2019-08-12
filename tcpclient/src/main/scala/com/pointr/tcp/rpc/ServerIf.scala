package com.pointr.tcp.rpc

abstract class ServerIf(serverIfName: String, val confOpt: Option[ServerIfConf] = None) {

  //    def name: String = confOpt.map( _.serviceName).getOrElse(_name)
  //    private var _name: String = "emptyName"
  //
  //    confOpt.map { conf =>
  //      this._name
  //    }
  //    @deprecated
  //    def this(_name: String) = {
  //      this(None)
  //      this._name = _name
  //    }

  def name() = confOpt.map { conf =>
    conf.serviceName
  }.getOrElse(this.serverIfName)

  def service(req: P2pReq[_]): P2pResp[_]
}
