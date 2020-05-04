/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.controllers;

import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.re.search.Location;
import com.cisco.dhruva.sip.re.search.interfaces.ProxyInterface;
import com.cisco.dhruva.sip.re.search.interfaces.ProxyResponseInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsControllerInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsInternalProxyErrorException;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyClientTransaction;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyCookieInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyParamsInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyServerTransaction;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyStatelessTransaction;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyTransaction;
import com.cisco.dhruva.sip.servergroups.DsLibs.DsPings.DsErrorResponseCodeSet;
import com.cisco.dhruva.sip.servergroups.DsLibs.loadbalancer.LBRepositoryHolder;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequireHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipSupportedHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import com.thoughtworks.qdox.Searcher;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This abstract class handles failover and loadbalancing. When used in conjuction with a <code>
 * Searcher</code> it is also capable of basic proxying and recursion. This functionality will only
 * exist if this class is subclassed and given a Searcher object. If this controller is used for its
 * DsControllerInterface, then it is the responsibility of the subclass to create a Searcher for
 * this class to use. If this class is used for its ProxyInterface, then all responses received will
 * be passed to the ProxyResponseInterface passed in the ProxyInterface methods. The <code>
 * BasicProxyController</code> is an example of a subclass that creates a Searcher.
 *
 * <p>Copyright 2001 dynamicsoft, inc. All rights reserved
 */
public abstract class DsProxyController implements DsControllerInterface, ProxyInterface {
  private static boolean errorAggregatorEnabled =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_ENABLE_ERROR_AGGREGATOR,
          DsConfigManager.PROP_ENABLE_ERROR_AGGREGATOR_DEFAULT);

  private static boolean errorMappedResponseEnabled =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_ENABLE_ERROR_MAPPED_RESPONSE,
          DsConfigManager.PROP_ENABLE_ERROR_MAPPED_RESPONSE_DEFAULT);

  private static boolean isCreateDnsServerGroup =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CREATE_DNS_SERVER_GROUP,
          DsConfigManager.PROP_CREATE_DNS_SERVER_GROUP_DEFAULT);;

  private static boolean isCreateCAEvents =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CREATE_CA_EVENTS, DsConfigManager.PROP_CREATE_CA_EVENTS_DEFAULT);

  /**
   * Indicates whether or not the Supported: path header value is required when adding a Path header
   * to REGISTER requests. If <code>true</code>, and the REGISTER request does NOT contain a
   * Supported: path header value, and the the server is configured to add a Path header, the
   * request is rejected with a 421 response. Otherwise, the server adds the Supported: path header
   * (if not present) in addition to the Path header.
   */
  public static final boolean REQUIRE_SUPPORTED_HEADER =
      Boolean.getBoolean("com.dynamicsoft.DsLibs.REQUIRE_SUPPORTED_HEADER");
  /**
   * This flag determines whether the ProxyController needs to add Supported: path if not present.
   *
   * <p>By default, ProxyController adds the Supported: path if not present.
   *
   * <p>Note that if REQUIRE_SUPPORTED_HEADER flag has been set to "true", this option is
   * meaningless as the Proxy expects to receive a Supported path header.
   */
  public static final boolean ADD_SUPPORTED_PATH =
      Boolean.getBoolean("com.dynamicsoft.DsLibs.ADD_SUPPORTED_PATH");

  public static final DsByteString BS_L_PATH = DsSipConstants.BS_PATH;

  /** The numerical constant for "421 - Extension Required" response code. */
  public static final int DS_RESPONSE_EXTENSION_REQUIRED = 421;
  /** The string constant for "Extension Required" string. */
  public static final String DS_STR_RESPONSE_EXTENSION_REQUIRED = "Extension Required";
  /** The byte string constant for "Extension Required" string. */
  public static final DsByteString DS_BS_RESPONSE_EXTENSION_REQUIRED =
      new DsByteString("Extension Required");

  // Defines failure reason to be sent in notifications(Alarm and SAEvent) generated by cloudproxy
  // after call failures
  public static final String failureReason = "Request timeout failure";
  public static final String icmpFailureReason = "ICMP - Error";

  // Forking types
  public static final byte SEARCH_PARALLEL = 0;
  public static final byte SEARCH_SEQUENTIAL = 1;
  public static final byte SEARCH_HIGHEST = 2;
  public static final int SEQUENTIAL_SEARCH_TIMEOUT_DEFAULT = 60000;
  static final boolean mEmulate2543 =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_EMULATE_RFC2543_RESPONSES,
          DsConfigManager.PROP_EMULATE_RFC2543_RESPONSES_DEFAULT);

  public HashMap ProxyParams = null;
  public HashMap parsedProxyParamsByType = null;

  public static final DsSipSupportedHeader supportedPath = new DsSipSupportedHeader(BS_L_PATH);
  public static final DsSipRequireHeader requirePath = new DsSipRequireHeader(BS_L_PATH);

  //////////// Vars passed in the constructor ////////////
  /* Stores forking/search type, default is parallel */
  protected byte searchType;
  /* Stores if we are recursing or not */
  protected boolean isRecursing;
  /* Stores the request for this controller */
  protected DsSipRequest ourRequest;
  /* Stores the original request as a clone */
  protected DsSipRequest originalRequest;
  /* Stores the request with pre-normalization and xcl processing applied */
  protected DsSipRequest preprocessedRequest;
  /* Stores if we are in stateful or stateless mode */
  protected byte stateMode = -1;
  /* Used for proxying and creating transactions */
  protected DsProxyParamsInterface ppIface;
  /* Holds the set of error codes that may contain retry-after headers */
  protected DsErrorResponseCodeSet errorResponseCodeSet;
  /* The default value to be used if an overload response comes back with no retry-after header */
  protected int defaultRetryAfterMillis;
  /** our proxy...we need to save it so as to give access to other methods. */
  protected DsProxyStatelessTransaction ourProxy;

  /** The sequential timeout if we are doing a sequential search * */
  protected int sequentialSearchTimeout = SEQUENTIAL_SEARCH_TIMEOUT_DEFAULT;
  /** remember the cancel request * */
  protected boolean gotCancel;
  /* Our searcher object */
  protected Searcher searcher;
  /* A mapping of Locations to client transactions used when cancelling */
  protected HashMap locToTransMap = new HashMap(11);
  /* Used to get the repository when creating a load balancer */
  protected LBRepositoryHolder repositoryHolder;
  /** our log object * */
  protected static Trace Log = Trace.getTrace(DsProxyController.class.getName());

  /** If true, will cancel all branches on CANCEL, 2xx and 6xx respnses */
  protected boolean cancelBranchesAutomatically = false;

  protected ArrayList unCancelledBranches = new ArrayList(3);

  protected boolean usingRouteHeader = false;

  protected DsSipServerTransaction m_ServerTransaction;

  // added by ketul,
  // callleg key used by stateless transactions only for failoverstateful
  // behaviour. callLegKey is a combination of callId + cSeq
  private String callLegKey = null;

  private boolean pathAdded = false;
  // Order in which the transport is selected.
  private static final int Transports[] = {
    DsSipTransportType.TLS, DsSipTransportType.TCP, DsSipTransportType.UDP
  };

  protected boolean respondedOnNewRequest = false;
  HashMap dnsServerGroups = new HashMap<>();
  private long timeToTry;

  /**
   * Creates a <CODE>DsProxyStatelessTransaction</CODE> object if the proxy is configured to be
   * stateless. Otherwise if either the proxy is configured to be stateful or if the controller
   * decides that the current transaction should be stateful , it creates the <CODE>
   * DsProxyTransaction</CODE> object. This method can only be used to create a transaction if one
   * has not been created yet.
   *
   * @param setStateful Indicates that the current transaction be stateful,irrespective of the
   *     controller configuration.
   * @param request The request that will be used to create the transaction
   */
  public void createProxyTransaction(
      boolean setStateful, DsSipRequest request, DsSipServerTransaction serverTrans) {
    if (ourProxy == null) {
      if (setStateful
          || (request != null && request.getBindingInfo().getTransport() == Transport.TCP)) {
        try {
          ourProxy = new DsProxyTransaction(this, ppIface, serverTrans, request);

          if (Log.on && Log.isDebugEnabled()) Log.debug("Created stateful proxy transaction ");
        } catch (DsInternalProxyErrorException e) {
          Log.error("createProxyTransaction() - couldn't create proxy transaction ", e);
        }
      } else {
        try {
          ourProxy = new DsProxyStatelessTransaction(this, ppIface, request);
        } catch (DsInternalProxyErrorException dse) {
          sendFailureResponse(DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR);
        }
        if (Log.on && Log.isDebugEnabled()) Log.debug("Created stateless proxy transaction ");
      }
    }
  }

  /**
   * Creates a <CODE>DsProxyStatelessTransaction</CODE> object if the proxy is configured to be
   * stateless. Otherwise if either the proxy is configured to be stateful or if the controller
   * decides that the current transaction should be stateful , it creates the <CODE>
   * DsProxyTransaction</CODE> object. This method can only be used to create a transaction if one
   * has not been created yet.
   *
   * @param setStateful Indicates that the current transaction be stateful,irrespective of the
   *     controller configuration.
   */
  protected void createProxyTransaction(boolean setStateful, DsSipServerTransaction serverTrans) {

    createProxyTransaction(setStateful, ourRequest, serverTrans);
  }

  /*
   * Sends a 404 or 500 response.
   */
  protected void sendFailureResponse(int errorResponseCode) {

    if (errorResponseCode == DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR) {
      if (changeToStatefulForResponse(DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR)) {
        try {
          DsProxyResponseGenerator.sendServerInternalErrorResponse(
              ourRequest, (DsProxyTransaction) ourProxy);
        } catch (DsException e) {
          Log.error("Error encountered while sending internal error response", e);
        }
        // failureResponseSent = true;
      }
    } else if (errorResponseCode == DsSipResponseCode.DS_RESPONSE_NOT_FOUND) {
      if (changeToStatefulForResponse(DsSipResponseCode.DS_RESPONSE_NOT_FOUND)) {
        try {
          DsProxyResponseGenerator.sendNotFoundResponse(ourRequest, (DsProxyTransaction) ourProxy);
        } catch (DsException e) {
          // Warn Logging
          Log.error("Unable to create not found response", e);
        }
        // failureResponseSent = true;
      }
    }
  }

  /* Attempts to change to stateful mode to send are response with the given response
   * code.
   * @param responseCode The response code of the response to send upstream.
   * @returns True if it could change to stateful mode, false if we couldn't
   */
  protected boolean changeToStatefulForResponse(int responseCode) {
    // Make sure we are stateful before sending the response
    boolean success = overwriteStatelessMode();
    if (!success) {
      Log.warn("Unable to change state to send " + responseCode + ", dropping the response");
    }

    return success;
  }

  /*
   * Overwrites a stateful DsProxyTransaction with a DsStatelessProxy transaction.
   */
  public boolean overwriteStatelessMode() {

    // Set it to null if it is stateless
    if (ourProxy != null && !(ourProxy instanceof DsProxyTransaction)) {
      ourProxy = null;
    }

    if (Log.on && Log.isDebugEnabled())
      Log.debug("Changing stateless proxy transaction to a stateful one");

    DsSipHeaderList vias = ourRequest.getHeaders(DsSipConstants.VIA);
    if (null != vias) {
      try {
        DsSipViaHeader topvia = (DsSipViaHeader) vias.getFirstHeader();
        if (DsControllerConfig.getCurrent()
            .recognize(null, topvia.getHost(), topvia.getPort(), topvia.getTransport())) {
          if (Log.on && Log.isDebugEnabled())
            Log.debug(
                "Removing the top via since its our own and we are trying to respond in stateless mode");
          vias.removeFirstHeader();
        }
      } catch (DsSipParserException e) {
        Log.error("Error in parsing the top via of the request", e);
      } catch (DsSipParserListenerException e) {
        Log.error("Error in parsing the top via of the request", e);
      }
    }

    // Create a stateful proxy
    createProxyTransaction(true, m_ServerTransaction);

    return !(ourProxy == null);
  }

  /* Used to initialize the controller.  This should be called before call backs
   * are made to this object.
   * @param searchType Either SEARCH_PARALLEL, SEARCH_SEQUENTIAL or SEARCH_HIGHEST.
   * @param seqtimeout The timeout to be used when sequential searching.
   * @param stateMode  the proxy  mode (0, 1 or 2 )
   * @param isRecursing True if recursion should be used on 3xx responses
   * @param ppIface The DsProxyParamsInterface to be used when proxying a request
   * when there is no server group for the URI which will be proxied. It holds the
   * request timout value, among other things.
   */
  public void init(
      byte searchType,
      int timeout,
      int sequentialSearchTimeout,
      byte stateMode,
      boolean isRecursing,
      DsProxyParamsInterface ppIface) {
    init(
        searchType,
        timeout,
        sequentialSearchTimeout,
        stateMode,
        isRecursing,
        ppIface,
        0,
        null,
        DsControllerConfig.NHF_ACTION_FAILOVER);
  }

  /* Used to initialize the controller.  This should be called before call backs
   * are made to this object.
   * @param searchType Either SEARCH_PARALLEL, SEARCH_SEQUENTIAL or SEARCH_HIGHEST.
   * @param seqtimeout The timeout to be used when sequential searching.
   * @param stateMode int for proxy state mode
   * @param isRecursing True if recursion should be used on 3xx responses
   * @param ppIface The DsProxyParamsInterface to be used when proxying a request
   * when there is no server group for the URI which will be proxied.
   * @param ercs The set of response codes to that will be failed over on.  Null if you
   * don't care about ever failing over on a failure response.
   * @param defaultRetryAfter The default value to be used if an overload response
   * (specified in <code>ercs</code>) comes back with no retry-after header
   */
  public void init(
      byte searchType,
      int timeout,
      int sequentialSearchTimeout,
      byte stateMode,
      boolean isRecursing,
      DsProxyParamsInterface ppIface,
      int defaultRetryAfterMillis,
      LBRepositoryHolder holder,
      byte nextHopFailoverAction) {}

  /**
   * DsControllerInterface onNewRequest() method implementation. Creates a proxy transaction if one
   * for this controller hasn't been set/created yet. If this is a request that maps to an existing
   * transaction and we are in FAILOVER_STATEFUL mode, then the request is sent again to the
   * endpoint it was sent to last time. If this is a new request and the mode is not
   * FAILOVER_STATEFUL, then the appropriate search (Sequential, Parallel or Highest-Q) begins using
   * the searcher that was passed in in init().
   *
   * @param request The incoming request
   */
  public DsProxyStatelessTransaction onNewRequest(
      DsSipServerTransaction serverTrans, DsSipRequest request) {
    return null;
  }

  // returns a mapping to the callLegKey if it exists in the Hashtable
  public DsFailOverStatefulWrapper getFailOverStatefulWrapper(DsSipRequest newRequest) {
    return null;
  }

  /**
   * This is invoked whenever an ACK is received for the response we sent back.
   *
   * @param proxy the ProxyTransaction object
   * @param transaction the ServerTransaction being ACKed
   * @param ack the ACK request
   */
  public void onAck(
      DsProxyTransaction proxy, DsProxyServerTransaction transaction, DsSipAckMessage ack) {
    // Do nothing
  }

  /**
   * This method is invoked when the proxy receives a response it would like to send.
   *
   * @param response The response the proxy believes is the best and would like to send.
   * @param proxy The proxy object Note: this interface will need to be changed to handle multiple
   *     200 OKs. My understanding is that Low Level API currently drops all 200 OKs after the first
   *     one so I didn't bother to define a controller API for this as well
   */
  public void onBestResponse(DsProxyTransaction proxy, DsSipResponse response) {}

  /**
   * This is called when a CANCEL is received for the original transaction. All branches mapping to
   * this transaction will be terminated.
   *
   * @param proxy The proxyTransaction object
   * @param trans DsProxyServerTransaction being cancelled
   * @param cancel the CANCEL request
   * @throws DsException
   */
  public void onCancel(
      DsProxyTransaction proxy, DsProxyServerTransaction trans, DsSipCancelMessage cancel)
      throws DsException {}

  public void onResponse(DsSipResponse response) {}

  /**
   * This method is invoked by the proxy when a 1xx response to a proxied request is received.
   *
   * @param response The response that was received.
   * @param proxy The proxy object.
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction representing the branch that the response was received on
   */
  public void onProvisionalResponse(
      DsProxyTransaction proxy,
      DsProxyCookieInterface cookie,
      DsProxyClientTransaction trans,
      DsSipResponse response) {}

  /**
   * This method is invoked by the proxy when a 2xx response to a proxied request is received. All
   * outstanding branches for this transaction are cancelled.
   *
   * @param response The response that was received.
   * @param proxy The ProxyTransaction object.
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction representing the branch that the response was received on
   */
  public void onSuccessResponse(
      DsProxyTransaction proxy,
      DsProxyCookieInterface cookie,
      DsProxyClientTransaction trans,
      DsSipResponse response) {}

  /**
   * This method is invoked by the proxy when a 3xx response to a proxied request is received. Its a
   * good opportunity to perform recursion if needed.
   *
   * @param response The redirect response that was received.
   * @param proxy The ProxyTransaction object.
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction representing the branch that the response was received on
   */
  public void onRedirectResponse(
      DsProxyTransaction proxy,
      DsProxyCookieInterface cookie,
      DsProxyClientTransaction trans,
      DsSipResponse response) {}

  /**
   * This method is invoked by the proxy when a 4xx or 5xx response to a proxied request is received
   *
   * @param response Response message that was received. Note that the top Via header will be
   *     stripped off before its passed.
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction representing the branch that the response was received on
   * @param proxy ProxyTransaction object
   */
  public void onFailureResponse(
      DsProxyTransaction proxy,
      DsProxyCookieInterface cookie,
      DsProxyClientTransaction trans,
      DsSipResponse response) {}

  /**
   * This method is invoked by the proxy when a 6xx response to a proxied request is received.
   *
   * @param response The response that was received.
   * @param proxy The ProxyTransaction object.
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction representing the branch that the response was received on
   */
  public void onGlobalFailureResponse(
      DsProxyTransaction proxy,
      DsProxyCookieInterface cookie,
      DsProxyClientTransaction trans,
      DsSipResponse response) {}

  /**
   * This method is invoked whenever a ClientTransaction times out before receiving a response
   *
   * @param proxy The proxy object
   * @param trans DsProxyClientTransaction where the timeout occurred
   * @param cookie cookie object passed to proxyTo()
   */
  public void onRequestTimeOut(
      DsProxyTransaction proxy, DsProxyCookieInterface cookie, DsProxyClientTransaction trans) {}

  /**
   * This method is invoked whenever a ServerTransaction times out. The method is only relevant for
   * INVITE transactions for which a non-200 response was sent
   *
   * @param proxy The proxy object
   * @param trans the transaction that has timed out. If controller decides to undertake any actions
   *     in response to this event, it might pass the request back to the ProxyTransaction to
   *     identify the timed out ClientTransaction
   */
  public void onResponseTimeOut(DsProxyTransaction proxy, DsProxyServerTransaction trans) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering onResponseTimeOut()");

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving onResponseTimeOut()");
  }

  /**
   * This is invoked whenever an ICMP error occurs while retransmitting a response over UDP
   *
   * @param proxy The proxy object
   * @param trans DsProxyServerTransaction where the timeout occurred
   */
  public void onICMPError(DsProxyTransaction proxy, DsProxyServerTransaction trans) {

    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering onICMPError() for a response ");
  }

  /**
   * This is invoked whenever an ICMP error occurs while retransmitting a request over UDP
   *
   * @param proxy The proxy object
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction where the timeout occurred
   */
  public void onICMPError(
      DsProxyTransaction proxy, DsProxyCookieInterface cookie, DsProxyClientTransaction trans) {}

  /**
   * This callback is invoked when there was a synchronous exception forwarding a request and
   * DsProxyClientTransaction object could not be created
   *
   * @param proxy ProxyTransaction object
   * @param cookie cookie object passed to proxyTo()
   * @param errorCode identifies the exception thrown when forwarding request
   * @param errorPhrase the String from the exception
   * @param exception exception that caused the error; null if not available
   */
  public void onProxyFailure(
      DsProxyStatelessTransaction proxy,
      DsProxyCookieInterface cookie,
      int errorCode,
      String errorPhrase,
      Throwable exception) {}

  /**
   * This callback is invoked if a request was forwarded successfully, i.e., without any synchronous
   * exceptions and a DsProxyClientTransaction is created NOTE: It is possible to receive
   * onProxySuccess callback first and then OnProxyFailure. This will happen when the error is
   * reported asynchronously to the Proxy Core
   *
   * @param proxy ProxyTransaction object
   * @param cookie cookie object passed to proxyTo()
   * @param trans newly created DsProxyClientTransaction
   */
  public void onProxySuccess(
      DsProxyStatelessTransaction proxy,
      DsProxyCookieInterface cookie,
      DsProxyClientTransaction trans) {}

  /**
   * This callback is invoked if a response was forwarded successfully, i.e., without any
   * synchronous exceptions and a DsProxyClientTransaction is created
   *
   * @param proxy ProxyTransaction object
   * @param trans DsProxyServerTransaction on which the response was sent
   */
  public void onResponseSuccess(DsProxyTransaction proxy, DsProxyServerTransaction trans) {}

  /**
   * This callback is invoked when there was a synchronous exception forwarding a response and
   * DsProxyClientTransaction object could not be created
   *
   * @param proxy ProxyTransaction object
   * @param errorCode identifies the exception thrown when forwarding request
   * @param errorPhrase the String from the exception
   */
  public void onResponseFailure(
      DsProxyTransaction proxy,
      DsProxyServerTransaction trans,
      int errorCode,
      String errorPhrase,
      Throwable exception) {}

  /**
   * If this is set to true, the controller will cancel all outstanding branches when it receives a
   * final response. Otherwise it won't.
   */
  public void setCancelBranchesAutomatically(boolean cancel) {
    cancelBranchesAutomatically = cancel;
  }

  public void setProxyTransaction(DsProxyStatelessTransaction proxy) {
    ourProxy = proxy;
  }

  public void cancel(Location location, boolean timedOut) {}

  public void respond(DsSipResponse response) {}

  /**
   * Send a 100 Trying response. If there is no proxy transaction created yet, we will create one
   * now.
   */
  public void sendTryingResponse(DsSipRequest request) {
    DsProxyResponseGenerator.sendByteBasedTryingResponse((DsProxyTransaction) ourProxy);
  }

  public DsNetwork getNetworkFromLocation(Location location) {
    // get the network from location if set
    return null;
  }

  public void proxyTo(
      Location location, DsSipRequest request, ProxyResponseInterface responseIf, long timeToTry) {

    // clone the request with pre-normalization and xcl processing applied. This is a call from xcl
    preprocessedRequest = (DsSipRequest) request.clone();
    proxyToInternal(location, request, responseIf, timeToTry);
  }

  /*
   * This method is used send the reqeust out to a logical address.  If a server group
   * is specified in the location, then the method will keep trying to send out a request
   * until it is successful, or all server groups have been tried.  If no server
   * group is specified in the <CODE>Location</CODE> then the request is
   * sent out to the host in the request uri via the proxyToLogical method which
   * just takes a URI.  The responseIf is used to propogate responses when a response
   * call back is received from the core via the ControllerInterface.
   * @returns true if a request was successfully sent out, false if it was unable to
   * send to any of the elements in the server group, if there was a server group.
   */
  private void proxyToInternal(
      Location location, DsSipRequest request, ProxyResponseInterface responseIf, long timeToTry) {}

  public DsProxyStatelessTransaction getStatelessTrans(DsFailOverStatefulWrapper failOverWrapper) {
    return null;
  }

  @Override
  public void proxyTo(Location location, DsSipRequest request, ProxyResponseInterface callbackIf) {}
}
