package com.cisco.dhruva.sip.loadbalancer;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.loadbalancer.LBMsid;
import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.ServerGroupInterface;
import com.cisco.dhruva.sip.loadbalancer.resources.RequestBuilder;
import com.cisco.dhruva.sip.servergroups.AbstractNextHop;
import com.cisco.dhruva.sip.servergroups.DefaultNextHop;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.transport.Transport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LBHashBasedMsidLargeDataSetTest {
  LBMsid msidObj;
  HashMap wrongSelectMap;
  LinkedList<Long> msidAV;
  LinkedList<ServerGroupElementInterface> avCalls;
  LinkedList<ServerGroupElementInterface> dsCalls;
  long msid;

  @BeforeMethod
  void setup() {
    msidObj = new LBMsid();
    wrongSelectMap = new HashMap<>();
    msidAV = new LinkedList<Long>();
    avCalls = new LinkedList<ServerGroupElementInterface>();
    dsCalls = new LinkedList<ServerGroupElementInterface>();
  }

  // input to test case : no of calls, no of elements, call Media Type
  @DataProvider(name = "dataProviders")
  public Object[][] dataProvider() {
    return new Object[][] {{50, 10, "audio", "applicationsharing"}};
  }

  @Test(
      dataProvider = "dataProviders",
      description =
          "creates a large no of SG elements and verifies load balancer for a large no of request objects")
  public void testMultipleCalls(
      int times, int elementsSize, String audio, String applicationsharing) {
    // select the element from load balancer for the for the given no of times for both audio and
    // applicationsharing calls.
    testCalls(times, elementsSize, audio);
    testCalls(times, elementsSize, applicationsharing);

    // assert selected element for audio call and application sharing call for the same msid
    assertEquals(avCalls, dsCalls);

    // assert if any element is wrongly selected after hashing.
    assertTrue(wrongSelectMap.isEmpty());
  }

  public void testCalls(int times, int elementsSize, String mParam) {
    for (int i = 0; i < times; i++) {
      // for the audio calls msid is generated from current time and the same msid is used for
      // applicationsharing calls
      if (mParam.equals("audio")) selectElementTest(elementsSize, mParam, 0.9f, 0.9f, 0);
      else selectElementTest(elementsSize, mParam, 0.9f, 0.9f, msidAV.get(i));
    }
  }

  public void selectElementTest(
      int elementsSize, String mParam, float qValue1, float qValue2, long msidAVCAll) {
    msidObj = new LBMsid();
    LBFactory.setTCPTries(1);
    // creating multiple ServerGroup elements with different transport
    TreeSet<ServerGroupElementInterface> set = new TreeSet<ServerGroupElementInterface>();
    for (int i = 0; i < elementsSize; i++) {
      set.add(
          new DefaultNextHop(
              new DsByteString("testNw"),
              new DsByteString("testHost"),
              i,
              Transport.UDP,
              qValue1,
              new DsByteString("SG2")));
    }

    List<ServerGroupElementInterface> list = new ArrayList<ServerGroupElementInterface>();
    list.addAll(set);

    // mocking ServerGroup getElements with a predefined SG elements
    ServerGroupInterface serverGroup = mock(ServerGroupInterface.class);
    Mockito.when(serverGroup.getElements()).thenReturn(set);

    HashMap map1 = new HashMap<>();
    map1.put(new DsByteString("SG2"), serverGroup);

    // for the audio calls create Ms-Conversation-Id from current time and store it in a list and
    // use the same list for DS calls
    if (mParam.equals("audio")) {
      msid = Instant.now().toEpochMilli();
      msidAV.add(msid);
    } else {
      msid = msidAVCAll;
    }
    // calculating hash for the given msid and for the defined number of SG elements, which will be
    // used to verify selected is correct.
    int hash = Math.abs(Long.toString(msid).hashCode() % 10);

    msidObj.setServerInfo(
        new DsByteString("SG2"),
        serverGroup,
        RequestBuilder.getRequestWithMParam(Long.toString(msid), mParam));
    AbstractNextHop selectedElement = (AbstractNextHop) msidObj.getServer(null);

    // if the selected element is wrong then put that element in a Map for assertion.
    if (selectedElement != list.get(hash)) wrongSelectMap.put(selectedElement, list.get(hash));

    // put all the audio and application sharing calls to List for assertion.
    if (mParam.equals("audio")) {
      avCalls.add(selectedElement);
    } else {
      dsCalls.add(selectedElement);
    }
  }
}
