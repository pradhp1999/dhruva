package com.cisco.dhruva.sip.cac;

import com.cisco.dhruva.sip.DsUtil.EndPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTrackingException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTrackingException.TrackingExceptions;
import com.cisco.dhruva.util.log.Trace;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SIPSessions {
  private static Trace Log = Trace.getTrace(SIPSessions.class.getName());

  private static long RECENT_SESSION_CLEANUP_TIMER = 5; // 5 minute
  private static Timer sessionTimer = new Timer("SIP SESSION TIMER QUEUE");

  protected static AtomicLong totalNumberOfCalls = new AtomicLong(0);
  protected static AtomicLong totalNumberOfFailedCalls = new AtomicLong(0);
  protected static AtomicLong totalNumberOfTimedoutCalls = new AtomicLong(0);

  private static final Object lockObject = new Object();

  private static int DEFAULT_SESSION_COUNT =
      54000; // 300cps with call time of 3min (300 * 60 * 3 = 54000)
  private static final ConcurrentHashMap<String, SIPSession> activeSessions =
      new ConcurrentHashMap<String, SIPSession>(DEFAULT_SESSION_COUNT);
  private static final ConcurrentHashMap<String, SIPSession> recentSessions =
      new ConcurrentHashMap<String, SIPSession>(DEFAULT_SESSION_COUNT);

  private static boolean tracKActiveSessions = false;
  private static boolean tracKRecentSessions = false;
  private static boolean notifyFailedSessionsOnly = true;

  private static final ConcurrentHashMap<EndPoint, HashMap<String, SIPSession>>
      activeSessionsByDestination =
          new ConcurrentHashMap<EndPoint, HashMap<String, SIPSession>>(DEFAULT_SESSION_COUNT);
  private static final ConcurrentHashMap<EndPoint, HashMap<String, SIPSession>>
      expiredSessionsByDestination =
          new ConcurrentHashMap<EndPoint, HashMap<String, SIPSession>>(DEFAULT_SESSION_COUNT);

  private static int MIN_SESSION_EXPIRATION = 5; // 5min
  private static int sessionTimeout = 720; // 12 hrs
  private static long DEFAULT_SESSION_CLEANUP_INTERVAL = 60; // 1hr
  private static long SESSION_CLEANUP_INTERVAL = DEFAULT_SESSION_CLEANUP_INTERVAL;

  private static ActiveSIPSessionExpirationThread activeSessionExpirationThread = null;

  // REFACTOR

  //    private static SIPSessionsMBeanImpl mBean;
  //
  //    static {
  //        try {
  //            mBean = SIPSessionsMBeanImpl.getInstance();
  //            ServerGlobalStateWrapper.setUsageLimitInterface(new CACLimit());
  //        }
  //        catch (Throwable t) {
  //            Log.error("SIPSessionsMBeanImpl ERROR", t);
  //        }
  //    }

  /**
   * Sets the timer value after which a unterminated sip session is considered expired and removed
   * from the active list.
   *
   * @param sessionTimeout session timeout in minutes
   * @throws Exception upon internal errors
   */
  public static void setSessionTimeout(int sessionTimeout) throws Exception {
    if (sessionTimeout < MIN_SESSION_EXPIRATION) {
      throw new Exception(
          "session timeout value should not be less than " + MIN_SESSION_EXPIRATION);
    }
    SIPSessions.sessionTimeout = sessionTimeout;
    if (SIPSessions.sessionTimeout < DEFAULT_SESSION_CLEANUP_INTERVAL) {
      SESSION_CLEANUP_INTERVAL = SIPSessions.sessionTimeout / 2;
    } else {
      SESSION_CLEANUP_INTERVAL = DEFAULT_SESSION_CLEANUP_INTERVAL;
    }

    if (activeSessionExpirationThread != null) {
      synchronized (activeSessionExpirationThread.threadLock) {
        activeSessionExpirationThread.threadLock.notifyAll();
      }
    }
  }

  public static int getSessionTimeout() {
    return sessionTimeout;
  }

  public static void setTrackSIPSessions(boolean flag) {
    /*
    if(!tracKActiveSessions && flag) {
        resetAllSessionCounts();
    }
    */
    tracKActiveSessions = flag;
  }

  public static boolean getTrackSIPSessions() {
    return tracKActiveSessions;
  }

  public static void setNotifyFailedSessionsOnly(boolean flag) {
    notifyFailedSessionsOnly = flag;
  }

  public static boolean getNotifyFailedSessionsOnly() {
    return notifyFailedSessionsOnly;
  }

  public static SIPSession createSession(DsSipRequest request) {

    if (activeSessionExpirationThread == null || !activeSessionExpirationThread.isAlive()) {
      activeSessionExpirationThread =
          new ActiveSIPSessionExpirationThread("ACTIVE SESSIONS CLEANUP");
      activeSessionExpirationThread.start();
    }
    SIPSession session = new SIPSession(request, sessionTimeout);
    try {
      activeSessions.put(session.getSessionID(), session);
      if (request.getMethodID() != DsSipRequest.OPTIONS) {
        totalNumberOfCalls.incrementAndGet();
      }
    } catch (NullPointerException e) {
      Log.warn(e);
    }
    return session;
  }

  protected static void addActiveSessionByDestination(
      EndPoint sipDestination, SIPSession sipSession) {
    HashMap<String, SIPSession> activeSessions =
        activeSessionsByDestination.computeIfAbsent(
            sipDestination, k -> new HashMap<String, SIPSession>(DEFAULT_SESSION_COUNT));
    activeSessionsByDestination.computeIfPresent(
        sipDestination,
        (key, val) -> {
          val.put(sipSession.getSessionID(), sipSession);
          return val;
        });
  }

  public static void removeSession(String callId) {
    Log.debug("Removing session for callid " + callId);
    SIPSession sipSession = SIPSessions.getActiveSession(callId);
    if (sipSession != null) {
      sipSession.terminated();
    }
  }

  protected static SIPSession removeActiveSession(String sessionID) throws DsTrackingException {
    SIPSession session;
    try {
      session = activeSessions.remove(sessionID);
    } catch (NullPointerException e) {
      Log.warn(e);
      throw new DsTrackingException(
          TrackingExceptions.NULLPOINTEREXCEPTION,
          "Session is already removed for session ID " + sessionID);
    }

    // remove session from destination
    Iterator<EndPoint> itr = activeSessionsByDestination.keySet().iterator();
    while (itr.hasNext()) {
      EndPoint sipDestination = (EndPoint) itr.next();
      try {
        HashMap<String, SIPSession> activeSessions =
            activeSessionsByDestination.computeIfPresent(
                sipDestination,
                (key, val) -> {
                  val.remove(sessionID);
                  return val;
                });
        if (activeSessions != null && activeSessions.size() == 0) {
          activeSessionsByDestination.remove(sipDestination);
        }
      } catch (Exception e) {
        Log.warn("RemoveActiveSession: Exception while retrieving active sessions", e);
      }
    }

    if (session.getSessionState() == SessionStateType.FAILED) {
      SIPSessions.totalNumberOfFailedCalls.incrementAndGet();
      // notify failed sip sessions via mbean about this session
      // REFACTOR
      //      if (notifyFailedSessionsOnly) {
      //        mBean.notify(session);
      //      }
    } else if (session.getSessionState() == SessionStateType.TIMED_OUT) {
      SIPSessions.totalNumberOfTimedoutCalls.incrementAndGet();
      // notify timedout sip sessions via mbean about this session
      //      if (notifyFailedSessionsOnly) {
      //        mBean.notify(session);
      //      }
    }

    // notify all sip sessions via mbean about this session
    //    if (!notifyFailedSessionsOnly) {
    //      mBean.notify(session);
    //    }

    // recent session tracking
    if (tracKRecentSessions) {
      recentSessions.put(sessionID, session);
      expiredSessionsByDestination.computeIfAbsent(
          session.getLastDestination(),
          k -> new HashMap<String, SIPSession>(DEFAULT_SESSION_COUNT));
      activeSessionsByDestination.computeIfPresent(
          session.getLastDestination(),
          (key, val) -> {
            val.put(sessionID, session);
            return val;
          });
      sessionTimer.schedule(
          new SIPSessionCleanupTask(sessionID),
          new Date(System.currentTimeMillis() + (RECENT_SESSION_CLEANUP_TIMER * 60 * 1000)));
    }
    return session;
  }

  // MEETPASS NULL Pointer
  protected static SIPSession removeRecentSession(String sessionID) {
    SIPSession session = null;
    try {
      session = recentSessions.remove(sessionID);
    } catch (NullPointerException e) {
      Log.warn(e);
      return null;
    }
    if (session != null) {
      EndPoint sipDestination = session.getLastDestination();
      try {
        HashMap<String, SIPSession> expSessions =
            expiredSessionsByDestination.computeIfPresent(
                sipDestination,
                (key, val) -> {
                  val.remove(sessionID);
                  return val;
                });
        if (expSessions != null && expSessions.size() == 0) {
          expiredSessionsByDestination.remove(sipDestination);
        }
      } catch (Exception e) {
        Log.warn(e);
      }
    }
    return session;
  }

  public static long getTotalNumberOfSessions() {
    return totalNumberOfCalls.get();
  }

  public static long getTotalNumberOfFailedSessions() {
    return totalNumberOfFailedCalls.get();
  }

  public static long getTotalNumberOfTimedoutSessions() {
    return totalNumberOfTimedoutCalls.get();
  }

  public static void resetTotalSessionCounts() {
    synchronized (lockObject) {
      // reset to active sessions count
      totalNumberOfCalls.set(activeSessions.size());
      totalNumberOfFailedCalls.set(0);
      // totalNumberOfTimedoutCalls.set(0);
      // REFACTOR

      //            ServerGroupRepository sgr =
      // CallProcessingConfig.getInstance().getServerGroupRepository();
      //            if(sgr != null) {
      //                for(Object o : sgr.getServerPool().values()) {
      //                    ServerGlobalStateWrapper wrapper = (ServerGlobalStateWrapper) o;
      //                    wrapper.resetTotalCounts(getActiveSessionCountByEndPoint(wrapper));
      //                }
      //            }
    }
  }

  public static void resetAllSessionCounts() {
    synchronized (lockObject) {
      resetActiveSessions();
      // reset to 0 since active sessions have been reset
      totalNumberOfCalls.set(0);
      totalNumberOfFailedCalls.set(0);
      totalNumberOfTimedoutCalls.set(0);

      // REFACTOR
      //            ServerGroupRepository sgr =
      // CallProcessingConfig.getInstance().getServerGroupRepository();
      //            if(sgr != null) {
      //                for(Object o : sgr.getServerPool().values()) {
      //                    ServerGlobalStateWrapper wrapper = (ServerGlobalStateWrapper) o;
      //                    wrapper.resetTotalCounts(0);
      //                }
      //            }
    }
  }

  public static SIPSession getActiveSession(String sessionID) {
    SIPSession s = null;
    try {
      s = activeSessions.get(sessionID);
    } catch (NullPointerException e) {
      Log.warn("Exception in retrieving active session", e);
    }
    return s;
  }

  public static List<SIPSession> getActiveSessions() {
    return new LinkedList<SIPSession>(activeSessions.values());
  }

  public static int getActiveSessionCount() {
    synchronized (lockObject) {
      return activeSessions.size();
    }
  }

  public static void resetActiveSessions() {
    synchronized (lockObject) {
      totalNumberOfTimedoutCalls.set(0);
      for (SIPSession session : new HashSet<SIPSession>(activeSessions.values())) {
        session.forcedRemove();
      }
    }
  }

  public static List<SIPSession> getActiveSessionsByEndPoint(EndPoint sipDestination) {
    HashMap<String, SIPSession> sipSessions = activeSessionsByDestination.get(sipDestination);
    if (sipSessions != null) {
      return new LinkedList<SIPSession>(sipSessions.values());
    }
    return null;
  }

  public static int getActiveSessionCountByEndPoint(EndPoint sipDestination) {
    HashMap<String, SIPSession> sipSessions = activeSessionsByDestination.get(sipDestination);
    if (sipSessions != null) {
      return sipSessions.size();
    }
    return 0;
  }

  public static void resetActiveSessionsByEndPoint(EndPoint endPoint) {
    synchronized (lockObject) {
      HashMap<String, SIPSession> sipSessions = activeSessionsByDestination.get(endPoint);
      if (sipSessions != null) {
        for (SIPSession session : new HashSet<SIPSession>(sipSessions.values())) {
          session.forcedRemove();
        }
      }
    }
  }

  // REFACTOR
  public static long getTotalSessionCountByEndPoint(EndPoint sipDestination) {
    //        ServerGroupRepository sgr =
    // CallProcessingConfig.getInstance().getServerGroupRepository();
    //        if(sgr != null) {
    //            ServerGlobalStateWrapper wrapper = (ServerGlobalStateWrapper) sgr.getServerPool()
    //                    .get(sipDestination.getHashKey());
    //
    //            if (wrapper != null) {
    //                return wrapper.getTotalUsageCount();
    //            }
    //            else {
    //                // log ???
    //            }
    //        }
    return 0;
  }

  // REFACTOR
  public static long getTotalFailedSessionCountByEndPoint(EndPoint sipDestination) {
    //        ServerGroupRepository sgr =
    // CallProcessingConfig.getInstance().getServerGroupRepository();
    //        if(sgr != null) {
    //            ServerGlobalStateWrapper wrapper = (ServerGlobalStateWrapper) sgr.getServerPool()
    //                    .get(sipDestination.getHashKey());
    //
    //            if (wrapper != null) {
    //                return wrapper.getTotalFailureCount();
    //            }
    //            else {
    //                // log ???
    //            }
    //        }
    return 0;
  }

  // REFACTOR
  public static void resetTotalSessionCountsByEndPoint(EndPoint sipDestination) {
    //        ServerGroupRepository sgr =
    // CallProcessingConfig.getInstance().getServerGroupRepository();
    //        if(sgr != null) {
    //            ServerGlobalStateWrapper wrapper = (ServerGlobalStateWrapper) sgr.getServerPool()
    //                    .get(sipDestination.getHashKey());
    //
    //            if (wrapper != null) {
    //                //reset to active session count for this endpoint
    //                wrapper.resetTotalCounts(getActiveSessionCountByEndPoint(sipDestination));
    //            }
    //            else {
    //                // log ???
    //            }
    //        }
  }

  public static void resetAllSessionCountsByEndPoint(EndPoint sipDestination) {
    //        synchronized (lockObject) {
    //            resetActiveSessionsByEndPoint(sipDestination);
    //
    //            ServerGroupRepository sgr =
    // CallProcessingConfig.getInstance().getServerGroupRepository();
    //            if(sgr != null) {
    //                ServerGlobalStateWrapper wrapper = (ServerGlobalStateWrapper)
    // sgr.getServerPool()
    //                        .get(sipDestination.getHashKey());
    //
    //                if (wrapper != null) {
    //                    //reset to 0 since active sessions are also reset;
    //                    wrapper.resetTotalCounts(0);
    //                }
    //                else {
    //                    // log ???
    //                }
    //            }
    //        }
  }

  // ############# Recent Sessions tracking ########################
  public static void setTrackRecentSIPSessions(boolean flag) {
    tracKRecentSessions = flag;
  }

  public static boolean getTrackRecentSIPSessions() {
    return tracKRecentSessions;
  }

  public static List<SIPSession> getRecentSessions() {
    synchronized (lockObject) {
      return new LinkedList<SIPSession>(recentSessions.values());
    }
  }

  public static int getRecentSessionsCount() {
    synchronized (lockObject) {
      return recentSessions.size();
    }
  }

  public static List<SIPSession> getRecentSessionsByEndPoint(EndPoint sipDestination) {
    HashMap<String, SIPSession> sipSessions = expiredSessionsByDestination.get(sipDestination);
    if (sipSessions != null) {
      return new LinkedList<SIPSession>(sipSessions.values());
    }
    return null;
  }

  public static int getRecentSessionsCountByEndPoint(EndPoint sipDestination) {
    HashMap<String, SIPSession> sipSessions = expiredSessionsByDestination.get(sipDestination);
    if (sipSessions != null) {
      return sipSessions.size();
    }
    return 0;
  }
  // #############################################

  private static class SIPSessionCleanupTask extends TimerTask {
    private String sessionID;

    SIPSessionCleanupTask(String sessionID) {
      this.sessionID = sessionID;
    }

    @Override
    public void run() {
      SIPSessions.removeRecentSession(sessionID);
    }
  }

  private static class ActiveSIPSessionExpirationThread extends Thread {
    public Object threadLock = new Object();

    ActiveSIPSessionExpirationThread(String name) {
      super(name);
    }

    @Override
    public void run() {
      while (true) {
        try {
          synchronized (lockObject) {
            long currentTime = System.currentTimeMillis();
            for (SIPSession session : new HashSet<SIPSession>(activeSessions.values())) {
              if (session.getSessionExpirationTime() <= currentTime) {
                session.timedout();
              }
            }
          }
        } catch (Throwable t) {
          Log.warn("ERROR in Active SIP Session expiration thread", t);
        }

        try {
          synchronized (threadLock) {
            threadLock.wait(SESSION_CLEANUP_INTERVAL * 60 * 1000);
          }
        } catch (InterruptedException e) {
          Log.warn(e);
        }
      }
    }
  }
}
