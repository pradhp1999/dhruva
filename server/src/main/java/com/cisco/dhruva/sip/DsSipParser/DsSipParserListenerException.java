package com.cisco.dhruva.sip.DsSipParser;

import com.cisco.dhruva.sip.DsUtil.DsException;

/** Used to propagate exceptions from the listeners back through the parser. */
public class DsSipParserListenerException extends DsException {
  /**
   * Constructor which accepts the exception message.
   *
   * @param message the exception message
   */
  public DsSipParserListenerException(String message) {
    super(message);
  }

  /**
   * Constructor which accepts the original exception. This exception's <code>printStackTrace()
   * </code> will be used. <code>exception.toString()</code> will be used for this exceptions
   * message.
   *
   * @param exception the exception that was re-cast to this exception
   */
  public DsSipParserListenerException(Exception exception) {
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
  public DsSipParserListenerException(String message, Exception exception) {
    super(message, exception);
  }
}
