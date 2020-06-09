package com.cisco.dhruva.router;

public class ReqURIRoute<T> {
  private final T destination;
  private final String reqURI;

  public ReqURIRoute(String reqURI, T destination) {
    this.reqURI = reqURI;
    this.destination = destination;
  }

  public static boolean match(String reqURI) {
    // MeetPass
    // Need to check validity of req uri
    // If IP is Dhruva IP?
    // Format is not proper, RegEX
    return true;
  }

  public T getDestination() {
    return destination;
  }

  public String getReqURI() {
    return this.reqURI;
  }
}
