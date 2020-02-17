// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/** Exception class for message version validation exceptions. */
public class DsSipVersionValidationException extends DsSipMessageValidationException {
  /**
   * Constructor which accepts the exception message.
   *
   * @param message the exception message.
   */
  public DsSipVersionValidationException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsSipVersionValidationException(Exception exception) {
    super(exception);
  }

  /**
   * Constructor which accepts the original exception and a message. This exception's <code>
   * printStackTrace()</code> will be used. <code>message</code> will be used for this exceptions
   * message.
   *
   * @param message the exception message
   * @param exception the exception that was re-cast to this exception
   */
  public DsSipVersionValidationException(String message, Exception exception) {
    super(message, exception);
  }
}
