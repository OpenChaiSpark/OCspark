package com.pointr.tcp.java.sparcle;

import junit.framework.TestCase;
import scala.Array;

public class AppContainerServiceTest extends TestCase {

  public void testAppContainerService() throws InterruptedException {
        String ret = (String) AppContainerServiceDriver.run();
    System.out.println(ret);
    for (String api : new String[]{"ExecuteAppCommandResp", "ExecuteAppCommand2Resp","GetAppStatusResp", "ReadNextFromSparcleResp",
      "WriteAppDataToSparcleResp","WriteFeedDataToSparcleResp","WriteAppNativeDataToSparcleResp"}) {
      assert ret.contains(api) : "Missing response for api=%s".format(api) ;
    }

  }

}
