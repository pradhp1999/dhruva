package com.cisco.dhruva.sip.loadbalancer;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import com.cisco.dhruva.loadbalancer.LBHashBasedMsid;
import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.ServerGroupInterface;
import com.cisco.dhruva.sip.loadbalancer.resources.RequestBuilder;
import com.cisco.dhruva.sip.servergroups.AbstractNextHop;
import com.cisco.dhruva.sip.servergroups.DefaultNextHop;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.transport.Transport;
import java.util.HashMap;
import java.util.TreeSet;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LBBaseMsidTest {
  LBHashBasedMsid lbmsidObj;
  DsSipRequest request;

  @BeforeMethod
  void setup() {
    lbmsidObj = new LBHashBasedMsid();
    request = RequestBuilder.getRequestWithMParam("1528940592285", "applicationsharing");
  }

  @DataProvider(name = "dataProviders")
  public Object[][] dataProviders() {
    return new Object[][] {{true, "applicationsharing"}, {false, "audio"}};
  }

  // by providing the different combination of data providers incoming request is verified for av
  // call or desktop call.
  @Test(
      dataProvider = "dataProviders",
      description =
          "creates a request object for desktop call, then test isDsCall method of LBBaseMsid")
  public void testDSCall(boolean result, String mParam) {
    request = RequestBuilder.getRequestWithMParam("1528940592285", mParam);
    lbmsidObj.setServerInfo(new DsByteString("testSG"), null, request);
    assertEquals(lbmsidObj.isDsCall(), result);
  }

  @DataProvider(name = "dataProvider")
  public Object[][] dataProvider() {
    return new Object[][] {
      {true, "applicationsharing", 2},
      {false, "applicationsharing", 2},
      {true, "audio", 2},
      {false, "audio", 2}
    };
  }

  // initializeElements method for set of elements with different call type is verified.
  @Test(
      dataProvider = "dataProvider",
      description =
          "creates a request object for desktop call, then test initializeDomains method of LBBaseMsid")
  public void initializeElements(boolean next, String mParam, int result) throws Exception {
    // create multiple Server Group Elements
    AbstractNextHop anh1 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            000,
            Transport.UDP,
            0.1f,
            new DsByteString("testSG"));
    AbstractNextHop anh2 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            001,
            Transport.UDP,
            0.1f,
            new DsByteString("testSG2"));

    TreeSet<ServerGroupElementInterface> set = new TreeSet<ServerGroupElementInterface>();
    set.add(anh1);
    set.add(anh2);

    ServerGroupInterface serverGroup = mock(ServerGroupInterface.class);
    Mockito.when(serverGroup.getElements()).thenReturn(set);

    HashMap map1 = mock(HashMap.class);
    Mockito.when(map1.get(Mockito.any())).thenReturn(serverGroup);

    lbmsidObj.setServerInfo(
        new DsByteString("SG2"), serverGroup, RequestBuilder.getRequestWithMParam("12345", mParam));

    lbmsidObj.initializeDomains();

    assertEquals(lbmsidObj.getDomainsToTry().size(), result);
  }
}
