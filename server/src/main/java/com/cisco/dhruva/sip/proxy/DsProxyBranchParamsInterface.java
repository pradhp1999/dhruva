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

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.transport.Transport;

/**
 * Encapsulates parameters that can be passed to ProxyTransaction API calls (i.e., only proxyTo()
 * roght now) to modify the default behavior for a single branch NOTE: The API for this class might
 * change as OAM&P matures
 */
public interface DsProxyBranchParamsInterface {

  /**
   * Specifies whether the proxy needs to insert itself into the Record-Route
   *
   * @return Record-Route setting
   */
  public boolean doRecordRoute();

  /**
   * Returns the address to proxy to
   *
   * @return the address to proxy to, null if the default forwarding logic is to be used
   */
  public DsByteString getProxyToAddress();

  /**
   * Returns port to proxy to
   *
   * @return the port to proxy to; if -1 is returned, default port will be used
   */
  public int getProxyToPort();

  /** @return protocol to use for outgoing request */
  public Transport getProxyToProtocol();

  /**
   * @return the timeout value in milliseconds for outgoing requests. -1 means default timeout This
   *     allows to set timeout values that are _lower_ than SIP defaults. Values higher than SIP
   *     deafults will have no effect.
   */
  public long getRequestTimeout();

  public String getRequestDirection();

  public DsByteString getRecordRouteUserParams();
}
