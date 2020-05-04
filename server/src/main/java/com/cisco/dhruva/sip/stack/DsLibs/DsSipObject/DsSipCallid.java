// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.event.Level;

/** This class represents a Call-ID as specified in RFC 3261. */
public class DsSipCallid implements Serializable, Cloneable {
  /** The Call ID itself. */
  private DsByteString m_strCallid;
  /** A seed for random numbers. */
  private static int seed = 0;
  /** <code>true</code> if this object has changed. */
  private boolean m_bChanged;

  /** Default constructor. */
  public DsSipCallid() {}

  /**
   * Constructor used to set the host name.
   *
   * @param pHostName the host name
   */
  public DsSipCallid(DsByteString pHostName) {
    reGenerate(pHostName);
  }

  /**
   * Returns the callid as DsByteString object.
   *
   * @return the byte string value of this callid object.
   */
  public DsByteString getValue() {
    return m_strCallid;
  }

  /** Regenerates the Call ID. */
  public void reGenerate() {
    try {
      if (DsLog4j.headerCat.isEnabled(Level.DEBUG))
        DsLog4j.headerCat.log(Level.DEBUG, "Regenerating the Call id using the local host name");

      // possibly cache this value - jsm
      //            reGenerate(DsDsString.getHostAddress(InetAddress.getLocalHost()));
      reGenerate(new DsByteString(InetAddress.getLocalHost().getHostAddress()));
    } catch (UnknownHostException e) {
    }
  }

  /**
   * Regenerates the Call ID.
   *
   * @param pHostName the hostname to be used
   */
  public void reGenerate(DsByteString pHostName) {
    if (pHostName == null) {
      try {
        if (DsLog4j.headerCat.isEnabled(Level.DEBUG))
          DsLog4j.headerCat.log(Level.DEBUG, "Regenerating the Call id using the local host name");

        // possibly cache this value - jsm
        pHostName = new DsByteString(InetAddress.getLocalHost().getHostAddress());
      } catch (Exception e) {

      }
    }

    if (DsLog4j.headerCat.isEnabled(Level.DEBUG))
      DsLog4j.headerCat.log(Level.DEBUG, "Generating the call id using the host name " + pHostName);

    long curr_time = System.currentTimeMillis();

    StringBuffer buff =
        new StringBuffer(64)
            .append(curr_time)
            .append(getNextSeedValue())
            .append("@")
            .append(pHostName);
    m_strCallid = new DsByteString(buff.toString());

    if (DsLog4j.headerCat.isEnabled(Level.DEBUG))
      DsLog4j.headerCat.log(Level.DEBUG, "Call id is  " + m_strCallid);
    m_bChanged = true;
  }

  /**
   * Sets the call ID.
   *
   * @param pCallid the Call ID
   */
  public void setCallId(DsByteString pCallid) {
    m_strCallid = pCallid;
    m_bChanged = true;
  }

  /**
   * Used to clone this Call-ID object.
   *
   * @return the cloned Call-ID object
   */
  public Object clone() {
    DsSipCallid cid = new DsSipCallid();
    cid.m_strCallid = m_strCallid;
    return cid;
  }

  /**
   * Checks for equality of Call IDs.
   *
   * @param obj the object to check
   * @return <code>true</code> if the Call IDs are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipCallid dsSipCallid = null;
    try {
      dsSipCallid = (DsSipCallid) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if ((m_strCallid == null || m_strCallid.length() == 0)
        && (dsSipCallid.m_strCallid == null || dsSipCallid.m_strCallid.length() == 0)) {
      // null == "" - this is ok
    } else if (!m_strCallid.equals(dsSipCallid.m_strCallid)) {
      return false;
    }

    return true;
  }

  /**
   * Generates the next seed value.
   *
   * @return the next seed.
   */
  private synchronized int getNextSeedValue() {
    seed++;
    if (seed == 100000) // I don't want it to be too big
    seed = 0;
    return seed;
  }
}
