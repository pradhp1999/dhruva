package com.cisco.dhruva.util.cac;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.DsLibs.DsUtil.DsTrackingException;
import com.cisco.dhruva.DsLibs.DsUtil.EndPoint;
import com.cisco.dhruva.util.log.Trace;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;

/** class defines the SIP Call Session */
public class SIPSession {
  private static final String SESSION_CREATED = "Created at ";
  private static final String SESSION_PROVISIONAL = "Provisional, response=";
  private static final String SESSION_ACCEPTED = "Accepted, response=";
  private static final String SESSION_REDIRECTED = "Redirected, response=";
  private static final String SESSION_FAILED = "Failed, response=";
  private static final String SESSION_TERMINATED = "Terminated at ";
  private static final String SESSION_ACTIVITY = "Activity=";
  private static final String SESSION_TIMEDOUT = "Timed Out at ";
  public static final String SESSION_TIMEOUT = "Time Out";
  public static final String SESSION_FORCED_REMOVE = "Forced Remove";
  public static final String LICENSE_EXCEEDED = "License Exceeded";
  private static final String DESTINATION = "Destination=";
  private static final String SERVERGROUP = "ServerGroup=";
  private static final String ROUTEPOLICY = "RoutePolicy=";
  private static final String ROUTE = "Route=";
  private static final String OB = " (";
  private static final String CB = ")";
  private String sessionID;
  private String caller;
  private String callee;
  private SessionStateType sessionState;
  private String reason;
  private EndPoint lastDestination;
  private LinkedList<String> sessionHistory;
  private Date sessionSetupTime;
  private long sessionExpirationTime;
  private long sessionTimeout;
  private boolean licenseExceeded = false;
  private ConcurrentHashMap<String, String> normalizationVarMap;
  private static final int STR_MAX_LENGTH = 512;
  private static final Trace Log = Trace.getTrace(SIPSession.class.getName());
  public SIPSessionID sessionAttrib;
  // REFACTOR
  // private DsProxyErrorAggregator proxyErrorAggregator;
  //  private CallAnalyzerInfo callAnalyzerInfo;

  protected SIPSession(DsSipRequest request, long sessionTimeout) {
    sessionState = SessionStateType.INITIAL;
    sessionSetupTime = new Date();
    sessionID = sanitise(request.getCallId().toString());
    callee = sanitise(request.getURI().toString());
    caller = sanitise(request.getFromHeader().getValue().toString());
    sessionHistory = new LinkedList<String>();
    sessionHistory.add(SESSION_CREATED + sessionSetupTime);
    this.sessionTimeout = sessionTimeout;
    sessionExpirationTime = sessionSetupTime.getTime() + (this.sessionTimeout * 60 * 1000);
    sessionAttrib = new SIPSessionID();
    sessionAttrib.createSessionUuid(request);
    sessionAttrib.uacNetwork = request.getNetwork();
  }

  /**
   * Trim the given string to size of {@value #STR_MAX_LENGTH} if the original size is great than
   * that
   *
   * @param inputStr the input string
   * @return the trimmed string
   */
  private String sanitise(final String inputStr) {
    String sanStr = null;
    if (inputStr == null) {
      sanStr = "";
    } else if (inputStr.length() > SIPSession.STR_MAX_LENGTH) {
      sanStr = inputStr.substring(0, SIPSession.STR_MAX_LENGTH);
    } else {
      sanStr = inputStr;
    }
    return sanStr;
  }

  public void provisional(DsSipResponse response) throws IllegalStateException {
    if (sessionState.getStateAsInt() >= SessionStateType.FAILED.getStateAsInt()) {
      throw new IllegalStateException("SIP Session is closed");
    }

    if (sessionState.getStateAsInt() > SessionStateType.PROVISIONAL.getStateAsInt()) {
      throw new IllegalStateException("Illegal session state");
    }

    sessionState = SessionStateType.PROVISIONAL;
    sessionHistory.add(SESSION_PROVISIONAL + response.getStatusCode());
  }

  public void accepted(DsSipResponse response) throws IllegalStateException {
    if (sessionState.getStateAsInt() >= SessionStateType.FAILED.getStateAsInt()) {
      throw new IllegalStateException("SIP Session is closed");
    }

    if (sessionState.getStateAsInt() >= SessionStateType.ACCEPTED.getStateAsInt()) {
      throw new IllegalStateException("Illegal session state");
    }

    sessionState = SessionStateType.ACCEPTED;
    sessionHistory.add(SESSION_ACCEPTED + response.getStatusCode());

    if (SIPSessions.getTrackSIPSessions()) {
      touch();
    } else {
      remove();
    }
  }

  public void redirected(DsSipResponse response) throws IllegalStateException {
    if (sessionState.getStateAsInt() >= SessionStateType.FAILED.getStateAsInt()) {
      throw new IllegalStateException("SIP Session is closed");
    }

    if (sessionState.getStateAsInt() >= SessionStateType.ACCEPTED.getStateAsInt()) {
      throw new IllegalStateException("Illegal session state");
    }

    sessionState = SessionStateType.REDIRECTED;
    sessionHistory.add(SESSION_REDIRECTED + response.getStatusCode());
    remove();
  }

  public void timedout() throws IllegalStateException {
    if (sessionState.getStateAsInt() >= SessionStateType.TIMED_OUT.getStateAsInt()) {
      throw new IllegalStateException("SIP Session is closed");
    }

    sessionState = SessionStateType.TIMED_OUT;
    sessionHistory.add(SESSION_TIMEDOUT + new Date());
    reason = SESSION_TIMEOUT;
    remove();

    if (Trace.on && Log.isEnabled(Level.DEBUG))
      Log.debug("SIP Session has timed out: " + this.getSessionID());
  }

  public void licenseFailure() throws IllegalStateException {
    licenseExceeded = true;
  }

  public void failed(DsSipResponse response) throws IllegalStateException {
    failed(Integer.toString(response.getStatusCode()));
  }

  private void failed(String reasonPhrase) throws IllegalStateException {

    if (sessionState.getStateAsInt() >= SessionStateType.FAILED.getStateAsInt()) {
      throw new IllegalStateException("SIP Session is closed");
    }

    sessionState = SessionStateType.FAILED;
    if (licenseExceeded) {
      reason = LICENSE_EXCEEDED;
      reasonPhrase =
          new StringBuffer(reasonPhrase).append(OB).append(LICENSE_EXCEEDED).append(CB).toString();
    } else {
      reason = reasonPhrase;
    }
    sessionHistory.add(SESSION_FAILED + reasonPhrase);
    remove();
  }

  public void terminated() throws IllegalStateException {
    if (sessionState == SessionStateType.TERMINATED) {
      throw new IllegalStateException("SIP Session is closed");
    }

    sessionState = SessionStateType.TERMINATED;
    sessionHistory.add(SESSION_TERMINATED + new Date());
    remove();
  }

  public void forcedRemove() {
    sessionState = SessionStateType.FORCED_REMOVE;
    reason = SESSION_FORCED_REMOVE;
    sessionHistory.add(SESSION_FORCED_REMOVE);
    remove();
  }

  public void setServerGroup(String serverGroup) {
    if (serverGroup == null) {
      throw new IllegalArgumentException("servergroup cannot be null");
    }
    sessionHistory.add(SERVERGROUP + serverGroup);
  }

  public void setRoutePolicy(String routePolicy) {
    if (routePolicy == null) {
      throw new IllegalArgumentException("route policy cannot be null");
    }
    sessionHistory.add(ROUTEPOLICY + routePolicy);
  }

  public void setRoute(String route) {
    if (route == null) {
      throw new IllegalArgumentException("route cannot be null");
    }
    sessionHistory.add(ROUTE + route);
  }

  public void setDestination(EndPoint sipDestination)
      throws IllegalArgumentException, IllegalStateException {
    if (sipDestination == null) {
      throw new IllegalArgumentException("SIP Destination cannot be null");
    }
    Log.debug(
        "Inside setDestination for "
            + sipDestination
            + " Session state is  "
            + sessionState.getStateAsStr());
    if (sessionState.getStateAsInt() >= SessionStateType.ACCEPTED.getStateAsInt()) {
      // throw new IllegalStateException("SIP Session is closed");
    }
    SIPSessions.addActiveSessionByDestination(sipDestination, this);
    sessionHistory.add(DESTINATION + sipDestination.getHashKey());
    lastDestination = sipDestination;
  }

  public EndPoint getLastDestination() {
    return lastDestination;
  }

  public SessionStateType getSessionState() {
    return sessionState;
  }

  public String getSessionID() {
    return sessionID;
  }

  public Date getSetupTime() {
    return sessionSetupTime;
  }

  public String getReason() {
    return reason;
  }

  public List<String> getSessionHistory() {
    return sessionHistory;
  }

  public String getCaller() {
    return caller;
  }

  public String getCallee() {
    return callee;
  }

  public long getSessionExpirationTime() {
    return sessionExpirationTime;
  }

  public void activity(DsSipRequest request) {
    sessionHistory.add(SESSION_ACTIVITY + request.getMethod().toString());
    touch();
  }

  public ConcurrentHashMap<String, String> getNormalizationVarMap() {
    if (normalizationVarMap == null) {
      normalizationVarMap = new ConcurrentHashMap<String, String>();
    }
    return normalizationVarMap;
  }

  public void setNormalizationVarMap(ConcurrentHashMap<String, String> normalizationVarMap) {
    this.normalizationVarMap = normalizationVarMap;
  }

  private void remove() {
    Log.debug("Removing the sip session " + sessionID);
    try {
      SIPSessions.removeActiveSession(sessionID);
    } catch (DsTrackingException e) {
      Log.warn("Error in removing SIP Active session" + sessionID, e);
    }
  }

  public void touch() {
    sessionExpirationTime = System.currentTimeMillis() + (this.sessionTimeout * 60 * 1000);
  }
  // REFACTOR
  //  public DsProxyErrorAggregator getProxyErrorAggregator() {
  //    return proxyErrorAggregator;
  //  }
  //
  //  public void setProxyErrorAggregator(DsProxyErrorAggregator proxyErrorAggregator) {
  //    this.proxyErrorAggregator = proxyErrorAggregator;
  //  }

  // REFACTOR
  //  public CallAnalyzerInfo getCallAnalyzerInfo() {
  //    return callAnalyzerInfo;
  //  }
  //
  //  public void setCallAnalyzerInfo(CallAnalyzerInfo callAnalyzerInfo) {
  //    this.callAnalyzerInfo = callAnalyzerInfo;
  //  }
}
