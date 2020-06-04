package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.cac.SIPSession;
import com.cisco.dhruva.sip.cac.SIPSessions;
import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.util.*;

/**
 * The proxy object. The proxy class interfaces to the client and server transactions, and presents
 * a unified view of the proxy operation through a proxy interface. The proxy worries about things
 * like forking and knows when it has gotten the best response for a request. However, it leaves the
 * decision about sending responses to the controller, which is passed to the SIP proxy.
 */
public class DsProxyTransaction extends DsProxyStatelessTransaction {

  public static final String NL = System.getProperty("line.separator");

  /** the vector of branches for proxied requests */
  private Map branches = null;

  private boolean m_isForked = false;
  private DsSipClientTransaction m_originalClientTrans = null;
  private DsProxyClientTransaction m_originalProxyClientTrans = null;

  /** the original transaction that initialized this proxy */
  private DsProxyServerTransaction serverTransaction = null;

  /** best response received so far */
  private DsSipResponse bestResponse = null;

  /**
   * this indicates whether all branches have completed with a final response or timeout. More
   * specifically, this is the number of uncompleted branches
   */
  private int branchesOutstanding = 0;

  /** the current state of this ProxyTransaction */
  private int currentClientState = PROXY_INITIAL;

  private int currentServerState = PROXY_INITIAL;

  /** Implements DsSipClientTransactionInterface and DsSipServerTransactionInterface */
  private TransactionInterfaces transactionInterfaces;

  private boolean serverTimerSet = false;

  protected DsHeaderMasking m_HeaderMasker;

  private static final Logger Log = DhruvaLoggerFactory.getLogger(DsProxyTransaction.class);

  //   ProxyStates
  //   /** defines the states for the proxy */

  /** We received a request but have not proxied it yet */
  static final int PROXY_INITIAL = 0;

  /**
   * We have proxied some requests, and may have received some responses, but have not received a
   * 200 or 600, and there are requests for which responses are still pending
   */
  static final int PROXY_PENDING = 1;

  /**
   * We have received a 200 response to some proxied request. There are still requests pending; we
   * may have received responses to other requests, including 600
   */
  static final int PROXY_GOT_200 = 2;

  /**
   * We have receive a 600 response to some proxied request. There are still requests pending; we
   * may have received responses to other requests, but not a 200.
   */
  static final int PROXY_GOT_600 = 3;

  /**
   * We have received a final response for each request we proxied. None of these final responses
   * were 200 or 600
   */
  static final int PROXY_FINISHED = 5;

  /**
   * We have received a final response for each request we proxied. One of these was a 600. There
   * was no 200.
   */
  static final int PROXY_FINISHED_600 = 6;

  /** We have received a final response for each request we proxied. One of these was a 200 */
  static final int PROXY_FINISHED_200 = 7;

  /**
   * We have sent a final response to the request, and it was not a 200. We will not send any other
   * final response, excepting a 200 received
   */
  static final int PROXY_SENT_NON200 = 8;

  /**
   * We have sent a 200 OK response to the request. In this state, we won't send other responses,
   * but may forward additional 200 responses that are received
   */
  static final int PROXY_SENT_200 = 9;

  /** We sent back a provisional */
  static final int PROXY_SENT_100 = 10;

  /** Used when no state transition is desired */
  static final int PROXY_ANY_STATE = 11;

  //   ProxyEvents
  /** Defines the events that can occur in the operation of the proxy */

  /** We have received a 200 class response for a request */
  static final int GOT_200 = 1;

  /** We have received a 100 class response for a request */
  static final int GOT_100 = 2;

  /** We have received a 3xx, 4xx, or 5xx response for a request */
  static final int GOT_345 = 3;

  /** We have received a 6xx response for a request */
  static final int GOT_600 = 4;

  /** We have been asked to add another branch */
  static final int PROXYTO = 5;

  /** We have been asked to cancel all pending branches */
  static final int PROXY_CANCEL = 6;

  static final int SEND_100 = 7;

  static final int SEND_200 = 8;

  static final int SEND_3456 = 9;

  //  ProxyActions (Not Used, but kept for historical reasons)

  /** defines the actions that get executed upon events in the proxy state machine */

  /**
   * We have received a response which is something we'd like to send now. Invoke the best callback.
   * Don't send a CANCEL for all pending requests or send the response, though, since the controller
   * will do that
   */
  static final int GOTBEST = 1;

  /**
   * We have a provisional response which we may want to send. Invoke the provisional method of the
   * controller. Let it decide whether to send it or note
   */
  static final int PROVISIONAL = 2;

  /** Do nothing */
  static final int NOOP = 3;

  /** proxy the request to a URL supplied */
  static final int PROXY_TO = 4;

  /** send a response back to the UAC */
  static final int RESPOND = 5;

  /**
   * We received a final response on one of the branches and need to notify the controller about
   * that
   */
  static final int GOT_FINAL = 6;

  /** Default constructor; useful for object pooling */
  public DsProxyTransaction() {
    transactionInterfaces = new TransactionInterfaces();
  }

  /**
   * The main constructor. Its called by the proxy controller when a new request is received
   *
   * @param controller Controller that gets notified of state changes and events in this
   *     ProxyTransaction
   * @param config configuration settings for this ProxyTransaction
   * @param request SIP request that initiated this transaction
   */
  public DsProxyTransaction(
      DsControllerInterface controller,
      DsProxyParamsInterface config,
      DsSipServerTransaction server,
      DsSipRequest request)
      throws DsInternalProxyErrorException {

    transactionInterfaces = new TransactionInterfaces();

    init(controller, config, server, request);
  }

  /**
   * We allow DsSipServerTransaction for the app server guys. And also that they are expected to use
   * this method
   */
  public synchronized void init(
      DsControllerInterface controller,
      DsProxyParamsInterface config,
      DsSipServerTransaction server,
      DsSipRequest request)
      throws DsInternalProxyErrorException {
    Log.debug("Entering init()");

    super.init(controller, config, request);

    DsSipServerTransaction llServer = server;

    if (llServer == null) {

      Log.debug("In DsProxyTransaction init().  Getting serverTransaction again!!!2");

      llServer = DsSipTransactionManager.getTransactionManager().findServerTransaction(request);
    }

    if (llServer != null) {
      // if not STRAY
      llServer.setInterface(transactionInterfaces);
    }

    if (controller != null) {
      this.controller = controller;
    }

    currentClientState = PROXY_INITIAL;
    currentServerState = PROXY_INITIAL;

    if (branches != null) {
      branches.clear();
    } else {
      Log.debug("No need to clear branches.  They're null!");
    }

    serverTransaction = null;
    bestResponse = null;
    branchesOutstanding = 0;

    serverTimerSet = false;

    try {

      // HACK !!!!!!!!
      // some hacking around to provide quick and easy support for
      // stray ACKs and CANCELs
      if (getStrayStatus() == NOT_STRAY) {
        serverTransaction = createProxyServerTransaction(llServer, request);
      }
    } catch (Throwable e) {
      Log.error("Error creating proxy server transaction", e);
      throw new DsInternalProxyErrorException(e.getMessage());
    }

    Log.debug("Leaving init()");
  }

  /**
   * This allows derived classes to use a class derived from DsSipClientTransaction
   *
   * @param request the request to be sent on this branch
   * @return DsSipClientTransaction or a derived class
   */
  protected DsSipClientTransaction createClientTransaction(DsSipRequest request)
      throws IOException, DsException {

    DsSipClientTransportInfo stub =
        new DsSipClientTransportInfo() {

          public DsBindingInfo getViaInfoForTransport(Transport transport, DsNetwork network) {
            try {
              // TODO remove the reference to internal
              DsListenInterface lif =
                  getDefaultParams().getViaInterface(transport, network.getName());
              if (lif != null)
                return new DsBindingInfo(
                    lif.getAddress().toString(), lif.getPort(), lif.getProtocol());
              else {
                Log.debug("ListenInterface is null !!!!!!!!!!!!!");
                return null;
              }
            } catch (Throwable e) {
              Log.error("Error creating client transaction", e);
              return null;
            }
          }

          public Set getSupportedTransports(DsNetwork network) {
            HashSet set = new HashSet(3, 1);
            // TODO remove the references to internal below
            DsListenInterface lif =
                getDefaultParams().getViaInterface(Transport.UDP, network.getName());
            if (lif != null) {
              set.add(DsSipTransportType.TRANSPORT_ARRAY[Transport.UDP.getValue()]);
              Log.info("UDP transport added");
            }

            lif = getDefaultParams().getViaInterface(Transport.TCP, network.getName());
            if (lif != null) {
              set.add(DsSipTransportType.TRANSPORT_ARRAY[DsSipTransportType.TCP]);
              Log.info("TCP transport added");
            }

            lif = getDefaultParams().getViaInterface(Transport.TLS, network.getName());
            if (lif != null) {
              set.add(DsSipTransportType.TRANSPORT_ARRAY[DsSipTransportType.TLS]);
              Log.info("TLS transport added");
            }

            return set;
          }
        };

    return DsSipTransactionManager.getTransactionManager()
        .startClientTransaction(request, stub, transactionInterfaces);
  }

  /**
   * This allows derived classes to overwrite DsProxyClientTransaction
   *
   * @param clientTrans Low Level DsSipClientTransaction
   * @param request the request to be sent on this branch
   * @return DsProxyClientTransaction or a derived class
   */
  protected DsProxyClientTransaction createProxyClientTransaction(
      DsSipClientTransaction clientTrans, DsProxyCookieInterface cookie, DsSipRequest request) {
    return new DsProxyClientTransaction(this, clientTrans, cookie, request);
  }

  /**
   * This allows derived classes to overwrite DsProxyServerTransaction
   *
   * @param serverTrans Low Level DsSipServerTransaction
   * @param request the request that created this transaction
   * @return DsProxyServerTransaction or a derived class
   */
  protected DsProxyServerTransaction createProxyServerTransaction(
      DsSipServerTransaction serverTrans, DsSipRequest request) {
    return new DsProxyServerTransaction(this, serverTrans, request);
  }

  /**
   * @return the TransactionInterfaces object that contains implementation of Low Level Server- and
   *     ClientTransaction interfaces
   */
  protected TransactionInterfaces getTransactionInterfaces() {
    return transactionInterfaces;
  }

  /**
   * This allows to change the controller midstream, e.g., it allows a generic controller to replace
   * itself with something more specific. Note that no synchronization is provided for this method.
   *
   * @param controller Controller to notify of proxy events.
   */
  public void setController(DsControllerInterface controller) {
    if (controller != null) this.controller = controller;
  }

  /**
   * Returns the DsControllerInterface used for callbacks
   *
   * @return controller Controller to notify of proxy events.
   */
  public DsControllerInterface getController() {
    return controller;
  }

  /** @return the DsProxyServerTransaction */
  public DsProxyServerTransaction getServerTransaction() {
    return serverTransaction;
  }

  /**
   * This method allows the controller to proxy to a specified URL using specified parameters the
   * code will not check to make sure the controller is not adding or removing critical headers like
   * To, From, Call-ID.
   *
   * @param request request to send
   * @param params extra params to set for this branch
   */
  public synchronized void proxyTo(
      DsSipRequest request, DsProxyCookieInterface cookie, DsProxyBranchParamsInterface params) {
    try {

      Log.debug("Entering DsProxyTransaction proxyTo()");

      DsControllerConfig ctrlConfig = DsControllerConfig.getCurrent();
      if (ctrlConfig.isMaskingEnabled(request.getNetwork().getName())) {
        m_HeaderMasker = new DsHeaderMasking(ctrlConfig);
        m_HeaderMasker.encryptHeaders(request);
      }

      // if a stray ACK or CANCEL, proxy statelessly.
      if (getStrayStatus() == STRAY_ACK || getStrayStatus() == STRAY_CANCEL) {
        super.proxyTo(request, cookie, params);

        Log.debug("Leaving DsProxyTransaction proxyTo()");

        return;
      }

      if (currentServerState == PROXY_SENT_200 || currentServerState == PROXY_SENT_NON200) {
        Log.debug("Leaving DsProxyTransaction proxyTo()");
        throw new DsInvalidStateException("Cannot fork once a final response has been sent!");
      }

      switch (currentClientState) {
        case PROXY_GOT_200:
        case PROXY_FINISHED_200:
        case PROXY_GOT_600:
        case PROXY_FINISHED_600:
          Log.debug("Leaving DsProxyTransaction proxyTo()");
          throw new DsInvalidStateException(
              "Cannot fork once a 200 or 600 response has been received!");
        default:
          break;
      }

      try {
        prepareRequest(request, params);

        Log.debug("proxying request");
        Log.debug(
            "Creating SIP client transaction with request:"
                + NL
                + request.maskAndWrapSIPMessageToSingleLineOutput());
        Log.debug("Binding info for request is: " + request.getBindingInfo());

        DsSipClientTransaction clientTrans = createClientTransaction(request);

        DsProxyClientTransaction proxyClientTrans =
            createProxyClientTransaction(clientTrans, cookie, request);

        SIPSession sipSession = SIPSessions.getActiveSession(request.getCallId().toString());

        // adding end point to the sip session
        if (sipSession != null) {
          // MEETPASS TODO
          //          EndPoint ep =
          //              new EndPoint(
          //
          // DsByteString.newInstance(request.getBindingInfo().getNetwork().toString()),
          //
          // DsByteString.newInstance(request.getBindingInfo().getRemoteAddressStr()),
          //                  request.getBindingInfo().getRemotePort(),
          //                  request.getBindingInfo().getTransport());
          //          sipSession.setDestination(ep);
        }

        if ((!m_isForked) && (m_originalClientTrans == null)) {
          m_originalProxyClientTrans = proxyClientTrans;
          m_originalClientTrans = clientTrans;
        } else {
          if (branches == null) {
            branches = new HashMap(2);
          }
          branches.put(clientTrans, proxyClientTrans);

          if (!m_isForked) {
            branches.put(m_originalClientTrans, m_originalProxyClientTrans);
            m_isForked = true;
          }
        }
        branchesOutstanding++;

        // set the user provided timer if necessary
        long timeout;
        if (params != null) timeout = params.getRequestTimeout();
        else timeout = 0;

        if (timeout > 0) {
          proxyClientTrans.setTimeout(timeout);
        }

        controller.onProxySuccess(this, cookie, proxyClientTrans);

      } catch (DsException e) {
        Log.error("Got DsException in proxyTo()!", e);
        // This exception looks like it will be caught immediately by the series
        // of catch blocks below.  Can we do this in a less expensive way? - JPS
        throw new DsInvalidParameterException("Cannot proxy! " + e.getMessage());
      } catch (Exception e) {
        Log.error("Got exception in proxyTo()!", e);
        // This exception looks like it will be caught immediately by the series
        // of catch blocks below.  Can we do this in a less expensive way? - JPS
        DsDestinationUnreachableException exception =
            new DsDestinationUnreachableException(e.getMessage());
        exception.addSuppressed(e);
        throw exception;
      }

    } catch (DsInvalidStateException e) {
      controller.onProxyFailure(
          this, cookie, DsControllerInterface.INVALID_STATE, e.getMessage(), e);
    } catch (DsInvalidParameterException e) {
      controller.onProxyFailure(
          this, cookie, DsControllerInterface.INVALID_PARAM, e.getMessage(), e);
    } catch (DsDestinationUnreachableException e) {
      controller.onProxyFailure(
          this, cookie, DsControllerInterface.DESTINATION_UNREACHABLE, e.getMessage(), e);
    } catch (Throwable e) {
      controller.onProxyFailure(
          this, cookie, DsControllerInterface.UNKNOWN_ERROR, e.getMessage(), e);
    }
  }

  /**
   * This is a utility methods that creates a copy of the request to make sure that forking does not
   * get broken
   */
  protected DsSipRequest cloneRequest() {

    DsSipRequest clone;
    if (m_originalProxyClientTrans != null) {
      clone =
          DsProxyUtils.cloneRequestForForking(
              getOriginalRequest(), processVia(), getDefaultParams().doRecordRoute());
    } else {
      clone = getOriginalRequest();
      clone.setBindingInfo(new DsBindingInfo());
    }

    return clone;
  }

  /**
   * This method allows the controller to send a response. This response can be created by the
   * controller, or can be one obtained from the proxy through the proxy interface.
   *
   * @param response The response to send Note that if response != null, it will be sent verbatim -
   *     be extra careful when using it.
   */
  public synchronized void respond(DsSipResponse response) {
    Log.debug("Entering respond()");

    try {

      int responseClass = 1;

      if (response != null) responseClass = response.getResponseClass();

      if (responseClass != 2
          && currentServerState != PROXY_SENT_100
          && currentServerState != PROXY_INITIAL) {
        // we're in an invalid state and can't send the response
        controller.onResponseFailure(
            this,
            getServerTransaction(),
            DsControllerInterface.INVALID_STATE,
            "Cannot send "
                + DsIntStrCache.intToStr(responseClass)
                + "xx response in "
                + DsIntStrCache.intToStr(currentServerState)
                + " state",
            null);
        return;
      } else if (getStrayStatus() == NOT_STRAY) {
        getServerTransaction().respond(response);

        assert response != null;
        Log.debug("Response sent for {}" + Arrays.toString(response.getSessionId()));
      }
      Log.info("Didn't send response to stray ACK or CANCEL: " + getStrayStatus());
    } catch (DsDestinationUnreachableException e) {
      controller.onResponseFailure(
          this,
          getServerTransaction(),
          DsControllerInterface.DESTINATION_UNREACHABLE,
          e.getMessage(),
          e);
    } catch (Throwable e) {
      controller.onResponseFailure(
          this, getServerTransaction(), DsControllerInterface.UNKNOWN_ERROR, e.getMessage(), e);
    }
  }

  /** This method allows the controller to send the best response received so far. */
  public synchronized void respond() {

    DsSipResponse response = getBestResponse();

    if (response == null) {
      controller.onResponseFailure(
          this,
          getServerTransaction(),
          DsControllerInterface.INVALID_STATE,
          "No final response received so far!",
          null);
    } else respond(response);
  }

  /**
   * This method allows the controller to cancel all pending requests. Only requests for which no
   * response is yet received will be cancelled. Once this method is invoked, subsequent invocations
   * will do nothing. OPEN ISSUE: should we invoke the various response interfaces after the
   * controller calls cancel?
   */
  public synchronized void cancel() {
    Log.debug("Entering cancel()");

    if (!m_isForked) {
      if (m_originalProxyClientTrans != null) {
        m_originalProxyClientTrans.cancel();
      }
    } else {

      DsProxyClientTransaction trans;
      for (Object o : branches.values()) {
        try {
          trans = (DsProxyClientTransaction) o;
          trans.cancel();
        } catch (Exception e) {
          Log.error("Error canceling request", e);
        }
      }
    }
    // A HACK!!!!! This is the only way I can think of of removing
    // the server INVITE transaction with a cancelled branch on which no final
    // response is ever received

    if (!serverTimerSet
        && (getServerTransaction().getResponse() == null
            || getServerTransaction().getResponse().getResponseClass() == 1)) {
      serverTimerSet = true;
    }
  }

  /**
   * Handles ACK message for a branch we cannot match (i.e., with an unknown To tag). Currently just
   * treats it as a stray ACK but may be overwritten in derived classes
   */
  protected void handleAckForUnknownBranch(DsSipAckMessage ack) {
    DsSipProxyManager.getInstance().strayAck(ack);
  }

  /**
   * this should only be called when ack is for a 200 OK, we should probably overload the server
   * transaction to do this. we will need to anyway, since we need a send200 method or something
   * like that which doesn't retransmit anyway, the only action to take here is to invoke the ack
   * callback this is invoked in any state (I think)
   *
   * @param trans
   * @param ack
   */
  protected synchronized void ackCallBack(DsSipServerTransaction trans, DsSipAckMessage ack) {

    Log.debug("Entering ackCallBack()");

    DsSipResponse response = getServerTransaction().getResponse();

    if (response != null && DsProxyUtils.getResponseClass(response) == 2) {

      // we actually need to handle the case of ACKs to multiple
      // 200 OKs here - I know this sucks but Low Level doesn't
      // give me a choice right now
      DsByteString toTag;
      try {
        toTag = response.getToHeaderValidate().getTag();
      } catch (Exception e) {
        toTag = null;
        Log.error("Error getting To header", e);
      }
      boolean branchFound = false;

      if (!m_isForked) {
        try {
          branchFound = checkAckOnBranch(m_originalProxyClientTrans, ack, toTag);
        } catch (Exception e) {
          Log.error("Exception propagating ACK to 200OK!!", e);
        }
      } else {
        Iterator iter = branches.values().iterator();
        DsProxyClientTransaction branch;

        while (iter.hasNext()) {
          try {
            branch = (DsProxyClientTransaction) iter.next();

            if (checkAckOnBranch(branch, ack, toTag)) {
              branchFound = true;
              break;
            }
          } catch (Exception e) {
            Log.error("Exception propagating ACK to 200OK!!", e);
          }
        }
      }

      if (!branchFound) {
        Log.info("Couldn't find the branch to propagate ACK to 200OK, process it statelessly");
        handleAckForUnknownBranch(ack);
      }
    }

    controller.onAck(this, getServerTransaction(), ack);
  }

  private boolean checkAckOnBranch(
      DsProxyClientTransaction branch, DsSipAckMessage ack, DsByteString toTag) throws Exception {

    DsSipResponse branchResponse = branch.getResponse();

    if (branchResponse != null
        && branchResponse.getResponseClass() == DsSipResponseCode.DS_SUCCESS) {

      DsByteString branchToTag = branchResponse.getToHeaderValidate().getTag();

      if ((toTag == null && branchToTag == null) || (toTag != null && toTag.equals(branchToTag))) {

        try {
          Log.debug("propagating ACK to 200OK downstream...");

          if (branch.getState() != DsProxyClientTransaction.STATE_ACK_SENT) {
            DsSipRouteHeader route = (DsSipRouteHeader) ack.getHeaderValidate(DsSipRouteHeader.sID);
            if (route != null && DsControllerConfig.getCurrent().recognize(route.getURI(), false)) {
              ack.removeHeader(DsSipRouteHeader.sID);
            }
            // REDDY how to solve when servergroup is used
            branch.ack(ack);
            Log.debug("propagated ACK to 200OK downstream");
          } else {
            // if a retransmission of the ACK, forward it statelessly
            // to work around bug #2562
            handleAckForUnknownBranch(ack);
            Log.debug("propagated retransmitted ACK to 200OK downstream");
          }

          return true;
        } catch (Exception e) {
          Log.error("Exception propagating ACK to 200OK!!", e);
        }
      }
    }
    return false;
  }

  /**
   * if the cancel is for the primary transaction, invoke the cancel method of the controller.
   * Otherwise, do nothing. This happens in any state (note we can't get cancel once we've sent a
   * response)
   *
   * <p>NOTE: change cancel behavior in server transaction to not send final response to request, or
   * overload to do this. proxy shouldn't send response to request on cancel.
   *
   * @param trans
   * @param cancel
   */
  protected synchronized void cancelCallBack(
      DsSipServerTransaction trans, DsSipCancelMessage cancel) {
    Log.debug("Entering cancelCallBack()");
    try {
      controller.onCancel(this, getServerTransaction(), cancel);
    } catch (DsException e) {
      Log.warn("Exception at cancel CallBack", e);
    }
    Log.debug("Leaving cancelCallBack()");
  }

  protected synchronized void timeOut(DsSipServerTransaction trans) {
    DsProxyServerTransaction serverTrans = getServerTransaction();
    if (trans != null
        && serverTrans != null
        && serverTrans.getResponse() != null
        && serverTrans.getResponse().getResponseClass() != 2) {
      Log.debug("Calling controller.onResponseTimeout()");
      controller.onResponseTimeOut(this, serverTrans);
    }
  }

  protected synchronized void timeOut(DsSipClientTransaction trans) {
    Log.debug("Entering timeOut()");
    DsProxyClientTransaction proxyClientTrans;

    if (m_isForked) {
      proxyClientTrans = (DsProxyClientTransaction) branches.get(trans);
    } else {
      proxyClientTrans = m_originalProxyClientTrans;
    }

    if (proxyClientTrans == null) {
      Log.warn("timeOut(ClientTrans) callback called for transaction we don't know!");
      return;
    }

    int clientState = proxyClientTrans.getState();
    if (proxyClientTrans.isTimedOut()
        || (clientState != DsProxyClientTransaction.STATE_REQUEST_SENT
            && clientState != DsProxyClientTransaction.STATE_PROV_RECVD)) {
      Log.debug("timeOut(ClientTrans) called in no_action state");
      return;
    }

    branchDone();

    if (clientState == DsProxyClientTransaction.STATE_PROV_RECVD) {
      Log.debug("cancelling ProxyClientTrans");
      proxyClientTrans.cancel();
    }

    // ignore future responses except 200 OKs
    proxyClientTrans.timedOut();

    // invoke the cancel method on the transaction??
    // construct a timeout response

    try {
      DsSipResponse response =
          DsProxyResponseGenerator.createResponse(
              DsSipResponseCode.DS_RESPONSE_REQUEST_TIMEOUT, getOriginalRequest());
      updateBestResponse(response);
    } catch (DsException e) {
      Log.error("Exception thrown creating response for timeout", e);
    }

    // invoke the finalresponse method above
    controller.onRequestTimeOut(this, proxyClientTrans.getCookie(), proxyClientTrans);

    if (areAllBranchesDone()) {
      controller.onBestResponse(this, getBestResponse());
    }
  }

  /**
   * callback when an icmp error occurs on Datagram socket
   *
   * @param trans DsSipClientTransaction on which ICMP error was received
   */
  protected synchronized void icmpError(DsSipClientTransaction trans) {

    DsProxyClientTransaction proxyClientTrans;
    // look up in action table and do execute
    if (m_isForked) {
      proxyClientTrans = (DsProxyClientTransaction) branches.get(trans);
    } else {
      proxyClientTrans = m_originalProxyClientTrans;
    }

    if (proxyClientTrans == null) {
      Log.info("Can't find client transaction in ICMP error callback. Probably a CANCEL");
      return;
    }

    branchDone();

    // ignore future responses except 200 OKs
    proxyClientTrans.timedOut(); // do I really need to call this?

    // invoke the cancel method on the transaction??
    // construct a timeout response
    try {
      DsSipResponse response =
          DsProxyResponseGenerator.createResponse(
              DsSipResponseCode.DS_RESPONSE_NOT_FOUND, getOriginalRequest());
      updateBestResponse(response);
    } catch (DsException e) {
      Log.error("Error generating response in ICMP", e);
    }

    controller.onICMPError(this, proxyClientTrans.getCookie(), proxyClientTrans);

    if (areAllBranchesDone()) {
      controller.onBestResponse(this, getBestResponse());
    }
  }

  /**
   * callback used when server closed TCP/TLS connection We'll treat this close as an equivalent to
   * receiving a 500 response
   *
   * @param trans DsSipClientTransaction on which the connection was closed
   */
  protected synchronized void close(DsSipClientTransaction trans) {
    // look up in action table and do execute
    DsProxyClientTransaction clientTrans;

    if (m_isForked) {
      clientTrans = (DsProxyClientTransaction) branches.get(trans);
    } else {
      clientTrans = m_originalProxyClientTrans;
    }

    if (clientTrans != null) {
      try {
        DsSipResponse resp =
            DsProxyResponseGenerator.createResponse(
                DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR, clientTrans.getRequest());
        finalResponse(trans, resp);
      } catch (DsException e) {
        Log.error("Error creating response in close", e);
      }
    } else {
      Log.info("Can't find client transaction in close callback. Probably a CANCEL");
    }
  }

  /**
   * callback when an icmp error occurs on Datagram socket
   *
   * @param trans DsSipServerTransaction on which the ICMP error occurred
   */
  protected synchronized void icmpError(DsSipServerTransaction trans) {
    controller.onICMPError(this, getServerTransaction());
  }

  /** These are the implementations of the client interfaces */
  protected synchronized void provisionalResponse(
      DsSipClientTransaction trans, DsSipResponse response) {
    Log.debug("Entering provisionalResponse()");
    // look up in action table and do execute
    DsProxyClientTransaction clientTrans;

    if (m_isForked) {
      clientTrans = (DsProxyClientTransaction) branches.get(trans);
    } else {
      clientTrans = m_originalProxyClientTrans;
    }

    if (clientTrans != null) {
      if (processVia()) {
        DsProxyUtils.removeTopVia(response);
      }

      if (m_HeaderMasker != null) m_HeaderMasker.decryptHeaders(response);

      clientTrans.gotResponse(response);

      if (!clientTrans.isTimedOut())
        controller.onProvisionalResponse(this, clientTrans.getCookie(), clientTrans, response);
    } else {
      Log.debug("Couldn't find ClientTrans for a provisional");
      Log.debug("Possibly got response to a CANCEL");
    }
    Log.debug("Leaving provisionalResponse()");
  }

  protected synchronized void finalResponse(DsSipClientTransaction trans, DsSipResponse response) {
    Log.debug("Entering finalResponse()");

    controller.onResponse(response);

    DsProxyClientTransaction proxyClientTransaction;

    if (m_isForked) {
      proxyClientTransaction = (DsProxyClientTransaction) branches.get(trans);
    } else {
      proxyClientTransaction = m_originalProxyClientTrans;
    }

    if (proxyClientTransaction != null) {

      boolean retransmit200 = false;

      if (processVia()) {
        DsProxyUtils.removeTopVia(response);
      }

      if (m_HeaderMasker != null) m_HeaderMasker.decryptHeaders(response);

      if (!proxyClientTransaction.isTimedOut()) branchDone(); // otherwise it'd already been done()

      updateBestResponse(response);

      proxyClientTransaction.gotResponse(response);

      int responseClass = response.getStatusCode() / 100;

      // in the first switch, send ACKs and update the state
      switch (responseClass) {
        case 2:
          if (proxyClientTransaction.getState()
                  == DsProxyClientTransaction.STATE_FINAL_RETRANSMISSION_RECVD
              || proxyClientTransaction.getState() == DsProxyClientTransaction.STATE_ACK_SENT) {
            // retransmission of a 200 OK response
            try {
              Log.info("Proxy received a retransmission of 200OK");

              retransmit200 = true;

              // getServerTransaction().retransmit200(response);
              getServerTransaction().retransmit200();

            } catch (Exception e) {
              Log.error("Exception retransmitting 200!", e);
            }
          }
          break;
        case 3:
        case 4:
        case 5:
          if (proxyClientTransaction.isInvite())
            try {
              proxyClientTransaction.ack();
            } catch (Exception e) {
              Log.error("Exception sending ACK: ", e);
            }
          break;
        case 6:
          if (proxyClientTransaction.isInvite())
            try {
              proxyClientTransaction.ack();
            } catch (Exception e) {
              Log.error("Exception sending ACK: ", e);
            }
          break;
      }

      // in the second switch, notify the controller

      // Notify the controller on an initial 2xx response or on a 2xx response
      // to INVITE received after the transaction has timed out
      if ((responseClass == 2 && !retransmit200)
          && (!proxyClientTransaction.isTimedOut() || proxyClientTransaction.isInvite()))
        controller.onSuccessResponse(
            this, proxyClientTransaction.getCookie(), proxyClientTransaction, response);

      if (!proxyClientTransaction.isTimedOut()) {
        switch (responseClass) {
          case 3:
            controller.onRedirectResponse(
                this, proxyClientTransaction.getCookie(), proxyClientTransaction, response);
            break;
          case 4:
          case 5:
            controller.onFailureResponse(
                this, proxyClientTransaction.getCookie(), proxyClientTransaction, response);
            break;
          case 6:
            controller.onGlobalFailureResponse(
                this, proxyClientTransaction.getCookie(), proxyClientTransaction, response);
            // cancel();  Edgar asked us to change this.
            break;
        }
      }

      if (!retransmit200 && (responseClass == 6 || responseClass == 2 || areAllBranchesDone())) {
        if ((responseClass == 2 && proxyClientTransaction.isInvite())
            || !proxyClientTransaction.isTimedOut()) {
          controller.onBestResponse(this, getBestResponse());
        }
      }

    } else {
      Log.debug("Couldn't find ClientTrans for a final response");
      Log.debug("Possibly got response to a CANCEL");
    }
    Log.debug("Leaving finalResponse()");
  }

  public DsSipResponse getBestResponse() {
    return bestResponse;
  }

  protected boolean areAllBranchesDone() {
    return branchesOutstanding == 0;
  }

  private void updateBestResponse(DsSipResponse response) {
    if (bestResponse == null
        || bestResponse.getStatusCode() > response.getStatusCode()
        || response.getResponseClass() == DsSipResponseCode.DS_SUCCESS) {
      // Note that _all_ 200 responses must be forwarded

      bestResponse = response;

      Log.debug(
          "Best response updated to" + NL + response.maskAndWrapSIPMessageToSingleLineOutput());
    }
  }

  private void branchDone() {
    if (branchesOutstanding > 0) branchesOutstanding--;
  }

  /**
   * This is an inner class that implements the DsEvent interface that should replace the
   * TransactionInterfaces inner class, which implements the deprecated DsObserverInterface - BJ
   */
  protected class TimeoutInterface implements DsEvent {

    protected final Trace Log = Trace.getTrace(DsProxyTransaction.TimeoutInterface.class.getName());

    /**
     * Handler for timer event.
     *
     * @param argument argument supplied to DsTimer.schedule
     * @see DsTimer#schedule
     */
    public void run(Object argument) {
      Log.debug("User timeout called on transaction: " + DsProxyTransaction.this);

      DsSipClientTransaction transaction = (DsSipClientTransaction) argument;

      DsProxyTransaction.this.timeOut(transaction);
    }
  }

  /**
   * This is an inner class that implements ServerTransactionInterface and
   * ClientTransactionInterface, thus hiding the necessarily public methods from the Controller
   */
  protected class TransactionInterfaces
      implements DsSipClientTransactionInterface, DsSipServerTransactionInterface, DsEvent {

    //	protected static final Trace Log =
    // Trace.getTrace(DsProxyTransaction.TransactionInterfaces.class.getName());

    /**
     * The following methods are all implementations of the interfaces that will be invoked by the
     * client and server transaction callbacks They should not be public but that's the easiest way
     * to implement them. I'll have a fix later when I get time.
     */

    /* implementation of serverevents */

    public void ack(DsSipServerTransaction trans, DsSipAckMessage ack) {

      Log.debug("ack(ServerTransaction) call back called by the Low Level");

      DsProxyTransaction.this.ackCallBack(trans, ack);
    }

    public void cancel(DsSipServerTransaction trans, DsSipCancelMessage cancel) {
      Log.debug("cancel() call back called by the Low Level");

      DsProxyTransaction.this.cancelCallBack(trans, cancel);
    }

    public void prack(
        DsSipServerTransaction dsSipServerTransaction,
        DsSipServerTransaction dsSipServerTransaction1) {
      // TODO REDDY
    }

    public void timeOut(DsSipServerTransaction trans) {
      Log.debug("timeOut(ServerTransaction) call back called by the Low Level");

      DsProxyTransaction.this.timeOut(trans);
    }

    /**
     * Method that gets invoked when the client closes the connection This method is functionally
     * equivalent to CANCEL
     *
     * @param serverTransaction handle of transaction
     */
    public void close(DsSipServerTransaction serverTransaction) {
      Log.debug("close(ServerTransaction) call back called by the Low Level");

      DsProxyTransaction.this.cancelCallBack(serverTransaction, null);
    }

    // callback when an icmp error occurs on Datagram socket
    public void icmpError(DsSipClientTransaction trans) {
      Log.debug("icmpError(ClientTransaction) call back called by the Low Level");

      DsProxyTransaction.this.icmpError(trans);
    }

    // callback when server closed TCP/TLS connection
    public void close(DsSipClientTransaction trans) {
      Log.debug("close(clientTransaction) call back called by the Low Level");

      DsProxyTransaction.this.close(trans);
    }

    // callback when an icmp error occurs on Datagram socket
    public void icmpError(DsSipServerTransaction trans) {
      Log.debug("icmpError(ServerTransaction) call back called by the Low Level");

      DsProxyTransaction.this.icmpError(trans);
    }

    /* These are the implementations of the client interface */

    public void provisionalResponse(DsSipClientTransaction trans, DsSipResponse response) {
      Log.debug("provisionalResponse() call back called by the Low Level");

      DsProxyTransaction.this.provisionalResponse(trans, response);
    }

    public void finalResponse(DsSipClientTransaction trans, DsSipResponse response) {
      Log.debug("finalResponse() call back called by the Low Level");

      DsProxyTransaction.this.finalResponse(trans, response);
    }

    public void timeOut(DsSipClientTransaction trans) {
      Log.debug("timeOut(ClientTransaction) call back called by the Low Level");

      DsLog4j.logSessionId(DsProxyTransaction.this.getOriginalRequest());
      DsProxyTransaction.this.timeOut(trans);
    }

    /**
     * BJ - Implementation of DsEvent.run() Replaces DsObserver.onNotification() In my case it's
     * called when a user-set branch timeout expires
     *
     * @param argument argument supplied to DsTimer.schedule
     * @see DsTimer#schedule
     */
    public void run(Object argument) {
      Log.debug("User timeout called on transaction: " + DsProxyTransaction.this);

      DsSipClientTransaction transaction = (DsSipClientTransaction) argument;

      this.timeOut(transaction);
    }
  }
}
