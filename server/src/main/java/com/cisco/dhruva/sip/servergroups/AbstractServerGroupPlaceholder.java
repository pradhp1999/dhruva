/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.SubServerGroupInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;

/**
 * This class is a placeholder for a sub server group contained in a server group. It is a wrapper
 * around the sub server group name.
 *
 * @see ServerGroupElement
 * @see SubServerGroupInterface
 */
public abstract class AbstractServerGroupPlaceholder extends ServerGroupElement
    implements SubServerGroupInterface {

  private DsByteString serverGroupName = null;

  /**
   * Creates a new ServerGroupPlaceholder with the given name and q-value.
   *
   * @param serverGroupName the name of the sub server group.
   * @param qValue the q-value of this server group element.
   */
  public AbstractServerGroupPlaceholder(
      DsByteString serverGroupName, float qValue, DsByteString parentGroupName) {
    this.serverGroupName = serverGroupName;
    setQValue(qValue);
    setParent(parentGroupName);
  }

  public AbstractServerGroupPlaceholder(DsByteString parentGroupName) {
    this(null, ServerGroupElement.DEFAULT_Q, parentGroupName);
  }

  /**
   * Gets the sub server group name.
   *
   * @return the sub server group name.
   */
  public final DsByteString getServerGroupName() {
    return serverGroupName;
  }

  public final void setServerGroupName(DsByteString serverGroupName) {
    this.serverGroupName = serverGroupName;
  }

  /**
   * Determines if the given object has the same server group name and <code>qValue</code> as this
   * object.
   *
   * @param obj the object to compare.
   * @return <code>true</code> if this object is equal to the given object.
   */
  public final boolean equals(Object obj) {
    if (obj == null) return false;
    try {
      AbstractServerGroupPlaceholder sgp = (AbstractServerGroupPlaceholder) obj;
      if (Float.compare(getQValue(), sgp.getQValue()) == 0)
        if (getServerGroupName().equals(sgp.getServerGroupName())) return true;
    } catch (ClassCastException cce) {
    }
    return false;
  }

  /**
   * ******************************************************** ServerGroupElement methods *
   * ********************************************************
   */

  /**
   * Determines if the given <code>ServerGroupElementInterface</code> has the same sub server group
   * name as this <code>ServerGroupPlaceholder</code>.
   *
   * @param element the <code>ServerGroupElementInterface</code> to compare.
   * @return <code>true</code> if the given element has the same sub server group name, <code>false
   *     </code> otherwise.
   */
  public final boolean isSameReferenceTo(ServerGroupElementInterface element) {
    if (!element.isNextHop()) {
      AbstractServerGroupPlaceholder sgp = (AbstractServerGroupPlaceholder) element;
      return serverGroupName.equals(sgp.serverGroupName);
    }
    return false;
  }

  /**
   * Compares this server group placeholder to the given object.
   *
   * @return a positive integer if this placeholder should come after the given object the supplied
   *     object in a sorted list, a negative integer if this placeholder should come before the
   *     given object in a sorted list, or zero if the two objects are equivalent.
   * @param obj the object to compare.
   * @throws ClassCastException
   */
  public final int compareTo(Object obj) throws ClassCastException {
    int compare = super.compareTo(obj);
    if (compare != 0) return compare;
    ServerGroupElement e = (ServerGroupElement) obj;
    if (e.isNextHop()) compare = -1;
    else {
      AbstractServerGroupPlaceholder sgp = (AbstractServerGroupPlaceholder) e;
      compare = getServerGroupName().compareTo(sgp.getServerGroupName());
    }
    return compare;
  }

  /**
   * Overrides Object
   *
   * @return A String representation of this object
   */
  public abstract String toString();
}
