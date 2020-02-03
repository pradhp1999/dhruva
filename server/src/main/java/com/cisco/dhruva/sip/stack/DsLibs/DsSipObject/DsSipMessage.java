/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

public class DsSipMessage {

  static final String REGEX_CRYPT_LINE = "(a=crypto.*inline:).*";
  static final String REGEX_CRYPT_MASK = "$1********MASKED***************";

  /* constants used for wrapping message output */
  private static final String NEW_LINE_REGEX_PATTERN = "[\\t\\n\\r]+";
  private static final String NEW_LINE_REPLACEMENT_DELIMITER = " ";
  private static final String BEGIN_SIP_MESSAGE = "BEGIN SIP MESSAGE:";
  private static final String END_SIP_MESSAGE = "END SIP MESSAGE:";
  /**
   * Returns a String representation of this message after masking any crypto info if present.
   *
   * @return a String representation of this message.
   */
  public static String toCryptoInfoMaskedString(String msg) {
    return msg.replaceAll(REGEX_CRYPT_LINE, REGEX_CRYPT_MASK);
  }

  public static String maskAndWrapSIPMessageToSingleLineOutput(String msg) {
    msg = toCryptoInfoMaskedString(msg);
    return formWrappedMessage(msg);
  }

  private static String formWrappedMessage(String msg) {
    msg = msg.replaceAll(NEW_LINE_REGEX_PATTERN, NEW_LINE_REPLACEMENT_DELIMITER);
    msg = BEGIN_SIP_MESSAGE + msg + END_SIP_MESSAGE;
    return msg;
  }
}
