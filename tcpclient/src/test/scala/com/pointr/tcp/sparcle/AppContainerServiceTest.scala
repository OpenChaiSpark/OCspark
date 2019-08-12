package com.pointr.tcp.sparcle

import org.scalatest.FunSuite

class AppContainerServiceTest extends FunSuite {

  test("AppContainerServiceDriver") {
    val ret = AppContainerServiceDriver.run(Array.empty[String])
    println(ret)
    for (api <- Seq("ExecuteAppCommandResp", "ExecuteAppCommand2Resp","GetAppStatusResp", "ReadNextFromSparcleResp",
      "WriteAppDataToSparcleResp","WriteFeedDataToSparcleResp","WriteAppNativeDataToSparcleResp","Foo")) {
      assert(ret.contains(api),s"Missing response for api=$api")
    }
  }

}
