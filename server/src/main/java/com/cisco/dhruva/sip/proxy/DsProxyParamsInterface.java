/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyBranchParamsInterface.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.transport.Transport;

/** Describes configuration settings of a ProxyTransaction */
public interface DsProxyParamsInterface extends DsProxyBranchParamsInterface {

  /** @return default SIP port number */
  int getDefaultPort();

  /**
   * @return the interface to be inserted into Record-Route if the transport parameter of that
   *     interface is NONE, no transport parameter will be used in R-R; otherwise, the transport of
   *     this interface will be used.
   */
  DsSipRecordRouteHeader getRecordRouteInterface(String direction);

  /**
   * @param protocol UDP or TCP
   * @return the address and port number that needs to be inserted into the Via header for a
   *     specific protocol used
   */
  DsViaListenInterface getViaInterface(Transport protocol, String direction);

  /**
   * @return default protocol we are listening on (one of the constants defined in
   *     DsSipTransportType.java) //This is used in Record-Route, for example This is not really
   *     used by the proxy core anymore
   */
  Transport getDefaultProtocol();
}
