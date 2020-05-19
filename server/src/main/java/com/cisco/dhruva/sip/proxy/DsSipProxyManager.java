package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.adaptor.ProxyAdaptorFactory;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.sip.DsUtil.DsReConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.util.Arrays;
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

  public static final int EXTERNAL_NETWORK = 0;
  public static final int INTERNAL_NETWORK = 0;
  public static final String EXTERNAL_NETWORK_STR = "EXTERNAL";
  public static final String INTERNAL_NETWORK_STR = "INTERNAL";

  private static final boolean USE_CONNECTED_UDP =
      Boolean.getBoolean("com.cisco.DsLibs.DsSipProxyManager.TOKENSIP_CONNECTED_UDP");

  protected static Logger Log = DhruvaLoggerFactory.getLogger(DsSipProxyManager.class);

  // some private strings
  private static final DsByteString colon = new DsByteString(":");
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
      Log.error("Exception creating ViaHandler", e);
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
    DsSipTransactionManager.setProxyServerMode(true);
    transManager.setStrayMessageInterface(this);
    transManager.setTransactionEventInterface(this);

    Log.info("DsSipProxyManager created");
  }

  /**
   * Sets the ControllerFactory. This allows to change the ControllerFactory after the ProxyManager
   * was created
   *
   * @param cf the ControllerFactory to use whenever a request with this method is received
   */
  public void setControllerFactory(DsControllerFactoryInterface cf) {
    if (cf == null) {
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
    Log.info("TCP cleanup interval is set to " + seconds);
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

  /**
   * This method is called by the transaction manager when a new transaction has been created. It
   * will find a proxy object for the transaction if one exists, and invoke its
   * DsSipRequestInterface, otherwise it obtains a Controller from a ControllerFactory and asks it
   * to create a new proxy transaction
   */
  public void request(DsSipServerTransaction serverTransaction) {
    request(serverTransaction, serverTransaction.getRequest());
  }

  public void request(DsSipServerTransaction serverTransaction, DsSipRequest request) {

    DsLog4j.logSessionId(request);
    Log.info("handle request " + Arrays.toString(request.getSessionId()) + request.getMethod());

    DsControllerFactoryInterface cf = getControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = new AppSession();

    DsControllerInterface controller = cf.getController(serverTransaction, request, pf, app);

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
    Log.debug(
        "Entering proxyStrayResponse(DsSipResponse response) {}"
            + Arrays.toString(response.getSessionId()));

    String direction = response.getNetwork().getName();

    proxyStrayResponse(response, direction);
    Log.debug("Leaving proxyStrayResponse(DsSipResponse response)");
  }

  public void proxyStrayResponse(DsSipResponse response, String direction) {

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
      Log.info("Dropped stray response with bad Via");
      return;
    }

    if (!getViaHandler().isLocalInterface(myVia.getHost(), myVia.getPort(), myVia.getTransport())) {
      Log.info("Dropped stray response with bad Via");
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
      Log.debug("About to set interface");

      newBinding.setNetwork(DsControllerConfig.getCurrent().getDefaultNetwork());

      DsSipConnection strayResponseConnection = DsSipTransactionManager.getConnection(response);

      if (strayResponseConnection != null) {
        strayResponseConnection.send(response);
      }
      Log.info("sent stray response");

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
    Log.debug("Entering setRecordRouteInterface()");

    if (msg.getHeaders(DsSipRecordRouteHeader.sID) != null) {
      // stateless, must flip them all
      setRecordRouteInterfaceStateless(msg);
    }
    Log.debug("Leaving setRecordRouteInterface()");
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

        Log.debug(
            "Outgoing network of the message for which record route has to be modified : "
                + network);
        DsSipRecordRouteHeader recordRouteInterfaceHeader =
            config.getRecordRouteInterface(network, false);

        if (recordRouteInterfaceHeader == null) {
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
        Log.debug("Modified Record route URL to : " + currentRRURL);
      }
    }
  }

  private void setRecordRouteInterfaceStateless(DsSipMessage msg) throws DsException {
    DsSipHeaderList rrHeaders = msg.getHeadersValidate(DsSipConstants.RECORD_ROUTE);
    boolean compress = msg.shouldCompress();
    DsTokenSipDictionary encode = msg.shouldEncode();
    if (rrHeaders != null && rrHeaders.size() > 0) {
      for (Object rrHeader : rrHeaders) {
        DsSipRecordRouteHeader recordRouteHeader = (DsSipRecordRouteHeader) rrHeader;
        setRRHelper(msg, (DsSipURL) recordRouteHeader.getURI(), compress, encode);
      }
    }
  }

  public static void setM_Singleton(DsSipProxyManager m_Singleton) {
    DsSipProxyManager.m_Singleton = m_Singleton;
  }
}
