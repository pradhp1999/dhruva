/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.rep.re.util.DsReConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipClientTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipConnection;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipRequestInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipStrayMessageInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionEventInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransportLayer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPRACKMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRouteFixInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLTrustManager;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.net.InetAddress;
import java.util.StringTokenizer;

public class DsSipProxyManager
    implements DsSipRequestInterface, DsSipStrayMessageInterface, DsSipTransactionEventInterface {

  // data members

  private DsSipTransactionManager transManager = null;
  private DsSipTransportLayer sipTransportLayer;
  private DsControllerFactoryInterface controllerFactory = null;

  // local reference to viaHandler. Saved here so that I don't
  // have to call DsViaHadler.getInstance() every time I need a ViaHandler
  // (as if it're any different...)
  private DsViaHandler viaHandler;

  private static DsSipProxyManager m_Singleton = null;
  private static boolean initCalled = false;

  private DsStrayResponseInterface strayResponseInterface = null;
  private DsSSLTrustManager trustManager = null;

  public static final int OVERLOAD_NONE = 0;
  public static final int OVERLOAD_REJECT = 1;
  public static final int OVERLOAD_REDIRECT = 2;

  public static final int DEFAULT_EVENT_QUEUE_LENGTH = 300;
  public static final int DEFAULT_REQUEST_QUEUE_LENGTH = 300;

  public static final int EXTERNAL_NETWORK = 0;
  public static final int INTERNAL_NETWORK = 0;
  public static final String EXTERNAL_NETWORK_STR = "EXTERNAL";
  public static final String INTERNAL_NETWORK_STR = "INTERNAL";

  public static final int EXTERNAL_WIRELESS_UDP_NETWORK = 1;
  public static final String EXTERNAL_WIRELESS_UDP_NETWORK_STR = "EXTERNAL_UDP_WIRELESS";

  public static final int EXTERNAL_WIRELESS_SIGCOMP_NETWORK = 2;
  public static final String EXTERNAL_WIRELESS_SIGCOMP_NETWORK_STR = "EXTERNAL_SIGCOMP_WIRELESS";

  public static final int EXTERNAL_WIRELESS_TOKENIZED_SIP_NETWORK = 3;
  public static final String EXTERNAL_WIRELESS_TOKENIZED_SIP_NETWORK_STR =
      "EXTERNAL_TOKENIZED_SIP_WIRELESS";

  private static final boolean USE_CONNECTED_UDP =
      Boolean.getBoolean("com.cisco.DsLibs.DsSipProxyManager.TOKENSIP_CONNECTED_UDP");

  protected static Trace Log = Trace.getTrace(DsSipProxyManager.class.getName());

  private static Trace perfControllerFactoryLog = Trace.getTrace("perf.ControllerFactory");
  private static Trace perfProxyManagerLog = Trace.getTrace("perf.ProxyManager");

  // some private strings
  private static final DsByteString colon = new DsByteString(":");
  private static final DsByteString PING = new DsByteString("PING");
  // public methods

  /**
   * MUST be called exactly once before using the proxy library This creates DsViaHandler
   *
   * @param transport TransportLayer to be used by the ViaHandler's loop detection
   * @return true if completed succesfully, false if already initialized
   */
  public static boolean initProxyLib(DsSipTransportLayer transport) throws DsCryptoInitException {

    if (!initCalled) initCalled = true;
    else return false;

    try {
      DsViaHandler.init(transport);
    } catch (Throwable e) {
      if (Log.on && Log.isErrorEnabled()) Log.error("Exception creating ViaHandler", e);
      throw new DsCryptoInitException(e.getMessage());
    }

    return false;
  }

  /**
   * The constructor creates all underlying DsLibs objects
   *
   * @param transportLayer transport layer to use in DsLibs
   * @param cf ControllerFactory
   */
  public DsSipProxyManager(DsSipTransportLayer transportLayer, DsControllerFactoryInterface cf)
      throws DsSingletonException, DsException, DsCryptoInitException {

    if (m_Singleton != null) {
      if (Log.on && Log.isInfoEnabled())
        Log.info("null ControolerFactory passed to the constructor!");
      throw new DsSingletonException("Only one instance of " + "DsSipProxyManager can be created");
    }
    m_Singleton = this;

    setControllerFactory(cf);
    this.sipTransportLayer = transportLayer;

    initProxyLib(transportLayer);
    viaHandler = DsViaHandler.getInstance();

    // construct a low level transaction manager. The proxy manager
    // implements the DsSipRequestInterface, so pass this to the
    // constructor
    transManager = new DsSipTransactionManager(transportLayer, this);
    transManager.setProxyServerMode(true);
    transManager.setStrayMessageInterface(this);
    transManager.setTransactionEventInterface(this);
    addSupportedExtension(DsSipConstants.BS_PATH.toString());

    if (Log.on && Log.isInfoEnabled()) Log.info("DsSipProxyManager created");
  }

  /**
   * Sets the ControllerFactory. This allows to change the ControllerFactory after the ProxyManager
   * was created
   *
   * @param cf the ControllerFactory to use whenever a request with this method is received
   */
  public void setControllerFactory(DsControllerFactoryInterface cf) {
    if (cf == null) {
      if (Log.on && Log.isInfoEnabled())
        Log.info("null ControolerFactory passed to setControllerFactory!");

      throw new NullPointerException("ControllerFactory should not be null");
    }

    // remember the Controller Factory
    controllerFactory = cf;
  }

  /**
   * Sets the callback to handle stray responses
   *
   * @param ifc implements DsStrayResponseInterface
   */
  public void setStrayResponseInterface(DsStrayResponseInterface ifc) {
    strayResponseInterface = ifc;
  }

  public void setLoopDetector(DsLoopCheckInterface loopDetector) {
    getViaHandler().setLoopDetector(loopDetector);
  }

  /**
   * Sets a trust manager that can be used for authorizing SSL connections
   *
   * @param trustManager implementation of DsSSLTrustManager that will be invoked every time a new
   *     SSL connection is established
   *     <p>The parameters to the callbacks is the chain of credentials proving the identity of the
   *     peer. The first element is the chain is the identity of the peer; that's what Proxy needs
   *     to look at
   */
  public void setSSlTrustManagerInterface(DsSSLTrustManager trustManager) {
    this.trustManager = trustManager;
  }

  /**
   * Adds an extension supported by the application to the list of extensions accepted by the Core.
   * If a request with an unsupported Proxy-Require header is present in the received request, the
   * request will be automatically rejected.
   *
   * @param extension exyension name as it will appear in Proxy-Require (note that the string is
   *     case-sensitive)
   */
  public void addSupportedExtension(String extension) {
    DsSupportedExtensions.addExtension(new DsByteString(extension));
  }

  /**
   * Removes an extension supported by the application from the list of extensions accepted by the
   * Core. If a request with an unsupported Proxy-Require header is present in the received request,
   * the request will be automatically rejected.
   *
   * @param extension exyension name as it will appear in Proxy-Require (note that the string is
   *     case-sensitive)
   * @return true if the extension was previously accepted, false otherwise
   */
  public boolean removeSupportedExtension(String extension) {
    return DsSupportedExtensions.removeExtension(new DsByteString(extension));
  }

  /** @return the transport layer passed as a parameter to constructor */
  protected DsSipTransportLayer getTransportLayer() {
    return m_Singleton.sipTransportLayer;
  }

  /** @return an instance of ProxyManager */
  public static DsSipProxyManager getInstance() {
    return m_Singleton;
  }

  public synchronized void setRouteFixInterface(DsSipRouteFixInterface rfi) {
    transManager.setRouteFixInterface(rfi);
  }

  /**
   * Sets the timeout after wich a TCP connection will be closed
   *
   * @param seconds timeout in seconds
   */
  public void setTCPCleanupInterval(int seconds) {
    sipTransportLayer.setIncomingConnectionTimeout(seconds);
    sipTransportLayer.setOutgoingConnectionTimeout(seconds);
    if (Log.on && Log.isInfoEnabled()) Log.info("TCP cleanup interval is set to " + seconds);
  }

  /** @return SSL Context */
  public DsSSLContext getSSLContext() {
    return sipTransportLayer.getSSLContext();
  }

  /** @return TCP connection timeout */
  public int getTCPCleanupInterval() {
    return sipTransportLayer.getIncomingConnectionTimeout();
  }

  /** @return the number of transaction in TransactionManager */
  public int getTransactionCount() {
    return transManager.getSizeClientTransactionMap() + transManager.getSizeServerTransactionMap();
  }

  /*
    public DsNetwork getNetwork(int network) {
      switch (network) {
        case EXTERNAL_NETWORK:
        case EXTERNAL_WIRELESS_UDP_NETWORK:
        case EXTERNAL_WIRELESS_SIGCOMP_NETWORK:
          return externalNetwork;
        default:
          return internalNetwork;
          //return DsNetwork.getDefault();
      }
    }
  */

  /**
   * Adds another port to listen to
   *
   * @param network configured network to listen on
   * @param aPort port number
   * @param transportType transport type (UDP or TCP)
   * @param aHostAddress network address of host
   */
  public void listenPort(
      DsNetwork network, int aPort, Transport transportType, InetAddress aHostAddress)
      throws DsException, IOException {
    sipTransportLayer.listenPort(network, aPort, transportType, aHostAddress);
    if (Log.on && Log.isDebugEnabled())
      Log.debug(
          "listen on network="
              + network
              + ", port="
              + aPort
              + ", transport="
              + transportType
              + " ,address="
              + aHostAddress);
  }

  /**
   * This method is called by the transaction manager when a new transaction has been created. It
   * will find a proxy object for the transaction if one exists, and invoke its
   * DsSipRequestInterface, otherwise it obtains a Controller from a ControllerFactory and asks it
   * to create a new proxy transaction
   */
  public void request(DsSipServerTransaction serverTransaction) {

    // through a unknow sip request trap when we receive a unknow sip request
    // CR9206
    int mId = serverTransaction.getRequest().getMethodID();
    DsByteString mName = serverTransaction.getRequest().getMethod();
    request(serverTransaction, serverTransaction.getRequest());
  }

  public void request(DsSipServerTransaction serverTransaction, DsSipRequest request) {

    DsLog4j.logSessionId(request);

    DsSipResponse errorResponse = DsProxyUtils.validateRequest(request, true);

    if (errorResponse != null) {

      // serverTrans is null when dealing with stray ACKs and CANCELs
      if (serverTransaction != null) {
        try {
          DsProxyUtils.sendErrorResponse(serverTransaction, errorResponse);
        } catch (Exception e) {
          Log.error("Exception sending " + errorResponse.getStatusCode() + "error response!", e);
          try {
            serverTransaction.abort();
          } catch (Exception e1) {

            // This is a valid condition when operating on a stray ACK or
            // CANCEL. Since there is currently no efficient way to check
            // the request, I'm just leaving the log4j statement out for now.

            //  		Log.warn(
            //  				  "Exception aborting transaction. Must never happen", e1);
          }
        }
      }
      return;
    }

    DsControllerFactoryInterface cf = getControllerFactory();

    if (perfControllerFactoryLog.on) {
      perfControllerFactoryLog.debug("Before ControllerFactory getCurrent");
    }
    DsControllerInterface controller = cf.getController(serverTransaction, request);

    if (perfControllerFactoryLog.on) {
      perfControllerFactoryLog.debug("After ControllerFactory getCurrent");
    }

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, request);

    if (proxy != null) {
      if (!(proxy instanceof DsProxyTransaction)) {
        // if stateless, abort the low level transaction
        try {
          serverTransaction.abort();
        } catch (Exception e1) {
          // This is a valid condition when operating on a stray ACK or
          // CANCEL. Since there is currently no efficient way to check
          // the request, I'm just leaving the log4j statement out for now.

          // 		Log.warn(
          // 				  "Exception aborting transaction. Must never happen", e1);
        }
      }
    }

    if (perfProxyManagerLog.on) perfProxyManagerLog.debug("Leaving ProxyManager request()");
  }

  protected DsControllerFactoryInterface getControllerFactory() {
    return controllerFactory;
  }

  private DsViaHandler getViaHandler() {
    return viaHandler;
  }

  /**
   * Ack message was received without an associated transaction
   *
   * @param ack ack message that was received
   */
  public void strayAck(DsSipAckMessage ack) {
    if (!proxyBasedOnRoute(ack)) {
      // hack !!!!!!!
      request(null, ack);
    }
  }

  /**
   * Cancel message was received without an associated transaction
   *
   * @param cancel cancel message that was received
   */
  public void strayCancel(DsSipCancelMessage cancel) {
    if (!proxyBasedOnRoute(cancel)) {
      // hack !!!!!!!!
      request(null, cancel);
    }
  }

  public void strayPrack(DsSipPRACKMessage dsSipPRACKMessage) {
    // TODO REDDY
  }

  /**
   * Response that received without an associated transaction
   *
   * @param response response that was received
   */
  public void strayResponse(DsSipResponse response) {
    if (strayResponseInterface != null) strayResponseInterface.strayResponse(response);
    else proxyStrayResponse(response);
  }

  /**
   * Statelessy proxies a response upstream.
   *
   * @param response the response to be proxied
   */
  public void proxyStrayResponse(DsSipResponse response) {
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Entering proxyStrayResponse(DsSipResponse response)");

    String direction = response.getNetwork().getName();

    proxyStrayResponse(response, direction);
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Leaving proxyStrayResponse(DsSipResponse response)");
  }

  public void proxyStrayResponse(DsSipResponse response, String direction) {

    if (Log.on && Log.isDebugEnabled())
      Log.debug("Entering proxyStrayResponse(DsSipResponse response, int direction)");

    DsControllerConfig config = DsControllerConfig.getCurrent();
    // check the record route
    if (config.doRecordRoute()) {
      // REDDY_RR_CHANGE
      // CSCui91407
      // Change the record route for stray responses as well
      try {
        setRecordRouteInterface(response);
      } catch (DsException e) {
        e.printStackTrace(); // To change body of catch statement use File | Settings | File
        // Templates.
      }
    }

    // check the top Via;
    DsSipViaHeader myVia;
    try {
      myVia = response.getViaHeaderValidate();
    } catch (DsException e) {
      myVia = null;
      Log.error("Error in getting top Via header", e);
    }
    if (myVia == null) return;

    // check the the top Via matches our proxy
    if (myVia.getBranch() == null) { // we always insert branch
      if (Log.on && Log.isInfoEnabled()) Log.info("Dropped stray response with bad Via");
      return;
    }

    if (!getViaHandler().isLocalInterface(myVia.getHost(), myVia.getPort(), myVia.getTransport())) {
      if (Log.on && Log.isInfoEnabled()) Log.info("Dropped stray response with bad Via");
      return;
    }

    DsProxyUtils.removeTopVia(response);

    DsSipViaHeader via;
    try {
      via = response.getViaHeaderValidate();
    } catch (DsException e) {
      via = null;
      Log.error("Error in getting new top Via header", e);
    }
    if (via == null) {
      Log.error("Top via header is null. Return");
      return;
    }

    if (!getViaHandler().decryptVia(via)) {
      if (Log.on && Log.isInfoEnabled())
        Log.info("Dropped stray response with bad Via (canot decrypt)");
      return;
    }

    // added to support draft-ietf-sip-icm
    // basically this is only need to allow icm
    // traversal for stateless responses being
    // sent to a symmetric NAT
    DsBindingInfo newBinding = new DsBindingInfo();

    newBinding.setNetwork(config.getDefaultNetwork());

    /*
    // 06/07/05 - this was commented out as JUA now provides the
    //            ability for responses to follow the request
    //            when a network is NATed.
    response.setBindingInfo(newBinding);
    */

    if (via.hasParameter(DsProxyStatelessTransaction.RPORT)) {

      // get the branch parameter from this server's via
      DsByteString branch = myVia.getBranch();

      // get the rport cookie from the branch
      // if found, it will be formatted as <ip>:<port>
      // where <ip> and <port> are the IP address and port
      // of the interface on which the
      // original request was received
      DsByteString rportCookie = DsProxyStatelessTransaction.getRPORTCookie(branch);
      if (rportCookie != null) {
        int indexOfColon = rportCookie.indexOf(colon);
        DsByteString rportAddr = rportCookie.substring(0, indexOfColon);
        DsByteString rportPort = rportCookie.substring(indexOfColon + 1);
        String rportAddrStr = rportAddr.toString();
        String rportPortStr = rportPort.toString();
        if (Log.on && Log.isDebugEnabled())
          Log.debug("Setting local binding to " + rportAddrStr + ' ' + rportPortStr);
        try {
          newBinding.setLocalAddress(InetAddress.getByName(rportAddrStr));
          newBinding.setLocalPort(Integer.parseInt(rportPortStr));

        } catch (Throwable t) {
          Log.error("Error setting the local binding", t);
        }
      }
    }

    try {
      if (Log.on && Log.isDebugEnabled()) Log.debug("About to set interface");

      newBinding.setNetwork(DsControllerConfig.getCurrent().getDefaultNetwork());

      DsSipConnection strayResponseConnection = DsSipTransactionManager.getConnection(response);

      if (strayResponseConnection != null) {
        strayResponseConnection.send(response);
      }
      if (Log.on && Log.isInfoEnabled()) Log.info("sent stray response");

    } catch (Exception e) {
      Log.error("Couldn't forward stray response", e);
    }
  }

  /**
   * Checks if the message can ve proxied based on the Route I can't really proxy based on
   * Record-Route because I don't know what to put in the Via!!!! Route header processing is done on
   * the Controller code.
   */
  private boolean proxyBasedOnRoute(DsSipRequest request) {
    return false;
  }

  /** Notification that the transaction manager is shutting down. */
  @Override
  public void transactionManagerShutdown() {}

  /**
   * Notification that the transaction has been removed from the transaction manager transaction
   * map.
   *
   * @param transaction the transaction that has been removed
   */
  public void transactionTerminated(DsSipTransaction transaction) {}

  /**
   * Notification that the transaction submitted to the transaction manager has been started.
   *
   * @param transaction the transaction that has been started
   */
  public void transactionStarted(DsSipTransaction transaction) {}

  /**
   * Handle an individual error.
   *
   * @param transaction the transaction in which the error occured
   * @param message the message being sent
   * @param error the error encountered
   */
  public void transactionError(
      DsSipClientTransaction transaction, DsSipMessage message, Throwable error) {}

  /**
   * Handle an individual error.
   *
   * @param transaction the transaction in which the error occured
   * @param message the message being sent
   * @param error the error encountered
   */
  public void transactionError(
      DsSipServerTransaction transaction, DsSipMessage message, Throwable error) {}
  /**
   * Set the RR header properly in incoming message
   *
   * @param msg the incoming SIP message in which RR needs to be set
   */
  public void setRecordRouteInterface(DsSipMessage msg) throws DsException {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering setRecordRouteInterface()");

    if (msg.getHeaders(DsSipRecordRouteHeader.sID) != null) {
      // stateless, must flip them all
      /**
       * For stray responses we will not have any context on the position of our record route so we
       * have to check the complete list and rewrite our Record-Route. This logic will not work if
       * the request gets spiraled through the proxy and response is a stray response which is a
       * very corner case. One way to fix this is to add a token to the record route which will
       * identify as added by the this Particular proxy. (Token should be generated using top via
       * and a randon number identifying this proxy).
       */
      setRecordRouteInterfaceStateless(msg);
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving setRecordRouteInterface()");
  }

  // REDDY_RR_CHANGE
  private void setRecordRouteInterfaceStateful(DsSipMessage msg) throws DsException {
    // int interfacing = (m_RequestDirection == DsControllerConfig.INBOUND) ?
    // DsControllerConfig.OUTBOUND : DsControllerConfig.INBOUND;
    DsSipHeaderList rrList = null;
    int m_MyRRIndexFromEnd;

    rrList = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);

    boolean compress = msg.shouldCompress();
    DsTokenSipDictionary encode = msg.shouldEncode();
    DsSipHeaderList ll = msg.getHeaders(DsSipRecordRouteHeader.sID);

    if (ll == null) {
      m_MyRRIndexFromEnd = 0;
    } else {
      m_MyRRIndexFromEnd = ll.size();
    }

    int routeIndex = rrList.size() - 1;

    if ((routeIndex >= 0) && (routeIndex < rrList.size())) {
      DsSipRecordRouteHeader rrHeader = (DsSipRecordRouteHeader) rrList.get(routeIndex);
      DsSipURL currentRRURL = (DsSipURL) rrHeader.getNameAddress().getURI();
      setRRHelper(msg, currentRRURL, compress, encode);
    }
  }

  private void setRRHelper(
      DsSipMessage msg, DsSipURL currentRRURL, boolean compress, DsTokenSipDictionary tokDic) {
    if (currentRRURL != null) {
      DsControllerConfig config = DsControllerConfig.getCurrent();
      String network = null;
      String name =
          config.checkRecordRoutes(
              currentRRURL.getUser(),
              currentRRURL.getHost(),
              currentRRURL.getPort(),
              currentRRURL.getTransportParam());
      if (name != null) {
        // todo optimize when get a chance
        if (Log.on && Log.isDebugEnabled())
          Log.debug("Record Route URL to be modified : " + currentRRURL);
        DsByteString u = currentRRURL.getUser();
        String user = null;
        if (u != null) {
          try {
            user = DsSipURL.getUnescapedString(u).toString();
          } catch (DsException e) {
            Log.error("Error in unescaping the RR URI user portion", e);
            user = u.toString();
          }
        }
        if (user != null) {
          StringTokenizer st = new StringTokenizer(user);
          String t = st.nextToken(DsReConstants.DELIMITER_STR);
          while (t != null) {
            if (t.startsWith(DsReConstants.NETWORK_TOKEN)) {
              network = t.substring(DsReConstants.NETWORK_TOKEN.length());
              user = user.replaceFirst(t, DsReConstants.NETWORK_TOKEN + name);
              break;
            }
            t = st.nextToken(DsReConstants.DELIMITER_STR);
          }
          currentRRURL.setUser(
              DsSipURL.getEscapedString(new DsByteString(user), DsSipURL.USER_ESCAPE_BYTES));
        } else {
          network = msg.getNetwork().getName();
        }

        if (Log.on && Log.isDebugEnabled())
          Log.debug(
              "Outgoing network of the message for which record route has to be modified : "
                  + network);
        DsSipRecordRouteHeader recordRouteInterfaceHeader =
            config.getRecordRouteInterface(network, false);

        if (recordRouteInterfaceHeader == null) {
          if (Log.on && Log.isDebugEnabled())
            Log.debug("Did not find the Record Routing Interface!");
          return;
        }

        DsSipURL recordRouteInterface = (DsSipURL) recordRouteInterfaceHeader.getURI();

        currentRRURL.setHost(recordRouteInterface.getHost());

        if (recordRouteInterface.hasPort()) {
          currentRRURL.setPort(recordRouteInterface.getPort());
        } else {
          currentRRURL.removePort();
        }

        if (recordRouteInterface.hasTransport()) {
          currentRRURL.setTransportParam(recordRouteInterface.getTransportParam());
        } else {
          currentRRURL.removeTransportParam();
        }
        if (compress) {
          currentRRURL.setCompParam(DsSipConstants.BS_SIGCOMP);
        } else {
          currentRRURL.removeCompParam();
          if (null != tokDic) {
            currentRRURL.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
          } else {
            currentRRURL.removeParameter(DsTokenSipConstants.s_TokParamName);
          }
        }
        if (Log.on && Log.isDebugEnabled())
          Log.debug("Modified Record route URL to : " + currentRRURL);
      }
    }
  }

  private void setRecordRouteInterfaceStateless(DsSipMessage msg) throws DsException {
    DsSipHeaderList rrHeaders = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);
    boolean compress = msg.shouldCompress();
    DsTokenSipDictionary encode = msg.shouldEncode();
    if (rrHeaders != null && rrHeaders.size() > 0) {
      for (int headerCount = 0; headerCount < rrHeaders.size(); headerCount++) {
        DsSipRecordRouteHeader recordRouteHeader =
            (DsSipRecordRouteHeader) rrHeaders.get(headerCount);
        setRRHelper(msg, (DsSipURL) recordRouteHeader.getURI(), compress, encode);
      }
    }
  }

  public static void setM_Singleton(DsSipProxyManager m_Singleton) {
    DsSipProxyManager.m_Singleton = m_Singleton;
  }
}
