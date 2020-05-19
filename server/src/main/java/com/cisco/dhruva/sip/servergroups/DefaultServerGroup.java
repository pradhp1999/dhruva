/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/*
 * User: bjenkins
 * Date: Mar 25, 2002
 * Time: 1:12:55 PM
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.util.*;

/**
 * This class implements a Server Group with the capability to notify listeners when the state of
 * the Server Group changes. It is assumed that instances of this class are elements of
 * DefaultServerGroupRepository objects and contain DefaultNextHop objects.
 *
 * @see DefaultServerGroupRepository
 */
public class DefaultServerGroup extends AbstractServerGroup
    implements EndpointListener, ServerGroupListener {

  protected static final Trace Log = Trace.getTrace(DefaultServerGroup.class.getName());
  protected ServerGroupEvent clearEvent = null;
  protected ServerGroupEvent unreachableEvent = null;
  protected HashSet listeners = null;
  private int unreachable = 0;
  private int overloaded = 0;
  protected boolean wasAvailable = false;

  /**
   * Constructs a new <code>AbstractServerGroup</code> with the given name.
   *
   * @param name the name of this <code>AbstractServerGroup</code>
   * @param lbType the type of load balancing for this server group.
   */
  public DefaultServerGroup(DsByteString name, DsByteString network, int lbType, boolean pingOn) {
    this(name, network, null, lbType, pingOn);
  }

  /**
   * Constructs a new <code>AbstractServerGroup</code> with the given name.
   *
   * @param name the name of this <code>AbstractServerGroup</code>
   * @param lbType the fully qualified class name of the type of load balancing for this server
   *     group.
   */
  public DefaultServerGroup(
      DsByteString name, DsByteString network, String lbType, boolean pingOn) {
    this(name, network, null, lbType, pingOn);
  }

  /**
   * Constructs a new <code>AbstractServerGroup</code> with the given name and the given <code>
   * TreeSet</code> full of <code>ServerGroupElement</code> objects.
   *
   * @param name the name of this <code>AbstractServerGroup</code>
   * @param elements a <code>ArrayList</code> of <code>ServerGroupElement</code>s.
   * @param lbType the type of load balancing for this server group.
   */
  protected DefaultServerGroup(
      DsByteString name, DsByteString network, TreeSet elements, int lbType, boolean pingOn) {

    super(name, network, elements, lbType, pingOn);
    clearEvent = new ServerGroupEvent(this, ServerGroupEvent.CLEAR_UNREACHABLE);
    unreachableEvent = new ServerGroupEvent(this, ServerGroupEvent.UNREACHABLE);
  }

  /**
   * Constructs a new <code>AbstractServerGroup</code> with the given name and the given <code>
   * TreeSet</code> full of <code>ServerGroupElement</code> objects.
   *
   * @param name the name of this <code>AbstractServerGroup</code>
   * @param elements a <code>ArrayList</code> of <code>ServerGroupElement</code>s.
   * @param lbType fully qualified class name of the type of load balancing for this server group.
   */
  protected DefaultServerGroup(
      DsByteString name, DsByteString network, TreeSet elements, String lbType, boolean pingOn) {
    super(name, network, elements, lbType, pingOn);
    clearEvent = new ServerGroupEvent(this, ServerGroupEvent.CLEAR_UNREACHABLE);
    unreachableEvent = new ServerGroupEvent(this, ServerGroupEvent.UNREACHABLE);
  }

  protected boolean addElement(ServerGroupElement element) {
    boolean b = super.addElement(element);
    if (b) {
      if (element.isAvailable()) unreachableClear();
    }
    return b;
  }

  /**
   * Overrides Object
   *
   * @return A String representation of this object
   */
  public String toString() {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering toString()");
    String value = null;
    HashMap elementMap = new HashMap();
    elementMap.put(SG.sgSgName, name);
    elementMap.put(SG.sgSgLbType, LBFactory.getLBTypeAsString(lbType));
    // MIGRATION
    value = elementMap.toString();
    /*
    try {
      value = UmsCliUtil.buildSyntax(UmsReaderFactory.getInstance().getReader(),
                                     SG.dsSgSg, elementMap, true);
    }
    catch (Throwable t) {
      if (Log.on && Log.isEnabledFor(Level.WARN))
        Log.warn("Error converting server group " + name + " to CLI command", t);
    }
    */
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving toString(), returning " + value);
    return value;
  }

  public void addServerGroupListener(ServerGroupListener e) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering " + name + ".addServerGroupListener()");
    if (listeners == null) {
      listeners = new HashSet();
    }
    listeners.add(e);
    if (!isAvailable()) {
      e.servergroupUnreachable(unreachableEvent);
    }
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving addServerGroupListener()");
  }

  public void removeServerGroupListener(ServerGroupListener e) {
    if (Log.on && Log.isTraceEnabled())
      Log.trace("Entering " + name + ".removeServerGroupListener()");
    if (listeners != null) listeners.remove(e);
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving removeServerGroupListener()");
  }

  public void removeAllListeners() {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering " + name + ".removeAllListeners()");
    listeners = null;
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving removeAllListeners()");
  }

  protected void notifyListeners(int type) {
    if (Log.on && Log.isInfoEnabled()) {
      String status = "down";
      if (type == ServerGroupEvent.CLEAR_UNREACHABLE) status = "up";
      Log.info(
          "Entering notifyListeners(" + type + "), " + "server group " + name + " is " + status);
    }
    if ((listeners != null) && (listeners.size() > 0)) {
      for (Iterator i = listeners.iterator(); i.hasNext(); ) {
        ServerGroupListener l = (ServerGroupListener) i.next();
        switch (type) {
          case ServerGroupEvent.UNREACHABLE:
            l.servergroupUnreachable(unreachableEvent);
            break;
          case ServerGroupEvent.CLEAR_UNREACHABLE:
            l.servergroupUnreachableClear(clearEvent);
            break;
        }
      }
    }
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving notifyListeners()");
  }

  protected void cleanUp(ServerGroupElement element) {
    if (element.isNextHop()) {
      DefaultNextHop nh = (DefaultNextHop) element;
      nh.removeEndPointListener(this);
    }
    servergroupUnreachable(null);
  }

  /**
   * **************************************************** Methods overridding AbstractServerGroup
   * ****************************************************
   */

  /**
   * Removes the given element from the server group.
   *
   * @param element the server group element to remove.
   */
  protected boolean remove(ServerGroupElement element) {
    boolean b = super.remove(element);
    if (b) {
      cleanUp(element);
    }
    return b;
  }

  protected Set removeAll() {
    Set s = super.removeAll();
    if (s != null) {
      for (Iterator i = s.iterator(); i.hasNext(); ) {
        cleanUp((ServerGroupElement) i.next());
      }
    }
    return s;
  }

  /**
   * Gives the availability of this server group, in this case, whether or not the number of down
   * elements is less than the size of the server group. Subclasses should override this method if
   * they have some other definition of whether or not the server group is available.
   *
   * @return <code>true</code> if the size of the server group is greater than zero, <code>false
   *     </code> otherwise.
   */
  public boolean isAvailable() {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering isAvailable()");
    boolean success = true;
    if (size() == 0) success = false;
    else {
      if ((unreachable + overloaded) >= size()) {
        for (Iterator i = elements.iterator(); i.hasNext(); ) {
          ServerGroupElement sge = (ServerGroupElement) i.next();
          success = sge.isAvailable();
          if (success) break;
        }
      }
    }
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving isAvailable(), returning " + success);
    return success;
  }

  /**
   * ************************************************ ServerGroupListener methods
   * ************************************************
   */
  public void servergroupUnreachable(ServerGroupEvent e) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering servergroupUnreachable()");
    if (unreachable < size()) unreachable++;
    unreachable();
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving servergroupUnreachable()");
  }

  public void servergroupUnreachableClear(ServerGroupEvent e) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering servergroupUnreachableClear()");
    if (unreachable > 0) unreachable--;
    unreachableClear();
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving servergroupUnreachableClear()");
  }

  /**
   * ******************************************************* EndpointListener Methods
   * *******************************************************
   */
  public void endpointUnreachable(EndpointEvent e) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering endpointUnreachable()");
    if (unreachable < size()) unreachable++;
    unreachable();
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointUnreachable()");
  }

  public void endpointOverloaded(EndpointEvent e) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering endpointOverloaded()");
    if (overloaded < size()) overloaded++;
    unreachable();
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointOverloaded()");
  }

  public void endpointUnreachableClear(EndpointEvent e) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering endpointUnreachableClear()");
    if (unreachable > 0) unreachable--;
    unreachableClear();
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointUnreachableClear()");
  }

  public void endpointOverloadedClear(EndpointEvent e) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering endpointOverloadedClear()");
    if (overloaded > 0) overloaded--;
    unreachableClear();
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointOverloadedClear()");
  }

  protected void unreachable() {
    if (!isAvailable()) {
      wasAvailable = false;
      notifyListeners(ServerGroupEvent.UNREACHABLE);
    }
  }

  protected void unreachableClear() {
    if (!wasAvailable) {
      notifyListeners(ServerGroupEvent.CLEAR_UNREACHABLE);
    }
    wasAvailable = true;
  }
}
