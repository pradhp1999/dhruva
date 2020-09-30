package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;

/**
 * This interface defines all the methods that are needed to perform load balancing on a sub server
 * group.
 */
public interface SubServerGroupInterface {

  /**
   * Gets the name of the sub server group.
   *
   * @return the name of the sub server group.
   */
  public DsByteString getServerGroupName();
}
