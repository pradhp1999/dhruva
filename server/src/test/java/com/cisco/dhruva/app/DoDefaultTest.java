package com.cisco.dhruva.app;

import static org.testng.Assert.*;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.cisco.dhruva.app.CallFlow.DoCallFlowForRequest;
import com.cisco.dhruva.app.CallProcessor.RouteResponse;
import com.cisco.dhruva.common.messaging.DhruvaMessageImpl;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class DoDefaultTest {

  static final ActorTestKit testKit = ActorTestKit.create();

  @Test
  public void testHandleRequest() {

    ActorRef<CallFlow.Command> defaultCallFlowActor =
        testKit.spawn(DoDefault.create(), "defaultCallFlowActor");
    TestProbe<RouteResponse> responseProbe = testKit.createTestProbe();
    IDhruvaMessage iDhruvaMessage = new DhruvaMessageImpl(null, null, null);
    iDhruvaMessage.setReqURI("guru@webex.com");
    defaultCallFlowActor.tell(new DoCallFlowForRequest(responseProbe.ref(), iDhruvaMessage));
    System.out.println(responseProbe.receiveMessage().destination);
  }

  @AfterClass
  public static void cleanup() {
    testKit.shutdownTestKit();
  }
}
