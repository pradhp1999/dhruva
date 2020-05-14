/*
 * Copyright (c) 2001-2002, 2003-2015 by cisco Systems, Inc.
 * All rights reserved.
 */
/*
 * Created by IntelliJ IDEA.
 * User: bjenkins
 * Date: Sep 11, 2002
 * Time: 9:46:04 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.util.log.Trace;
import javax.management.Notification;

public class SGTraps implements ServerGroupListener, EndpointListener {

  private static final Trace Log = Trace.getTrace(SGTraps.class.getName());

  private static SGTraps _instance = null;

  private static volatile boolean snmpTrapEnabled;

  private SGTraps() {}

  private static final String ELEMENT_STATUS_NOTIFICATION = "Element Status Notification";
  private static final String SG_STATUS_NOTIFICATION = "Server Group Notification";
  private static long seqNo;
  private static long sgSeqNO;

  static {
    _instance = new SGTraps();
  }

  public static SGTraps getInstance() {
    return _instance;
  }

  public static boolean getSnmpTrapEnabled() {
    return snmpTrapEnabled;
  }

  public static void setSnmpTrapEnabled(boolean trapEnabled) {
    snmpTrapEnabled = trapEnabled;
  }

  public static void configError() {
    System.out.println("Notification for events sample");
    try {
      Notification n =
          new Notification(
              SG_STATUS_NOTIFICATION, "Test Object", 1, "Server Group already present");
      String[] data = new String[2];
      data[0] = "192.168.1.1";
      data[1] = "2";
      n.setUserData(data);
      SIPServerStatusMBeanImpl.getInstance().sendNotification(n);
    } catch (Throwable t) {
      System.out.println("Notification POC failed with exception");
    }
  }

  public void servergroupUnreachable(ServerGroupEvent e) {}

  public void servergroupUnreachableClear(ServerGroupEvent e) {}

  public void endpointUnreachable(EndpointEvent e) {}

  public void endpointOverloaded(EndpointEvent e) {
    if (Log.on && Log.isTraceEnabled())
      Log.trace("Entering endpointOverloaded(" + e.getEndPoint().toString() + ")");
    // MIGRATION
    /*
    MgmtNotification mn = new MgmtNotification(SG.sgTrapElementOverloaded, e.getSource(), 0);
    mn.bind(SG.sgElementOverloadedIP, e.getEndPoint().getHost());
    mn.bind(SG.sgElementOverloadedPort, new Integer(e.getEndPoint().getPort()));
    mn.bind(SG.sgElementOverloadedTransport, DsSipTransportType.getTypeAsString(e.getEndPoint().getProtocol()));
    mn.bind(SG.sgElementOverloadedMessage, "This endpoint is overloaded");
    sendNotification(mn);
    */
    if (Log.on && Log.isInfoEnabled())
      Log.info("Server " + e.getEndPoint().toString() + " is overloaded.");
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointOverloaded()");
  }

  public void endpointUnreachableClear(EndpointEvent e) {}

  public void endpointOverloadedClear(EndpointEvent e) {
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Entering endpointOverloadedClear(" + e.getEndPoint().toString() + ")");
    // MIGRATION
    /*
    MgmtNotification mn = new MgmtNotification(SG.sgTrapElementOverloadedClear, e.getSource(), 0);
    mn.bind(SG.sgElementOverloadedClearIP, e.getEndPoint().getHost());
    mn.bind(SG.sgElementOverloadedClearPort, new Integer(e.getEndPoint().getPort()));
    mn.bind(SG.sgElementOverloadedClearTransport, DsSipTransportType.getTypeAsString(e.getEndPoint().getProtocol()));
    mn.bind(SG.sgElementOverloadedClearMessage, "The Retry-After period for this server group element has expired.");
    sendNotification(mn);
    */
    Log.warn("Server " + e.getEndPoint().toString() + " is no longer overloaded.");
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving endpointOverloadedClear()");
  }

  // MIGRATION
  /*
  private static void sendNotification(MgmtNotification mn) {
    // Send notification.
    MgmtNotificationServiceMBean mns = MgmtNotificationService.getInstance();
    try {
      if (mns != null) mns.sendNotification(mn);
    }
    catch (Throwable t) {
      if (Log.on && Log.isEnabledFor(Level.WARN)) Log.warn("WARNING: Unable to send notification " + t.getMessage(), t);
    }
  }
  */
}
