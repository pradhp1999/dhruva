package com.cisco.dhruva.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;

/**
 * Provides Read-Only access to a CSeq header.
 *
 * <p>
 *
 * <blockquote>
 *
 * + <b>Sequence Number</b><br>
 * + <b>Method</b><br>
 *
 * </blockquote>
 */
public class DsSipCSeqElements implements DsSipElementListener {
  /** Index of the Sequence Number, add 1 for length. */
  public static final int NUMBER = 0;
  /** Index of the Method, add 1 for length. */
  public static final int METHOD = 2;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 4;

  byte[] idx;
  int base;
  boolean complete;

  DsSipCSeqElements(int base, byte[] index) {
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
    return this;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    int which = -1;
    switch (elementId) {
      case DsSipConstants.CSEQ_NUMBER:
        which = NUMBER;
        break;
      case DsSipConstants.CSEQ_METHOD:
        which = METHOD;
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
    // no parameters in CSeq headers
  }
}
