// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.Level;

/**
 * When many threads are trying to connect to the same endpoint simultaneously, this class will
 * force all but the first thread to wait until the first thread establishes the connection. It the
 * requests using a lock for each endpoint being tried.
 */

/*
TODO handle connection get
 */
class DsConnectionBarrier {
  private static final int LPU = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;

  private Set m_tryingSet;

  DsConnectionBarrier() {
    m_tryingSet = new HashSet();
  }

  /**
   * Connect to the last returned binding information. First, see if the connection is cached in the
   * connection table, then see if another thread is trying to connect. If so, wait. Otherwise
   * connect ourselves and notify all other waiting threads.
   *
   * @param info the binding info that contains the place to connect to
   * @return the DsSipConnection or null if a connection couldn't be created
   */
  DsSipConnection connectTo(DsBindingInfo info) {
    DsSipConnection ret_connection = null;

    if (info == null) return null;

    try {
      ret_connection =
          (DsSipConnection)
              DsSipTransactionManager.getTransportLayer()
                  .getConnection(
                      info.getNetworkReliably(),
                      info.getLocalAddress(),
                      info.getLocalPort(),
                      info.getRemoteAddress(),
                      info.getRemotePort(),
                      info.getTransport(),
                      false);
    } catch (Exception e) {
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.warn("Failed to get connection: ", e);
      }
    }

    if (ret_connection == null) {
      ret_connection = waitIfTrying(info);
    }

    if (ret_connection == null) {
      ret_connection = tryConnect(info);
    }

    return ret_connection;
  }

  /**
   * If we are currently trying to connect to info in any DsConnectionBarrier then wait until we are
   * done trying and then return the connection. As a side effect, if no other object is trying, we
   * will add the provided DsBindingInfo to the trying list.
   *
   * @param info the binding info that contains the place to connect to
   * @return the DsSipConnection or null if a connection couldn't be created
   */
  private DsSipConnection waitIfTrying(DsBindingInfo info) {
    DsSipConnection ret_connection = null;

    /* DsBindingInfo found = null;

        synchronized (m_tryingSet) {
          Object[] infos = m_tryingSet.toArray();

          for (int i = 0; i < infos.length; ++i) {
            if (infos[i].equals(info)) {
              found = (DsBindingInfo) infos[i];

              break;
            }
          }

          // if not found then add myself because I will try
          m_tryingSet.add(info);
          synchronized (info) {
            info.setTrying(true);
          }
        }

        if (found != null) {
          synchronized (found) {
            if (found.isTrying()) {
              try {
                if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                  DsLog4j.connectionCat.log(Level.DEBUG, "waitIfTrying:waiting..on " + found);
                }

                found.wait();

                if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                  DsLog4j.connectionCat.log(Level.DEBUG, "waitIfTrying continuing.");
                }
              } catch (InterruptedException ie) {
              }
            }
          }
          ret_connection =
              (DsSipConnection)
                  m_connectionTable.get(
                      found.getLocalAddress(),
                      found.getLocalPort(),
                      found.getRemoteAddress(),
                      found.getRemotePort(),
                      found.getTransport());
        }

        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(Level.DEBUG, "waitIfTrying:ret_connection == " + ret_connection);
        }
    */
    return ret_connection;
  }

  /**
   * Try to connection to the binding info specified by 'info.' Uses the connection table shared
   * with the transport layer. The connection table object is shared to prevent its exposure via a
   * transport layer method.
   *
   * @param info the DsBindingInfo to connect to
   * @return the DsSipConnection or null if no connection could be established based on the
   *     specified binding info
   */
  private DsSipConnection tryConnect(DsBindingInfo info) {
    DsSipConnection ret_connection = null;
    /*


        int transport = info.getTransport();
        try {
          switch (transport) {
            case DsSipTransportType.UDP:
              if (info.getLocalAddress() == null && info.getLocalPort() == LPU) {
                ret_connection =
                    (DsSipConnection)
                        m_connectionFactory.createConnection(
                            info.getNetwork(),
                            info.getRemoteAddress(),
                            info.getRemotePort(),
                            transport);
              } else {
                ret_connection =
                    (DsSipConnection)
                        m_connectionFactory.createConnection(
                            info.getNetwork(),
                            info.getLocalAddress(),
                            info.getLocalPort(),
                            info.getRemoteAddress(),
                            info.getRemotePort(),
                            transport);
              }
              break;
            case DsSipTransportType.TCP:
            case DsSipTransportType.TLS:
              DsSSLContext context = DsSipTransactionManager.getTransportLayer().getSSLContext();
              if (info.getLocalAddress() == null && info.getLocalPort() == LPU) {
                ret_connection =
                    (DsSipConnection)
                        m_connectionFactory.createConnection(
                            info.getNetwork(),
                            info.getRemoteAddress(),
                            info.getRemotePort(),
                            transport,
                            context);
              } else {
                ret_connection =
                    (DsSipConnection)
                        m_connectionFactory.createConnection(
                            info.getNetwork(),
                            info.getLocalAddress(),
                            info.getLocalPort(),
                            info.getRemoteAddress(),
                            info.getRemotePort(),
                            transport,
                            context);
              }
              break;
          }
        } catch (SocketException se) {
          ret_connection = null;
          if (DsLog4j.connectionCat.isDebugEnabled()) {
            DsLog4j.connectionCat.debug("tryConnect(DsBindingInfo) socket exception: ", se);
          }
        } catch (IOException ioe) {
          ret_connection = null;
          if (DsLog4j.connectionCat.isDebugEnabled()) {
            DsLog4j.connectionCat.debug("tryConnect(DsBindingInfo) IO exception: ", ioe);
          }
        } catch (Throwable throwable) {
          ret_connection = null;
          if (DsLog4j.connectionCat.isDebugEnabled()) {
            DsLog4j.connectionCat.debug("tryConnect(DsBindingInfo) exception: ", throwable);
          }
        }

        if (ret_connection != null) {
          DsNetwork network = info.getNetwork();

          if ((network == null)
              || network.getDatagramConnectionStrategy() != DsNetwork.DGRAM_PER_THREAD) {
            m_connectionTable.put(ret_connection);

            try {
              DsSipTransactionManager.getTransportLayer().monitorConnection(ret_connection);
            } catch (IOException ioe) {
              ret_connection = null;
            }
          }
        }

        synchronized (m_tryingSet) {
          m_tryingSet.remove(info);
        }

        synchronized (info) {
          info.setTrying(false);
          if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
            DsLog4j.connectionCat.log(Level.DEBUG, "tryConnect callling notify on " + info);
          info.notifyAll();
        }
    */
    return ret_connection;
  }
}
