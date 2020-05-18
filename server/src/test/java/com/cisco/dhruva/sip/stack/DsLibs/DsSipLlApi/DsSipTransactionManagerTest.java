/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import static org.mockito.Mockito.mock;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPMessageGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DsSipTransactionManagerTest {

  private DsSipTransactionManager sipTransactionManager;
  private DsSipTransportLayer transportLayer;
  private DsSipStrayMessageInterface strayMessageInterface;
  private DsSipRequestInterface requestInterface;
  private DsSipTransactionEventInterface transactionEventInterface;
  private DsBindingInfo incomingMessageBindingInfo;

  @BeforeClass
  public void init() throws UnknownHostException {
    transportLayer = mock(DsSipTransportLayer.class);
    strayMessageInterface = mock(DsSipStrayMessageInterface.class);
    requestInterface = mock(DsSipRequestInterface.class);
    transactionEventInterface = mock(DsSipTransactionEventInterface.class);

    InetAddress localAddress = InetAddress.getByName("127.0.0.1");
    InetAddress remoteAddress = InetAddress.getByName("127.0.0.1");
    int localPort = 5060, remotePort = 5070;
    incomingMessageBindingInfo =
        new DsBindingInfo(localAddress, localPort, localAddress, localPort, Transport.UDP);

    try {
      sipTransactionManager = new DsSipTransactionManager(transportLayer, requestInterface);
    } catch (DsException e) {
      Assert.fail(
          "DsSipTransactionManagerTest DsSipTransactionManager " + "object creation failed " + e);
    }

    DsSipTransactionManager.setProxyServerMode(true);
    sipTransactionManager.setStrayMessageInterface(strayMessageInterface);
    sipTransactionManager.setTransactionEventInterface(transactionEventInterface);
  }

  @Test(
      description =
          "Testing the Invite Processing by the Transaction Manager, "
              + "checks if Message is parseed ,Session is created ,SessionId header is added and Message is "
              + "forwarded to requestInterface ")
  public void testInviteProcessingInTransactionManager() {

    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    sipTransactionManager.processMessageBytes(sipMessageBytes);
  }
}
