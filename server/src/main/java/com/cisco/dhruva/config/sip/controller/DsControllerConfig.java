/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.config.sip.controller;

import com.cisco.dhruva.config.sip.RE;
import com.cisco.dhruva.sip.DsUtil.DsReConstants;
import com.cisco.dhruva.sip.DsUtil.ListenIf;
import com.cisco.dhruva.sip.DsUtil.ViaListenIf;
import com.cisco.dhruva.sip.controller.DsProxyController;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.controller.exceptions.DsSipHostNotValidException;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipResolverUtils;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerLocator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerLocatorFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerNotFoundException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.IPValidator;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public final class DsControllerConfig
    implements DsProxyParamsInterface, DsSipRouteFixInterface, Cloneable {

  public static final byte UDP = (byte) DsSipTransportType.UDP;
  public static final byte SIGCOMP = 100; // (byte)DsSipTransportType.SIGCOMP;
  public static final byte TCP = (byte) DsSipTransportType.TCP;
  public static final byte NONE = (byte) DsSipTransportType.NONE;
  public static final byte MULTICAST = (byte) DsSipTransportType.MULTICAST;
  public static final byte TLS = (byte) DsSipTransportType.TLS;
  public static final int DEFAULT_TIMEOUT = -1;

  public static final byte STATEFUL = (byte) RE.index_dsReStateMode_stateful;
  public static final byte STATELESS = (byte) RE.index_dsReStateMode_stateless;
  public static final byte FAILOVER_STATEFUL = (byte) RE.index_dsReStateMode_failover_stateful;

  public static final String INBOUND =
      "inbound"; // TODO both these values are bogus and are slated for removal
  public static final String OUTBOUND =
      "outbound"; // TODO both these values are bogus and are slated for removal

  public static final String LISTEN_INTERNAL =
      "internal"; // TODO both these values are bogus and are slated for removal
  public static final String LISTEN_EXTERNAL =
      "external"; // TODO both these values are bogus and are slated for removal

  public static final short MASK_VIA = 1;

  public static final byte FAILOVER = 0;
  public static final byte DROP = 1;

  public static final String FAILOVER_TOKEN = "failover";
  public static final String DROP_TOKEN = "drop";

  protected static HashMap onNextHopFailureMap = null;
  protected byte stateMode =
      (byte) RE.getValidValueAsInt(RE.dsReStateMode, RE.dsReStateModeDefault);
  protected static Logger Log = DhruvaLoggerFactory.getLogger(DsControllerConfig.class);

  // next hop failure vaiues
  public static final byte NHF_ACTION_DROP = 8; // NHF stands for Next hop Failure :)
  public static final byte NHF_ACTION_FAILOVER = 9;

  protected byte nextHopFailureAction = NHF_ACTION_FAILOVER;
  // the interval between two consecutive Timer cleanups of the
  // above hashmap. also, the time after which a particular entry
  // in the above callLegToNextHopMap should be removed. (milliseconds)
  private static int INTERVAL = 64000;

  protected byte searchType = DsProxyController.SEARCH_PARALLEL;

  /**
   * Holds the domain names that indicate that a request URI/Route header was inserted by this RE
   */
  protected LinkedList<DsByteString> ourRoutes = new LinkedList<>();

  protected HashMap<DsByteString, LinkedList<DsByteString>> popNames =
      new HashMap<DsByteString, LinkedList<DsByteString>>();

  // TODO optimize below, we don't need the objects created even before configured
  protected HashMap NetworkIf = new HashMap<>();
  protected HashMap NetworkIfMap = new HashMap<>();

  protected HashMap<Integer, PathObj> PathIf = new HashMap<Integer, PathObj>();
  protected HashMap PathIfMap = new HashMap<>();

  protected HashMap ViaIf = new HashMap<>();
  protected HashMap ViaIfMap = new HashMap<>();

  protected HashMap ViaListenHash = new HashMap<>();

  protected HashMap RecordRouteIf = new HashMap();
  protected HashMap RecordRouteIfMap = new HashMap();

  protected HashMap<String, MaskObj> MaskIfMap = new HashMap<>();

  protected HashMap<ListenIf, ListenIf> listenIf = new HashMap<ListenIf, ListenIf>();

  protected DsSipPathHeader path = null;

  protected HashMap<String, DsSipRecordRouteHeader> recordRoutesMap = new HashMap<>();

  protected static DsControllerConfig currentConfig;
  protected static boolean updated = false;

  // MEETPASS Adding by default
  protected boolean doRecordRoute = true;

  protected boolean isRecursing = false;

  protected int defaultRetryAfterMilliSeconds = 0;

  /** default constructor is protected. It's only used from clone() method. */
  static {
    currentConfig = new DsControllerConfig();
  }

  private Transport defaultProtocol;

  private DsSipServerLocatorFactory dsSIPServerLocatorFactory =
      DsSipServerLocatorFactory.getInstance();
  /** Our Constructor */
  private DsControllerConfig() {}

  /**
   * This is the only way to get access to current configuration settings.
   *
   * @return the DsControllerConfig object containing the settings at the moment this method was
   *     called.
   */
  public static synchronized DsControllerConfig getCurrent() {
    updated = false;
    return currentConfig;
  }

  /** @return state-mode in int value */
  public byte getStateMode() {
    return stateMode;
  }

  /** getter for the Next hop failure action */
  public synchronized byte getNextHopFailureAction() {
    return currentConfig.nextHopFailureAction;
  }

  /** @return if true, the proxy will operate in stateful mode be default. */
  public boolean isStateful() {
    return (currentConfig.stateMode == STATEFUL);
  }

  // TODO
  @Override
  public int getDefaultPort() {
    return 0;
  }

  public byte getSearchType() {
    return searchType;
  }

  /**
   * Fetches the record-route interface based on the specified direction
   *
   * @param direction The direction in which we need to get the record route interface.
   * @return record route interface based on the specified direction or null if does not exist.
   */
  public DsSipRecordRouteHeader getRecordRouteInterface(String direction) {
    return getRecordRouteInterface(direction, true);
  }

  @Override
  public DsSipPathHeader getPathInterface(int protocol, String direction) {
    return null;
  }

  public static int getTimerInterval() {
    return INTERVAL;
  }

  public DsSipRecordRouteHeader getRecordRouteInterface(String direction, boolean clone) {
    Log.debug("recordRoutesMap contains :\n" + recordRoutesMap.toString() + '\n');
    DsSipRecordRouteHeader rrHeader = (DsSipRecordRouteHeader) recordRoutesMap.get(direction);
    if (rrHeader != null && clone) {
      rrHeader = (DsSipRecordRouteHeader) rrHeader.clone();
    }
    Log.debug("Leaving getRecordRouteInterface() returning: " + rrHeader);
    return rrHeader;
  }

  /** normalizes the protocol value to either UDP, TCP */
  public static byte normalizedProtocol(int protocol) {
    if ((protocol != DsControllerConfig.TCP) && (protocol != DsControllerConfig.TLS)) {
      return DsControllerConfig.UDP;
    }

    return (byte) protocol;
  }

  public ViaObj getVia(Transport transport, DsNetwork direction) {
    HashMap viaDirMap = (HashMap) ViaIfMap.get(direction);
    if (viaDirMap != null) {
      return (ViaObj) viaDirMap.get(new Integer(String.valueOf(transport)));
    }
    return null;
  }

  public int getDefaultRetryAfterMilliSeconds() {
    return defaultRetryAfterMilliSeconds;
  }

  public boolean isRecursing() {
    return isRecursing;
  }

  /**
   * @param protocol UDP or TCP
   * @param direction
   * @return the address and port number that needs to be inserted into the Via header for a
   *     specific protocol used
   */
  // TODO

  public DsViaListenInterface getViaInterface(Transport protocol, String direction) {

    DsNetwork net;
    // Grab the via interface if it has already been stored by protocol and direction
    DsViaListenInterface viaIf;
    try {
      net = DsNetwork.getNetwork(direction);
    } catch (DsException ex) {
      Log.error("exception getting network {}", direction);
      return null;
    }
    viaIf = getVia(protocol, net);
    if (viaIf == null) {
      viaIf = (DsViaListenInterface) ViaListenHash.get(protocol.getValue());
    }
    if (viaIf == null) {

      Log.info("No via interface stored for this protocol/direction pair, creating one");

      // Find a listen if with the same protocol and direction, if there is more
      // than one the first on will be selected.

      DsListenInterface tempInterface = getInterface(protocol, net);
      if (tempInterface != null) {
        try {
          viaIf =
              new ViaListenIf(
                  tempInterface.getPort(),
                  tempInterface.getProtocol(),
                  tempInterface.getAddress(),
                  net,
                  -1,
                  null,
                  null,
                  null,
                  -1);
        } catch (UnknownHostException | DsException unhe) {
          Log.error("Couldn't create a new via interface", unhe);
          return null;
        }
        HashMap viaListenHashDir = (HashMap) ViaListenHash.get(direction);
        if (viaListenHashDir == null) {
          viaListenHashDir = new HashMap();
          ViaListenHash.put(direction, viaListenHashDir);
        }
        viaListenHashDir.put(protocol.getValue(), viaIf);
      }
    }

    Log.debug(
        "Leaving getViaInterface(+ "
            + protocol
            + ", "
            + direction
            + " ) with return value: "
            + viaIf);

    return viaIf;
  }

  /**
   * @return default protocol we are listening on (one of the constants defined in
   *     DsSipTransportType.java) //This is used in Record-Route, for example This is not really
   *     used by the proxy core anymore
   */
  @Override
  public Transport getDefaultProtocol() {
    return Transport.UDP;
  }

  /**
   * Specifies whether the proxy needs to insert itself into the Record-Route
   *
   * @return Record-Route setting
   */
  @Override
  public boolean doRecordRoute() {
    return doRecordRoute;
  }

  /**
   * Returns the address to proxy to
   *
   * @return the address to proxy to, null if the default forwarding logic is to be used
   */
  // TODO
  @Override
  public DsByteString getProxyToAddress() {
    return null;
  }

  /**
   * Returns port to proxy to
   *
   * @return the port to proxy to; if -1 is returned, default port will be used
   */
  // TODO
  @Override
  public int getProxyToPort() {
    return 0;
  }

  /** @return protocol to use for outgoing request */
  @Override
  public Transport getProxyToProtocol() {
    return Transport.NONE;
  }

  /**
   * @return the timeout value in milliseconds for outgoing requests. -1 means default timeout This
   *     allows to set timeout values that are _lower_ than SIP defaults. Values higher than SIP
   *     deafults will have no effect.
   */
  // TODO
  @Override
  public long getRequestTimeout() {
    return 0;
  }

  // TODO
  @Override
  public String getRequestDirection() {
    return null;
  }

  // TODO
  @Override
  public DsByteString getRecordRouteUserParams() {
    return null;
  }

  public DsNetwork getDefaultNetwork() {
    return null;
  }

  /**
   * This method is invoked by the DsSipRequest to perform the procedures necessary to interoperate
   * with strict routers. For incoming requests, the class which implements this interface is first
   * asked to recognize the request URI. If the request URI is recognized, it is saved internally by
   * the invoking DsSipRequest as the LRFIX URI and replaced by the URI of the bottom Route header.
   * If the request URI is not recognized, the supplied interface is asked to recognize the URI of
   * the top Route header. If the top Route header's URI is recognized, it is removed and saved
   * internally as the LRFIX URI. If neither is recognized, the DsSipRequest's FIX URI is set to
   * null.
   *
   * @param uri a URI from the SIP request as described above
   * @param isRequestURI boolean to indicate whether the uri is a request-uri
   * @return <code>true</code> if the uri is recognized as a uri that was inserted into a
   *     Record-Route header, otherwise returns <code>false</code>
   */
  /**
   * This method is invoked by the DsSipRequest to perform the procedures necessary to interoperate
   * with strict routers. For incoming requests, the class which implements this interface is first
   * asked to recognize the request URI. If the request URI is recognized, it is saved internally by
   * the invoking DsSipRequest as the LRFIX URI and replaced by the URI of the bottom Route header.
   * If the request URI is not recognized, the supplied interface is asked to recognize the URI of
   * the top Route header. If the top Route header's URI is recognized, it is removed and saved
   * internally as the LRFIX URI. If neither is recognized, the DsSipRequest's FIX URI is set to
   * null.
   *
   * @param uri a URI from the SIP request as described above
   * @param isRequestURI true if the uri is the request-URI else false
   * @return true if the uri is recognized as a uri that was inserted into a Record-Route header,
   *     otherwise returns false
   */
  public boolean recognize(DsURI uri, boolean isRequestURI) {
    boolean b = false;
    if (uri.isSipURL()) {
      DsSipURL url = (DsSipURL) uri;

      DsByteString host = null;
      int port = url.getPort();

      Transport transport = url.getTransportParam();
      if (transport == Transport.NONE) transport = Transport.UDP;

      DsByteString user = url.getUser();

      if (isRequestURI) {
        host = url.getHost();
        b = (null != checkRecordRoutes(user, host, port, transport));
        if (b) Log.debug("request-uri matches with one of Record-Route interfaces");
      } else {
        host = url.getMAddrParam();
        if (host == null) host = url.getHost();
        b = recognize(user, host, port, transport);
      }
    }
    Log.debug("Leaving recognize(), returning " + b);
    return b;
  }

  public boolean recognize(DsByteString user, DsByteString host, int port, Transport transport) {
    // Check Record-Route
    Log.debug("Checking Record-Route interfaces");

    if (null != checkRecordRoutes(user, host, port, transport)) return true;

    Log.debug("Checking listen interfaces");
    ArrayList<ListenIf> listenList = getListenPorts();
    for (ListenIf anIf : listenList) {
      if (isMyRoute(host, port, transport, anIf)) return true;
    }
    // Check popid
    return false;
  }

  /*
   * Checks if the passed host,transport,port,user info is recognized.
   * If host is Domain name , this methods first resolves the domain to IP and then
   * checks if its recognized.This method only works for SRV
   *
   */

  public boolean recognize(
      DsByteString user,
      DsByteString host,
      int port,
      Transport transport,
      DsNetwork network,
      boolean fallBackToAQuery)
      throws UnknownHostException, DsSipHostNotValidException, DsSipServerNotFoundException {

    // check for IP and cases where host matches aliases.
    if (recognize(user, host, port, transport)) return true;

    if (!IPValidator.hostIsIPAddr(host.toString())) {
      DsSipServerLocator dnsResolver = dsSIPServerLocatorFactory.createNewSIPServerLocator();
      DsSipServerLocator.setAQuery(fallBackToAQuery);
      try {
        dnsResolver.initialize(
            network, null, DsSipResolverUtils.LPU, host.toString(), port, transport, false);
        boolean isDNSResolverEmpty = true;
        while (dnsResolver.hasNextBindingInfo()) {
          isDNSResolverEmpty = false;
          DsBindingInfo bindingInfo = dnsResolver.getNextBindingInfo();
          if (recognize(
              user,
              new DsByteString(bindingInfo.getRemoteAddressStr()),
              bindingInfo.getRemotePort(),
              bindingInfo.getTransport())) {
            return true;
          }
        }
        if (isDNSResolverEmpty) {
          throw new DsSipHostNotValidException(dnsResolver.getFailureException());
        }
      } catch (UnknownHostException | DsSipHostNotValidException | DsSipServerNotFoundException e) {
        Log.error("Exception in resolving " + host, e);
        throw e;
      }
    }
    return false;
  }

  public String checkRecordRoutes(
      DsByteString user, DsByteString host, int port, Transport transport) {
    if (user != null) {
      String usr = user.toString();
      if (usr.startsWith(DsReConstants.RR_TOKEN)
          || usr.endsWith(DsReConstants.RR_TOKEN1)
          || usr.contains(DsReConstants.RR_TOKEN2)) {
        Set rrs = recordRoutesMap.keySet();
        String key;
        for (Object o : rrs) {
          key = (String) o;
          DsSipRecordRouteHeader rr = recordRoutesMap.get(key);
          if (rr != null) {
            if (DsProxyUtils.recognize(host, port, transport, (DsSipURL) rr.getURI())) return key;
          }
        }
      }
    }
    return null;
  }

  public static synchronized void addListenInterface(
      DsNetwork direction,
      InetAddress address,
      int port,
      Transport protocol,
      InetAddress translatedAddress)
      throws DsInconsistentConfigurationException, DsException, IOException {

    ListenIf newInterface =
        new ListenIf(
            port,
            protocol,
            new DsByteString(address.getHostAddress()),
            address,
            direction,
            new DsByteString(translatedAddress.getHostAddress()),
            translatedAddress,
            0);

    if (currentConfig.listenIf.containsKey(newInterface))
      throw new DsInconsistentConfigurationException("Entry already exists");

    currentConfig.defaultProtocol = Transport.UDP;

    // Store the interface in two different HashMaps.  One is indexed by index (SNMP),
    // while the other is by interface.  The second allows faster checks at add time
    // to ensure that we aren't adding the same interface twice.
    currentConfig.listenIf.put(newInterface, newInterface);

    // currentConfig.listenHash.put(newInterface, new Integer(index));
    Log.debug(
        "addListenInterface() - New list of interfaces we are listening on is: "
            + currentConfig.listenIf.keySet());
  }

  /** @return returns the <CODE>ArrayList</CODE> list containing <CODE>ListenIf</CODE> Objects */
  public ArrayList<ListenIf> getListenPorts() {
    return new ArrayList<>(listenIf.values());
  }

  public static synchronized void addRecordRouteInterface(
      InetAddress ipAddress, int port, Transport protocol, DsNetwork direction) throws Exception {

    currentConfig.doRecordRoute = true;

    // SimpleListenIf listenIf = new SimpleListenIf(port, protocol, interfaceIP, direction );
    // ListenIf newListenIf = new ListenIf( port, protocol, interfaceIP, direction, null, 0);
    // we will not verify the ip address for hostname of Record Route. Bug #4349.
    DsSipURL sipURL = null;

    /* check if an IPAddress was passed if yes check translated ip address
    if domain is passed do not pass convert translated IPAddress for RR
    since the FQDN would resolve to external IP

    TODO:
    We are not validating the hostname with our interface IP's ,we are blindly trusting
    the configuration. This could be changed to resolve and compare the hostname against our
    interface so that we don't allow invalid hostname
    */

    ListenIf listenIf =
        (ListenIf) DsControllerConfig.getCurrent().getInterface(ipAddress, protocol, port);
    DsByteString translatedIp = null;
    if (listenIf != null) translatedIp = listenIf.getTranslatedAddress();

    if (translatedIp != null) {
      sipURL = new DsSipURL(DsByteString.BS_EMPTY_STRING, translatedIp);
    }

    if (sipURL == null) {
      sipURL =
          new DsSipURL(DsByteString.BS_EMPTY_STRING, new DsByteString(ipAddress.getHostAddress()));
    }

    if (port > 0) sipURL.setPort(port);
    if (protocol != Transport.NONE) sipURL.setTransportParam(protocol);
    sipURL.setLRParam();
    DsSipRecordRouteHeader rr = new DsSipRecordRouteHeader(sipURL);

    currentConfig.recordRoutesMap.put(direction.getName(), rr);

    Log.info("Setting record route(" + rr + ") on network: " + direction);
  }

  /*
   * Implementation of the corresponding DsProxyParamsInterface method.  Returns
   * the first interface in our hashmap that is useing the specified protocol.
   */
  public DsListenInterface getInterface(Transport protocol, DsNetwork direction) {
    for (ListenIf li : listenIf.values()) {
      if (li.getProtocol() == protocol && li.getNetwork().equals(direction)) {
        return li;
      }
    }
    return null; // nothing is found
  }

  /**
   * Returns the interface stored by the config that has the address, port and protocol passed in.
   * If no interface is found, then null is returned.
   */
  public DsListenInterface getInterface(InetAddress address, Transport prot, int port) {

    ListenIf lookupIf = new ListenIf(port, prot, address);
    return currentConfig.listenIf.get(lookupIf);
  }

  public static boolean isMyRoute(
      DsByteString routeHost, int routePort, Transport routeTransport, ListenIf myIF) {
    boolean match = false;
    if (myIF != null) {
      if (routeHost.equals(myIF.getAddress())) {
        if (routePort == myIF.getPort()) {
          if (routeTransport == myIF.getProtocol()) {
            match = true;
          }
        }
      }
    }
    return match;
  }

  // TODO check this logic
  public boolean isMasked(String direction, int hdr) {
    boolean val = false;
    MaskObj mask = MaskIfMap.get(direction);
    if (mask != null) val = mask.isHeaderSet(hdr);
    return val;
  }

  public boolean isMaskingEnabled(String direction) {
    boolean val = false;
    MaskObj mask = MaskIfMap.get(direction);
    if (mask != null) val = mask.isHeaderMaskingEnabled();
    return val;
  }
}
