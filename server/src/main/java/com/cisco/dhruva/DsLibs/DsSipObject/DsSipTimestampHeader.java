// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;
import java.text.*;

/**
 * This class represents the Timestamp header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Timestamp  =  "Timestamp" HCOLON 1*(DIGIT) [ "." *(DIGIT) ] [ LWS delay ]
 * delay      =  *(DIGIT) [ "." *(DIGIT) ]
 * </pre> </code>
 */
public final class DsSipTimestampHeader extends DsSipFloatHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_TIMESTAMP;
  /** Header ID. */
  public static final byte sID = TIMESTAMP;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** The delay field. */
  private DsByteString m_strDelay;
  /** The time field. */
  private DsByteString m_strTime;

  /** Used to format floats as Strings properly */
  private static FormatterInit formatter = new FormatterInit();

  /** Default constructor. */
  public DsSipTimestampHeader() {
    super();
  }

  /**
   * Constructor accepting the time stamp and delay.
   *
   * @param timestamp the time stamp.
   * @param delay the delay.
   */
  public DsSipTimestampHeader(float timestamp, float delay) {
    super();
    setTimestamp(timestamp);
    setDelay(delay);
  }

  /**
   * Constructor accepting the time stamp and delay as byte strings.
   *
   * @param timestamp the time stamp.
   * @param delay the delay.
   */
  public DsSipTimestampHeader(DsByteString timestamp, DsByteString delay) {
    super();
    m_strTime = timestamp;
    m_strDelay = delay;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name.
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
    return BS_TIMESTAMP_TOKEN;
  }

  /**
   * Retrieves the timestamp.
   *
   * @return the timestamp value.
   */
  public float getTimestamp() {
    float f = 0.0f;
    if (m_strTime != null) {
      try {
        f = DsSipMsgParser.parseFloat(m_strTime.data(), m_strTime.offset(), m_strTime.length());
      } catch (NumberFormatException nfe) {
      }
    }
    return f;
  }

  /**
   * Retrieves the delay value.
   *
   * @return the delay value.
   */
  public float getDelay() {
    float f = 0.0f;
    if (m_strDelay != null) {
      try {
        f = DsSipMsgParser.parseFloat(m_strDelay.data(), m_strDelay.offset(), m_strDelay.length());
      } catch (NumberFormatException nfe) {
      }
    }
    return f;
  }

  /**
   * Sets the timestamp value.
   *
   * @param value the timestamp value.
   */
  public void setTimestamp(float value) {
    m_strTime = format(value);
  }

  /**
   * Sets the delay value.
   *
   * @param delay the delay value.
   */
  public void setDelay(float delay) {
    m_strDelay = format(delay);
  }

  /**
   * Sets the float value for this header to the specified <code>value</code>. In this case, its the
   * timestamp value.
   *
   * @param value the new float value of this header.
   */
  public void setFloatValue(float value) {
    setTimestamp(value);
  }

  /**
   * Returns the float value of this header. In this case, its the timestamp value.
   *
   * @return the float value of this header.
   */
  public float getFloatValue() {
    return getTimestamp();
  }

  /**
   * Retrieves the timestamp value as byte string.
   *
   * @return the timestamp value as byte string.
   */
  public DsByteString getTimestampStr() {
    return m_strTime;
  }

  /**
   * Retrieves the delay value as byte string.
   *
   * @return the delay value as byte string.
   */
  public DsByteString getDelayStr() {
    return m_strDelay;
  }

  /**
   * Sets the timestamp value.
   *
   * @param timestamp the timestamp value
   */
  public void setTimestamp(DsByteString timestamp) {
    m_strTime = timestamp;
  }

  /**
   * Sets the delay value.
   *
   * @param delay the delay value
   */
  public void setDelay(DsByteString delay) {
    m_strDelay = delay;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_strTime != null) {
      md.getEncoding(m_strTime).write(out);
    }
    if (m_strDelay != null) {
      out.write(' ');
      md.getEncoding(m_strDelay).write(out);
    }
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_strTime != null) {
      m_strTime.write(out);
    }
    if (m_strDelay != null) {
      out.write(B_SPACE);
      m_strDelay.write(out);
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipTimestampHeader source = (DsSipTimestampHeader) header;
    m_strTime = source.m_strTime;
    m_strDelay = source.m_strDelay;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return TIMESTAMP;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipTimestampHeader header = null;
    try {
      header = (DsSipTimestampHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    float fd1 = 0.0f, fd2 = 0.0f;
    try {
      fd1 =
          (m_strTime != null)
              ? DsSipMsgParser.parseFloat(m_strTime.data(), m_strTime.offset(), m_strTime.length())
              : 0.0f;
    } catch (NumberFormatException nfe1) {
    }
    try {
      fd2 =
          (header.m_strTime != null)
              ? DsSipMsgParser.parseFloat(
                  header.m_strTime.data(), header.m_strTime.offset(), header.m_strTime.length())
              : 0.0f;
    } catch (NumberFormatException nfe2) {
    }
    if (fd1 != fd2) {
      return false;
    }

    fd1 = 0.0f;
    fd2 = 0.0f;
    try {
      fd1 =
          (m_strDelay != null)
              ? DsSipMsgParser.parseFloat(
                  m_strDelay.data(), m_strDelay.offset(), m_strDelay.length())
              : 0.0f;
    } catch (NumberFormatException nfe1) {
    }
    try {
      fd2 =
          (header.m_strDelay != null)
              ? DsSipMsgParser.parseFloat(
                  header.m_strDelay.data(), header.m_strDelay.offset(), header.m_strDelay.length())
              : 0.0f;
    } catch (NumberFormatException nfe2) {
    }
    if (fd1 != fd2) {
      return false;
    }

    return true;
  }
  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        m_strTime = new DsByteString(buffer, offset, count);
        break;
      case DELAY:
        m_strDelay = new DsByteString(buffer, offset, count);
        break;
    }
  }

  private static DsByteString format(float value) {
    Formatter f = (Formatter) formatter.get();
    return new DsByteString(f.format(value));
  }

  // Formatter class to format float to string
  static class Formatter {
    DecimalFormat format = new DecimalFormat("0.000000");
    FieldPosition position = new FieldPosition(0);

    public String format(float number) {
      return format.format(number, new StringBuffer(20), position).toString();
    }
  }

  static class FormatterInit extends ThreadLocal {
    protected Object initialValue() {
      return new Formatter();
    }
  }
} // Ends class DsSipTimestampHeader
