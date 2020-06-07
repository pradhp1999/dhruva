// Copyright (c) 2005-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.transport.ConnectionKey;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Logger;
import gnu.trove.TIntArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.TimerTask;
import org.slf4j.event.Level;

/**
 * Implements the client side of the low level state machine.
 *
 * <p>This class implements DsSipClientTransaction using a state machine. The states and inputs of
 * this state machine are defined in DsSipStateMachineDefinitions. Input is provided to the state
 * machine via the execute method of this class. For each state of the state machine, there is a
 * method defined in this class which is called when the state machine enters (or re-enters) that
 * state. The single parameter of these state machine methods is an integer (int) which is an OR of
 * the state machine's current input and the previous state.
 */
public class DsSipClientTransactionImpl extends DsSipClientTransaction
    implements DsSipStateMachineDefinitions, DsSipConstants, DsEvent {
  ///////////////////////////////////////////////////////////////////////////
  //////////// statics //////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  static final Integer IN_CANCEL_TIMER = new Integer(DS_CT_IN_CANCEL_TIMER);
  static final Integer IN_T1 = new Integer(DS_CT_IN_T1);
  static final Integer IN_Tp = new Integer(DS_CT_IN_Tp);
  static final Integer IN_Tn = new Integer(DS_CT_IN_Tn);
  static final Integer IN_TIMEOUT = new Integer(DS_CT_IN_TIMEOUT);

  /** <code>true</code> if running as a JAIN stack. */
  static final boolean m_jainCompatability =
      DsConfigManager.getProperty(DsConfigManager.PROP_JAIN, DsConfigManager.PROP_JAIN_DEFAULT);

  /** <code>true</code> if emulating RFC 2543 responses. */
  static final boolean m_emulate2543Responses =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_EMULATE_RFC2543_RESPONSES,
          DsConfigManager.PROP_EMULATE_RFC2543_RESPONSES_DEFAULT);

  /** The queue through which to call the application code back. */
  private static DsWorkQueue m_callbackQueue;

  /** Size of the callback queue. */
  private static final int MAX_CALLBACK_EVENTS = 400;
  /** Number of threads in the callback queue. */
  private static final int MAX_CALLBACK_THREADS =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CLIENT_CB_WORKERS, DsConfigManager.PROP_CLIENT_CB_WORKERS_DEFAULT);

  /** <code>true</code> to use the simple resolver. */
  protected boolean m_simpleResolver = true;

  /**
   * <code>true</code> to clean up memory as soon as possible, storing only bytes for
   * retransmissions, rather than entire message.
   */
  protected static boolean m_cleanup = false;
  /** Used to determine of some clean up can happen. */
  protected static boolean m_x200Terminated =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_X200TERM, DsConfigManager.PROP_X200TERM_DEFAULT);

  /** General logging Category. */
  protected static Logger genCat; // = null;
  /** Callback logging Category. */
  protected static Logger cbCat; // = null;
  /** Timer logging Category. */
  protected static Logger cat; // = null;
  /** Resolver logging Category. */
  protected static Logger resolvCat; // = null;

  /** To use the Deterministic SRV resolver or not. The default is <code>true</code>. */
  protected static boolean m_useDeterministicLocator =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_DETERMINISTIC_RESOLVER,
          DsConfigManager.PROP_DETERMINISTIC_RESOLVER_DEFAULT);

  private static boolean m_useDsUnreachableTable =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_USE_DSUNREACHABLE_DESTINATION_TABLE,
          DsConfigManager.PROP_USE_DSUNREACHABLE_DESTINATION_TABLE_DEFAULT);

  static {
    genCat = DsLog4j.LlSMClientCat;
    cbCat = DsLog4j.LlSMClientUserCBCat;
    cat = DsLog4j.LlSMClientTimersCat;
    resolvCat = DsLog4j.resolvCat;

    m_cleanup =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_CLEANUP, DsConfigManager.PROP_CLEANUP_DEFAULT);

    m_callbackQueue =
        new DsWorkQueue(
            DsWorkQueue.CLIENT_CALLBACK_QNAME, MAX_CALLBACK_EVENTS, MAX_CALLBACK_THREADS);
    m_callbackQueue.setDiscardPolicy(DsWorkQueue.GROW_WITHOUT_BOUND);
    DsConfigManager.registerQueue((DsQueueInterface) m_callbackQueue);
  }

  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Set to <code>true</code> if the client transaction was replaced by a service unavailable
   * handler.
   */
  private boolean m_replaced; // = false;

  /** <code>true</code> if this transaction has been clean up already. */
  private boolean m_isCleanedup; // = false;
  /** <code>true</code> if this transaction handles multiple final responses. */
  private boolean m_multipleFinalResponseEnabled; // = false;
  /** <code>true</code> if a cancel callback has been set. */
  private boolean m_cancelCallback; // = false;

  /** For client transaction callbacks. */
  protected transient DsSipClientTransactionInterface m_clientInterface; // = null;
  /** For CANCEL callbacks. */
  protected transient DsSipClientTransactionInterface m_cancelInterface; // = null;
  /** The transport information for this transaction. */
  protected transient DsSipClientTransportInfo m_clientTransportInfo; // = null;

  /** The state table for this transaction. */
  protected DsSipStateTable m_stateTable;

  /** The request that started this transaction. */
  protected DsSipRequest m_sipRequest; // = null;
  /** The best response received for this transaction. */
  protected DsSipResponse m_sipResponse; // = null;
  // CAFFEINE 2.0 DEVELOPMENT - required to handle provisional response.
  /** The last reliable provisional response received for this transaction. */
  protected DsSipResponse m_sipRelProvisionalResponse; // = null;
  /** The CANCEL message, if this transaction was cancelled. */
  protected DsSipCancelMessage m_cancelMessage; // = null;
  /** The CANCEL transaction, if this transaction was cancelled. */
  protected DsSipClientTransaction m_cancelTransaction; // = null;

  // CAFFEINE 2.0 DEVELOPMENT - required to handle PRACK message.
  /** The last PRACK message, if this transaction was prack'ed. */
  protected DsSipPRACKMessage m_prackMessage; // = null;
  /** For last PRACK callbacks set. */
  protected transient DsSipClientTransactionInterface m_prackInterface; // = null;
  /** The last PRACK transaction, if this transaction was prack'ed. */
  protected DsSipClientTransaction m_prackTransaction; // = null;
  /**
   * Used to save the Rseq number for the last reliable 1xx response. Initially set to -1 to
   * indicate the first reliable provisional is not received yet.
   */
  protected long m_RSeqNumber = -1;

  /** The initial route set. */
  protected DsSipHeaderList m_initialRouteHeader; // = null;

  /** The timers, obtained from the network object. */
  protected DsSipTimers m_sipTimers;
  /** T1. */
  protected int m_T1;
  /** T2. */
  protected int m_T2;
  /** To. */
  protected int m_To;

  /** The cancel timer duration in seconds (default = 32). */
  protected byte m_cancelTimer = 32;
  /** Number of retransmits sent. */
  protected byte m_TCounter; // = 0;
  /** The maximum number of T1 timeouts before failure. */
  protected byte m_maxT1Timeouts;
  /** The class of response received. */
  protected byte m_responseClass;
  /** The ID of the method that started this transaction. */
  protected byte m_method;
  /** The connection for this transaction. */
  protected ConnectionWrapper m_connection;
  /** The key for this transaction. */
  protected DsSipTransactionKey m_key;
  /** The To tag for this transaction. */
  protected DsByteString m_toTag; // = null;

  /** Reference to the timer task for Tn so that it can be cancelled. */
  DsDiscreteTimerTask m_TimerTaskTn; // = null;
  /** Reference to the timer task for T1 so that it can be cancelled. */
  DsDiscreteTimerTask m_TimerTaskT1; // = null;

  /** <code>true</code> if proxy server mode is enabled. */
  protected boolean m_isProxyServerMode = DsSipTransactionManager.isProxyServerMode();

  // maivu - 11.01.06 - CSCsg22401
  /** Reference to the timer task for Expiration so that it can be cancelled. */
  private TimerTask m_expirationTimerTask;

  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Constructs a client transaction for the specified request and with the specified client
   * transaction interface and transaction parameters.
   *
   * @param request request to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param transactionParams Optional. Reserved for future use.
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipClientTransactionImpl(
      DsSipRequest request,
      DsSipClientTransactionInterface clientInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    this(request, (DsSipClientTransportInfo) null, clientInterface);
  }

  /**
   * Constructs a client transaction for the specified request and with the specified client
   * transaction interface and transport information.
   *
   * @param request request to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param clientTransportInfo If the client wishes to use transport information other than that
   *     held by transport layer, DsSipClientTransportInfo is implemented and passed to this
   *     constructor
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipClientTransactionImpl(
      DsSipRequest request,
      DsSipClientTransportInfo clientTransportInfo,
      DsSipClientTransactionInterface clientInterface)
      throws DsException {
    m_clientTransportInfo = clientTransportInfo;
    m_clientInterface = clientInterface;
    // Global value to turn on/off multiple final response handling???
    if (DsConfigManager.handlingMultipleFinalResponses()) {
      if (m_clientInterface instanceof DsSipMFRClientTransactionInterface) {
        m_multipleFinalResponseEnabled = true;
      }
    }

    m_sipRequest = request;

    m_method = (byte) request.getMethodID();
    createStateTable();
    DsNetwork network = request.getNetworkReliably();
    request.setNetwork(network);
    cat.info("Setting the network " + network + " to request ");
    m_sipTimers = network.getSipTimers();

    // fix for issue CSCsr11815 - Move 100rel support to DsNetwork from DsSipTransactionManager
    m_100relSupport = network.get100relSupport();

    m_simpleResolver = network.getSimpleResolver();

    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "going to use the following SIP timers:\n" + m_sipTimers);
    }
    if (cat.isEnabled(Level.INFO)) {
      cat.log(Level.INFO, "Init()-The request Binding Info:\n" + request.getBindingInfo());
    }
  }

  /** Resume callback queue workers. */
  static void init() {
    if (m_callbackQueue.getMaxThreads() == 0) {
      m_callbackQueue.init();
    }
  }

  /** Stop callback queue workers. Used by DsSipTransactionManager as part of stack shutdown. */
  static void stop() {
    m_callbackQueue.destroy();
  }

  /** Cleanup references when transaction goes to the completed state. */
  protected void nullRefs() {
    m_clientTransportInfo = null;

    m_sipResponse = null;
    // CAFFEINE 2.0 DEVELOPMENT - required to handle provisional response.
    m_sipRelProvisionalResponse = null;
    m_cancelMessage = null;
    m_cancelTransaction = null;
    m_initialRouteHeader = null;
    m_cancelInterface = null;
    m_clientTransportInfo = null;

    // CAFFEINE 2.0 DEVELOPMENT - required to handle PRACK.
    m_prackMessage = null;
    m_prackTransaction = null;
    m_prackInterface = null;

    //  for backward compatibility don't null the request
    if (m_cleanup) {
      m_sipRequest = null;
      if (isInvite()) {
        if (!m_x200Terminated) {
          m_clientInterface = null;
        }
      } else {
        m_clientInterface = null;
      }
    }
  }

  /** Select the correct state table. */
  protected void createStateTable() {
    // CAFFEINE 2.0 DEVELOPMENT - CT_TRANSITIONS has been moved to new class
    // DsSipStateMachineTransitions.
    m_stateTable = new DsSipStateTable(DsSipStateMachineTransitions.CT_TRANSITIONS);
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  ///      DsSipTransaction methods                  //////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /**
   * Starts the transaction. Performs the transition between the initial and next state of the state
   * machine. Perform initialization actions not performed by the constructor.
   *
   * @throws IOException if the execution of the state machine results in an IOException
   * @throws DsException if the transaction is no longer in a state consistent with the semantics of
   *     this method
   */
  public void start() throws IOException, DsException {
    DsSipHeaderList orig_route = m_sipRequest.getHeaders(ROUTE);

    if (orig_route == null) {
      m_initialRouteHeader = null;
    } else {
      m_initialRouteHeader = DsSipHeader.cloneHeaderList(orig_route);
    }

    try {
      DsSipResolver resolver = null;
      DsBindingInfo bi = m_sipRequest.getBindingInfo();
      DsByteString connectionId = null;

      if (null != bi) {
        connectionId = bi.getConnectionId();
        // here don't use the getNetworkReliably method because we want
        // to also set the binding info's network if it isn't already set
        DsNetwork network = bi.getNetwork();
        if (network == null) {
          network = DsNetwork.getDefault();
          bi.setNetwork(network);
          genCat.info("Using the default network");
        }
        genCat.info("Selected network " + network);

        // make sure that the local binding info is set for NAT
        if (network.isBehindNAT() && (bi.getTransport() == Transport.UDP || !bi.isTransportSet())) {
          // check to make sure that a local proxy is not going to change
          // the transport on us.
          Transport lopTransport =
              DsSipTransactionManager.getTransportLayer().getLocalProxyTransport();
          /* TODO
          if (lopTransport == Transport.NONE || lopTransport == Transport.UDP) {
            DsPacketListener listener = network.getUdpListener();
            if (listener != null) {
              bi.setLocalAddress(listener.m_address);
              bi.setLocalPort(listener.m_port);
            }
          }*/

          if (genCat.isEnabled(Level.DEBUG)) {
            genCat.debug("Binding Info After Local Info Set: " + bi);
          }
        }
      } else {
        if (DsNetwork.getDefault().isBehindNAT() && genCat.isEnabled(Level.DEBUG)) {
          genCat.debug("Binding Info is NULL");
        }
      }

      if (connectionId == null) {
        if (genCat.isEnabled(Level.DEBUG)) {
          genCat.debug("Connection ID is NULL, using resolver");
        }

        // here, based on static configuration,
        // decide whether to use a simple or DNS NATPR/SRV resolver
        if (useDeterministicLocator()) {
          resolver = m_simpleResolver ? null : new DsSipDetServerLocator(m_sipRequest);
        } else {
          resolver = m_simpleResolver ? null : new DsSipServerLocator();
        }
        if (genCat.isEnabled(Level.DEBUG)) {
          genCat.debug("Creating Connection Wrapper using Resolver");
        }
        m_connection = new ConnectionWrapper(m_sipRequest, resolver);
      } else {
        if (genCat.isEnabled(Level.DEBUG)) {
          genCat.debug("Using Connection ID: " + connectionId);
        }
        genCat.info("Creating Connection Wrapper using  connectionId " + connectionId);
        m_connection = new ConnectionWrapper(m_sipRequest, connectionId);
      }

      // If the server locator is not being used then, the connection
      // wrapper will itself serve as resolver.
      //
      if (resolvCat.isEnabled(Level.DEBUG)) {
        resolvCat.debug("using " + (m_simpleResolver ? "simple" : "ServerLocator") + " resolver");
      }

      if (null == resolver) {
        resolver = m_connection;
      }

      if ((m_clientTransportInfo != null) && (bi != null)) {
        DsNetwork network = bi.getNetworkReliably();
        Set supported_transports = m_clientTransportInfo.getSupportedTransports(network);
        if (supported_transports != null) {
          resolver.setSupportedTransports(
              DsSipResolverUtils.getMaskedTransports(supported_transports));
        }
      }
    } catch (Throwable t) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("can't connect to SIP server", t);
      }

      IOException ioException =
          new IOException(
              "DsSipClientTransactionImpl.DsSipClientTransactionImpl: "
                  + "can't connect to SIP server: "
                  + t.getMessage());

      ioException.addSuppressed(t);
      throw ioException;
    }

    // here we want an IO Exception...
    try {
      m_connection.check(m_sipRequest);
    } catch (Throwable t) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("can't connect to SIP server", t);
      }
      throw new IOException(
          "DsSipClientTransactionImpl.DsSipClientTransactionImpl: "
              + "can't connect to SIP server: "
              + t.getMessage());
    }

    // CAFFEINE 2.0 DEVELOPMENT - check/set 100rel as supported/required header.
    if (DsSipConstants.BS_INVITE.equals(m_sipRequest.getCSeqMethod()) && !m_isProxyServerMode) {
      // Make sure that the Require: or Supported: 100rel is in place.
      if (m_100relSupport == REQUIRE) {
        if (!m_sipRequest.headerContainsTag(REQUIRE, BS_100REL)) {
          DsSipRequireHeader requireHeader = new DsSipRequireHeader(DsSipConstants.BS_100REL);
          m_sipRequest.addHeader(requireHeader, false, false);
        }
      } else if (m_100relSupport == SUPPORTED) {
        if (!m_sipRequest.headerContainsTag(SUPPORTED, BS_100REL)) {
          DsSipSupportedHeader supportedHeader = new DsSipSupportedHeader(DsSipConstants.BS_100REL);
          m_sipRequest.addHeader(supportedHeader, false, false);
        }
      }
    }

    checkVia(m_sipRequest);

    m_key = m_sipRequest.createKey();
    if (m_key == null) {
      throw new DsException("DsSipClientTransactionImpl.start: can't create transaction key");
    }

    boolean toTag = (m_sipRequest.getKey().getToTag() != null);
    // CAFFEINE 2.0 DEVELOPMENT - required for handling PRACK.
    if (DsSipConstants.BS_PRACK.equals(m_sipRequest.getCSeqMethod())) {
      m_sipRequest.setKeyContext(DsSipTransactionKey.USE_VIA | DsSipTransactionKey.USE_METHOD);
    } else {
      m_sipRequest.setKeyContext(
          DsSipTransactionKey.USE_VIA
              | (toTag ? DsSipTransactionKey.USE_TO_TAG : DsSipTransactionKey.NONE));
    }

    DsSipTransactionManager.addTransaction(
        this, true, // client
        true); // useVia

    initializeTimers();

    if (m_sipTimers.clientTnValue < Integer.MAX_VALUE) {
      if (cat.isEnabled(Level.DEBUG)) {
        debugTraceTimer(true, "m_sipTimers.clientTnValue", "IN_Tn", m_sipTimers.clientTnValue);
      }

      m_TimerTaskTn = DsDiscreteTimerMgr.scheduleNoQ(m_sipTimers.clientTnValue, this, IN_Tn);
    }
    // maivu - 11.01.06 - CSCsg22401
    // Start the timer to track the request expiration.  This timer does not apply for Proxy.
    // The expiration is based on the Expires header of the request.
    // If there is no Expires header, the default will be used which is
    // DsConfigManager.getDefaultInviteExpiration
    // or DsConfigManager.getDefaultNonInviteExpiration
    if (!m_isProxyServerMode) {
      // expires headers for REGISTER and SUBSCRIBE have different meaning
      // and are handled by upper layer. Just handle Invite case here for
      // now. In the future, may handle other method
      if (isInvite()) {
        int timeout = 0;
        DsSipExpiresHeader exp =
            (DsSipExpiresHeader) m_sipRequest.getHeaderValidate(DsSipConstants.EXPIRES);
        if (exp != null) {
          timeout = exp.getValue().parseInt() * 1000;
        } else {
          DsBindingInfo bi = m_sipRequest.getBindingInfo();
          DsNetwork network = bi.getNetworkReliably();
          timeout = network.getDefaultInviteExpiration();
        }
        // create and start the timer
        if (timeout > 0) {
          m_expirationTimerTask = DsTimer.schedule(timeout, new ExpirationTimer(), null);
        }
      }
    }

    execute(DS_CT_IN_START);
  }

  /**
   * Returns <code>false</code> if the transaction is in the DsSipStateMachineDefinitions.DS_INITIAL
   * state. Otherwise returns <code>true</code>.
   *
   * @return <code>false</code> if the transaction is in the DsSipStateMachineDefinitions.DS_INITIAL
   *     state, otherwise returns <code>true</code>.
   */
  public boolean isStarted() {
    return m_stateTable.isStarted();
  }

  /**
   * Returns this transaction's initial request.
   *
   * @return this transaction's initial request.
   */
  public DsSipRequest getRequest() {
    return m_sipRequest;
  }

  /**
   * Test to see whether the transaction is a server transaction.
   *
   * @return true if the server is a server transaction, otherwise returns false
   */
  public boolean isServerTransaction() {
    return false;
  }

  /**
   * Returns <code>true</code> if this transaction uses a proxy server state machine.
   *
   * @return <code>true</code> if this transaction uses a proxy server state machine
   */
  public boolean isProxyServerMode() {
    return m_isProxyServerMode;
  }

  /**
   * If <code>true</code> this transaction will use a proxy server state machine, otherwise it will
   * not.
   *
   * @param mode if <code>true</code> this transaction will use a proxy server state machine,
   *     otherwise it will not.
   */
  public void setProxyServerMode(boolean mode) {
    m_isProxyServerMode = mode;
  }

  /**
   * Returns <code>true</code> if this is an INVITE transaction, otherwise returns <code>false
   * </code>.
   *
   * @return <code>true</code> if this is an INVITE transaction, otherwise returns <code>false
   *     </code>
   */
  public boolean isInvite() {
    return false;
  }

  /**
   * Returns the method of this transaction's request.
   *
   * @return the method of this transaction's request
   */
  public int getMethodID() {
    return m_method;
  }

  /**
   * Return the transaction key for this transaction.
   *
   * @return The transaction key for this transaction
   */
  public DsSipTransactionKey getKey() {
    return m_key;
  }

  /**
   * Returns the current state of this transaction.
   *
   * @return the current state of this transaction
   * @see DsSipStateMachineDefinitions
   */
  public synchronized int getState() {
    return m_stateTable.getState();
  }

  /**
   * Used for debugging.
   *
   * @return a string representation of this transaction
   */
  public String getAsString() {
    String state = DsSipStateTable.printState(m_stateTable.getState());
    DsByteString seq =
        (m_sipRequest == null) ? new DsByteString("??") : m_sipRequest.getCSeqMethod();
    DsByteString cid = (m_sipRequest == null) ? new DsByteString("??") : m_sipRequest.getCallId();
    return "CLIENT_TRANS METHOD:"
        + seq
        + " STATE:"
        + state
        + " KEY: "
        + m_key
        + "\nCALLID:: "
        + (cid != null ? DsByteString.toString(cid) : "null");
  }

  /////////////////////////////////////////////////////////////////////
  ///      DsSipClientTransaction methods            //////////////////
  /////////////////////////////////////////////////////////////////////
  /*
   *  javadoc inherited
   */
  public synchronized void ack(DsSipAckMessage request)
      throws DsException, IOException, UnknownHostException {
    throw new DsStateMachineException("attempt to ACK a non-INVITE");
  }

  /*
   *  javadoc inherited
   */
  public synchronized void cancel(DsSipCancelMessage request) throws DsException, IOException {
    if (m_cancelMessage == null) {
      m_cancelMessage = request;
    }

    execute(DS_CT_IN_CANCEL);
  }

  /*
   *  javadoc inherited
   */
  public synchronized void cancel(
      DsSipClientTransactionInterface cancelInterface, DsSipCancelMessage request)
      throws DsException, IOException {
    m_cancelInterface = cancelInterface;
    cancel(request);
  }

  // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
  /*
   *  javadoc inherited
   */
  public synchronized DsSipClientTransaction prack(DsSipPRACKMessage request)
      throws DsException, IOException {
    throw new DsStateMachineException(
        "attempt to invoke prack operation inside non-invite transaction");
  }

  /*
   *  javadoc inherited
   */
  public synchronized DsSipClientTransaction prack(
      DsSipClientTransactionInterface prackInterface, DsSipPRACKMessage request)
      throws DsException, IOException {
    throw new DsStateMachineException(
        "attempt to invoke prack operation inside non-invite transaction");
  }

  public synchronized void onIOException(IOException exc) {
    if (genCat.isEnabled(Level.WARN)) genCat.warn("onIOException async exception: ", exc);
    try {
      execute(DS_CT_IN_IO_EXCEPTION);
    } catch (DsStateMachineException sme) {
      if (genCat.isEnabled(Level.WARN))
        genCat.warn("onIOException(): state machine exception processing async excption", sme);
    }
  }

  /*
   *  javadoc inherited
   */
  protected synchronized void onResponse(DsSipResponse response) {
    // If we are an endpoint and the response has more than 1 Via, ignore it, as per bis-09
    // We chose to do this only for transactions that we know about, strays are passed
    // to the user code, so it can decide what to do with it
    if (!isProxyServerMode()) {
      DsSipHeaderList vias = response.getViaHeaders();
      if (vias.size() > 1) {
        // We could throw an exception, but the spec. says ignore, so this should be ok
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn(
              "onResponse(): dropping response for with more than one via header (we are a UAC)");
        }
        return;
      }
    }

    //  CANNOT CALL CANCELT1 HERE:  IT VIOLATES THE STATE
    //  MACHINE FOR NON-INVITES WHICH RETRANSMIT UNTIL FINAL IS
    //  RECEIVED
    //  it is fastest (and least risky) to comment it out at the moment
    //  cancelT1();

    // CAFFEINE 2.0 DEVELOPMENT - required to retrieve 1XX provisional response.
    int statusCode = response.getStatusCode();
    // Never reset the response and response class to a provisional
    // response when we have already seen a final response.
    byte responseClass = (byte) response.getResponseClass();
    if (m_responseClass >= 2 && responseClass == 1) {
      if (genCat.isEnabled(Level.INFO)) {
        genCat.info(
            "onResponse(): dropping provisional Response since a final Response has already been received for this transaction");
      }
      return;
    }
    m_responseClass = responseClass;
    m_sipResponse = response;

    try {
      // per rfc3263, we should failover on a 503
      if (response.getStatusCode() == DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE) {
        // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
        if (!m_simpleResolver) {
          if (m_connection.getBindingInfo() != null) {
            DsUnreachableDestinationTable.getInstance()
                .add(
                    m_connection.getBindingInfo().getRemoteAddress(),
                    m_connection.getBindingInfo().getRemotePort(),
                    m_connection.getBindingInfo().getTransport());
          }
        }

        // TODO: Switch threads: this might be too much work to do in the
        //   DATAI thread... -dg
        execute(DS_CT_IN_SERVICE_UNAVAILABLE);
      } else {
        switch (m_responseClass) {
          case DsSipResponseCode.DS_INFORMATIONAL:
            // CAFFEINE 2.0 DEVELOPMENT - required by handling provisional response.
            if (m_stateTable.getState() == DS_CTI_RELPROCEEDING
                && statusCode == DsSipResponseCode.DS_RESPONSE_TRYING) {
              // Ignore it, don't set m_sipResponse.
              genCat.warn(
                  "onResponse(): 100 response is received after reliable provisional response and will be ignored.");
              return;
            }
            if (m_100relSupport != UNSUPPORTED
                && response.headerContainsTag(REQUIRE, BS_100REL)
                && !m_isProxyServerMode) {
              // check the RSeq number to make sure that
              // - it does have the RSeq header;
              // - the 1xx come in order.
              DsSipRSeqHeader sipRSeqHeader =
                  (DsSipRSeqHeader) response.getHeaderValidate(DsSipConstants.RSEQ);
              if (sipRSeqHeader != null) {
                // No need to check on this when it's retransmission.
                if (!findAndUpdateRetransmission()) {
                  long RSeqNumber = sipRSeqHeader.getNumber();
                  // check validity of RSeq number
                  if (RSeqNumber < MIN_RSEQ || RSeqNumber > MAX_RSEQ) {
                    genCat.warn(
                        "onResponse(): the RSeq number ("
                            + RSeqNumber
                            + ") in the reliable provisional response is invalid.");
                    return;
                  }
                  if (m_RSeqNumber != -1 && RSeqNumber != m_RSeqNumber + 1) {
                    genCat.warn(
                        "onResponse(): the RSeq number in the reliable provisional response is NOT in order and will be ignored.");
                    return;
                  }
                  m_RSeqNumber = RSeqNumber;
                }
              } else {
                // Reliable provisional response "MUST include an RSeq header". Ignore it and Warn
                genCat.warn(
                    "onResponse(): reliable provisional response does NOT include RSeq header and will be ignored.");
                return;
              }
              // Save the reliable provisional response separately
              m_sipRelProvisionalResponse = response;

              execute(DS_CT_IN_REL_PROVISIONAL);
            } else if (m_100relSupport == REQUIRE
                && statusCode != DsSipResponseCode.DS_RESPONSE_TRYING
                && !response.headerContainsTag(REQUIRE, BS_100REL)
                && !m_isProxyServerMode) {
              // UAC requested reliable provisional response.
              genCat.warn(
                  "onResponse(): reliable provisional response does NOT include Require header (while the UAC requested it) and will be ignored.");
              return;
            } else {
              execute(DS_CT_IN_PROVISIONAL);
            }

            break;
          case DsSipResponseCode.DS_SUCCESS:
            execute(DS_CT_IN_2XX);
            break;
          default:
            execute(DS_CT_IN_3TO6XX);
            break;
        }
      }
    } catch (DsStateMachineException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("onResponse(): state machine exception processing response", dse);
      }
    }
    // CAFFEINE 2.0 DEVELOPMENT - required to catch other possible ds exceptions.
    catch (DsException dsex) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("Error from response.getHeaderValidate(RSEQ)", dsex);
      }
    }
  }

  /*
   *  javadoc inherited
   */
  protected DsByteString getToTag() {
    return m_toTag;
  }

  /*
   *  javadoc inherited
   */
  protected void setToTag(DsByteString toTag) {
    m_toTag = toTag;
  }

  /**
   * Create a client transaction to replace this one when a 503 is recieved.
   *
   * @param connection the connection to use to send the ACK (not used for non INVITE transaction).
   * @param via the via header for the ACK (not used for non INVITE transaction).
   * @param key the transaction key
   * @return a client transaction to replace this one when a 503 is recieved.
   */
  protected DsSipClientTransaction createServiceUnavailableHandler(
      DsSipConnection connection, DsSipViaHeader via, DsSipTransactionKey key) {
    return new ServiceUnavailableHandler(m_To, key);
  }

  /*
   *  javadoc inherited
   */
  protected DsSipClientTransaction createCopy(DsSipResponse response) throws DsException {
    return null; // Copying only supported in INVITE client transactions i.e.
    // DsSipClientTransactionIImpl
  }

  /*
   *  javadoc inherited
   */
  protected void onMultipleFinalResponse(
      DsSipClientTransaction originalTransaction, DsSipResponse response) {
    // Do nothing. Multiple final responses only valid for
    // INVITE client transactions i.e. DsSipClientTransactionIImpl
  }

  /*
   *  javadoc inherited
   */
  protected boolean multipleFinalResponsesEnabled() {
    return m_multipleFinalResponseEnabled;
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////
  ///       State Machine implemenation              //////////////////
  /////////////////////////////////////////////////////////////////////

  /**
   * This is the method by which input is delivered to the state machine.
   *
   * @param input the input to the state machine
   * @throws DsStateMachineException if error occurs in the state machine
   * @see DsSipStateMachineDefinitions
   */
  protected void execute(int input) throws DsStateMachineException {
    synchronized (this) {
      int transition = m_stateTable.switchState(input);
      try {
        switch (m_stateTable.getState()) {
          case DS_INITIAL:
            initial(transition);
            break;
          case DS_CALLING:
            calling(transition);
            break;
          case DS_PROCEEDING:
            proceeding(transition);
            break;
          case DS_COMPLETED:
            completed(transition);
            break;

          case DS_TERMINATED:
            terminated(transition);
            break;
          default:
            break;
        }
      } catch (DsStateMachineException dse) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionImpl.execute - DSE", dse);
        }
        throw dse;
      } catch (IOException exc) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionImpl.execute", exc);
        }
        // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
        if (!m_simpleResolver) {
          if (m_connection.getBindingInfo() != null) {
            DsUnreachableDestinationTable.getInstance()
                .add(
                    m_connection.getBindingInfo().getRemoteAddress(),
                    m_connection.getBindingInfo().getRemotePort(),
                    m_connection.getBindingInfo().getTransport());
          }
        }

        execute(DS_CT_IN_IO_EXCEPTION);
      } catch (DsException dse) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionImpl.execute", dse);
        }
        execute(DS_CT_IN_OTHER_EXCEPTION);
      } catch (Exception exc) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionImpl.execute", exc);
        }
        execute(DS_CT_IN_OTHER_EXCEPTION);
      }
    }
  }

  /**
   * The initial state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if some error occurs in the state machine
   */
  protected void initial(int transition) throws DsException {
    switch (transition) {
      case (DS_CALLING | DS_CT_IN_NEXT_SERVER):
        initializeTimers();
        execute(DS_CT_IN_START);
        break;
    }
  }

  /**
   * The calling state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void calling(int transition) throws DsException, IOException {
    boolean rtx;
    switch (transition) {
      case DS_INITIAL | DS_CT_IN_START:
        m_connection.send(m_sipRequest);

        // Set timer for all transports
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
        }
        m_TimerTaskT1 = DsDiscreteTimerMgr.scheduleNoQ(m_T1, this, IN_T1);

        break;
      case DS_CALLING | DS_CT_IN_T1:
        if (m_TCounter++ < m_maxT1Timeouts) {
          backOff();

          // Timer is set for both reliable and unreliable transports.
          // Only send the retransmission if unreliable.
          if (!DsSipTransportType.intern(m_connection.getTransport().name()).isReliable()) {
            m_connection.send(m_sipRequest);
          }

          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
          }
          m_TimerTaskT1 = DsDiscreteTimerMgr.scheduleNoQ(m_T1, this, IN_T1);
        } else {
          // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
          if (!m_simpleResolver) {
            if (m_connection.getBindingInfo() != null) {
              // CSCts72798: Only Blacklist Dialog Creating Failures, not Mid-dialog Failures
              // Only add to the black list when the failure does not come from mid-dialog requests
              if (!isMidDialogRequest(m_sipRequest)) {
                DsUnreachableDestinationTable.getInstance()
                    .add(
                        m_connection.getBindingInfo().getRemoteAddress(),
                        m_connection.getBindingInfo().getRemotePort(),
                        m_connection.getBindingInfo().getTransport());
              }
            }
          }

          if (m_connection.tryNextServer(m_sipRequest)) {
            execute(DS_CT_IN_NEXT_SERVER);
          } else {
            execute(DS_CT_IN_T1_EXPIRED);
          }
        }
        break;
      case DS_CALLING | DS_CT_IN_CANCEL:
        createCancelTransaction(transition);
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_cancelTimer*1000", "IN_CANCEL_TIMER", m_cancelTimer * 1000);
        }
        DsDiscreteTimerMgr.schedule(m_cancelTimer * 1000, this, IN_CANCEL_TIMER);
        break;
      case DS_CALLING | DS_CT_IN_SERVICE_UNAVAILABLE:
      case DS_PROCEEDING | DS_CT_IN_SERVICE_UNAVAILABLE:
        // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
      case DS_CTI_RELPROCEEDING | DS_CT_IN_SERVICE_UNAVAILABLE:
        rtx = findAndUpdateRetransmission();

        // save a reference to the previous connection
        DsSipConnection connection = m_connection.getConnection();
        DsSipViaHeader via = (DsSipViaHeader) m_sipRequest.getViaHeader().clone();
        DsSipTransactionKey key = (DsSipTransactionKey) m_key.clone();

        // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
        if (!m_simpleResolver) {
          if (m_connection.getBindingInfo() != null) {
            DsUnreachableDestinationTable.getInstance()
                .add(
                    m_connection.getBindingInfo().getRemoteAddress(),
                    m_connection.getBindingInfo().getRemotePort(),
                    m_connection.getBindingInfo().getTransport());
          }
        }

        if (m_connection.tryNextServer(m_sipRequest)) {
          // spin off another client transaction
          try {
            // In this case, we've gotten here because we're treating a
            //   503 response (SERVICE_UNAVAILABLE)  as an IOException.
            DsSipClientTransaction suh = createServiceUnavailableHandler(connection, via, key);

            //  replace the current transaction with the service unavailable handler
            DsSipTransactionManager.replaceClientTransaction(suh);

            suh.start();

            m_replaced = true;

            // continue on.....
            m_sipResponse = null;

            // CSCsz50949: ACK not sent in proxy call flow
            // Null out the to tag so that this transaction does not have the To tag from
            // the original transaction which is now the suh transaction
            m_toTag = null;

            // CAFFEINE 2.0 DEVELOPMENT - required by handling provisional response.
            m_sipRelProvisionalResponse = null;
            m_responseClass = 0;
          } catch (Exception exc) {
            if (genCat.isEnabled(Level.WARN)) {
              genCat.warn("error starting service unavailable handler", exc);
            }

            // if something goes wrong,  put this transaction
            //   back and go down the path of having received a
            //   final
            DsSipTransactionManager.replaceClientTransaction(this);
            execute(DS_CT_IN_3TO6XX);
            return;
          }

          execute(DS_CT_IN_NEXT_SERVER);
        } else // there are no more servers to try
        {
          // replace the connection so we can send the ACK
          m_connection.replace(connection);
          // here we have a 503 (SERVICE_UNAVAILABLE), but no
          //   more servers to try, so let the application handle
          //   the 503
          execute(DS_CT_IN_3TO6XX);
        }
        break;
      case DS_CALLING | DS_CT_IN_IO_EXCEPTION:
        if (m_connection.tryNextServer(m_sipRequest)) {
          execute(DS_CT_IN_NEXT_SERVER);
        } else // there are no more servers to try
        {
          execute(DS_CT_IN_NO_SERVER);
        }
        break;
        // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
      case DS_CTI_RELPROCEEDING | DS_CT_IN_2XX:
        rtx = findAndUpdateRetransmission();
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        if (m_isProxyServerMode && !m_x200Terminated) {
          // proxy does not generate ACK, so get rid of this trans
          cancelTn();
          cleanup();
        } else {
          // use CTIX state table
          m_stateTable.setStateTable(DsSipStateMachineTransitions.CTIX_TRANSITIONS);
          m_stateTable.m_curState = DS_XCOMPLETED;
          if (!m_isProxyServerMode) {
            // UAC core considers trans complete 64*T1 after first 2XX
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(
                  true, "64 * DsSipTimers.T1Value", "IN_TIMEOUT", 64 * m_sipTimers.T1Value);
            }
            DsDiscreteTimerMgr.scheduleNoQ(64 * m_sipTimers.T1Value, this, IN_TIMEOUT);
          } else {
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(true, "m_sipTimers.TU1Value", "IN_Tn", m_sipTimers.TU1Value);
            }
            m_TimerTaskTn = DsDiscreteTimerMgr.scheduleNoQ(m_sipTimers.TU1Value, this, IN_Tn);
          }
          cancelTn(); // transaction will terminate normally
        }

        break;
      default:
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("transition not valid: " + transition);
        }
        m_stateTable.throwException(transition);
        break;
    }
  }

  /**
   * The proceeding state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void proceeding(int transition) throws DsException, IOException {
    switch (transition) {
      case DS_CALLING | DS_CT_IN_PROVISIONAL:
        boolean rtx = findAndUpdateRetransmission();
        m_T1 = m_T2;
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_PROVISIONAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        break;
      case DS_PROCEEDING | DS_CT_IN_T1:
        if (m_TCounter++ < m_maxT1Timeouts) {
          m_T1 = m_T2;

          // Timer is set for both reliable and unreliable transports.
          // Only send the retransmission if unreliable.
          if (!DsSipTransportType.intern(m_connection.getTransport()).isReliable()) {
            m_connection.send(m_sipRequest);
          }

          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
          }
          m_TimerTaskT1 = DsDiscreteTimerMgr.scheduleNoQ(m_T1, this, IN_T1);
        } else {
          // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
          if (!m_simpleResolver) {
            if (m_connection.getBindingInfo() != null) {
              // CSCts72798: Only Blacklist Dialog Creating Failures, not Mid-dialog Failures
              // Only add to the black list when the failure does not come from mid-dialog requests
              if (!isMidDialogRequest(m_sipRequest)) {
                DsUnreachableDestinationTable.getInstance()
                    .add(
                        m_connection.getBindingInfo().getRemoteAddress(),
                        m_connection.getBindingInfo().getRemotePort(),
                        m_connection.getBindingInfo().getTransport());
              }
            }
          }

          execute(DS_CT_IN_T1_EXPIRED);
        }
        break;
      case DS_PROCEEDING | DS_CT_IN_CANCEL:
        createCancelTransaction(transition);
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_cancelTimer*1000", "IN_CANCEL_TIMER", m_cancelTimer * 1000);
        }
        DsDiscreteTimerMgr.schedule(m_cancelTimer * 1000, this, IN_CANCEL_TIMER);
        break;
      case DS_PROCEEDING | DS_CT_IN_PROVISIONAL:
        boolean rtx1 = findAndUpdateRetransmission();

        new ClientTransactionCallback(
                ClientTransactionCallback.CB_PROVISIONAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        break;
      default:
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("transition not valid: " + transition);
        }
        m_stateTable.throwException(transition);
        break;
    }
  }

  /**
   * The completed state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void completed(int transition) throws DsException, IOException {
    // maivu - 11.01.06 - CSCsg22401 Transition to Completed state so cancel the request expiration
    // timer
    // which was set in the DsSipClientTransactionImpl.start()
    cancelExpirationTimer();

    boolean rtx = false;
    switch (transition) {
      case DS_CALLING | DS_CT_IN_2XX:
      case DS_CALLING | DS_CT_IN_3TO6XX:
      case DS_PROCEEDING | DS_CT_IN_2XX:
      case DS_PROCEEDING | DS_CT_IN_3TO6XX:
        rtx = findAndUpdateRetransmission();
        // CAFFEINE 2.0 DEVELOPMENT - use response to determined which callback should be invoked.
        if (m_sipResponse.getMethodID() != DsSipConstants.CANCEL) {
          new ClientTransactionCallback(
                  ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
              .call();
        } else {
          if (m_cancelCallback) {
            new ClientTransactionCallback(
                    ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
                .call();
          }
        }

        nullRefs();
        if (m_To != 0) {
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
          }
          DsDiscreteTimerMgr.scheduleNoQ(m_To, this, IN_TIMEOUT);
        } else {
          execute(DS_CT_IN_TIMEOUT);
        }
        cancelTn(); // transaction will terminate normally
        break;
      case DS_COMPLETED | DS_CT_IN_T1:
        break;
      case DS_COMPLETED | DS_CT_IN_CANCEL_TIMER:
        break;
      case DS_COMPLETED | DS_CT_IN_PROVISIONAL:
        rtx = findAndUpdateRetransmission();
        break;
      case DS_COMPLETED | DS_CT_IN_SERVICE_UNAVAILABLE:
        // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
        if (!m_simpleResolver) {
          if (m_connection.getBindingInfo() != null) {
            DsUnreachableDestinationTable.getInstance()
                .add(
                    m_connection.getBindingInfo().getRemoteAddress(),
                    m_connection.getBindingInfo().getRemotePort(),
                    m_connection.getBindingInfo().getTransport());
          }
        }
      case DS_COMPLETED | DS_CT_IN_2XX:
      case DS_COMPLETED | DS_CT_IN_3TO6XX:
        break;
      default:
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("transition not valid: " + transition);
        }
        m_stateTable.throwException(transition);
        break;
    }
  }

  /**
   * The terminated state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void terminated(int transition) throws DsException, IOException {
    // maivu - 11.01.06 - CSCsg22401 Transition to Terminated state so cancel the request expiration
    // timer
    // which was set in the DsSipClientTransactionImpl.start()
    cancelExpirationTimer();

    switch (transition) {
      case DS_TERMINATED | DS_CT_IN_CANCEL:
        createCancelTransaction(transition);
        break;
      case DS_TERMINATED | DS_CT_IN_ACK:
        throw new DsException("Can't do ACK in TERMINATED state");
      default:
        /*
         * if we have come from any state but the terminated state,
         *   then cleanup, else do nothing
         */
        switch (transition & DS_MASK) {
          case (DS_TERMINATED):
            break;
          default:
            switch (transition & DS_INPUT_MASK) {
              case (DS_CT_IN_NO_SERVER):
                cancelTn();
                new ClientTransactionCallback(
                        ClientTransactionCallback.CB_EXCEPTION, null, m_clientInterface)
                    .call();
                break;
              case (DS_CT_IN_T1_EXPIRED):
              case (DS_CT_IN_CANCEL_TIMER):
                cancelTn();
              case (DS_CT_IN_Tn):
                new ClientTransactionCallback(
                        ClientTransactionCallback.CB_TIMEOUT, null, m_clientInterface)
                    .call();
                break;
              default:
                cancelTn();
                break;
            }
            cleanup();
            break;
        }
    }
  }

  /** Remove the transaction and release resources. */
  void cleanup() {
    if (m_isCleanedup) return;
    nullRefs();
    m_connection.release();
    try {
      DsSipTransactionManager.removeTransaction(this);
    } catch (DsException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("terminated(): Failed to remove transaction ", dse);
      }
    }
    m_isCleanedup = true;
  }

  /**
   * Terminates this transaction and free up the resources. Additionally this method facilitate the
   * implementation of Timer C functionality for the proxy application.
   *
   * <p>Note that this method should only be invoked by proxy server when it is handling Timer C
   * firing for an invite client transaction.
   *
   * @see #cancelTn()
   */
  public synchronized void terminate() {
    cancelTn();
    cleanup();
  }

  ////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////

  /** Create the client transaction for the CANCEL */
  void createCancelTransaction(int transition) throws IOException, DsException {
    if (m_cancelTransaction != null) {
      m_stateTable.throwException(transition, "Cancel transaction already exists");
    }

    if (m_cancelMessage == null) {
      if (m_sipRequest == null) {
        m_stateTable.throwException(
            transition,
            "Initial request has been nulled (cleaned up): can't create CANCEL message");
      }
      m_cancelMessage = new DsSipCancelMessage(m_sipRequest);
      // add route headers if they are present in the request so that they will
      //   go through the same path.
      m_cancelMessage.removeHeaders(ROUTE);
      m_cancelMessage.addHeaders(m_initialRouteHeader);
    }

    if (m_cancelMessage.getViaHeader() == null) {
      if (m_sipRequest == null) {
        m_stateTable.throwException(
            transition,
            "Initial request has been nulled (cleaned up): can't create via for CANCEL message");
      }
      m_cancelMessage.addHeader(
          m_sipRequest.getViaHeader(),
          true, // start
          true); // clone
    }

    // Check if cancel message has a connection id set if not then
    // Check if the Connection Id for the original request is set, if so,
    // set that for the CANCEL too.
    DsBindingInfo cancelBindingInfo = m_cancelMessage.getBindingInfo();
    if (cancelBindingInfo != null && cancelBindingInfo.getConnectionId() == null) {
      if (null != m_sipRequest) {
        DsBindingInfo bi = m_sipRequest.getBindingInfo();
        if (null != bi && bi.getConnectionId() != null) {
          cancelBindingInfo.setConnectionId(bi.getConnectionId());
        }
      }
    }

    if (m_cancelInterface == null) {
      m_cancelTransaction =
          DsSipTransactionManager.getTransactionManager()
              .startClientTransaction(
                  (DsSipRequest) m_cancelMessage, m_clientTransportInfo, m_clientInterface);
    } else {
      m_cancelTransaction =
          (DsSipClientTransactionImpl)
              DsSipTransactionManager.getTransactionManager()
                  .createClientTransaction(
                      (DsSipRequest) m_cancelMessage, m_clientTransportInfo, m_cancelInterface);
      ((DsSipClientTransactionImpl) m_cancelTransaction).setCancelCallback(true);
      m_cancelInterface = null;
      m_cancelTransaction.start();
    }
  }

  /**
   * If <code>true</code> the user code will be notified of responses to the CANCEL request.
   *
   * @param cancel_callback if <code>true</code> the user code will be notified of responses to the
   *     CANCEL request
   */
  public void setCancelCallback(boolean cancel_callback) {
    m_cancelCallback = cancel_callback;
  }

  // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
  /** Create the client transaction for the PRACK. */
  void createPrackTransaction(int transition, DsSipPRACKMessage prackMessage)
      throws IOException, DsException {
    if (m_prackTransaction != null) {
      m_stateTable.throwException(transition, "Prack transaction already exists");
    }

    if (prackMessage == null) {
      m_stateTable.throwException(
          transition, "Can't create PRACK transaction from null PRACK message");
    }

    // Check if the Connection Id for the original request is set, if so,
    // set that for the PRACK too.
    m_prackMessage
        .getBindingInfo()
        .setConnectionId(m_prackMessage.getBindingInfo().getConnectionId());

    if (m_prackInterface == null) {
      m_prackTransaction =
          DsSipTransactionManager.getTransactionManager()
              .startClientTransaction(m_prackMessage, m_clientTransportInfo, m_clientInterface);
    } else {
      m_prackTransaction =
          DsSipTransactionManager.getTransactionManager()
              .createClientTransaction(m_prackMessage, m_clientTransportInfo, m_prackInterface);
      m_prackInterface = null;
      m_prackTransaction.start();
    }
  }

  /*
   *  javadoc inherited
   */
  public void run(Object arg) {
    DsLog4j.logSessionId(m_sipRequest);
    try {
      if (arg == IN_CANCEL_TIMER) {
        execute(DS_CT_IN_CANCEL_TIMER);
      } else if (arg == IN_T1) {
        execute(DS_CT_IN_T1);
      } else if (arg == IN_Tn) {
        execute(DS_CT_IN_Tn);
      } else if (arg == IN_TIMEOUT) {
        execute(DS_CT_IN_TIMEOUT);
      }
    } catch (DsStateMachineException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("run(): state machine exception processing timer event", dse);
      }
    }
  }

  /** Retransmission backoff per the SIP spec. */
  protected void backOff() {
    if (m_T1 < m_T2) {
      m_T1 *= 2;
    } else {
      m_T1 = m_T2;
    }
  }

  /** Initialize timers. */
  protected void initializeTimers() {
    m_maxT1Timeouts = m_sipTimers.CLIENT_TRANS_RETRY;
    m_T1 = m_sipTimers.T1Value;
    m_T2 = m_sipTimers.T2Value;
    m_TCounter = 0;
    m_To = m_sipTimers.T4Value;

    Transport transport = m_connection.getTransport();
    boolean reliable = DsSipTransportType.intern(transport).isReliable();
    if (reliable) {
      m_To = 0;
    }
  }

  /**
   * Check for the presence of a Via header and whether the transport matches that of the current
   * connection. Also, if the DsNetwork for this request is set to be behind a NAT, the rport
   * parameter will be added to the via header.
   *
   * @param request the SIP request for which the via is being checked
   * @return true if the via changed
   * @throws DsException If no transport listener is found
   */
  protected boolean checkVia(DsSipRequest request) throws DsException {
    DsNetwork network = request.getNetworkReliably();
    DsSipViaHeader via = null;
    try {
      via = request.getViaHeaderValidate();
    } catch (Exception exc) {
      throw new DsException("checkVia: invalid via header");
    }

    // the via transport matches the current transport and the via has a branch param
    if ((via != null)
        && via.getTransport() == m_connection.getTransport()
        && via.getBranch() != null) {
      return checkViaRPort(via, network);
    }

    boolean mismatch = false;
    Transport transport = m_connection.getTransport();
    int port = 0;
    String addr = null;

    if (via == null || (via != null && via.getTransport() != m_connection.getTransport())) {
      mismatch = true;
      if (m_clientTransportInfo == null) {
        ConnectionKey listen_key =
            DsSipTransactionManager.getTransportLayer()
                .findListenKeyForTransport(m_connection.getTransport());

        if (listen_key != null) {
          addr = listen_key.getRemoteAddress().getHostAddress();
          port = listen_key.getRemotePort();
        } else {
          throw new DsException("can't find a " + transport + " listener transport");
        }
      } else {
        DsBindingInfo binfo = m_clientTransportInfo.getViaInfoForTransport(transport, network);
        if (binfo != null) {
          addr = binfo.getRemoteAddressStr();
          port = binfo.getRemotePort();
        } else {
          throw new DsException(
              "checkVia: user code getViaInfoForTransport returns null for  "
                  + transport
                  + "  transport");
        }
      }
    }

    if (via != null) {
      if (mismatch) {
        via.setTransport(transport);
        via.setHost(new DsByteString(addr));
        via.setPort(port);
      }

      if (!m_jainCompatability) {
        if (via.getBranch() == null) {
          via.setBranch(DsSipRequest.getBranchIdInterface().nextBranchId(null));
        }
      }
    } else {
      via = new DsSipViaHeader(new DsByteString(addr), port, transport);
      if (!m_jainCompatability) {
        via.setBranch(DsSipRequest.getBranchIdInterface().nextBranchId(null));
      }
      // this one DOES NOT clone the header
      request.addHeader(via, true, false);
    }

    checkViaRPort(via, network);

    //  here since the via has changed, we have to reserialize the
    //   message :(
    request.setFinalised(false);

    // never change the key on the update of the Via header for the ACK
    if (request.getMethodID() == DsSipConstants.ACK) {
      return false;
    }
    return true;
  }

  // here we are choosing to remove the rport string if the network is
  // set that way and it is NAT'd
  private boolean checkViaRPort(DsSipViaHeader via, DsNetwork network) {
    if (network.isBehindNAT() && !via.isRPortSet()) {
      if (network.getAddClientSideRPort()) {
        if (!via.isRPortNoValue()) {
          via.setRPort();
          return true;
        }
      } else {
        if (via.isRPortNoValue()) {
          via.removeRPort();
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Inform the transaction manager that this transaction's key has changed. Used during failover to
   * alternate transport.
   *
   * @param request the original request
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected void replaceKey(DsSipRequest request) throws DsException {
    if (request.getMethodID() == DsSipConstants.ACK) {
      if (genCat.isEnabled(Level.WARN))
        genCat.warn("Trying to replace the transaction key using the ACK, should NOT get here");
      return;
    }

    DsSipTransactionKey old_key = m_key;
    m_key = m_sipRequest.createKey();

    if (!m_key.equals(old_key)) {
      DsSipTransactionManager.replaceClientKey(this, old_key);
    }
  }

  /**
   * Bumps the branch Id to the next unique value, only if the stack is enabled to use the Branch ID
   * as the transaction key.
   */
  private void changeBranchId(DsSipRequest request) {
    if (request.nextBranchId()) {
      request.setFinalised(false);
      // here, the original client transaction has already been
      //   replaced by the service unavailable handler...
      if (m_replaced) {
        m_replaced = false;

        try {
          DsSipTransactionManager.addTransaction(
              this, true, // client
              true); // useVia
        } catch (Exception exc) {
          if (genCat.isEnabled(Level.WARN)) {
            genCat.warn("error adding client transaction on failover", exc);
          }
        }
      } else {
        try {
          replaceKey(request);
        } catch (DsException e) {
          // its just there for historical reasons. Never thrown.
        }
      }
    }
  }

  /**
   * Logs information for debugging.
   *
   * @param noq <code>true</code> if no queue is being used
   * @param varname the variable name
   * @param inname the input name
   * @param time the duration of the timer, in milliseconds
   */
  protected void debugTraceTimer(boolean noq, String varname, String inname, int time) {
    Logger cat = DsLog4j.LlSMClientTimersCat;
    cat.debug(
        " SCHEDULE_"
            + (noq ? "NOQUEUE" : "QUEUE")
            + " table: "
            + m_stateTable.getName()
            + " variable: "
            + varname
            + " input: "
            + inname
            + " time: "
            + time);
  }

  /**
   * Cancels the Tn timer for this transaction. Additionally this method facilitates the
   * implementation of Timer C functionality for the proxy application.
   *
   * <p>On start of any Client Transaction, Timer Tn is scheduled, that decides the life time of the
   * transaction. For an application ( for example, proxy server) to be able to extend the life time
   * of such a transaction, this method and {@link #terminate()} methods are provided. The
   * application can invoke {@link #cancelTn()} first and then can set its own timer (Timer C), and
   * when this timer fires, the application can invoke {@link #terminate()} to terminate this
   * transaction. Note that this method should only be invoked by proxy server when it is handling
   * Timer C firing for an invite client transaction.
   */
  public final void cancelTn() {
    if (m_TimerTaskTn != null) {
      m_TimerTaskTn.cancel();
      m_TimerTaskTn = null;
    }
  }

  final void cancelT1() {
    if (m_TimerTaskT1 != null) {
      m_TimerTaskT1.cancel();
      m_TimerTaskT1 = null;
    }
  }

  // maivu - 11.01.06 - CSCsg22401
  // Cancel the expiration timer if set before
  public final void cancelExpirationTimer() {
    if (m_expirationTimerTask != null) {
      m_expirationTimerTask.cancel();
      m_expirationTimerTask = null;
    }
  }

  /**
   * Gets the value of the Use Determistic Locator parameter.
   *
   * @return the value of the Use Determistic Locator parameter
   */
  public static boolean useDeterministicLocator() {
    return m_useDeterministicLocator;
  }

  /**
   * Sets the value of the Use Determistic Locator parameter.
   *
   * @param useDeterministicLocator use <code>true</code> use the determistic locator (default), use
   *     <code>false</code> to use the standard random one
   */
  public static void setUseDeterministicLocator(boolean useDeterministicLocator) {
    m_useDeterministicLocator = useDeterministicLocator;
  }

  // CSCts72798: Only Blacklist Dialog Creating Failures, not Mid-dialog Failures
  // Only add to the black list when the failure does not come from mid-dialog requests
  /**
   * Determine if this is a mid-dialog request by looking for a To Tag. A <code>null</code> request
   * returns false.
   *
   * @return <code>true</code> if the request has a populated To Tag, else <code>false</code>
   */
  public static boolean isMidDialogRequest(DsSipRequest request) {
    if (request == null) {
      return false;
    }

    DsByteString tag = request.getToTag();

    // If there is a To Tag, then this is a mid dialog request.
    return (tag != null) && (tag.length() > 0);
  }

  // //////////////////////////////////////////////////////////////
  // /////////// inner  class ClientTransactionCallback  //////////
  // //////////////////////////////////////////////////////////////

  class ClientTransactionCallback implements DsUnitOfWork {
    DsSipClientTransactionInterface cb;
    DsSipResponse response;
    DsSipClientTransaction originalTransaction;
    int type;

    public static final int CB_PROVISIONAL_RESPONSE = 0;
    public static final int CB_FINAL_RESPONSE = 1;
    public static final int CB_TIMEOUT = 2;
    public static final int CB_EXCEPTION = 3;
    public static final int CB_MULTIPLE_FINAL_RESPONSE = 4;
    // CAFFEINE 2.0 DEVELOPMENT - required by handling rel provisional response.
    public static final int CB_REL_PROVISIONAL_RESPONSE = 5;

    ClientTransactionCallback(
        int type, DsSipResponse response, DsSipClientTransactionInterface cb) {
      this.cb = cb;
      this.response = response;
      this.type = type;
    }

    ClientTransactionCallback(
        DsSipClientTransaction originalTransaction,
        DsSipResponse response,
        DsSipClientTransactionInterface cb) {
      this.cb = cb;
      this.response = response;
      this.type = CB_MULTIPLE_FINAL_RESPONSE;
      this.originalTransaction = originalTransaction;
    }

    public void call() {
      m_callbackQueue.nqueue(this);
    }

    public void run() {
      process();
    }

    public void process() {
      if (!DsLog4j.logSessionId(DsSipClientTransactionImpl.this.m_sipResponse))
        DsLog4j.logSessionId(DsSipClientTransactionImpl.this.m_sipRequest);

      if (type == CB_MULTIPLE_FINAL_RESPONSE) {
        if (cb instanceof DsSipMFRClientTransactionInterface) {
          ((DsSipMFRClientTransactionInterface) cb)
              .multipleFinalResponse(
                  originalTransaction, DsSipClientTransactionImpl.this, response);
        }
        return;
      }

      if (cb == null) {
        cb =
            DsSipTransactionManager.getTransactionManager()
                .getStatelessClientTransactionInterface();
      }

      switch (type) {
        case (CB_PROVISIONAL_RESPONSE):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.debug("CB_PROVISIONAL_RESPONSE: ");
            cbCat.debug(String.valueOf(m_key));
            cbCat.debug("CB_PROVISIONAL_RESPONSE: ");
            cbCat.debug(response.maskAndWrapSIPMessageToSingleLineOutput());
          }

          cb.provisionalResponse(DsSipClientTransactionImpl.this, response);
          response = null;
          break;
          // CAFFEINE 2.0 DEVELOPMENT - required by handling rel provisional response.
        case (CB_REL_PROVISIONAL_RESPONSE):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.log(Level.DEBUG, "CB_REL_PROVISIONAL_RESPONSE: ");
            cbCat.log(Level.DEBUG, String.valueOf(m_key));
            cbCat.log(Level.DEBUG, "CB_REL_PROVISIONAL_RESPONSE: ");
            cbCat.log(Level.DEBUG, response.maskAndWrapSIPMessageToSingleLineOutput());
          }

          cb.provisionalResponse(DsSipClientTransactionImpl.this, response);
          response = null;
          break;
        case (CB_FINAL_RESPONSE):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.debug("CB_FINAL_RESPONSE: ");
            cbCat.debug(String.valueOf(m_key));
            cbCat.debug("CB_FINAL_RESPONSE: ");
            cbCat.debug(response.maskAndWrapSIPMessageToSingleLineOutput());
          }

          cb.finalResponse(DsSipClientTransactionImpl.this, response);
          response = null;
          break;
        case (CB_TIMEOUT):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.debug("CB_TIMEOUT: ");
            cbCat.debug(String.valueOf(m_key));
          }

          if (DsSipTransportLayer.getCloseConnectionOnTimeout()) {
            if (m_connection != null) {
              DsSipConnection conn = m_connection.getConnection();
              if (conn != null) {
                if (conn.getTransportType() == Transport.TCP
                    || conn.getTransportType() == Transport.TLS) {
                  if (genCat.isEnabled(Level.DEBUG)) {
                    genCat.debug("Timeout: Closing connection.");
                  }

                  try {
                    conn.closeSocket();
                  } catch (Exception e) {
                    if (genCat.isEnabled(Level.WARN)) {
                      genCat.warn("Exception closing connection: ", e);
                    }
                  }
                }
              }
            }
          } else {
            if (genCat.isEnabled(Level.DEBUG)) {
              genCat.debug(
                  "Timeout: Not Closing Connection, DsSipTransportLayer.getCloseConnectionOnTimeout() is set to false.");
            }
          }

          cb.timeOut(DsSipClientTransactionImpl.this);
          // m_clientInterface = null;
          break;
        case (CB_EXCEPTION):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.debug("CB_EXCEPTION: ");
            cbCat.debug(String.valueOf(m_key));
          }

          cb.icmpError(DsSipClientTransactionImpl.this);
          break;
      }
    }

    public void abort() {}
  }

  // //////////////////////////////////////////////////////////////
  // /////////// end  class ClientTransactionCallback  ////////////
  // //////////////////////////////////////////////////////////////

  /**
   * Checks for the incoming response, if its retransmission or not. It assumes that in a
   * transaction only one response code per response class can be expected.
   *
   * @return <code>true</code> if the incoming response is a retransmission, <code>false</code>
   *     otherwise
   */
  // An optimized version of findAndUpdateRetransmission.  I still think that it could be better,
  // but can't come up with a better answer yet.  This saves the creation of:
  //     The HashSet and HashMap - in favor of the TIntArrayList
  //     The Entry for each entry
  //     The Short for each short
  //     The Object[] is cut from 4 to 3
  // There is a linear seach instead of a hash, which should be faster since the list is so
  // short (usually 1 or 2 elements).  Also, it is just comparing ints, rather than objects.
  // Saves modulus and call to equals(). - jsm
  protected boolean findAndUpdateRetransmission() {
    if (m_sipResponse == null) return false;

    int statusCode = m_sipResponse.getStatusCode();

    if (m_respList.contains(statusCode)) {
      m_sipResponse.setApplicationReason(DsMessageLoggingInterface.REASON_RETRANSMISSION);
      return true;
    } else {
      m_respList.add(statusCode);
      return false;
    }
  }

  private TIntArrayList m_respList = new TIntArrayList(3);

  // //////////////////////////////////////////////////////////////
  // /////////// inner  class ConnectionWrapper ///////////////////
  // //////////////////////////////////////////////////////////////
  /** Manage the underlying DsConnection reference count. */
  // qfang - 10.12.06 - CSCsg10882 need to act on connection closed/error
  // in order to support destination rollover
  protected class ConnectionWrapper
      implements DsSipResolver, DsConnectionEventListener, java.io.Serializable, Cloneable {
    private InetAddress addr;
    private int port;
    private transient DsSipConnection m_connection_ = null;

    private transient DsSipResolver m_resolver = null;

    //  used to implement simple resolver
    private boolean m_sizeExceedsMTU;

    private DsBindingInfo m_resolverInfo = null;

    private boolean m_end; // Set to true when we have
    // used this resolver to obtain a connection.
    // Here we're kind of faking that we have a list event though we just
    // have a single target.

    private boolean m_haveIP = false;
    private byte m_supportedTransports;
    private boolean m_tryingNext;
    private DsByteString m_strConnectionId;

    /**
     * Construct a connection wrapper from an existing wrapper. This is needed when multiple final
     * responses are received and the ACKs are going to be directed to separate endpoints.
     *
     * @param other the connection whose parameters to copy
     */
    ConnectionWrapper(ConnectionWrapper other) {
      genCat.info(
          "Creating Connection Wrapper, oldConnection "
              + m_connection_
              + ", newConnection "
              + other.m_connection_);
      addr = other.addr;
      port = other.port;
      m_connection_ = other.m_connection_;
      if (m_connection_ != null) {
        m_connection_.addReference();
        // qfang - 10.12.06 - CSCsg10882 need to act on connection closed/error
        // in order to support destination rollover
        m_connection_.addDsConnectionEventListener(this);
      }
      m_resolver = other.m_resolver;
      m_sizeExceedsMTU = other.m_sizeExceedsMTU;
      m_resolverInfo = other.m_resolverInfo;
      m_end = other.m_end;
      m_haveIP = other.m_haveIP;
      m_supportedTransports = other.m_supportedTransports;
      m_tryingNext = other.m_tryingNext;
      m_strConnectionId = other.m_strConnectionId;
    }

    ConnectionWrapper(DsSipRequest request, DsSipResolver resolver)
        throws IOException, DsException {
      m_resolver = (resolver == null) ? this : resolver;
      m_resolver.setSizeExceedsMTU(request.sizeExceedsMTU());
      // establish the initial set of endpoints to search
      replace(DsSipTransactionManager.getRequestConnection(request, m_resolver));
    }

    ConnectionWrapper(DsSipRequest request, DsByteString connectionId) throws DsException {
      m_resolver = this;
      m_resolver.setSizeExceedsMTU(request.sizeExceedsMTU());
      m_strConnectionId = connectionId;
      // establish the initial set of endpoints to search
      replace(DsSipConnectionAssociations.getConnection(m_strConnectionId));
      if (m_connection_ != null) {
        m_resolverInfo = m_connection_.getBindingInfo();
        addr = m_resolverInfo.getRemoteAddress();
        port = m_resolverInfo.getRemotePort();
      } else {
        throw new DsException(
            "ConnectionWrapper: No connection could be found for Connection ID ["
                + m_strConnectionId
                + "]");
      }
      m_end = true;
    }

    // these methods are for establishing a connection for the ACK
    void getRequestConnection(DsSipRequest request)
        throws DsException, UnknownHostException, IOException {
      // Use the same connection.
      if (m_strConnectionId != null) return;

      boolean sizeExceedsMTU = request.sizeExceedsMTU();
      boolean use_current_srv =
          request.getURI().isSipURL()
              && m_resolver.queryMatches((DsSipURL) request.getURI(), sizeExceedsMTU);
      if (!use_current_srv) {
        m_resolver.setSizeExceedsMTU(sizeExceedsMTU);
        // the resolver will be reinitialized here
        replace(DsSipTransactionManager.getRequestConnection(request, m_resolver));
      }
    }

    DsBindingInfo getBindingInfo() {
      if (m_connection_ == null) {
        return null;
      }

      return m_connection_.getBindingInfo();
    }

    void getSRVConnection(DsSipRequest request, DsSipURL url)
        throws SocketException, DsException, UnknownHostException, IOException {
      // Use the same connection.
      if (m_strConnectionId != null) return;

      boolean sizeExceedsMTU = request.sizeExceedsMTU();
      if (!m_resolver.queryMatches(url, sizeExceedsMTU)) {
        m_resolver.setSizeExceedsMTU(sizeExceedsMTU);
        // the resolver will be reinitialized here
        int lport = request.getBindingInfo().getLocalPort();
        InetAddress laddr = request.getBindingInfo().getLocalAddress();
        replace(
            DsSipTransactionManager.getSRVConnection(
                request.getNetwork(), laddr, lport, url, m_resolver));
      }
    }

    void check(DsSipRequest request) throws DsException {
      if (m_connection_ == null) {
        if (!tryNextServer(request)) {
          throw new DsException("ConnectionWrapper.check: can't establish connection");
        }
      }
    }

    private boolean tryNextServer(DsSipRequest request) {
      // Use the same connection.
      if (m_strConnectionId != null) return false;

      boolean ret_value = true;
      try {
        replace(m_resolver.tryConnect());
        DsBindingInfo current = m_resolver.getCurrentBindingInfo();
        if (current != null) {
          if (genCat.isEnabled(Level.DEBUG)) {
            genCat.debug("Replacing the Binding Info in the Request with: " + current);
          }

          request.updateBinding(current);
        }

        if (m_connection_ == null) {
          ret_value = false;

          if (genCat.isEnabled(Level.DEBUG)) {
            genCat.debug(
                "tryNextServer(DsSipRequest) returning false because (m_connection_ == null).");
          }
        }
      } catch (DsException e) {
        // e.printStackTrace();
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsException trying next server: ", e);
        }
        ret_value = false;
      } catch (IOException e) {
        // e.printStackTrace();
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("IOException trying next server: ", e);
        }
        ret_value = false;
      }
      // if we are able to get the next server to try, then change the
      // branch ID
      if (ret_value) {
        // We skip the branch ID bumping the first time.
        if (m_tryingNext) {
          // not for the ACK since we don't want to change
          //    the transaction id for the ACK
          if (request.getMethodID() != DsSipConstants.ACK) {
            changeBranchId(request);
          }
        }
        // sets the flag so that next time we know that we have to change
        // the branch ID.
        m_tryingNext = true;
      }
      return ret_value;
    }

    /**
     * Return the underlying SIP connection. This method should not be used unless we're creating a
     * service unavailable handler client transaction based on this one (i.e. it should never be
     * used to break the encapsulation of this wrapper).
     *
     * @return the underlying SIP connection.
     */
    DsSipConnection getConnection() {
      return m_connection_;
    }

    boolean isSet() {
      return (m_connection_ != null);
    }

    Transport getTransport() {
      return m_connection_.getTransportType();
    }

    int getPortNo() {
      return m_connection_.getPortNo();
    }

    InetAddress getInetAddress() {
      return m_connection_.getInetAddress();
    }

    /*
     * This method is used by the createCopy method of
     * DsSipClientTransactionIImpl (which is used for multiple final
     * response support.
     */
    void incrementReferenceCount() {
      m_connection_.addReference();
    }

    void replace(DsSipConnection new_connection) {
      genCat.info(
          "Inside replace connection , oldConnection "
              + m_connection_
              + " new Connection "
              + new_connection);
      if (m_connection_ == new_connection) return;
      if (m_connection_ != null) {
        m_connection_.removeReference();
        // qfang - 10.12.06 - CSCsg10882 need to act on connection closed/error
        // in order to support destination rollover
        m_connection_.removeDsConnectionEventListener(this);
      }
      m_connection_ = new_connection;
      if (m_connection_ != null) {
        addr = m_connection_.getBindingInfo().getRemoteAddress();
        port = m_connection_.getBindingInfo().getRemotePort();
        m_connection_.addReference();
        // qfang - 10.12.06 - CSCsg10882 need to act on connection closed/error
        // in order to support destination rollover
        m_connection_.addDsConnectionEventListener(this);
        if (genCat.isEnabled(Level.INFO)) {
          genCat.info("connection: " + m_connection_);
          genCat.info("connection addr: " + addr);
          genCat.info("connection port: " + port);
        }
      }
    }

    void release() {
      synchronized (m_connection) {
        if (m_connection_ != null) {
          m_connection_.removeReference();
          // qfang - 10.12.06 - CSCsg10882 need to act on connection closed/error
          // in order to support destination rollover
          m_connection_.removeDsConnectionEventListener(this);
          m_connection_ = null;
        }
      }
    }

    /**
     * Send a message via the current underlying connection.
     *
     * @param request the message to send
     * @return the serialized bytes of the message
     * @throws DsException If no transport could be found to send the data
     * @throws IOException if error occurs while sending data through the network
     */
    protected byte[] send(DsSipRequest request) throws IOException, DsException {
      if (checkVia(request)) replaceKey(request);

      request.setFinalised(true);
      byte[] bytes = m_connection_.sendTo(request, addr, port, DsSipClientTransactionImpl.this);
      // Update the binding info
      request.updateBinding(m_connection_.getBindingInfo());
      return bytes;
    }

    /**
     * Send a message as bytes via the current underlying connection.
     *
     * @param buffer the message bytes
     * @throws IOException if error occurs while sending data through the network
     */
    protected void send(byte buffer[]) throws IOException {
      m_connection_.sendTo(buffer, addr, port, DsSipClientTransactionImpl.this);
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

      if ((m_resolverInfo != null) && (m_resolverInfo.getRemoteAddress() == null)) {
        throw new IOException("can't resolve address: " + m_resolverInfo.getRemoteAddressStr());
      }

      DsSipConnection con = null;
      try {
        /**
         * edited by radmohan
         *
         * <p>CSCtz70393 Thread pool exhausted when all elements in srv group is down
         */
        if (!DsSipClientTransactionImpl.m_useDsUnreachableTable) {

          genCat.info(
              "DsSipClientTransactionImpl.m_useDsUnreachableTable: is set to :"
                  + DsSipClientTransactionImpl.m_useDsUnreachableTable);

          con =
              (DsSipConnection)
                  tl.getConnection(
                      m_resolverInfo.getNetwork(),
                      m_resolverInfo.getLocalAddress(),
                      m_resolverInfo.getLocalPort(),
                      m_resolverInfo.getRemoteAddress(),
                      m_resolverInfo.getRemotePort(),
                      m_resolverInfo.getTransport());
        } else {
          genCat.info(
              "DsSipClientTransactionImpl.m_useDsUnreachableTable: is set to :"
                  + DsSipClientTransactionImpl.m_useDsUnreachableTable);

          if (!DsUnreachableDestinationTable.getInstance()
              .contains(
                  m_resolverInfo.getRemoteAddress(),
                  m_resolverInfo.getRemotePort(),
                  m_resolverInfo.getTransport())) {
            genCat.info(
                "DsSipClientTransactionImpl.tryConnect: not present DsUnreachableDestinationTable:"
                    + m_resolverInfo.getRemoteAddress());
            con =
                (DsSipConnection)
                    tl.getConnection(
                        m_resolverInfo.getNetwork(),
                        m_resolverInfo.getLocalAddress(),
                        m_resolverInfo.getLocalPort(),
                        m_resolverInfo.getRemoteAddress(),
                        m_resolverInfo.getRemotePort(),
                        m_resolverInfo.getTransport());
          } else {

            genCat.info(
                "DsSipClientTransactionImpl.tryConnect:  present DsUnreachableDestinationTable:"
                    + m_resolverInfo.getRemoteAddress());
          }
        }
      } catch (Exception e) {
        if (genCat.isEnabled(Level.INFO)) {

          // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
          DsUnreachableDestinationTable.getInstance()
              .add(
                  m_resolverInfo.getRemoteAddress(),
                  m_resolverInfo.getRemotePort(),
                  m_resolverInfo.getTransport());

          genCat.info("Getting Connection from transport layer failed ", e);
        }

        // Try again with the UDP itself.
        if (m_sizeExceedsMTU) {
          DsNetwork network = m_resolverInfo.getNetwork();
          /* TODO
          if (network != null && network.isBehindNAT()) {
            // we un-set the local binding info earlier, now we need to put it back
            // since the TCP connection failed for some reason.
            DsUdpListener listener = network.getUdpListener();
            if (listener != null) {
              m_resolverInfo.setLocalAddress(listener.m_address);
              m_resolverInfo.setLocalPort(listener.m_port);
              if (genCat.isEnabled(Level.INFO)) {
                genCat.info(
                    "tryConnect() - resetting local binding info - host = "
                        + ((DsPacketListener) listener).m_address
                        + " / port = "
                        + ((DsPacketListener) listener).m_port
                        + " and trying UDP");
              }
            }
          }*/

          genCat.warn("tryConnect() m_sizeExceedsMTU trying UDP " + m_resolverInfo);
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
        }
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
     * @param sizeExceedsMTU <code>true</code> if this message was too large for UDP so it failed
     *     over to TCP
     * @return true if the resolver would initialize to it's current set of endpoints given this
     *     URL.
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
        if (genCat.isEnabled(Level.INFO)) {
          genCat.info(
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
        /*
        TODO
                try {

                  DsStreamListener listener =
                      (DsStreamListener) network.getListener(DsSipTransportType.TCP);
                  if (listener != null) {
                    localAddr = listener.localAddress;
                  }
                } catch (NullPointerException e) {
                  if (genCat.isEnabled(Level.WARN)) {
                    genCat.warn("Null pointer during access to network object for listener address.");
                  }
                }*/
      }

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

      if (genCat.isEnabled(Level.DEBUG)) {
        genCat.debug("Resolver Binding Info = " + m_resolverInfo);
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
     * Return <code>true</code> if this resolver has been configured to support a particular
     * transport as defined in DsSipObject.DsSipTransportType.
     *
     * @param transport the transport to see if it is supported
     * @return <code>true</code> if this resolver has been configured to support a particular
     *     transport as defined in DsSipObject.DsSipTransportType.
     */
    public boolean isSupported(Transport transport) {
      return DsSipResolverUtils.isSupported(transport, m_supportedTransports, m_sizeExceedsMTU);
    }

    // qfang - 10.12.06 - CSCsg10882 need to act on connection closed/error
    // in order to support destination rollover
    //////////////////////////////////////////////////
    /// Implement DsConnectionEventListener interface
    /**
     * Callback for connection closed event.
     *
     * @param evt DsConnectionClosedEvent
     */
    public void onDsConnectionClosedEvent(DsConnectionClosedEvent evt) {
      if (genCat.isEnabled(Level.INFO)) {
        genCat.info("ClientTransaction receives connection closed event");
      }
      // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
      if (!m_simpleResolver) {
        if (m_connection.getBindingInfo() != null) {
          DsUnreachableDestinationTable.getInstance()
              .add(
                  m_connection.getBindingInfo().getRemoteAddress(),
                  m_connection.getBindingInfo().getRemotePort(),
                  m_connection.getBindingInfo().getTransport());
        }
      }

      try {
        execute(DS_CT_IN_IO_EXCEPTION);
      } catch (DsStateMachineException ex) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("state machine exception processing connection closed event", ex);
        }
      }
    }

    /**
     * Callback for connection error event.
     *
     * @param evt DsConnectionErrorEvent
     */
    public void onDsConnectionErrorEvent(DsConnectionErrorEvent evt) {
      if (genCat.isEnabled(Level.INFO)) {
        genCat.info("ClientTransaction receives connection error event");
      }
      // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
      if (!m_simpleResolver) {
        if (m_connection.getBindingInfo() != null) {
          DsUnreachableDestinationTable.getInstance()
              .add(
                  m_connection.getBindingInfo().getRemoteAddress(),
                  m_connection.getBindingInfo().getRemotePort(),
                  m_connection.getBindingInfo().getTransport());
        }
      }

      try {
        execute(DS_CT_IN_IO_EXCEPTION);
      } catch (DsStateMachineException ex) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("state machine exception processing connection error event", ex);
        }
      }
    }

    /**
     * Callback for ICMP error event.
     *
     * @param evt DsConnectionIcmpErrorEvent
     */
    public void onDsConnectionIcmpErrorEvent(DsConnectionIcmpErrorEvent evt) {
      if (genCat.isEnabled(Level.INFO)) {
        genCat.info("ClientTransaction receives connection ICMP error event ");
      }
      // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
      if (!m_simpleResolver) {
        if (m_connection.getBindingInfo() != null) {
          DsUnreachableDestinationTable.getInstance()
              .add(
                  m_connection.getBindingInfo().getRemoteAddress(),
                  m_connection.getBindingInfo().getRemotePort(),
                  m_connection.getBindingInfo().getTransport());
        }
      }
      try {
        execute(DS_CT_IN_IO_EXCEPTION);
      } catch (DsStateMachineException ex) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("state machine exception processing connection ICMP error event", ex);
        }
      }
    }
    /// end implement DsConnectionEventListener interface

    // ////// end DsSipResolver methods ////////////////////////
  }

  // ///////////  end class ConnectionWrapper /////////////////////

  /** A class to absorb 503 retransmissions. */
  private static class ServiceUnavailableHandler extends DsSipClientTransaction implements DsEvent {
    private DsSipTransactionKey m_key;
    private int m_timeout;

    private static Logger m_cat = DsLog4j.LlSMClientCat;

    /**
     * Constructor.
     *
     * @param timeout the amount of time before this transaction should be terminated
     * @param key the key (used to remove the client transaction)
     */
    ServiceUnavailableHandler(int timeout, DsSipTransactionKey key) {
      m_key = key;
      m_timeout = timeout;
    }

    /*
     *  javadoc inherited
     */
    public synchronized void run(Object arg) {
      try {
        DsSipTransactionManager.removeTransaction(this);
      } catch (DsException dse) {
        if (m_cat.isEnabled(Level.WARN))
          m_cat.warn("terminated(): Failed to remove transaction ", dse);
      }
    }

    // //////////////////////////////////////////////////////////////////
    // //////////       DsSipClientTransaction  methods        //////////
    // //////////////////////////////////////////////////////////////////

    // stubs
    protected void onResponse(DsSipResponse response) {}

    public synchronized void onIOException(IOException ioe) {}

    public void ack(DsSipAckMessage request)
        throws DsException, IOException, UnknownHostException {}

    public void cancel(DsSipCancelMessage request) throws DsException, IOException {}

    public void cancel(DsSipClientTransactionInterface cancelInterface, DsSipCancelMessage request)
        throws DsException, IOException {}
    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK
    public DsSipClientTransaction prack(
        DsSipClientTransactionInterface clientInterface, DsSipPRACKMessage request)
        throws DsException, IOException {
      return null;
    }

    public DsSipClientTransaction prack(DsSipPRACKMessage request) throws DsException, IOException {
      return null;
    }

    protected DsByteString getToTag() {
      return null;
    }

    protected void setToTag(DsByteString toTag) {}

    protected DsSipClientTransaction createCopy(DsSipResponse response) throws DsException {
      return null;
    }

    protected void onMultipleFinalResponse(
        DsSipClientTransaction originalTransaction, DsSipResponse response) {}

    protected boolean multipleFinalResponsesEnabled() {
      return false;
    }

    public void cancelTn() {}

    public void terminate() {}

    // //////////////////////////////////////////////////////////////////
    // //////////       DsSipClientTransaction  methods        //////////
    // //////////////////////////////////////////////////////////////////

    public synchronized void start() throws IOException, DsException {
      DsDiscreteTimerMgr.scheduleNoQ(m_timeout, this, null);
    }

    public boolean isStarted() {
      return true;
    }

    public DsSipRequest getRequest() {
      return null;
    }

    public boolean isServerTransaction() {
      return false;
    }

    public boolean isProxyServerMode() {
      return false;
    }

    public void setProxyServerMode(boolean mode) {}

    public boolean isInvite() {
      return true;
    }

    public int getMethodID() {
      return DsSipConstants.INVITE;
    }

    public DsSipTransactionKey getKey() {
      return m_key;
    }

    public int getState() {
      return 0;
    }

    public String getAsString() {
      return "CLIENT_TRANS (503 handler) METHOD: ?? STATE: N/A  KEY: " + m_key + "\nCALLID:: null";
    }
  }

  // maivu - 11.01.06 - CSCsg22401
  /**
   * A Timer class for the request expiration. This timer is set in
   * DsSipClientTransactionImpl.start()
   */
  private class ExpirationTimer implements DsEvent {
    public void run(Object argument) {
      if (genCat.isEnabled(Level.DEBUG)) {
        genCat.debug("Client Transaction Expiration Timer FIRED");
      }
      try {
        cancel(null);
      } catch (Exception ex) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("Exception encountered when cancelling request on expires", ex);
        }
      }
    }
  }
}
