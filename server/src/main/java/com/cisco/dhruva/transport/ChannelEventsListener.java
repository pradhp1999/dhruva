/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import java.net.InetSocketAddress;

public interface ChannelEventsListener {

  void connectionActive(
      InetSocketAddress localAddress,
      InetSocketAddress remoteAddress,
      Transport transport,
      Connection connection);

  void connectionInActive(
      InetSocketAddress localAddress, InetSocketAddress remoteAddress, Transport transport);

  void onException(Throwable throwable);
}
