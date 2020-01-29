package com.cisco.dhruva.DsLibs.DsUtil;

public class DsTrackingException extends DsException {

  public enum TrackingExceptions {
    NULLPOINTEREXCEPTION("Tracking NullpointerException: "),
    ILLEGALSTATEEXCEPTION("Tracking IllegalStateException: "),
    ILLEGALARGUMENTEXCEPTION("Tracking IllegalArgumentException:"),
    CONCURRENTMODIFICATIONEXCEPTION("Tracking ConcurrentModificationException: "),
    ARRAYINDEXOUTOFBOUNDEXCEPTION("Tracking ArrayIndexOutOfBoundsException: ");

    private String exception;

    TrackingExceptions(String ex) {
      this.exception = ex;
    }
  }

  public DsTrackingException(TrackingExceptions tracking, String message, Exception exception) {
    super(tracking.exception + message, exception);
  }

  public DsTrackingException(TrackingExceptions tracking, String message) {
    super(tracking.exception + message);
  }

  public DsTrackingException(TrackingExceptions tracking, Exception exception) {
    super(tracking.exception, exception);
  }

  public DsTrackingException(Exception exception) {
    super(exception);
  }
}
