package com.cisco.dhruva.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;

/**
 * Provides Read-Only access to a Contact header.
 *
 * <p>
 *
 * <blockquote>
 *
 * + <b>Username</b><br>
 * + <b>Host</b><br>
 * + <b>Port</b><br>
 * + <b>Transport Parameter</b><br>
 * + <b>Maddr Parameter</b><br>
 * + <b>TTL Parameter</b><br>
 * + <b>User Parameter</b><br>
 * + <b>Method Parameter</b><br>
 * + <b>Q Value Parameter</b><br>
 * + <b>Action Parameter</b><br>
 * + <b>Expires Parameter</b><br>
 * + <b>Wildcard</b><br>
 *
 * </blockquote>
 */
public class DsSipContactElements implements DsSipElementListener {
  /** <code>true</code> to turn on print statements. */
  private static final boolean DEBUG = false;

  /** Index of the Username, add 1 for length. */
  public static final int USER = 0;
  /** Index of the Host, add 1 for length. */
  public static final int HOST = 2;
  /** Index of the Port, add 1 for length. */
  public static final int PORT = 4;
  /** Index of the Transport Paramter, add 1 for length. */
  public static final int TRANSPORT = 6;
  /** Index of the Maddr Parameter, add 1 for length. */
  public static final int MADDR = 8;
  /** Index of the TTL Paramter, add 1 for length. */
  public static final int TTL = 10;
  /** Index of the User Parameter, add 1 for length. */
  public static final int USER_PARAM = 12;
  /** Index of the Method Parameter, add 1 for length. */
  public static final int METHOD = 14;
  /** Index of the Q Value Parameter, add 1 for length. */
  public static final int QVALUE = 16;
  /** Index of the Action Parameter, add 1 for length. */
  public static final int ACTION = 18;
  /** Index of the Expires Parameter, add 1 for length. */
  public static final int EXPIRES = 20;
  /** Index of the Wildcard, add 1 for length. */
  public static final int WILD = 22;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 24;

  byte[] idx;
  int base;
  boolean complete;

  DsSipContactElements(int base, byte[] index) {
    this.idx = index;
    this.base = base;
    idx[N] = DsSipReadOnlyElement.COMPLETE;
    complete = true;
  }

  static byte[] createIndex() {
    return new byte[N + 1];
  }

  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
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

    return this;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean isValid)
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
      System.out.println("elementFound - value = [" + new String(buffer, offset, count) + "]");
      System.out.println();
    }

    int which = -1;
    switch (elementId) {
      case DsSipConstants.USERNAME:
        which = USER;
        break;
      case DsSipConstants.HOST:
        which = HOST;
        break;
      case DsSipConstants.WILDCARD:
        which = WILD;
        break;
      case DsSipConstants.PORT:
        which = PORT;
        break;
      default:
        idx[N] = DsSipReadOnlyElement.INCOMPLETE;
        break;
    }

    if (which != -1) {
      idx[which] = (byte) (offset - base);
      idx[which + 1] = (byte) count;
    }
  }

  public void parameterFound(
      int contextId, byte[] buffer, int noff, int ncount, int voff, int vcount)
      throws DsSipParserListenerException {
    DsByteString param = DsByteString.newLower(buffer, noff, ncount);
    int which = -1;

    if (param == DsSipConstants.BS_TRANSPORT) {
      which = TRANSPORT;
    } else if (param == DsSipConstants.BS_MADDR) {
      which = MADDR;
    } else if (param == DsSipConstants.BS_TTL) {
      which = TTL;
    } else if (param == DsSipConstants.BS_USER) {
      which = USER_PARAM;
    } else if (param == DsSipConstants.BS_METHOD) {
      which = METHOD;
    } else if (param == DsSipConstants.BS_Q) {
      which = QVALUE;
    } else if (param == DsSipConstants.BS_ACTION) {
      which = ACTION;
    } else if (param == DsSipConstants.BS_EXPIRES_VALUE) {
      which = EXPIRES;
    } else {
      idx[N] = DsSipReadOnlyElement.INCOMPLETE;
    }

    if (which != -1) {
      idx[which] = (byte) (voff - base);
      idx[which + 1] = (byte) vcount;
    }
  }
}
