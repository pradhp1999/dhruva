package com.cisco.dhruva.sip.servergroups;

/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME: $RCSfile: ServerGroup.java,v $
//
// MODULE:  lb
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
//
// MODIFICATIONS:
//
//
//////////////////////////////////////////////////////////////////////////////

import com.cisco.dhruva.config.sip.RE;
import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * This class implements a Server Group with the capability to notify listeners when the state of
 * the Server Group changes and a conversion into a CLI formatted command. It is assumed that
 * instances of this class are elements of ServerGroupRepository objects
 *
 * @see ServerGroup
 */
public class ServerGroup extends DefaultServerGroup {

  private static final Trace Log = Trace.getTrace(ServerGroup.class.getName());
  // some private strings
  private static final DsByteString colon = new DsByteString(":");

  /**
   * Constructs a new <code>ServerGroup</code> with the given name.
   *
   * @param name the name of this <code>ServerGroup</code>
   * @param lbType the type of load balancing for this server group.
   */
  public ServerGroup(DsByteString name, DsByteString network, int lbType, boolean pingOn) {
    super(name, network, lbType, pingOn);
    this.wasAvailable = true;
  }

  /**
   * Constructs a new <code>ServerGroup</code> with the given name.
   *
   * @param name the name of this <code>ServerGroup</code>
   * @param lbType the fully qualified class name of the type of load balancing for this server
   *     group.
   */
  public ServerGroup(DsByteString name, DsByteString network, String lbType, boolean pingOn) {
    super(name, network, lbType, pingOn);
    this.wasAvailable = true;
  }

  protected ServerGroup(
      DsByteString name, DsByteString network, TreeSet elements, String lbType, boolean pingOn) {
    super(name, network, elements, lbType, pingOn);
    this.wasAvailable = true;
  }

  public ServerGroup(
      DsByteString name, DsByteString network, TreeSet elements, int lbType, boolean pingOn) {
    super(name, network, elements, lbType, pingOn);
    this.wasAvailable = true;
  }

  public void addServerGroupListener(ServerGroupListener serverGroupListener) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering " + name + ".addServerGroupListener()");
    if (listeners == null) {
      listeners = new HashSet();
    }
    listeners.add(serverGroupListener);
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving addServerGroupListener()");
  }

  /**
   * Overrides Object
   *
   * @return the ServerGroup in CLI command format
   */
  public String toString() {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering toString()");
    String value;
    HashMap elementMap = new HashMap();
    elementMap.put(RE.reSgName, name);
    elementMap.put(RE.reSgLbType, LBFactory.getLBTypeAsString(lbType));

    // MIGRATION
    value = elementMap.toString();
    /*
    try {
      value = UmsCliUtil.buildSyntax(UmsReaderFactory.getInstance().getReader(), RE.dsReSg, elementMap, true);
    }
    catch (Throwable t) {
      if (Log.isEnabledFor(Level.WARN)) Log.warn("Error converting server group " + name + " to CLI command: " + t.getMessage(), t);
    }
    */
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving toString(), returning " + value);
    return value;
  }

  public void endpointUnreachable(EndpointEvent e) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering endpointUnreachable()");

    super.endpointUnreachable(e);

    // Replace this with Audit logging
    // Log4j 2 migration commenting out as we have disabled DsLoggingFactoryMgr
    /* try
            {
               // DsLoggingFactory lf = DsLoggingFactoryMgr.getFactory("unreachable-endpoint");
                if (lf.isEnabled())
                {

                    DsByteString tmp = new DsByteString(e.getEndPoint().getHost());
                    tmp.append(colon).append(DsByteString.valueOf(e.getEndPoint().getPort())).append(colon).append(DsSipTransportType.getTypeAsString(e.getEndPoint().getProtocol()).getBytes());

                    REFailLogEvent event = new REFailLogEvent(tmp.toString(), name.toString());
                  //  lf.getLogger().log(event);
                }
            }
            catch (DsNoSuchFactoryException dnsfe)
            {
                if (Log.isEnabled(Level.WARN))
                    Log.warn("Could not get logger for unreachable-endpoint event: " + dnsfe.getMessage(), dnsfe);
            }
    */
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointUnreachable()");
  }
}
