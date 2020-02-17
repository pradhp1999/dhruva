// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.io.*;
import javax.net.ssl.SSLException;

/**
 * Defines the exception for the error conditions while communicating through SSL sockets in case of
 * TLS transport type.
 */
public class DsSSLException extends SSLException {
  /** Used to get the original stack trace and exception message. */
  Exception exception;

  /**
   * Constructs an SSL exception object with the specified reason message <code>message</code>.
   *
   * @param message the description explaining the cause of this exception
   */
  public DsSSLException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsSSLException(Exception exception) {
    super(exception.toString());
    this.exception = exception;
  }

  /**
   * Constructor which accepts the original exception and a message. This exception's <code>
   * printStackTrace()</code> will be used. <code>message</code> will be used for this exceptions
   * message.
   *
   * @param message the exception message
   * @param exception the exception that was re-cast to this exception
   */
  public DsSSLException(String message, Exception exception) {
    super(message);
    this.exception = exception;
  }

  public void printStackTrace() {
    if (exception != null) {
      exception.printStackTrace();
    } else {
      super.printStackTrace();
    }
  }

  public void printStackTrace(PrintStream s) {
    if (exception != null) {
      exception.printStackTrace(s);
    } else {
      super.printStackTrace(s);
    }
  }

  public void printStackTrace(PrintWriter s) {
    if (exception != null) {
      exception.printStackTrace(s);
    } else {
      super.printStackTrace(s);
    }
  }
}
