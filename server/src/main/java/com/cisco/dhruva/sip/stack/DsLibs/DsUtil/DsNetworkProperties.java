// Copyright (c) 2005-2011, 2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;

/**
 * This class holds network settings used during the instantiation or property change of a {@link
 * DsNetwork} object. An instance of this class is passed in to the {@link
 * DsNetwork#getNetwork(String name, DsNetworkProperties prop)} method to create a DsNewtork object
 * or the {@link DsNetwork#updateProperties(DsNetworkProperties prop)} method to change of the
 * properties of an existing {@link DsNetwork} object.
 *
 * <p>The values of the properties passed in to the above {@link DsNetwork} methods must be set with
 * caution. See this classes data members for limitations.
 */
public class DsNetworkProperties {
  /** The maximum number of default networks. */
  private static final int m_MAX_DEFAULT_NETWORKS = 8;

  /** Default network properties per default network. */
  private static DsNetworkProperties m_defNetworks[] =
      new DsNetworkProperties[m_MAX_DEFAULT_NETWORKS];

  /**
   * Default Network ID: default network properties as held in {@link DsConfigManager}. Used in
   * getDefault()
   */
  public static final int DEF_NETWORK = 0;

  /** Default Network ID: default ICMP network properties. Used in getDefault() */
  public static final int DEF_ICMP_NETWORK = 4;

  /** Default Network ID: default non-ICMP network properties. Used in getDefault() */
  public static final int DEF_NON_ICMP_NETWORK = 5;

  /** Default Network ID: default SIGCOMP network properties. Used in getDefault() */
  public static final int DEF_SIGCOMP_NETWORK = 6;

  /** Default Network ID: default NAT'd network properties. Used in getDefault() */
  public static final int DEF_NAT_NETWORK = 7;

  /**
   * Default Values. Note that doubling them here along with the orignal datat members in the
   * DsNetwork object allows the DsNetwork class to initialize completely off this class and keep
   * backwards compatability.
   */
  /** {@link DsConfigManager#PROP_TCP_NODELAY_DEFAULT}. */
  static final boolean DEFAULT_NODELAY;

  /** {@link DsConfigManager#PROP_TCP_SEND_BUFFER_DEFAULT}. */
  static final int DEFAULT_TCP_SENDBUFSIZE;

  /** {@link DsConfigManager#PROP_TCP_SEND_BUFFER_DEFAULT}. */
  static final int DEFAULT_TLS_SENDBUFSIZE;

  /** {@link DsConfigManager#PROP_UDP_SEND_BUFFER_DEFAULT}. */
  static final int DEFAULT_UDP_SENDBUFSIZE;

  /** {@link DsConfigManager#PROP_UDP_REC_BUFFER_DEFAULT}. */
  static final int DEFAULT_TCP_RECBUFSIZE;

  /** {@link DsConfigManager#PROP_TCP_REC_BUFFER_DEFAULT}. */
  static final int DEFAULT_TLS_RECBUFSIZE;

  /** {@link DsConfigManager#PROP_UDP_REC_BUFFER_DEFAULT}. */
  static final int DEFAULT_UDP_RECBUFSIZE;

  /** Default MTU size: {@link DsConfigManager#PROP_MTU_DEFAULT}. */
  static int DEFAULT_MTU;

  /** {@link DsConfigManager#PROP_DGRAM_CONNECTION_STRATEGY_DEFAULT}. */
  static final int DEFAULT_DGRAM_CONNECTION_STRATEGY;

  /** {@link DsConfigManager#PROP_DGRAM_TYPE_DEFAULT}. */
  static final int DEFAULT_DGRAM_TYPE;

  /** {@link DsConfigManager#PROP_NETWORK_COMP_TYPE_DEFAULT}. */
  static final int DEFAULT_NETWORK_COMP_TYPE;

  /** {@link DsConfigManager#PROP_MAX_UDP_PACKET_SIZE_DEFAULT}. */
  static final int DEFAULT_MAX_UDP_PACKET_SIZE;

  /** {@link DsConfigManager#PROP_MAX_TCP_MSG_SIZE_DEFAULT}. */
  static final int DEFAULT_MAX_TCP_MSG_SIZE;

  /** {@link DsConfigManager#PROP_OUT_CONNECTION}. */
  static final boolean DEFAULT_OUT_CONNECTION;

  /** {@link DsConfigManager#PROP_XTCP_OUT_QLEN}. */
  static final int DEFAULT_MAX_QUEUE_LENGTH;

  /** {@link DsConfigManager#PROP_ADD_CLIENT_SIDE_RPORT}. */
  static final boolean DEFAULT_ADD_CLIENT_SIDE_RPORT;

  /** {@link DsConfigManager#}. */
  static final int DEFAULT_INVITE_EXPIRATION;

  /** {@link DsConfigManager#PROP_100REL_SUPPORT_DEFAULT}. */
  static final int DEFAULT_100REL_SUPPORT;

  /** {@link DsConfigManager#PROP_SIMPLE_RESOLVER_DEFAULT}. */
  // Not final, this can be changed by the app.
  static boolean DEFAULT_SIMPLE_RESOLVER;

  /** {@link DsConfigManager#PROP_NAPTR_DEFAULT}. */
  // Not final, this can be changed by the app.
  static boolean DEFAULT_NAPTR_ENABLED;

  /** {@link DsConfigManager#PROP_SO_TIMEOUT}. */
  static final int DEFAULT_SO_TIMEOUT;

  /** {@link DsConfigManager#PROP_TCP_CONN_TIMEOUT}. */
  static final int DEFAULT_TCP_CONN_TIMEOUT;

  /** {@link DsConfigManager#PROP_TCP_CONN_TIMEOUT}. */
  static final int DEFAULT_TLS_HANDSHAKE_TIMEOUT;

  /** {@link DsConfigManager#PROP_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED}. */
  static final boolean DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED;

  /** {@link DsConfigManager#PROP_DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED}. */
  static final boolean DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED;

  static final boolean DEFAULT_REMOVE_OWN_ROUTE_HEADER;

  /** {@link DsConfigManager#PROP_PEER_CERT_INFO_HEADER_ENABLED}. */
  static final boolean DEFAULT_PEER_CERT_INFO_HEADER_ENABLED;

  /**
   * Enabling this flag Adds a Peer Cert info header to the Initial Invite message in case of TLS *
   */
  boolean peerCertInfoHeader;

  static {
    DEFAULT_NODELAY =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_TCP_NODELAY, DsConfigManager.PROP_TCP_NODELAY_DEFAULT);
    DEFAULT_TCP_SENDBUFSIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_TCP_SEND_BUFFER, DsConfigManager.PROP_TCP_SEND_BUFFER_DEFAULT);
    DEFAULT_TLS_SENDBUFSIZE = DEFAULT_TCP_SENDBUFSIZE;
    DEFAULT_UDP_SENDBUFSIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_UDP_SEND_BUFFER, DsConfigManager.PROP_UDP_SEND_BUFFER_DEFAULT);
    DEFAULT_TCP_RECBUFSIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_TCP_REC_BUFFER, DsConfigManager.PROP_TCP_REC_BUFFER_DEFAULT);
    DEFAULT_TLS_RECBUFSIZE = DEFAULT_TCP_RECBUFSIZE;
    DEFAULT_UDP_RECBUFSIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_UDP_REC_BUFFER, DsConfigManager.PROP_UDP_REC_BUFFER_DEFAULT);

    DEFAULT_MTU =
        DsConfigManager.getProperty(DsConfigManager.PROP_MTU, DsConfigManager.PROP_MTU_DEFAULT);

    DEFAULT_DGRAM_CONNECTION_STRATEGY =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DGRAM_CONNECTION_STRATEGY,
            DsConfigManager.PROP_DGRAM_CONNECTION_STRATEGY_DEFAULT);

    DEFAULT_DGRAM_TYPE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DGRAM_TYPE, DsConfigManager.PROP_DGRAM_TYPE_DEFAULT);

    DEFAULT_NETWORK_COMP_TYPE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_NETWORK_COMP_TYPE, DsConfigManager.PROP_NETWORK_COMP_TYPE_DEFAULT);

    DEFAULT_MAX_UDP_PACKET_SIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_MAX_UDP_PACKET_SIZE,
            DsConfigManager.PROP_MAX_UDP_PACKET_SIZE_DEFAULT);

    DEFAULT_MAX_TCP_MSG_SIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_MAX_TCP_MSG_SIZE, DsConfigManager.PROP_MAX_TCP_MSG_SIZE_DEFAULT);

    DEFAULT_OUT_CONNECTION =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_OUT_CONNECTION, DsConfigManager.PROP_OUT_CONNECTION_DEFAULT);

    DEFAULT_MAX_QUEUE_LENGTH =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_XTCP_OUT_QLEN, DsConfigManager.PROP_XTCP_OUT_QLEN_DEFAULT);

    DEFAULT_ADD_CLIENT_SIDE_RPORT =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_ADD_CLIENT_SIDE_RPORT,
            DsConfigManager.PROP_ADD_CLIENT_SIDE_RPORT_DEFAULT);

    DEFAULT_INVITE_EXPIRATION =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DEFAULT_INVITE_EXPIRATION,
            DsConfigManager.getDefaultInviteExpiration());

    DEFAULT_SIMPLE_RESOLVER =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SIMPLE_RESOLVER, DsConfigManager.PROP_SIMPLE_RESOLVER_DEFAULT);

    DEFAULT_NAPTR_ENABLED =
        DsConfigManager.getProperty(DsConfigManager.PROP_NAPTR, DsConfigManager.PROP_NAPTR_DEFAULT);

    DEFAULT_SO_TIMEOUT =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SO_TIMEOUT, DsConfigManager.PROP_SO_TIMEOUT_DEFAULT);

    DEFAULT_TCP_CONN_TIMEOUT =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_TCP_CONN_TIMEOUT, DsConfigManager.PROP_TCP_CONN_TIMEOUT_DEFAULT);

    DEFAULT_TLS_HANDSHAKE_TIMEOUT =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SSL_HANDSHAKE_TIMEOUT,
            DsConfigManager.PROP_SSL_HANDSHAKE_TIMEOUT_DEFAULT);

    DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED,
            DsConfigManager.PROP_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED_DEFAULT);

    DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED,
            DsConfigManager.PROP_DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED_DEFAULT);

    DEFAULT_REMOVE_OWN_ROUTE_HEADER =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_REMOVE_OWN_ROUTE_HEADER,
            DsConfigManager.PROP_REMOVE_OWN_ROUTE_HEADER_DEFAULT);

    DEFAULT_PEER_CERT_INFO_HEADER_ENABLED =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_PEER_CERT_INFO_HEADER_ENABLED,
            DsConfigManager.PROP_PEER_CERT_INFO_HEADER_ENABLED_DEFAULT);
    int tmp100RelSupport;
    try {
      DsSipTransactionManager tm = DsSipTransactionManager.getTransactionManager();
      tmp100RelSupport = tm.get100relSupport();
    } catch (NullPointerException e) {
      tmp100RelSupport =
          DsConfigManager.getProperty(
              DsConfigManager.PROP_100REL_SUPPORT, DsConfigManager.PROP_100REL_SUPPORT_DEFAULT);
    }
    DEFAULT_100REL_SUPPORT = tmp100RelSupport;

    for (int id = 0; id < m_MAX_DEFAULT_NETWORKS; id++) {
      m_defNetworks[id] = new DsNetworkProperties();
    }
    setupDefaultTLS();
    setupDefaultTCP();
    setupDefaultUDP();
    setupDefaultICMP();
    setupDefaultNonICMP();
    setupDefaultSIGCOMP();
    setupDefaultNAT();
  }

  private static void setupDefaultTLS() {
    // nothing specifically different from defaults needed
  }

  private static void setupDefaultTCP() {
    // nothing specifically different from defaults needed
  }

  private static void setupDefaultUDP() {
    // nothing specifically different from defaults needed
  }

  private static void setupDefaultICMP() {
    m_defNetworks[DEF_ICMP_NETWORK].m_datagramConnectionStrategy = DsNetwork.DGRAM_PER_ENDPOINT;
    m_defNetworks[DEF_ICMP_NETWORK].m_datagramType = DsNetwork.DGRAM_ICMP;
  }

  private static void setupDefaultNonICMP() {
    m_defNetworks[DEF_NON_ICMP_NETWORK].m_datagramConnectionStrategy = DsNetwork.DGRAM_PER_THREAD;
    m_defNetworks[DEF_NON_ICMP_NETWORK].m_datagramType = DsNetwork.DGRAM_DEFAULT;
  }

  private static void setupDefaultSIGCOMP() {
    m_defNetworks[DEF_SIGCOMP_NETWORK].m_compressionType = DsNetwork.NET_COMP_SIGCOMP;
    m_defNetworks[DEF_SIGCOMP_NETWORK].m_datagramType = DsNetwork.DGRAM_DEFAULT;
  }

  private static void setupDefaultNAT() {
    m_defNetworks[DEF_NAT_NETWORK].m_datagramConnectionStrategy =
        DsNetwork.DGRAM_USING_LISTEN_PORTS;
    m_defNetworks[DEF_NAT_NETWORK].m_datagramType = DsNetwork.DGRAM_DEFAULT;
  }

  /**
   * Retrieve a copy of the default the properties object for a given identifier. This returned
   * DsNetworkProperties object can then be altered to suit the needs of the application and passed
   * in to {@link DsNetwork} creation and update methods.
   *
   * @param defPropertyID The unique identifier of the desired default properties object.
   * @return a copy of the desired default properties object
   * @throws IllegalArgumentException if the identifier is an invalid value.
   */
  public static DsNetworkProperties getDefault(int defPropertyID) {
    if (defPropertyID >= DEF_NETWORK && defPropertyID < m_MAX_DEFAULT_NETWORKS) {
      return new DsNetworkProperties(m_defNetworks[defPropertyID]);
    } else {
      throw new IllegalArgumentException("Invalid identifier for default network property request");
    }
  }

  /** The buffer sizes. */
  public int[] m_receiveBufferSizes = new int[DsSipTransportType.ARRAY_SIZE];

  /** The send buffer sizes. */
  public int[] m_sendBufferSizes = new int[DsSipTransportType.ARRAY_SIZE];

  /** TCP no delay. */
  public boolean m_tcpNoDelay;

  /** Configured (vs discovered) MTU. */
  public int m_MTU;

  /**
   * The datagram connection strategy. If this is set to {@link DsNetwork#DGRAM_USING_LISTEN_PORTS}
   * then the m_datagramType must be set to {@link DsNetwork#DGRAM_DEFAULT}.
   */
  public int m_datagramConnectionStrategy;

  /** * The compression type. If this is set to then the * m_datagramType must be set to */
  public int m_compressionType;

  /** Maximum TCP message size. */
  public int m_maxTcpMessageSize;

  /** Default expiration for an INVITE. */
  public int m_defaultInviteExpiration;

  /** Whether to use the simple resolver or not. */
  public boolean m_simpleResolver;

  /** Whether to use NAPTR lookups or not. */
  public boolean m_NAPTREnabled;

  /** Value of the 100 reliable support. */
  public byte m_100relSupport;

  /** Maximum UDP message size. */
  public int m_maxUdpPacketSize;

  /** Tells whether a new outgoing connection can be opened through this network. */
  public boolean m_outConnection;

  /** Maximum queue size. */
  public int m_maxQueueSize;

  /**
   * Datagram Socket Type. If this is set to {@link DsNetwork#DGRAM_ICMP} then the
   * m_datagramConnectionStrategy must be set to {@link DsNetwork#DGRAM_PER_ENDPOINT}.
   */
  public int m_datagramType;

  /** Whether or not to add "rport" to the via header. */
  public boolean m_addClientSideRPort;

  /** SO_TIMEOUT parameter, in milliseconds. */
  public int m_soTimeout;

  /** TCP_CONN_TIMEOUT parameter, in milliseconds. */
  public int m_tcpConnectionTimeout;

  /** TCP_CONN_TIMEOUT parameter, in milliseconds. */
  public int m_tlsHandshakeTimeout;

  /** Dns lookup Lync Federation enabled flag. */
  public boolean m_dnsLookupTLSLyncFederationEnabled;

  public boolean certServiceTrustManagerEnabled = false;

  /** Rewrites destination Route to IP */
  public boolean convertDestinationRouteToIP;

  public boolean removeOwnRouteHeader;

  //////////////////////////////////////////////////////////////////
  /** Default constructor. Uses default values as defined in {@link DsNetwork}. */
  public DsNetworkProperties() {
    // set default values
    m_receiveBufferSizes[DsSipTransportType.TCP] = DEFAULT_TCP_RECBUFSIZE;
    m_receiveBufferSizes[DsSipTransportType.TLS] = DEFAULT_TLS_RECBUFSIZE;
    m_receiveBufferSizes[DsSipTransportType.UDP] = DEFAULT_UDP_RECBUFSIZE;

    m_sendBufferSizes[DsSipTransportType.TCP] = DEFAULT_TCP_SENDBUFSIZE;
    m_sendBufferSizes[DsSipTransportType.TLS] = DEFAULT_TLS_SENDBUFSIZE;
    m_sendBufferSizes[DsSipTransportType.UDP] = DEFAULT_UDP_SENDBUFSIZE;

    m_tcpNoDelay = DEFAULT_NODELAY;
    m_MTU = DEFAULT_MTU;
    m_datagramConnectionStrategy = DEFAULT_DGRAM_CONNECTION_STRATEGY;
    m_compressionType = DEFAULT_NETWORK_COMP_TYPE;
    m_maxTcpMessageSize = DEFAULT_MAX_TCP_MSG_SIZE;
    m_maxUdpPacketSize = DEFAULT_MAX_UDP_PACKET_SIZE;
    m_outConnection = DEFAULT_OUT_CONNECTION;
    m_maxQueueSize = DEFAULT_MAX_QUEUE_LENGTH;
    m_datagramType = DEFAULT_DGRAM_TYPE;
    m_addClientSideRPort = DEFAULT_ADD_CLIENT_SIDE_RPORT;
    m_defaultInviteExpiration = DEFAULT_INVITE_EXPIRATION;
    m_simpleResolver = DEFAULT_SIMPLE_RESOLVER;
    m_NAPTREnabled = DEFAULT_NAPTR_ENABLED;
    m_100relSupport = (byte) DEFAULT_100REL_SUPPORT;
    m_soTimeout = DEFAULT_SO_TIMEOUT;
    m_tcpConnectionTimeout = DEFAULT_TCP_CONN_TIMEOUT;
    m_tlsHandshakeTimeout = DEFAULT_TLS_HANDSHAKE_TIMEOUT;
    m_dnsLookupTLSLyncFederationEnabled = DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED;
    convertDestinationRouteToIP = DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED;
    removeOwnRouteHeader = DEFAULT_REMOVE_OWN_ROUTE_HEADER;
    peerCertInfoHeader = DEFAULT_PEER_CERT_INFO_HEADER_ENABLED;
  }

  /** Copy constructor. */
  private DsNetworkProperties(DsNetworkProperties prop) {
    // copy all the data elements
    System.arraycopy(
        prop.m_sendBufferSizes, 0, m_sendBufferSizes, 0, prop.m_sendBufferSizes.length);
    System.arraycopy(
        prop.m_receiveBufferSizes, 0, m_receiveBufferSizes, 0, prop.m_receiveBufferSizes.length);

    m_tcpNoDelay = prop.m_tcpNoDelay;
    m_MTU = prop.m_MTU;
    m_datagramConnectionStrategy = prop.m_datagramConnectionStrategy;
    m_compressionType = prop.m_compressionType;
    m_maxTcpMessageSize = prop.m_maxTcpMessageSize;
    m_maxUdpPacketSize = prop.m_maxUdpPacketSize;
    m_outConnection = prop.m_outConnection;
    m_maxQueueSize = prop.m_maxQueueSize;
    m_datagramType = prop.m_datagramType;
    m_addClientSideRPort = prop.m_addClientSideRPort;
    m_defaultInviteExpiration = prop.m_defaultInviteExpiration;
    m_simpleResolver = prop.m_simpleResolver;
    m_NAPTREnabled = prop.m_NAPTREnabled;
    m_100relSupport = prop.m_100relSupport;
    m_soTimeout = prop.m_soTimeout;
    m_tcpConnectionTimeout = prop.m_tcpConnectionTimeout;
    m_tlsHandshakeTimeout = prop.m_tlsHandshakeTimeout;
    m_dnsLookupTLSLyncFederationEnabled = prop.m_dnsLookupTLSLyncFederationEnabled;
    convertDestinationRouteToIP = prop.convertDestinationRouteToIP;
    peerCertInfoHeader = prop.peerCertInfoHeader;
    certServiceTrustManagerEnabled = prop.certServiceTrustManagerEnabled;
  }

  /**
   * Utility method to validate the network propertys parameters combintation. The input buffer will
   * be used to contain the reason for failed validation if the properties are not valid for a
   * DsNetwork. The following restrictions apply to network properties:<br>
   *
   * <pre>
   *    - Datagram Socket Type of DGRAM_ICMP must be used with DsNetwork.DGRAM_PER_ENDPOINT");
   *    - Datagram Connection Strategy of type DsNetwork.DGRAM_USING_LISTEN_PORTS must be used
   *      with Datagram Socket default type
   *    - Datagram Compression Type NET_COMP_SIGCOMP can only be used with Datagram Socket default type
   *    - Datagram Compression Type must be a valid value:
   *      DsNetwork.NET_COMP_NONE, DsNetwork.NET_COMP_SIGCOMP, DsNetwork.NET_COMP_TOKEN_SIP
   *    - Datagram Connection Strategy must be a valid value:
   *      DsNetwork.DGRAM_PER_ENDPOINT, DsNetwork.DGRAM_USING_LISTEN_PORTS, DsNetwork.DGRAM_PER_THREAD
   *    - Datagram type must be a valid value: DsNetwork.DGRAM_DEFAULT, DsNetwork.DGRAM_ICMP
   * </pre>
   *
   * @param retError the reason for a false return value
   * @return <code>true</code> if the given properties represent a valid DsNetwork, otherwise <code>
   *     false</code>
   */
  public boolean validate(StringBuffer retError) {
    if (m_datagramType == DsNetwork.DGRAM_ICMP
        && m_datagramConnectionStrategy != DsNetwork.DGRAM_PER_ENDPOINT) {
      retError.append("Datagram Socket Type of DGRAM_ICMP must be used with DGRAM_PER_ENDPOINT");
      return false;
    }

    if (m_datagramConnectionStrategy == DsNetwork.DGRAM_USING_LISTEN_PORTS
        && m_datagramType != DsNetwork.DGRAM_DEFAULT) {
      retError.append(
          "Datagram Connection Strategy of type DGRAM_USING_LIS TEN_PORTS must be used with Datagram Socket default type");
      return false;
    }

    if (m_compressionType == DsNetwork.NET_COMP_SIGCOMP
        && m_datagramType != DsNetwork.DGRAM_DEFAULT) {
      retError.append(
          "Datagram Compression Type NET_COMP_SIGCOMP can only be used with Datagram Socket default type");
      return false;
    }

    if (m_compressionType < DsNetwork.NET_COMP_NONE
        || m_compressionType > DsNetwork.NET_COMP_TOKEN_SIP) {
      retError.append("Invalid Network Compression value: ").append(m_compressionType);
      retError.append("Invalid Network Compression value: ").append(m_compressionType);
      return false;
    }

    if (m_datagramConnectionStrategy < DsNetwork.DGRAM_PER_ENDPOINT
        || m_datagramConnectionStrategy > DsNetwork.DGRAM_USING_LISTEN_PORTS) {
      retError
          .append("Invalid Datagram Connection Stragegy value: ")
          .append(m_datagramConnectionStrategy);
      return false;
    }

    if (m_datagramType < DsNetwork.DGRAM_DEFAULT || m_datagramType > DsNetwork.DGRAM_ICMP) {
      retError.append("Invalid Datagram Socket Type value: ").append(m_datagramType);
      return false;
    }

    // Add this if we end up porting QoS here
    // if (m_QoS < 0 || m_QoS > 255)
    // {
    //    retError.append("qos is not in range 0 -- 255");
    //    return false;
    // }

    return true;
  }

  /**
   * Gets the default for whether or not NAPTR lookups are enabled.
   *
   * @return <code>true</code> if NAPTR lookup are enabled by default
   */
  public static boolean getNAPTREnabledDefault() {
    return DEFAULT_NAPTR_ENABLED;
  }

  /**
   * Sets the default for whether or not NAPTR lookups are enabled. This becomes the new default but
   * does not change the value in the existing networks, only future networks.
   *
   * @param enabled if <code>true</code>, then NAPTR lookup will be enabled, disabled otherwise.
   */
  public static void setNAPTREnabledDefault(boolean enabled) {
    DEFAULT_NAPTR_ENABLED = enabled;
  }

  /**
   * Gets the default MTU size.
   *
   * @return the default MTU size
   */
  public static int getMTUDefaultSize() {
    return DEFAULT_MTU;
  }

  /**
   * Sets the default MTU size. This becomes the new default but does not change the value in the
   * existing networks, only future networks.
   *
   * @param size the new size of the MTU for this network
   */
  public static void setMTUDefaultSize(int size) {
    DEFAULT_MTU = size;
  }

  /**
   * Gets the default for whether or not the Simple Resolver is used.
   *
   * @return <code>true</code> if the Simple Resolver is enabled by default
   */
  public static boolean getSimpleResolverDefault() {
    return DEFAULT_SIMPLE_RESOLVER;
  }

  /**
   * Sets the default for whether or not the Simple Resolver is used. This becomes the new default
   * but does not change the value in the existing networks, only future networks.
   *
   * @param enabled if <code>true</code>, then the Simple Resolver will be enabled, disabled
   *     otherwise.
   */
  public static void setSimpleResolverDefault(boolean enabled) {
    DEFAULT_SIMPLE_RESOLVER = enabled;
  }
}
