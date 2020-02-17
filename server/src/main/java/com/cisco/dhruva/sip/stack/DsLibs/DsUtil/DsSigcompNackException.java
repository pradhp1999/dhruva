// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.io.*;

/**
 * This exception gets thrown when a cached compressed message is sent and the compartment has
 * received a sigcomp NAK. In this case the client code should recompress the original message.
 */
public class DsSigcompNackException extends IOException {
  /** Used to get the original stack trace and exception message. */
  Exception exception;

  /**
   * Constructor that takes an error message.
   *
   * @param message the error message
   */
  public DsSigcompNackException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsSigcompNackException(Exception exception) {
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
  public DsSigcompNackException(String message, Exception exception) {
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
