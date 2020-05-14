package com.cisco.dhruva.sip.servergroups;

import java.util.TreeSet;

public interface ServerGroupInterface {

  /**
   * Gets all the <code>ServerGroupElement</code>s in this <code>ServerGroup</code>. This is the
   * orginal list: modification to the list will modify the original list itself.
   *
   * @return a TreeSet of <code>ServerGroupElement</code>.
   */
  public TreeSet getElements();

  /**
   * Gets the load balancing type of this server group.
   *
   * @return the load balancing type of this server group.
   */
  public int getLBType();

  /**
   * Gets the load balancing type of this server group.
   *
   * @return the load balancing type of this server group.
   */
  public String getStrLBType();
}
