package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;

/**
 * Provides Read-Only access to a URI-based header.
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
 * + <b>Value</b><br>
 *
 * </blockquote>
 */
public class DsSipURIElements implements DsSipElementListener {
  /** <code>true</code> to turn on print statements. */
  private final boolean DEBUG = false;

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
  /** Index of the Value, add 1 for length. */
  public static final int VALUE = 10;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 12;

  byte[] idx;
  int base;
  boolean complete;

  DsSipURIElements(int base, byte[] index) {
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
    if ((elementId == DsSipConstants.UNKNOWN_URL)
        || (elementId == DsSipConstants.SIP_URL)
        || (elementId == DsSipConstants.TEL_URL)) {
      return this;
    }
    return null;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    int which = -1;
    switch (elementId) {
      case DsSipConstants.SIP_URL_ID:
      case DsSipConstants.UNKNOWN_URL:
      case DsSipConstants.SIP_URL:
      case DsSipConstants.TEL_URL:
        which = VALUE;
        break;
      case DsSipConstants.USERNAME:
        which = USER;
        break;
      case DsSipConstants.HOST:
        which = HOST;
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
    }
    if (param == DsSipConstants.BS_MADDR) {
      which = MADDR;
    } else {
      idx[N] = DsSipReadOnlyElement.INCOMPLETE;
    }

    if (which != -1) {
      idx[which] = (byte) (voff - base);
      idx[which + 1] = (byte) vcount;
    }
  }
}
