// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;

/**
 * This class defines the exception arising in the parsing process. It has been extended to contain
 * more information about what type of parsing error has occurred.
 */
public class DsSipParserException extends DsException {
  /** The value that had an error while being parsed. */
  DsByteString value;
  /** The type that was being parsed. */
  int type;

  /** The list of headers being parsed. */
  LinkedList headerList;

  /**
   * Constructor which accepts an exception, plus additional information for recovery from the
   * exception. This Exceptions message and stack trace will be useded.
   *
   * @param e the exception used to generate this exception
   * @param inType the type of header that was being parsed
   * @param inValue the value that was being parsed
   */
  public DsSipParserException(Exception e, int inType, DsByteString inValue) {
    super(e);

    // call setValue since some processing of the input string may be necessary
    setValue(inValue);
    type = inType;
  }

  /**
   * Constructor which accepts the exception message, plus additional information for recovery from
   * the exception.
   *
   * @param message the exception message
   * @param inType the type of header that was being parsed
   * @param inValue the value that was being parsed
   */
  public DsSipParserException(String message, int inType, DsByteString inValue) {
    super(message);

    // call setValue since some processing of the input string may be necessary
    setValue(inValue);
    type = inType;
  }

  /**
   * Constructor which accepts the exception message.
   *
   * @param message the exception message
   */
  public DsSipParserException(String message) {
    super(message);
    setValue(null);
  }

  /**
   * Constructor which accepts the exception that generated this exception.
   *
   * @param exception the exception message
   */
  public DsSipParserException(Exception exception) {
    super(exception);
    setValue(null);
  }

  /**
   * Retrieves the exception information.
   *
   * @return the exception information
   */
  public String getMessage() {
    String details = "";
    if (type > 0 && type < DsSipMsgParser.HEADER_NAMES.length) {
      details =
          "Type = "
              + type
              + "/"
              + DsSipMsgParser.HEADER_NAMES[type]
              + " - Original String = ["
              + String.valueOf(value)
              + "] - ";
    }

    return (details + super.getMessage());
  }

  /**
   * Sets the value whose parsing was attempted and failed.
   *
   * @param inValue the value that failed to be parsed.
   * @see #getValue
   */
  public void setValue(DsByteString inValue) {
    if (inValue == null) {
      value = new DsByteString("");
      return;
    }

    // when we parse, we add a null to the end of the string to greatly enhance
    // the speed of parsing.  Here, we must remove it, if present.
    if (inValue.length() > 0 && inValue.charAt(inValue.length() - 1) == '\0') {
      inValue = inValue.substring(0, inValue.length() - 1);
    }
    value = inValue;
  }

  /**
   * Sets the type whose parsing was attempted and failed.
   *
   * @param inType the type of header that failed to be parsed
   * @see #getType
   */
  public void setType(int inType) {
    type = inType;
  }

  /**
   * Get the value that had an error during parsing.
   *
   * @return the data that was not parsed due to errors
   * @see #setValue
   */
  public DsByteString getValue() {
    return value;
  }

  /**
   * Get the type if header that had an error during parsing.
   *
   * @return the data that was not parsed due to errors
   * @see #setType
   */
  public int getType() {
    return type;
  }

  /**
   * Set the header list, the value of each header is just the data string that failed parsing. The
   * header is marked as invalid.
   *
   * @param hList the list of headers
   * @see #getHeaderList
   */
  public void setHeaderList(LinkedList hList) {
    headerList = hList;
  }

  /**
   * Get the header list, may be null.
   *
   * @return the list of invalid headers
   * @see #setHeaderList
   */
  public LinkedList getHeaderList() {
    return headerList;
  }
}
