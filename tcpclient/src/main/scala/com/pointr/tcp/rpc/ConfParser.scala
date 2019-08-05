package com.pointr.tcp.rpc

import com.pointr.tcp.util.{FileUtils, YamlUtils}

object ConfParser {

  def parseServerConf(confPath: String) = {
    val conf = YamlUtils.toScala[TcpServerConf](FileUtils.readFileAsString(confPath))
    conf
  }

}
