/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.servergroups.DsLibs.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;

/**
 * This interface defines the methods that are used by objects external to the loadbalancer package
 * to access the necessary data members.
 */
public interface LBInterface {

  /**
   * This method performs the appropriate load balancing algorithm to determine the next hop.
   * Successive calls to this method during the same transaction should return another potential
   * next hop, but SHOULD NOT consider <code>ServerGroupElement</code>s which have already been
   * attempted as valid next hops.
   *
   * @return the <code>ServerInterface</code> that is the next best hop.
   */
  public ServerInterface getServer();

  /**
   * This method performs the appropriate load balancing algorithm to determine the next hop, using
   * the passed in key (varKey), to perform the hashing.
   */
  public ServerInterface getServer(DsByteString varKey);

  /**
   * Gets the last server that was tried.
   *
   * @return the last server tried.
   */
  public ServerInterface getLastServerTried();

  /**
   * Gets the number of remaining elements that can be routed to.
   *
   * @return the number of remaining elements that can be routed to.
   */
  public int getNumberOfUntriedElements();
}
