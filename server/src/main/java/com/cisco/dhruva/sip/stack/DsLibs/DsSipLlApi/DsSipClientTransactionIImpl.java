// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTrackingException.TrackingExceptions;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.cac.SIPSession;
import com.cisco.dhruva.util.cac.SIPSessionID;
import com.cisco.dhruva.util.cac.SIPSessions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Implements the client side of the low level INVITE state machine.
 *
 * @see DsSipClientTransactionIImpl
 * @see DsSipClientTransactionImpl
 */
public class DsSipClientTransactionIImpl extends DsSipClientTransactionImpl {
  ///////////////////////////////////////////////////////////////////////////
  //////////// statics  /////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  /** The ACK message. */
  protected DsSipAckMessage m_ackMessage;
  /** The ACK message bytes. */
  protected byte[] m_ackBytes;

  private boolean firstAckToNon2XX = true;
  private boolean firstAckTo2XX = true;
  private boolean AckNon2xxInTerminated = false;
  // protected int m_Tp = DsSipStateTable.defaultTcValue;

  /**
   * Constructor for a client transaction.
   *
   * @param request request to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param transactionParams Optional. Reserved for future use.
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipClientTransactionIImpl(
      DsSipRequest request,
      DsSipClientTransactionInterface clientInterface,
      DsSipTransactionParams transactionParams)
      throws DsException {
    super(request, (DsSipClientTransportInfo) null, clientInterface);
  }

  /**
   * Constructor for a client transaction.
   *
   * @param request request to be sent to server.
   * @param clientInterface Optional callback interface to user-level callbacks.
   * @param clientTransportInfo If the client wishes to use transport information other than that
   *     held by transport layer, DsSipClientTransportInfo is implemented and passed to this
   *     constructor
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected DsSipClientTransactionIImpl(
      DsSipRequest request,
      DsSipClientTransportInfo clientTransportInfo,
      DsSipClientTransactionInterface clientInterface)
      throws DsException {
    super(request, clientTransportInfo, clientInterface);
  }

  /*
   * javadoc inherited
   */
  protected void createStateTable() {
    // CAFFEINE 2.0 DEVELOPMENT - moved to new class DsSipStateMachineTransitions.
    m_stateTable = new DsSipStateTable(DsSipStateMachineTransitions.CTI_TRANSITIONS);
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  ///      DsSipTransaction methods                  //////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /*
   *  javadoc inherited
   */
  public boolean isInvite() {
    return true;
  }

  /*
   *  javadoc inherited
   */
  protected void nullRefs() {
    super.nullRefs();
    DsSipAckMessage ack = m_ackMessage;
    m_ackMessage = null;
    if (m_isProxyServerMode && (m_responseClass == DsSipResponseCode.DS_SUCCESS)) {
      // in this case the ACK will be re-delivered by the proxy code
      m_ackBytes = null;
    } else {
      // If the ack is compressed, we will need to recompress.  Also, in
      // the case when the ack is compressed, the m_ackBytes should
      // always be null to ensure that we recompress.
      if (ack != null && ack.shouldCompress()) {
        m_ackMessage = ack;
        m_ackBytes = null;
      }
    }
  }

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
            // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
          case DS_CTI_RELPROCEEDING:
            reliableProceeding(transition);
            break;

            /* we start by switch from xinitial state to xcompleted, so
            no need to handle DS_XINITIAL */
          case DS_XCOMPLETED:
            xcompleted(transition);
            break;
          case DS_XTERMINATED:
            xterminated(transition);
            break;
          default:
            break;
        }
      } catch (DsStateMachineException dse) {
        // CAFFEINE 2.0 DEVELOPMENT - Adding more debug
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionIImpl.execute - DSE", dse);
        }
        throw dse;
      } catch (IOException exc) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionIImpl.execute", exc);
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
          genCat.warn("DsSipClientTransactionIImpl.execute", dse);
        }
        execute(DS_CT_IN_OTHER_EXCEPTION);
      } catch (Exception exc) {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn("DsSipClientTransactionIImpl.execute", exc);
        }
        execute(DS_CT_IN_OTHER_EXCEPTION);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  ///      DsSipClientTransaction methods            //////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  /*
   *  javadoc inherited
   */
  public synchronized void ack(DsSipAckMessage request)
      throws DsException, IOException, UnknownHostException {
    if (m_ackMessage == null) {
      m_ackMessage = request;
    }

    if (m_ackMessage != null) {
      DsNetwork network = m_ackMessage.getBindingInfo().getNetworkReliably();
      // make sure that the local binding info is set for NAT
      if (network.isBehindNAT()) {
        m_ackMessage.setBindingInfo((DsBindingInfo) m_sipRequest.getBindingInfo().clone());
      }
    }
    // if it is null, the local binding info will get set when setAckMessage is called.

    execute(DS_CT_IN_ACK);
  }

  private void setAckMessage() {
    m_ackMessage = new DsSipAckMessage(m_sipResponse, null, null);
    DsNetwork network = m_ackMessage.getBindingInfo().getNetworkReliably();
    // make sure that the local binding info is set for NAT
    if (network.isBehindNAT()) {
      m_ackMessage.setBindingInfo((DsBindingInfo) m_sipRequest.getBindingInfo().clone());
    }
  }

  /*
   *  javadoc inherited
   */
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  public synchronized DsSipClientTransaction prack(DsSipPRACKMessage request)
      throws DsException, IOException {
    if (m_100relSupport == UNSUPPORTED) {
      throw new DsException("100rel unsupported on UAC side and PRACK is not allowed.");
    }
    if (m_sipRelProvisionalResponse != null) {
      if (!m_sipRelProvisionalResponse.headerContainsTag(REQUIRE, BS_100REL)) {
        throw new DsException("100rel unsupported on UAS side and PRACK is not allowed.");
      }
    } else {
      throw new DsException(
          "could not send PRACK message as reliable provisional response is null");
    }

    if (request == null) {
      m_prackMessage = new DsSipPRACKMessage(m_sipRelProvisionalResponse);
    } else {
      m_prackMessage = request;
    }
    // add route headers if they are present in the request so that they will
    //   go through the same path.
    // TBD: The logic here should be re-examined when fixing CSCed39231
    // Comment out for now.
    // m_prackMessage.removeHeaders(ROUTE);
    // m_prackMessage.addHeaders(m_initialRouteHeader);

    execute(DS_CT_IN_PRACK);

    DsSipClientTransaction ret = m_prackTransaction;
    m_prackTransaction = null;
    return ret;
  }

  /*
   *  javadoc inherited
   */
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  public synchronized DsSipClientTransaction prack(
      DsSipClientTransactionInterface prackInterface, DsSipPRACKMessage request)
      throws DsException, IOException {
    m_prackInterface = prackInterface;
    return (prack(request));
  }

  /*
   *  javadoc inherited
   */
  protected DsSipClientTransaction createCopy(DsSipResponse response) throws DsException {
    DsSipClientTransactionIImpl copy =
        (DsSipClientTransactionIImpl)
            DsSipTransactionManager.getTransactionManager()
                .createClientTransaction(
                    (DsSipRequest) getRequest().clone(),
                    null, // DsSipClientTransportInfo
                    m_clientInterface);

    // DsSipClientTransactionIImpl copy = new
    // DsSipClientTransactionIImpl((DsSipRequest)getRequest().clone(),
    //                                                                    m_clientInterface,
    //                                                                    null);

    copy.m_toTag = response.getKey().getToTag();
    copy.m_stateTable.m_curState = DS_COMPLETED;
    copy.m_sipResponse = response;
    copy.m_key = copy.m_sipResponse.getKey();
    copy.m_sipResponse.setKeyContext(DsSipTransactionKey.USE_TO_TAG);
    copy.m_responseClass = (byte) response.getResponseClass();
    copy.createConnection(this);
    return copy;
  }

  /**
   * Create a connection wrapper from the connection wrapper of the provided transaction.
   *
   * @param other the client transaction from which to copy the connection wrapper.
   */
  private void createConnection(DsSipClientTransactionIImpl other) {
    m_connection = new ConnectionWrapper(other.m_connection);
  }

  /*
   *  javadoc inherited
   */
  protected synchronized void onMultipleFinalResponse(
      DsSipClientTransaction originalTransaction, DsSipResponse response) {
    new ClientTransactionCallback(originalTransaction, response, m_clientInterface).call();
  }

  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////
  ///       State Machine implemenation              //////////////////
  /////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////

  /*
   *  javadoc inherited
   */
  protected void proceeding(int transition) throws DsException, IOException {
    switch (transition) {
      case DS_CALLING | DS_CT_IN_PROVISIONAL:
        boolean rtx = findAndUpdateRetransmission();
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_PROVISIONAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        // Tp no longer used in bis 7
        // DsDiscreteTimerMgr.scheduleNoQ(m_Tp, new TimeoutEvent(this), IN_Tp);
        break;
      case DS_PROCEEDING | DS_CT_IN_T1:
        break;
      default:
        super.proceeding(transition);
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
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  protected void reliableProceeding(int transition) throws DsException, IOException {
    switch (transition) {
      case DS_CTI_RELPROCEEDING | DS_CT_IN_PROVISIONAL:
        // We are in RELPROCEEDING state, should not receive 100 or non-reliable 1xx
        // Warn and ignore it. (This won't happen when UAS is using the Cisco Java SIP Stack).
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn(
              "reliableProceeding(): should not receive 100 or non-reliable 1xx while 100rel was requested");
        }
        break;
      case DS_CALLING | DS_CT_IN_REL_PROVISIONAL:
      case DS_PROCEEDING | DS_CT_IN_REL_PROVISIONAL:
      case DS_CTI_RELPROCEEDING | DS_CT_IN_REL_PROVISIONAL:
        boolean rtx = findAndUpdateRetransmission();
        // PRACK: Per the RFC3262, "A UAC SHOULD NOT retransmit
        // the PRACK request when it receives a
        // retransmitssion of the provisional response being acknologed"
        // Also per RFC3262, "Once a reliable response is received,
        // the retransmissions of that
        // response MUST be discarded"
        // But, will user want to see this 1xx retransmission?
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_REL_PROVISIONAL_RESPONSE,
                m_sipRelProvisionalResponse,
                m_clientInterface)
            .call();
        break;
      case DS_CTI_RELPROCEEDING | DS_CT_IN_T1:
        if (m_TCounter++ >= m_maxT1Timeouts) {
          execute(DS_CT_IN_T1_EXPIRED);
        }
        break;
      case DS_CTI_RELPROCEEDING | DS_CT_IN_PRACK:
        if (m_prackTransaction == null) {
          createPrackTransaction(transition, m_prackMessage);
        } else {
          if (genCat.isEnabled(Level.WARN)) {
            genCat.warn("reliableProceeding(): null PRACK transaction was encountered.");
          }
        }
        break;
      case DS_CTI_RELPROCEEDING | DS_CT_IN_CANCEL:
        if (m_cancelTransaction == null) {
          createCancelTransaction(transition);
        }
        break;
      case DS_CTI_RELPROCEEDING | DS_CT_IN_2XX:
        boolean rtx2 = findAndUpdateRetransmission();
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();

        cancelTn();
        break;
      default:
        m_stateTable.throwException(transition);
        break;
    }
  }

  private void sendAck() throws IOException, DsException {
    if (m_ackBytes != null) {
      m_connection.send(m_ackBytes);
    } else if (m_ackMessage != null) {

      m_ackBytes = m_connection.send(m_ackMessage);
      // This ensures that we use DsSipSigcompConnection.send(DsSipMessage)
      // where the logic lives to compress the SIP message.  Retransmitted
      // messages must be recompressed.
      if (m_ackMessage.shouldCompress()) {
        m_ackBytes = null;
      }
    } else {
      throw new DsException("Tried to send a null ACK?");
    }
  }

  /*
   *  javadoc inherited
   */
  protected void completed(int transition) throws DsException, IOException {
    // maivu - 11.01.06 - CSCsg22401 Transition to Completed state so cancel the request expiration
    // timer
    // which was set in the DsSipClientTransactionImpl.start()
    cancelExpirationTimer();

    boolean rtx = false;
    int reason = DsMessageLoggingInterface.REASON_REGULAR;
    switch (transition) {
      case DS_CALLING | DS_CT_IN_3TO6XX:
      case DS_PROCEEDING | DS_CT_IN_3TO6XX:
        rtx = findAndUpdateRetransmission();
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        if (m_To == 0) // reliable transport
        {
          AckNon2xxInTerminated = true;
          execute(DS_CT_IN_TIMEOUT);
        } else {
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
          }
          DsDiscreteTimerMgr.scheduleNoQ(m_To, this, IN_TIMEOUT);
        }
        cancelTn(); // transaction will terminate normally
        break;
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case DS_CTI_RELPROCEEDING | DS_CT_IN_3TO6XX:
        rtx = findAndUpdateRetransmission();
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        if (m_To == 0) // reliable transport
        {
          AckNon2xxInTerminated = true;
          execute(DS_CT_IN_TIMEOUT);
        } else {
          if (cat.isEnabled(Level.DEBUG)) {
            debugTraceTimer(true, "m_To", "IN_TIMEOUT", m_To);
          }
          DsDiscreteTimerMgr.scheduleNoQ(m_To, this, IN_TIMEOUT);
        }
        cancelTn(); // transaction will terminate normally
        break;
      case DS_COMPLETED | DS_CT_IN_ACK:
        if (firstAckToNon2XX) {
          if (m_ackMessage == null) {
            setAckMessage();
            reason = DsMessageLoggingInterface.REASON_GENERATED;
          }
          getAckConnection();
          sendAck();
          nullRefs();
          firstAckToNon2XX = false;
        } else if (!m_isProxyServerMode) {
          m_stateTable.throwException(transition);
        }
        break;
      case DS_COMPLETED | DS_CT_IN_T1:
      case DS_COMPLETED | DS_CT_IN_CANCEL:
      case DS_COMPLETED | DS_CT_IN_CANCEL_TIMER:
        break;
      case DS_COMPLETED | DS_CT_IN_PROVISIONAL:
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case DS_COMPLETED | DS_CT_IN_REL_PROVISIONAL:
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

      case DS_COMPLETED | DS_CT_IN_3TO6XX:
        if (!firstAckToNon2XX) // already acked
        {
          sendAck();
        }
        rtx = findAndUpdateRetransmission();
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
  protected void terminated(int transition) throws DsException, IOException {
    // maivu - 11.01.06 - CSCsg22401 Transition to Terminated state so cancel the request expiration
    // timer
    // which is set in the DsSipClientTransactionImpl.start()
    cancelExpirationTimer();

    switch (transition) {
      case DS_TERMINATED | DS_CT_IN_CANCEL:
        createCancelTransaction(transition);
        break;
      case DS_CALLING | DS_CT_IN_2XX:
      case DS_PROCEEDING | DS_CT_IN_2XX:
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case DS_CTI_RELPROCEEDING | DS_CT_IN_2XX:
        boolean rtx = findAndUpdateRetransmission();
        new ClientTransactionCallback(
                ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
            .call();
        if (m_isProxyServerMode && !m_x200Terminated) {
          // proxy does not generate ACK, so get rid of this trans
          cancelTn();
          cleanup();
        } else {
          // use CTIX state table
          // CAFFEINE 2.0 DEVELOPMENT - moved to new class DsSipStateMachineTransitions.
          m_stateTable.setStateTable(DsSipStateMachineTransitions.CTIX_TRANSITIONS);
          m_stateTable.m_curState = DS_XCOMPLETED;
          if (!m_isProxyServerMode) {
            // UAC core considers trans complete 64*T1 after first 2XX
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(
                  true, "64 * DsSipTimers.T1Value", "IN_TIMEOUT", 64 * m_sipTimers.T1Value);
            }
            DsDiscreteTimerMgr.scheduleNoQ(64 * m_sipTimers.T1Value, this, IN_TIMEOUT);

            cancelTn(); // transaction will terminate normally
          } else {
            cancelTn(); // transaction will terminate normally
            if (cat.isEnabled(Level.DEBUG)) {
              debugTraceTimer(true, "m_sipTimers.TU1Value", "IN_Tn", m_sipTimers.TU1Value);
            }
            m_TimerTaskTn = DsDiscreteTimerMgr.scheduleNoQ(m_sipTimers.TU1Value, this, IN_Tn);
          }
        }
        break;
      case DS_COMPLETED | DS_CT_IN_TIMEOUT:
        // if it is reliable transport, upon 300-6xx response, timeout switched
        // us to this terminated state, still need to do ACK. so do not cleanup
        // when AckNon2xxInTerminated is true
        if (!AckNon2xxInTerminated) {
          cancelTn();
          cleanup();
        }
        break;

      case DS_TERMINATED | DS_CT_IN_ACK:
        // if it is a reliable transport, upon 300-6xx response, timeout switched
        // us to this terminated state, need to do ACK here.
        if (AckNon2xxInTerminated) {
          cancelTn();
          int reason = DsMessageLoggingInterface.REASON_REGULAR;
          if (m_ackMessage == null) {
            setAckMessage();
            reason = DsMessageLoggingInterface.REASON_GENERATED;
          }
          getAckConnection();
          sendAck();
          SIPSession sipSession = SIPSessions.getActiveSession(m_ackMessage.getCallId().toString());
          sipSession.sessionAttrib.setRemoteUuid(SIPSessionID.getNillsessionid());
          AckNon2xxInTerminated = false;
          cleanup();
        } else // if (!m_isProxyServerMode)
        {
          m_stateTable.throwException(transition);
        }
        break;
      default:
        /*
         * if we have come from any state but the terminated state,
         *   then cleanup, else do nothing
         */
        switch (transition & DS_INPUT_MASK) {
          case (DS_CT_IN_NO_SERVER):
            cancelTn();
            new ClientTransactionCallback(
                    ClientTransactionCallback.CB_EXCEPTION, null, m_clientInterface)
                .call();
            break;
          case (DS_CT_IN_T1_EXPIRED):
            // case (DS_CT_IN_Tp): //not used in bis 07
          case (DS_CT_IN_CANCEL_TIMER):
            cancelTn();
          case (DS_CT_IN_Tn):
            new ClientTransactionCallback(
                    ClientTransactionCallback.CB_TIMEOUT, null, m_clientInterface)
                .call();
            break;
        }
        cleanup();

        break;
    }
  }

  /**
   * The xcompleted state machine method. This method is used only by 2XX Client INVITE
   * transactions.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void xcompleted(int transition) throws DsException, IOException {
    boolean rtx = false;
    int reason = DsMessageLoggingInterface.REASON_REGULAR;
    if (m_isProxyServerMode) {
      switch (transition) {
        case DS_XCOMPLETED | DS_CT_IN_T1:
        case DS_XCOMPLETED | DS_CT_IN_Tp:
        case DS_XCOMPLETED | DS_CT_IN_CANCEL:
        case DS_XCOMPLETED | DS_CT_IN_CANCEL_TIMER:
          break;
        case DS_XCOMPLETED | DS_CT_IN_ACK:
          if (m_ackMessage == null) {
            setAckMessage();
            reason = DsMessageLoggingInterface.REASON_GENERATED;
          }
          getAckConnection();
          sendAck();
          nullRefs();
          break;
        case DS_XCOMPLETED | DS_CT_IN_2XX:
          rtx = findAndUpdateRetransmission();
          new ClientTransactionCallback(
                  ClientTransactionCallback.CB_FINAL_RESPONSE, m_sipResponse, m_clientInterface)
              .call();
          break;
        case DS_XCOMPLETED | DS_CT_IN_PROVISIONAL:
          // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
        case DS_XCOMPLETED | DS_CT_IN_REL_PROVISIONAL:
          rtx = findAndUpdateRetransmission();
          break;
      }
    } else {
      switch (transition) {
        case DS_XCOMPLETED | DS_CT_IN_T1:
        case DS_XCOMPLETED | DS_CT_IN_Tp:
        case DS_XCOMPLETED | DS_CT_IN_CANCEL:
        case DS_XCOMPLETED | DS_CT_IN_CANCEL_TIMER:
          break;
        case DS_XCOMPLETED | DS_CT_IN_PROVISIONAL:
          // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
        case DS_XCOMPLETED | DS_CT_IN_REL_PROVISIONAL:
          rtx = findAndUpdateRetransmission();
          break;
        case DS_XCOMPLETED | DS_CT_IN_ACK:
          if (firstAckTo2XX) {
            if (m_ackMessage == null) {
              setAckMessage();
              reason = DsMessageLoggingInterface.REASON_GENERATED;
            }
            getAckConnection();
            sendAck();
            nullRefs();
            firstAckTo2XX = false;
          } else {
            m_stateTable.throwException(transition);
          }
          break;
        case DS_XCOMPLETED | DS_CT_IN_2XX:
          rtx = findAndUpdateRetransmission();
          if (!firstAckTo2XX) {
            sendAck();
          }
          break;
        default:
          m_stateTable.throwException(transition);
          break;
      }
    }
  }

  /**
   * The xterminated state machine method. This method is used only by 2XX Client INVITE
   * transactions.
   *
   * @param transition (previous state) OR (input)
   * @throws DsException if error occurs in the state machine
   * @throws IOException if error occurs while sending data through the network
   */
  protected void xterminated(int transition) throws DsException, IOException {
    if (!m_isProxyServerMode) {
      switch (transition & DS_INPUT_MASK) {
        case DS_CT_IN_Tn:
          new ClientTransactionCallback(
                  ClientTransactionCallback.CB_TIMEOUT, null, m_clientInterface)
              .call();
          break;
        case DS_CT_IN_TIMEOUT: // can be combined with "default".
          cancelTn();
          break;
        default:
          cancelTn();
          break;
      }
    }
    cleanup();
  }

  ////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////

  /** Obtain a connection for the ACK message. Does proper route handling etc... */
  private void getAckConnection() throws DsException, IOException, UnknownHostException {
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    boolean acking_200final = (m_responseClass == DsSipResponseCode.DS_SUCCESS);

    if (m_sipRequest != null) {
      m_ackMessage.setURI(m_sipRequest.getURI());
    } else {
      genCat.warn("SipRequest is null while getting connection for ACK message.");
      throw new DsTrackingException(
          TrackingExceptions.NULLPOINTEREXCEPTION,
          "SipRequest is null while getting connection for ACK message.");
    }

    boolean haveConnection = m_connection.isSet();

    DsSipTransactionManager.getTransactionManager()
        .getTransportLayer()
        .setLocalProxyRoute(m_ackMessage);

    // Check if the network is set for ACK, if not, then set as per the initial INVITE.
    if (m_ackMessage.getNetwork() == null) {
      m_ackMessage.setNetwork(m_sipRequest.getNetworkReliably());
    }
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    // route header only applies to 200 final responses
    if (acking_200final) {
      DsSipRouteHeader routeHeader = null;
      try {
        routeHeader = (DsSipRouteHeader) m_ackMessage.getHeaderValidate(DsSipConstants.ROUTE);
      } catch (Exception exc) {
        if (genCat.isEnabled(Level.WARN))
          genCat.warn("getAckConnection(): can't parse ROUTE header", exc);
      }

      DsSipContactHeader contactHeader = null;
      try {
        contactHeader = m_sipResponse.getContactHeaderValidate();
      } catch (Exception exc) {
        if (genCat.isEnabled(Level.WARN))
          genCat.warn("getAckConnection(): can't parse Contact header", exc);
      }

      // this is very wasteful if the dialog layer is on top of
      //    this code, since the URI will already be propertly set -dg
      DsSipURL contactURL = null;
      if (contactHeader != null) {
        try {
          contactURL = (DsSipURL) contactHeader.getURI().clone();
        } catch (ClassCastException exc) {
          throw new DsException(
              "DsSipClientTransactionImpl.ack: 2xx contact"
                  + " header is a non SIP URL: "
                  + contactHeader.getURI());
        }
        m_ackMessage.setURI(contactURL);
      } else {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.warn(
              "Trying to create ACK with no Contact in 2xx; request URI will be that of INVITE");
        }
      }

      // now deterime where to send the message  (Route or Contact)
      if (routeHeader != null) {
        DsURI routeURI;
        if (!m_emulate2543Responses) {
          routeURI = m_ackMessage.lrEscape();
        } else {
          DsSipRouteHeader topRoute = (DsSipRouteHeader) m_ackMessage.getHeaderValidate(ROUTE);
          if (topRoute != null) {
            m_ackMessage.setURI((DsSipURL) topRoute.getURI().clone());
            m_ackMessage.removeHeader(ROUTE, true);
            if (contactHeader != null) {
              // this behavior is unknown and needs testing. this code is not hit since
              // m_emulate2543Responses is disabled
              m_ackMessage.addHeader(new DsSipRouteHeader(contactHeader.getURI()), false, true);
            }
          }
          routeURI = m_ackMessage.getURI();
        }

        if (routeURI.isSipURL()) {
          DsSipURL routeURL = (DsSipURL) routeURI;
          m_connection.getSRVConnection(m_ackMessage, routeURL);
          m_connection.check(m_ackMessage);
        } else {
          throw new DsException(
              "DsSipClientTransactionImpl.ack: ack route"
                  + " header is a non SIP URL:"
                  + routeURI.toString());
        }
      } else // if no route header, look at the contact of the 200 final response
      {
        if (contactURL != null) {
          // MAKE SURE THIS IS CALLED BEFORE WE REMOVE THE ROUTING INFORMATION
          //    (just below)
          m_connection.getSRVConnection(m_ackMessage, contactURL);

          m_connection.check(m_ackMessage);
        } else {
          if (genCat.isEnabled(Level.WARN)) {
            genCat.warn(
                "No Route header on ACK and no contact URL in 2xx response; will route to request URI of INVITE");
          }
        }
      }
    }

    // If the connection was not initialized before.
    if (!haveConnection) {
      m_connection.getRequestConnection(m_ackMessage);
      m_connection.check(m_ackMessage);
    }

    // Verify that Via header is added to all requests.
    if (m_ackMessage.getViaHeader() == null) {
      DsSipViaHeader via = (DsSipViaHeader) m_sipRequest.getViaHeader();
      m_ackMessage.addHeader(via, false); // addHeader() does clone

      // if we're ack'ing a 200, ensure that the branch id is changed
      if (m_responseClass == DsSipResponseCode.DS_SUCCESS) {
        m_ackMessage.nextBranchId();
      }

      m_ackMessage.setFinalised(false);
    }

    // update binding info for ACK message
    m_ackMessage.updateBinding(m_connection.getBindingInfo());
  }

  /*
   * javadoc inherited
   */
  protected void backOff() {
    m_T1 *= 2;
  }

  /*
   * javadoc inherited
   */
  protected void initializeTimers() {
    m_maxT1Timeouts = m_sipTimers.INVITE_CLIENT_TRANS_RETRY;
    m_T1 = m_sipTimers.T1Value;
    m_TCounter = 0;
    m_To = m_sipTimers.TU2Value; // equals to 64 * DsSipTimers.T1Value;

    Transport transport = m_connection.getTransport();
    boolean reliable = DsSipTransportType.intern(transport).isReliable();
    if (reliable) {
      m_To = 0;
    }
  }

  /**
   * Create a client transaction to replace this one when a 503 is recieved.
   *
   * @param connection the connection to use to send the ACK.
   * @param via the via header for the ACK.
   * @param key the transaction key
   * @return a client transaction to replace this one when a 503 is recieved.
   */
  protected DsSipClientTransaction createServiceUnavailableHandler(
      DsSipConnection connection, DsSipViaHeader via, DsSipTransactionKey key) {
    return new ServiceUnavailableHandler(m_sipRequest, m_sipResponse, m_To, connection, key, via);
  }

  /**
   * This class handles sending the ACK for a 503 response. It is created by an INVITE client
   * transaction that gets a 503 response. When a 503 is received, the INVITE client transaction
   * will try the next server in its target set. The client transaction will be reinitialized with a
   * new branch id, and an instance of this class will replace the original transaction in the
   * transaction table.
   */
  private static class ServiceUnavailableHandler extends DsSipClientTransaction implements DsEvent {
    private static Logger m_cat = DsLog4j.LlSMClientCat;

    private DsSipAckMessage m_ackMessage;
    private byte[] m_ackBytes;

    private DsSipConnection m_connection;
    private DsSipTransactionKey m_key;

    private int m_timeout;

    /**
     * Constructor.
     *
     * @param request the INVITE that initiated the original transaction
     * @param response the response from which to build the ACK
     * @param timeout the amount of time before this transaction should be terminated
     * @param connection the connection to send the ACK over (N/A for non INVITEs)
     * @param key the key (used to remove the client transaction)
     */
    ServiceUnavailableHandler(
        DsSipRequest request,
        DsSipResponse response,
        int timeout,
        DsSipConnection connection,
        DsSipTransactionKey key,
        DsSipViaHeader via) {
      m_ackMessage = new DsSipAckMessage(response, null, null);
      m_ackMessage.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
      m_ackMessage.addHeader(via, false, false); // don't clone (it's cloned already)
      m_ackMessage.setURI(request.getURI());

      genCat.info(
          "Creating ServiceUnavailableHandler, m_connection "
              + m_connection
              + " Connection "
              + connection);

      m_connection = connection;
      if (m_connection != null) {
        m_connection.addReference();
      }

      m_key = key;
      m_timeout = timeout;
    }

    // //////////////////////////////////////////////////////////////////
    // //////////       DsSipClientTransaction  methods        //////////
    // //////////////////////////////////////////////////////////////////

    /*
     * retransmit the ACK
     */
    protected synchronized void onResponse(DsSipResponse response) {
      // rtx the ACK
      try {
        if (m_ackBytes != null) {
          m_connection.send(m_ackBytes);
        } else if (m_ackMessage != null) {
          m_connection.send(m_ackMessage);
        } else {
          if (m_cat.isEnabled(Level.WARN))
            m_cat.warn("ServiceUnavailableHandler.onResponse: both ack and bytes are null!");
        }
      } catch (Exception exc) {
        if (m_cat.isEnabled(Level.WARN))
          m_cat.warn("ServiceUnavailableHandler.onResponse: exception retransmitting ACK!", exc);
      }
    }

    public synchronized void onIOException(IOException ioe) {
      // I *think* this is N/A
    }

    /*
     *  javadoc inherited
     */
    public synchronized void run(Object arg) {
      m_connection.removeReference();
      try {
        DsSipTransactionManager.removeTransaction(this);
      } catch (DsException dse) {
        if (m_cat.isEnabled(Level.WARN))
          m_cat.warn("terminated(): Failed to remove transaction ", dse);
      }
    }

    // stubs
    public void ack(DsSipAckMessage request)
        throws DsException, IOException, UnknownHostException {}

    public void cancel(DsSipCancelMessage request) throws DsException, IOException {}

    public void cancel(DsSipClientTransactionInterface cancelInterface, DsSipCancelMessage request)
        throws DsException, IOException {}
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
    public DsSipClientTransaction prack(DsSipPRACKMessage request) throws DsException, IOException {
      return null;
    }

    public DsSipClientTransaction prack(
        DsSipClientTransactionInterface clientInterface, DsSipPRACKMessage request)
        throws DsException, IOException {
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
      try {
        m_ackBytes = m_connection.send(m_ackMessage);
        if (m_ackMessage.shouldCompress()) {
          m_ackBytes = null;
        } else {
          m_ackMessage = null;
        }
      } catch (IOException ioe) {
        DsDiscreteTimerMgr.scheduleNoQ(m_timeout, this, null);
        throw ioe;
      } catch (Exception exc) {
        DsDiscreteTimerMgr.scheduleNoQ(m_timeout, this, null);
        throw new DsException(exc);
      }
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
      DsByteString cid = (m_ackMessage == null) ? new DsByteString("??") : m_ackMessage.getCallId();
      return "CLIENT_TRANS (503 handler) METHOD: INVITE STATE: N/A  KEY: "
          + m_key
          + "\nCALLID:: "
          + (cid != null ? DsByteString.toString(cid) : "null");
    }
  }
}
