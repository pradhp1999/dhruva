// Copyright (c) 2005-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface.SipMsgNormalizationState;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.cac.SIPSessions;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TimerTask;
import org.slf4j.event.Level;

/**
 * Implements the sever side of the low level state machine.
 *
 * <p>This class implements DsSipServerTransaction using a state machine. The states and inputs of
 * this state machine are defined in DsSipStateMachineDefinitions. Input is provided to the state
 * machine via the execute method of this class. For each state of the state machine, there is a
 * method defined in this class which is called when the state machine enters (or re-enters) that
 * state. The single parameter of these state machine methods is an integer (int) which is an OR of
 * the state machine's current input and the previous state.
 */
public class DsSipServerTransactionImpl extends DsSipServerTransaction
    implements DsSipStateMachineDefinitions, DsSipConstants, DsEvent {
  ///////////////////////////////////////////////////////////////////////////
  //////////// statics  /////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  // put this here for now
  /** The queue through which to call the application code back. */
  private static DsWorkQueue m_callbackQueue;
  /** Size of the callback queue. */
  private static final int MAX_CALLBACK_EVENTS = 400;
  /** Number of threads in the callback queue. */
  private static final int MAX_CALLBACK_THREADS =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SERVER_CB_WORKERS, DsConfigManager.PROP_SERVER_CB_WORKERS_DEFAULT);
  /** Static variable for auto response flag */
  private static boolean m_autoResponseStatic = true;

  /** Constant for Resolver Stage. */
  protected static final int USING_MADDR = 0;
  /** Constant for Resolver Stage. */
  protected static final int USING_RECEIVED = 1;
  /** Constant for Resolver Stage. */
  protected static final int USING_SIMPLE_SENT_BY = 2;
  /** Constant for Resolver Stage. */
  protected static final int USING_SRV_SENT_BY = 3;

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

  /** Integer that represent the T1 state. */
  public static final Integer IN_T1 = new Integer(DS_ST_IN_T1);
  /** Integer that represent the Tn state. */
  public static final Integer IN_Tn = new Integer(DS_ST_IN_Tn);
  /** Integer that represent the Timeout state. */
  public static final Integer IN_TIMEOUT = new Integer(DS_ST_IN_TIMEOUT);
  /** Integer that represent the T Provisional state. */
  public static final Integer IN_TPROVISIONAL = new Integer(DS_ST_IN_TPROVISIONAL);

  /** General logging Category. */
  protected static Logger genCat = null;
  /** Callback logging Category. */
  protected static Logger cbCat = null;
  /** Resolver logging Category. */
  protected static Logger cat = null;

  /** The remote port network. */
  protected static DsNetwork m_RPORTNetwork = null;

  /** To use the Deterministic SRV resolver or not. The default is <code>true</code>. */
  protected static boolean m_useDeterministicLocator =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_DETERMINISTIC_RESOLVER,
          DsConfigManager.PROP_DETERMINISTIC_RESOLVER_DEFAULT);

  static {
    genCat = DsLog4j.LlSMServerCat;
    cbCat = DsLog4j.LlSMServerUserCBCat;
    cat = DsLog4j.LlSMServerTimersCat;

    m_cleanup =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_CLEANUP, DsConfigManager.PROP_CLEANUP_DEFAULT);

    m_callbackQueue =
        new DsWorkQueue(
            DsWorkQueue.SERVER_CALLBACK_QNAME, MAX_CALLBACK_EVENTS, MAX_CALLBACK_THREADS);
    m_callbackQueue.setDiscardPolicy(DsWorkQueue.GROW_WITHOUT_BOUND);
    DsConfigManager.registerQueue(m_callbackQueue);

    m_autoResponseStatic =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_AUTO_RESPONSE_CANCEL,
            DsConfigManager.PROP_AUTO_RESPONSE_CANCEL_DEFAULT);
  }

  ///////////////////////////////////////////////////////////////////////////
  //////////// instance /////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  /** The default server interface. */
  protected transient DefaultServerInterface m_defaultServerInterface; // = null;
  /** The CANCEL transaction, if this transaction was cancelled. */
  protected DsSipServerTransaction m_cancelTransaction; // = null;

  /** The timers, obtained from the network object. */
  protected DsSipTimers m_sipTimers;
  /** To. */
  protected int m_To;
  /** Reference to the timer task for Tn so that it can be cancelled. */
  DsDiscreteTimerTask m_TimerTaskTn; // = null;

  /** The request that started this transaction. */
  protected DsSipRequest m_sipRequest;
  /** The CANCEL message, if this transaction was cancelled. */
  protected DsSipCancelMessage m_cancelMessage;

  /** The best response sent for this transaction. */
  protected DsSipResponse m_sipResponse;
  /** The best response sent for this transaction, stored as bytes. */
  protected byte[] m_sipResponseBytes;

  // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
  /** The PRACK transaction. */
  protected DsSipServerTransaction m_prackTransaction; // = null;
  /** The PRACK message, if this transaction was pracked. */
  protected DsSipPRACKMessage m_prackMessage;

  protected DsSipDialogID m_dialogID;

  /** The status code of response sent. */
  protected short m_statusCode;
  /** The ID of the method that started this transaction. */
  protected byte m_method;

  /** <code>true</code> if the user register a callback interface. */
  private boolean m_userRegistered; // = false;
  /** <code>true</code> if this transaction does not contain a merged request. */
  protected boolean m_isOriginal; // = false;
  /** <code>true</code> the first time isNew() is called, then <code>false</code> after that. */
  protected boolean m_isNew = true;

  /** <code>true</code> if proxy server mode is enabled. */
  protected boolean m_isProxyServerMode = DsSipTransactionManager.isProxyServerMode();

  /** The state table for this transaction. */
  protected DsSipStateTable m_stateTable;
  /** The connection for this transaction. */
  protected ConnectionWrapper m_connection;

  /** For server transaction callbacks. */
  protected transient DsSipServerTransactionInterface m_serverInterface; // = null;
  /** The key for this transaction. */
  protected DsSipTransactionKey m_key;
  /** Current stage of the resolver, see Resolver Constants. */
  protected int m_resolverStage;
  /** Flag that let's us know if we should retry on an IO Exception or if we have already. */
  protected boolean m_retryOnIoExc = true;
  /** The Binding Information for this transaction. */
  DsBindingInfo m_bindingInfo;

  /** The Via header from the original request. */
  DsSipViaHeader m_via;
  /** The server locator. */
  DsSipServerLocator client_locator;

  /** The address that this transaction is using. */
  String m_addr_str;
  /** The port that this transaction is using. */
  int m_port;
  /** The transport protocol that this transaction is using. */
  Transport m_transport;

  /** Reference to the timer task for TPROVISIONAL so that it can be cancelled. */
  DsDiscreteTimerTask m_TimerTaskTProvisional; // = null;

  // maivu - 11.01.06 - CSCsg22401
  /** Reference to the timer task for Expiration so that it can be cancelled. */
  private TimerTask m_expirationTimerTask;

  /**
   * The auto response flag, that tells whether 487 response will be generated and sent on receiving
   * CANCEL for this transaction, while still in CALLING or PROCEEDING state.
   */
  protected boolean m_autoResponse = m_autoResponseStatic;

  /**
   * Reason for message generation defined in DsMessageLoggingInterface messageReason is a hack to
   * know the message generation reason when response object is null(in cases where byte array is
   * used or in cases where only response code is sent
   */
  private int messageReason;

  /** store normalization state for current response */
  protected SipMsgNormalizationState sipMsgNormalizationState = SipMsgNormalizationState.UNMODIFIED;

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

  /**
   * Removes the Session Object if the the message is a Bye transaction Should only be called when
   * the server transaction gets teared down. We are checking the whether the transaction for which
   * this called is corresponds to a BYE transaction
   */
  public void removeSession() {

    if (m_sipRequest != null) {
      if (m_sipRequest.getMethodID() == DsSipRequest.BYE
          || m_sipRequest.getMethodID() == DsSipRequest.OPTIONS) {
        SIPSessions.removeSession(m_sipRequest.getCallId().toString());
      }

    } else if (m_sipResponse != null) {

      if (m_sipResponse.getCSeqType() == DsSipResponse.BYE
          || m_sipResponse.getCSeqType() == DsSipResponse.OPTIONS) {
        SIPSessions.removeSession(m_sipResponse.getCallId().toString());
      }
    }
  }

  /** Cleanup references when transaction goes to the completed state. */
  protected final void nullRefs() {
    removeSession();
    if (m_sipResponse != null) {
      // do not null the reference to a compressed response
      if (!m_sipResponse.shouldCompress()) {
        m_sipResponse = null;
      }
    }
    // m_cancelMessage = null;
    m_defaultServerInterface = null;
    m_cancelTransaction = null;
    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
    m_prackTransaction = null;
    m_non100ResponseSent = false;

    if (m_cleanup) {
      m_sipRequest = null;
      if (isInvite()) {
        if (!m_x200Terminated) {
          m_serverInterface = null;
        }
      } else {
        m_serverInterface = null;
      }
    }
  }

  /**
   * Send the current response or response bytes.
   *
   * @throws DsException if there is an exception in the User Agent
   * @throws IOException if the underlying socket throws this exception
   */
  protected final void sendCurrentResponse() throws DsException, IOException {
    if (m_sipResponse != null) {
      m_sipResponseBytes = m_connection.send(m_sipResponse);
      // Setting the message generation reason in the messageReason as if
      // the compression is enabled
      // then the m_sipResponse object is made null and wont be available
      // when logResponse is called and hence there is no way to know the
      // message generation reason
      messageReason = m_sipResponse.getApplicationReason();
      // make sure we use the cached bytes obly if the message is NOT
      // compressed.  This ensures that we recompress the bytes --
      // required by sigcomp stateful compression
      if (!m_sipResponse.shouldCompress()) {
        m_sipResponse = null;
      } else {
        m_sipResponseBytes = null;
      }
    } else if (m_sipResponseBytes != null) {
      m_connection.send(m_sipResponseBytes);
    } else {
      throw new DsException("Tried to send a null response?");
    }
  }

  /**
   * This constructor calculates the transaction's keys and sets the server transaction interface.
   * It is designed to do minimal work.
   *
   * @param request the incoming request
   * @param serverInterface optional callback interface to user-level callbacks
   * @param transactionParams optional reserved for future use.
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipServerTransactionImpl(
      DsSipRequest request,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    this(request, null, null, serverInterface, transactionParams);
  }

  /**
   * This constructor calculates the transaction's keys and sets the server transaction interface.
   * It is designed to do minimal work.
   *
   * @param request the incoming request
   * @param serverInterface optional callback interface to user-level callbacks
   * @param transactionParams optional reserved for future use.
   * @param isOriginal a boolean indicate whether this transaction is the the one created for the
   *     original request or whether it was created for a merged request
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipServerTransactionImpl(
      DsSipRequest request,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams,
      boolean isOriginal)
      throws DsException {
    this(request, null, null, serverInterface, transactionParams, isOriginal);
  }

  /**
   * This constructor calculates the transaction's keys and sets the server transaction interface.
   * It is designed to do minimal work.
   *
   * @param request the incoming request
   * @param keyWithVia the transaction key constructed with the Via or null if this key should be
   *     calculated here
   * @param keyNoVia the transaction key constructed without the Via or null if this key should be
   *     calculated here
   * @param serverInterface optional callback interface to user-level callbacks
   * @param transactionParams optional reserved for future use.
   * @param isOriginal a boolean indicate whether this transaction is the the one created for the
   *     original request or whether it was created for a merged request
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipServerTransactionImpl(
      DsSipRequest request,
      DsSipTransactionKey keyWithVia,
      DsSipTransactionKey keyNoVia,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams,
      boolean isOriginal)
      throws DsException {
    this(request, keyWithVia, keyNoVia, serverInterface, transactionParams);
    m_isOriginal = isOriginal;
  }

  /**
   * This constructor calculates the transaction's keys and sets the server transaction interface.
   * It is designed to do minimal work.
   *
   * @param keyWithVia No longer used - pass null.
   * @param keyNoVia No longer used - pass null.
   * @param request the incoming request
   * @param serverInterface optional callback interface to user-level callbacks
   * @param transactionParams optional reserved for future use.
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipServerTransactionImpl(
      DsSipRequest request,
      DsSipTransactionKey keyWithVia,
      DsSipTransactionKey keyNoVia,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    if (genCat.isEnabled(Level.DEBUG)) {
      genCat.debug("DsSipServerTransactionImpl.DsSipServerTransactionImpl");
    }

    if (serverInterface == null) {
      m_defaultServerInterface = new DefaultServerInterface();
      m_serverInterface = m_defaultServerInterface;
      m_userRegistered = false;
    } else {
      m_serverInterface = serverInterface;
      m_userRegistered = true;
    }
    // keys passed in constructor are just for backward compatability with the old API
    m_key = request.getKey();

    m_sipRequest = request;

    // We have to clone the binding info since request could change.
    m_bindingInfo = (DsBindingInfo) m_sipRequest.getBindingInfo().clone();
    if (cat.isEnabled(Level.INFO)) {
      cat.log(Level.INFO, "The Binding Info for the incoming request:\n" + m_bindingInfo);
    }

    m_via = m_sipRequest.getViaHeaderValidate();
    m_method = (byte) request.getMethodID();
    m_connection = new ConnectionWrapper(null);
    createStateTable();

    DsNetwork network = request.getBindingInfo().getNetworkReliably();

    // fix for issue CSCsr11815 - Move 100rel support to DsNetwork from DsSipTransactionManager
    m_100relSupport = network.get100relSupport();

    m_simpleResolver = network.getSimpleResolver();

    m_sipTimers = network.getSipTimers();
    if (cat.isEnabled(Level.DEBUG)) {
      cat.log(Level.DEBUG, "going to use the following SIP timers:\n" + m_sipTimers);
    }

    // use m_sipTimers.serverTnValue directly
    // m_Tn = m_sipTimers.serverTnValue;
    if (m_sipTimers.serverTnValue < Integer.MAX_VALUE) // max transaction duration
    {
      if (cat.isEnabled(Level.DEBUG)) {
        debugTraceTimer(true, "m_sipTimers.serverTnValue", "IN_Tn", m_sipTimers.serverTnValue);
      }
      m_TimerTaskTn = DsDiscreteTimerMgr.scheduleNoQ(m_sipTimers.serverTnValue, this, IN_Tn);
    }

    // This is a timer to send a delayed provisional response for a
    // non-INVITE.  This behavior is outside of the scope of the SIP spec,
    // but useful in some networks.
    if (!isInvite() && (m_sipTimers.TU3Value < Integer.MAX_VALUE) && (m_sipTimers.TU3Value > 0)) {
      if (cat.isEnabled(Level.DEBUG)) {
        debugTraceTimer(
            true, "m_sipTimers.TU3Value", "DS_ST_IN_TPROVISIONAL", m_sipTimers.TU3Value);
      }
      m_TimerTaskTProvisional =
          DsDiscreteTimerMgr.scheduleNoQ(m_sipTimers.TU3Value, this, IN_TPROVISIONAL);
    }
    // maivu - 11.01.06 - CSCsg22401
    // Start the timer to track the request expiration.  The timer does not apply to Proxy mode
    // The expiration is based on the Expires header of the request.
    // If there is no Expires header, the default will be used which are
    // DsConfigManager.getDefaultInviteExpiration
    // or DsConfigManager.getDefaultNonInviteExpiration
    if (!m_isProxyServerMode) {
      // expires headers for REGISTER and SUBSCRIBE have different meaning
      // and are handled by upper layer. Just handle Invite case here for
      // now. In the future, may handle other method
      if (isInvite()) {
        int timeout = 0;
        DsSipExpiresHeader exp =
            (DsSipExpiresHeader) request.getHeaderValidate(DsSipConstants.EXPIRES);
        if (exp != null) {
          timeout = exp.getValue().parseInt() * 1000;
        } else {
          timeout = network.getDefaultInviteExpiration();
        }
        // create and start the timer
        if (timeout > 0) {
          m_expirationTimerTask = DsTimer.schedule(timeout, new ExpirationTimer(), null);
        }
      }
    }
  }

  /** Creates state table for this transaction. */
  protected void createStateTable() {
    // CAFFEINE 2.0 DEVELOPMENT - ST_TRANSITIONS is maintained in new class
    // DsSipStateMachineTransitions.
    m_stateTable = new DsSipStateTable(DsSipStateMachineTransitions.ST_TRANSITIONS);
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////// Transaction /////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited
   */
  public synchronized void start() throws IOException, DsException {
    if (isStarted()) return;

    if (m_cancelTransaction == null) {
      execute(DS_ST_IN_START);
    } else {
      execute(DS_ST_IN_CANCEL);
    }
  }

  /** Initialize the SIP timers. */
  protected void initializeTimers() {
    if (m_connection.getTransport().isReliable()) {
      // reliable transport
      m_To = 0;
    } else {
      // unreliable transport
      m_To = m_sipTimers.TU2Value; // equals to 64 * DsSipTimers.T1Value;
    }
  }

  /*
   * javadoc inherited
   */
  public final boolean isStarted() {
    return m_stateTable.isStarted();
  }

  /*
   * javadoc inherited
   */
  public final DsSipRequest getRequest() {
    return m_sipRequest;
  }

  /*
   * javadoc inherited
   */
  public final boolean isServerTransaction() {
    return true;
  }

  /*
   * javadoc inherited
   *
   * proxy server gets callback for ACK messages for 200 final responses
   * in the COMPLETED state
   *
   * there are no automatic 487 responses sent for CANCELs
   *
   * there are no timed retransmissions for 200 final responses
   *
   * proxy server is allowed to forward 200 responses in COMPLETED and
   * FINAL_STATUS states
   *
   */
  public final boolean isProxyServerMode() {
    return m_isProxyServerMode;
  }

  /*
   * javadoc inherited
   *
   * proxy server gets callback for ACK messages for 200 final responses
   * in the COMPLETED state
   *
   * there are no automatic 487 responses sent for CANCELs
   *
   * there are no timed retransmissions for 200 final responses
   *
   * proxy server is allowed to forward 200 responses in COMPLETED and
   * FINAL_STATUS states
   *
   */
  public final void setProxyServerMode(boolean mode) {
    m_isProxyServerMode = mode;
  }

  /*
   * javadoc inherited
   */
  public boolean isInvite() {
    return false;
  }

  /*
   * javadoc inherited
   */
  public final int getMethodID() {
    return m_method;
  }

  /*
   * javadoc inherited
   */
  public final DsSipTransactionKey getKey() {
    return m_key;
  }

  /*
   * javadoc inherited
   */
  public final synchronized int getState() {
    return m_stateTable.getState();
  }

  /** @deprecated - use toString() */
  public final String getAsString() {
    return toString();
  }

  public final String toString() {
    String state = DsSipStateTable.printState(m_stateTable.getState());

    DsByteString seq =
        (m_sipRequest == null) ? new DsByteString("??") : m_sipRequest.getCSeqMethod();
    DsByteString cid = (m_sipRequest == null) ? new DsByteString("??") : m_sipRequest.getCallId();
    long sNo = (m_sipRequest == null) ? 0 : m_sipRequest.getCSeqNumber();
    return "SERVER_TRANS METHOD:"
        + sNo
        + " "
        + seq
        + " STATE:"
        + state
        + " KEY: "
        + m_key
        + "CALLID:: "
        + (cid != null ? DsByteString.toString(cid) : "null");
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////// Server Transaction //////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited
   */
  public final void sendNotAcceptable() throws IOException, DsException {
    sendResponse(DsSipResponseCode.DS_RESPONSE_NOT_ACCEPTABLE);
  }

  /*
   * javadoc inherited
   */
  public synchronized void sendResponse(int responseCode) throws IOException, DsException {
    // only do this check here (response code version), since we
    // are being asked to create the correct response
    if (m_sipRequest != null) {
      if (m_sipRequest.getViaHeader() != m_via) {
        throw new DsException(
            "Can't form response connection: topmost via header has changed! pre:->"
                + m_via
                + "<- post: ->"
                + m_sipRequest.getViaHeader()
                + "<-");
      }
    }
    DsSipResponse resp = new DsSipResponse(responseCode, m_sipRequest, null, null);
    resp.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    sendResponse(resp);
  }

  /*
   * javadoc inherited
   */
  public synchronized void sendResponse(byte[] response, int statusCode)
      throws IOException, DsException {
    // Don't let the user send a provisional response after a
    // final.  The state machine would ignore it, but the response
    // gets overwritten at this point.
    if (m_stateTable.getState() > DS_PROCEEDING) {
      if (statusCode < DsSipResponseCode.DS_RESPONSE_OK) {
        return;
      } else if (statusCode != m_statusCode) // not a retransmission
      {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn(
              "Attempted to send more than one final response.  Second response ignored:\n"
                  + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(new String(response)));
        }

        throw new DsException(
            "Attempted to send more than one final response.  Second response ignored.");
      }
    }

    m_sipResponseBytes = response;
    m_statusCode = (short) statusCode;
    m_sipResponse = null;

    if (m_statusCode < 200) {
      // CAFFEINE 2.0 DEVELOPMENT - required by handling 1XX reliable response.
      // Make sure the state transition properly with 1xx reliable responses
      if (m_statusCode != 100) {
        // This is for easier manipulation of the response
        m_sipResponse = (DsSipResponse) DsSipMessage.createMessage(m_sipResponseBytes);
        sendResponse(m_sipResponse);
      } else {
        if (m_stateTable.getState() == DS_WAIT_PRACK
            || m_stateTable.getState() == DS_STI_RELPROCEEDING) {
          // We are in reliable state, no 100 should be sent.
          throw new DsException("No 100 Trying after reliable 1xx is sent");
        } else {
          execute(DS_ST_IN_PROVISIONAL);
        }
      }
    } else if (m_statusCode < 300) {
      execute(DS_ST_IN_2XX);
    } else {
      execute(DS_ST_IN_3TO6XX);
    }
  }

  /*
   * javadoc inherited
   */
  public synchronized void sendResponse(DsSipResponse response) throws IOException, DsException {
    short statusCode;
    if (response == null) {
      // Don't let the user send a provisional response after a
      // final.  The state machine would ignore it, but the response
      // gets overwritten at this point.
      if (m_stateTable.getState() > DS_PROCEEDING) {
        return;
      }

      statusCode = m_statusCode = (short) DsSipResponseCode.DS_RESPONSE_TRYING;
      generate100Response();

    } else {
      statusCode = (short) response.getStatusCode();

      messageReason = response.getApplicationReason();

      // Don't let the user send a provisional response after a
      // final.  The state machine would ignore it, but the response
      // gets overwritten at this point.
      if (m_stateTable.getState() > DS_PROCEEDING) {
        if (statusCode < DsSipResponseCode.DS_RESPONSE_OK) {
          return;
        } else if (statusCode != m_statusCode) // not a retransmission
        {
          if (genCat.isEnabled(Level.WARN)) {
            genCat.warn(
                "Attempted to send more than one final response.  Second response ignored:\n"
                    + response.maskAndWrapSIPMessageToSingleLineOutput());
          }

          throw new DsException(
              "Attempted to send more than one final response.  Second response ignored.");
        }
      }

      DsSipViaHeader header = response.getViaHeaderValidate();
      if (header != null) {
        if (!DsSipResponse.receiveAlways) header.removeReceived();
      } else {
        throw new DsException("No Via header in reponse.");
      }

      m_statusCode = statusCode;
      m_sipResponse = response;
    }

    // normalized state stored and used at logging time
    if (response != null) {
      sipMsgNormalizationState = response.getNormalizationState();
    }

    if (statusCode < 200) {
      // CAFFEINE 2.0 DEVELOPMENT - required by handling 1XX reliable response.
      if (statusCode != 100) {
        // "The provisional response MUST establish a dialog,
        // if one is not yet created".
        // This is also necessary for retrieving the INVITE/PRACK txn later!
        // Either the 1xx has toTag, or it supports 100rel (reliable 1xx)
        if (m_dialogID == null && !m_isProxyServerMode) {
          m_dialogID = DsSipTransactionManager.constructDialogID(m_sipResponse);
          DsSipTransactionManager.addToDialogMap(this);
        }

        // "The UAS MUST send any non-100 provisinal response
        //  reliably if the initial request contained a Require
        //  header field with option tag 100rel.
        //  If the UAS is unwilling to do so, it MUST reject
        // the initial request with a 420 (Bad Extension)..."
        if (m_sipRequest.headerContainsTag(REQUIRE, BS_100REL)
            && m_100relSupport == UNSUPPORTED
            && !m_isProxyServerMode) {
          DsSipResponse response420 =
              new DsSipResponse(
                  DsSipResponseCode.DS_RESPONSE_BAD_EXTENSION, m_sipRequest, null, null);
          DsSipUnsupportedHeader unsupportedHeader = new DsSipUnsupportedHeader(BS_100REL);
          response420.addHeader(unsupportedHeader);
          sendResponse(response420);
          throw new DsException(
              "UAC requires 100rel, but UAS does not support 100rel. 100rel negotiation failed");
        }
      }

      // Make sure the state transition properly with 1xx reliable responses
      if ((m_statusCode != 100)
          && (m_100relSupport != UNSUPPORTED)
          && (m_sipRequest != null)
          && ((m_sipRequest.headerContainsTag(REQUIRE, BS_100REL))
              || (m_sipRequest.headerContainsTag(SUPPORTED, BS_100REL)))
          && !m_isProxyServerMode) {
        // Make sure that Require: / Supported: 100rel is in place
        // The provisional response to be sent reliably is constructed by the
        // UAS core according to the procedures of Section 8.2.6 of RFC 3261.
        // In addition, it MUST contain a Require header field containing the
        // option tag 100rel, and MUST include an RSeq header field.

        if (!m_sipResponse.headerContainsTag(REQUIRE, BS_100REL)) {
          DsSipRequireHeader requireHeader = new DsSipRequireHeader(DsSipConstants.BS_100REL);
          m_sipResponse.addHeader(requireHeader, false, false);
        }
        DsSipRSeqHeader rseqHeader =
            (DsSipRSeqHeader) m_sipResponse.getHeaderValidate(DsSipConstants.RSEQ);
        if (rseqHeader == null) {
          rseqHeader = new DsSipRSeqHeader(0);
        }
        rseqHeader.setNumber(++m_rseqNumber);
        m_sipResponse.updateHeader(rseqHeader);

        execute(DS_ST_IN_REL_PROVISIONAL);
        // Set the flag to indicate that the 1xx is sent
        m_non100ResponseSent = true;
      } else {
        if (m_stateTable.getState() == DS_WAIT_PRACK
            || m_stateTable.getState() == DS_STI_RELPROCEEDING) {
          // We are in reliable state, no 100 should be sent.
          throw new DsException("No 100 Trying, or non-reliable 1xx after reliable 1xx is sent");
        } else {
          execute(DS_ST_IN_PROVISIONAL);
        }
      }
    } else if (statusCode < 300) {
      execute(DS_ST_IN_2XX);
    } else {
      execute(DS_ST_IN_3TO6XX);
    }
  }

  private void generate100Response() {
    // can't create response bytes directly if we're compressing
    if (m_via.getComp() == null) {
      m_sipResponseBytes =
          DsSipResponse.createResponseBytes(
              DsSipResponseCode.DS_RESPONSE_TRYING, m_sipRequest, null, null);
      // set to null to ensure that the bytes are the most
      // current response
      m_sipResponse = null;
    } else {
      m_sipResponse =
          new DsSipResponse(DsSipResponseCode.DS_RESPONSE_TRYING, m_sipRequest, null, null);
    }
  }

  /*
   * javadoc inherited
   */
  public final void setInterface(DsSipServerTransactionInterface serverInterface) {
    if (serverInterface != null) {
      /* If the client is registering for the first time, we need
      to update the registered interface with past events */
      if ((m_defaultServerInterface != null) && !m_userRegistered) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG, "setInterface(): user code registered for first time");
        }
        // tell the default interface to tell the new
        // interface if a CANCEL was received
        m_defaultServerInterface.notifyClient(serverInterface);
      }

      //  depending on the user code's action, chance of actually
      //  missng the CANCEL here..  but synchronization would hurt
      //  us worse -dg */
      m_serverInterface = serverInterface;
      m_userRegistered = true;
    } else // serverInterface == null
    {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(
            Level.DEBUG, "setInterface(): setting interface to default interface");
      }
      m_serverInterface = m_defaultServerInterface;
    }
  }

  /*
   * javadoc inherited
   */
  public final synchronized void abort() throws DsException {
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(Level.INFO, "abort(): begin");
    }

    if (m_cancelTransaction != null) {
      DsSipTransactionManager.getTransactionManager().processStrayCancel(m_cancelTransaction);
    }

    // remove ourselves from the transaction manager
    try {
      DsSipTransactionManager.removeTransaction(this);
    } catch (Exception dse) // prevent loop -- bury and warn   -dg
    {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("abort(): exception removing transaction from map", dse);
      }
    }

    // if Tn was set, cancel it
    cancelTn();
    // if TProvisional was set, cancel it
    cancelTProvisional();
  }

  public final DsSipTransactionKey getKeyNoVia() {
    return m_key;
  }

  /*
   * javadoc inherited
   */
  protected final void onRequestRetransmission(DsSipRequest request) {
    try {
      execute(DS_ST_IN_REQUEST);
    } catch (DsStateMachineException sme) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn(
            "onRequestRetransmission(): state machine exception processing request rtx", sme);
      }
    }
  }

  public final synchronized void onIOException(IOException exc) {
    if (genCat.isEnabled(Level.WARN)) {
      genCat.warn("onIOException async exception: ", exc);
    }

    try {
      execute(DS_ST_IN_IO_EXCEPTION);
    } catch (DsStateMachineException sme) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("onIOException(): state machine exception processing async exception", sme);
      }
    }
  }

  /*
   * javadoc inherited
   */
  protected void onAck(DsSipAckMessage request) {
    if (genCat.isEnabled(Level.WARN)) genCat.warn("onAck(): can't ACK a non-INVITE");
  }

  /*
   * javadoc inherited
   */
  // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
  protected final synchronized void onPrack(
      DsSipServerTransaction prackTransaction, DsSipPRACKMessage request) throws DsException {
    if (genCat.isEnabled(Level.DEBUG)) {
      genCat.log(Level.DEBUG, "onPrack: ");
      genCat.log(Level.DEBUG, String.valueOf(m_key));
      genCat.log(Level.DEBUG, "onPrack: ");
      genCat.log(Level.DEBUG, request.toString());
    }
    m_prackMessage = request;
    m_prackTransaction = prackTransaction;
    execute(DS_ST_IN_PRACK);
  }

  /*
   * javadoc inherited
   */
  protected final synchronized void onCancel(DsSipCancelMessage request)
      throws IOException, DsException {
    if (genCat.isEnabled(Level.DEBUG)) {
      genCat.debug("onCancel: ");
      genCat.debug(String.valueOf(m_key));
      genCat.debug("onCancel: ");
      genCat.debug(request.toCryptoInfoMaskedString());
    }
    m_cancelMessage = request;
    execute(DS_ST_IN_CANCEL);
  }

  /*
   * javadoc inherited
   */
  protected final synchronized boolean tryMarkCancelled(DsSipServerTransaction cancel_txn) {
    boolean started = isStarted();
    if (!started) {
      m_cancelTransaction = cancel_txn;
      m_cancelMessage = (DsSipCancelMessage) cancel_txn.getRequest();
    }
    return !started;
  }

  /*
   * javadoc inherited
   */
  protected final boolean isMerged() {
    return !m_isOriginal;
  }

  /*
   * javadoc inherited
   */
  protected final boolean isNew() {
    if (m_isNew) {
      m_isNew = false;
      return true;
    } else {
      return false;
    }
  }

  ////////////////////////////////////////////////////////////////

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////// State Machine ///////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /**
   * This is the method by which input is delivered to the state machine.
   *
   * @param input the input to the state machine
   * @throws DsStateMachineException if error occurs in the state machine
   * @see DsSipStateMachineDefinitions
   */
  protected synchronized void execute(int input) throws DsStateMachineException {
    int transition = m_stateTable.switchState(input);
    try {
      switch (m_stateTable.getState()) {
          // This is here historically if we want to do anything in the initial state
          // case DS_INITIAL:
          // if (DsPerf.ON) DsPerf.start(DsPerf.SERVER_EXEC_INITIAL);
          // initial(transition);
          // if (DsPerf.ON) DsPerf.stop(DsPerf.SERVER_EXEC_INITIAL);
          // break;
        case DS_CALLING:
          calling(transition);
          break;
        case DS_PROCEEDING:
          proceeding(transition);
          break;
        case DS_COMPLETED:
          completed(transition);
          break;
        case DS_CONFIRMED:
          confirmed(transition);
          break;
        case DS_TERMINATED:
          terminated(transition);
          break;
        default:
          break;
      }
    } catch (DsStateMachineException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionImpl.execute - DSE", dse);
      }
      throw dse;
    } catch (IOException exc) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionImpl.execute", exc);
      }
      execute(DS_ST_IN_IO_EXCEPTION);
    } catch (DsException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionImpl.execute", dse);
      }
      execute(DS_ST_IN_OTHER_EXCEPTION);
    } catch (Exception exc) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionImpl.execute", exc);
      }
      execute(DS_ST_IN_OTHER_EXCEPTION);
    }
  }

  /**
   * The finalStatus state machine method.
   *
   * @param transition (previous state) OR (input)
   */
  protected final void initial(int transition) {}

  /**
   * The calling state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void calling(int transition) throws DsException, IOException {
    switch (transition) {
      case DS_INITIAL | DS_ST_IN_START:
        break;
      case DS_INITIAL | DS_ST_IN_CANCEL:
        // no need to collect stat for this cancel. TM already did it.
        DsSipResponse cancelResponse =
            new DsSipResponse(DsSipResponseCode.DS_RESPONSE_OK, m_cancelMessage, null, null);
        cancelResponse.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
        m_cancelTransaction.sendResponse(cancelResponse);
        m_cancelTransaction = null;
        // fall through
      case DS_CALLING | DS_ST_IN_CANCEL:
        // no need to collect stat for this cancel. TM already did it.
        if (!m_isProxyServerMode && m_autoResponse) {
          // we are not a proxy server,
          // so automatically generate the 487 response and send it
          byte[] txnCancelledResponseBytes =
              DsSipResponse.createResponseBytes(
                  DsSipResponseCode.DS_RESPONSE_TRANSACTION_CANCELLED, m_sipRequest, null, null);
          m_connection.getResponseConnection();
          sendResponse(
              txnCancelledResponseBytes, DsSipResponseCode.DS_RESPONSE_TRANSACTION_CANCELLED);
        }
        new ServerTransactionCallback(
                ServerTransactionCallback.CB_CANCEL, m_cancelMessage, m_serverInterface)
            .call();
        m_cancelMessage = null;
        break;
      case DS_CALLING | DS_ST_IN_REQUEST:
        // no need to collect stat for the request. TM already did it.
        break;
        // CAFFEINE 2.0 DEVELOPMENT - required by handling reliable response.
      case DS_STI_RELPROCEEDING | DS_ST_IN_2XX:
        sendCurrentResponse();
        nullRefs();
        cancelTn(); // transaction will terminate normally
        break;
      case DS_CALLING | DS_ST_IN_ACK:
        // this is possible for proxy Invite Server txn when 100/200 crosses wire with
        // Invite retransmission. The 200 has terminated original proxy Invite server txn,
        // and the Invite rtx causes a new Invite server txn to be created and it could
        // be in calling state when the ACK for 200 comes in. Ignore that
        // ACK. The proxy will work out.
        if (m_isProxyServerMode) break;
        // else go to next case
      default:
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
    // maivu - 11.01.06 - CSCsg22401 Transition to Proceeding state so cancel the server tx timer
    // which was set in the DsSipServerTransactionImpl constructor
    cancelExpirationTimer();

    switch (transition) {
      case DS_INITIAL | DS_ST_IN_TPROVISIONAL:
      case DS_CALLING | DS_ST_IN_TPROVISIONAL:
        m_statusCode = (short) DsSipResponseCode.DS_RESPONSE_TRYING;
        generate100Response();
      case DS_INITIAL | DS_ST_IN_PROVISIONAL:
      case DS_CALLING | DS_ST_IN_PROVISIONAL:
        m_connection.getResponseConnection();
        initializeTimers();
        sendCurrentResponse();
        break;
      case DS_PROCEEDING | DS_ST_IN_REQUEST:
        // no need to collect stat for the request. TM already did it.
        sendCurrentResponse();
        break;
      case DS_PROCEEDING | DS_ST_IN_TPROVISIONAL:
        // do nothing: provisional has been sent
        break;
      case DS_PROCEEDING | DS_ST_IN_PROVISIONAL:
        sendCurrentResponse();
        break;
      case DS_PROCEEDING | DS_ST_IN_CANCEL:
        // no need to collect stat for this cancel. TM already did it.
        if (!m_isProxyServerMode && m_autoResponse) {
          byte[] txnCancelledResponseBytes =
              DsSipResponse.createResponseBytes(
                  DsSipResponseCode.DS_RESPONSE_TRANSACTION_CANCELLED, m_sipRequest, null, null);
          sendResponse(
              txnCancelledResponseBytes, DsSipResponseCode.DS_RESPONSE_TRANSACTION_CANCELLED);
        }
        new ServerTransactionCallback(
                ServerTransactionCallback.CB_CANCEL, m_cancelMessage, m_serverInterface)
            .call();
        m_cancelMessage = null;
        break;
      case DS_PROCEEDING | DS_ST_IN_ACK:
        // this is possible for proxy Invite Server txn when 100/200 crosses wire with
        // Invite retransmission. The 200 terminates original proxy Invite server txn,
        // and the Invite rtx causes a new Invite server txn to be created and it can
        // send 100 and be in proceeding state when the ACK for 200 comes in. Ignore that
        // ACK. The proxy will work out.
        if (m_isProxyServerMode) break;
        // else go to next case
      case DS_PROCEEDING | DS_ST_IN_NEXT_CLIENT:
        // m_connection.getResponseConnection();
        sendCurrentResponse();
      case DS_PROCEEDING | DS_ST_IN_IO_EXCEPTION:
        if (m_connection.getNextConnection()) {
          execute(DS_ST_IN_NEXT_CLIENT);
        } else {
          execute(DS_ST_IN_NO_CLIENT);
        }
        break;
      default:
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
    // which was set in the DsSipServerTransactionImpl constructor
    cancelExpirationTimer();

    switch (transition) {
      case DS_INITIAL | DS_ST_IN_2XX:
      case DS_INITIAL | DS_ST_IN_3TO6XX:
      case DS_CALLING | DS_ST_IN_2XX:
      case DS_CALLING | DS_ST_IN_3TO6XX:
        m_connection.getResponseConnection();
        initializeTimers();
        // fall through
      case DS_PROCEEDING | DS_ST_IN_2XX:
      case DS_PROCEEDING | DS_ST_IN_3TO6XX:
        sendCurrentResponse();
        nullRefs();
        if (m_To == 0) // directly go to TERMINATED without using timer
        {
          execute(DS_ST_IN_TIMEOUT);
        } else {
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
          }
          DsDiscreteTimerMgr.scheduleNoQ(m_To, this, IN_TIMEOUT);
        }
        cancelTn(); // transaction will terminate normally
        break;
      case DS_COMPLETED | DS_ST_IN_REQUEST:
        // no need to collect stat for the request. TM already did it.
        sendCurrentResponse();
        break;
      case DS_COMPLETED | DS_ST_IN_2XX:
      case DS_COMPLETED | DS_ST_IN_3TO6XX:
        // ignore new final response
        break;
      case DS_COMPLETED | DS_ST_IN_CANCEL:
        // no need to collect stat for this cancel. TM already did it.
        // ignore cancels to completed transactions - nothing left to cancel
        break;
      case DS_COMPLETED | DS_ST_IN_IO_EXCEPTION:
        if (m_connection.getNextConnection()) {
          execute(DS_ST_IN_NEXT_CLIENT);
        } else {
          execute(DS_ST_IN_NO_CLIENT);
        }
        break;
      case DS_COMPLETED | DS_ST_IN_NEXT_CLIENT:
        // m_connection.getResponseConnection();
        sendCurrentResponse();
        nullRefs();
        if (m_To == 0) // directly go to TERMINATED without using timer
        {
          execute(DS_ST_IN_TIMEOUT);
        } else {
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
          }
          DsDiscreteTimerMgr.scheduleNoQ(m_To, this, IN_TIMEOUT);
        }
        cancelTn(); // transaction will terminate normally
        break;
      case DS_COMPLETED | DS_ST_IN_TPROVISIONAL:
        // do nothing; the transaction completed normally - ignore
        //    timed provisional input
        break;
      default:
        m_stateTable.throwException(transition);
        break;
    }
  }

  /**
   * The confimed state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void confirmed(int transition) throws DsException, IOException {
    throw new DsStateMachineException(
        "confirmed: Non-INVITE server transaction should not enter this state");
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
    // which was set in the DsSipServerTransactionImpl constructor
    cancelExpirationTimer();

    switch (transition & DS_MASK) {
      case (DS_TERMINATED):
        break;
      default:
        switch (transition & DS_INPUT_MASK) {
          case (DS_ST_IN_NO_CLIENT):
            new ServerTransactionCallback(
                    ServerTransactionCallback.CB_EXCEPTION, null, m_serverInterface)
                .call();
          case (DS_ST_IN_Tn):
            new ServerTransactionCallback(
                    ServerTransactionCallback.CB_TIMEOUT, null, m_serverInterface)
                .call();
            break;
          default:
            cancelTn();
            break;
        }
        nullRefs();
        releaseConnections();
        try {
          DsSipTransactionManager.removeTransaction(this);
        } catch (DsException dse) {
          if (genCat.isEnabled(Level.WARN))
            genCat.warn("terminated(): failed to remove transaction", dse);
        }

        break;
    }
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited
   */
  public void run(Object arg) {
    try {
      if (arg == IN_T1) {
        execute(DS_ST_IN_T1);
      } else if (arg == IN_Tn) {
        execute(DS_ST_IN_Tn);
      } else if (arg == IN_TIMEOUT) {
        execute(DS_ST_IN_TIMEOUT);
      } else if (arg == IN_TPROVISIONAL) {
        execute(DS_ST_IN_TPROVISIONAL);
      }

    } catch (DsStateMachineException sme) {
      if (genCat.isEnabled(Level.WARN))
        genCat.warn("run(): state machine exception processing timer event", sme);
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
    Logger cat = DsLog4j.LlSMServerTimersCat;
    // CAFFEINE 2.0 DEVELOPMENT - requried check before logging the message.
    if (cat.isEnabled(Level.DEBUG)) {
      cat.debug(
          " SCHEDULE_"
              + (noq ? "NOQUEUE " : "QUEUE ")
              + "table: "
              + m_stateTable.getName()
              + " variable: "
              + varname
              + " input: "
              + inname
              + " time: "
              + time);
    }
  }

  /** Release connection -- decrement its ref count. */
  protected final void releaseConnections() {
    m_connection.release();
  }

  final void cancelTn() {
    if (m_TimerTaskTn != null) {
      m_TimerTaskTn.cancel();
      m_TimerTaskTn = null;
    }
  }

  private final void cancelTProvisional() {
    if (m_TimerTaskTProvisional != null) {
      m_TimerTaskTProvisional.cancel();
      m_TimerTaskTProvisional = null;
    }
  }

  // maivu - 11.01.06 - CSCsg22401 Cancel the request expiration timer if it is set before.
  public final void cancelExpirationTimer() {
    if (m_expirationTimerTask != null) {
      m_expirationTimerTask.cancel();
      m_expirationTimerTask = null;
    }
  }

  /**
   * Sets the auto response flag, that tells whether 487 response will be generated and sent on
   * receiving CANCEL for this non-proxy server mode transaction automatically, while this
   * transaction is still in CALLING or PROCEEDING state.
   *
   * @param auto if <code>true</code>, then the 487 response will be generated and send, for this
   *     non-proxy server mode transaction.
   */
  public void setAutoResponse(boolean auto) {
    m_autoResponse = auto;
  }

  /**
   * Tells whether 487 response will be generated and sent on receiving CANCEL for this non-proxy
   * server mode transaction automatically, while this transaction is still in CALLING or PROCEEDING
   * state.
   *
   * @return <code>true</code> if 487 response will be generated and send, for this non-proxy server
   *     mode transaction.
   */
  public boolean getAutoResponse() {
    return m_autoResponse;
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

  /** Manage the underlying DsSipConnection reference count. */
  protected final class ConnectionWrapper implements java.io.Serializable // , DsSipResolver
  {
    private transient DsSipConnection m_connection_; // = null;
    private transient InetAddress addr;
    private transient int port;

    ConnectionWrapper(DsSipConnection connection) {
      replace(connection);
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

    DsSipConnection replace(DsSipConnection new_connection) {
      if (m_connection_ == new_connection) return new_connection;
      if (m_connection_ != null) {
        m_connection_.removeReference();
      }

      m_connection_ = new_connection;
      if (m_connection_ != null) {
        addr = m_connection_.getBindingInfo().getRemoteAddress();
        port = m_connection_.getBindingInfo().getRemotePort();
        m_connection_.addReference();
      }

      return m_connection_;
    }

    /**
     * Send a message via the current underlying connection.
     *
     * @param message the message to send
     * @return the serialized bytes of the message
     * @throws IOException if the underlying socket throws this exception
     */
    protected byte[] send(DsSipMessage message) throws IOException {
      if (m_connection_ == null) {
        throw new IOException("Can't connect to client.");
      }

      // serialize and set the finalized flag
      message.setFinalised(true);
      byte[] ret_bytes = m_connection_.sendTo(message, addr, port, DsSipServerTransactionImpl.this);
      // Update the binding info
      message.updateBinding(m_connection_.getBindingInfo());
      return ret_bytes;
    }

    /**
     * Send a message as bytes via the current underlying connection.
     *
     * @param buffer the message bytes
     * @throws IOException if the underlying socket throws this exception
     */
    protected void send(byte buffer[]) throws IOException {
      if (m_connection_ == null) {
        throw new IOException("Can't connect to client.");
      }

      m_connection_.sendTo(buffer, addr, port, DsSipServerTransactionImpl.this);
    }

    void release() {
      synchronized (m_connection) {
        if (m_connection_ != null) {
          m_connection_.removeReference();
          m_connection_ = null;
        }
      }
    }

    DsSipConnection getConnection() {
      return m_connection_;
    }

    DsBindingInfo getBindingInfo() {
      if (m_connection_ == null) {
        return null;
      }

      return m_connection_.getBindingInfo();
    }

    // try the next client using the existing client locator
    private boolean tryNextClient(DsSipResponse response) {
      boolean ret_value = true;
      try {
        replace(client_locator.tryConnect());
        if (response != null) {
          DsBindingInfo current = client_locator.getCurrentBindingInfo();
          if (current != null) {
            response.setBindingInfo(current);
          }
        }

        if (m_connection_ == null) {
          ret_value = false;
        }
      } catch (DsException dse) {
        ret_value = false;
      } catch (IOException ioe) {
        ret_value = false;
      }

      return ret_value;
    }

    /**
     * When we are currently use connection from "received", use sent-by and do SRV query. If we are
     * already using a connection from SRV query, try next host. This method is called when we have
     * an IO Exception and want to try to connect to another host.
     *
     * @return <code>true</code> if the next connection is available, <code>false
     *         </code> otherwise.
     */
    boolean getNextConnection()
        throws DsException, SocketException, UnknownHostException, IOException {
      DsSipTransportLayer transport_layer = DsSipTransactionManager.getTransportLayer();

      InetAddress laddr = null;
      int lport = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;

      DsNetwork network = m_bindingInfo.getNetwork();

      if (network != null && network.isBehindNAT()) {
        laddr = m_bindingInfo.getLocalAddress();
        lport = m_bindingInfo.getLocalPort();
      }

      switch (m_resolverStage) {
        case USING_MADDR:
          m_addr_str = DsByteString.toString(m_via.getReceived());

          if (m_addr_str != null) {
            m_resolverStage = USING_RECEIVED;
            addr = InetAddress.getByName(m_addr_str);
            DsSipConnection ret_connection =
                (DsSipConnection)
                    transport_layer.getConnection(
                        m_bindingInfo.getNetwork(), laddr, lport, addr, m_port, m_transport);
            replace(ret_connection);
            return true;
          }
          // else fall through
        case USING_RECEIVED:

          // use sent-by addr to locate host
          m_addr_str = DsByteString.toString(m_via.getHost());
          if (m_addr_str == null) {
            throw new DsException("maddr, received, and host parameters are null in VIA header");
          }

          if (m_simpleResolver) {
            m_resolverStage = USING_SIMPLE_SENT_BY;
            addr = InetAddress.getByName(m_addr_str);
            DsSipConnection ret_connection =
                (DsSipConnection)
                    transport_layer.getConnection(
                        m_bindingInfo.getNetwork(), laddr, lport, addr, m_port, m_transport);
            replace(ret_connection);
            return true;
          } else {
            m_resolverStage = USING_SRV_SENT_BY;
            // pass m_sipRequest in only to use its callid for hashing
            if (useDeterministicLocator()) {
              client_locator = new DsSipDetServerLocator(m_sipRequest);
            } else {
              client_locator = new DsSipServerLocator();
            }
            // first try to get existing connection, if fails, initialize server locator
            DsSipConnection ret_connection =
                DsSipTransactionManager.getSRVConnection(
                    m_sipRequest.getNetwork(),
                    laddr,
                    lport,
                    m_addr_str,
                    m_port,
                    m_transport,
                    client_locator);
            if (ret_connection == null) ret_connection = client_locator.tryConnect();
            return !(replace(ret_connection) == null);
          }

        case USING_SRV_SENT_BY: // already using sent-by and client locator
          return tryNextClient(m_sipResponse);

        case USING_SIMPLE_SENT_BY: // do not use SRV lookup, nothing can be done *
          // * Now we have one thing we can try if we have not already, retry once in case the
          // connection dropped
          // (not for UDP) mid-transaction and we an recover by trying to re-connect to the client.
          if (m_simpleResolver && m_retryOnIoExc && m_bindingInfo.getTransport() != Transport.UDP) {
            m_retryOnIoExc = false; // retry only once

            // start over with get via connection
            getViaConnection();

            return true;
          }
          return false;

        default:
          return false;
      }
    }

    /**
     * Get the connection for the SIP response using the via of the request. See bis 07 19.2.2 for
     * the logic description.
     *
     * @throws SocketException if the underlying socket throws this exception
     * @throws DsException if there is an error in the User Agent
     * @throws UnknownHostException if the underlying socket throws this exception
     * @throws IOException if the underlying socket throws this exception
     */
    protected void getViaConnection()
        throws SocketException, DsException, UnknownHostException, IOException {
      InetAddress laddr = null;
      int lport = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;

      DsSipConnection ret_connection = null;
      DsSipTransportLayer transport_layer = DsSipTransactionManager.getTransportLayer();

      InetAddress addr = m_bindingInfo.getRemoteAddress();
      m_port = m_bindingInfo.getRemotePort();
      m_transport = m_bindingInfo.getTransport();

      boolean reliable = DsSipTransportType.intern(m_transport).isReliable();

      if (reliable) {
        // CSCsq83582: Reusing TCP connection to send response
        // Try to use the entire binding info so that we take into account the local Addr and Port
        // This will find the right connection if the app is using its own connection pooling
        ret_connection = (DsSipConnection) transport_layer.findConnection(m_bindingInfo);

        // Fall back to the old style if the lookup with local info does not work
        if (ret_connection == null) {
          ret_connection =
              (DsSipConnection) transport_layer.findConnection(addr, m_port, m_transport);
        }
      }

      if (ret_connection == null) {
        DsNetwork network = m_bindingInfo.getNetwork();
        if (m_via == null) {
          throw new DsException("No Via Header in initial request");
        }
        m_transport = m_via.getTransport();

        if (m_via.isRPortSet()) {
          // must send response to source address/ source port
          m_port = m_via.getRPort();
          addr = m_bindingInfo.getRemoteAddress();

          // and from the destination port of the request
          laddr = m_bindingInfo.getLocalAddress();
          lport = m_bindingInfo.getLocalPort();

          // Prevent failover logic from being invoked by saying that
          //  we're already trying the sentby.  Failover doesn't make sense
          //  if we're using RPORT and listening port for sending data
          m_resolverStage = USING_SIMPLE_SENT_BY;

          if (genCat.isEnabled(Level.DEBUG)) {
            genCat.debug(
                "getViaConnection:"
                    + " using rport, local addr = "
                    + laddr
                    + " local port = "
                    + lport
                    + " addr = "
                    + addr
                    + " port = "
                    + m_port);
          }

          // force us to use a listening port for the send
          ret_connection =
              (DsSipConnection)
                  transport_layer.getConnection(
                      network, laddr, lport, addr, m_port, m_transport, true);
        } // end if RPORT is set
        else {
          if (network != null && network.isBehindNAT()) {
            laddr = m_bindingInfo.getLocalAddress();
            lport = m_bindingInfo.getLocalPort();
            m_port = m_bindingInfo.getRemotePort();
            addr = m_bindingInfo.getRemoteAddress();
            m_resolverStage = USING_SIMPLE_SENT_BY;
            genCat.debug(
                "getViaConnection: "
                    + "behind NAT, local addr = "
                    + laddr
                    + " local port = "
                    + lport
                    + " addr = "
                    + addr
                    + " port = "
                    + m_port);
          } else {
            // retrieve the received port
            m_port = m_via.getPort();
            laddr = m_bindingInfo.getLocalAddress();

            // check maddr first use as addresss, then received
            // then source addr
            m_addr_str = DsByteString.toString(m_via.getMaddr());
            if (m_addr_str != null) {
              m_resolverStage = USING_MADDR;
              addr = InetAddress.getByName(m_addr_str);
            } else {
              m_addr_str = DsByteString.toString(m_via.getReceived());
              if (m_addr_str != null) {
                m_resolverStage = USING_RECEIVED;
                addr = InetAddress.getByName(m_addr_str);
              } else {
                // use sent-by addr to locate host
                m_addr_str = DsByteString.toString(m_via.getHost());
                if (m_addr_str == null) {
                  throw new DsException(
                      "maddr, received, and host parameters are null in VIA header");
                }
                if (m_simpleResolver) {
                  m_resolverStage = USING_SIMPLE_SENT_BY;
                  addr = InetAddress.getByName(m_addr_str);
                } else {
                  m_resolverStage = USING_SRV_SENT_BY;
                }
              }
            } // end else for if m_addr_str != null
          } // end else for behind NAT

          if (m_resolverStage == USING_SRV_SENT_BY) {
            // pass m_sipRequest in only to use its callid
            // for hashing
            if (useDeterministicLocator()) {
              client_locator = new DsSipDetServerLocator(m_sipRequest);
            } else {
              client_locator = new DsSipServerLocator();
            }
            // first try to get existing connection, if fails,
            // initialize server locator
            ret_connection =
                DsSipTransactionManager.getSRVConnection(
                    m_sipRequest.getNetwork(), m_addr_str, m_port, m_transport, client_locator);
            if (ret_connection == null) {
              ret_connection = client_locator.tryConnect();
            }
          } else {
            ret_connection =
                (DsSipConnection)
                    transport_layer.getConnection(
                        m_bindingInfo.getNetwork(), laddr, lport, addr, m_port, m_transport);
          }
        } // end else for  if RPORT is set
      } // end if ret_connection is null
      replace(ret_connection);
    } // end getViaConnection()

    /**
     * Returns the conenction response using the Via header.
     *
     * @throws DsException if there is a DsException
     * @throws SocketException the there is a socket exception
     * @throws UnknownHostException if the host is not known
     * @throws IOException if there is an I/O exception
     */
    protected void getResponseConnection()
        throws DsException, SocketException, UnknownHostException, IOException {
      if (m_connection_ == null) {
        getViaConnection();
      }
    }
  }

  /*
          protected void getViaConnection()
              throws SocketException, DsException, UnknownHostException, IOException
          {
              if (DsPerf.ON) DsPerf.start(DsPerf.SERVER_GET_VIA_CONNECTION);
              DsSipConnection     ret_connection = null;

              DsSipTransportLayer transport_layer = DsSipTransactionManager.getTransportLayer();

              InetAddress         addr = m_bindingInfo.getRemoteAddress();
              int                 port = m_bindingInfo.getRemotePort();
              int                 transport = m_bindingInfo.getTransport();

              boolean reliable = DsSipTransportType.intern(transport).isReliable();
              if (reliable)
              {
                  //ret_connection = transport_layer.findConnection(info);
                  ret_connection = (DsSipConnection)transport_layer.findConnection(addr, port, transport);
              }

              if (ret_connection == null)
              {
                  if (m_via == null)
                  {
                      throw new DsException("No Via Header in initial request");
                  }

                  transport = m_via.getTransport();
                  port = m_via.getPort();

                  // check maddr first use as addresss, then received then source addr
                  DsByteString addr_byte_str = m_via.getMaddr();

                  if (addr_byte_str != null)
                  {
                      addr = InetAddress.getByName(addr_byte_str.toString());
                  }
                  else
                  {
                      addr_byte_str = m_via.getReceived();

                      if (addr_byte_str != null)
                      {
                          addr = InetAddress.getByName(addr_byte_str.toString());
                      }
                      else
                      {
                          addr = m_bindingInfo.getRemoteAddress();
                      }
                  }

                  ret_connection = (DsSipConnection)transport_layer.getConnection(addr, port, transport);
              }

              replace(ret_connection);
              if (DsPerf.ON) DsPerf.stop(DsPerf.SERVER_GET_VIA_CONNECTION);
          }
      }
  */

  /** Client callback object. */
  final class ServerTransactionCallback implements DsUnitOfWork {
    public static final int CB_ACK = 0;
    public static final int CB_CANCEL = 1;
    public static final int CB_TIMEOUT = 2;
    public static final int CB_EXCEPTION = 3;
    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
    public static final int CB_PRACK = 4;

    DsSipServerTransactionInterface cb;
    DsSipRequest request;
    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
    DsSipServerTransaction st;
    int type;

    ServerTransactionCallback(int type, DsSipRequest request, DsSipServerTransactionInterface cb) {
      this.cb = cb;
      this.request = request;
      this.type = type;
    }

    ServerTransactionCallback(
        int type,
        DsSipRequest request,
        DsSipServerTransaction serverTransaction,
        DsSipServerTransactionInterface cb) {
      this.cb = cb;
      this.request = request;
      this.st = serverTransaction;
      this.type = type;
    }

    public void call() {
      m_callbackQueue.nqueue(this);
    }

    public void run() {
      process();
    }

    public void process() {
      if (cb == null) {
        cb =
            DsSipTransactionManager.getTransactionManager()
                .getStatelessServerTransactionInterface();
      }
      switch (type) {
        case (CB_ACK):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.debug("CB_ACK: ");
            cbCat.debug(String.valueOf(m_key));
            cbCat.debug("CB_ACK: ");
            cbCat.debug(request.maskAndWrapSIPMessageToSingleLineOutput());
          }

          DsSipRouteFixInterface rfi =
              DsSipTransactionManager.getTransactionManager().getRouteFixInterface();
          if ((rfi != null) && (request != null)) {
            try {
              request.lrFix(rfi);
            } catch (Exception exc) {
              if (cbCat.isEnabled(Level.WARN)) {
                cbCat.warn("Exception calling route fix interface for ACK", exc);
              }
            }
          }

          cb.ack(DsSipServerTransactionImpl.this, (DsSipAckMessage) request);
          request = null;
          break;
        case (CB_CANCEL):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.debug("CB_CANCEL: ");
            cbCat.debug(String.valueOf(m_key));
            cbCat.debug("CB_CANCEL: ");
            cbCat.debug(request.maskAndWrapSIPMessageToSingleLineOutput());
          }

          cb.cancel(DsSipServerTransactionImpl.this, (DsSipCancelMessage) request);
          request = null;
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

          cb.timeOut(DsSipServerTransactionImpl.this);
          // m_serverInterface = null;
          break;
        case (CB_EXCEPTION):
          if (cbCat.isEnabled(Level.INFO)) {
            cbCat.info("CB_EXCEPTION: ");
            cbCat.info(String.valueOf(m_key));
          }

          cb.icmpError(DsSipServerTransactionImpl.this);
          break;
          // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
        case (CB_PRACK):
          if (cbCat.isEnabled(Level.DEBUG)) {
            cbCat.log(Level.DEBUG, "CB_PRACK: ");
            cbCat.log(Level.DEBUG, String.valueOf(m_key));
            cbCat.log(Level.DEBUG, "CB_PRACK: ");
            cbCat.log(Level.DEBUG, request.maskAndWrapSIPMessageToSingleLineOutput());
          }

          cb.prack(DsSipServerTransactionImpl.this, st);
          request = null;
          break;
      }
    }

    public void abort() {}
  }

  /**
   * The DefaultServerInterface class holds the default implementation of the
   * DsSipServerTransactionInterface and is utilized by default if the user of the lower layer does
   * not supply their own interface.
   */
  final class DefaultServerInterface implements DsSipServerTransactionInterface {
    private DsSipCancelMessage m_cancel = null;
    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
    private DsSipPRACKMessage m_prack = null;

    /**
     * The default server interfaces timeOut() method.
     *
     * @param serverTransaction
     * @param ackMessage
     */
    public void ack(DsSipServerTransaction serverTransaction, DsSipAckMessage ackMessage) {}

    /**
     * The default server interfaces close() method.
     *
     * @param serverTransaction
     */
    public void close(DsSipServerTransaction serverTransaction) {}

    /**
     * The default server interfaces cancel() method.
     *
     * @param serverTransaction
     * @param cancelMessage
     */
    public void cancel(DsSipServerTransaction serverTransaction, DsSipCancelMessage cancelMessage) {
      m_cancel = cancelMessage;
    }

    // CAFFEINE 2.0 DEVELOPMENT - required by handling PRACK.
    /**
     * PRACK: The default server interfaces prack() method.
     *
     * @param inviteServerTransaction handle of the INVITE server transaction
     * @param prackServerTransaction handle of the PRACK server transaction
     */
    public void prack(
        DsSipServerTransaction inviteServerTransaction,
        DsSipServerTransaction prackServerTransaction) {
      m_prack = (DsSipPRACKMessage) (prackServerTransaction.getRequest());
    }

    /**
     * The default server interfaces timeOut() method.
     *
     * @param serverTransaction
     */
    public void timeOut(DsSipServerTransaction serverTransaction) {}

    public void icmpError(DsSipServerTransaction serverTransaction) {}

    /**
     * Notify the client if a CANCEL has been received between the time of instantiation and now.
     */
    void notifyClient(DsSipServerTransactionInterface st_interface) {
      if (m_cancel != null) {
        st_interface.cancel(DsSipServerTransactionImpl.this, m_cancel);

        if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
          DsLog4j.connectionCat.log(
              Level.INFO, "setInterface(): user code was notified of a CANCEL");
        }
      } else {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(Level.DEBUG, "setInterface(): no updates");
        }
      }
    }
  } // End DefaultServerInterface

  // maivu - 11.01.06 - CSCsg22401
  /**
   * A Timer event for the request expiration. This timer is set during the
   * DsSipServerTransactionImpl constructor.
   */
  private class ExpirationTimer implements DsEvent {
    public void run(Object argument) {
      if (genCat.isEnabled(Level.DEBUG)) {
        genCat.debug("Server Transaction Expiration Timer FIRED");
      }

      try {
        sendResponse(DsSipResponseCode.DS_RESPONSE_REQUEST_TERMINATED);
        new ServerTransactionCallback(ServerTransactionCallback.CB_TIMEOUT, null, m_serverInterface)
            .call();
      } catch (Exception ex) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("Exception thrown when reject request on expiration: " + ex.getMessage(), ex);
        }
      }
    }
  }

  /*
   * javadoc inherited
   */
  public DsSipResponse getResponse() {
    if (m_sipResponse != null) {
      return m_sipResponse;
    }

    try {
      return (DsSipResponse) DsSipMessage.createMessage(m_sipResponseBytes);
    } catch (Exception e) {
      // this should never happen
      return null;
    }
  }

  /**
   * Return value is the effective logging reason In cases where response object is null , message
   * bytes is used to log the message, in such cases "messageReason" variable will have the reason
   * if its generated internally
   *
   * @param response response being logged
   * @param loggingReason logging reason
   * @return returns AUTO if it is set in the response , or returns the logging reason
   */
  public int getReason(DsSipResponse response, int loggingReason) {
    // condition added to send correct reason
    // messageReason is taken from applicationReason which is right value for retransmission
    if (DsMessageLoggingInterface.REASON_RETRANSMISSION == messageReason) {
      return messageReason;
    }

    int responseReason = DsMessageLoggingInterface.REASON_REGULAR;
    if (response == null) {
      responseReason = messageReason;
    }
    return (responseReason == DsMessageLoggingInterface.REASON_AUTO)
        ? responseReason
        : loggingReason;
  }
}
