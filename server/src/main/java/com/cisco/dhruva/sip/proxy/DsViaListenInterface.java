/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsViaListenInterface.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;


import java.net.InetAddress;


/** the class is used to  describe an interface to insert into Via header (port/protocol for now)
 * to listen on. The proxy uses it to populate Via and Record-Route
 */
public interface DsViaListenInterface extends DsListenInterface {

  /**
   * @return port to be used as the source port of outgoing requests
   */
  public int getSourcePort();

  /**
   * @return the interface to be used as the source port of outgoing requests
   */
  public InetAddress getSourceAddress();

}
