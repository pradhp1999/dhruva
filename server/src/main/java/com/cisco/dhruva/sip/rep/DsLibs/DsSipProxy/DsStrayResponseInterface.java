/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

/**
 * The stray response interface defines callbacks from the proxy manager for stray responses. It is
 * needed to do some extra processing on responses. It is suggested that the callback calls
 * DsSipProxyManager.proxyStrayResponse() as the last statement.
 */
public interface DsStrayResponseInterface {
  /**
   * Response that received without an associated transaction
   *
   * @param response response that was received
   */
  void strayResponse(DsSipResponse response);
}
