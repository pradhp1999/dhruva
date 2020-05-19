/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

/**
 * This class defines all the methods that must be implemented by a load balancer That are exposed
 * to the LBFactory.
 *
 * @see LBFactory
 */
public interface RepositoryReceiverInterface extends LBInterface {

  /**
   * Gives the load balancer all the information necessary to pick the next hop.
   *
   * @param serverGroupName the name of the server group that this load balancer is created for.
   * @param serverGroups the entire server group repository.
   * @param key the key used for the hash algorithm.
   */
  void setServerInfo(
      DsByteString serverGroupName, ServerGroupInterface serverGroup, DsSipRequest request);
}
