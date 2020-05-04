/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipClientTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

/**
 * This interface is used to control a proxy. The proxy will invoke the various public methods of
 * the controller at opportune times.
 */
public interface DsControllerInterface {

  /**
   * The first method invoked by ProxyTransaction right after it's created, i.e., whenever a new
   * request is received The implementation of this method MUST create a DsProxyTransaction object
   * and return it to the ProxyManager // * @param proxy ProxyTransaction object that will handle //
   * * the received request
   *
   * @param serverTransaction ServerTransaction for this request
   * @param request received request
   * @return ProxyTransaction
   */
  public DsProxyStatelessTransaction onNewRequest(
      DsSipServerTransaction serverTransaction, DsSipRequest request);

  /* =============================================================== */
  /* =============================================================== */

  /*
   * New asynchronous callbacks
   */

  public static final int INVALID_STATE = 1;
  public static final int INVALID_PARAM = 2;
  public static final int DESTINATION_UNREACHABLE = 3;
  public static final int UNKNOWN_ERROR = 4;

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
      DsProxyClientTransaction trans);

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
      Throwable exception);

  /**
   * This callback is invoked if a response was forwarded successfully, i.e., without any
   * synchronous exceptions and a DsProxyClientTransaction is created
   *
   * @param proxy ProxyTransaction object
   * @param trans DsProxyServerTransaction on which the response was sent
   */
  public void onResponseSuccess(DsProxyTransaction proxy, DsProxyServerTransaction trans);

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
      Throwable exception);

  /* =============================================================== */
  /* =============================================================== */

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
      DsSipResponse response);

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
      DsSipResponse response);

  /**
   * This method is invoked by the proxy when a 2xx response to a proxied request is received.
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
      DsSipResponse response);

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
      DsSipResponse response);

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
      DsSipResponse response);

  /**
   * This method is invoked when the proxy receives a response it would like to send.
   *
   * @param response The response the proxy believes is the best and would like to send.
   * @param proxy The proxy object Note: this interface will need to be changed to handle multiple
   *     200 OKs. My understanding is that Low Level API currently drops all 200 OKs after the first
   *     one so I didn't bother to define a controller API for this as well
   */

  //   * @param trans The server transaction on which this response
  //   * needs to be sent. This is only relevant for multiple 200 OK
  //   * responses.

  public void onBestResponse(
      DsProxyTransaction proxy,
      /*DsProxyServerTransaction trans,*/
      DsSipResponse response);

  /**
   * This method is invoked whenever a ClientTransaction times out before receiving a response
   *
   * @param proxy The proxy object
   * @param trans DsProxyClientTransaction where the timeout occurred
   * @param cookie cookie object passed to proxyTo()
   */
  public void onRequestTimeOut(
      DsProxyTransaction proxy, DsProxyCookieInterface cookie, DsProxyClientTransaction trans);

  /**
   * This method is invoked whenever a ServerTransaction times out, i.e., no ACK is received within
   * the allotted time The method is only relevant for INVITE transactions for which a non-200
   * response was sent
   *
   * @param proxy The proxy object
   * @param trans DsProxyServerTransaction where the timeout occurred If controller decides to
   *     undertake any actions in response to this event, it might pass the request back to the
   *     ProxyTransaction to identify the timed out ClientTransaction
   */
  public void onResponseTimeOut(DsProxyTransaction proxy, DsProxyServerTransaction trans);

  /**
   * This is invoked whenever an ICMP error occurs while retransmitting a response over UDP
   *
   * @param proxy The proxy object
   * @param trans DsProxyServerTransaction where the timeout occurred
   */
  public void onICMPError(DsProxyTransaction proxy, DsProxyServerTransaction trans);

  /**
   * This is invoked whenever an ICMP error occurs while retransmitting a request over UDP
   *
   * @param proxy The proxy object
   * @param cookie cookie object passed to proxyTo()
   * @param trans DsProxyClientTransaction where the timeout occurred
   */
  public void onICMPError(
      DsProxyTransaction proxy, DsProxyCookieInterface cookie, DsProxyClientTransaction trans);

  /**
   * This is invoked whenever an ACK is received for the response we sent back.
   *
   * @param proxy the ProxyTransaction object
   * @param transaction the ServerTransaction being ACKed
   * @param ack the ACK request
   */
  public void onAck(
      DsProxyTransaction proxy, DsProxyServerTransaction transaction, DsSipAckMessage ack);

  /**
   * this is called when a CANCEL is received for the original Transaction
   *
   * @param proxy The proxyTransaction object
   * @param trans ServerTransaction being cancelled
   * @param cancel the CANCEL request
   * @throws DsException
   */
  public void onCancel(
      DsProxyTransaction proxy, DsProxyServerTransaction trans, DsSipCancelMessage cancel)
      throws DsException;

  /**
   * this method is triggered whenever {@link
   * DsProxyTransaction#finalResponse(DsSipClientTransaction, DsSipResponse)} receives response
   *
   * @param response
   * @return
   */
  public void onResponse(DsSipResponse response);
}
