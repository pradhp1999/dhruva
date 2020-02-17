// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.io.IOException;

/** Holds a dialog ID. */
public class DsSipDialogID {
  /** The dash character represented as a string. */
  private static final DsByteString DASH = new DsByteString("-");

  /** The Call-ID value. */
  DsByteString callID;
  /** The To tag. */
  DsByteString toTag;
  /** The From tag. */
  DsByteString fromTag;

  /** The hash code. */
  int hash;

  /**
   * Construct the dialog ID.
   *
   * @param callID the call ID from call ID header
   * @param toTag the tag from To header
   * @param fromTag the tag from From header
   * @throws DsException when one or more of the parameters are null
   */
  public DsSipDialogID(DsByteString callID, DsByteString toTag, DsByteString fromTag)
      throws DsException {
    if (callID == null /*|| toTag == null || fromTag == null*/)
      throw new DsException("Error creating dialogID. The call ID is null.");
    this.callID = callID;
    this.toTag = toTag;
    this.fromTag = fromTag;
  }

  /**
   * Checks whether another object is equal to this one.
   *
   * @param obj the object to compare to.
   * @return <code>true</code> when they are equal. <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    try {
      DsSipDialogID other = (DsSipDialogID) obj;
      if (other != null)
        return callID.equals(other.callID)
            && exclEquals(toTag, other.toTag)
            && exclEquals(fromTag, other.fromTag);
      else return false;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Helper method for equals. Performs the null check and comparison.
   *
   * @param s1 a string to compare.
   * @param s2 a string to compare.
   * @returnt <code>true</code> when they are equal. <code>false</code> otherwise.
   */
  private final boolean exclEquals(DsByteString s1, DsByteString s2) {
    if (s1 == null) {
      return s2 == null;
    } else {
      return s1.equals(s2);
    }
  }

  /**
   * Generates a hash code for this object.
   *
   * @return the hash code.
   */
  public int hashCode() {
    if (hash == 0) {
      computeHash();
    }
    return hash;
  }

  /** Computes the hashCode of this DialogId. */
  private void computeHash() {
    hash = addHash(hash, callID);
    hash = (toTag == null ? (31 * hash + '-') : addHash(hash, toTag));
    hash = (fromTag == null ? (31 * hash + '-') : addHash(hash, fromTag));
  }

  /**
   * Adds to the hashCode of this DialogId.
   *
   * @param h the current value of the hash code.
   * @param bs the string to add to the hash code.
   * @return the new value of the hash code.
   */
  private final int addHash(int h, DsByteString bs) {
    // this is basically the algorithm used by String.hashCode()
    byte[] val = bs.data();
    int off = bs.offset();
    int len = bs.length();

    for (int i = 0; i < len; i++) {
      h = 31 * h + val[off++];
    }
    return h;
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object.
   */
  public String toString() {
    String buffer2String = null;
    try (ByteBuffer buf = ByteBuffer.newInstance(80)) {
      buf.write(callID);
      buf.write(',');
      buf.write(toTag == null ? DASH : toTag);
      buf.write(',');
      buf.write(fromTag == null ? DASH : fromTag);
      buffer2String = buf.toString();
    } catch (IOException ie) {

    }
    return buffer2String;
  }
}
