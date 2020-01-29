// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/** Represents an exception for no such element. */
public class DsNoSuchElementException extends DsException {
  /**
   * Constructs DsNoSuchElementException object with the specified <code>message</code>.
   *
   * @param message the message explaining the description about the cause of exception
   */
  public DsNoSuchElementException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsNoSuchElementException(Exception exception) {
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
  public DsNoSuchElementException(String message, Exception exception) {
    super(message, exception);
  }
}
