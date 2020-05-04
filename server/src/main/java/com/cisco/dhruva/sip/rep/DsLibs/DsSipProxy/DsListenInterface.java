/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.transport.Transport;

/**
 * the class is used to describe an interface (port/protocol for now) to listen on. The proxy uses
 * it to populate Via and Record-Route
 */
public interface DsListenInterface {

  /** @return port to insert into Via header */
  public int getPort();

  /** @return protocol to insert into Via header */
  public Transport getProtocol();

  /** @return the interface to insert into Via header */
  public DsByteString getAddress();

  //   public int getInterfaceIndicator();

}
