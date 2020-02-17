package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * Provides Read-Only access to a Content-Type header.
 *
 * <p>
 *
 * <blockquote>
 *
 * + <b>Type</b><br>
 * + <b>Subtype</b><br>
 *
 * </blockquote>
 */
public class DsSipContentTypeElements implements DsSipElementListener {
  /** <code>true</code> to turn on print statements. */
  private static final boolean DEBUG = false;

  /** Index of the Type, add 1 for length. */
  public static final int TYPE = 0;
  /** Index of the Subtype, add 1 for length. */
  public static final int SUB_TYPE = 2;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 4;

  byte[] idx;
  int base;
  boolean complete;

  DsSipContentTypeElements(int base, byte[] index) {
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
      case DsSipConstants.TYPE:
        which = TYPE;
        break;
      case DsSipConstants.SUB_TYPE:
        which = SUB_TYPE;
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
      throws DsSipParserListenerException {}
}
