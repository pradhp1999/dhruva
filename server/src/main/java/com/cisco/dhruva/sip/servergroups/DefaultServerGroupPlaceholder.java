/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/*
 * Created by IntelliJ IDEA.
 * User: bjenkins
 * Date: Aug 28, 2002
 * Time: 9:42:41 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashMap;

public class DefaultServerGroupPlaceholder extends AbstractServerGroupPlaceholder {

  private static final Trace Log = Trace.getTrace(DefaultServerGroupPlaceholder.class.getName());

  private DefaultServerGroup myServerGroup = null;

  /**
   * Creates a new ServerGroupPlaceholder with the given name and q-value. This constructor must
   * always be used when adding a DefaultServerGroupPlaceholder to a DefaultServerGroupRepository
   *
   * @param myServerGroup the server group this placeholder is wrapping
   * @param qValue the q-value of this server group element.
   * @param parentGroupName the name of the server group this server group is being added to.
   */
  public DefaultServerGroupPlaceholder(
      DefaultServerGroup myServerGroup, float qValue, DsByteString parentGroupName) {
    super(myServerGroup.getName(), qValue, parentGroupName);
    this.myServerGroup = myServerGroup;
  }

  /**
   * Creates a new ServerGroupPlaceholder with the given name and q-value. This constructor should
   * only be used when you need to reference an existing DefaultServerGroupPlaceholder for the same
   * <code>myServerGroupName</code>
   *
   * @param myServerGroupName the server group this placeholder is wrapping
   * @param qValue the q-value of this server group element.
   * @param parentGroupName the name of the server group this server group is being added to.
   * @see DefaultServerGroupRepository#removeElement(ServerGroupElement, AbstractServerGroup)
   */
  public DefaultServerGroupPlaceholder(
      DsByteString myServerGroupName, float qValue, DsByteString parentGroupName) {
    super(myServerGroupName, qValue, parentGroupName);
  }

  public boolean isAvailable() {
    if (myServerGroup != null) return myServerGroup.isAvailable();
    return false;
  }

  public String toString() {
    String value = null;
    HashMap elementMap = new HashMap();
    elementMap.put(SG.sgSgElementSgName, parent);
    elementMap.put(SG.sgSgElementSgReference, getServerGroupName());
    elementMap.put(SG.sgSgElementQValue, String.valueOf(getQValue()));
    elementMap.put(SG.sgSgElementWeight, String.valueOf(getWeight()));

    // MIGRATION
    value = elementMap.toString();
    /*
    try {
      value = UmsCliUtil.buildSyntax(UmsReaderFactory.getInstance().getReader(),
                                     SG.dsSgSgElement, elementMap, true);
    }
    catch (Throwable t) {
      if (Log.on && Log.isEnabledFor(Level.WARN))
        Log.warn("Error converting server group element " + getServerGroupName() +
                 " " + getQValue() + " to CLI command", t);
    }
    */
    return value;
  }

  public Object clone() {
    return new DefaultServerGroupPlaceholder(
        this.myServerGroup, this.getQValue(), this.getParent());
  }
}
