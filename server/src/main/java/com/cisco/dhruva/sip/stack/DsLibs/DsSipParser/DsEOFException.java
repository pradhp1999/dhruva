// ////////////////////////////////////////////////////////////////
// FILENAME:    DsEOFException.java
//
// MODULE:      DsSipParser
//
// COPYRIGHT:
// ============== copyright 2004 Cisco Systems, Inc. =================
// ==================== all rights reserved =======================
// ////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

import java.io.*;

/**
 * This class defines the end-of-stream exception arising in message framing. It extends
 * java.io.EOFException by providing error code to further define the scenario of the exception.
 */
public class DsEOFException extends EOFException {
  /** Constructor which accepts the exception that generated this exception. */
  public DsEOFException() {
    super();
  }

  /**
   * Constructor which accepts the exception message.
   *
   * @param message the exception message
   */
  public DsEOFException(String message) {
    super(message);
  }

  /** Constructor which accepts the exception that generated this exception. */
  public DsEOFException(byte errorCode) {
    super();
    this.errorCode = errorCode;
  }

  /** Javadoc inherited. */
  public String getMessage() {
    return errorCodeToString(errorCode);
  }

  /**
   * Returns the error code for this exception.
   *
   * @return the error code for this exception.
   */
  public byte getErrorCode() {
    return errorCode;
  }

  /**
   * Sets the error code for this exception.
   *
   * @param errorCode the error code for this exception.
   */
  public void setErrorCode(byte errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * Convert error code to string.
   *
   * @param errorCode error code.
   * @return a string corresponding to the error code.
   */
  public static String errorCodeToString(byte errorCode) {
    switch (errorCode) {
      case EC_FIRST_READ:
        return "End of stream exception during the first byte read when framing SIP messages";
      default:
        return "Generic end-of-stream exception";
    }
  }

  /** Error code */
  private byte errorCode = EC_GENERIC;

  /** Generic EOF exception */
  public static final byte EC_GENERIC = 0;
  /** EOF exception encountered during the first blocking read() and nothing has been read yet. */
  public static final byte EC_FIRST_READ = 1;
}
