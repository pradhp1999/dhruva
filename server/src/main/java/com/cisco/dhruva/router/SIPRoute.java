package com.cisco.dhruva.router;

public class SIPRoute<T> implements RouteResult<T> {

  private int responseCode;
  private final T destination;

  public SIPRoute(T destination) {
    this.destination = destination;
  }

  @Override
  public T getDestination() {
    return destination;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(final int rc) {
    this.responseCode = rc;
  }

  public String toLocationJSONString() {
    return "{\"location\": \"" + destination + "\" }";
  }

  public RouteResult<T> match(String path) {
    if (ReqURIRoute.match(path)) {
      return () -> destination;
    } else {
      return null;
    }
  }
}
