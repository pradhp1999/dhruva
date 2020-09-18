// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.event.Level;

/**
 * Implements the server side of the low level INVITE state machine.
 *
 * <p>As per RFC 3261, the ACK request is a transaction by itself and should have different branch
 * id than that of the corresponding INVITE request. While in Proxy Server mode, the INVITE server
 * transaction need not hold the state to match up with the consecutive ACK message and thus gives
 * the oppertunity to clean up the server transaction after sending the 2xx final response. Once the
 * INVITE server transaction is cleaned, then the consecutive incoming ACK message will be delivered
 * to the user code as a stray ACK. If the application wants to receive this ACK through the INVITE
 * server transaction interface then Java System property {@link DsConfigManager#PROP_X200TERM
 * "com.dynamicsoft.DsLibs.DsSipLlApi.x200Terminated"} should be set to <code>true</code>. By
 * default, this property is {@link DsConfigManager#PROP_X200TERM_DEFAULT false}.
 *
 * @see DsSipServerTransactionImpl
 * @see DsSipClientTransactionIImpl
 * @see DsSipClientTransactionImpl
 */
public class DsSipServerTransactionIImpl extends DsSipServerTransactionImpl {
  /** <code>true</code> if we are running as a JAIN stack. */
  private static final boolean m_jainCompatability;

  private static boolean m_send100UponInvite; // = false;
  private static DsByteString TO_TAG;

  private DsSipAckMessage m_ackMessage;
  private DsSipDialogID m_dialogID;

  // moved from Impl
  private int m_T1; // = DsSipTimers.T1Value;
  private int m_T2; // = DsSipTimers.T2Value;
  private byte m_TCounter; // = 0;

  ScheduledFuture m_TimerTaskT1;

  static {
    m_jainCompatability =
        DsConfigManager.getProperty(DsConfigManager.PROP_JAIN, DsConfigManager.PROP_JAIN_DEFAULT);
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
  protected DsSipServerTransactionIImpl(
      DsSipRequest request,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    super(request, serverInterface, transactionParams);
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
  protected DsSipServerTransactionIImpl(
      DsSipRequest request,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams,
      boolean isOriginal)
      throws DsException {
    super(request, serverInterface, transactionParams, isOriginal);
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
  protected DsSipServerTransactionIImpl(
      DsSipRequest request,
      DsSipTransactionKey keyWithVia,
      DsSipTransactionKey keyNoVia,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams,
      boolean isOriginal)
      throws DsException {
    super(request, keyWithVia, keyNoVia, serverInterface, transactionParams, isOriginal);
  }

  /**
   * This constructor calculates the transaction's keys and sets the server transaction interface.
   * It is designed to do minimal work.
   *
   * @param keyWithVia the transaction key constructed with the Via or null if this key should be
   *     calculated here
   * @param keyNoVia the transaction key constructed without the Via or null if this key should be
   *     calculated here
   * @param request the incoming request
   * @param serverInterface optional callback interface to user-level callbacks
   * @param transactionParams optional reserved for future use.
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipServerTransactionIImpl(
      DsSipRequest request,
      DsSipTransactionKey keyWithVia,
      DsSipTransactionKey keyNoVia,
      DsSipServerTransactionInterface serverInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    super(request, keyWithVia, keyNoVia, serverInterface, transactionParams);
  }

  protected void initializeTimers() {
    m_T1 = m_sipTimers.T1Value;
    m_T2 = m_sipTimers.T2Value;
    m_To = m_sipTimers.T4Value;

    if (DsSipTransportType.intern(m_connection.getTransport()).isReliable()) {
      m_To = 0;
    }
  }

  /** Creates state table for this transaction. */
  protected void createStateTable() {
    // CAFFEINE 2.0 DEVELOPMENT - Moved to a new class DsSipStateMachineTransitions
    m_stateTable = new DsSipStateTable(DsSipStateMachineTransitions.STI_TRANSITIONS);
  }

  /*
   * javadoc inherited
   */
  public boolean isInvite() {
    return true;
  }

  /**
   * Sets whether this INVITE server transaction will send 100 response immediately after it
   * receives an INVITE, which is the desired behavior in bis 05. by default, no 100 is sent upon
   * receipt of first INVITE.
   *
   * @param sendFlag whether to send 100 upon first INVITE automatically.
   */
  public static final void send100UponInvite(boolean sendFlag) {
    m_send100UponInvite = sendFlag;
  }

  /**
   * Gets whether this INVITE server transaction will send 100 response immediately after it
   * receives an INVITE, which is the desired behavior in bis 05. by default, no 100 is sent upon
   * receipt of first INVITE.
   *
   * @return whether to send 100 upon first INVITE automatically.
   */
  public static boolean send100UponInvite() {
    return m_send100UponInvite;
  }

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
          // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
        case DS_WAIT_PRACK:
          waitPrack(transition);
          break;
        case DS_STI_RELPROCEEDING:
          reliableProceeding(transition);
          break;

          /* we start by switch from xinitial state to xcompleted, so
          no need to handle DS_XINITIAL */
        case DS_XCOMPLETED:
          xcompleted(transition);
          break;
        case DS_XCONFIRMED:
          xconfirmed(transition);
          break;
        case DS_XTERMINATED:
          xterminated(transition);
          break;
        default:
          break;
      }
    } catch (DsStateMachineException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionIImpl.execute - DSE", dse);
      }
      throw dse;
    } catch (IOException exc) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionIImpl.execute", exc);
      }
      execute(DS_ST_IN_IO_EXCEPTION);
    } catch (DsException dse) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionIImpl.execute", dse);
      }
      execute(DS_ST_IN_OTHER_EXCEPTION);
    } catch (Exception exc) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn("DsSipServerTransactionIImpl.execute", exc);
      }
      execute(DS_ST_IN_OTHER_EXCEPTION);
    }
  }

  /**
   * Cleanup references when transaction goes to the completed state.
   *
   * @param nullCallback set to <code>true</code> to null the server transaction callback as well
   */
  protected void nullRefs(boolean nullCallback) {
    removeSession();
    m_ackMessage = null;
    m_sipResponseBytes = null;
    m_sipResponse = null;
    // m_cancelMessage = null;
    m_defaultServerInterface = null;
    m_cancelTransaction = null;
    if (m_cleanup) {
      m_sipRequest = null;
      if (nullCallback) {
        m_serverInterface = null;
      }
    }
  }

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  // Adding 2 new methods below: waitPrack and reliableProceeding

  /**
   * The wait PRACK state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void waitPrack(int transition)
      throws DsException, IOException, ExecutionException, InterruptedException {
    switch (transition) {
      case DS_WAIT_PRACK | DS_ST_IN_PROVISIONAL:
        // We are in reliable state, should not send any 100, or non-reliable 1xx
        cancelT1();
        m_stateTable.throwException(transition);
        break;
      case DS_INITIAL | DS_ST_IN_REL_PROVISIONAL:
      case DS_CALLING | DS_ST_IN_REL_PROVISIONAL:
      case DS_PROCEEDING | DS_ST_IN_REL_PROVISIONAL:
        // The second case could happen, if UAS sent 100 Trying first,
        // which will get us to DS_PROCEEDING state; then, UAS sends
        // reliable 180/183 provisional response
      case DS_STI_RELPROCEEDING | DS_ST_IN_REL_PROVISIONAL:
        // This is new 1xx response after the first 1xx was PRACK'ed
        m_connection.getResponseConnection();
        initializeTimers();

        sendCurrentResponse();

        // Set timer for all transports
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
        }
        m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);

        break;
      case DS_WAIT_PRACK | DS_ST_IN_REL_PROVISIONAL:
        // If it's a new 1xx (before the previous 1xx was PRACK'ed),
        // "The UAS MUST NOT send a second reliable provisional response until
        // the first is acknowledged." - let it throw exception for this case.
        cancelT1();
        throw new DsException(
            "The UAS MUST NOT send a second reliable provisional response until the previous 1xx is acknowledged.");
      case DS_WAIT_PRACK | DS_ST_IN_T1:
        if ((m_100relSupport != UNSUPPORTED)
            && (m_sipRequest != null)
            && (m_sipRequest.headerContainsTag(REQUIRE, BS_100REL)
                || m_sipRequest.headerContainsTag(SUPPORTED, BS_100REL))) {
          // Retransmit 1xx reliably, only when the INVITE requested.
          if (m_TCounter++ < m_sipTimers.INVITE_SERVER_TRANS_RETRY) {
            backOff();

            if (!DsSipTransportType.intern(m_connection.getTransport()).isReliable()) {
              sendCurrentResponse(); // Retransmit the 1xx
            }

            // Set timer for all transports
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
            }
            m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);
          } else {
            // If a reliable provisional response is retransmitted
            // for 64*T1 seconds without reception of a
            // corresponding PRACK, the UAS SHOULD reject the
            // original request with a 5xx response.
            byte[] responseBytes =
                DsSipResponse.createResponseBytes(
                    DsSipResponseCode.DS_RESPONSE_GATEWAY_TIMEOUT, m_sipRequest, null, null);
            sendResponse(responseBytes, DsSipResponseCode.DS_RESPONSE_GATEWAY_TIMEOUT);
            execute(DS_ST_IN_T1_EXPIRED);
          }
        }
        break;
      case DS_WAIT_PRACK | DS_ST_IN_CANCEL:
        // no need to collect stat for this cancel. TM already did it.
        if (!m_isProxyServerMode) {
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
      case DS_WAIT_PRACK | DS_ST_IN_IO_EXCEPTION:
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
   * The reliableProceeding state machine method.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void reliableProceeding(int transition)
      throws DsException, IOException, ExecutionException, InterruptedException {
    switch (transition) {
      case DS_WAIT_PRACK | DS_ST_IN_PRACK:
        if (m_prackTransaction != null) {
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_PRACK,
                  m_prackMessage,
                  m_prackTransaction,
                  m_serverInterface)
              .call();
        } else {
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_PRACK, m_prackMessage, m_serverInterface)
              .call();
        }
        m_prackMessage = null;
        // "Retransmissions of the reliable provisional response
        // cease when a matching PRACK is received by the UAS core"
        cancelT1();

        break;
      case DS_STI_RELPROCEEDING | DS_ST_IN_CANCEL:
        // no need to collect stat for this cancel. TM already did it.
        if (!m_isProxyServerMode) {
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
      case DS_STI_RELPROCEEDING | DS_ST_IN_PRACK:
        break;
      case DS_STI_RELPROCEEDING | DS_ST_IN_T1:
        if ((m_100relSupport != UNSUPPORTED)
            && (m_sipRequest != null)
            && (m_sipRequest.headerContainsTag(REQUIRE, BS_100REL)
                || m_sipRequest.headerContainsTag(SUPPORTED, BS_100REL))) {
          if (m_TCounter++ < m_sipTimers.INVITE_SERVER_TRANS_RETRY) {
            backOff();

            if (!DsSipTransportType.intern(m_connection.getTransport()).isReliable()) {
              sendCurrentResponse();
            }

            // Set timer for all transports
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
            }
            m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);
          } else {
            byte[] responseBytes =
                DsSipResponse.createResponseBytes(
                    DsSipResponseCode.DS_RESPONSE_GATEWAY_TIMEOUT, m_sipRequest, null, null);
            sendResponse(responseBytes, DsSipResponseCode.DS_RESPONSE_GATEWAY_TIMEOUT);
            execute(DS_ST_IN_T1_EXPIRED);
          }
        }
        break;
      case DS_STI_RELPROCEEDING | DS_ST_IN_NEXT_CLIENT:
        sendCurrentResponse();
      case DS_STI_RELPROCEEDING | DS_ST_IN_IO_EXCEPTION:
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

  /*
   * javadoc inherited
   */
  protected void completed(int transition)
      throws DsException, IOException, ExecutionException, InterruptedException {
    // maivu - 11.01.06 - CSCsg22401 Transition to Completed state so cancel the request expiration
    // timer
    // which was set in the DsSipServerTransactionImpl constructor
    cancelExpirationTimer();

    switch (transition) {
      case DS_INITIAL | DS_ST_IN_3TO6XX:
        // completed state is equivalent to final state in old bis
        // fall through
      case DS_CALLING | DS_ST_IN_3TO6XX:
        m_connection.getResponseConnection();
        initializeTimers();
        // fall through
      case DS_PROCEEDING | DS_ST_IN_3TO6XX:
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case DS_WAIT_PRACK | DS_ST_IN_3TO6XX:
      case DS_STI_RELPROCEEDING | DS_ST_IN_3TO6XX:
        sendCurrentResponse();

        // Set timer for all transports
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
        }
        m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);

        break;
      case DS_COMPLETED | DS_ST_IN_REQUEST:
        // no need to collect stat for the request. TM already did it.
        sendCurrentResponse();
        break;
      case DS_COMPLETED | DS_ST_IN_3TO6XX:
        // DG - should possibly be an exception all of the time?
        if (!m_isProxyServerMode) {
          m_stateTable.throwException(transition);
        }
        break;
      case DS_COMPLETED | DS_ST_IN_CANCEL:
        // no need to collect stat for the cancel. TM already did it.
        // is this right - or should we just ignore it like in the parent class?
        // look up in spec
        new ServerTransactionCallback(
                ServerTransactionCallback.CB_CANCEL, m_cancelMessage, m_serverInterface)
            .call();
        m_cancelMessage = null;
        break;
      case DS_COMPLETED | DS_ST_IN_T1:
        if (m_TCounter++ < m_sipTimers.INVITE_SERVER_TRANS_RETRY) {
          backOff();

          if (!DsSipTransportType.intern(m_connection.getTransport()).isReliable()) {
            sendCurrentResponse();
          }

          // Set timer for all transports
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
          }
          m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);
        } else {
          execute(DS_ST_IN_T1_EXPIRED);
        }
        break;
      case DS_COMPLETED | DS_ST_IN_NEXT_CLIENT:
        // m_connection.getResponseConnection();
        sendCurrentResponse();

        // Set timer for all transports
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
        }
        m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);

        break;
      case DS_COMPLETED | DS_ST_IN_IO_EXCEPTION:
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
   * This is the confirmed state machine method. CONFIRMED state is added in bis05
   *
   * @param transition transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void confirmed(int transition) throws DsException, IOException {
    switch (transition) {
      case DS_COMPLETED | DS_ST_IN_ACK:
        // it is OK to null our reference to the user code if the ACK message
        //   has a Route
        boolean null_callback = (m_ackMessage.getHeader(DsSipConstants.ROUTE) != null);

        new ServerTransactionCallback(
                ServerTransactionCallback.CB_ACK, m_ackMessage, m_serverInterface)
            .call();
        nullRefs(null_callback);

        if (m_To == 0) // directly go to TERMINATED without using timer
        {
          execute(DS_ST_IN_TIMEOUT);
        } else {
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
          }
          DsTimer.schedule(m_To, this, IN_TIMEOUT);
        }
        cancelTn(); // transaction will terminate normally
        break;
      case DS_CONFIRMED | DS_ST_IN_REQUEST:
        // no need to collect stat for the request. TM already did it.
        break;
      case DS_CONFIRMED | DS_ST_IN_CANCEL:
        // no need to collect stat for the cancel. TM already did it.
        break;
      case DS_CONFIRMED | DS_ST_IN_T1:
        break;
      case DS_CONFIRMED | DS_ST_IN_ACK:
        m_ackMessage = null;
        break;
      case DS_CONFIRMED | DS_ST_IN_IO_EXCEPTION:
        break;
      default:
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
  protected void terminated(int transition)
      throws DsException, IOException, ExecutionException, InterruptedException {
    // maivu - 11.01.06 - CSCsg22401 Transition to Terminated state so cancel the request expiration
    // timer
    // which was set in the DsSipServerTransactionImpl constructor
    cancelExpirationTimer();

    // can't ignore all input when already in terminated state
    if ((transition & DS_MASK) == DS_TERMINATED) {
      switch (transition & DS_INPUT_MASK) {
        case DS_ST_IN_NO_CLIENT:
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_EXCEPTION, null, m_serverInterface)
              .call();
          // fall through
        case DS_ST_IN_TIMEOUT:
          cancelTn();
          cleanup();
          break;
        case DS_ST_IN_IO_EXCEPTION:
          if (m_connection.getNextConnection()) {
            execute(DS_ST_IN_NEXT_CLIENT);
          } else {
            execute(DS_ST_IN_NO_CLIENT);
          }

          break;
        case DS_ST_IN_NEXT_CLIENT:
          send2XXResponse(transition);
          break;
        default: // should already be cleanedup and be ignored
          break;
      }
      return;
    }

    switch (transition & DS_INPUT_MASK) {
      case DS_ST_IN_NO_CLIENT:
        new ServerTransactionCallback(
                ServerTransactionCallback.CB_EXCEPTION, null, m_serverInterface)
            .call();
        // fall through
      case DS_ST_IN_TIMEOUT: // can be combined with "default"
        cancelTn();
        cleanup();
        break;
      case (DS_ST_IN_T1_EXPIRED):
        cancelTn();
        // fall through
      case (DS_ST_IN_Tn):
        new ServerTransactionCallback(ServerTransactionCallback.CB_TIMEOUT, null, m_serverInterface)
            .call();
        cleanup();
        break;
      case (DS_ST_IN_2XX):
        send2XXResponse(transition);
        break;
      default:
        cancelTn();
        cleanup();
        break;
    }
  }

  private void send2XXResponse(int transition)
      throws DsException, IOException, ExecutionException, InterruptedException {
    // terminated state is equivalent to final in old bis for invite 2XX

    // switch statement style is being ruined here
    // please fix me and do not propogate this style
    if ((transition & DS_MASK) < DS_PROCEEDING) {
      m_connection.getResponseConnection();
      initializeTimers();
    }
    if (m_isProxyServerMode && !m_x200Terminated) // terminate now after sending 2XX
    {
      cancelTn();
      try {
        DsSipTransactionManager.removeTransaction(this);
      } catch (DsException dse) {
        if (genCat.isEnabled(Level.WARN))
          genCat.warn("terminated(): failed to remove transaction", dse);
      }
      // both proxy server itself and UA must do failover. UA alone doing it is not enough.
      // Will implement it together with proxy
      sendCurrentResponse();
      nullRefs(true);
      releaseConnections();
    } else // non-proxy || x200 extended transaction
    {
      DsSipTransactionManager.addToDialogMap(this);
      // do response failover here
      boolean failed = false;
      boolean hasNext = true;
      do {
        try {
          sendCurrentResponse();
          failed = false;
        } catch (IOException ioe) {
          failed = true;
        }
      } while (failed && (hasNext = m_connection.getNextConnection()));

      if (!hasNext) {
        if (genCat.isEnabled(Level.WARN))
          genCat.warn("terminated(): failed to send response after trying response failover");
        cancelTn();
        cleanup();
        return;
      }

      // use STIX table for endpoint 2XX
      // CAFFEINE 2.0 DEVELOPMENT - Moved to new class DsSipStateMachineTransitions
      m_stateTable.setStateTable(DsSipStateMachineTransitions.STIX_TRANSITIONS);
      m_stateTable.m_curState = DS_XCOMPLETED;

      if (!m_isProxyServerMode) {
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
        }
        m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);
      } else {
        cancelTn();
        if (cat.isEnabled(Level.DEBUG)) {
          debugTraceTimer(true, "m_sipTimers.TU1Value", "IN_Tn", m_sipTimers.TU1Value);
        }
        m_TimerTaskTn = DsTimer.schedule(m_sipTimers.TU1Value, this, IN_Tn);
      }
    }
  }

  /**
   * The xcompleted state machine method. This method is used only by 2XX server INVITE
   * transactions.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void xcompleted(int transition)
      throws DsException, IOException, ExecutionException, InterruptedException {
    if (m_isProxyServerMode) {
      switch (transition) {
        case DS_XCOMPLETED | DS_ST_IN_2XX:
          sendCurrentResponse();
          break;
        default:
          m_stateTable.throwException(transition);
      }
    } else {
      switch (transition) {
        case DS_XCOMPLETED | DS_ST_IN_REQUEST:
          // no need to collect stat for the request. TM already did it.
          sendCurrentResponse();
          break;
        case DS_XCOMPLETED | DS_ST_IN_2XX:
          // endpoint can not send another 2XX
          m_stateTable.throwException(transition);
          break;
        case DS_XCOMPLETED | DS_ST_IN_CANCEL:
          // no need to collect stat for the request. TM already did it.
          // is this right - or should we just ignore it like in the parent class?
          // look up in spec
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_CANCEL, m_cancelMessage, m_serverInterface)
              .call();
          m_cancelMessage = null;
          break;
        case DS_XCOMPLETED | DS_ST_IN_T1:
          if (m_TCounter++ < m_sipTimers.INVITE_SERVER_TRANS_RETRY) {
            backOff();

            if (!DsSipTransportType.intern(m_connection.getTransport()).isReliable()) {
              sendCurrentResponse();
            }

            // Set timer for all transports
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
            }
            m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);
          } else {
            execute(DS_ST_IN_T1_EXPIRED);
          }
          break;
        case DS_XCOMPLETED | DS_ST_IN_NEXT_CLIENT:
          // m_connection.getResponseConnection();
          sendCurrentResponse();
          // Set timer for all transports
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(false, "m_T1", "IN_T1", m_T1);
          }
          m_TimerTaskT1 = DsTimer.schedule(m_T1, this, IN_T1);

          break;
        case DS_XCOMPLETED | DS_ST_IN_IO_EXCEPTION:
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
  }

  /**
   * The xconfirmed state machine method. This method is used only by 2XX server INVITE
   * transactions.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void xconfirmed(int transition) throws DsException, IOException {
    if (m_isProxyServerMode) {
      switch (transition) {
        case DS_XCONFIRMED | DS_ST_IN_REQUEST:
          // no need to collect stat for the request. TM already did it.
          break;
        case DS_XCONFIRMED | DS_ST_IN_2XX:
          sendCurrentResponse();
          break;
        case DS_XCOMPLETED | DS_ST_IN_ACK:
          // it is OK to null our reference to the user code if the ACK message
          //   has a Route
          boolean null_callback = (m_ackMessage.getHeader(DsSipConstants.ROUTE) != null);
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_ACK, m_ackMessage, m_serverInterface)
              .call();
          nullRefs(null_callback);
          break;
        case DS_XCONFIRMED | DS_ST_IN_ACK:
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_ACK, m_ackMessage, m_serverInterface)
              .call();
          m_ackMessage = null;
          m_sipResponse = null;
          break;
        default:
          m_stateTable.throwException(transition);
          break;
      }
    } else {
      switch (transition) {
        case DS_XCOMPLETED | DS_ST_IN_ACK:
          // it is OK to null our reference to the user code if the ACK message
          //   has a Route
          boolean null_callback = (m_ackMessage.getHeader(DsSipConstants.ROUTE) != null);

          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_ACK, m_ackMessage, m_serverInterface)
              .call();
          nullRefs(null_callback);

          if (m_To == 0) // go directly to XTERMINATED without using timer
          {
            execute(DS_ST_IN_TIMEOUT);
          } else {
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
            }
            DsTimer.schedule(m_To, this, IN_TIMEOUT);
          }
          cancelTn(); // transaction will terminate normally
          break;
        case DS_XCONFIRMED | DS_ST_IN_REQUEST:
          // no need to collect stat for the request. TM already did it.
          break;
        case DS_XCONFIRMED | DS_ST_IN_CANCEL:
          // no need to collect stat for the request. TM already did it.
          break;
        case DS_XCONFIRMED | DS_ST_IN_T1:
          break;
        case DS_XCONFIRMED | DS_ST_IN_ACK:
          m_ackMessage = null;
          break;
        case DS_XCONFIRMED | DS_ST_IN_IO_EXCEPTION:
          break;
        default:
          m_stateTable.throwException(transition);
          break;
      }
    }
  }

  /**
   * The xterminated state machine method. This method is used only by 2XX server INVITE
   * transactions.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void xterminated(int transition) throws DsException, IOException {
    // ignore any input if already in terminated state
    if ((transition & DS_MASK) == DS_XTERMINATED) return;

    if (!m_isProxyServerMode) {
      switch (transition & DS_INPUT_MASK) {
        case DS_ST_IN_T1_EXPIRED:
          cancelTn();
          // fall through
        case DS_ST_IN_Tn:
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_TIMEOUT, null, m_serverInterface)
              .call();
          break;
        case DS_ST_IN_NO_CLIENT:
          new ServerTransactionCallback(
                  ServerTransactionCallback.CB_EXCEPTION, null, m_serverInterface)
              .call();
          // fall through
        case DS_ST_IN_TIMEOUT:
          // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
        case DS_ST_IN_T1:
          // fall through
        default:
          cancelTn();
          break;
      }
    }
    cleanup();
  }

  final void cancelT1() {
    if (m_TimerTaskT1 != null) {
      m_TimerTaskT1.cancel(false);
      m_TimerTaskT1 = null;
    }
  }

  /** Cleanup this server transaction. */
  private void cleanup() {
    nullRefs(true);
    releaseConnections();
    try {
      DsSipTransactionManager.removeTransaction(this);
      // it is possible that this txn is not in the dialog map which is OK
      if (m_dialogID != null) {
        DsSipTransactionManager.removeFromDialogMap(m_dialogID);
      }
    } catch (DsException dse) {
      if (genCat.isEnabled(Level.WARN))
        genCat.warn("terminated(): failed to remove transaction", dse);
    }
  }

  /*
   * javadoc inherited
   */
  protected synchronized void onAck(DsSipAckMessage request) {
    if (m_ackMessage == null) {
      m_ackMessage = request;
    }

    // all ACKs cancel final response retransmissions - jsm
    cancelT1();

    try {
      execute(DS_ST_IN_ACK);
    } catch (DsStateMachineException sme) {
      if (genCat.isEnabled(Level.WARN))
        genCat.warn("onAck(): state machine exception processing ACK", sme);
    }
  }

  /** Increase the T1 timer by 2 times, capping at T2. */
  protected void backOff() {
    if (m_T1 < m_T2) {
      m_T1 *= 2;
    } else {
      m_T1 = m_T2;
    }
  }

  DsSipDialogID getDialogID() throws DsException {
    if (m_dialogID == null) {
      if (m_sipResponse == null) {
        throw new DsException("can't create dialog ID from a null response");
      }

      // do not add the to tag in for JAIN
      if (!m_jainCompatability) {
        DsSipToHeader to = m_sipResponse.getToHeaderValidate();
        if (to.getTag() == null) {
          if (TO_TAG == null) {
            TO_TAG = DsSipTag.generateTag();
          }
          to.setTag(TO_TAG);
        }
      }
      m_dialogID = DsSipTransactionManager.constructDialogID(m_sipResponse);
    }
    return m_dialogID;
  }
}
