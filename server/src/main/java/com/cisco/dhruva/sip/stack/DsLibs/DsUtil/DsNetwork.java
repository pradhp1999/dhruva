// Copyright (c) 2005-2011, 2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTimers;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.util.HashMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

/**
 * This class holds network specific configuration data. It is used to allow the application to
 * control characteristics of the sockets and buffers based on logical network. When a listen point
 * or connection is created, an instance of this class may be used to control the associated
 * parameters. The application code may associate an instance of this class with an outgoing message
 * using {@link DsSipRequest#setBindingInfo}. An instance of this class is associated with each
 * listen point and becomes part of the binding information for messages received on that listen
 * point.
 *
 * <p>If no network specific behavior is desired, the application code need not be concerned with
 * this class: reasonable defaults will be applied.
 */
public class DsNetwork implements Cloneable {
  // //////// Static ///////////////////////////////
  //
  /** The logger for this class. */
  private static Logger cat = DsLog4j.connectionCat;

  /** The next network number. */
  private static short m_next = 0;

  /** The lock for access to the m_next counter */
  private static Object m_counterLock = new Object();

  /** The max number of logical networks. */
  private static final int MAX_NETWORKS = 256; // we can support 256 networks (byte limitation)

  /** A Map from string to DsNetwork. */
  private static HashMap m_fromString = new HashMap();

  /** A Map from DsByteString to DsNetwork. */
  private static HashMap m_fromByteString = new HashMap();

  /** Map from byte to DsNetwork. */
  private static DsNetwork[] m_fromByte = new DsNetwork[MAX_NETWORKS];

  /** Connection strategy: create a datagram socket per endpoint. */
  public static final int DGRAM_PER_ENDPOINT = 0;

  /** Connection strategy: create a datagram socket per sending thread. */
  public static final int DGRAM_PER_THREAD = 1;

  /**
   * Connection strategy: send SIP Requests using the same datagram socket as the listen point
   * specified in the local binding info. for Responses use the local binding info of the
   * corresponding request to find the connection object to send messages.
   */
  public static final int DGRAM_USING_LISTEN_PORTS = 2;

  /**
   * Datagram Type: Default Datagram type is JDK datagram sockets that are not connected by default.
   */
  public static final int DGRAM_DEFAULT = 0;

  /**
   * Datagram Type: ICMP Datagram Socket are connected to remote address, if not already connected,
   * upon first send request.
   */
  public static final int DGRAM_ICMP = 1;

  /**
   * Data Compression Type: No data compression used over connections made on this type of network.
   */
  public static final int NET_COMP_NONE = 0;

  /**
   * Data Compression Type: SIGCOMP data comression used over connections made on this type of
   * network.
   */
  public static final int NET_COMP_SIGCOMP = 1;

  /**
   * Data Compression Type: Token SIP data comression used over connections made on this type of
   * network.
   */
  public static final int NET_COMP_TOKEN_SIP = 2;

  /** Descriptions of datagram connection strategies */
  private static String[] m_datagramConnectionStrategyDefs = {
    "creation strategy DGRAM_PER_ENDPOINT: For sending, create a datagram socket per endpoint",
    "creation strategy DGRAM_PER_THREAD:   For sending, create a datagram socket per sending thread",
    "creation strategy DGRAM_USING_LISTEN_PORT:  Send SIP Requests using the same datagram socket as the listen"
        + " point specified in the local binding info or, for Responses, the local binding info of the corresponding request"
  };

  /** Descriptions of datagram compression types */
  private static String[] m_networkCompressionTypeDefs = {
    "network compression type NET_COMP_NONE: Perform no compression on data before sending - default",
    "network compression type NET_COMP_SIGCOMP: Perform sigcomp compression on data before sending  for Solaris - fallback to JDK default",
    "network compression type NET_COMP_TOKEN_SIP: Perform Tokenized SIP compression on data before sending; for Solaris - fallback to JDK default"
  };

  /** Descriptions of Datagram Types */
  private static String[] m_datagramTypeDefs = {
    "datagram type DGRAM_DEFAULT: Use the default (JDK) datagram socket",
    "datagram type DGRAM_ICMP: Use automatically connected datagram sockets - fallback to JDK default"
  };

  /** No network set. */
  public static final byte NONE = -1;

  /** A lock object that is used to synchronization. */
  private static final Object lockObj = new Object();

  /** Parameter description. */
  private static final String DESC_NODELAY = "Don't Use Nagle's Algorithm";
  /** {@link DsConfigManager#PROP_TCP_NODELAY_DEFAULT}. */
  public static boolean DEFAULT_NODELAY;

  /** Parameter description. */
  private static final String DESC_TCP_SENDBUFSIZE = "TCP Send Buffer Size";
  /** {@link DsConfigManager#PROP_TCP_SEND_BUFFER_DEFAULT}. */
  public static int DEFAULT_TCP_SENDBUFSIZE;

  /** Parameter description. */
  private static final String DESC_TLS_SENDBUFSIZE = "TLS Send Buffer Size";
  /** {@link DsConfigManager#PROP_TCP_SEND_BUFFER_DEFAULT}. */
  public static int DEFAULT_TLS_SENDBUFSIZE;

  /** Parameter description. */
  private static final String DESC_UDP_SENDBUFSIZE = "UDP Send Buffer Size";
  /** {@link DsConfigManager#PROP_UDP_SEND_BUFFER_DEFAULT}. */
  public static int DEFAULT_UDP_SENDBUFSIZE;

  /** Parameter description. */
  private static final String DESC_TCP_RECBUFSIZE = "TCP Receive Buffer Size";
  /** {@link DsConfigManager#PROP_UDP_REC_BUFFER_DEFAULT}. */
  public static int DEFAULT_TCP_RECBUFSIZE;

  /** Parameter description. */
  private static final String DESC_TLS_RECBUFSIZE = "TLS Receive Buffer Size";
  /** {@link DsConfigManager#PROP_TCP_REC_BUFFER_DEFAULT}. */
  public static int DEFAULT_TLS_RECBUFSIZE;

  /** Parameter description. */
  private static final String DESC_UDP_RECBUFSIZE = "UDP Receive Buffer Size";
  /** {@link DsConfigManager#PROP_UDP_REC_BUFFER_DEFAULT}. */
  public static int DEFAULT_UDP_RECBUFSIZE;

  /** Parameter description. */
  private static final String DESC_MTU = "Path MTU";
  /** 1500. */
  public static final int DEFAULT_MTU;

  /** Parameter description. */
  private static final String DESC_DGRAM_CONNECTION_STRATEGY = "Datagram Connection Strategy";
  /** {@link DsConfigManager#PROP_DGRAM_CONNECTION_STRATEGY_DEFAULT}. */
  public static final int DEFAULT_DGRAM_CONNECTION_STRATEGY;

  /** Parameter description. */
  private static final String DESC_DGRAM_TYPE = "Datagram Type";
  /** {@link DsConfigManager#PROP_DGRAM_TYPE_DEFAULT}. */
  public static final int DEFAULT_DGRAM_TYPE;

  /** Parameter description. */
  private static final String DESC_NETWORK_COMP_TYPE = "Network Compression Type";
  /** {@link DsConfigManager#PROP_NETWORK_COMP_TYPE_DEFAULT}. */
  public static final int DEFAULT_NETWORK_COMP_TYPE;

  /** Parameter description. */
  private static final String DESC_MAX_UDP_PACKET_SIZE = "Maximum UDP packet size";
  /** {@link DsConfigManager#PROP_MAX_UDP_PACKET_SIZE_DEFAULT}. */
  public static int DEFAULT_MAX_UDP_PACKET_SIZE;

  /** Parameter description. */
  private static final String DESC_MAX_TCP_MSG_SIZE = "Maximum TCP/TLS message size";
  /** {@link DsConfigManager#PROP_MAX_TCP_MSG_SIZE_DEFAULT}. */
  public static int DEFAULT_MAX_TCP_MSG_SIZE;

  /** Parameter description. */
  private static final String DESC_DEFAULT_INVITE_EXPIRATION = "Default Invite expiration";
  /** {@link DsConfigManager#PROP_DEFAULT_INVITE_EXPIRATION}. */
  private static final int DEFAULT_INVITE_EXPIRATION;

  /** Parameter description. */
  private static final String DESC_100REL_SUPPORT = "100rel is Required/Supported/Unsupported";
  /** {@link DsConfigManager#PROP_100REL_SUPPORT_DEFAULT}. */
  private static final int DEFAULT_100REL_SUPPORT;

  /** Parameter description. */
  private static final String DESC_NAPTR_ENABLED = "True if NAPTR is enabled.";
  /** {@link DsConfigManager#PROP_NAPTR_DEFAULT}. */
  private static final boolean DEFAULT_NAPTR_ENABLED;

  /** Parameter description. */
  private static final String DESC_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED =
      "True if Dns loookup tls for lync federation is enabled.";
  /** {@link DsConfigManager#PROP_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED}. */
  private static final boolean DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED;

  /** Parameter description. */
  private static final String DESC_PEER_CERT_INFO_HEADER_ENABLED =
      "True if Peer Cert info header insertion is enabled.";
  /** Parameter description. */
  private static final String DESC_SIMPLE_RESOLVER =
      "True if the simple resolver is being used, false for SRV";
  /** {@link DsConfigManager#PROP_SIMPLE_RESOLVER_DEFAULT}. */
  private static final boolean DEFAULT_SIMPLE_RESOLVER;

  /** Parameter description. */
  private static final String DESC_OUT_CONNECTION =
      "Tells if new outgoing connections are allowed to be made on this network";
  /** {@link DsConfigManager#PROP_OUT_CONNECTION}. */
  public static boolean DEFAULT_OUT_CONNECTION;

  /** Parameter description. */
  private static final String DESC_MAX_QUEUE_LENGTH =
      "The number of SIP messages to hold once the TCP output buffer has filled.";
  /** {@link DsConfigManager#PROP_XTCP_OUT_QLEN}. */
  public static int DEFAULT_MAX_QUEUE_LENGTH;

  /** Parameter description. */
  private static final String DESC_ADD_CLIENT_SIDE_RPORT =
      "Add rport to a request sent from this network.";
  /** {@link DsConfigManager#PROP_ADD_CLIENT_SIDE_RPORT}. */
  public static boolean DEFAULT_ADD_CLIENT_SIDE_RPORT;

  /** Parameter description. */
  private static final String DESC_SSL_CONTEXT = "Set this to use TLS.";

  /** Parameter description. */
  private static final String DESC_SO_TIMEOUT = "The SO_TIMEOUT socket parameter for TCP and TLS.";
  /** {@link DsConfigManager#PROP_SO_TIMEOUT}. */
  public static int DEFAULT_SO_TIMEOUT;

  /** Parameter description. */
  private static final String DESC_TLS_HANDSHAKE_TIMEOUT = "The TLS handshake timeout parameter.";
  /** {@link DsConfigManager#PROP_TCP_CONN_TIMEOUT}. */
  public static int DEFAULT_TLS_HANDSHAKE_TIMEOUT;

  /** Parameter description. */
  private static final String DESC_TCP_CONN_TIMEOUT =
      "The connection setup timeout parameter for TCP and TLS.";
  /** {@link DsConfigManager#PROP_TCP_CONN_TIMEOUT}. */
  public static int DEFAULT_TCP_CONN_TIMEOUT;

  /** The name of the default network. */
  public static final String STR_DEFAULT = "DEFAULT";

  /** The default network. */
  public static DsNetwork DEFAULT;

  static {
    DEFAULT_NODELAY = DsNetworkProperties.DEFAULT_NODELAY;
    DEFAULT_TCP_SENDBUFSIZE = DsNetworkProperties.DEFAULT_TCP_SENDBUFSIZE;
    DEFAULT_TLS_SENDBUFSIZE = DsNetworkProperties.DEFAULT_TCP_SENDBUFSIZE;
    DEFAULT_UDP_SENDBUFSIZE = DsNetworkProperties.DEFAULT_UDP_SENDBUFSIZE;
    DEFAULT_TCP_RECBUFSIZE = DsNetworkProperties.DEFAULT_TCP_RECBUFSIZE;
    DEFAULT_TLS_RECBUFSIZE = DsNetworkProperties.DEFAULT_TCP_RECBUFSIZE;
    DEFAULT_UDP_RECBUFSIZE = DsNetworkProperties.DEFAULT_UDP_RECBUFSIZE;
    DEFAULT_MTU = DsNetworkProperties.DEFAULT_MTU;
    DEFAULT_DGRAM_CONNECTION_STRATEGY = DsNetworkProperties.DEFAULT_DGRAM_CONNECTION_STRATEGY;
    DEFAULT_DGRAM_TYPE = DsNetworkProperties.DEFAULT_DGRAM_TYPE;
    DEFAULT_NETWORK_COMP_TYPE = DsNetworkProperties.DEFAULT_NETWORK_COMP_TYPE;
    DEFAULT_MAX_UDP_PACKET_SIZE = DsNetworkProperties.DEFAULT_MAX_UDP_PACKET_SIZE;
    DEFAULT_MAX_TCP_MSG_SIZE = DsNetworkProperties.DEFAULT_MAX_TCP_MSG_SIZE;
    DEFAULT_OUT_CONNECTION = DsNetworkProperties.DEFAULT_OUT_CONNECTION;
    DEFAULT_MAX_QUEUE_LENGTH = DsNetworkProperties.DEFAULT_MAX_QUEUE_LENGTH;
    DEFAULT_ADD_CLIENT_SIDE_RPORT = DsNetworkProperties.DEFAULT_ADD_CLIENT_SIDE_RPORT;
    DEFAULT_INVITE_EXPIRATION = DsNetworkProperties.DEFAULT_INVITE_EXPIRATION;
    DEFAULT_100REL_SUPPORT = DsNetworkProperties.DEFAULT_100REL_SUPPORT;
    DEFAULT_SIMPLE_RESOLVER = DsNetworkProperties.DEFAULT_SIMPLE_RESOLVER;
    DEFAULT_NAPTR_ENABLED = DsNetworkProperties.DEFAULT_NAPTR_ENABLED;
    DEFAULT_SO_TIMEOUT = DsNetworkProperties.DEFAULT_SO_TIMEOUT;
    DEFAULT_TCP_CONN_TIMEOUT = DsNetworkProperties.DEFAULT_TCP_CONN_TIMEOUT;
    DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED =
        DsNetworkProperties.DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED;
    try {
      // create the default network.
      DEFAULT =
          getNetwork(STR_DEFAULT, DsNetworkProperties.getDefault(DsNetworkProperties.DEF_NETWORK));
    } catch (Exception e) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn("Exception during default network creation.");
      }
    }
  }

  ///////// Member Data /////////////////////////////
  // IMPORTANT NOTE: If you add a member here it also needs to be added to
  // the DsNetworkProperties class. Default values are retrieved by that
  // class.
  // Also ensure you add new values to the dump method.
  //

  /** The name displayed by toString. */
  private String m_dispname;

  /** The unique name. */
  private String m_name;

  /** The base name chosen by application code. */
  private String m_basename;

  /** Byte String version of the unique name. */
  private DsByteString m_nameBs;

  /** Byte String version of the name displayed by toString. */
  private DsByteString m_dispnameBs;

  /** The unique number. */
  private byte m_number;

  /** The buffer sizes. */
  private int[] m_receiveBufferSizes = new int[DsSipTransportType.ARRAY_SIZE];

  /** The send buffer sizes. */
  private int[] m_sendBufferSizes = new int[DsSipTransportType.ARRAY_SIZE];

  /** TCP no delay. */
  private boolean m_tcpNoDelay;

  /** Configured (vs. discovered) MTU. */
  private int m_MTU;

  /** SIP timers. */
  private DsSipTimers m_sipTimers;

  /** Maximum TCP message size. */
  private int m_maxTcpMessageSize;

  /** Maximum UDP message size. */
  private int m_maxUdpPacketSize;

  /** Tells whether a new outgoing connection can be opened through this network. */
  private boolean m_outConnection;
  /** The number of SIP messages to hold once the TCP output buffer has filled. */
  private int m_maxQueueSize;

  /** Compression Type for data. */
  private int m_compressionType;

  /** Datagram Connection strategy. */
  private int m_datagramConnectionStrategy;

  /** Type of datagram sockets to use for UDP connections. */
  private int m_datagramType;

  /** Whether or not to add "rport" to the via header. */
  private boolean m_addClientSideRPort;

  /** Expiration for an INVITE. */
  private int m_defaultInviteExpiration;

  /** Used to indicate if 100rel is Required/Supported/Unsupported. */
  private byte m_100relSupport;

  /** Use simple resolver flag. */
  private boolean m_simpleResolver;

  /** NAPTR enabled flag. */
  private boolean m_NAPTREnabled;

  /** SSL Context. */
  private DsSSLContext m_SSLContext;

  /** SO_TIMEOUT. */
  private int m_soTimeout;

  /** TCP Connection setup timeout, in milliseconds. */
  private int m_tcpConnectionTimeout;

  /** TCP Connection setup timeout, in milliseconds. */
  private int m_tlsHandshakeTimeout;

  /** Dns lookup Lync Federation enabled flag. */
  private boolean m_dnsLookupTLSLyncFederationEnabled;

  /** Rewrites destination Route to IP */
  private boolean convertDestinationRouteToIP;

  private boolean removeOwnRouteHeader;

  /** variables to store threshold and TLS buffer size* */
  private int m_threshold = 80;

  private int m_maxBuffer = 256;

  private boolean peerCertInfoHeader;

  private boolean certServiceTrustManagerEnabled = false;

  private static final String UDP_EVENTLOOP_THREAD_COUNT = "dhruva.network.udpEventloopThreadCount";

  private static final Integer DEFAULT_UDP_EVENTLOOP_THREAD_COUNT = 1;

  private static final String CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_SECONDS =
      "dhruva.network.connectionCache.connectionIdleTimeout";

  private static final Integer DEFAULT_CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_MINUTES = 14400;

  private Environment env;

  public DsNetwork(Environment env) {
    this.env = env;
  }

  public int udpEventPoolThreadCount() {
    return env.getProperty(
        UDP_EVENTLOOP_THREAD_COUNT, Integer.class, DEFAULT_UDP_EVENTLOOP_THREAD_COUNT);
  }

  public int connectionCacheConnectionIdleTimeout() {
    return env.getProperty(
        CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_SECONDS,
        Integer.class,
        DEFAULT_CONNECTION_CACHE_CONNECTION_IDLE_TIMEOUT_MINUTES);
  }

  ///////// Static Method /////////////////////////////

  /**
   * Find a DsNetwork by number.
   *
   * @param number the network number.
   * @return the instance of the network for the supplied name
   */
  public static DsNetwork getNetwork(byte number) {
    short pNum = pos(number);
    if ((pNum < 0) || (pNum >= m_next)) {
      return null;
    }

    return m_fromByte[pNum];
  }

  /**
   * Create a new DsNetwork or retrieve an existing instance of a DsNetwork.
   *
   * @param name the base name of the network. If the named network does not already exist, the
   *     provided name will be used to create a new DsNetwork This method will assign a number to
   *     the network which is unique across all instances of this class. The network instance
   *     created by this method may be retrieved using DsNetwork.getNetwork(byte number) by using
   *     the value provided by DsNetwork.getNumber() called on the returned network object.
   * @return the instance of the network for the supplied name
   * @throws DsException if there is an error creating the network
   */
  public static DsNetwork getNetwork(String name) throws DsException {
    DsNetwork network;
    synchronized (m_fromString) {
      network = get(name);
      if (network == null) {
        network = create(name, DsNetworkProperties.getDefault(DsNetworkProperties.DEF_NETWORK));
        add(network.getName(), network);
      }
    }
    return network;
  }

  /**
   * Create a new DsNetwork or retrieve an existing instance of a DsNetwork.
   *
   * @param name the base name of the network. If the named network does not already exist, the
   *     provided name will be used to create a new DsNetwork This method will assign a number to
   *     the network which is unique across all instances of this class. The network instance
   *     created by this method may be retrieved using DsNetwork.getNetwork(byte number) by using
   *     the value provided by DsNetwork.getNumber() called on the returned network object.
   * @return the instance of the network for the supplied name
   * @throws DsException if there is an error creating the network
   */
  public static DsNetwork getNetwork(DsByteString name) throws DsException {
    DsNetwork network;
    synchronized (m_fromByteString) {
      network = get(name);
      if (network == null) {
        network = create(name, DsNetworkProperties.getDefault(DsNetworkProperties.DEF_NETWORK));
        add(network.getName(), network);
      }
    }
    return network;
  }

  /**
   * Create a new network object based upon the Properties given or retrieve the existing network
   * with the given name.
   *
   * @param name the base name of the network. If the named network does not already exist, the
   *     provided name is modified by this method to be unique and a new DsNetwork is created. This
   *     method will assign a number to the network which will be appended to the provided name
   *     (e.g. mynetwork becomes "mynetwork-n" where n is the unique across all instances of this
   *     class.) The network instance created by this method may be referenced in the future by
   *     using the name provided by DsNetwork.getName() on the returned network object.
   * @param prop The properties used to set the new network objects capabilities. See {@link
   *     DsNetworkProperties} for more information.
   * @throws DsException if there is an error creating the network
   * @throws IllegalArgumentException if there are disparaging property values set.
   */
  public static DsNetwork getNetwork(String name, DsNetworkProperties prop) throws DsException {
    DsNetwork network;
    synchronized (m_fromString) {
      network = get(name);
      if (network == null) {
        if (prop == null) {
          throw new IllegalArgumentException("null DsProperties");
        }
        network = create(name, prop);
        add(network.getName(), network);
      }
    }
    return network;
  }

  /**
   * Create a new network object based upon the Properties given or retrieve the existing network
   * with the given name.
   *
   * @param name the base name of the network. If the named network does not already exist, the
   *     provided name is modified by this method to be unique and a new DsNetwork is created. This
   *     method will assign a number to the network which will be appended to the provided name
   *     (e.g. mynetwork becomes "mynetwork-n" where n is the unique across all instances of this
   *     class.) The network instance created by this method may be referenced in the future by
   *     using the name provided by DsNetwork.getName() on the returned network object.
   * @param prop The properties used to set the new network objects capabilities. See {@link
   *     DsNetworkProperties} for more information.
   * @throws DsException if there is an error creating the network
   * @throws IllegalArgumentException if there are disparaging property values set.
   */
  public static DsNetwork getNetwork(DsByteString name, DsNetworkProperties prop)
      throws DsException {
    DsNetwork network;
    synchronized (m_fromByteString) {
      network = get(name);
      if (network == null) {
        if (prop == null) {
          throw new IllegalArgumentException("null DsProperties");
        }
        network = create(name, prop);
        add(network.getName(), network);
      }
    }
    return network;
  }

  /**
   * Retrieve the existing network with the given name. The method will return <code>null</code> if
   * the named network does not exist.
   *
   * @param name the name of the network to retrieve.
   * @return network associated with the given parameter name or null if the network object does not
   *     exist.
   */
  public static DsNetwork findNetwork(String name) {
    DsNetwork network;
    synchronized (m_fromString) {
      network = get(name);
    }
    return network;
  }

  /**
   * Remove the existing network with the given String name. The method will return <code>null
   * </code> if the named network does not exist.
   *
   * <p>The method will throw a IllegalArgumentException if the name is null.
   *
   * @param name the name of the network to remove.
   * @return previous network associated with the given parameter name or null if the network object
   *     does not exist.
   * @throws NullPointerException if the network name is null.
   */
  public static DsNetwork removeNetwork(String name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    DsNetwork network;
    synchronized (lockObj) {
      network = (DsNetwork) m_fromString.remove(name);
      m_fromByte[pos(network.getNumber())] = null;
      m_fromByteString.remove(new DsByteString(name));
    }
    return network;
  }

  /**
   * Remove the existing network with the given DsByteString name. The method will return <code>null
   * </code> if the named network does not exist.
   *
   * <p>The method will throw a IllegalArgumentException if the name is null.
   *
   * @param name the name of the network to remove.
   * @return previous network associated with the given parameter name or null if the network object
   *     does not exist.
   * @throws NullPointerException if the network name is null.
   */
  public static DsNetwork removeNetwork(DsByteString name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    DsNetwork network;
    synchronized (lockObj) {
      network = (DsNetwork) m_fromByteString.remove(name);
      m_fromByte[pos(network.getNumber())] = null;
      m_fromString.remove(name.toString());
    }
    return network;
  }

  /**
   * Retrieve the existing network with the given name. The method will return null if the named
   * network does not exist.
   *
   * @param name the name of the network to retrieve.
   * @return network associated with the given parameter name or null if the network object does not
   *     exist.
   */
  public static DsNetwork findNetwork(DsByteString name) {
    DsNetwork network;
    synchronized (m_fromByteString) {
      network = get(name);
    }
    return network;
  }

  /**
   * Find a DsNetwork by its String name.
   *
   * @param name the network name.
   * @return the instance of the network for the supplied name
   * @see #getName
   */
  protected static DsNetwork get(String name) {
    DsNetwork network = (DsNetwork) m_fromString.get(name);
    return network;
  }

  /**
   * Find a DsNetwork by its Byte String name.
   *
   * @param name the network name.
   * @return the instance of the network for the supplied name
   * @see #getName
   */
  protected static DsNetwork get(DsByteString name) {
    DsNetwork network = (DsNetwork) m_fromByteString.get(name);
    return network;
  }

  /**
   * Add a new DsNetwork to the global static map.
   *
   * @param name the name of the network.
   * @param network the network to add.
   */
  protected static void add(String name, DsNetwork network) {
    m_fromByte[pos(network.getNumber())] = network;
    m_fromString.put(name, network);
    m_fromByteString.put(name, network);
  }

  /**
   * Add a new DsNetwork to the global static map.
   *
   * @param name the name of the network.
   * @param network the network to add.
   */
  protected static void add(DsByteString name, DsNetwork network) {
    m_fromByte[pos(network.getNumber())] = network;
    m_fromString.put(name.toString(), network);
    m_fromByteString.put(name, network);
  }

  /**
   * Create a new DsNetwork by String.
   *
   * @param name the name of the network to create.
   * @param prop the properties that determine the network's behavior
   * @return the created network
   * @throws DsException if the maximum number of logical networks (256) is exceeded.
   *     IllegalArgumentException if invalid configuration.
   */
  protected static DsNetwork create(String name, DsNetworkProperties prop) throws DsException {
    DsNetwork network = new DsNetwork(name);
    network.updateProperties(prop);
    return network;
  }

  /**
   * Create a new DsNetwork by Byte String.
   *
   * @param name the name of the network to create.
   * @param prop the properties that determine the network's behavior
   * @return the created network
   * @throws DsException if the maximum number of logical networks (256) is exceeded.
   *     IllegalArgumentException if invalid configuration.
   */
  protected static DsNetwork create(DsByteString name, DsNetworkProperties prop)
      throws DsException {
    DsNetwork network = new DsNetwork(name);
    network.updateProperties(prop);
    return network;
  }

  /**
   * Return the default network. This network is used if none is associated with a SIP message or
   * provided to a transport layer method.
   *
   * @return the default network.
   */
  public static DsNetwork getDefault() {
    return DEFAULT;
  }

  ////// protected network constructor //////////////////////////////
  /**
   * Create a new DsNetwork by String.
   *
   * @param name the name of the network to create.
   * @throws DsException if the maximum number of logical networks (256) is exceeded.
   */
  protected DsNetwork(String name) throws DsException {
    synchronized (m_counterLock) {
      if (m_next == MAX_NETWORKS) {
        throw new DsException("MAX_NETWORKS(" + MAX_NETWORKS + ") exceeded");
      }

      m_number = (byte) m_next++;
    }
    m_basename = name;
    m_name = name;
    m_dispname = m_name;
    m_nameBs = m_dispnameBs = new DsByteString(name);
    m_sipTimers = new DsSipTimers(m_name);
  }

  /**
   * Create a new DsNetwork by Byte String.
   *
   * @param name the name of the network to create.
   * @throws DsException if the maximum number of logical networks (256) is exceeded.
   */
  protected DsNetwork(DsByteString name) throws DsException {
    synchronized (m_counterLock) {
      if (m_next == MAX_NETWORKS) {
        throw new DsException("MAX_NETWORKS(" + MAX_NETWORKS + ") exceeded");
      }

      m_number = (byte) m_next++;
    }

    m_nameBs = m_dispnameBs = name;

    m_basename = name.toString();
    m_name = m_basename;
    m_dispname = m_name;

    m_sipTimers = new DsSipTimers(m_name);
  }

  ////// public object level methods///////////////////////////////////////////////////////////

  /**
   * Gets a property object based on the current state of the network object.
   *
   * @return the current properties of the network
   */
  public DsNetworkProperties getProperties() {
    DsNetworkProperties prop = new DsNetworkProperties();

    // Copy all the data elements
    System.arraycopy(m_sendBufferSizes, 0, prop.m_sendBufferSizes, 0, m_sendBufferSizes.length);
    System.arraycopy(
        m_receiveBufferSizes, 0, prop.m_receiveBufferSizes, 0, m_receiveBufferSizes.length);
    prop.m_tcpNoDelay = m_tcpNoDelay;
    prop.m_MTU = m_MTU;
    prop.m_datagramConnectionStrategy = m_datagramConnectionStrategy;
    prop.m_compressionType = m_compressionType;
    prop.m_maxTcpMessageSize = m_maxTcpMessageSize;
    prop.m_maxUdpPacketSize = m_maxUdpPacketSize;
    prop.m_outConnection = m_outConnection;
    prop.m_maxQueueSize = m_maxQueueSize;
    prop.m_datagramType = m_datagramType;
    prop.m_addClientSideRPort = m_addClientSideRPort;
    prop.m_defaultInviteExpiration = m_defaultInviteExpiration;
    prop.m_simpleResolver = m_simpleResolver;
    prop.m_NAPTREnabled = m_NAPTREnabled;
    prop.m_100relSupport = m_100relSupport;
    prop.m_SSLContext = m_SSLContext;
    prop.m_soTimeout = m_soTimeout;
    prop.m_tcpConnectionTimeout = m_tcpConnectionTimeout;
    prop.m_tlsHandshakeTimeout = m_tlsHandshakeTimeout;
    prop.m_dnsLookupTLSLyncFederationEnabled = m_dnsLookupTLSLyncFederationEnabled;
    prop.convertDestinationRouteToIP = convertDestinationRouteToIP;
    prop.removeOwnRouteHeader = removeOwnRouteHeader;
    prop.peerCertInfoHeader = peerCertInfoHeader;
    prop.certServiceTrustManagerEnabled = certServiceTrustManagerEnabled;

    return prop;
  }

  /**
   * Set the properties of a network object.
   *
   * @param prop The properties used to set the new network objects capabilities. See {@link
   *     DsNetworkProperties} for more information.
   * @throws DsException if there is an error creating the network
   * @throws IllegalArgumentException if there are disparaging property values set.
   */
  public void updateProperties(DsNetworkProperties prop) {
    StringBuffer error = new StringBuffer();
    if (!prop.validate(error)) {
      throw new IllegalArgumentException(error.toString());
    }

    for (int proto = 0; proto < DsSipTransportType.ARRAY_SIZE; proto++) {
      setSendBufferSize(proto, prop.m_sendBufferSizes[proto]);
      setReceiveBufferSize(proto, prop.m_receiveBufferSizes[proto]);
    }

    setTcpNoDelay(prop.m_tcpNoDelay);
    setMTU(prop.m_MTU);
    setMaxUdpPacketSize(prop.m_maxUdpPacketSize);
    setMaxTcpMsgSize(prop.m_maxTcpMessageSize);
    setMaxOutputQueueSize(prop.m_maxQueueSize);
    setOutgoingConnection(prop.m_outConnection);
    setDefaultInviteExpiration(prop.m_defaultInviteExpiration);
    setSimpleResolver(prop.m_simpleResolver);
    setNAPTREnabled(prop.m_NAPTREnabled);
    set100relSupport(prop.m_100relSupport);
    setSSLContext(prop.m_SSLContext);
    setSoTimeout(prop.m_soTimeout);
    setTcpConnectionTimeout(prop.m_tcpConnectionTimeout);
    setTlsHandshakeTimeout(prop.m_tlsHandshakeTimeout);
    setDnsLookupTLSLyncFederationEnabled(prop.m_dnsLookupTLSLyncFederationEnabled);
    setConvertDestinationRouteToIP(prop.convertDestinationRouteToIP);
    setRemoveOwnRouteHeader(prop.removeOwnRouteHeader);
    setPeerCertInfoHeader(prop.peerCertInfoHeader);
    setCertServiceTrustManagerEnabled(prop.certServiceTrustManagerEnabled);

    m_compressionType = prop.m_compressionType;
    m_datagramConnectionStrategy = prop.m_datagramConnectionStrategy;
    m_datagramType = prop.m_datagramType;

    setAddClientSideRPort(prop.m_addClientSideRPort);
  }

  /**
   * Returns if rport will be added to via headers for NAT'd networks.
   *
   * @return <code>true</code> if rport will be added to via headers for NAT'd networks
   */
  public boolean getAddClientSideRPort() {
    return m_addClientSideRPort;
  }

  /** Sets if rport will be added to via headers for NAT'd networks. */
  public void setAddClientSideRPort(boolean rPortFlag) {
    m_addClientSideRPort = rPortFlag;
  }

  /**
   * Return the compression type for the network.
   *
   * @return the compression type performed on the data sent over this network.
   */
  public int getCompressionType() {
    return m_compressionType;
  }

  /**
   * Return the datagram connection strategy for the network.
   *
   * @return the datagram connection strategy for this network.
   */
  public int getDatagramConnectionStrategy() {
    return m_datagramConnectionStrategy;
  }

  /**
   * Return the datagram socket type for the network.
   *
   * @return the type of datagram socket used over this network.
   */
  public int getDatagramType() {
    return m_datagramType;
  }

  /**
   * Reset this DsNetwork to use default values. These are based upon the default values as
   * specified in the {@link DsNetworkProperties} class for default, icmp, NAT, and SIGCOMP
   * networks.
   */
  public void setDefaults() {
    DsNetworkProperties prop;

    if (m_datagramType == DGRAM_ICMP) {
      prop = DsNetworkProperties.getDefault(DsNetworkProperties.DEF_ICMP_NETWORK);
    } else if (m_compressionType == NET_COMP_SIGCOMP) {
      prop = DsNetworkProperties.getDefault(DsNetworkProperties.DEF_SIGCOMP_NETWORK);
    } else if (m_datagramConnectionStrategy == DGRAM_USING_LISTEN_PORTS) {
      prop = DsNetworkProperties.getDefault(DsNetworkProperties.DEF_NAT_NETWORK);
    } else {
      prop = DsNetworkProperties.getDefault(DsNetworkProperties.DEF_NETWORK);
    }

    updateProperties(prop);
  }

  /**
   * Returns if this network is behind a NAT. Currently this is a comparison of the datagram
   * connection strategy.
   *
   * @return <code>true</code> if this network has been set to use listening ports as sending ports
   *     for UDP packets i.e. the datagram connection strategy is set to {@link
   *     DsNetwork#DGRAM_USING_LISTEN_PORTS}.
   */
  public boolean isBehindNAT() {
    return m_datagramConnectionStrategy == DGRAM_USING_LISTEN_PORTS;
  }

  /** Make a copy, but increment the network number. Keep the same base name. */
  public Object clone() throws CloneNotSupportedException {
    if (cat.isEnabled(Level.DEBUG)) {
      DsException e = new DsException("Someone called DsNetwork.clone()");
      cat.debug("DsNetwork.clone() was called from: ", e);
    }

    DsNetwork clone = null;
    synchronized (m_counterLock) {
      if (m_next == MAX_NETWORKS)
        throw new CloneNotSupportedException("MAX_NETWORKS(" + MAX_NETWORKS + ") exceeded");

      try {
        clone = (DsNetwork) super.clone();
      } catch (CloneNotSupportedException cne) {
        // We know the clone is supported :)
      }

      clone.m_number = (byte) m_next++;
    }
    clone.m_name = m_basename + "-" + clone.m_number;
    clone.m_dispname = m_name;

    clone.m_nameBs = new DsByteString(clone.m_name);
    clone.m_dispnameBs = new DsByteString(clone.m_dispname);

    System.arraycopy(m_sendBufferSizes, 0, clone.m_sendBufferSizes, 0, m_sendBufferSizes.length);
    System.arraycopy(
        m_receiveBufferSizes, 0, clone.m_receiveBufferSizes, 0, m_receiveBufferSizes.length);

    add(clone.getName(), clone);
    return clone;
  }

  /**
   * Get the name of the network used in debug output.
   *
   * @return the name of the network used in debug output.
   */
  public DsByteString getDisplayNameBs() {
    return m_dispnameBs;
  }

  /**
   * Set the name of the network used in debug output.
   *
   * @param dispname the name of the network used in debug output.
   * @throws IllegalArgumentException if <code>dispname</code> is <code>null</code>
   */
  public void setDisplayName(DsByteString dispname) {
    if (dispname == null) {
      throw new IllegalArgumentException("display name cannot be null");
    }
    m_dispnameBs = dispname;
    m_dispname = dispname.toString();
  }

  /**
   * Get the name of the network used in debug output.
   *
   * @return the name of the network used in debug output.
   */
  public String getDisplayName() {
    return m_dispname;
  }

  /**
   * Set the name of the network used in debug output.
   *
   * @param dispname the name of the network used in debug output.
   * @throws IllegalArgumentException if <code>dispname</code> is <code>null</code>
   */
  public void setDisplayName(String dispname) {
    if (dispname == null) {
      throw new IllegalArgumentException("display name cannot be null");
    }
    m_dispname = dispname;
  }

  /**
   * Get the unique name.
   *
   * @return the unique name.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Get the unique name.
   *
   * @return the unique name.
   */
  public DsByteString getNameBs() {
    return m_nameBs;
  }

  /**
   * Get the network number.
   *
   * @return the network number.
   */
  public byte getNumber() {
    return m_number;
  }

  /**
   * Get the name of the network used in debug output.
   *
   * @return the name of the network used in debug output.
   */
  public String toString() {
    return m_dispname;
  }

  /**
   * Get the name of the network used in debug output.
   *
   * @return the name of the network used in debug output.
   */
  public DsByteString toByteString() {
    return m_dispnameBs;
  }

  /**
   * Get the network number.
   *
   * @return the network number.
   */
  public int hashCode() {
    return m_number;
  }

  /**
   * Return <code>true</code> if the networks have the same number.
   *
   * @param other the network to compare to
   * @return <code>true</code> if the networks have the same number.
   */
  @Override
  public boolean equals(Object other) {
    DsNetwork network = (DsNetwork) other;
    return network.m_number == m_number;
  }

  /**
   * Return the SIP timers used for this network.
   *
   * @return the SIP timers used for this network
   */
  public DsSipTimers getSipTimers() {
    return m_sipTimers;
  }

  /**
   * Set to <code>true</code> if TCP_NODELAY should be enabled on sockets created using this
   * network.
   *
   * @param nodel set to <code>true</code> if TCP_NODELAY should be enabled on sockets created using
   *     this network.
   */
  public void setTcpNoDelay(boolean nodel) {
    m_tcpNoDelay = nodel;
  }

  /**
   * Return <code>true</code> if TCP_NODELAY will be enabled on sockets created using this network.
   *
   * @return <code>true</code> if TCP_NODELAY will be enabled on sockets created using this network.
   */
  public boolean getTcpNoDelay() {
    return m_tcpNoDelay;
  }

  /**
   * Set the send buffer size for a given protocol.
   *
   * @param proto the protocol as defined in {@link DsSipTransportType}.
   * @param size the size
   * @throws IllegalArgumentException if the provided protocol is not valid.
   */
  public void setSendBufferSize(int proto, int size) {
    checkProto(proto);
    m_sendBufferSizes[proto] = size;
  }

  /**
   * Set the receive buffer size for a given protocol.
   *
   * @param proto the protocol as defined in {@link DsSipTransportType}.
   * @param size the size
   * @throws IllegalArgumentException if the provided protocol is not valid.
   */
  public void setReceiveBufferSize(int proto, int size) {
    checkProto(proto);
    m_receiveBufferSizes[proto] = size;
  }

  /**
   * Get the send buffer size for a given protocol.
   *
   * @param proto the protocol as defined in {@link DsSipTransportType}.
   * @return the send buffer size for a given protocol.
   * @throws IllegalArgumentException if the provided protocol is not valid.
   */
  public int getSendBufferSize(int proto) {
    checkProto(proto);
    return m_sendBufferSizes[proto];
  }

  /**
   * Get the received buffer size for a given protocol.
   *
   * @param proto the protocol as defined in {@link DsSipTransportType}.
   * @return the receive buffer size for a given protocol.
   * @throws IllegalArgumentException if the provided protocol is not valid.
   */
  public int getReceiveBufferSize(int proto) {
    checkProto(proto);
    return m_receiveBufferSizes[proto];
  }

  /**
   * Configure the path maximum transmission unit (MTU). This parameter is used to decide whether a
   * request may be sent using UDP.
   *
   * @param mtu the known path MTU.
   */
  public void setMTU(int mtu) {
    m_MTU = mtu;
  }

  /**
   * Get the configured path maximum transmission unit (MTU). This parameter is used to decide
   * whether a request may be sent using UDP.
   *
   * @return the configured path maximum transmission unit (MTU).
   */
  public int getMTU() {
    return m_MTU;
  }

  /**
   * Set the maximum expected UDP message size for this network.
   *
   * @param size the maximum expected UDP message size for this network.
   */
  public void setMaxUdpPacketSize(int size) {
    m_maxUdpPacketSize = size;
  }

  //  maivu - 11.01.06 - CSCsg22401 Getter and Setter for default expiration of INVITE and
  // non-INVITE requests
  /**
   * Retrieve the DEFAULT INVITE request expiration.
   *
   * @return the default invitation expiration
   */
  public int getDefaultInviteExpiration() {
    return m_defaultInviteExpiration;
  }

  /**
   * Set the DEFAULT INVITE expiration. This timeout value will be used ONLY in the absence of the
   * Expires header in the INVITE.
   *
   * <p>The application is responsible to set this expiration value. If it is not set, the default
   * value will be 64s.
   *
   * @param expiration the default expiration value
   */
  public void setDefaultInviteExpiration(int expiration) {
    m_defaultInviteExpiration = expiration;
  }

  /**
   * Retrieve the SO_TIMEOUT, specified in milliseconds.
   *
   * @return the SO_TIMEOUT
   */
  public int getSoTimeout() {
    return m_soTimeout;
  }

  /**
   * Set the SO_TIMEOUT.
   *
   * @param timeout the specified timeout, in milliseconds
   */
  public void setSoTimeout(int timeout) {
    m_soTimeout = timeout;
  }

  /**
   * Retrieve the TCP connection setup timeout, specified in milliseconds.
   *
   * @return the TCP Connection timeout, in milliseconds
   */
  public int getTcpConnectionTimeout() {
    return m_tcpConnectionTimeout;
  }

  /**
   * Set the TCP connection setup timeout
   *
   * @param timeout TCP connection timeout in milliseconds
   */
  public void setTcpConnectionTimeout(int timeout) {
    m_tcpConnectionTimeout = timeout;
  }

  /**
   * Retrieve the TCP connection setup timeout, specified in milliseconds.
   *
   * @return the TCP Connection timeout, in milliseconds
   */
  public int getTlsHandshakeTimeout() {
    return m_tlsHandshakeTimeout;
  }

  /**
   * Set the TCP connection setup timeout
   *
   * @param timeout TCP connection timeout in milliseconds
   */
  public void setTlsHandshakeTimeout(int timeout) {
    m_tlsHandshakeTimeout = timeout;
  }

  /**
   * Retrieve the Dns lookup Lync Federation flag.
   *
   * @return the Dns lookup Lync Federation flag
   */
  public boolean getDnsLookupTLSLyncFederationEnabled() {
    return m_dnsLookupTLSLyncFederationEnabled;
  }

  public boolean getPeerCertInfoHeader() {
    return peerCertInfoHeader;
  }

  /**
   * Set the the Dns lookup Lync Federation flag.
   *
   * @param dnsLookupTLSLyncFederationEnabled Dns lookup Lync Federation flag
   */
  public void setDnsLookupTLSLyncFederationEnabled(boolean dnsLookupTLSLyncFederationEnabled) {
    m_dnsLookupTLSLyncFederationEnabled = dnsLookupTLSLyncFederationEnabled;
  }

  /**
   * Sets the enablePeerCertInfoHeader flag , which controls Insertion of TLS cert info to initial
   * Invite header
   *
   * @param enablePeerCertInfoHeader
   */
  public void setPeerCertInfoHeader(Boolean enablePeerCertInfoHeader) {
    peerCertInfoHeader = enablePeerCertInfoHeader;
  }

  /**
   * Sets the enableCertServiceTrustManager flag , which controls which cert Service Trust Manager
   * Enable
   */
  public void setCertServiceTrustManagerEnabled(Boolean certServiceTrustManagerEnabled) {
    this.certServiceTrustManagerEnabled = certServiceTrustManagerEnabled;
  }

  public boolean isCertServiceTrustManagerEnabled() {
    return this.certServiceTrustManagerEnabled;
  }

  /**
   * Retrieve the Simple Resolver flag.
   *
   * @return the Simple Resolver flag
   */
  public boolean getSimpleResolver() {
    return m_simpleResolver;
  }

  /**
   * Set the Simple Resolver flag.
   *
   * @param simpleResolver the new value of the Simple Resolver flag for this network
   */
  public void setSimpleResolver(boolean simpleResolver) {
    m_simpleResolver = simpleResolver;
  }

  /**
   * Retrieve the NAPTR Enabled setting.
   *
   * @return <code>true</code> if NAPTR is enabled
   */
  public boolean isNAPTREnabled() {
    return m_NAPTREnabled;
  }

  /**
   * Set the NAPTR Enabled flag.
   *
   * @param NAPTREnabled the new value of the NAPTR Enabled flag for this network
   */
  public void setNAPTREnabled(boolean NAPTREnabled) {
    m_NAPTREnabled = NAPTREnabled;
  }

  /**
   * Retrieve the DsSSLContext.
   *
   * @return the DsSSLContext
   */
  public DsSSLContext getSSLContext() {
    return m_SSLContext;
  }

  /**
   * Set the DsSSLContext.
   *
   * @param context the DsSSLContext
   */
  public void setSSLContext(DsSSLContext context) {
    m_SSLContext = context;
  }

  // CAFFEINE 2.0 DEVELOPMENT - PRACK required
  /**
   * Set the 100rel support level to one of "Require" (REQUIRE), "Supported" (SUPPORTED), or
   * "Unsupported" (UNSUPPORTED).
   *
   * @param attribute pass in one of the above attributes; if null is passed, the default is in
   *     effect.
   */
  public void set100relSupport(byte attribute) {
    if (attribute == DsSipConstants.REQUIRE
        || attribute == DsSipConstants.SUPPORTED
        || attribute == DsSipConstants.UNSUPPORTED) {
      m_100relSupport = attribute;
    } else {
      if (cat.isEnabled(Level.WARN))
        cat.log(Level.WARN, "set100relSupport() Failed with invalid attribute: " + attribute);
    }
  }

  /**
   * Use get100relSupport() to get the value of 100rel support level.
   *
   * @return The value of the current 100rel support level is returned.
   */
  public byte get100relSupport() {
    return m_100relSupport;
  }

  /**
   * Set the maximum expected TCP message size for this network.
   *
   * @param size the maximum expected TCP message size for this network.
   */
  public void setMaxTcpMsgSize(int size) {
    m_maxTcpMessageSize = size;
  }

  /**
   * Get the maximum expected TCP message size for this network.
   *
   * @return the maximum expected TCP message size for this network.
   */
  public int getMaxTcpMsgSize() {
    return m_maxTcpMessageSize;
  }

  /**
   * Get that maximum number of SIP messages that queue up on an output connection before the
   * connection is closed.
   *
   * @return the maximum queue size
   */
  public int getMaxOutputQueueSize() {
    return m_maxQueueSize;
  }

  /**
   * Set that maximum number of SIP messages that queue up on an output connection before the
   * connection is closed. For high speed Server to Server communications over TCP/TLS, this number
   * should be large, on the order of thousands or higher. For Server to Client fan-out, this number
   * should be small, on the order of tens.
   *
   * @param size the maximum queue size
   */
  public void setMaxOutputQueueSize(int size) {
    m_maxQueueSize = size;
  }

  /**
   * Get the maximum expected UDP message size for this network.
   *
   * @return the maximum expected UDP message size for this network.
   */
  public int getMaxUdpPacketSize() {
    return m_maxUdpPacketSize;
  }

  /**
   * Tells the Tokenized SIP property for this network. Returns <code>true</code> if the SIP
   * Messages sent through this network interface will be serialized in the Tokenized SIP format.
   *
   * @return <code>true</code> if the SIP Messages that are sent through this network interface will
   *     be serialized in the Tokenized SIP format, otherwise <code>false</code>.
   */
  public boolean isTSIPEnabled() {
    return getCompressionType() == NET_COMP_TOKEN_SIP;
  }

  /**
   * Returns a debug String.
   *
   * @return a debug String.
   */
  public String dump() {
    // Dump the network's default values.

    return new StringBuffer(512)
        .append("Logical Network ")
        .append(m_name)
        .append(" (")
        .append(m_dispname)
        .append(") settings\n-----------\n")
        .append(DESC_NODELAY)
        .append(" (def: ")
        .append(DEFAULT_NODELAY)
        .append(") == ")
        .append(m_tcpNoDelay)
        .append("\n")
        .append(DESC_TCP_SENDBUFSIZE)
        .append(" (def: ")
        .append(DEFAULT_TCP_SENDBUFSIZE)
        .append(") == ")
        .append(m_sendBufferSizes[DsSipTransportType.TCP])
        .append("\n")
        .append(DESC_TCP_RECBUFSIZE)
        .append(" (def: ")
        .append(DEFAULT_TCP_RECBUFSIZE)
        .append(") == ")
        .append(m_receiveBufferSizes[DsSipTransportType.TCP])
        .append("\n")
        .append(DESC_TLS_SENDBUFSIZE)
        .append(" (def: ")
        .append(DEFAULT_TLS_SENDBUFSIZE)
        .append(") == ")
        .append(m_sendBufferSizes[DsSipTransportType.TLS])
        .append("\n")
        .append(DESC_TLS_RECBUFSIZE)
        .append(" (def: ")
        .append(DEFAULT_TLS_RECBUFSIZE)
        .append(") == ")
        .append(m_receiveBufferSizes[DsSipTransportType.TLS])
        .append("\n")
        .append(DESC_UDP_SENDBUFSIZE)
        .append(" (def: ")
        .append(DEFAULT_UDP_SENDBUFSIZE)
        .append(") == ")
        .append(m_sendBufferSizes[DsSipTransportType.UDP])
        .append("\n")
        .append(DESC_UDP_RECBUFSIZE)
        .append(" (def: ")
        .append(DEFAULT_UDP_RECBUFSIZE)
        .append(") == ")
        .append(m_receiveBufferSizes[DsSipTransportType.UDP])
        .append("\n")
        .append(DESC_MTU)
        .append(" (def: ")
        .append(DEFAULT_MTU)
        .append(") == ")
        .append(m_MTU)
        .append("\n")
        .append(DESC_DGRAM_CONNECTION_STRATEGY)
        .append(" (def: ")
        .append(DEFAULT_DGRAM_CONNECTION_STRATEGY)
        .append(") == ")
        .append(getDatagramConnectionStrategy())
        .append(", ")
        .append(m_datagramConnectionStrategyDefs[getDatagramConnectionStrategy()])
        .append("\n")
        .append(DESC_DGRAM_TYPE)
        .append(" (def: ")
        .append(DEFAULT_DGRAM_TYPE)
        .append(") == ")
        .append(getDatagramType())
        .append(", ")
        .append(m_datagramTypeDefs[getDatagramType()])
        .append("\n")
        .append(DESC_NETWORK_COMP_TYPE)
        .append(" (def: ")
        .append(DEFAULT_NETWORK_COMP_TYPE)
        .append(") == ")
        .append(getCompressionType())
        .append(", ")
        .append(m_networkCompressionTypeDefs[getCompressionType()])
        .append("\n")
        .append(DESC_MAX_TCP_MSG_SIZE)
        .append(" (def: ")
        .append(DEFAULT_MAX_TCP_MSG_SIZE)
        .append(") == ")
        .append(m_maxTcpMessageSize)
        .append("\n")
        .append(DESC_MAX_UDP_PACKET_SIZE)
        .append(" (def: ")
        .append(DEFAULT_MAX_UDP_PACKET_SIZE)
        .append(") == ")
        .append(m_maxUdpPacketSize)
        .append("\n")
        .append(DESC_OUT_CONNECTION)
        .append(" (def: ")
        .append(DEFAULT_OUT_CONNECTION)
        .append(") == ")
        .append(m_outConnection)
        .append("\n")
        .append(DESC_MAX_QUEUE_LENGTH)
        .append(" (def: ")
        .append(DEFAULT_MAX_QUEUE_LENGTH)
        .append(") == ")
        .append(m_maxQueueSize)
        .append("\n")
        .append(DESC_DEFAULT_INVITE_EXPIRATION)
        .append(" (def: ")
        .append(DEFAULT_INVITE_EXPIRATION)
        .append(") == ")
        .append(m_defaultInviteExpiration)
        .append("\n")
        .append(DESC_100REL_SUPPORT)
        .append(" (def: ")
        .append(DEFAULT_100REL_SUPPORT)
        .append(") == ")
        .append(m_100relSupport)
        .append("\n")
        .append(DESC_SIMPLE_RESOLVER)
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_SIMPLE_RESOLVER)
        .append(") == ")
        .append(m_simpleResolver)
        .append("\n")
        .append(DESC_NAPTR_ENABLED)
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_NAPTR_ENABLED)
        .append(") == ")
        .append(m_NAPTREnabled)
        .append("\n")
        .append(DESC_SSL_CONTEXT)
        .append(" (def: null) == ")
        .append((m_SSLContext == null) ? "null" : "set")
        .append("\n")
        .append(DESC_SO_TIMEOUT)
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_SO_TIMEOUT)
        .append(") == ")
        .append(m_soTimeout)
        .append("\n")
        .append(DESC_TCP_CONN_TIMEOUT)
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_TCP_CONN_TIMEOUT)
        .append(") == ")
        .append(m_tcpConnectionTimeout)
        .append("\n")
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_TLS_HANDSHAKE_TIMEOUT)
        .append(") == ")
        .append(m_tlsHandshakeTimeout)
        .append("\n")
        .append(DESC_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED)
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED)
        .append(") == ")
        .append(m_dnsLookupTLSLyncFederationEnabled)
        .append("\n")
        .append(DESC_PEER_CERT_INFO_HEADER_ENABLED)
        .append(" (def: ")
        .append(DsNetworkProperties.DEFAULT_PEER_CERT_INFO_HEADER_ENABLED)
        .append(") == ")
        .append(peerCertInfoHeader)
        .append("\n")
        .toString();
  }

  /**
   * Throws IllegalArgumentException if the provided protocol is not valid.
   *
   * @param proto the protocol as defined in {@link DsSipTransportType}.
   * @throws IllegalArgumentException if the provided protocol is not valid.
   */
  private void checkProto(int proto) {
    if ((proto < 0) || (proto >= DsSipTransportType.ARRAY_SIZE)) {
      throw new IllegalArgumentException(
          "Specified protocol: " + proto + " is not recognized as a DsSipTransportType constant");
    }
  }

  /**
   * Sets the flag that tells whether this network allows the creation of outgoing connections. By
   * default, this flag is <code>true</code> and would mean that the outgoing connection creation is
   * allowed.
   *
   * @param enable if <code>true</code>, then outgoing connections can be created through this
   *     network, otherwise outgoing connections would not be allowed through this network.
   */
  public void setOutgoingConnection(boolean enable) {
    m_outConnection = enable;
  }

  /**
   * Tells whether this network allows the creation of outgoing connections. By default, this flag
   * is <code>true</code> and would mean that the outgoing connection creation is allowed.
   *
   * @return <code>true</code> if the outgoing connections can be created through this network,
   *     <code>false</code> otherwise.
   */
  public boolean isOutgoingConnection() {
    return m_outConnection;
  }

  /**
   * Converts a byte to a positive short for indexing arrays.
   *
   * @param b the byte to convert
   * @return the byte represented as a positive short
   */
  private static short pos(byte b) {
    return (short) (b & 0xff);
  }

  /** below methods will be called by Node API to set threshold and TLS buffer size* */
  public void setThreshold(int size) {
    this.m_threshold = (int) (size * 0.01 * this.m_maxBuffer);
    cat.log(Level.INFO, "setting threshold to  " + this.m_threshold);
  }

  public int getThreshold() {
    return this.m_threshold;
  }

  public void setMaxbuffer(int size) {
    this.m_maxBuffer = size;
  }

  public int getMaxbuffer() {
    return this.m_maxBuffer;
  }

  public boolean isConvertDestinationRouteToIP() {
    return convertDestinationRouteToIP;
  }

  public void setConvertDestinationRouteToIP(boolean convertDestinationRouteToIP) {

    this.convertDestinationRouteToIP = convertDestinationRouteToIP;
  }

  public boolean isRemoveOwnRouteHeader() {
    return this.removeOwnRouteHeader;
  }

  public void setRemoveOwnRouteHeader(boolean removeOwnRouteHeader) {
    this.removeOwnRouteHeader = removeOwnRouteHeader;
  }

  public void setenv(Environment env) {
    this.env = env;
  }
}
