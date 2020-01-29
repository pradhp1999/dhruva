package com.cisco.dhruva.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;

/**
 * Provides Read-Only access to a String-based header.
 *
 * <p>
 *
 * <blockquote>
 *
 * + <b>Value</b><br>
 *
 * </blockquote>
 */
public class DsSipStringElements implements DsSipElementListener {
  /** <code>true</code> to turn on print statements. */
  private static final boolean DEBUG = false;

  /** Index of the Value, add 1 for length. */
  public static final int VALUE = 0;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 2;

  byte[] idx;
  int base;
  boolean complete;

  DsSipStringElements(int base, byte[] index) {
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
      case DsSipConstants.SINGLE_VALUE:
        which = VALUE;
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
