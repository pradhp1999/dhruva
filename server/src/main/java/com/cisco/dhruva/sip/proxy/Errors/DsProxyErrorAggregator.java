package com.cisco.dhruva.sip.proxy.Errors;

import static com.cisco.dhruva.sip.proxy.DsControllerInterface.DESTINATION_UNREACHABLE;
import static com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface.*;

import com.cisco.dhruva.sip.cac.SIPSession;
import com.cisco.dhruva.sip.cac.SIPSessions;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DsProxyErrorAggregator {

  public enum ErrorType {
    PROXY_FAILURE, // Request forward failed (tcp, tls, srv, dns failurs etc)
    XCL_FAILURE, // Route not Found
    SERVER_GROUP_DOWN, // Route Chosen ServerGroup Down
    FAILURE_RESPONSE, // Received Failure Response
    REQUEST_TIMEOUT, // Response not received
    RESPONSE_SENT_FAILURE // Response forward failed (client not reachable, socket closed etc)
  }

  private static final Set<DsProxyErrorListener> LISTENERS = new HashSet<>();

  private List<DsProxyError> proxyErrorList;
  private DsSipRequest request;

  private static Trace Log = Trace.getTrace(DsProxyErrorAggregator.class.getName());

  public DsProxyErrorAggregator() {
    this.proxyErrorList = new LinkedList<>();
  }

  public static void registerListener(DsProxyErrorListener proxyErrorListener) {
    synchronized (LISTENERS) {
      LISTENERS.add(proxyErrorListener);
    }
  }

  public void setDsSipRequest(DsSipRequest request) {
    this.request = request;
  }

  public DsSipRequest getDsSipRequest() {
    return request;
  }

  public List<DsProxyError> getProxyErrorList() {
    return proxyErrorList;
  }

  private void sipRequestCheck() {
    if (request == null) {
      throw new IllegalStateException(
          "DsSipRequest can't be NULL. Don't call any method without setting request.");
    }
  }

  public void onProxyFailure(Throwable error, DsBindingInfo bindingInfo, int errorCode) {
    DsProxyError proxyError = getProxyError(error);
    if (proxyError == null) {
      proxyError = getProxyError(error, bindingInfo, null, errorCode);
    }
    add(proxyError);

    notifyError(proxyError, ErrorType.PROXY_FAILURE);
  }

  /**
   * failure occurs when CP sends/forwards response back to client
   *
   * @param throwable
   * @param errorCode
   */
  public void onResponseFailure(Throwable throwable, DsSipResponse response, int errorCode) {
    DsProxyError proxyError = getProxyError(throwable);
    if (proxyError == null) {
      DsBindingInfo bindingInfo = response != null ? response.getBindingInfo() : null;
      proxyError = getProxyError(throwable, bindingInfo, response, errorCode);
    }
    add(proxyError);

    notifyError(proxyError, ErrorType.RESPONSE_SENT_FAILURE);
  }

  public void onRequestTimeOut(DsBindingInfo bindingInfo) {
    DsProxyError proxyError = new DsProxyRequestTimedOutError(bindingInfo);
    add(proxyError);

    notifyError(proxyError, ErrorType.REQUEST_TIMEOUT);
  }

  public void onFailureResponse(DsSipResponse response) {
    add(new DsProxyFailureResponse(response));
  }

  public void onXclRoutingFailure(DsSipResponse response) {
    DsProxyError proxyError = new DsXclRoutingFailureException(response);
    add(proxyError);

    int statusCode = response.getStatusCode();
    int reason = response.getApplicationReason();

    // log only when response is "no matching algorithm found" | "routing policy has no algorithms?"
    /**
     * xcl failure is called when CP can't find any routeGroup, connection refused & retry of next
     * server group failure<br>
     * CAEvent should be logged only when there is no routeGroup. Other failure logging is done
     * wherever its required.<br>
     * (ie) When connection refused & retry failure happens CA logging is done at
     * DsProxyController#onProxyFailure()
     *
     * <p>Refer dsnrs_route.xcl for more details
     */
    if (statusCode == 404 && reason == REASON_AUTO) {
      notifyError(proxyError, ErrorType.XCL_FAILURE);
    }
  }

  public void onServerGroupDown(String sgName, boolean sgDownAlready) {
    DsProxyError proxyError = new DsProxyServerGroupDownError(sgName);
    add(proxyError);

    if (!sgDownAlready) {
      return;
    }

    notifyError(proxyError, ErrorType.SERVER_GROUP_DOWN);
  }

  private void add(DsProxyError proxyError) {

    sipRequestCheck();

    try {
      proxyErrorList.add(proxyError);
      if (Log.on && Log.isInfoEnabled())
        Log.info("proxyError[added]: " + proxyError.getDescription());
    } catch (Throwable t) {
      Log.error("proxyError exception", t);
    }
  }

  private DsProxyError getProxyError(Throwable throwable) {
    DsProxyError proxyError = null;
    Exception exception = null;

    if (throwable instanceof Exception) {
      exception = (Exception) throwable;
    }

    while (exception != null) {
      if (exception instanceof DsProxyError) {
        proxyError = (DsProxyError) exception;
        break;
      }

      if (exception.getSuppressed() != null && exception.getSuppressed().length > 0) {
        exception = (Exception) exception.getSuppressed()[0];
      } else {
        exception = null;
      }
    }

    return proxyError;
  }

  private DsProxyError getProxyError(
      Throwable throwable, DsBindingInfo bindingInfo, DsSipResponse response, int errorCode) {

    if (DESTINATION_UNREACHABLE == errorCode) {
      return new DsProxyClientUnreachableError(throwable, response, bindingInfo);
    }

    return new DsProxyUnidentifiedError(throwable, bindingInfo);
  }

  private void notifyError(DsProxyError proxyError, ErrorType errorType) {

    SIPSession sipSession = SIPSessions.getActiveSession(request.getCallId().toString());
    DsProxyErrorDetail errorDetail =
        new DsProxyErrorDetail(request, sipSession, errorType, proxyError);

    // iterate all registered listener and notify
    LISTENERS.forEach(listener -> listener.notifyError(errorDetail));
  }
}
