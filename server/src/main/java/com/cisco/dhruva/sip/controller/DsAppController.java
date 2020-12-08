package com.cisco.dhruva.sip.controller;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipClientTransactionImpl;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.Optional;

public class DsAppController extends DsProxyController implements DsControllerInterface {

  // MEETPASS set through config
  private boolean processRouteHeader = true;

  protected static Logger Log = DhruvaLoggerFactory.getLogger(DsAppController.class);

  private ProxyAdaptorFactoryInterface proxyAdaptorFactory;

  private AppInterface appSession;

  private AppAdaptorInterface adaptor;

  public DsAppController(ProxyAdaptorFactoryInterface f, AppInterface app) {
    setAdaptorFactory(f);
    setAppInterface(app);
  }

  /**
   * The first method invoked by ProxyManager right after it has retreived a controller from the
   * controller factory (this happens when it receives a new request). The implementation of this
   * method MUST create a DsProxyTransaction object and return it to the ProxyManager
   *
   * @param request The incoming request that triggered this method
   * @return ProxyStatelessTransaction
   */
  public DsProxyStatelessTransaction onNewRequest(
      DsSipServerTransaction serverTrans, DsSipRequest request) {

    DsProxyStatelessTransaction trans = super.onNewRequest(serverTrans, request);
    if (trans == null) throw new AssertionError();
    Log.debug("DsAppController.onNewRequest()");

    if (respondedOnNewRequest) {
      // request has been responded and no need to continue
      return (DsProxyStatelessTransaction) ourProxy;
    }

    try {

      if (processRouteHeader
          || request.getHeader(DsSipConstants.ROUTE) == null
          || !DsSipClientTransactionImpl.isMidDialogRequest(request)) {
        Log.info(
            "sending the request to adaptor layer for further processing, not a midcall: "
                + !DsSipClientTransactionImpl.isMidDialogRequest(request)
                + "; route: "
                + request.getHeader(DsSipConstants.ROUTE)
                + "; processRoute: "
                + processRouteHeader);
        AppAdaptorInterface proxyAdaptor = getProxyAdaptor();
        Optional<AppAdaptorInterface> p = Optional.ofNullable(proxyAdaptor);
        if (p.isPresent()) proxyAdaptor.handleRequest(request);
        else Log.warn("proxy adaptor null ");
      } else {
        // We don't want App Adaptor layer to process this request, so we by bypass it
        // by making a call directly to the proxy core.  Note that failover
        // will not be possible for this branch.  Nothing more will be done until
        // the core makes the onBestResponse callback

        Log.debug("Skipping App layer and sending to the URL in the " + " Route header");

        Location loc = new Location(request.getURI());
        loc.setProcessRoute(true);
        this.usingRouteHeader = true;

        this.proxyTo(loc, ourRequest, null);
      }
    } catch (Throwable e) {
      // Error Logging
      Log.error("onNewRequest() - Execution error while invoking adaptor layer ", e);
      // Set the request var in the proxy controller so it can send the response
      ourRequest = request;
      // Try to send a 500
      sendFailureResponse(DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR);
      return (DsProxyStatelessTransaction) ourProxy;
    }

    return (DsProxyStatelessTransaction) ourProxy;
  }

  /** Send the cancel to App for processing. */
  public void onCancel(
      DsProxyTransaction proxy, DsProxyServerTransaction trans, DsSipCancelMessage cancel)
      throws DsException {

    Log.debug("Entering onCancel()");

    gotCancel = true;

    try {
      // MEETPASS
      // For Cancel, do we need a new proxyAdaptor
      AppAdaptorInterface adaptor = this.getProxyAdaptor();
      Optional<AppAdaptorInterface> proxyAdaptor = Optional.ofNullable(adaptor);
      if (proxyAdaptor.isPresent()) {
        adaptor.handleRequest(cancel);
      }
    } catch (DhruvaException e) {
      Log.error("Exception while handling cancel message");
      throw new DsException("AppAdaptor:OnCancel", e);
    }
  }

  /** Send the ack to the App layer for processing. */
  public void onAck(DsProxyTransaction proxy, DsProxyServerTransaction trans, DsSipAckMessage ack) {
    Log.debug("Entering onAck() {}");

    try {
      AppAdaptorInterface adaptor = this.getProxyAdaptor();
      Optional<AppAdaptorInterface> proxyAdaptor = Optional.ofNullable(adaptor);
      if (proxyAdaptor.isPresent()) {
        adaptor.handleRequest(ack);
      }
    } catch (DhruvaException e) {
      Log.error("Exception while handling cancel message");
    }
  }

  public void onBestResponse(DsProxyTransaction proxy, DsSipResponse response) {

    Log.debug("Entering onBestResponse() ");

    // If we sent to a route header without the App layer, then we will use the
    // best response that the core has.
    if (usingRouteHeader) {
      Log.debug(
          "We must have sent to a route header or gotten a cancel, responding "
              + " with the core's best response: \n"
              + response.maskAndWrapSIPMessageToSingleLineOutput());

      // Forward the best response upstream
      proxy.respond();
    }
  }

  protected ProxyAdaptorFactoryInterface getAdaptorFactory() {
    return proxyAdaptorFactory;
  }

  public void setAdaptorFactory(ProxyAdaptorFactoryInterface pf) {
    requireNonNull(pf, "proxy adaptor interface cannot be null");
    proxyAdaptorFactory = pf;
  }

  public void setAppInterface(AppInterface session) {
    requireNonNull(session, "app session cannot be null");
    appSession = session;
  }

  public synchronized AppAdaptorInterface getProxyAdaptor() {
    if (adaptor == null) {
      ProxyAdaptorFactoryInterface f = getAdaptorFactory();
      adaptor = f.getProxyAdaptor(this, appSession);
    }
    return adaptor;
  }

  public synchronized void setProxyAdaptor(AppAdaptorInterface proxyAdaptor) {
    requireNonNull(proxyAdaptor, "adaptor must not be null");
    this.adaptor = proxyAdaptor;
  }
}
