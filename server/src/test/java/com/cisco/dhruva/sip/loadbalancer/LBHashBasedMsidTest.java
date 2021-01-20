package com.cisco.dhruva.sip.loadbalancer;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.loadbalancer.LBMsid;
import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.ServerGroupInterface;
import com.cisco.dhruva.sip.loadbalancer.resources.RequestBuilder;
import com.cisco.dhruva.sip.servergroups.AbstractNextHop;
import com.cisco.dhruva.sip.servergroups.DefaultNextHop;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.transport.Transport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LBHashBasedMsidTest {
  LBMsid msidObj;
  DsSipRequest request;

  @BeforeMethod
  void setup() {
    msidObj = new LBMsid();
    request = RequestBuilder.getRequestWithMParam("1234", "applicationsharing");
  }

  @DataProvider(name = "dataProvider")
  public Object[][] dataProviders() {
    return new Object[][] {
      {"1528940592285", "applicationsharing", 0.5f, 0.8f, 1},
      {"1528940592285", "applicationsharing", 0.9f, 0.4f, 0},
      {"1528940592286", "audio", 0.5f, 0.8f, 2},
      {"1528940592286", "audio", 0.9f, 0.4f, 1}
    };
  }
  // test case to verify selected element for the given elements with different qValue, msid and
  // media type
  @Test(
      dataProvider = "dataProvider",
      description =
          "creates a request object for desktop call, then test selectElement method of LBHashBasedMsid")
  public void selectElement(String msid, String mParam, float qValue1, float qValue2, int result) {
    // create multiple Server Group Elements
    AbstractNextHop anh1 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0001,
            Transport.UDP,
            qValue1,
            new DsByteString("testSG1"));
    AbstractNextHop anh2 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0002,
            Transport.UDP,
            qValue1,
            new DsByteString("testSG2"));
    AbstractNextHop anh3 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0003,
            Transport.UDP,
            qValue2,
            new DsByteString("testSG3"));
    AbstractNextHop anh4 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0004,
            Transport.UDP,
            qValue2,
            new DsByteString("testSG4"));
    AbstractNextHop anh5 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0005,
            Transport.UDP,
            qValue2,
            new DsByteString("testSG5"));

    TreeSet<ServerGroupElementInterface> set = new TreeSet<ServerGroupElementInterface>();
    set.add(anh1);
    set.add(anh2);
    set.add(anh3);
    set.add(anh4);
    set.add(anh5);

    List<ServerGroupElementInterface> list = new ArrayList<ServerGroupElementInterface>();
    list.addAll(set);

    ServerGroupInterface serverGroup = mock(ServerGroupInterface.class);
    Mockito.when(serverGroup.getElements()).thenReturn(set);

    HashMap map1 = mock(HashMap.class);
    Mockito.when(map1.get(Mockito.any())).thenReturn(serverGroup);

    msidObj.setServerInfo(
        new DsByteString("SG2"), serverGroup, RequestBuilder.getRequestWithMParam(msid, mParam));
    msidObj.setDomainsToTry(set);

    assertEquals(msidObj.getServer(null), list.get(result));
  }

  @DataProvider(name = "dataProviders")
  public Object[][] dataProvider() {
    return new Object[][] {{"1528940592285", "audio", 0.9f, 0.4f, 0}};
  }
  // test case to verify if first selected element for av call went down, then next desktop call
  // will select same element but av call will select next available element.
  @Test(
      dataProvider = "dataProviders",
      description = "verifying select element logic for element going down")
  public void selectElementMockDownAV(
      String msid, String mParam, float qValue1, float qValue2, int result) {
    LBFactory.setTCPTries(1);
    AbstractNextHop anh1 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0001,
            Transport.TCP,
            qValue1,
            new DsByteString("testSG1"));
    AbstractNextHop anh2 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0002,
            Transport.TCP,
            qValue1,
            new DsByteString("testSG2"));
    AbstractNextHop anh3 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0003,
            Transport.TCP,
            qValue2,
            new DsByteString("testSG3"));
    AbstractNextHop anh4 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0004,
            Transport.TCP,
            qValue2,
            new DsByteString("testSG4"));
    AbstractNextHop anh5 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0005,
            Transport.TCP,
            qValue2,
            new DsByteString("testSG5"));

    TreeSet<ServerGroupElementInterface> set = new TreeSet<ServerGroupElementInterface>();
    set.add(anh1);
    set.add(anh2);
    set.add(anh3);
    set.add(anh4);
    set.add(anh5);

    List<ServerGroupElementInterface> list = new ArrayList<ServerGroupElementInterface>();
    list.addAll(set);

    ServerGroupInterface serverGroup = mock(ServerGroupInterface.class);
    Mockito.when(serverGroup.getElements()).thenReturn(set);

    HashMap map1 = mock(HashMap.class);
    Mockito.when(map1.get(Mockito.any())).thenReturn(serverGroup);

    msidObj.setServerInfo(
        new DsByteString("SG2"), serverGroup, RequestBuilder.getRequestWithMParam(msid, mParam));
    msidObj.setDomainsToTry(set);
    AbstractNextHop firstSelectedElement = (AbstractNextHop) msidObj.getServer(null);

    // make the first selected element as down and add it to the set for the next call
    LBFactory.setTCPTries(0);
    anh2 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0002,
            Transport.TCP,
            qValue1,
            new DsByteString("testSG2"));
    set.add(anh2);

    // make desktop call where first selected element is down
    LBMsid msidObj2 = new LBMsid();
    msidObj2.setDomainsToTry(set);
    msidObj2.setServerInfo(
        new DsByteString("SG2"),
        serverGroup,
        RequestBuilder.getRequestWithMParam(msid, "applicationsharing"));
    AbstractNextHop secondSelectedElement = (AbstractNextHop) msidObj2.getServer(null);

    // make a av call where first element is down
    LBMsid msidObj3 = new LBMsid();
    msidObj3.setDomainsToTry(set);
    msidObj3.setServerInfo(
        new DsByteString("SG2"), serverGroup, RequestBuilder.getRequestWithMParam(msid, "audio"));
    AbstractNextHop thirdSelectedElement = (AbstractNextHop) msidObj3.getServer(null);

    assertEquals(firstSelectedElement, anh2);
    assertEquals(secondSelectedElement, anh2);
    assertEquals(thirdSelectedElement, anh1);
  }

  @DataProvider(name = "dataProviders1")
  public Object[][] dataProviders1() {
    return new Object[][] {
      {"1528940592285", "applicationsharing", 0.9f, 0.4f, 0},
      {"1528940592286", "audio", 0.5f, 0.8f, 2},
      {"1528940592286", "audio", 0.9f, 0.4f, 0}
    };
  }
  // test case to verify next element selection when first selected element goes down
  @Test(
      dataProvider = "dataProviders1",
      description =
          "creates a request object for for the given paramters, then test selectElement method of LBHashBasedMsid")
  public void selectElementMockDown(
      String msid, String mParam, float qValue1, float qValue2, int result) {
    LBFactory.setTCPTries(0);
    AbstractNextHop anh1 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0001,
            Transport.TCP,
            qValue1,
            new DsByteString("testSG1"));
    AbstractNextHop anh2 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0002,
            Transport.UDP,
            qValue1,
            new DsByteString("testSG2"));
    AbstractNextHop anh3 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0003,
            Transport.UDP,
            qValue2,
            new DsByteString("testSG3"));
    AbstractNextHop anh4 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0004,
            Transport.UDP,
            qValue2,
            new DsByteString("testSG4"));
    AbstractNextHop anh5 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost"),
            0005,
            Transport.UDP,
            qValue2,
            new DsByteString("testSG5"));

    TreeSet<ServerGroupElementInterface> set = new TreeSet<ServerGroupElementInterface>();
    set.add(anh1);
    set.add(anh2);
    set.add(anh3);
    set.add(anh4);
    set.add(anh5);

    List<ServerGroupElementInterface> list = new ArrayList<ServerGroupElementInterface>();
    list.addAll(set);

    ServerGroupInterface serverGroup = mock(ServerGroupInterface.class);
    Mockito.when(serverGroup.getElements()).thenReturn(set);

    HashMap map1 = mock(HashMap.class);
    Mockito.when(map1.get(Mockito.any())).thenReturn(serverGroup);

    msidObj.setServerInfo(
        new DsByteString("SG2"), serverGroup, RequestBuilder.getRequestWithMParam(msid, mParam));
    msidObj.setDomainsToTry(set);

    assertEquals(msidObj.getServer(null), list.get(result));
  }
}
