// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsUtil.DsException;

/**
 * Specifies the error condition that the SIP server could not be found while looking for the SIP
 * servers running SIP services.
 */
public class DsSipServerNotFoundException extends DsException {
  /**
   * Constructs this exception with the specified reason description.
   *
   * @param message the message to be passed in the exception
   */
  public DsSipServerNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsSipServerNotFoundException(Exception exception) {
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
  public DsSipServerNotFoundException(String message, Exception exception) {
    super(message, exception);
  }
}
