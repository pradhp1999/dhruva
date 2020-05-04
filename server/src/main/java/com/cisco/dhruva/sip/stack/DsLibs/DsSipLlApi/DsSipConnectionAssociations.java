// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Logger;
import java.util.HashMap;

/**
 * This class provides for the management of the connection associations.
 *
 * <p>The connection associations are useful in case of SIP over TCP where the Server side is not in
 * a position to open a connection to the Client. The Client establishes a persistent connection to
 * the Server, creates a registration to the Server with a unique connection ID (this connection ID
 * is unique accross all the clients). This registration would allow the Server to communicate to
 * the client through this persistent connection.
 *
 * <p>It maintains a map for the connection Ids (as the Key) and the corresponding binding
 * informations (as the Value).
 *
 * <p>When the application code gets a callback for a new request, it should determine whether a
 * connection association should be created. The application would extract the connection ID from
 * the request and take the {@link DsSipRequest#getBindingInfo() binding information from the
 * request}, and create a connection association by invoking {@link #associate(DsByteString,
 * DsBindingInfo) associate()}. If the connection association for the specified connection ID
 * already exists, it will be overriden.
 *
 * <p>The responses corresponding to that request, would be sent over the same connection by the
 * {@link DsSipServerTransactionImpl#sendResponse(int)} and should be taken care by the Server
 * Transaction. Where as for the requests that needs to be forwarded to the Client, the underlying
 * connection can be determined by:
 *
 * <blockquote>
 *
 * - Retreive the connection ID parameter for the Client from the Registeration service.<br>
 * - Retreive the connection by invoking {@link #getConnection(DsByteString)}.<br>
 * - Use the retreived connection to forward the request.<br>
 *
 * </blockquote>
 */
public final class DsSipConnectionAssociations {
  /** The mapping of connections. */
  private static HashMap m_bindings = new HashMap(); // Find out the reasonable initial capacity

  private static Logger logger = DsLog4j.connAssocCat;

  private static final boolean m_shouldClose =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CONNECTION_ID_CLOSE,
          DsConfigManager.PROP_CONNECTION_ID_CLOSE_DEFAULT);
  /** The instance of this class can not be constructed. */
  private DsSipConnectionAssociations() {}

  /**
   * Creates an association of the specified <code>connectionId</code> with the specified <code>
   * binding</code> information. This assumes that a network connection has been established with
   * the remote network destination as specified in the <code>binding</code> information. Once this
   * binding is established, the underlying network connection can be queried by invoking {@link
   * #getConnection(DsByteString) getConnection()}.
   *
   * @param connectionId A unique connection id string that will be associated with a network
   *     binding information and that network binding information will correspond to an established
   *     connection.
   * @param binding The network binding information that will be associated with the specified
   *     <code>connectionId</code> string and assume that there will be an established network
   *     connection to the remote destination as specified in the binding information.
   * @throws IllegalArgumentException if either of the specified parameters is null.
   */
  public static void associate(DsByteString connectionId, DsBindingInfo binding) {
    if (connectionId == null || binding == null) {
      logger.warn(
          "throwing IllegalArgumentException for [ " + connectionId + " : " + binding + " ] ");
      throw new IllegalArgumentException("The connection ID and the binding info can not be null");
    }

    DsSipConnection connection = getConnection(binding);
    if (null != connection) {
      // Set the connection Id for the binding info of the underlying connection.
      connection.getBindingInfo().setConnectionId(connectionId);
    }

    DsBindingInfo oldBinding = null;
    // Add the connection association entry.
    synchronized (m_bindings) {
      // setting connection id so that when this binding info is used else where they know about the
      // connection id
      binding.setConnectionId(connectionId);
      oldBinding = (DsBindingInfo) m_bindings.put(connectionId, binding);
    }

    closeOldConnection(oldBinding);

    if (logger.isDebugEnabled()) {
      logger.debug("New Connection Association [ " + connectionId + " --> " + binding + " ]");
    }
  }

  private static void closeOldConnection(DsBindingInfo oldBinding) {
    DsSipConnection connection = null;
    if (m_shouldClose && null != oldBinding) {
      connection = getConnection(oldBinding);
      if (null != connection) {
        if (oldBinding.getTransport() == Transport.TCP
            || oldBinding.getTransport() == Transport.TLS) {
          try {

            // TODO take care of cleanup
            // ((DsTcpConnection) connection).startCleaner();
          } catch (ClassCastException cce) {
            if (logger.isInfoEnabled()) {
              logger.info(
                  "Exception while trying to close Connection Association [ "
                      + oldBinding.getConnectionId()
                      + " --> "
                      + oldBinding
                      + " ]",
                  cce);
            }
          }
        } else {
          DsNetwork network = oldBinding.getNetwork();
          if (network != null
              && network.getDatagramConnectionStrategy() != DsNetwork.DGRAM_PER_THREAD) {
            DsSipTransactionManager.getTransportLayer().removeConnection(connection);
          }
        }
      }
    }
  }

  /**
   * Removes the association, if any, for the specified <code>connectionId</code>. Once this
   * association is removed, then even if there is an established connection with the remote
   * destination, as specified by the removed binding information, then no connection can be queried
   * through this connection Id.
   *
   * @param connectionId A unique connection id string for which the connection association needs to
   *     be removed.
   */
  public static void dissociate(DsByteString connectionId) {
    DsBindingInfo bindingInfo = null;
    if (connectionId != null) {
      synchronized (m_bindings) {
        bindingInfo = (DsBindingInfo) m_bindings.remove(connectionId);
      }
      closeOldConnection(bindingInfo);
    }
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Removed Connection Association [ " + connectionId + " --> " + bindingInfo + " ]");
    }
  }

  /**
   * Removes the association, if any, for the specified <code>bindingInfo</code>. Once this
   * association is removed, then even if there is an established connection with the remote
   * destination, as specified by the removed binding information, then no connection can be queried
   * through the connection Id of the specified <code>bindingInfo</code>.
   *
   * @param bindingInfo The binding information that includes the connection ID, for which the
   *     connection association needs to be removed, if any.
   */
  public static void dissociate(DsBindingInfo bindingInfo) {
    if (null != bindingInfo) {
      DsByteString connectionId = bindingInfo.getConnectionId();

      if (null != connectionId) {
        DsBindingInfo bi = null;
        synchronized (m_bindings) {
          bi = (DsBindingInfo) m_bindings.get(connectionId);
          if (null != bi && bindingInfo.equals(bi)) {
            m_bindings.remove(connectionId);
          }
        } // _synchronized
        closeOldConnection(bindingInfo);
        if (logger.isDebugEnabled()) {
          logger.debug("Removed Connection Association [ " + connectionId + " --> " + bi + " ]");
        } // _if
      } // _if
    } // _if
  }

  /**
   * Returns an existing connection that corresponds to the specified <code>connectionId</code>.
   * First the binding information that is associated with this <code>connectionId</code> is
   * searched and then for that found binding information, the corresponding connection is looked
   * up. If there is no association for the specified <code>connectionId</code> exists, then <code>
   * null</code> is returned.
   *
   * @param connectionId A unique connection id string for which the connection needs to be queried.
   * @return the connection associated witht his ID.
   */
  public static DsSipConnection getConnection(DsByteString connectionId) {
    return getConnection(connectionId, true); // check for shut down.
  }
  /**
   * Returns an existing connection that corresponds to the specified <code>connectionId</code>.
   * First the binding information that is associated with this <code>connectionId</code> is
   * searched and then for that found binding information, the corresponding connection is looked
   * up. If there is no association for the specified <code>connectionId</code> exists, then <code>
   * null</code> is returned.
   *
   * @param connectionId A unique connection id string for which the connection needs to be queried.
   * @param checkShutDown whether to check if this connection is already in shut down mode. If so,
   *     then return null.
   * @return the connection associated with this ID.
   */
  public static DsSipConnection getConnection(DsByteString connectionId, boolean checkShutDown) {
    if (null == connectionId) return null;
    DsBindingInfo bi = null;
    synchronized (m_bindings) {
      bi = (DsBindingInfo) m_bindings.get(connectionId);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Retrieving Connection Association [ " + connectionId + " --> " + bi + " ]");
    }

    DsSipConnection connection = null;
    DsSipTransportLayer transportLayer = DsSipTransactionManager.getTransportLayer();
    if (bi != null && transportLayer != null) {
      try {
        connection = (DsSipConnection) transportLayer.findConnection(bi, checkShutDown);

        // setting the connection id so that the request binding info has connection id
        if (connection != null) connection.getBindingInfo().setConnectionId(connectionId);
      } catch (Exception exc) {
        logger.error("Exception while Retreiving the connection", exc);
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Retrieved Connection [ "
              + connection
              + " ] from Association  [ "
              + connectionId
              + " --> "
              + bi
              + " ]");
    }
    return connection;
  }

  /**
   * Returns an existing connection ID that corresponds to the specified <code>bindingInfo</code>.
   * If there is no existing connection ID associated with the specified <code>bindingInfo</code>,
   * then <code>null</code> is returned.
   *
   * @param bindingInfo The binding information for the request, that was received through a TCP
   *     connection, for which the connection ID needs to be queried.
   * @return the connection ID for the specified binding information.
   */
  public static DsByteString getConnectionId(DsBindingInfo bindingInfo) {
    if (logger.isDebugEnabled()) {
      logger.debug("Retrieving Connection ID for [ " + bindingInfo + " ]");
    }

    DsByteString connectionId = null;
    DsSipConnection connection = getConnection(bindingInfo);
    if (null != connection) {
      connectionId = connection.getBindingInfo().getConnectionId();
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Retrieved Connection ID [ " + connectionId + " ] for [ " + bindingInfo + " ]");
    }
    return connectionId;
  }

  /**
   * Returns an existing connection that corresponds to the specified <code>bindingInfo</code>. If
   * there is no existing connection for the specified <code>bindingInfo</code>, then <code>null
   * </code> is returned.
   *
   * @param bindingInfo The binding information for the request, that was received through a TCP
   *     connection, for which the connection needs to be queried.
   * @return the connection for the specified binding information.
   */
  private static DsSipConnection getConnection(DsBindingInfo bindingInfo) {
    if (null == bindingInfo) return null;

    DsSipConnection connection = null;
    DsTransportLayer transportLayer = DsSipTransactionManager.getTransportLayer();

    if (logger.isDebugEnabled()) {
      logger.debug("Retrieving Connection for [ " + bindingInfo + " ]");
    }

    if (transportLayer != null) {
      connection = (DsSipConnection) transportLayer.findConnection(bindingInfo);
    }
    return connection;
  }
} // Ends DsSipConnectionAssociations
