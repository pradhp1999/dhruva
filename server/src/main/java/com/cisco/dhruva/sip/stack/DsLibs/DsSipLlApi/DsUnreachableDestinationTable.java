// Copyright (c) 2006-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDiscreteTimerTask;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTimer;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.event.Level;

/**
 * This class manages unreachable destination. Client module can add a destination to this table
 * when it finds it is not reachable or not in service, so that the module that is responsible to
 * pick destination may skip it and return the next one to achieve better performance.
 *
 * <p>A timer is associated with each destination entry added to this table, once expires, it will
 * be removed from the table.
 */
public class DsUnreachableDestinationTable implements DsEvent {
  /** Logger. */
  protected static Logger cat = DsLog4j.connectionMCat;

  /** The singleton. */
  private static DsUnreachableDestinationTable m_instance;

  /** Timeout, in seconds. */
  private static long UNREACH_DEST_TIMEOUT =
      DsConfigManager.getProperty(
              DsConfigManager.PROP_UNREACHABLE_DEST_TIMEOUT,
              DsConfigManager.PROP_UNREACHABLE_DEST_TIMEOUT_DEFAULT)
          * 1000;

  /** Unreachable destinations map. */
  private Map m_destinations = new HashMap();

  /** Singleton, disallow external construction. */
  private DsUnreachableDestinationTable() {}

  /**
   * Sets the unreachable timeout, in seconds, for this class. This does not affect existing
   * destinations. This overrides the value set by DsConfigManager.PROP_UNREACHABLE_DEST_TIMEOUT.
   *
   * @param seconds the number of seconds to keep a destination marked as unreachable
   */
  public static void setUnreachableTimeout(long seconds) {
    UNREACH_DEST_TIMEOUT = seconds * 1000;
  }

  /**
   * Gets the unreachable timeout, in seconds, for this class.
   *
   * @return the unreachable timeout, in seconds
   */
  public static long getUnreachableTimeout() {
    return (UNREACH_DEST_TIMEOUT / 1000);
  }

  /**
   * Get the singleton. It is created on the first call.
   *
   * @return the singleton
   */
  public static DsUnreachableDestinationTable getInstance() {
    if (m_instance == null) {
      m_instance = new DsUnreachableDestinationTable();
    }

    return m_instance;
  }

  /**
   * Add an unreachable destination.
   *
   * @param address the address of the unreachable destination
   * @param port the port of the unreachable destination
   * @param transport the transport type of the unreachable destination
   */
  public void add(InetAddress address, int port, Transport transport) {
    if (DsSipServerLocator.m_useDsUnreachableTable) {
      String key = createKey(address, port, transport);
      synchronized (m_destinations) {
        if (cat.isEnabled(Level.DEBUG)) {
          cat.debug(
              "UnreachableDestinationTable - add ("
                  + key
                  + "). Current count: "
                  + m_destinations.size());
        }

        ScheduledFuture timerTask = DsTimer.schedule(UNREACH_DEST_TIMEOUT, this, key);
        timerTask = (ScheduledFuture) m_destinations.put(key, timerTask);
        if (timerTask != null) {
          // it's unsual this happen, possible sign of duplicated call
          if (cat.isEnabled(Level.DEBUG)) {
            cat.debug("UnreachableDestinationTable - cancel previous timer for (" + key + ")");
          }
          timerTask.cancel(false);
        }
      }
    }
  }

  /**
   * Determine if a destination is already in the table.
   *
   * @return <code>true</code> if this destintion is already considered unreachable, else <code>
   *     false</code>
   */
  public boolean contains(InetAddress address, int port, Transport transport) {
    if (!DsSipServerLocator.m_useDsUnreachableTable) {
      return false;
    }

    synchronized (m_destinations) {
      return m_destinations.containsKey(createKey(address, port, transport));
    }
  }

  /**
   * Remove an entry from the unreachable table, effectively marking it as reachable again.
   *
   * @param address the address of the reachable destination
   * @param port the port of the reachable destination
   * @param transport the transport type of the reachable destination
   */
  public void remove(InetAddress address, int port, Transport transport) {
    DsDiscreteTimerTask timerTask =
        (DsDiscreteTimerTask) remove(createKey(address, port, transport));
    if (timerTask != null) {
      timerTask.cancel();
    }
  }

  /**
   * Remove all entries from the unreachable table, effectively marking all destinations as
   * reachable.
   */
  public void clear() {
    if (cat.isEnabled(Level.DEBUG)) {
      cat.debug("UnreachableDestinationTable - clear()");
    }

    synchronized (m_destinations) {
      Set<String> keys = m_destinations.keySet();

      for (String key : keys) {
        DsDiscreteTimerTask timerTask = (DsDiscreteTimerTask) m_destinations.get(key);
        if (timerTask != null) {
          timerTask.cancel();
        }
      }

      m_destinations.clear();
    }
  }

  /**
   * Remove an entry from the unreachable table, effectively marking it as reachable again.
   *
   * @param key the destination to remove
   */
  private Object remove(String key) {
    if (cat.isEnabled(Level.DEBUG)) {
      cat.debug(
          "UnreachableDestinationTable - remove ("
              + key
              + "). Current count: "
              + m_destinations.size());
    }

    synchronized (m_destinations) {
      return m_destinations.remove(key);
    }
  }

  private String createKey(InetAddress address, int port, Transport transport) {
    StringBuffer buf = new StringBuffer(32);

    buf.append(address.getHostAddress()).append(':').append(port).append(':').append(transport);

    return buf.toString();
  }

  /**
   * Timer expires, remove the entry.
   *
   * @param argument the key, must be a String
   */
  public void run(Object argument) {
    remove((String) argument);
  }

  /*
  public static void main(String[] args)
  {
      try {
          InetAddress host = InetAddress.getByName("qfang-linux");
          InetAddress host2 = InetAddress.getByName("christmi-dev");
          int port = 5060;
          int transport = DsSipTransportType.UDP;
          DsUnreachableDestinationTable.getInstance().add(host, port, transport);
          DsUnreachableDestinationTable.getInstance().add(host2, port, transport);

          if (DsUnreachableDestinationTable.getInstance().contains(host, port, transport))
          {
              System.out.println("Pass - found host!");
          }
          else
          {
              System.out.println("Fail - didn't find host!");
          }

          if (DsUnreachableDestinationTable.getInstance().contains(host2, port, transport))
          {
              System.out.println("Pass - found host2!");
          }
          else
          {
              System.out.println("Fail - didn't find host2!");
          }

          DsUnreachableDestinationTable.getInstance().clear();

          if (DsUnreachableDestinationTable.getInstance().contains(host, port, transport))
          {
              System.out.println("Fail - found host!");
          }
          else
          {
              System.out.println("Pass - didn't find host!");
          }

          if (DsUnreachableDestinationTable.getInstance().contains(host2, port, transport))
          {
              System.out.println("Fail - found host2!");
          }
          else
          {
              System.out.println("Pass - didn't find host2!");
          }

          Thread.sleep(5000);
          System.out.println("Finished!");
          System.exit(0);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
  }
  */
}
