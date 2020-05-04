/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipClientTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTimer;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.TimerTask;

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
  private TimerTask timeoutTimer = null;

  private boolean isTimedOut = false;

  private static final Trace Log = Trace.getTrace(DsProxyClientTransaction.class.getName());

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
    if (Log.on && Log.isDebugEnabled())
      Log.debug("ProxyClientTransaction for " + request.getMethod() + " : connid=" + connId);
    // end
    if (proxy.processVia()) {
      topVia = (DsSipHeaderInterface) request.getViaHeader().clone();
    }

    network = request.getNetwork();

    if (Log.on && Log.isDebugEnabled())
      Log.debug("ProxyClientTransaction created to " + request.getURI());
  }

  /** Cancels this branch if in the right state */
  public void cancel() {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering cancel()");
    if ((getState() == STATE_REQUEST_SENT || getState() == STATE_PROV_RECVD)
        && (getRequest().getMethodID() == DsSipConstants.INVITE)) {
      try {
        if (Log.on) {
          if (Log.isDebugEnabled()) Log.debug("Canceling branch to " + getRequest().getURI());
          if (Log.isInfoEnabled()) Log.info("starting cancel: state=" + getState());
        }

        DsSipCancelMessage cancel = new DsSipCancelMessage(request);
        cancel.setApplicationReason(DsMessageLoggingInterface.REASON_GENERATED);

        DsBindingInfo binfo = request.getBindingInfo();
        if (binfo == null) {
          if (Log.on && Log.isDebugEnabled()) Log.debug("Creating new BindingInfo for CANCEL");
          cancel.setBindingInfo(binfo);
        } else {
          cancel.setBindingInfo((DsBindingInfo) binfo.clone());
        }
        // added new
        if (Log.on && Log.isDebugEnabled())
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
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving cancel()");
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
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering ack()");
    if (response == null || (DsProxyUtils.getResponseClass(response) == 1))
      throw new DsInvalidStateException("Cannot ACK before the final response is received");

    if (topVia != null) ack.addHeader(topVia, true);

    // reset local BindingInfo to work with new UA NAt traversal code
    // 06/07/05: The following three lines were commented as there is no
    //           special handling required for the ACK as JUA was fixed.
    ack.setBindingInfo(new DsBindingInfo());
    // ack.setLocalBindingPort(DsBindingInfo.LOCAL_PORT_UNSPECIFIED);
    // ack.setLocalBindingAddress((InetAddress) null);

    // added new
    if (Log.on) Log.debug("setting connection id for the ACK message :" + connId);

    ack.getBindingInfo().setConnectionId(connId);
    // end

    ack.setNetwork(network);

    if (Log.on && Log.isDebugEnabled()) {
      Log.debug("ACK just before forwarding:" + NL + ack);
      Log.debug("ACK binding info: " + ack.getBindingInfo());
    }
    branch.ack(ack);
    state = STATE_ACK_SENT;
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving ack()");
  }

  /** Saves the last response received */
  protected void gotResponse(DsSipResponse resp) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering gotResponse()");

    if (response == null || response.getResponseClass() == 1) {
      response = resp;
      int responseClass = response.getResponseClass();

      if (responseClass == 1) {
        state = STATE_PROV_RECVD;
        if (Log.on && Log.isDebugEnabled()) Log.debug("In STATE_PROV_RECVD");
      } else {
        state = STATE_FINAL_RECVD;
        // we've just received a final response so there is no point
        // holding up the references until the max-timeout timer
        if (!removeTimeout()) {
          if (Log.on && Log.isInfoEnabled())
            Log.info("Cannot remove the user-defined timer for client transaction");
        }
        if (Log.on && Log.isDebugEnabled()) Log.debug("In STATE_FINAL_RECVD");
      }
    } else if (getState() == STATE_FINAL_RECVD) {
      state = STATE_FINAL_RETRANSMISSION_RECVD;

      response.setApplicationReason(resp.getApplicationReason());

      if (Log.on && Log.isDebugEnabled()) Log.debug("In STATE_FINAL_RETRANSMISSION_RECVD");
    }
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving gotResponse()");
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
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Set user timer for " + milliSec + " milliseconds");
  }

  protected boolean removeTimeout() {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering removeTimeout()");
    boolean success = false;
    if (timeoutTimer != null) {
      timeoutTimer.cancel();
      timeoutTimer = null;
      success = true;
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving removeTimeout(), returning " + success);
    return success;
  }

  public void timedOut() {
    isTimedOut = true;
    if (!removeTimeout()) {
      if (Log.on && Log.isInfoEnabled())
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
