/*
 * Copyright (c) 2015 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.util.log.Trace;
import javax.management.*;
import javax.management.openmbean.OpenDataException;

public class SIPServerStatusMBeanImpl extends StandardMBean
    implements SIPServerStatusMBean, NotificationEmitter {

  protected static Trace Log = Trace.getTrace(SIPServerStatusMBeanImpl.class.getName());

  private static SIPServerStatusMBeanImpl _singleton;

  private static ObjectName objectName;

  private static NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

  public SIPServerStatusMBeanImpl()
      throws NotCompliantMBeanException, MalformedObjectNameException, OpenDataException {
    super(SIPServerStatusMBean.class);

    objectName = new ObjectName("com.cisco.cusp.mgmt.mbeans" + ":type=SIPServerStatusMBean");
  }

  public static SIPServerStatusMBeanImpl getInstance()
      throws NotCompliantMBeanException, OpenDataException, MalformedObjectNameException {
    if (_singleton == null) {
      _singleton = new SIPServerStatusMBeanImpl();
    }

    return _singleton;
  }

  public ObjectName getObjectName() {
    return objectName;
  }

  public void removeNotificationListener(
      NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener, filter, handback);
    if (Log.on && Log.isInfoEnabled())
      Log.info(
          "removing notification: listener->"
              + listener
              + " filter->"
              + filter
              + " handbackObj->"
              + handback);
  }

  public void addNotificationListener(
      NotificationListener listener, NotificationFilter filter, Object handback)
      throws IllegalArgumentException {
    broadcaster.addNotificationListener(listener, filter, handback);
    if (Log.on && Log.isInfoEnabled())
      Log.info(
          "adding notification: listener->"
              + listener
              + " filter->"
              + filter
              + " handbackObj->"
              + handback);
    // To change body of implemented methods use File | Settings | File Templates.
  }

  public void removeNotificationListener(NotificationListener listener)
      throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener);
    if (Log.on && Log.isInfoEnabled()) Log.info("removing notification: " + listener);
  }

  public void sendNotification(Notification n) {
    broadcaster.sendNotification(n);
    if (Log.on && Log.isDebugEnabled()) Log.debug("sent notification:" + n);
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    return broadcaster.getNotificationInfo();
  }

  public void getElementStatus() {}
}
