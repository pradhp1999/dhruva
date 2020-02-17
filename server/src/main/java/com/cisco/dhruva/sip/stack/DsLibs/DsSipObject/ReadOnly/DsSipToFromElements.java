package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * Provides Read-Only access to a To or From header.
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
 * + <b>Tag Parameter</b><br>
 * + <b>Scheme</b><br>
 *
 * </blockquote>
 */
public class DsSipToFromElements implements DsSipElementListener {
  /** <code>true</code> to turn on print statements. */
  private static final boolean DEBUG = false;

  /** Index of the Username, add 1 for length. */
  public static final int USER = 0;
  /** Index of the Host, add 1 for length. */
  public static final int HOST = 2;
  /** Index of the Port, add 1 for length. */
  public static final int PORT = 4;
  /** Index of the Transport Parameter, add 1 for length. */
  public static final int TRANSPORT = 6;
  /** Index of the Maddr Parameter, add 1 for length. */
  public static final int MADDR = 8;
  /** Index of the TTL Parameter, add 1 for length. */
  public static final int TTL = 10;
  /** Index of the User Parameter, add 1 for length. */
  public static final int USER_PARAM = 12;
  /** Index of the Method Parameter, add 1 for length. */
  public static final int METHOD = 14;
  /** Index of the Tag Paramter, add 1 for length. */
  public static final int TAG = 16;
  /** Index of the Scheme, add 1 for length. */
  public static final int SCHEME = 18;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 20;

  byte[] idx;
  int base;
  boolean complete;

  DsSipToFromElements(int base, byte[] index) {
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
      case DsSipConstants.PORT:
        which = PORT;
        break;
      case DsSipConstants.URI_SCHEME:
        which = SCHEME;
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
    } else if (param == DsSipConstants.BS_TAG) {
      which = TAG;
    } else if (param == DsSipConstants.BS_MADDR) {
      which = MADDR;
    } else if (param == DsSipConstants.BS_TTL) {
      which = TTL;
    } else if (param == DsSipConstants.BS_USER) {
      which = USER_PARAM;
    } else if (param == DsSipConstants.BS_METHOD) {
      which = METHOD;
    } else {
      idx[N] = DsSipReadOnlyElement.INCOMPLETE;
    }

    if (which != -1) {
      idx[which] = (byte) (voff - base);
      idx[which + 1] = (byte) vcount;
    }
  }
}
