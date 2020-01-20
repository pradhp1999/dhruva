/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class UDPConnectionTest {

  @Test(
      enabled = false,
      description =
          "Send byte[] data to UDPConnection using send when connection is active , and then check the received data at the channel")
  public void testSendWhenConnectionActive() {}

  @Test(
      enabled = false,
      description =
          "Send byte[] data to UDPConnection using send when connection is Inactive ,Future should return Exception")
  public void testSendWhenConnectionInActive() {}

  @Test(
      enabled = false,
      description =
          "Send byte[] data to UDPConnection using send when connection is in Closed state , Future should return ChannelClosedException")
  public void testSendWhenConnectionClosed() {}
}
