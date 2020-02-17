// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import java.io.*;

/** This class represents a CSeq number as specified in RFC 3261. */
public class DsSipCSeq implements Serializable, Cloneable {
  private static long fs_CSeq = 0;

  /** The CSeq value. */
  private long m_CSeq;

  /** Default constructor. */
  public DsSipCSeq() {
    m_CSeq = ++fs_CSeq;
  }

  /**
   * Constructor used to set the CSequence value.
   *
   * @param aCSeq The CSeq value
   */
  public DsSipCSeq(long aCSeq) {
    m_CSeq = aCSeq;

    fs_CSeq = aCSeq;
  }

  /**
   * Retrieves the CSeq value.
   *
   * @return The CSeq value
   */
  public long getAsLong() {
    return (m_CSeq);
  }

  /**
   * Get a copy of the CSeq object.
   *
   * @return a copy of the cloned CSeq object
   */
  public Object clone() {
    DsSipCSeq cSeq = new DsSipCSeq();

    cSeq.m_CSeq = m_CSeq;

    return cSeq;
  }
}
