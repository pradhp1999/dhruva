package com.cisco.dhruva.router;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.springframework.stereotype.Component;

@Singleton
@Component
public class AppRouterManager {

  private AppRouter appRouter;
  private Track track;

  public AppRouter getAppRouter() {
    return appRouter;
  }

  public void setAppRouter(AppRouter router) {
    this.appRouter = router;
  }

  public static class Tallies {

    public int getMissCount() {
      return missCount;
    }

    public void setMissCount(final int missCount) {
      this.missCount = missCount;
    }

    public int getErrCount() {
      return errCount;
    }

    public void setErrCount(final int errCount) {
      this.errCount = errCount;
    }

    public int getStaticRouteCount() {
      return staticRouteCount;
    }

    public void setStaticRouteCount(final int staticRouteCount) {
      this.staticRouteCount = staticRouteCount;
    }

    public int missCount;
    public int errCount;
    public int staticRouteCount;
  }

  public static class Track {

    long time;
    RouteType routeType;
    String fqdn;
    RouteResultDetails resultDetails;
    RouteType type = RouteType.REQURI;

    public Track() {
      start();
    }

    public String toString() {
      return fqdn + " - " + type;
    }

    public void setRouteType(final RouteType routeType, final String fqdn) {
      this.routeType = routeType;
      this.fqdn = fqdn;
    }

    public void setType(final RouteType type) {
      this.type = type;
    }

    public RouteType getRouteType() {
      return type;
    }

    public void setResultDetails(final RouteResultDetails resultDetails) {
      this.resultDetails = resultDetails;
    }

    public RouteResultDetails getResultDetails() {
      return resultDetails;
    }

    public final void start() {
      time = System.currentTimeMillis();
    }

    public final void end() {
      time = System.currentTimeMillis() - time;
    }
  }

  private final Map<String, Tallies> routeMap = new HashMap<String, Tallies>();

  public static Track getTrack() {
    return new Track();
  }

  public int getTotalDnsCount() {
    return totalDnsCount;
  }

  public long getAverageDnsTime() {
    if (totalDnsCount == 0) {
      return 0;
    }
    return totalDnsTime / totalDnsCount;
  }

  private int totalDnsCount;
  private long totalDnsTime;

  private int totalReqURICount;

  public void saveTrack(final Track t) {

    t.end();

    synchronized (this) {
      Map<String, Tallies> map = routeMap;
      switch (t.routeType) {
        case DNS:
          totalDnsCount++;
          totalDnsTime += t.time;
        case REQURI:
          totalReqURICount++;
      }
      Tallies tallies = map.get(t.fqdn);
      if (tallies == null) {
        tallies = new Tallies();
        map.put((t.fqdn == null) ? "null" : t.fqdn, tallies);
      }
      incTally(t, tallies);
    }
  }

  private static void incTally(final Track t, final Tallies tallies) {
    RouteResultDetails result = t.getResultDetails();
    switch (result) {
      case ERROR:
        tallies.errCount++;
        break;
      case ROUTE_URI_SUCCESS:
        tallies.staticRouteCount++;
        break;
      default:
        break;
    }
  }
}
