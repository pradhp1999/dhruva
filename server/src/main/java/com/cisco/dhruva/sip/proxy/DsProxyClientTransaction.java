/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyClientTransaction.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipClientTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;

/**
 * This is a wrapper class for the ClientTransaction, which adds some additional data members needed
 * by the proxy and only exposes the functionality needed by the Controller
 */
public class DsProxyClientTransaction {

  public static final String NL = System.getProperty("line.separator");

  protected static final int STATE_REQUEST_SENT = 0;
  protected static final int STATE_PROV_RECVD = 1;
  protected static final int STATE_FINAL_RECVD = 2;
  protected static final int STATE_ACK_SENT = 3;
  protected static final int STATE_CANCEL_SENT = 4;
  protected static final int STATE_FINAL_RETRANSMISSION_RECVD = 5;

  /** the ProxyTransaction */
  private DsProxyTransaction proxy;

  /** The client transaction */
  private DsSipClientTransaction branch;

  private DsSipRequest request;

  private DsSipResponse response;

  private DsSipHeaderInterface topVia;

  private DsByteString connId;

  private int state;

  /** Holds a cookie used in asynchronous callbacks */
  private DsProxyCookieInterface cookie;

  protected DsNetwork network;

  // holds the (max-request-timeout)timer inserted into the DsScheduler
  // Once it fires, we send CANCEL if the transaction is not terminated yet
  // We also need to remove this timer if the transaction completes in some
  // other way so that we don't hold the references unnecessarily (this
  // will enable garbage collection)
  private ScheduledFuture timeoutTimer = null;

  private boolean isTimedOut = false;

  private static final Logger Log = DhruvaLoggerFactory.getLogger(DsProxyClientTransaction.class);

  protected DsProxyClientTransaction(
      DsProxyTransaction proxy,
      DsSipClientTransaction branch,
      DsProxyCookieInterface cookie,
      DsSipRequest request) {

    this.proxy = proxy;
    this.branch = branch;
    this.request = request;
    state = STATE_REQUEST_SENT;
    this.cookie = cookie;
    // added new
    connId = request.getBindingInfo().getConnectionId();
    Log.debug("ProxyClientTransaction for " + request.getMethod() + " : connid=" + connId);
    // end
    if (proxy.processVia()) {
      topVia = (DsSipHeaderInterface) request.getViaHeader().clone();
    }

    network = request.getNetwork();

    Log.debug("ProxyClientTransaction created to " + request.getURI());
  }

  /** Cancels this branch if in the right state */
  public void cancel() {
    Log.debug("Entering cancel()");
    if ((getState() == STATE_REQUEST_SENT || getState() == STATE_PROV_RECVD)
        && (getRequest().getMethodID() == DsSipConstants.INVITE)) {
      try {

        Log.debug("Canceling branch to " + getRequest().getURI());
        Log.info("starting cancel: state=" + getState());

        DsSipCancelMessage cancel = new DsSipCancelMessage(request);
        cancel.setApplicationReason(DsMessageLoggingInterface.REASON_GENERATED);

        DsBindingInfo binfo = request.getBindingInfo();
        if (binfo == null) {
          Log.debug("Creating new BindingInfo for CANCEL");
          cancel.setBindingInfo(binfo);
        } else {
          cancel.setBindingInfo((DsBindingInfo) binfo.clone());
        }
        // added new
        Log.debug("setting connection id for the CANCEL message :" + connId);

        cancel.getBindingInfo().setConnectionId(connId);
        // end
        cancel.setNetwork(network);

        if (topVia != null) {
          DsProxyUtils.removeHeader(cancel, DsSipViaHeader.sID);
          cancel.addHeader(topVia, true);
        }

        branch.cancel(cancel);
        state = STATE_CANCEL_SENT;

      } catch (Throwable e) {
        Log.error("Error sending CANCEL", e);
      }
    }
    Log.debug("Leaving cancel()");
  }

  /** @return <b>true</b> if this is an INVITE transaction, <b>false</b> otherwise */
  public boolean isInvite() {
    return (getRequest().getMethodID() == DsSipConstants.INVITE);
  }

  /** sends an ACK */
  protected void ack()
      throws DsException, IOException, UnknownHostException, DsInvalidStateException {

    DsSipAckMessage ack = new DsSipAckMessage(response, null, null);
    ack.setApplicationReason(DsMessageLoggingInterface.REASON_GENERATED);
    ack(ack);
  }

  /**
   * sends an ACK
   *
   * @param ack the ACK message to send
   */
  protected void ack(DsSipAckMessage ack)
      throws DsException, IOException, UnknownHostException, DsInvalidStateException {
    Log.debug("Entering ack()");
    if (response == null || (DsProxyUtils.getResponseClass(response) == 1))
      throw new DsInvalidStateException("Cannot ACK before the final response is received");

    if (topVia != null) ack.addHeader(topVia, true);

    // reset local BindingInfo to work with new UA NAt traversal code
    // 06/07/05: The following three lines were commented as there is no
    //           special handling required for the ACK as JUA was fixed.
    ack.setBindingInfo(new DsBindingInfo());

    // added new
    Log.debug("setting connection id for the ACK message :" + connId);

    ack.getBindingInfo().setConnectionId(connId);
    // end

    ack.setNetwork(network);

    Log.debug("ACK just before forwarding:" + NL + ack);
    Log.debug("ACK binding info: " + ack.getBindingInfo());

    branch.ack(ack);

    state = STATE_ACK_SENT;
    Log.debug("Leaving ack()");
  }

  /** Saves the last response received */
  protected void gotResponse(DsSipResponse resp) {
    Log.debug("Entering gotResponse()");

    if (response == null || response.getResponseClass() == 1) {
      response = resp;
      int responseClass = response.getResponseClass();

      if (responseClass == 1) {
        state = STATE_PROV_RECVD;
        Log.debug("In STATE_PROV_RECVD");
      } else {
        state = STATE_FINAL_RECVD;
        // we've just received a final response so there is no point
        // holding up the references until the max-timeout timer
        if (!removeTimeout()) {
          Log.info("Cannot remove the user-defined timer for client transaction");
        }
        Log.debug("In STATE_FINAL_RECVD");
      }
    } else if (getState() == STATE_FINAL_RECVD) {
      state = STATE_FINAL_RETRANSMISSION_RECVD;

      response.setApplicationReason(resp.getApplicationReason());

      Log.debug("In STATE_FINAL_RETRANSMISSION_RECVD");
    }
    Log.debug("Leaving gotResponse()");
  }

  /** @return The cookie that the user code associated with this branch */
  protected DsProxyCookieInterface getCookie() {
    return cookie;
  }

  protected int getState() {
    return state;
  }

  protected DsSipRequest getRequest() {
    return request;
  }

  protected void setTimeout(long milliSec) {
    timeoutTimer = DsTimer.schedule(milliSec, proxy.getTransactionInterfaces(), branch);
    Log.debug("Set user timer for " + milliSec + " milliseconds");
  }

  protected boolean removeTimeout() {
    Log.debug("Entering removeTimeout()");
    boolean success = false;
    if (timeoutTimer != null) {
      timeoutTimer.cancel(false);
      timeoutTimer = null;
      success = true;
    }
    Log.debug("Leaving removeTimeout(), returning " + success);
    return success;
  }

  public void timedOut() {
    isTimedOut = true;
    if (!removeTimeout()) {
      Log.info("Cannot remove the user-defined timer for client transaction");
    }
  }

  protected boolean isTimedOut() {
    return isTimedOut;
  }

  /**
   * @return the last provisional or the final response received by this transaction. This method is
   *     not strictly necessary but it makes application's life somewhat easier as the application
   *     is not required to save the response for later reference NOTE: modifying this response will
   *     have unpredictable consequences on further operation of this transaction
   */
  public DsSipResponse getResponse() {
    return response;
  }
}
