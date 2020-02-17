package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * Provides Read-Only access to a Remote-Party-ID header.
 *
 * <p>
 *
 * <blockquote>
 *
 * + <b>User</b><br>
 * + <b>Host</b><br>
 * + <b>Scheme</b><br>
 * + <b>ID Type</b><br>
 * + <b>Screen Paramter</b><br>
 * + <b>Tel</b><br>
 *
 * </blockquote>
 */
public class DsSipRemotePartyIdElements implements DsSipElementListener {
  /** <code>true</code> to turn on print statements. */
  private static final boolean DEBUG = false;

  /** Index of the Username, add 1 for length. */
  public static final int USER = 0;
  /** Index of the Host, add 1 for length. */
  public static final int HOST = 2;
  /** Index of the Scheme, add 1 for length. */
  public static final int SCHEME = 4;
  /** Index of the ID Type, add 1 for length. */
  public static final int ID_TYPE = 6;
  /** Index of the Screen Parameter, add 1 for length. */
  public static final int SCREEN = 8;
  /** Index of the Tel, add 1 for length. */
  public static final int TEL = 10;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 12;

  byte[] idx;
  int base;
  boolean complete;

  DsSipRemotePartyIdElements(int base, byte[] index) {
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
      case DsSipConstants.TEL_URL_NUMBER:
        which = TEL;
        break;
      case DsSipConstants.USERNAME:
        idx[TEL] = (byte) (offset - base);
        idx[TEL + 1] = (byte) count;
        which = USER;
        break;
      case DsSipConstants.URI_SCHEME:
        which = SCHEME;
        break;
      case DsSipConstants.HOST:
        which = HOST;
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

    if (param == DsSipConstants.BS_SCREEN) {
      which = SCREEN;
    }
    if (param == DsSipConstants.BS_ID_TYPE) {
      which = ID_TYPE;
    } else {
      idx[N] = DsSipReadOnlyElement.INCOMPLETE;
    }

    if (which != -1) {
      idx[which] = (byte) (voff - base);
      idx[which + 1] = (byte) vcount;
    }
  }
}
