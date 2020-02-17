// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;

/** Exception class for key validation exceptions. */
public class DsSipKeyValidationException extends DsSipParserListenerException {
  /** The SIP message where the exception occurred. */
  DsSipMessage m_message;

  /**
   * Constructor which accepts the exception message.
   *
   * @param message the exception message.
   */
  public DsSipKeyValidationException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsSipKeyValidationException(Exception exception) {
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
  public DsSipKeyValidationException(String message, Exception exception) {
    super(message, exception);
  }

  /**
   * Sets the SIP message where the exception occurred.
   *
   * @param msg the SIP message where the exception occurred.
   */
  public void setSipMessage(DsSipMessage msg) {
    m_message = msg;
  }

  /**
   * Gets the exception message.
   *
   * @return the SIP message where the exception occurred.
   */
  public DsSipMessage getSipMessage() {
    return m_message;
  }
}
