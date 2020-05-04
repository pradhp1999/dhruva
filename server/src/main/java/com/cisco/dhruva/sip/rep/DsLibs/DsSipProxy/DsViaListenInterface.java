/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import java.net.InetAddress;

/**
 * the class is used to describe an interface to insert into Via header (port/protocol for now) to
 * listen on. The proxy uses it to populate Via and Record-Route
 */
public interface DsViaListenInterface extends DsListenInterface {

  /** @return port to be used as the source port of outgoing requests */
  public int getSourcePort();

  /** @return the interface to be used as the source port of outgoing requests */
  public InetAddress getSourceAddress();
}
