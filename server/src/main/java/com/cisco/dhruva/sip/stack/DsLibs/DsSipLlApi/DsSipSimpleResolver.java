package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Level;

/**
 * Created by IntelliJ IDEA. User: rrachuma Date: Jan 15, 2009 Time: 12:24:51 PM To change this
 * template use File | Settings | File Templates.
 */
public class DsSipSimpleResolver implements DsSipResolver {
  private static final Trace Log = Trace.getTrace(DsSipSimpleResolver.class.getName());

  //  used to implement simple resolver
  private boolean m_sizeExceedsMTU;

  private DsBindingInfo m_resolverInfo = null;

  private boolean m_end; // Set to true when we have
  // used this resolver to obtain a connection.
  // Here we're kind of faking that we have a list event though we just
  // have a single target.

  private boolean m_haveIP = false;
  private byte m_supportedTransports;
  private DsByteString m_strConnectionId;

  public DsSipSimpleResolver(DsSipRequest request) {
    setSizeExceedsMTU(request.sizeExceedsMTU());
  }

  public DsSipSimpleResolver(DsSipRequest request, DsByteString connectionId) throws DsException {
    setSizeExceedsMTU(request.sizeExceedsMTU());
    m_strConnectionId = connectionId;
    DsSipConnection conn = DsSipConnectionAssociations.getConnection(m_strConnectionId);
    if (conn != null) {
      m_resolverInfo = conn.getBindingInfo();
    } else {
      throw new DsException(
          "DsSipSimpleResolver: No connection could be found for Connection ID ["
              + m_strConnectionId
              + "]");
    }
    m_end = true;
  }

  // ////////////////////////////////////////////////////////////////
  // ////// DsSipResolver methods        ////////////////////////////
  // ////////////////////////////////////////////////////////////////

  /**
   * Return a connection to the next endpoint.
   *
   * @return a connection to the next endpoint
   * @throws DsException if there is an exception in the User Agent code
   * @throws IOException if thrown by the underlying socket
   */
  public DsSipConnection tryConnect() throws DsException, IOException {
    // todo: use DsConnectionBarrier to prevent multiple threads from creating the
    //    same entry in the connection table

    if (m_end) return null;

    if (m_resolverInfo == null) return null;

    m_end = true;
    DsSipTransportLayer tl = DsSipTransactionManager.getTransportLayer();

    if (m_resolverInfo.getRemoteAddress() == null) {
      throw new IOException("can't resolve address: " + m_resolverInfo.getRemoteAddressStr());
    }

    DsSipConnection con = null;
    try {
      con =
          (DsSipConnection)
              tl.getConnection(
                  m_resolverInfo.getNetwork(),
                  m_resolverInfo.getLocalAddress(),
                  m_resolverInfo.getLocalPort(),
                  m_resolverInfo.getRemoteAddress(),
                  m_resolverInfo.getRemotePort(),
                  m_resolverInfo.getTransport());
    } catch (Exception e) {
      if (Log.isEnabled(Level.INFO)) {
        Log.info("tryConnect() - getConnection failed: ", e);
      }

      /*    // Try again with the UDP itself.
      if (m_sizeExceedsMTU) {


        TODO take care
        DsNetwork network = m_resolverInfo.getNetwork();
        if (network != null && network.isBehindNAT()) {
          // we un-set the local binding info earlier, now we need to put it back
          // since the TCP connection failed for some reason.
          DsUdpListener listener = network.getUdpListener();
          if (listener != null) {
            m_resolverInfo.setLocalAddress(listener.m_address);
            m_resolverInfo.setLocalPort(listener.m_port);
            if (Log.isEnabled(Level.INFO)) {
              Log.info(
                  "tryConnect() - resetting local binding info - host = "
                      + ((DsPacketListener) listener).m_address
                      + " / port = "
                      + ((DsPacketListener) listener).m_port
                      + " and trying UDP");
            }
          }
        }

        m_resolverInfo.setTransport(Transport.UDP);
        con =
            (DsSipConnection)
                tl.getConnection(
                    network,
                    m_resolverInfo.getLocalAddress(),
                    m_resolverInfo.getLocalPort(),
                    m_resolverInfo.getRemoteAddress(),
                    m_resolverInfo.getRemotePort(),
                    m_resolverInfo.getTransport());
      }*/
    }

    return con;
  }

  /**
   * Return the DsBindingInfo for the connection last returned by tryConnect.
   *
   * @return the DsBindingInfo for the connection last returned by tryConnect.
   */
  public DsBindingInfo getCurrentBindingInfo() {
    return m_resolverInfo;
  }

  /**
   * Return true if the resolver would initialize to it's current set of endpoints given this URL.
   *
   * @param url the URL to resolve against
   * @param sizeExceedsMTU <code>true</code> if this message was too large for UDP so it failed over
   *     to TCP
   * @return true if the resolver would initialize to it's current set of endpoints given this URL.
   */
  public boolean queryMatches(DsSipURL url, boolean sizeExceedsMTU) {
    if (m_resolverInfo == null) return false;
    return DsSipResolverUtils.queryMatches(
            url,
            m_resolverInfo.getRemoteAddressStr(),
            m_resolverInfo.getRemotePort(),
            m_resolverInfo.getTransport())
        && (m_sizeExceedsMTU == sizeExceedsMTU);
  }

  /**
   * Called to initialize the the resolver. The resolver will typically create a list of potential
   * endpoints to contact. Two versions of signatures - with/without local binding specified.
   */
  public void initialize(DsNetwork network, DsSipURL url)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException {
    // delegate to helper
    DsSipResolverUtils.initialize(network, this, url);
  }

  public void initialize(DsNetwork network, InetAddress localAddress, int localPort, DsSipURL url)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException {
    // delegate to helper
    DsSipResolverUtils.initialize(network, this, localAddress, localPort, url);
  }

  public void initialize(DsNetwork network, String host, int port, Transport transport)
      throws UnknownHostException, DsSipServerNotFoundException {
    // delegate to helper
    DsSipResolverUtils.initialize(network, this, host, port, transport);
  }

  public void initialize(
      DsNetwork network,
      InetAddress localAddress,
      int localPort,
      String host,
      int port,
      Transport transport)
      throws UnknownHostException, DsSipServerNotFoundException {
    // delegate to helper
    DsSipResolverUtils.initialize(network, this, localAddress, localPort, host, port, transport);
  }

  public void initialize(
      DsNetwork network,
      InetAddress localAddr,
      int localPort,
      String host,
      int port,
      Transport transport,
      boolean haveIP)
      throws UnknownHostException, DsSipServerNotFoundException {
    m_haveIP = haveIP;

    if (transport == DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED) {
      // GOGONG - 07.13.05 - get default outgoing transport from DsConfiManager instead of UDP
      // always
      transport = DsConfigManager.getDefaultOutgoingTransport();
    }

    // since the user sets the whether or not the protocol is supported we don't have to
    // worry about it here
    if (transport == Transport.UDP && m_sizeExceedsMTU && isSupported(Transport.TCP)) {
      if (Log.isEnabled(Level.INFO)) {
        Log.info(
            "initialize() - Exceeded UDP MTU size, switching to TCP and removing "
                + "local binding info.  Original localAddr = "
                + localAddr
                + " / localPort = "
                + localPort);
      }

      transport = Transport.TCP;

      // Here we are switching transports from UDP to TCP, because this packet will not fit
      // over UDP.  But, there is a case where we have set the local binding info for NAT,
      // and we will get a bind exception if this is the case for TCP, since we are already
      // bound to that port.  So we see if we indeed have a TCP listener
      // associated with this network object and use its address

      // we need to un-set this local binding info in case there is no listener
      localAddr = null;
      localPort = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;

      try {
        /* TODO
        DsStreamListener listener = (DsStreamListener) network.getListener(DsSipTransportType.TCP);
         if (listener != null) {
           localAddr = listener.localAddress;
         }*/
      } catch (NullPointerException e) {
        if (Log.isEnabled(Level.WARN)) {
          Log.info("Null pointer during access to network object for listener address.");
        }
      }
    }
    // MEETPASS TODO
    //    if (!isSupported(transport)) {
    //      throw new DsSipServerNotFoundException(
    //          "This stack does not support(not listening on) "
    //              + DsSipTransportType.intern(transport.name()));
    //    }

    if (port == DsBindingInfo.REMOTE_PORT_UNSPECIFIED) {
      if (transport == Transport.TLS) {
        port = DsSipTransportType.T_TLS.getDefaultPort();
      } else {
        port = DsSipTransportType.T_UDP.getDefaultPort(); // same as UDP
      }
    }

    m_resolverInfo =
        new DsBindingInfo(localAddr, localPort, InetAddress.getByName(host), port, transport);

    m_resolverInfo.setNetwork(network);

    m_end = false;

    if (Log.isEnabled(Level.DEBUG)) {
      Log.debug("Resolver Binding Info = " + m_resolverInfo);
    }
  }

  public void setSizeExceedsMTU(boolean sizeExceedsMTU) {
    m_sizeExceedsMTU = sizeExceedsMTU;
  }

  public void setSupportedTransports(byte supportedTransports) {
    m_supportedTransports = supportedTransports;
  }

  /** Call before calling initialize to determine whether or not it should be called. */
  public boolean shouldSearch(DsSipURL sip_url) throws DsSipParserException {
    return true;
  }

  public boolean shouldSearch(String host_name, int port, Transport transport) {
    return true;
  }

  /**
   * Return <code>true</code> if this resolver has been configured to support a particular transport
   * as defined in DsSipObject.DsSipTransportType.
   *
   * @param transport the transport to see if it is supported
   * @return <code>true</code> if this resolver has been configured to support a particular
   *     transport as defined in DsSipObject.DsSipTransportType.
   */
  public boolean isSupported(Transport transport) {
    return DsSipResolverUtils.isSupported(transport, m_supportedTransports, m_sizeExceedsMTU);
  }
}
