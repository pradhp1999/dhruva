/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

/** An interface used to do loop detection in Proxy Core. */
public interface DsLoopCheckInterface {

  /**
   * Checks if the request is looped, normally by examining the list of Via headers and request URI
   * but other algorithms may also be implemented
   *
   * @param request the SIP request in question
   */
  public boolean isLooped(DsSipRequest request);

  /**
   * returns the branch ID to be used in the Via inserted by the proxy core.
   *
   * @param n_branch an integer representing the branch of this request (starts with 1 and gets
   *     incremented every time we fork)
   * @param request request being forwarded
   */
  public DsByteString getBranchID(int n_branch, DsSipRequest request);
}
