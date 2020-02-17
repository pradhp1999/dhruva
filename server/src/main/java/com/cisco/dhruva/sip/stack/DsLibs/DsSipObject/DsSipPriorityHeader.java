// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Level header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Level       =  "Level" ":" priority-value
 * priority-value =  "emergency" | "urgent" | "normal" | "non-urgent" | other-priority
 * other-priority =  token
 * </pre> </code>
 */
public final class DsSipPriorityHeader extends DsSipStringHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_PRIORITY;
  /** Header ID. */
  public static final byte sID = PRIORITY;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_PRIORITY;

  /** The "emergency" token. */
  public static final DsByteString EMERGENCY = new DsByteString("emergency");
  /** The "urgent" token. */
  public static final DsByteString URGENT = new DsByteString("urgent");
  /** The "normal" token. */
  public static final DsByteString NORMAL = new DsByteString("normal");
  /** The "non-urgent" token. */
  public static final DsByteString NON_URGENT = new DsByteString("non-urgent");

  /** Default constructor. */
  public DsSipPriorityHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipPriorityHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipPriorityHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipPriorityHeader(DsByteString value) {
    super(value);
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getCompactToken() {
    return sCompactToken;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_PRIORITY_TOKEN;
  }

  /**
   * Checks if the priority is <b>emergency</b>.
   *
   * @return <code>true</code> if priority is <b>emergency</b>, <code>false</code> otherwise.
   */
  public boolean isEmergency() {
    return (m_strValue != null) ? EMERGENCY.equalsIgnoreCase(m_strValue) : false;
  }

  /**
   * Checks if the priority is <b>urgent</b>.
   *
   * @return <code>true</code> if priority is <b>emergency</b>, <code>false</code> otherwise.
   */
  public boolean isUrgent() {
    return (m_strValue != null) ? URGENT.equalsIgnoreCase(m_strValue) : false;
  }

  /**
   * Checks if the priority is <b>Normal</b>.
   *
   * @return <code>true</code> if priority is <b>normal</b>, <code>false</code> otherwise.
   */
  public boolean isNormal() {
    return (m_strValue != null) ? NORMAL.equalsIgnoreCase(m_strValue) : false;
  }

  /**
   * Checks if the priority is <b>non-urgent</b>.
   *
   * @return <code>true</code> if priority is <b>non-urgent</b>, <code>false</code> otherwise.
   */
  public boolean isNonUrgent() {
    return (m_strValue != null) ? NON_URGENT.equalsIgnoreCase(m_strValue) : false;
  }

  /** Sets the priority to <b>emergency</b>. */
  public void setEmergency() {
    setValue(EMERGENCY);
  }

  /** Sets the priority to <b>non-urgent</b>. */
  public void setNonUrgent() {
    setValue(NON_URGENT);
  }
  /** Sets the priority to <b>urgent</b>. */
  public void setUrgent() {
    setValue(URGENT);
  }

  /** Sets the priority to <b>normal</b>. */
  public void setNormal() {
    setValue(NORMAL);
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return PRIORITY;
  }
}
