// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This class represents state machine exception that might be raised during an error condition in
 * the state machine operations.
 */
public class DsStateMachineException extends DsException {
  /**
   * Constructor which accepts the exception message.
   *
   * @param message the exception message
   */
  public DsStateMachineException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsStateMachineException(Exception exception) {
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
  public DsStateMachineException(String message, Exception exception) {
    super(message, exception);
  }
}
