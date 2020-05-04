/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.configs;

import com.cisco.dhruva.sip.re.controllers.DsProxyController;
import com.cisco.dhruva.sip.re.util.ListenIf;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsListenInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyParamsInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyUtils;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsViaListenInterface;
import com.cisco.dhruva.sip.rep.re.configs.RE;
import com.cisco.dhruva.sip.rep.re.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.rep.re.util.DsReConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPathHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRouteFixInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipServiceRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public final class DsControllerConfig
    implements DsProxyParamsInterface, DsSipRouteFixInterface, Cloneable {

  /* The following two variables are used for dynamic
   * reconfiguration.
   * currentConfig refers to the most current copy
   * of the configuration
   * updated  true when some configuration settings
   * have been changed since the last getCurrent()
   */
  protected static DsControllerConfig currentConfig = null;
  protected static boolean updated = false;
  private static Logger Log = DhruvaLoggerFactory.getLogger(DsControllerConfig.class);

  // Default state is:
  // stateful with request-uri load balancing and no record route
  protected byte searchType = DsProxyController.SEARCH_PARALLEL;

  protected boolean doRecordRoute;
  protected boolean doAddServiceRoute;
  protected boolean doModifyServiceRoute;

  protected byte stateMode =
      (byte) RE.getValidValueAsInt(RE.dsReStateMode, RE.dsReStateModeDefault);

  protected boolean isRecursing;

  protected int defaultRetryAfterMilliSeconds = 0;

  public static final byte STATEFUL = (byte) RE.index_dsReStateMode_stateful;
  public static final byte STATELESS = (byte) RE.index_dsReStateMode_stateless;
  public static final byte FAILOVER_STATEFUL = (byte) RE.index_dsReStateMode_failover_stateful;

  // next hop failure vaiues
  public static final byte NHF_ACTION_DROP = 8; // NHF stands for Next hop Failure :)
  public static final byte NHF_ACTION_FAILOVER = 9;

  protected HashMap MaskIfMap = new HashMap();

  protected byte nextHopFailureAction = NHF_ACTION_FAILOVER;

  protected HashMap listenIf = new HashMap();
  protected HashMap recordRoutesMap = new HashMap();

  protected HashMap PathIf = new HashMap();
  protected HashMap PathIfMap = new HashMap();

  protected HashMap AddServiceRouteIf = new HashMap();
  protected HashMap AddServiceRouteIfMap = new HashMap();

  protected LinkedList ourRoutes = new LinkedList();
  protected HashMap popNames = new HashMap();

  protected HashMap ModifyServiceRouteIf = new HashMap();
  protected HashMap ModifyServiceRouteIfMap = new HashMap();

  public static final String LISTEN_INTERNAL = "internal";

  /** default constructor is protected. It's only used from clone() method. */
  static {
    currentConfig = new DsControllerConfig();
  }

  private Transport defaultProtocol;

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

  /**
   * Returns the current search mechanism being used.
   *
   * @return The current search mechanism being used; DsProxyController.SEARCH_HIGHEST,
   *     DsProxyController.SEARCH_PARALLEL or DsProxyController.SEARCH_SEQUENTIAL.
   */
  public byte getSearchType() {
    return searchType;
  }

  /** @return state-mode in int value */
  public byte getStateMode() {
    return stateMode;
  }

  /*
   * Returns true if we are recursing on 3xx responses
   * @return True if we are recursing on 3xx response, false otherwise
   */
  public boolean isRecursing() {
    return isRecursing;
  }

  public int getDefaultRetryAfterMilliSeconds() {
    return defaultRetryAfterMilliSeconds;
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

  /**
   * Fetches the record-route interface based on the specified direction
   *
   * @param direction The direction in which we need to get the record route interface.
   * @return record route interface based on the specified direction or null if does not exist.
   */
  public DsSipRecordRouteHeader getRecordRouteInterface(String direction) {
    return getRecordRouteInterface(direction, true);
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
  /**
   * Get the interface to be inserted into Path headers. If the port of that interface is -1, no
   * port will be used in the Path. If the transport is no transport parameter will be used the the
   * Path.
   *
   * @param transport
   * @param direction
   * @return the interface to be inserted into a Path header, or <code>null</code> if no Path header
   *     should be used
   */
  @Override
  public DsSipPathHeader getPathInterface(Transport transport, String direction) {
    PathObj path = getPath(transport, direction);
    if (path != null) {
      return path.getPathHeader();
    }
    return null;
  }

  public boolean doAddServiceRoute() {
    return doAddServiceRoute;
  }

  /**
   * @param protocol UDP or TCP
   * @param direction
   * @return the address and port number that needs to be inserted into the Via header for a
   *     specific protocol used
   */
  // TODO
  @Override
  public DsViaListenInterface getViaInterface(Transport protocol, String direction) {
    return null;
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
    ArrayList listenList = getListenPorts();
    if (listenList != null) {
      for (int i = 0; i < listenList.size(); i++) {
        if (isMyRoute(host, port, transport, (ListenIf) listenList.get(i))) return true;
      }
    }

    // Check popid
    Log.debug("Checking pop-ids");
    if (ourRoutes.contains(host.toLowerCase())) return true;

    /*  //Check Path
    // graivitt Commenting as its not used now
    Log.debug("Checking Path interface");
    if (null != checkPaths(user, host, port, transport))
      return true;*/

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
        for (Iterator i = rrs.iterator(); i.hasNext(); ) {
          key = (String) i.next();
          DsSipRecordRouteHeader rr = (DsSipRecordRouteHeader) recordRoutesMap.get(key);
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

  public static synchronized void addServiceRouteAdd(
      int index,
      DsNetwork direction,
      String ServiceRouteAddress,
      int ServiceRoutePort,
      int ServiceRouteTransport,
      int ServiceRouteSequence,
      String ServiceRouteParams)
      throws DsException {

    ArrayList svcRouteDirList = (ArrayList) currentConfig.AddServiceRouteIfMap.get(direction);
    if (svcRouteDirList == null) {
      svcRouteDirList = new ArrayList();
      currentConfig.AddServiceRouteIfMap.put(direction, svcRouteDirList);
    }
    currentConfig.doAddServiceRoute = true;
    ServiceRouteObj svcRoute =
        new ServiceRouteObj(
            direction,
            ServiceRouteAddress,
            ServiceRoutePort,
            ServiceRouteTransport,
            ServiceRouteSequence,
            ServiceRouteParams);
    int seq = svcRoute.getServiceRouteSequence();
    if (seq >= svcRouteDirList.size()) svcRouteDirList.add(svcRoute);
    else svcRouteDirList.add(seq, svcRoute);
    currentConfig.AddServiceRouteIf.put(new Integer(index), svcRoute);
  }

  public ArrayList getServiceRouteAdd(String direction) {
    return (ArrayList) AddServiceRouteIfMap.get(direction);
  }

  /** @return returns the <CODE>ArrayList</CODE> list containing <CODE>ListenIf</CODE> Objects */
  public ArrayList getListenPorts() {
    return new ArrayList(listenIf.values());
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

    currentConfig.recordRoutesMap.put(direction, rr);

    Log.info("Setting record route(" + rr + ") on network: " + direction);
  }

  public static synchronized void addPath(
      int index, DsNetwork direction, String PathAddress, int PathPort, int PathTransport)
      throws DsException {
    HashMap pathDirMap = (HashMap) currentConfig.PathIfMap.get(direction);
    if (pathDirMap == null) {
      pathDirMap = new HashMap();
      currentConfig.PathIfMap.put(direction, pathDirMap);
    }
    PathObj path = new PathObj(direction, PathAddress, PathPort, PathTransport);
    pathDirMap.put(new Integer(PathTransport), path);
    currentConfig.PathIf.put(new Integer(index), path);
  }

  public static synchronized void removePath(int index) {
    Integer i = new Integer(index);
    PathObj path = (PathObj) currentConfig.PathIf.get(i);
    if (path != null) {
      HashMap pathDirMap = (HashMap) currentConfig.PathIfMap.get(path.getDirection());
      if (pathDirMap != null) {
        pathDirMap.remove(new Integer(path.getPathTransport()));
        if (pathDirMap.isEmpty()) {
          currentConfig.PathIfMap.remove(path.getDirection());
        }
      }
      currentConfig.PathIf.remove(i);
    }
  }

  public PathObj getPath(int index) {
    return (PathObj) PathIf.get(new Integer(index));
  }

  public PathObj getPath(Transport transport, String direction) {
    HashMap pathDirMap = (HashMap) PathIfMap.get(direction);
    if (pathDirMap != null) {
      return (PathObj) pathDirMap.get(transport);
    }
    return null;
  }

  public boolean isPathSet() {
    return !PathIfMap.isEmpty();
  }

  public String checkPaths(DsByteString user, DsByteString host, int port, Transport transport) {
    if (user != null) {
      String usr = user.toString();
      if (usr.startsWith(DsReConstants.PR_TOKEN)
          || usr.endsWith(DsReConstants.PR_TOKEN1)
          || usr.contains(DsReConstants.PR_TOKEN2)) {

        Set ps = PathIfMap.keySet();
        String key;
        for (Iterator i = ps.iterator(); i.hasNext(); ) {
          key = (String) i.next();
          HashMap map = (HashMap) PathIfMap.get(key);
          if (map != null) {
            PathObj path = (PathObj) map.get(transport);
            if (path != null) {
              if (DsProxyUtils.recognize(
                  host, port, transport, (DsSipURL) path.getPathHeader().getURI())) return key;
            }
          }
        }
      }
    }
    return null;
  }

  /*
   * Implementation of the corresponding DsProxyParamsInterface method.  Returns
   * the first interface in our hashmap that is useing the specified protocol.
   */
  public DsListenInterface getInterface(Transport protocol, DsNetwork direction) {
    Iterator listenEntries = listenIf.values().iterator();
    while (listenEntries.hasNext()) {
      ListenIf li = (ListenIf) listenEntries.next();
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
    return (DsListenInterface) currentConfig.listenIf.get(lookupIf);
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

  public static synchronized void addPopName(String name, String value) {
    DsByteString bsName = (new DsByteString(name)).toLowerCase();

    LinkedList list = (LinkedList) currentConfig.popNames.get(bsName);
    if (list == null) list = new LinkedList();

    list.add((new DsByteString(value)).toLowerCase());

    currentConfig.popNames.put(bsName, list);

    if (name.equalsIgnoreCase("self")) {
      DsByteString byteDomain = (new DsByteString(value)).toLowerCase();
      currentConfig.ourRoutes.add(byteDomain);
    }
  }

  public static synchronized void addServiceRouteModify(
      int index,
      DsNetwork direction,
      String ServiceRouteAddress,
      int ServiceRoutePort,
      int ServiceRouteTransport)
      throws DsException {
    currentConfig.doModifyServiceRoute = true;
    ServiceRouteObj svcRoute =
        new ServiceRouteObj(
            direction, ServiceRouteAddress, ServiceRoutePort, ServiceRouteTransport);
    currentConfig.ModifyServiceRouteIfMap.put(direction, svcRoute);
    currentConfig.ModifyServiceRouteIf.put(new Integer(index), svcRoute);
  }

  public boolean doModifyServiceRoute() {
    return doModifyServiceRoute;
  }

  public DsSipServiceRouteHeader getServiceRouteInterface(String direction) {
    ServiceRouteObj svcRoute = (ServiceRouteObj) ModifyServiceRouteIfMap.get(direction);
    if (svcRoute != null) return svcRoute.getServiceRouteHeader();
    return null;
  }

  // TODO check this logic
  public boolean isMasked(String direction, int hdr) {
    boolean val = false;
    MaskObj mask = (MaskObj) MaskIfMap.get(direction);
    if (mask != null) val = mask.isHeaderSet(hdr);
    return val;
  }

  public boolean isMaskingEnabled(String direction) {
    boolean val = false;
    MaskObj mask = (MaskObj) MaskIfMap.get(direction);
    if (mask != null) val = mask.isHeaderMaskingEnabled();
    return val;
  }
}
