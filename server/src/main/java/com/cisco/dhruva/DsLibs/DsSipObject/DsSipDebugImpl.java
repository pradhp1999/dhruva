// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;

/** A class that implements all of the parser interfaces and prints the values if DEBUG is set. */
public final class DsSipDebugImpl
    implements DsSipElementListener,
        DsSipHeaderListener,
        DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipConstants {
  /** Debug flag to turn debug output on and off. */
  public static final boolean DEBUG = false;

  /** The singleton static instance of DsSipDebugImpl. */
  public static final DsSipDebugImpl instance = new DsSipDebugImpl();

  /** <code>true</code> to parse deeply. */
  public static boolean s_deepParse = true;

  private DsSipDebugImpl() {}

  public static void setDeep(boolean flag) {
    s_deepParse = flag;
  }

  /**
   * Returns the singleton static instance of DsSipDebugImpl.
   *
   * @return the singleton static instance of DsSipDebugImpl.
   */
  public static DsSipDebugImpl getInstance() {
    return instance;
  }

  // interface DsSipMessageListenerFactory

  /*
   * javadoc inherited
   */
  public DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded) {
    if (DEBUG) {
      System.out.println(
          "requestBegin - method = ["
              + DsByteString.newString(buffer, methodOffset, methodCount)
              + "]");
      System.out.println("requestBegin - isEncoded = [" + isEncoded + "]");
      System.out.println();
    }

    return instance;
  }

  /*
   * javadoc inherited
   */
  public DsSipMessageListener responseBegin(
      byte[] buffer, int code, int reasonOffset, int reasonCount, boolean isEncoded) {
    if (DEBUG) {
      System.out.println(
          "responseBegin - reason = ["
              + DsByteString.newString(buffer, reasonOffset, reasonCount)
              + "]");
      System.out.println("responseBegin - code = [" + code + "]");
      System.out.println("responseBegin - isEncoded = [" + isEncoded + "]");
      System.out.println();
    }

    return instance;
  }

  // interface DsSipMessageListener

  /*
   * javadoc inherited
   */
  public DsSipElementListener requestURIBegin(byte[] buffer, int schemeOffset, int schemeCount) {
    if (DEBUG) {
      System.out.println(
          "requestURIBegin - scheme = ["
              + DsByteString.newString(buffer, schemeOffset, schemeCount)
              + "]");
      System.out.println();
    }

    return instance;
  }

  /*
   * javadoc inherited
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid) {
    if (DEBUG) {
      System.out.println(
          "requestURIFound = [" + DsByteString.newString(buffer, offset, count) + "]");
      if (!valid) System.out.println("requestURIFound - NOT VALID");
      System.out.println();
    }
  }

  /*
   * javadoc inherited
   */
  public DsSipHeaderListener getHeaderListener() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void messageFound(byte[] buffer, int offset, int count, boolean messageValid)
      throws DsSipParserListenerException {
    if (DEBUG) {
      System.out.println(
          "bodyFound - body = [" + DsByteString.newString(buffer, offset, count) + "]");
      if (!messageValid) System.out.println("bodyFound - MESSAGE INVALID");
      System.out.println();
    }
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    if (DEBUG) {
      System.out.println(
          "bodyFoundInRequestURI - body = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
  }

  // interface DsSipHeaderListener

  /*
   * javadoc inherited
   */
  public DsSipElementListener headerBegin(int headerId) {
    if (DEBUG) {
      System.out.println("headerBegin = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println();
    }

    if (s_deepParse) {
      return instance;
    } else {
      return null;
    }
  }

  /*
   * javadoc inherited
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid) {
    if (DEBUG) {
      System.out.println("headerFound - type = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println(
          "headerFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
  }

  // interface DsSipElementListener

  /*
   * javadoc inherited
   */
  public DsSipElementListener elementBegin(int contextId, int elementId) {
    if (DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }

    if (s_deepParse
        && (elementId == SIP_URL
            || elementId == TEL_URL
            || elementId == HTTP_URL
            || elementId == UNKNOWN_URL
            || (elementId == SINGLE_VALUE
                && (contextId == CONTENT_LANGUAGE
                    || contextId == ACCEPT_LANGUAGE
                    || contextId == ALLOW_EVENTS
                    || contextId == EVENT))
            || (contextId == DATE && elementId == SINGLE_VALUE)
            || (elementId == SIP_DATE))) {
      return instance;
    } else {
      return null;
    }
  }

  /*
   * javadoc inherited
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      if (!valid) System.out.println("elementFound - NOT VALID");
      System.out.println();
    }
  }

  /*
   * javadoc inherited
   */
  public void unknownFound(
      byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount, boolean valid)
      throws DsSipParserListenerException {
    if (DEBUG) {
      System.out.println(
          "unknownFound - name [offset, count] = [" + nameOffset + ", " + nameCount + "]");
      System.out.println(
          "unknownFound - name = [" + DsByteString.newString(buffer, nameOffset, nameCount) + "]");
      System.out.println(
          "unknownFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "unknownFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      if (!valid) System.out.println("unknownFound - NOT VALID");
      System.out.println();
    }
  }

  /*
   * javadoc inherited
   */
  public void parameterFound(
      int contextId,
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount) {
    if (DEBUG) {
      System.out.println(
          "parameterFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
  }

  /*
   * javadoc inherited
   */
  public void protocolFound(
      byte[] buffer,
      int protocolOffset,
      int protocolCount,
      int majorOffset,
      int majorCount,
      int minorOffset,
      int minorCount,
      boolean valid)
      throws DsSipParserListenerException {
    if (DEBUG) {
      System.out.println(
          "protocolFound - protocol = ["
              + DsByteString.newString(buffer, protocolOffset, protocolCount)
              + "]");
      System.out.println(
          "protocolFound - major version = ["
              + DsByteString.newString(buffer, majorOffset, majorCount)
              + "]");
      System.out.println(
          "protocolFound - minor version = ["
              + DsByteString.newString(buffer, minorOffset, minorCount)
              + "]");
      if (!valid) System.out.println("unknownFound - NOT VALID");
      System.out.println();
    }
  }
}
