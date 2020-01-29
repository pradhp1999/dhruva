// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

/**
 * The interface that provides for the next unique branch ID for the Via Header. This is the branch
 * ID that will be used as the transaction key in the bis-09 scenario.
 */
public interface DsSipBranchIdInterface {
  /**
   * Returns the next unique Branch ID for the Via Header for the specified SIP Request. The passed
   * in request should not be changed.
   *
   * @param request the request whose branch parameter needs to be updated. This request is just for
   *     the information and should not be changed by the user implementation.
   * @return the next unique Branch ID for the Via Header for the next SIP Request.
   */
  public DsByteString nextBranchId(DsSipRequest request);
}
