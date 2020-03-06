// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTrackingException.TrackingExceptions;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.*;
import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import org.apache.logging.log4j.Level;

/**
 * Implements SIP server location per the spec. If an maddr is present, it is treated as though it
 * was in the host position of the URL -- that is to say, SRV lookups are done if a port is not
 * specified.
 */
public class DsSipServerLocator implements DsSipResolver, Serializable {
  // /////////////////////////////////////////////////
  // ///// static data ///////////////////////////////
  // /////////////////////////////////////////////////
  /** Tells whether the debugging option is enabled or not. */
  public static final boolean m_debugMode;

  /** The local domain name, may be null. */
  private static String MY_DOMAIN;

  /** Flag to force InetAddress based lookups for A Records. */
  private static boolean m_useInetLookupForARecords = false;

  // the cases this class handles
  //     (maddr not shown but handles this too)

  /* sip:bob@dynamicsoft.com */
  private static final short NOIP_NOPROTO_NOPORT = 0;

  /* sip:bob@dynamicsoft.com:5070 */
  private static final short NOIP_NOPROTO_PORT = 1;

  /* sip:bob@dynamicsoft.com;transport=tcp */
  private static final short NOIP_PROTO_NOPORT = 2;

  /* sip:bob@dynamicsoft.com:5070;transport=tcp */
  private static final short NOIP_PROTO_PORT = 3;

  /* sip:bob@128.29.222.100 */
  private static final short IP_NOPROTO_NOPORT = 4;

  /* sip:bob@128.29.222.100:5070 */
  private static final short IP_NOPROTO_PORT = 5;

  /* sip:bob@128.29.222.100:5070;transport=tcp */
  private static final short IP_PROTO_NOPORT = 6;

  /* sip:bob@128.29.222.100:5070;transport=tcp */
  private static final short IP_PROTO_PORT = 7;

  private static final String[] SRV_ATTR_IDS = {"SRV"};
  private static final String[] A_ATTR_IDS = {"A"};
  private static final String[] NAPTR_ATTR_IDS = {"NAPTR"};
  private static final int DEFAULT_PORT = DsSipTransportType.T_UDP.getDefaultPort();
  private static final int TLS_DEFAULT_PORT = DsSipTransportType.T_TLS.getDefaultPort();

  // used for weighted selection algorithm
  private SecureRandom randomGenerator;
  private static boolean m_useSRV = false;
  private boolean initFailed = false;
  private DirContext jndiDNS;

  // Start up the java sip stack if not already started
  /** changes done by kiran for nework flactuation defect */
  static boolean m_useDsUnreachableTable = false;

  private static DsConnectionBarrier m_connectionBarrier;

  // maivu - 05.22.07 - CSCsi98575 - Add Local SRV configuration functionality instead of DNS
  // A mapping table to hold local SRV configurations.
  private static Map m_localSRVRecords;
  // A config knob to enable/disable the use of the local mapping SRV configurations.
  private static boolean m_useLocalSRVLookupOnly;

  private static boolean doAQuery;

  // new variables to store SRV and A record results, used in DNS Server Group creation
  private DsSRVWrapper[] srvResults = null;
  private InetAddress[] aRecordResults = null;

  private Exception failureException;

  public DsSRVWrapper[] getSrvResults() {
    return srvResults;
  }

  public void setSrvResults(DsSRVWrapper[] srvResults) {
    this.srvResults = srvResults;
  }

  public InetAddress[] getARecordResults() {
    return aRecordResults;
  }

  public void setARecordResults(InetAddress[] aRecordResults) {
    this.aRecordResults = aRecordResults;
  }

  static {
    m_connectionBarrier = new DsConnectionBarrier();

    m_debugMode =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DNS_DEBUG, DsConfigManager.PROP_DNS_DEBUG_DEFAULT);

    // maivu - 05.22.07 - CSCsi98575 - Add Local SRV configuration functionality instead of DNS
    // Read the config flag to know whether using the local DNS only
    m_useLocalSRVLookupOnly =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_USE_LOCAL_DNS_ONLY,
            DsConfigManager.PROP_USE_LOCAL_DNS_ONLY_DEFAULT);

    doAQuery =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_DO_A_QUERY_AFTER_SRV,
            DsConfigManager.PROP_DO_A_QUERY_AFTER_SRV_DEFAULT);
    /**
     * changes done by radmohan
     *
     * <p>CSCtz70393 Thread pool exhausted when all elements in srv group is down
     */
    setM_useDsUnreachableTable(
        DsConfigManager.getProperty(
            DsConfigManager.PROP_USE_DSUNREACHABLE_DESTINATION_TABLE,
            DsConfigManager.PROP_USE_DSUNREACHABLE_DESTINATION_TABLE_DEFAULT));

    try {
      String value =
          DsConfigManager.getProperty(
              DsConfigManager.PROP_LOCAL_DOMAIN, DsConfigManager.PROP_LOCAL_DOMAIN_DEFAULT);

      if (value.equalsIgnoreCase("null")) {
        // configured to disable this feature
        MY_DOMAIN = null;
      } else if (value.length() == 0) {
        // configured to get the local domain from the local host
        InetAddress addr = InetAddress.getLocalHost();
        String fqdn = addr.getCanonicalHostName();
        int index = fqdn.indexOf('.');
        if (index == -1) {
          MY_DOMAIN = null;
        } else {
          MY_DOMAIN = fqdn.substring(index);
        }
      } else {
        // user configured value
        MY_DOMAIN = value;
      }
    } catch (Exception e) {
      MY_DOMAIN = null;
    }
  }

  // ////// end static data //////////////////////////

  // /////////////////////////////////////////////////
  // ///// instance data /////////////////////////////
  // /////////////////////////////////////////////////

  /** Contains the list of located SIP servers. */
  private short m_case; // keep track for calls to refresh

  private transient Iterator m_iterator; // internal iterator for calls  to getNextBindingInfo
  private Vector m_bindingInfo; // sorted list of DsBindingInfo
  private DsBindingInfo m_currentBindingInfo;
  private byte m_supportedTransports = DsSipResolverUtils.TRANSPORT_NONE;

  // the original data from the constructor
  InetAddress m_localAddress;
  int m_localPort = DsSipResolverUtils.LPU;
  private String m_host;
  private int m_port = DsSipResolverUtils.RPU;
  private byte m_transport = (byte) DsSipResolverUtils.BTU;

  private boolean m_sizeExceedsMTU = false; // message size exceeds MTU

  // (TODO:   these are BIG objects -- we should implement a cache)
  // ////// end instance data //////////////////////////

  public static void setAQuery(boolean doAQuery) {
    DsSipServerLocator.doAQuery = doAQuery;
  }

  public void setJndiDNSContext(DirContext jndiDNSContext) {
    this.jndiDNS = jndiDNSContext;
  }

  public DirContext getJndiDNSContext() {
    return jndiDNS;
  }
  /**
   * Provides for enabling or disabling the NAPTR lookup. If set to true then the NAPTR lookup is
   * done as per the draft-ietf-sip-srv-06.txt.
   *
   * <p>This NAPTR setting is now settable per network. If you set it here it will overwrite this
   * setting for all existing and future networks. This mimics the previous behavior for backward
   * compatability.
   *
   * @param enable if <code>true</code>, then NAPTR lookup will be enabled, disabled otherwise.
   */
  public static void setUseNAPTR(boolean enable) {
    DsNetwork network;

    for (byte b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
      network = DsNetwork.getNetwork(b);
      if (network != null) {
        network.setNAPTREnabled(enable);

        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(
              Level.DEBUG, "Setting Use NAPTR to " + enable + " for network: " + network);
        }
      }

      // This stops the infinite loop when b wraps
      // Do not delete this even though it looks unncessary.  -stemayer
      if (b == Byte.MAX_VALUE) {
        break;
      }
    }

    DsNetworkProperties.setNAPTREnabledDefault(enable);
  }

  /**
   * Tells whether the NAPTR lookup is enabled.
   *
   * <p>This NAPTR setting is now per network. For backward compatability this method was left here.
   * It returns the NAPTR lookup value from the default network. This mimics the previous behavior
   * for backward compatability as best as possible.
   *
   * @return <code>true</code> if the NAPTR lookup is enabled on the default network
   */
  public static boolean usingNAPTR() {
    return DsNetwork.getDefault().isNAPTREnabled();
  }

  /**
   * Gets the local domain, either from the local host lookup or the configuration.
   *
   * @return the local domain, may be <code>null</code>
   */
  protected static String getLocalDomain() {
    return MY_DOMAIN;
  }

  /*  taggedForRemovalBegin
   *  Commenting as Code is not used  */
  /**
   * Add a server to the list of DNS servers.
   *
   * @param server the new server to add
   * @return <code>false</code> if DNS is active (an SRV query has already been performed).
   *     Otherwise returns <code>true</code>.
   */
  /*
  public static boolean addDNSServer(String server)
  {

      if (server != null)
      {
          for (int i = 0; i < m_DNSServers.length; ++i)
          {
              if (server.equals(m_DNSServers[i])) return true;
          }
          String [] newList = new String[m_DNSServers.length + 1];
          System.arraycopy(m_DNSServers, 0, newList, 0, m_DNSServers.length);
          newList[newList.length -1] = server;
          m_DNSServers = newList;
      }
      return true;
  }
  */
  /**
   * Remove a server from the list of DNS servers.
   *
   * @param server the server to remove
   * @return <code>false</code> if DNS is active (an SRV query has already been performed) or the
   *     server was not in the list. Otherwise returns <code>true</code>.
   */
  /*
  public static boolean removeDNSServer(String server)
  {

      if (server != null)
      {
          boolean found = false;
          int idx = -1;
          for (int i = 0; i < m_DNSServers.length; ++i)
          {
              if (server.equals(m_DNSServers[i]))
              {
                  found = true;
                  idx = i;
                  break;
              }
          }
          if (!found) return false;
          String [] newList = new String[m_DNSServers.length - 1];
          for (int i = 0; i < m_DNSServers.length; ++i)
          {
              if (i < idx)
              {
                  newList[i] = m_DNSServers[i];
              }
              else if (i > idx)
              {
                  newList[i-1] = m_DNSServers[i];
              }
          }
          m_DNSServers = newList;
      }
      return true;
  }

  */
  /**
   * Provide a list of DNS servers to be used by all DsSipServerLocator(s) for SRV record lookup. If
   * a list is not provided, DNS servers will be found in a platform dependent manner. This method
   * can only be called before the first SRV query is performed.
   *
   * @param servers the list of servers
   * @return <code>false</code> if DNS is active (an SRV query has already been performed).
   *     Otherwise reurns <code>true</code>.
   */
  /*
  public static boolean setDNSServers(String[] servers)
  {

      if (servers != null)
      {
          m_DNSServers = new String[servers.length];
          System.arraycopy(servers, 0, m_DNSServers, 0, servers.length);
      }
      return true;
  }

  */
  /**
   * Return the list of DNS servers being used for SRV lookup.
   *
   * @return the list of DNS servers being used for SRV lookup
   */
  /*
  public static String[] getDNSServers()
  {
      String ret[] = new String[m_DNSServers.length];
      System.arraycopy(m_DNSServers, 0, ret, 0, m_DNSServers.length);
      return ret;
  }
  taggedForRemovalEnd
  */

  /**
   * Tell all DsSipServerLocator(s) whether or not to use SRV record lookup. Provides a way to "turn
   * off" the SRV feature of the user agent stack. This can be done at any time.
   *
   * @param useSRV If set to <code>true</code>, SRV will be used. If set to <code>false</code> SRV
   *     record lookup will not be used. Default value is <code>true</code>.
   */
  public static void setUseSRV(boolean useSRV) {
    m_useSRV = useSRV;
  }

  /**
   * Return <code>true</code> if usinging SRV lookup, otherwise returns <code>false</code>.
   *
   * @return <code>true</code> if usinging SRV lookup, otherwise returns <code>false</code>
   */
  public static boolean usingSRV() {
    return m_useSRV;
  }

  // maivu - 05.22.07 - CSCsi98575 - Add Local SRV configuration functionality instead of DNS
  /**
   * Set the mapping table of local SRV configurations.
   *
   * <p><b>Note:</b>
   * <li>The Map must map String to String[]. Passing anything else in will result in undefined
   *     behavior.
   * <li>The new Map will overwrite the current one if there is existing one
   *
   * @param records a mapping table of local SRV configurations.
   * @throws IllegalArgumentException if <code>records</code> is <code>null</code> or <code>empty
   *     </code>.
   */
  public static synchronized void setLocalSRVRecords(Map records) {
    if (records == null) {
      throw new IllegalArgumentException("Invalid (NULL) records.");
    }
    if (records.size() == 0) {
      throw new IllegalArgumentException("Invalid (empty) records.");
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "Set a local mapping SRV records.");
    }

    // always take the new local mapping.
    m_localSRVRecords = records;
    dumpLocalSRVRecords();
  }

  /**
   * Add (append) a new Map of local SRV records into the existing mapping table.
   *
   * <p>Note:
   * <li>The Map must map String to String[]. Passing anything else in will result in undefined
   *     behavior.
   *
   * @param records a new map.
   * @throws NullPointerException if <code>records</code> is <code>null</code>.
   * @throws IllegalArgumentException if <code>records</code> is <code>empty</code>.
   * @throws UnsupportedOperationException if Map.putAll(Map) throws this exception.
   * @throws ClassCastException if Map.putAll(Map) throws this exception.
   */
  public static void addLocalSRVRecords(Map records) {
    if (records == null) {
      throw new NullPointerException("Invalid (NULL) records.");
    }
    if (records.size() == 0) {
      throw new IllegalArgumentException("Invalid (empty) records.");
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "Add a new Map to existing mapping table.");
    }

    if (m_localSRVRecords == null) {
      setLocalSRVRecords(records);
      return;
    }

    synchronized (m_localSRVRecords) {
      // the new records will append to the current mapping table.
      m_localSRVRecords.putAll(records);
      dumpLocalSRVRecords();
    }
  }

  /**
   * Add a new key and associated SRV records in the mapping table.
   *
   * @param key the host name
   * @param records the SRV records
   * @throws NullPointerException if <code>key</code> is <code>null</code> or <code>records</code>
   *     is <code>null</code>.
   * @throws IllegalArgumentException if empty <code>SRV records</code>.
   * @throws IllegalArgumentException if <code>key</code> already exists.
   */
  public static void addLocalSRVRecord(String key, DsSRVWrapper[] records) {
    if (key == null) {
      throw new NullPointerException("Invalid (NULL) Key.");
    }
    if (records == null) {
      throw new NullPointerException("Invalid (NULL) SRV records.");
    }
    if (records.length == 0) {
      throw new IllegalArgumentException("Invalid (empty) SRV records.");
    }
    if (m_localSRVRecords == null) {
      m_localSRVRecords = new HashMap();
    }
    synchronized (m_localSRVRecords) {
      if (m_localSRVRecords.containsKey(key)) {
        throw new IllegalArgumentException("Map already contains key.");
      }
      m_localSRVRecords.put(key, records);
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      for (int i = 0; i < records.length; i++) {
        DsLog4j.resolvCat.log(
            Level.DEBUG, "Add a local SRV records: key = " + key + ", records = " + records[i]);
      }
    }
  }

  /**
   * Delete the existing key and associated SRV records from the mapping table.
   *
   * @param key the existing key in the mapping table.
   * @return String[] the records associated with the key or <code>null</code> if key not exists or
   *     <code>null</code> if the local SRV table <code>empty</code>.
   * @throws IllegalArgumentException if <code>key</code> is <code>null</code>.
   */
  public static String[] deleteLocalSRVRecord(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Invalid (NULL) Key.");
    }
    if (m_localSRVRecords == null) {
      return null;
    }
    String[] retRecords = null;
    synchronized (m_localSRVRecords) {
      if (!m_localSRVRecords.containsKey(key)) {
        return null;
      }
      retRecords = (String[]) m_localSRVRecords.remove(key);
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(
          Level.DEBUG, "Remove the key = " + key + " with associated SRV records.");
    }

    return retRecords;
  }

  /**
   * Set Enable/Disable flag to local SRV configurations.
   *
   * @param key the existing key in the mapping table.
   * @param host the InetAddress host
   * @param flag the boolean to enable/disable the local SRV configurations.
   * @return int the number of records has been set with the flag.
   * @throws NullPointerException if <code>key</code> is <code>null</code> or <code>host</code> is
   *     <code>null</code>.
   */
  public static int setLocalSRVRecordStatus(String key, InetAddress host, boolean flag) {
    int set_count = 0;
    if (key == null) {
      throw new NullPointerException("Invalid (NULL) Key.");
    }
    if (host == null) {
      throw new NullPointerException("Invalid (NULL) host.");
    }

    synchronized (m_localSRVRecords) {
      Object obj = m_localSRVRecords.get(key);
      if (obj == null) {
        return set_count;
      }
      DsSRVWrapper[] records = (DsSRVWrapper[]) obj;
      for (int i = 0; i < records.length; i++) {
        try {
          if (records[i].getIPAddress().equals(host)) {
            records[i].setEnabled(flag);
            set_count++;
          }
        } catch (UnknownHostException e) {
          if (DsLog4j.resolvCat.isDebugEnabled()) {
            DsLog4j.resolvCat.debug(
                "No SRV Record in the local configurations matched for host: " + host);
          }
        }
      }
    }

    return set_count;
  }

  /**
   * Sets the amount of time in seconds to wait for a response before giving up on a particular DNS
   * server for a particular query. Applies to all servers found or provided via setDNSServers. This
   * method can only be called before the first SRV query is performed. The default timeout is 10
   * seconds.
   *
   * @param seconds the timeout in seconds to use
   * @return <code>false</code> if DNS is active (an SRV query has already been performed).
   *     Otherwise reurns <code>true</code>.
   * @deprecated not supported any more
   */
  public static boolean setDNSTimeout(int seconds) {
    return false;
  }

  /**
   * Set whether to use TCP instead of UDP when communicating with DNS servers for SRV record
   * lookup. Applies to all servers found or provided via setDNSServers. This method can only be
   * called before the first SRV query is performed.
   *
   * @param useTCP if set to <code>true</code>, use TCP, otherwise, use UDP.
   * @return <code>false</code> if DNS is active (an SRV query has already been performed).
   *     Otherwise returns <code>true</code>.
   * @deprecated not supported any more
   */
  public static boolean setDNSUseTCP(boolean useTCP) {
    return false;
  }

  /**
   * Should we or should we not obtain a list of servers to try. In this implementation, if maddr is
   * not an IP address, it treated as if it were in the host field. SRV lookup is done if no port is
   * specified.
   *
   * @param sipURL the SIP URL to examine
   * @return <code>true</code> if a server should be searched for, false if there is only a single
   *     address, protocol, port to try
   * @throws DsSipParserException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   */
  public boolean shouldSearch(DsSipURL sipURL) throws DsSipParserException {
    boolean ret_value = true;
    String host = DsByteString.toString(sipURL.getMAddrParam());

    if (host == null) {
      host = DsByteString.toString(sipURL.getHost());
    }

    ret_value =
        !(IPValidator.hostIsIPAddr(host)
            && (sipURL.isSecure() || sipURL.hasTransport())
            && sipURL.hasPort());

    return ret_value;
  }

  /**
   * Should we or should we not obtain a list of servers to try. In this case, it is already known
   * if the host (or maddr) specifies an IP addr. Passing as param prevents a costly parse.
   *
   * @param sipURL the SIP URL to examine
   * @param hostIsIP true if the host is an IP address
   * @return <code>true</code> if a server should be searched for, <code>false</code> if there is
   *     only a single address, protocol, port to try
   * @throws DsSipParserException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   */
  public boolean shouldSearch(DsSipURL sipURL, boolean hostIsIP) throws DsSipParserException {
    return !(hostIsIP && (sipURL.isSecure() || sipURL.hasTransport()) && sipURL.hasPort());
  }

  /**
   * Determine if, according to the SIP spec, more than one service end point should be tried.
   *
   * @param hostName host part of URI
   * @param port port in the URI
   * @param transport transport param of the URI
   * @return <code>true</code> if according to the RFC, more than one service end point should be
   *     tried otherwise returns <code>false</code>
   */
  public boolean shouldSearch(String hostName, int port, int transport) {
    return !(IPValidator.hostIsIPAddr(hostName)
        && (port != DsSipResolverUtils.RPU)
        && (transport != DsSipResolverUtils.BTU));
  }

  /**
   * Constructs with default parameters and need to invoke initialize() for locating server hosts
   * based on the host information present in the SIP URL passed to the initialize() method.
   */
  public DsSipServerLocator() {
    m_host = null;
    m_transport = (byte) DsSipResolverUtils.BTU;
    // m_port = -1;
    m_case = -1;
    m_iterator = null;
    m_bindingInfo = null;
    m_currentBindingInfo = null;
    randomGenerator = new SecureRandom();
    initializeDNS();
  }

  /**
   * Check to see if the query for 'sipURL' would match the current query. Used to prevent duplicate
   * lookups, if the client is already holding a DsSipServerLocator.
   *
   * @param sipURL to check
   * @param sizeExceedsMTU <code>true</code> if this message was too large for UDP
   * @return true if query for this url would match the current query
   */
  public boolean queryMatches(DsSipURL sipURL, boolean sizeExceedsMTU) {
    return DsSipResolverUtils.queryMatches(sipURL, m_host, m_port, m_transport)
        && (m_sizeExceedsMTU == sizeExceedsMTU);
  }

  /**
   * Initialize this server locator after construction.
   *
   * @param network the associated network
   * @param host the host part of the URI
   * @param port the port part of the URI
   * @param proto the protocol part of the URI
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be located
   */
  public void initialize(DsNetwork network, String host, int port, int proto)
      throws UnknownHostException, DsSipServerNotFoundException {
    DsSipResolverUtils.initialize(network, this, host, port, proto);
  }

  /**
   * Initialize this server locator after construction.
   *
   * @param network the associated network
   * @param localAddr local address
   * @param localPort local port
   * @param host the host part of the URI
   * @param port the port part of the URI
   * @param proto the protocol part of the URI
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be located
   */
  public void initialize(
      DsNetwork network, InetAddress localAddr, int localPort, String host, int port, int proto)
      throws UnknownHostException, DsSipServerNotFoundException {
    DsSipResolverUtils.initialize(network, this, localAddr, localPort, host, port, proto);
  }

  /**
   * Initialize this class as per the specified SIP URL. Does the lookup depending upon the "maddr"
   * parameter of the specified SIP URL. If "maddr" parameter is present, then uses this host name
   * for lookup query, otherwise uses the host name present in the SIP URL itself.
   *
   * @param network the associated network
   * @param sipURL The SIP URL containing the hostname which requires the lookup.
   * @throws DsSipParserException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be found
   */
  public void initialize(DsNetwork network, DsSipURL sipURL)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException {
    DsSipResolverUtils.initialize(network, this, sipURL);
  }

  /**
   * Initialize this class as per the specified SIP URL. Does the lookup depending upon the "maddr"
   * parameter of the specified SIP URL. If "maddr" parameter is present, then uses this host name
   * for lookup query, otherwise uses the host name present in the SIP URL itself.
   *
   * @param network the associated network
   * @param localAddr local address
   * @param localPort local port
   * @param sipURL The SIP URL containing the hostname which requires the lookup.
   * @throws DsSipParserException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be found
   */
  public void initialize(DsNetwork network, InetAddress localAddr, int localPort, DsSipURL sipURL)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException {
    DsSipResolverUtils.initialize(network, this, localAddr, localPort, sipURL);
  }

  /**
   * Initialize this server locator after construction.
   *
   * @param network the associated network
   * @param lAddr local address
   * @param lPort local port
   * @param host the host part of the URI
   * @param port the port part of the URI
   * @param proto the protocol part of the URI
   * @param haveIP tells if and IP address is used or a host name
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be located
   */
  public void initialize(
      DsNetwork network,
      InetAddress lAddr,
      int lPort,
      String host,
      int port,
      int proto,
      boolean haveIP)
      throws UnknownHostException, DsSipServerNotFoundException {
    boolean have_proto = (proto != DsSipResolverUtils.BTU);
    boolean have_port = (port != DsSipResolverUtils.RPU);

    this.m_localAddress = lAddr;
    this.m_localPort = lPort;
    this.m_host = host;
    this.m_transport = (byte) proto;
    this.m_port = port;

    if (haveIP) {
      if (have_proto && have_port) {
        m_case = IP_PROTO_PORT;
      } else if (have_proto) {
        m_case = IP_PROTO_NOPORT;
      } else if (have_port) {
        m_case = IP_NOPROTO_PORT;
      } else {
        m_case = IP_NOPROTO_NOPORT;
      }
    } else {
      if (have_proto && have_port) {
        m_case = NOIP_PROTO_PORT;
      } else if (have_proto) {
        m_case = NOIP_PROTO_NOPORT;
      } else if (have_port) {
        m_case = NOIP_NOPROTO_PORT;
      } else {
        m_case = NOIP_NOPROTO_NOPORT;
      }
    }

    buildList(network);

    if (m_bindingInfo != null) {
      Iterator it = m_bindingInfo.iterator();
      if (it != null) {
        while (it.hasNext()) {
          ((DsBindingInfo) it.next()).setNetwork(network);
        }
      }
    }

    start();
  }

  public void setSizeExceedsMTU(boolean sizeExceedsMTU) {
    m_sizeExceedsMTU = sizeExceedsMTU;
  }

  /**
   * Force the search to stop. Sets internal iterator to null. hasNextBindingInfo() will return
   * <code>false</code>.
   */
  public void stop() {
    m_iterator = null;
  }

  /**
   * Get the current binding info.
   *
   * @return the current binding info
   */
  public DsBindingInfo getCurrentBindingInfo() {
    return m_currentBindingInfo;
  }

  /**
   * Client interface to get the next binding info to try.
   *
   * @return the next binding info or null if the list has been exhausted
   */
  public DsBindingInfo getNextBindingInfo() {
    // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
    m_currentBindingInfo = null;

    if (m_iterator != null) {
      while (m_iterator.hasNext()) {
        m_currentBindingInfo = (DsBindingInfo) m_iterator.next();
        // if this is only one in the list, just use it w/o checking
        // whether it is in the unreachable destination table
        /**
         * edited by radmohan
         *
         * <p>CSCtz70393 Thread pool exhausted when all elements in srv group is down
         */
        if (!DsSipServerLocator.m_useDsUnreachableTable) {

          DsLog4j.resolvCat.log(
              Level.INFO,
              "DsSipServerLocator.m_useDsUnreachableTable: is set to :"
                  + DsSipServerLocator.m_useDsUnreachableTable
                  + "returning "
                  + m_currentBindingInfo);
          return m_currentBindingInfo;

        } else {
          DsLog4j.resolvCat.log(
              Level.DEBUG,
              "DsSipServerLocator.m_useDsUnreachableTable: is set to :"
                  + DsSipServerLocator.m_useDsUnreachableTable);
          if (!DsUnreachableDestinationTable.getInstance()
              .contains(
                  m_currentBindingInfo.getRemoteAddress(),
                  m_currentBindingInfo.getRemotePort(),
                  m_currentBindingInfo.getTransport())) {

            DsLog4j.resolvCat.log(
                Level.DEBUG,
                "DsSipServerLocator.getNextBindingInfo: not present DsUnreachableDestinationTable:"
                    + m_currentBindingInfo.getRemoteAddress());

            return m_currentBindingInfo;
          } else {

            DsLog4j.resolvCat.log(
                Level.DEBUG,
                "DsSipServerLocator.getNextBindingInfo:  present DsUnreachableDestinationTable:"
                    + m_currentBindingInfo.getRemoteAddress());

            m_currentBindingInfo = null;
          }
        }
      }
    }
    //  DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.getNextBindingInfo:  present
    // DsUnreachableDestinationTable: returning null");
    return m_currentBindingInfo;
  }

  /**
   * See if there are more binding infos in the list.
   *
   * @return true if there are more binding infos in the list.
   */
  public boolean hasNextBindingInfo() {
    boolean ret_val = false;

    if (m_iterator != null) {
      ret_val = m_iterator.hasNext();
    } else {
      ret_val = false;
    }

    return ret_val;
  }

  /**
   * Separate from constructor to allow reuse without going thru IP addr checks again.
   *
   * @param network the network that this list is being build for, gets passed to addSRVQuery for
   *     NAPTR setting
   * @throws UnknownHostException if host is not known
   */
  // * @throws DsSipServerNotFoundException if no server could be found
  private void buildList(DsNetwork network)
      throws UnknownHostException // , DsSipServerNotFoundException
      {
    m_bindingInfo = new Vector(3);

    switch (m_case) {
      case (NOIP_NOPROTO_NOPORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList: NOIP_NOPROTO_NOPORT");
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList: adding SRV query");
        }

        if (!addSRVQuery(m_host, network)) {
          if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
            DsLog4j.resolvCat.log(
                Level.INFO,
                "DsSipServerLocator.buildList: no SRV records. host "
                    + m_host
                    + "network "
                    + network);
          }

          if (doAQuery) {
            if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
              DsLog4j.resolvCat.log(
                  Level.INFO, "DsSipServerLocator.buildList: Trying A records for " + m_host);
            }
            addAQuery(m_host);
          }
        }
        break;

      case (NOIP_NOPROTO_PORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList: NOIP_NOPROTO_PORT");
        }

        addAQuery(m_host, m_port);
        break;

      case (NOIP_PROTO_NOPORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList: NOIP_PROTO_NOPORT");
        }

        if (!addSRVQuery(m_host, m_transport, network)) {
          if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
            DsLog4j.resolvCat.log(
                Level.INFO,
                "DsSipServerLocator.buildList: no SRV records for host "
                    + m_host
                    + " transport "
                    + m_transport
                    + " network "
                    + network);
          }

          if (doAQuery) {
            if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
              DsLog4j.resolvCat.log(
                  Level.INFO, "DsSipServerLocator.buildList: Trying A records for " + m_host);
            }
            addAQuery(
                m_host,
                m_transport,
                (m_transport == DsSipTransportType.TLS) ? TLS_DEFAULT_PORT : DEFAULT_PORT);
          }
        }
        break;

      case (NOIP_PROTO_PORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList: NOIP_PROTO_PORT ");
        }

        addAQuery(m_host, m_transport, m_port);
        break;

      case (IP_NOPROTO_NOPORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList:IP_NOPROTO_NOPORT ");
        }

        addProtocols(m_host);
        break;

      case (IP_NOPROTO_PORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList:IP_NOPROTO_PORT ");
        }

        addProtocols(m_host, m_port);
        break;

      case (IP_PROTO_NOPORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList: IP_PROTO_NOPORT");
        }

        add(
            m_localAddress,
            m_localPort,
            m_host,
            (m_transport == DsSipTransportType.TLS) ? TLS_DEFAULT_PORT : DEFAULT_PORT,
            m_transport);
        break;

      case (IP_PROTO_PORT):
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.buildList:IP_PROTO_PORT ");
        }

        add(m_localAddress, m_localPort, m_host, m_port, m_transport);
        break;
    }

    if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
      if (m_bindingInfo.size() == 0) {
        DsLog4j.resolvCat.log(
            Level.WARN,
            "DsSipServerLocator.buildList: list of endpoints to search is empty for "
                + " (host = "
                + m_host
                + " protocol = "
                + m_transport
                + " port = "
                + m_port
                + ").  Check available transports.");
      }
    }
  }

  // qfang 10.28/2005 CSCsc29805 try to make DsConfigManager.setDefaultOutgoingTransport work
  // for ServerLocator resolver
  /** Add UDP, TCP and TLS using respective default port. */
  private void addProtocols(InetAddress addr) {
    addProtocols(addr, DsSipResolverUtils.RPU);
  }

  // qfang 10.28/2005 CSCsc29805 try to make DsConfigManager.setDefaultOutgoingTransport work
  // for ServerLocator resolver
  /**
   * Add UDP, TCP and TLS using respective default port.
   *
   * @throws UnknownHostException if host is not known
   */
  private void addProtocols(String host) throws UnknownHostException {
    addProtocols(host, DsSipResolverUtils.RPU);
  }

  // qfang 10.28/2005 CSCsc29805 try to make DsConfigManager.setDefaultOutgoingTransport work
  // for ServerLocator resolver. Previously default transport is hard-coded
  // to UDP but now is determined by default outgoing transport in
  // DsConfigManager that is changeable by application
  /**
   * Add UDP, TCP and then TLS for the provided port and host.
   *
   * @throws UnknownHostException if host is not known
   */
  private void addProtocols(String host, int port) throws UnknownHostException {
    if (m_sizeExceedsMTU) {
      if (port == DsSipResolverUtils.RPU) {
        port = DEFAULT_PORT;
      }
      add(m_localAddress, m_localPort, host, port, DsSipTransportType.TCP);
    }

    if (!m_sizeExceedsMTU
        || DsConfigManager.getDefaultOutgoingTransport() != DsSipTransportType.TCP) {
      if (port == DsSipResolverUtils.RPU) {
        port =
            (DsConfigManager.getDefaultOutgoingTransport() == DsSipTransportType.TLS)
                ? TLS_DEFAULT_PORT
                : DEFAULT_PORT;
      }
      add(m_localAddress, m_localPort, host, port, DsConfigManager.getDefaultOutgoingTransport());
    }
  }

  // qfang 10.28/2005 CSCsc29805 try to make DsConfigManager.setDefaultOutgoingTransport work
  // for ServerLocator resolver. Previously default transport is hard-coded
  // to UDP but now is determined by default outgoing transport in
  // DsConfigManager that is changeable by application
  /** Add UDP, TCP and then TLS for the provided port and addr. */
  private void addProtocols(InetAddress addr, int port) {
    if (m_sizeExceedsMTU) {
      if (port == DsSipResolverUtils.RPU) {
        port = DEFAULT_PORT;
      }
      add(m_localAddress, m_localPort, addr, port, DsSipTransportType.TCP);
    }

    if (!m_sizeExceedsMTU
        || DsConfigManager.getDefaultOutgoingTransport() != DsSipTransportType.TCP) {
      if (port == DsSipResolverUtils.RPU) {
        port =
            (DsConfigManager.getDefaultOutgoingTransport() == DsSipTransportType.TLS)
                ? TLS_DEFAULT_PORT
                : DEFAULT_PORT;
      }
      add(m_localAddress, m_localPort, addr, port, DsConfigManager.getDefaultOutgoingTransport());
    }
  }

  /**
   * Perform a DNS A record query for this host. Add entries for UDP, TCP and TLS using respective
   * default port
   *
   * @throws UnknownHostException if host is not known
   */
  private void addAQuery(String host) throws UnknownHostException {
    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("Entering addAQuery(String): " + host);
    }

    InetAddress[] records = null;
    records = getARecords(host);
    if (records == null) {
      if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
        DsLog4j.resolvCat.log(Level.INFO, "NO A records found!. Leaving addAQuery for " + host);
      }
      return;
    }

    for (int i = 0; i < records.length; ++i) {
      addProtocols(records[i]);
    }
  }

  /**
   * Perform a DNS A record query for this host. Add entries for TLS, TCP and UDP
   *
   * @throws UnknownHostException if host is not known
   */
  private void addAQuery(String host, int port) throws UnknownHostException {
    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("Entering addAQuery(String, int): " + host + ":" + port);
    }

    InetAddress[] records = null;
    records = getARecords(host);
    if (records == null) {
      if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
        DsLog4j.resolvCat.log(Level.INFO, "NO A records found!. Leaving addAQuery for " + host);
      }
      return;
    }

    for (int i = 0; i < records.length; ++i) {
      addProtocols(records[i], port);
    }
  }

  /**
   * Perform a DNS A record query for this host. Add entries for the resultant addresses
   *
   * @throws UnknownHostException if host is not known
   */
  private void addAQuery(String host, int proto, int port) throws UnknownHostException {
    if (!isSupported(proto)) {
      DsLog4j.resolvCat.log(
          Level.WARN,
          "addAQuery for host "
              + host
              + " Transport "
              + proto
              + " not supported , host:port = "
              + host
              + ":"
              + port
              + " returning false");
      return;
    }

    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug(
          "Entering addAQuery(String, int, int): " + host + ":" + proto + ":" + port);
    }

    InetAddress[] records = null;
    records = getARecords(host);
    if (records == null) {
      if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
        DsLog4j.resolvCat.log(
            Level.INFO,
            "NO A records found!. Leaving addAQuery for " + host + ":" + proto + ":" + port);
      }
      return;
    }
    for (int i = 0; i < records.length; ++i) {
      add(m_localAddress, m_localPort, records[i], port, proto);
    }
  }

  private Attributes getQueryAttributes(String query, String[] attrIds) throws NamingException {
    long timeBeforeLookup = System.currentTimeMillis();
    Attributes attributes = jndiDNS.getAttributes(query, attrIds);
    long timeTakenForLookup = System.currentTimeMillis() - timeBeforeLookup;
    DsLog4j.resolvCat.info("Time taken to resolve " + query + " " + timeTakenForLookup);
    return attributes;
  }

  /**
   * Use the old JNDI-based lookups for A Records or force it to go right to InetAddress to do the
   * lookups, picking up the local DNS in files first.
   *
   * @param flag set to <code>true</code> to force InetAddress lookups for A Records or <code>false
   *     </code>, the default for the old JNDI behavior
   */
  public static void setUseInetForARecords(boolean flag) {
    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("Setting m_useInetLookupForARecords to: " + flag);
    }

    m_useInetLookupForARecords = flag;
  }

  public InetAddress[] getARecords(String query) throws UnknownHostException {
    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("Entering getARecords(String): " + query);
    }

    // fix for CSCte67425: SRV resolver does not recognize etc host file
    if (m_useInetLookupForARecords) {
      InetAddress[] res = InetAddress.getAllByName(query);
      if (res != null && res.length > 0) {
        return res;
      }

      return null;
    }

    if (initFailed) {
      if (DsLog4j.resolvCat.isWarnEnabled()) {
        DsLog4j.resolvCat.warn("DNS Initialization failed! returning null");
      }
      return null;
    }
    try {
      Attributes attributes = getQueryAttributes(query, A_ATTR_IDS);
      if (attributes != null) {
        NamingEnumeration all = attributes.getAll();
        if (all.hasMore()) {
          Attribute attr = (Attribute) all.next();
          NamingEnumeration values = attr.getAll();
          InetAddress[] result = new InetAddress[attr.size()];
          int i = 0;
          while (values.hasMore()) {
            String host = (String) values.next();
            result[i++] = InetAddress.getByName(host);
          }
          setARecordResults(result);
          return result;
        } else {
          if (DsLog4j.resolvCat.isDebugEnabled()) {
            DsLog4j.resolvCat.debug("NO A records found! returning null");
          }
        }
      }
    } catch (CommunicationException e) {
      // REFACTOR
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_A, query, e, m_currentBindingInfo));
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn(
            "Exception during JNDI DNS attribute retrieval for query :"
                + query
                + " Returning null!!",
            e);
      }
      return null;

    } catch (NamingException e) {
      // REFACTOR
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_A, query, e, m_currentBindingInfo));
      if ((MY_DOMAIN == null) || (query.indexOf('.') != -1)) {
        if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
          DsLog4j.resolvCat.warn("NamingException in add A query! returning null: ", e);
        }
      } else {
        if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
          DsLog4j.resolvCat.info(
              "NamingException for " + query + " - trying with " + (query + MY_DOMAIN));
        }

        return getARecords(query + MY_DOMAIN);
      }
    } catch (Exception e) {
      // REFACTOR
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_A, query, e, m_currentBindingInfo));
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn("Exception in add A query! returning null: ", e);
      }
    }
    return null;
  }

  /* query _sip.m_transport */

  /**
   * Perform a DNS SRV record query for this host and protocol. Sort the results according to RFC
   * 2782 and add the results
   */
  private boolean addSRVQuery(
      String host, int proto, DsNetwork network) // throws DsSipServerNotFoundException
      {
    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(
          Level.DEBUG, "Entering addSRVQuery: host(" + host + ") protocol(" + proto + ")");
    }

    if (!isSupported(proto)) {
      DsLog4j.resolvCat.log(
          Level.WARN,
          "addSRVQuery for host "
              + host
              + " Transport "
              + proto
              + " not supported in "
              + network
              + " returning false");
      return false;
    }
    DsSRVWrapper[] records =
        (m_debugMode) ? doSRVDebugQuery(host, proto) : doSRVQuery(host, proto, network);

    if (records == null) {
      if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
        DsLog4j.resolvCat.log(
            Level.INFO,
            "NO SRV records found!. Leaving addSRVQuery, returning false for "
                + host
                + ":"
                + proto);
      }

      return false;
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "SRV records found for " + host);
      for (int i = 0; i < records.length; i++) {
        try {
          DsLog4j.resolvCat.log(Level.DEBUG, "SRV record: " + records[i].getIPAddress().toString());
        } catch (UnknownHostException e) {
          DsLog4j.resolvCat.log(
              Level.DEBUG, "Exception resolving the SRV record IP:" + e.getMessage());
        }
      }
    }

    Vector result = null;

    switch (proto) {
      case DsSipTransportType.TLS:
        result = sortRecords(records, null, null);
        break;
      case DsSipTransportType.TCP:
        result = sortRecords(null, records, null);
        break;
      case DsSipTransportType.UDP:
        result = sortRecords(null, null, records);
    }
    if (result != null) {
      Iterator it = result.iterator();
      DsBindingInfo info = null;
      while (it.hasNext()) {
        info = (DsBindingInfo) it.next();
        info.setLocalAddress(m_localAddress);
        info.setLocalPort(m_localPort);
        add(info);
      }
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "Leaving addSRVQuery. returning true");
    }

    return true;
  }

  /* query _sip._tcp and _sip._udp */

  /**
   * Perform a DNS SRV record query for this host and for the protocols(UDP, TCP and TLS). Sort the
   * results according to RFC 2782 and add the results
   *
   * @param host the host to query
   * @param network the network, used for NAPTR setting
   */
  private boolean addSRVQuery(String host, DsNetwork network) // throws DsSipServerNotFoundException
      {
    DsSRVWrapper[] tls_records = null;
    DsSRVWrapper[] tcp_records = null;
    DsSRVWrapper[] udp_records = null;

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "Entering DsSipServerLocator.addSRVQuery: " + host);
    }

    if (network.isNAPTREnabled()) {
      if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
        DsLog4j.resolvCat.log(Level.DEBUG, "Doing a NAPTR lookup for: " + host);
      }

      // query NAPTR records
      DsNAPTRRecord[] records = m_debugMode ? getDebugNAPTRRecords() : getNAPTRRecords(host);
      if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
        if (records != null) {
          if (records.length != 0) {
            DsLog4j.resolvCat.log(Level.DEBUG, "getNAPTRRecords() returned:");
            for (int recCount = 0; recCount < records.length; recCount++) {
              DsLog4j.resolvCat.log(Level.DEBUG, "\t" + records[recCount]);
            }

          } else {
            DsLog4j.resolvCat.log(Level.DEBUG, "getNAPTRRecords() returned zero records!");
          }
        } else {
          DsLog4j.resolvCat.log(Level.DEBUG, "get*NAPTRRecords() returned null!");
        }
      }

      if (null != records) {
        String query = null;
        String service = null;
        Vector recs = new Vector();

        // check for the valid SIP NAPTR records and also if the
        // corresponding transport is supported by the client.

        for (int i = 0; i < records.length; i++) {
          if (records[i] == null) continue;

          query = records[i].m_strReplacement;
          service = records[i].m_strService;

          if (null != query && null != service) {
            int trans = DsNAPTRRecord.serviceType(service);

            if (isSupported(trans)) {
              recs.add(records[i]);
            }
          }
        }

        // sort the NAPTR records if required
        if (recs.size() > 1) {
          sortNAPTRRecords(recs);
        }

        Vector tlsRecords = null;
        Vector udpRecords = null;
        Vector tcpRecords = null;

        // Retreive the SRV records for the corresponding NAPTR record
        // and merge the ones with the same transport type.

        for (int j = 0; j < recs.size(); j++) {
          DsNAPTRRecord record = (DsNAPTRRecord) recs.get(j);
          query = record.m_strReplacement;
          if (query == null || query.length() < 1) {
            continue;
          }
          switch (DsNAPTRRecord.serviceType(record.m_strService)) {
            case DsSipTransportType.UDP:
              udp_records =
                  m_debugMode
                      ? getDebugSRVRecords(query, DsSipTransportType.UDP)
                      : getSRVRecords(query, DsSipTransportType.UDP);
              if (null != udp_records) {
                udpRecords = sortRecords(null, null, udp_records);
                Iterator it = udpRecords.iterator();
                DsBindingInfo info = null;
                while (it.hasNext()) {
                  info = (DsBindingInfo) it.next();
                  info.setLocalAddress(m_localAddress);
                  info.setLocalPort(m_localPort);
                  add(info);
                }
              }
              break;
            case DsSipTransportType.TCP:
              tcp_records =
                  m_debugMode
                      ? getDebugSRVRecords(query, DsSipTransportType.TCP)
                      : getSRVRecords(query, DsSipTransportType.TCP);
              if (null != tcp_records) {
                tcpRecords = sortRecords(null, tcp_records, null);
                Iterator it = tcpRecords.iterator();
                DsBindingInfo info = null;
                while (it.hasNext()) {
                  info = (DsBindingInfo) it.next();
                  info.setLocalAddress(m_localAddress);
                  info.setLocalPort(m_localPort);
                  add(info);
                }
              }
              break;
            case DsSipTransportType.TLS:
              tls_records =
                  m_debugMode
                      ? getDebugSRVRecords(query, DsSipTransportType.TLS)
                      : getSRVRecords(query, DsSipTransportType.TLS);
              if (null != tls_records) {
                tlsRecords = sortRecords(tls_records, null, null);
                Iterator it = tlsRecords.iterator();
                DsBindingInfo info = null;
                while (it.hasNext()) {
                  info = (DsBindingInfo) it.next();
                  info.setLocalAddress(m_localAddress);
                  info.setLocalPort(m_localPort);
                  add(info);
                }
              }
              break;
          } // _switch
        } // _for
      } // _if
    }

    if (tls_records == null && tcp_records == null && udp_records == null) {
      // either NAPTR is disabled or no NAPTR records found
      if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
        DsLog4j.resolvCat.log(
            Level.DEBUG, "either NAPTR is disabled or no NAPTR records found for: " + host);
      }

      if (isSupported(DsSipTransportType.UDP_MASK)) {
        addSRVQuery(host, DsSipTransportType.UDP, network);
      }
      if (isSupported(DsSipTransportType.TCP_MASK)) {
        addSRVQuery(host, DsSipTransportType.TCP, network);
      }
      if (isSupported(DsSipTransportType.TLS_MASK)) {
        addSRVQuery(host, DsSipTransportType.TLS, network);
      }
    } // _if

    if (m_bindingInfo.size() < 1) {
      if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
        DsLog4j.resolvCat.log(
            Level.DEBUG,
            "bindingInfo Map is empty. Leaving DsSipServerLocator.addSRVQuery returning false");
      }

      return false;
    }
    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "DsSipServerLocator.addSRVQuery returning true");
    }
    return true;
  }

  /**
   * RFC 2782 algorithm for sorting the SRV query results.
   *
   * @param tls_records the TLS records found from the DNS query
   * @param tcp_records the TCP records found from the DNS query
   * @param udp_records the UDP records found from the DNS query
   * @return the records sorted per the RFC
   */
  protected Vector sortRecords(
      DsSRVWrapper[] tls_records, DsSRVWrapper[] tcp_records, DsSRVWrapper[] udp_records) {
    Vector ordered_records = new Vector();
    TreeMap SRVMap = new TreeMap();
    // merge the SRV query result
    if (tls_records != null) {
      for (int i = 0; i < tls_records.length; ++i) {
        if (!tls_records[i].isEnabled()) continue;

        // SRVRecord this_rec = (SRVRecord) tls_records[i];
        Short priority = new Short((short) tls_records[i].getLevel());

        if (!SRVMap.containsKey(priority)) {
          SRVMap.put(priority, new LinkedList());
        }

        ((LinkedList) SRVMap.get(priority)).add(tls_records[i]);
      }
    }

    if (tcp_records != null) {
      for (int i = 0; i < tcp_records.length; ++i) {

        if (!tcp_records[i].isEnabled()) continue;

        // SRVRecord this_rec = (SRVRecord) tcp_records[i];
        Short priority = new Short((short) tcp_records[i].getLevel());

        if (!SRVMap.containsKey(priority)) {
          SRVMap.put(priority, new LinkedList());
        }

        ((LinkedList) SRVMap.get(priority)).add(tcp_records[i]);
      }
    }

    if (udp_records != null) {
      for (int i = 0; i < udp_records.length; ++i) {

        if (!udp_records[i].isEnabled()) continue;
        // SRVRecord this_rec = (SRVRecord) udp_records[i];
        Short priority = new Short((short) udp_records[i].getLevel());

        if (!SRVMap.containsKey(priority)) {
          SRVMap.put(priority, new LinkedList());
        }

        ((LinkedList) SRVMap.get(priority)).add(udp_records[i]);
      }
    }

    Set entrySet = SRVMap.entrySet();
    Iterator entries = entrySet.iterator();

    DsBindingInfo bindingInfo = null;
    while (entries.hasNext()) {
      LinkedList eqLevelSet = (LinkedList) ((Map.Entry) entries.next()).getValue();

      while (!eqLevelSet.isEmpty()) {
        DsSRVWrapper selection = makeSelection(eqLevelSet);

        bindingInfo = selection.getBindingInfo();
        if (null != bindingInfo) {
          ordered_records.add(bindingInfo);
        }
      }
    }

    return ordered_records;
  }

  /**
   * RFC 2782 algorithm for weighted selection.
   *
   * @param eqLevelSet a set of SRV records having equal priority
   * @return srv wrapper object containing selected server
   */
  protected DsSRVWrapper makeSelection(LinkedList eqLevelSet) {
    DsSRVWrapper ret_wrapper = null;
    int sum = 0;
    DsSRVWrapper[] srvList =
        (DsSRVWrapper[]) eqLevelSet.toArray(new DsSRVWrapper[eqLevelSet.size()]);
    //
    // Use a local variable to store the running sum to avoid several threads updating the same
    // running sum.
    //
    int[] runningSum = new int[eqLevelSet.size()];

    for (int i = 0; i < srvList.length; i++) {
      DsSRVWrapper srvWrapper = srvList[i];
      sum += srvWrapper.getWeight();
      runningSum[i] = sum;
    }

    int rand = genRandom(sum);

    for (int i = 0; i < srvList.length; i++) {
      DsSRVWrapper srvWrapper = srvList[i];
      if (runningSum[i] >= rand) {
        ret_wrapper = srvWrapper;

        eqLevelSet.remove(srvWrapper);

        break;
      }
    }

    return ret_wrapper;
  }

  /**
   * RFC 2782 algorithm for weighted selection.
   *
   * @param running_sum the running sum
   * @return random number based on the specified running sum
   */
  protected int genRandom(int running_sum) {
    return randomGenerator.nextInt(running_sum + 1);
  }

  /** Initialize using the JNDI DNS service provider. */
  private void initializeJNDIDNS() throws javax.naming.NoInitialContextException, NamingException {
    // REFACTOR
    //    setJndiDNSContext(DsJNDIDirContextFactory.createJNDIContext());
  }

  private void initializeDNS() {
    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("initializing DNS");
    }

    try {
      initializeJNDIDNS();
    } catch (Exception e) {
      initFailed = true;
      DsLog4j.resolvCat.warn(
          "Server locator JNDI DNS initialization failed! UA Stack will not use SRV records.", e);
    }
  }

  /**
   * Perform a DNS query.
   *
   * @param query the query to perform
   * @param protocol the transport protocol
   * @return the result of the query
   */
  public DsSRVWrapper[] getSRVRecords(String query, int protocol) {
    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("Entering getSRVRecords: " + query + " proto = " + protocol);
    }

    /** added for CSCtz70393 threadpool defect radmohan */
    if (!DsSipServerLocator.m_useDsUnreachableTable) {

      DsLog4j.resolvCat.log(
          Level.DEBUG,
          "DsSipServerLocator.m_useDsUnreachableTable: is set to :"
              + DsSipServerLocator.m_useDsUnreachableTable);
    } else {
      DsLog4j.resolvCat.log(
          Level.DEBUG,
          "DsSipServerLocator.m_useDsUnreachableTable: is set to :"
              + DsSipServerLocator.m_useDsUnreachableTable);

      InetAddress addr = null;
      try {
        addr = InetAddress.getByAddress(query, "127.0.0.1".getBytes());
      } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        DsLog4j.resolvCat.warn(
            "Exception in SRV query InetAddress.getByAddress : " + query,
            new DsTrackingException(TrackingExceptions.NULLPOINTEREXCEPTION, e));
        return null;
      }

      if (!DsUnreachableDestinationTable.getInstance().contains(addr, 5060, 1)) {

        DsLog4j.resolvCat.log(
            Level.DEBUG,
            "DsSipServerLocator.getSRVRecords: not present DsUnreachableDestinationTable:" + query);

        // return m_currentBindingInfo;
      } else {

        DsLog4j.resolvCat.log(
            Level.DEBUG,
            "DsSipServerLocator.getSRVRecords:  present DsUnreachableDestinationTable:" + query);

        return null;
      }
    }

    if (initFailed) {
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn("DNS Initialization failed! returning null");
      }
      return null;
    }
    try {
      return doSRVQuery(query, protocol);
    } catch (CommunicationException e) {
      // REFACTOR
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_SRV, query, e, m_currentBindingInfo));
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn(
            "Exception during JNDI DNS attribute retrieval for query :"
                + query
                + " and protocol: "
                + protocol
                + " Returning null!!",
            e);
      }
      return null;

    } catch (NamingException e) {
      // REFACTOR
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_SRV, query, e, m_currentBindingInfo));
      if (MY_DOMAIN == null || countDots(query) != 2) {
        if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
          DsLog4j.resolvCat.warn("Exception in SRV query NamingException! returning null: ", e);
        }

        InetAddress addr = null;
        try {
          addr = InetAddress.getByAddress(query, "127.0.0.1".getBytes());
        } catch (UnknownHostException eh) {
          // TODO Auto-generated catch block
          DsLog4j.resolvCat.warn(
              "Exception in SRV query InetAddress.getByAddress and query is : " + query, eh);
          return null;
        }

        if (addr != null) {
          DsUnreachableDestinationTable.getInstance().add(addr, 5060, 1);
        }

      } else {
        if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
          DsLog4j.resolvCat.info(
              "NamingException for " + query + " - trying with " + (query + MY_DOMAIN));
        }

        return getSRVRecords(query + MY_DOMAIN, protocol);
      }
    } catch (Exception e) {
      // REFACTOR
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_SRV, query, e, m_currentBindingInfo));
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn("Exception in SRV query Exception! returning null: ", e);
      }
      InetAddress addr = null;
      try {
        addr = InetAddress.getByAddress(query, "127.0.0.1".getBytes());
      } catch (UnknownHostException eh) {
        // TODO Auto-generated catch block
        DsLog4j.resolvCat.warn(
            "Exception in SRV query InetAddress.getByAddress and query is : " + query,
            new DsTrackingException(TrackingExceptions.NULLPOINTEREXCEPTION, e));
        return null;
      }

      if (addr != null) {
        DsUnreachableDestinationTable.getInstance().add(addr, 5060, 1);
      }
      return null;
    }

    return null;
  }

  /**
   * Perform a DNS query.
   *
   * @param query the query to perform
   * @param protocol the transport protocol
   * @return the result of the query
   * @throws NamingException
   */
  public DsSRVWrapper[] doSRVQuery(String query, int protocol) throws NamingException {
    Attributes attributes = getQueryAttributes(query, SRV_ATTR_IDS);
    DsSRVWrapper[] records = null;
    if (attributes != null) {
      NamingEnumeration all = attributes.getAll();
      if (all.hasMore()) {
        records = wrapRecords((Attribute) all.next(), protocol);
      } else {
        if (DsLog4j.resolvCat.isInfoEnabled()) {
          DsLog4j.resolvCat.info("NO SRV records found! returning null");
        }
        records = null;
      }
    }
    return records;
  }

  private static int countDots(String s) {
    int count = 0;

    int index = s.indexOf('.');
    while (index != -1) {
      ++count;
      index = s.indexOf('.', index + 1);
    }

    return count;
  }

  private static DsSRVWrapper[] getDebugSRVRecords(String query, int protocol) {
    return doSRVDebugQuery(query, protocol);
  }

  private DsNAPTRRecord[] getNAPTRRecords(String query) {

    if (initFailed) {
      if (DsLog4j.resolvCat.isWarnEnabled()) {
        DsLog4j.resolvCat.warn("DNS Initialization failed! returning null");
      }
      return null;
    }
    DsNAPTRRecord[] records = null;
    try {
      Attributes naptrAttributes = getQueryAttributes(query, NAPTR_ATTR_IDS);
      if (naptrAttributes != null) {
        NamingEnumeration all = naptrAttributes.getAll();
        Attribute attributes = null;
        if (all.hasMore()) {
          attributes = (Attribute) all.next();
          if (null != attributes) {
            if (DsLog4j.resolvCat.isDebugEnabled()) {
              DsLog4j.resolvCat.debug("attributes sise is " + attributes.size());
            }

            records = new DsNAPTRRecord[attributes.size()];
            all = attributes.getAll();
            int index = 0;
            if (DsLog4j.resolvCat.isDebugEnabled()) {
              if (!all.hasMore()) {
                DsLog4j.resolvCat.debug("NAPTR Records actually empty!");
              }
            }

            while (all.hasMore()) {
              records[index++] = DsNAPTRRecord.getRecord((String) all.next());
              if (DsLog4j.resolvCat.isDebugEnabled()) {
                DsLog4j.resolvCat.debug("got NAPTR Record: " + records[index - 1]);
              }
            }
          } else {
            if (DsLog4j.resolvCat.isDebugEnabled()) {
              DsLog4j.resolvCat.debug("all.next() returned null attributes!");
            }
          }
        } else {
          if (DsLog4j.resolvCat.isDebugEnabled()) {
            DsLog4j.resolvCat.debug("getQueryAttributes()returned empty attributes!");
          }
        }
      } else {
        if (DsLog4j.resolvCat.isDebugEnabled()) {
          DsLog4j.resolvCat.debug("getQueryAttributes()returned null!!");
        }
      }
    } catch (CommunicationException e) {
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn(
            "Exception during JNDI DNS attribute retrieval for query :"
                + query
                + " Returning null!!",
            e);
      }
      return null;

    } catch (NamingException e) {
      if ((MY_DOMAIN == null) || (query.indexOf('.') != -1)) {
        if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
          DsLog4j.resolvCat.warn("Exception in getNAPTRRecords()! (returning existing records)", e);
        }
      } else {
        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.debug(
              "NamingException for " + query + " - trying with " + (query + MY_DOMAIN));
        }

        return getNAPTRRecords(query + MY_DOMAIN);
      }
    } catch (Exception e) {
      if (DsLog4j.resolvCat.isEnabled(Level.WARN)) {
        DsLog4j.resolvCat.warn("Exception in getNAPTRRecords()! (returning existing records)", e);
      }
    }
    return records;
  }

  /** Sorts the NAPTR records as per RFC 2915. */
  private static void sortNAPTRRecords(Vector records) {
    int len = records.size();
    DsNAPTRRecord a = null;
    DsNAPTRRecord b = null;
    for (int i = len - 2; i >= 0; i--) {
      for (int j = 0; j <= i; j++) {
        a = (DsNAPTRRecord) records.get(j + 1);
        b = (DsNAPTRRecord) records.get(j);
        if ((a.m_lOrder < b.m_lOrder)
            || (a.m_lOrder == b.m_lOrder && a.m_lPreference < b.m_lPreference)) {
          records.set(j, a);
          records.set(j + 1, b);
        }
      }
    }
  }

  private DsSRVWrapper[] doSRVQuery(String host, int proto, DsNetwork network) {
    DsSRVWrapper[] records = null;

    // maivu - 05.22.07 - CSCsi98575 - Add Local SRV configuration functionality instead of DNS
    records = doLocalSRVLookup(host, proto);
    // Found the host in the local configurations.
    if (records != null) {
      return records;
    } else {
      if (DsLog4j.resolvCat.isDebugEnabled()) {
        DsLog4j.resolvCat.debug(
            "No SRV Record in the local configurations matched for host: " + host);
      }
    }
    // Only do the remote DNS SRV lookup if
    // 1. there is NO local mapping
    // OR
    // 2. local mapping return no host found AND the flag useLocalOnly is
    // false
    if ((m_localSRVRecords == null) || (!DsSipServerLocator.m_useLocalSRVLookupOnly)) {
      if (isSupported(proto)) {
        switch (proto) {
          case (DsSipTransportType.TCP):
            records = getSRVRecords("_sip._tcp." + host, proto);
            break;
          case (DsSipTransportType.UDP):
            records = getSRVRecords("_sip._udp." + host, proto);
            break;
          case (DsSipTransportType.TLS):
            if (network.getDnsLookupTLSLyncFederationEnabled()) {
              records = getSRVRecords("_sipfederationtls._tcp." + host, proto);
            } else {
              records = getSRVRecords("_sips._tcp." + host, proto);
            }
            break;
          default:
            break;
        }
      }
    }
    setSrvResults(records);
    return records;
  }

  /**
   * Do the local SRV lookup if local mapping table exists.
   *
   * @param host the lookup host
   * @param proto the protocol
   * @return DsSRVWrapper[] the list of SRV records
   */
  private static DsSRVWrapper[] doLocalSRVLookup(String host, int proto) {
    if (m_localSRVRecords == null) {
      return null;
    }
    DsSRVWrapper[] records = null;
    // The local mapping table exists; hence use the local SRV configurations now.

    if (DsLog4j.resolvCat.isDebugEnabled()) {
      DsLog4j.resolvCat.debug("Use the local SRV Records.");
    }

    synchronized (m_localSRVRecords) {
      records = (DsSRVWrapper[]) m_localSRVRecords.get(host);
    }
    return records;
  }

  public DsSRVWrapper[] wrapRecords(Attribute attr, int protocol) {
    if (attr == null) {
      return null;
    }

    DsSRVWrapper ret_records[] = new DsSRVWrapper[attr.size()];
    int i = 0;
    try {
      NamingEnumeration values = attr.getAll();
      while (values.hasMore()) {
        ret_records[i++] = new DsStringSRVWrapper((String) values.next(), protocol);
      }
    } catch (Exception e) {
      //      setFailureException(
      //          new DsProxyDnsException(
      //              DsProxyDnsException.QUERY_TYPE_SRV, null, e, m_currentBindingInfo));
      ret_records = null;
    }

    return ret_records;
  }

  public boolean isSupported(int transport) {
    return DsSipResolverUtils.isSupported(transport, m_supportedTransports, m_sizeExceedsMTU);
  }

  private void add(DsBindingInfo info) {
    if (isSupported(info.getTransport())) {
      m_bindingInfo.add(info);
    }
  }

  private void add(
      InetAddress localAddr, int localPort, InetAddress remoteAddr, int remotePort, int transport) {
    if (isSupported(transport)) {
      m_bindingInfo.add(new DsBindingInfo(localAddr, localPort, remoteAddr, remotePort, transport));
    }
  }

  // unused add methods, but we may want them in the future
  //    private void add(InetAddress addr, int port, int transport)
  //    {
  //        if (isSupported(transport))
  //        {
  //            m_bindingInfo.add(new DsBindingInfo(addr, port, transport));
  //        }
  //    }

  //    private void add(String addr, int port, int transport)
  //    {
  //        if (isSupported(transport))
  //        {
  //            m_bindingInfo.add(new DsBindingInfo(addr, port, transport));
  //        }
  //    }

  private void add(InetAddress localAddr, int localPort, String addr, int port, int transport) {
    if (isSupported(transport)) {
      m_bindingInfo.add(new DsBindingInfo(localAddr, localPort, addr, port, transport));
    }
  }

  /** Sets up the iterator. */
  private void start() {
    m_iterator = m_bindingInfo.iterator();
  }

  /**
   * Iterate through the binding infos, trying to connect until a TCP connection is established, UDP
   * binding info is found, or the end of the list is reached.
   *
   * @return the the DsSipConnection or null if no TCP connection could be established or no UDP
   *     binding info in list.
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsSipConnection tryConnect() throws DsException, IOException {
    DsSipConnection ret_connection = null;

    DsBindingInfo binfo = null;
    while (hasNextBindingInfo()) {
      binfo = getNextBindingInfo();
      // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
      // there is possibility that hasNextBindingInfo() return true but
      // it is left out by getNextBindingInfo
      if (binfo == null) break;

      if (binfo.getRemoteAddress() == null) {
        throw new IOException("can't resolve address: " + binfo.getRemoteAddressStr());
      }

      ret_connection = m_connectionBarrier.connectTo(binfo);

      if (DsLog4j.resolvCat.isEnabled(Level.INFO)) {
        DsLog4j.resolvCat.log(
            Level.INFO,
            "DsSipServerLocator.tryConnect() searching binding info = "
                + binfo
                + " ret_connection == "
                + ret_connection);
      }

      if (ret_connection != null) {
        break;
      }

      // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
      DsUnreachableDestinationTable.getInstance()
          .add(binfo.getRemoteAddress(), binfo.getRemotePort(), binfo.getTransport());
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(
          Level.DEBUG,
          "DsSipServerLocator.tryConnect() returning binding info = "
              + binfo
              + " ret_connection == "
              + ret_connection);
    }

    return ret_connection;
  }

  /**
   * Set this connection factory to the one specified.
   *
   * @param factory the factory used to generate connections
   */
  public static synchronized void setConnectionFactory(DsConnectionFactory factory) {
    m_connectionBarrier.setConnectionFactory(factory);
  }

  /**
   * Set this connection table to the one specified.
   *
   * @param table the table used to hold connections
   */
  public static synchronized void setConnectionTable(DsConnectionTable table) {
    m_connectionBarrier.setConnectionTable(table);
  }

  synchronized void setSupportedTransports(Set supported_transports) {
    if (supported_transports != null) {
      m_supportedTransports = DsSipResolverUtils.getMaskedTransports(supported_transports);
    }
  }

  public synchronized void setSupportedTransports(byte supported_transports) {
    m_supportedTransports = supported_transports;
  }

  /** Prints some debug information to stdout. */
  public void dump() {
    Iterator it = m_bindingInfo.iterator();
    System.out.println("----------------------------------------");
    while (it.hasNext()) {
      System.out.println("DsSipServerLocator.dump: " + (DsBindingInfo) it.next());
    }
    System.out.println("----------------------------------------");
  }

  /** Debugging of the local mapping SRV records */
  public static String dumpLocalSRVRecords() {

    if (m_localSRVRecords == null) return null;

    StringBuffer sb = new StringBuffer();

    Iterator iter = m_localSRVRecords.keySet().iterator();
    while (iter.hasNext()) {
      String key = (String) iter.next();
      DsSRVWrapper[] records = (DsSRVWrapper[]) m_localSRVRecords.get(key);
      sb.append("\n\nSRV key = " + key);
      for (int i = 0; i < records.length; i++) {
        sb.append("\nrecord = " + records[i].toString());
      }
    }

    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "Local mapping SRV records: " + sb.toString());
    }

    return sb.toString();
  }

  // /////////////////////////////////////////////////////
  // ///////// Debug Data    /////////////////////////////
  // /////////////////////////////////////////////////////

  private static String debugNAPTR[] = {
    "dynamicsoft.com 100 IN NAPTR 1 50 s SIP+D2U \"\"  _sip._udp.dynamicsoft.com",
    "dynamicsoft.com 100 IN NAPTR 2 49 s SIP+D2T \"\"  _sip._tcp.dynamicsoft.com",
    "dynamicsoft.com 100 IN NAPTR 3 48 s SIPS+D2T \"\"  _sips._tcp.dynamicsoft.com"
  };

  static String debugUDPRecords[] = {"0 1 5062 gardner", "0 1 5063 gardner"};
  static String debugTCPRecords[] = {"1 10 5560 leyland", "1 1 5561 leyland.dynamicsoft.com"};

  private static DsNAPTRRecord[] getDebugNAPTRRecords() {
    DsNAPTRRecord[] records = new DsNAPTRRecord[debugNAPTR.length];
    for (int i = 0; i < debugNAPTR.length; i++) {
      records[i] = DsNAPTRRecord.getRecord(debugNAPTR[i]);
    }
    return records;
  }

  private static DsSRVWrapper[] doSRVDebugQuery(String host, int proto) {
    DsSRVWrapper[] records = null;

    // maivu - 05.22.07 - CSCsi98575 - Add Local SRV configuration functionality instead of DNS
    records = doLocalSRVLookup(host, proto);
    // Found the host in the local configurations.
    if (records != null) {
      return records;
    } else {
      if (DsLog4j.resolvCat.isDebugEnabled()) {
        DsLog4j.resolvCat.debug(
            "No SRV Record in the local configurations matched for host: " + host);
      }
    }
    // Only do the remote DNS SRV lookup if
    // 1. there is NO local mapping
    // OR
    // 2. local mapping return no host found AND the flag useLocalOnly is
    // false
    if ((m_localSRVRecords == null) || (!DsSipServerLocator.m_useLocalSRVLookupOnly)) {
      // Use the debugging hardcoded data
      switch (proto) {
        case (DsSipTransportType.TCP):
        case (DsSipTransportType.TLS):
          records = new DsSRVWrapper[debugTCPRecords.length];
          for (int i = 0; i < records.length; ++i) {
            records[i] = new DsStringSRVWrapper(debugTCPRecords[i], proto);
          }
          break;
        case (DsSipTransportType.UDP):
          records = new DsSRVWrapper[debugUDPRecords.length];
          for (int i = 0; i < records.length; ++i) {
            records[i] = new DsStringSRVWrapper(debugUDPRecords[i], proto);
          }
          break;
        default:
          break;
      }
    }
    return records;
  }

  public static void setM_useDsUnreachableTable(boolean m_useDsUnreachableTable) {
    DsSipServerLocator.m_useDsUnreachableTable = m_useDsUnreachableTable;
  }

  public static boolean isM_useDsUnreachableTable() {
    return m_useDsUnreachableTable;
  }

  public Exception getFailureException() {
    return failureException;
  }

  public void setFailureException(Exception failureException) {
    this.failureException = failureException;
  }

  // //////// End Debug ////////////////////////////////

  // public static void main(String [] args)
  // {
  //    setUseNAPTR(true);
  // }
  //// public static void main(String [] args)
  //    {
  //        int i = 100;
  //        while (i-- >= 0)
  //            try
  //            {
  //                System.out.println("\n\n\n\n");
  //                DsSipServerLocator l1 = new DsSipDetServerLocator();
  //                System.out.println("Deterministic");
  //                l1.initialize("foo.com", 0, 0);
  //                l1.dump();
  //                DsSipServerLocator l2 = new DsSipServerLocator();
  //                l2.initialize("foo.com", 0, 0);
  //                l2.dump();
  //            } catch (Exception exc){exc.printStackTrace();}
  //    }
  //

  //    public static void main(String [] args)
  //    {
  //        DsSipServerLocator l = new DsSipServerLocator();
  //        String host = "192.168.2.123";
  //        String url = "sip:leyland.dynamicsoft.com";
  //        try
  //        {
  //            java.net.InetAddress ia = java.net.InetAddress.getByName(host);
  //            //System.out.println("IP = " + ia.toString());
  //
  //            //System.out.println("TCP");
  //            Record [] records = dns.getSRVRecords("_sip._tcp." + host,
  //                    Type.SRV);
  //            if (records != null)
  //                for (int j=0; j < records.length; j++)
  //                {
  //                    records[j].toString();
  //                }
  //            //System.out.println("UDP");
  //            records = dns.getSRVRecords("_sip._udp." + host,
  //                    Type.SRV);
  //            for (int k=0; k < records.length; k++)
  //            {
  //                records[k].toString();
  //            }
  //        }
  //        catch (Exception exc)
  //        {
  //            exc.printStackTrace();
  //        }
  //
  //    }
  //

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            DsLog4j.initialize();
  //            DsSipURL url = new DsSipURL(new DsByteString(args[0]));
  //            DsSipServerLocator.m_staticSupportedTransports = 1 | 2 | 4 | 8;// | 16 ;
  //            DsSipServerLocator.m_debugMode = true;
  //            DsSipServerLocator.m_useSRV = true;
  //            DsSipServerLocator.m_NAPTREnabled = false;
  //            DsSipServerLocator loc = new DsSipServerLocator(url);
  //            loc.dump();
  //        }
  //        catch (Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }
  //
}
