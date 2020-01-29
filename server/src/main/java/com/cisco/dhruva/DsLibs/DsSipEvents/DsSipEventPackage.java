// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

// CAFFEINE 2.0 - Water (Backup And Restore) Featurette (EDCS-383083)
package com.cisco.dhruva.DsLibs.DsSipEvents;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Encapsulates the parameters of an RFC 3265 event package. It allows users to define instance
 * properties such as:
 *
 * <pre> <code>
 *     Allow multiple subscription when a SUBSCRIBE request forks.
 *     Event Package name.
 *     Default expiration of subscriptions.
 *     Minimum expiration of subscriptions.
 *     List of Accept header to put into requests.
 * </code> </pre>
 *
 * And static properties such as:
 *
 * <pre> <code>
 *     Insertion of Allow-Events headers into 2xx responses
 *     Insertion of Allow-Events headers into NOTIFY requests
 *     Insertion of Allow-Events headers into SUBSCRIBE requests
 * </code> </pre>
 */
// CAFFEINE 2.0 - Water (Backup And Restore) Featurette (EDCS-383083)
public class DsSipEventPackage implements Serializable {
  /** Logger for the DsSipEvents package. */
  private static Logger m_cat = DsLog4j.eventsCat;
  /** The table of packages. */
  private static HashMap m_pkgMap = new HashMap(8);

  /**
   * The list of Allow-Events headers for 489 responses. May also be inserted into 2xx responses and
   * Requests.
   */
  private static DsSipHeaderList m_allowEventsList;
  /**
   * If <code>true</code> the Allow-Events headers will be inserted into 2xx responses. Default is
   * true.
   */
  private static boolean m_insertAllowEvents2xx = true;
  /**
   * If <code>true</code> the Allow-Events headers will be inserted into NOTIFY requests. Default is
   * true.
   */
  private static boolean m_insertAllowEventsNotify = true;
  /**
   * If <code>true</code> the Allow-Events headers will be inserted into SUBSCRIBE requests. Default
   * is true.
   */
  private static boolean m_insertAllowEventsSusbcribe = true;

  /** The name of the event package. */
  private DsByteString m_name;
  /** The value to use for expiration if it does not appear in the message. */
  private long m_defaultExpiration;
  /** The minimum acceptable value for an expiration. */
  private long m_minExpiration;
  /** <code>true</code> to allow multiple subscriptions to be installed via forking. */
  private boolean m_multiFork;

  /** The list of Accept headers for SUBSCRIBE messages. */
  private DsSipHeaderList m_acceptList;

  // qfang 08/24/2005 add role as member CSCsb68743
  /** Role of event package */
  public static final short ROLE_NOTIFIER = 0X01;

  public static final short ROLE_SUBSCRIBER = 0X02;
  public static final short ROLE_BOTH = (ROLE_NOTIFIER | ROLE_SUBSCRIBER);

  private short m_role;

  /** DsByteString form of "refer" for the refer event package name. */
  public static final DsByteString REFER_PKG_NAME = new DsByteString("refer");

  // CAFFEINE 1.0 - add REFER support(EDCS-295393)
  /**
   * Create static refer package with default expiration of 3600 seconds and min_expiration of 3600
   * second. The NOTIFYs of a REFER request can set application specific expiration values.
   */

  /**
   * By RFC 3515 section 2.4, the application should set the number in expire paramter of
   * subscription-state header on the first notify, 3600 is used as default before the application
   * sends the first NOTIFY.
   */
  public static final DsSipEventPackage REFER_PKG =
      new DsSipEventPackage(REFER_PKG_NAME, 3600, 1, true, null, ROLE_BOTH);

  static {
    add(
        REFER_PKG.getName(),
        REFER_PKG.getDefaultExpiration(),
        REFER_PKG.getMinExpiration(),
        REFER_PKG.getAllowMultipleSubscribesOnForking(),
        REFER_PKG.getAcceptHeaderList(),
        REFER_PKG.getRole());
  }

  // qfang 08/24/2005 add role as member CSCsb68743
  /**
   * Construct an event package from the basic information.
   *
   * @param name the name of the event package.
   * @param defaultExpiration the value to use for expiration if it does not appear in the message.
   * @param minExpiration the minimum acceptable value for an expiration.
   * @param multiFork <code>true</code> to allow multiple subscriptions to be installed via forking.
   * @param acceptList the list of Accept headers for SUBSCRIBE messages.
   */
  private DsSipEventPackage(
      DsByteString name,
      long defaultExpiration,
      long minExpiration,
      boolean multiFork,
      DsSipHeaderList acceptList,
      short role) {
    setName(name);
    setDefaultExpiration(defaultExpiration);
    setMinExpiration(minExpiration);
    setAllowMultipleSubscribesOnForking(multiFork);
    setAcceptHeaderList(acceptList);
    // qfang 08/24/2005 add role as member CSCsb68743
    m_role = role;

    if (m_cat.isEnabled(Level.DEBUG)) {
      m_cat.debug("New Event Package:\n" + this);
    }
  }

  /**
   * Add a new event package that is understood by the application.
   *
   * @deprecated Please use the one with additional "role" parameter. This API assumes the role of
   *     both SUBSCRIBER and NOTIFIER.
   * @param name the name of the event package.
   * @param defaultExpiration the value to use for expiration if it does not appear in the message.
   * @param minExpiration the minimum acceptable value for an expiration.
   * @param multiFork <code>true</code> to allow multiple subscriptions to be installed via forking.
   * @return the newly created and added event package
   */
  public static DsSipEventPackage add(
      DsByteString name, long defaultExpiration, long minExpiration, boolean multiFork) {
    // qfang 08/24/2005 add role as member CSCsb68743
    return add(name, defaultExpiration, minExpiration, multiFork, null, ROLE_BOTH);
  }

  // qfang 08/24/2005 add role as member CSCsb68743
  /**
   * Add a new event package that is understood by the application.
   *
   * @param name the name of the event package.
   * @param defaultExpiration the value to use for expiration if it does not appear in the message.
   * @param minExpiration the minimum acceptable value for an expiration.
   * @param multiFork <code>true</code> to allow multiple subscriptions to be installed via forking.
   * @return the newly created and added event package
   */
  public static DsSipEventPackage add(
      DsByteString name,
      long defaultExpiration,
      long minExpiration,
      boolean multiFork,
      short role) {
    return add(name, defaultExpiration, minExpiration, multiFork, null, role);
  }

  /**
   * Add a new event package that is understood by the application.
   *
   * @deprecated Please use the one with additional "role" parameter. This API assumes the role of
   *     both SUBSCRIBER and NOTIFIER.
   * @param name the name of the event package.
   * @param defaultExpiration the value to use for expiration if it does not appear in the message.
   * @param minExpiration the minimum acceptable value for an expiration.
   * @param multiFork <code>true</code> to allow multiple subscriptions to be installed via forking.
   * @param acceptList the list of Accept headers for SUBSCRIBE messages.
   * @return the newly created and added event package
   */
  public static DsSipEventPackage add(
      DsByteString name,
      long defaultExpiration,
      long minExpiration,
      boolean multiFork,
      DsSipHeaderList acceptList) {
    // qfang 08/24/2005 add role as member CSCsb68743
    return add(name, defaultExpiration, minExpiration, multiFork, acceptList, ROLE_BOTH);
  }

  // qfang 08/24/2005 add role as member CSCsb68743
  /**
   * Add a new event package that is understood by the application.
   *
   * @param name the name of the event package.
   * @param defaultExpiration the value to use for expiration if it does not appear in the message.
   * @param minExpiration the minimum acceptable value for an expiration.
   * @param multiFork <code>true</code> to allow multiple subscriptions to be installed via forking.
   * @param acceptList the list of Accept headers for SUBSCRIBE messages.
   * @param role role of the caller - subscriber, notifier or both
   * @return the newly created and added event package
   */
  public static DsSipEventPackage add(
      DsByteString name,
      long defaultExpiration,
      long minExpiration,
      boolean multiFork,
      DsSipHeaderList acceptList,
      short role) {
    DsSipEventPackage eventPkg =
        new DsSipEventPackage(name, defaultExpiration, minExpiration, multiFork, acceptList, role);

    synchronized (m_pkgMap) {
      m_pkgMap.put(name, eventPkg);
    }

    if ((role & ROLE_NOTIFIER) == ROLE_NOTIFIER) {
      if (m_allowEventsList == null) {
        m_allowEventsList = new DsSipHeaderList();
      }

      DsSipAllowEventsHeader hdr = new DsSipAllowEventsHeader();
      hdr.setPackage(name);

      // Do not modify the original list,
      // TBD: ask Dynamicesoft the reason for not changing
      DsSipHeaderList newList = (DsSipHeaderList) m_allowEventsList.clone();
      newList.add(hdr);

      m_allowEventsList = newList;
    }
    return eventPkg;
  }

  /**
   * Gets an event package from the supported events table.
   *
   * @param name the full name of the event package to retrieve.
   * @return the requested event package or <code>null</code> if it was not found in the table
   */
  public static DsSipEventPackage get(DsByteString name) {
    if (name == null) {
      return null;
    }

    synchronized (m_pkgMap) {
      return (DsSipEventPackage) m_pkgMap.get(name);
    }
  }

  /**
   * Remove an event package from the map. Applications should only use this when they are
   * restarting the stack. Removing an event package while processing SUBSCRIBE dialogs results in
   * undefined behavior.
   *
   * @param name the name of the event package to remove
   * @return the removed event package, may be <code>null</code>
   */
  public static DsSipEventPackage remove(DsByteString name) {
    if (name == null) {
      return null;
    }

    synchronized (m_pkgMap) {
      return (DsSipEventPackage) m_pkgMap.remove(name);
    }
  }

  /**
   * Gets an event package from the supported events table.
   *
   * @param header the Event Header to use to retrieve the package name from.
   * @return the requested event package or <code>null</code> if it was not found in the table
   */
  public static DsSipEventPackage get(DsSipEventHeader header) {
    if (header == null) {
      return get(DsSipConstants.BS_PINT);
    }

    return get(header.getFullPackageName());
  }

  /**
   * Gets an event package from the supported events table.
   *
   * @param message the SIP message containing an Event header
   * @return the requested event package or <code>null</code> if it was not found in the table
   */
  public static DsSipEventPackage get(DsSipMessage message) {
    if (message == null) {
      return null;
    }

    try {
      DsSipEventHeader header = (DsSipEventHeader) message.getHeaderValidate(DsSipConstants.EVENT);
      return get(header);
    } catch (DsException e) {
      return null;
    }
  }

  /**
   * Allows the application to retrieve the property controlling multiple dialogs upon forking.
   *
   * <p>According to RFC 3265:
   *
   * <blockquote>
   *
   * Each event package MUST specify whether forked SUBSCRIBE requests are allowed to install
   * multiple subscriptions.
   *
   * </blockquote>
   *
   * @return <code>true</code> if forking installs multiple subscriptions, else <code>false</code>
   */
  public boolean getAllowMultipleSubscribesOnForking() {
    return m_multiFork;
  }

  /**
   * Allows the application to define the property controlling multiple dialogs upon forking.
   *
   * <p>According to RFC 3265:
   *
   * <blockquote>
   *
   * Each event package MUST specify whether forked SUBSCRIBE requests are allowed to install
   * multiple subscriptions.
   *
   * </blockquote>
   *
   * @param multiFork <code>true</code> if forking installs multiple subscriptions, else <code>false
   *     </code>.
   * @see #getAllowMultipleSubscribesOnForking()
   */
  public void setAllowMultipleSubscribesOnForking(boolean multiFork) {
    m_multiFork = multiFork;
  }

  /**
   * Gets the name of this event package.
   *
   * @return the name of this event package.
   */
  public DsByteString getName() {
    return m_name;
  }

  /**
   * Sets the name of this event package.
   *
   * @param name the name of the event package.
   * @see #getName
   */
  private void setName(DsByteString name) {
    m_name = name;
  }

  // qfang 08/24/2005 add role as member CSCsb68743
  /**
   * Gets the role of this event package.
   *
   * @return short role of the event package
   */
  public short getRole() {
    return m_role;
  }

  // Note: does not have corresponding setRole() since otherwise has to deal
  // with updating the m_allowEventsList when role is changed

  /**
   * Gets the default expiration, the value to use for expiration if it does not appear in the
   * message.
   *
   * @return the default expiration, the value to use for expiration if it does not appear in the
   *     message.
   */
  public long getDefaultExpiration() {
    return m_defaultExpiration;
  }

  /**
   * Sets the default expiration, the value to use for expiration if it does not appear in the
   * message.
   *
   * @param defaultExpiration the value to use for expiration if it does not appear in the message.
   * @see #getDefaultExpiration()
   */
  public void setDefaultExpiration(long defaultExpiration) {
    m_defaultExpiration = defaultExpiration;
  }

  /**
   * Gets the minimum expiration, the minimum acceptable value for an expiration.
   *
   * @return the minimum expiration, the minimum acceptable value for an expiration.
   */
  public long getMinExpiration() {
    return m_minExpiration;
  }

  /**
   * Sets the minimum expiration, the minimum acceptable value for an expiration.
   *
   * @param minExpiration the minimum acceptable value for an expiration.
   * @see #getMinExpiration()
   */
  public void setMinExpiration(long minExpiration) {
    m_minExpiration = minExpiration;
  }

  /**
   * Gets the Accept header list for SUBSCRIBE messages.
   *
   * @return the Accept header list for SUBSCRIBE messages
   * @see #setAcceptHeaderList(DsSipHeaderList)
   */
  public DsSipHeaderList getAcceptHeaderList() {
    return m_acceptList;
  }

  /**
   * Sets the Accept header list for SUBSCRIBE messages.
   *
   * @param acceptList the Accept header list for SUBSCRIBE messages.
   * @see #getAcceptHeaderList()
   */
  public void setAcceptHeaderList(DsSipHeaderList acceptList) {
    m_acceptList = acceptList;
  }

  /**
   * Returns <code>true</code> if the Allow-Events headers will be inserted into 2xx responses.
   *
   * @return <code>true</code> if the Allow-Events headers will be inserted into 2xx responses.
   * @see #setInsertAllowEvents2xx(boolean)
   */
  public static boolean getInsertAllowEvents2xx() {
    return m_insertAllowEvents2xx;
  }

  /**
   * Determinie if the Allow-Events headers will be inserted into 2xx responses.
   *
   * @param flag <code>true</code> to insert Allow-Events headers into 2xx responses.
   * @see #getInsertAllowEvents2xx()
   */
  public static void setInsertAllowEvents2xx(boolean flag) {
    m_insertAllowEvents2xx = flag;
  }

  /**
   * Returns <code>true</code> if the Allow-Events headers will be inserted into NOTIFY requests.
   *
   * @return <code>true</code> if the Allow-Events headers will be inserted into NOTIFY requests.
   * @see #setInsertAllowEventsNotify(boolean)
   */
  public static boolean getInsertAllowEventsNotify() {
    return m_insertAllowEventsNotify;
  }

  /**
   * Determinie if the Allow-Events headers will be inserted into NOTIFY requests.
   *
   * @param flag <code>true</code> to insert Allow-Events headers into NOTIFY requests.
   * @see #getInsertAllowEventsNotify()
   */
  public static void setInsertAllowEventsNotify(boolean flag) {
    m_insertAllowEventsNotify = flag;
  }

  /**
   * Returns <code>true</code> if the Allow-Events headers will be inserted into SUBSCRIBE requests.
   *
   * @return <code>true</code> if the Allow-Events headers will be inserted into SUBSCRIBE requests.
   * @see #setInsertAllowEventsSusbcribe(boolean)
   */
  public static boolean getInsertAllowEventsSusbcribe() {
    return m_insertAllowEventsSusbcribe;
  }

  /**
   * Determinie if the Allow-Events headers will be inserted into SUBSCRIBE requests.
   *
   * @param flag <code>true</code> to insert Allow-Events headers into SUBSCRIBE requests.
   * @see #getInsertAllowEventsSusbcribe()
   */
  public static void setInsertAllowEventsSusbcribe(boolean flag) {
    m_insertAllowEventsSusbcribe = flag;
  }

  /**
   * Add the Allow-Events headers to this request. They will only be added if there is not already
   * an Allow-Events header in this message. And, only if this class is configured to add them to
   * messages.
   *
   * @param request the message to add the Allow-Events headers to.
   */
  public static void addAllowEvents(DsSipRequest request) {
    int method = request.getMethodID();

    if ((method == DsSipConstants.SUBSCRIBE && m_insertAllowEventsSusbcribe)
        || (method == DsSipConstants.NOTIFY && m_insertAllowEventsNotify)) {
      DsSipHeaderInterface hdr = request.getHeader(DsSipConstants.ALLOW_EVENTS);

      // If headers already exist in the message, then just leave those there.
      if (hdr == null) {
        // Intentional clone, because any modification will modify the master list.
        request.addHeaders(m_allowEventsList);
      }
    }
  }

  /**
   * Add the Allow-Events headers to this response. They will only be added if there is not already
   * an Allow-Events header in this message. And, only if this is a 489 response or a 2xx response
   * and this class is configured to add them 2xx responses.
   *
   * @param response the message to add the Allow-Events headers to.
   */
  public static void addAllowEvents(DsSipResponse response) {
    if (response.getStatusCode() == DsSipResponseCode.DS_RESPONSE_BAD_EVENT
        || // 489
        (response.getResponseClass() == DsSipResponseCode.DS_SUCCESS
            && m_insertAllowEvents2xx)) // 2xx
    {
      DsSipHeaderInterface hdr = response.getHeader(DsSipConstants.ALLOW_EVENTS);

      // If headers already exist in the message, then just leave those there.
      if (hdr == null) {
        // Intentional clone, because any modification will modify the master list.
        response.addHeaders(m_allowEventsList);
      }
    }
  }

  /**
   * Generated a multi-line String that shows the internal information about this instance. This is
   * useful for debug purpose only.
   *
   * @return a multi-line debug String representation of this instances data.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(256);

    sb.append("Name =           [" + m_name + "]\n");
    sb.append("Default Expire = [" + m_defaultExpiration + "]\n");
    sb.append("Min Expire =     [" + m_minExpiration + "]\n");
    sb.append("Multi Fork =     [" + m_multiFork + "]\n");
    sb.append("Accept List =    [" + m_acceptList + "]\n");

    return sb.toString();
  }
}
