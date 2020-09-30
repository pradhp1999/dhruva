/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public interface Server {

  void startListening(InetAddress address, int port, CompletableFuture serverStartFuture);

  public void addConnectionEventHandler(ChannelEventsListener connectionEventHandler);
}
