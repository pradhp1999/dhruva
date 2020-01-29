package com.cisco.dhruva.DsLibs.DsSipObject.ReadOnly;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;

/**
 * Represents Read-Only Via header elements.
 *
 * <p>
 *
 * <blockquote>
 *
 * X <b>ProtocolName</b> - [SIP]<br>
 * X <b>ProtocolVersion</b> - [2.0]<br>
 * + <b>Transport</b> - [UDP]<br>
 * + <b>Host</b><br>
 * X <b>Comment</b><br>
 * + <b>Maddr Parameter</b><br>
 * + <b>Received Parameter</b><br>
 * + <b>Branch Parameter</b><br>
 * + <b>Port</b> - [5060]<br>
 * + <b>TTL Parameter</b> - [-1]<br>
 * + <b>Hidden Parameter</b> - [false]<br>
 *
 * </blockquote>
 */
public class DsSipViaElements implements DsSipElementListener {
  /** Index of the Transport, add 1 for length. */
  public static final int TRANSPORT = 0;
  /** Index of the Port, add 1 for length. */
  public static final int PORT = 2;
  /** Index of the Host, add 1 for length. */
  public static final int HOST = 4;
  /** Index of the Maddr Parameter, add 1 for length. */
  public static final int MADDR = 6;
  /** Index of the Received Parameter, add 1 for length. */
  public static final int RECEIVED = 8;
  /** Index of the TTL Parameter, add 1 for length. */
  public static final int TTL = 10;
  /** Index of the Branch Parameter, add 1 for length. */
  public static final int BRANCH = 12;
  /** Index of the Hidden Parameter, add 1 for length. */
  public static final int HIDDEN = 14;

  /**
   * The index of the flag to indicate whether there was anything that fell outside of the subset of
   * elements storable by the readonly element. If nothing fell off, we can use the readonly element
   * to serialize the header.
   */
  private static final int N = 16;

  byte[] idx;
  int base;
  boolean complete;

  DsSipViaElements(int base, byte[] index) {
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
    return null;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    int which = -1;
    switch (elementId) {
      case DsSipConstants.TRANSPORT:
        which = TRANSPORT;
        break;
      case DsSipConstants.PORT:
        which = PORT;
        break;
      case DsSipConstants.HOST:
        which = HOST;
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
    if (param == DsSipConstants.BS_MADDR) {
      which = MADDR;
    } else if (param == DsSipConstants.BS_BRANCH) {
      which = BRANCH;
    } else if (param == DsSipConstants.BS_RECEIVED) {
      which = RECEIVED;
    } else if (param == DsSipConstants.BS_TTL) {
      which = TTL;
    } else if (param == DsSipConstants.BS_HIDDEN) {
      which = HIDDEN;
    } else {
      idx[N] = DsSipReadOnlyElement.INCOMPLETE;
    }

    if (which != -1) {
      idx[which] = (byte) (voff - base);
      idx[which + 1] = (byte) vcount;
    }
  }
}
