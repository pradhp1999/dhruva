// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsPerf;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the unknown header which is not specified in RFC 3261. It provides methods
 * to build, access, modify, serialize and clone the header.
 */
public final class DsSipUnknownHeader extends DsSipStringHeader {
  /** Header ID. */
  public static final byte sID = UNKNOWN_HEADER;
  /** Header token - only used in error situations when the header name is not supplied. */
  public static final DsByteString sToken = BS_UNKNOWN;

  /** The header name. */
  private DsByteString m_strName = sToken;

  /** Default constructor. */
  public DsSipUnknownHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * . The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipUnknownHeader(byte[] value) {
    this(sToken, new DsByteString(value));
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
  public DsSipUnknownHeader(byte[] value, int offset, int count) {
    this(sToken, new DsByteString(value, offset, count));
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipUnknownHeader(DsByteString value) {
    this(sToken, value);
  }

  /**
   * Constructor used to set the name and value.
   *
   * @param name the name of the header.
   * @param value the value of the header.
   */
  public DsSipUnknownHeader(DsByteString name, DsByteString value) {
    super();
    m_strName = name;
    setValue(value);
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return the complete token name.
   */
  public DsByteString getToken() {
    return (m_strName != null) ? m_strName : sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name.
   */
  public DsByteString getCompactToken() {
    return getToken();
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    DsByteString bs = getToken();
    int len = bs.length();
    byte[] b = new byte[(len + 2)];
    bs.appendTo(b);
    b[len] = B_COLON;
    b[len + 1] = B_SPACE;
    return new DsByteString(b);
  }

  /**
   * Sets the name of this header.
   *
   * @param name the new name for this header.
   */
  public void setName(DsByteString name) {
    m_strName = name;
  }

  /**
   * Gets the name of this header.
   *
   * @return the name for this header.
   */
  public DsByteString getName() {
    return m_strName;
  }

  /** Copy another header's members to me. */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipUnknownHeader source = (DsSipUnknownHeader) header;
    m_strName = source.m_strName;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return UNKNOWN_HEADER;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check.
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipUnknownHeader header = null;
    try {
      header = (DsSipUnknownHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (!DsByteString.equals(m_strName, header.m_strName)) {
      return false;
    }
    if (!DsByteString.equals(m_strValue, header.m_strValue)) {
      return false;
    }
    return true;
  }

  /**
   * Serializes this header in its SIP format. If the flag <code>compact</code> is set, then this
   * header will be serialized with the compact header. name, otherwise full header name will be
   * serialized. Invoke {@link DsSipHeader#setCompact(boolean)} to set or reset this flag.
   *
   * @param out the output stream where this header data (bytes) needs to be serialized.
   * @throws IOException if there is an error while writing to the specified output stream.
   */
  public void write(OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.HEADER_WRITE);
    DsByteString bs = getToken();
    if (bs != null) {
      bs.write(out);
    }
    out.write(B_COLON);
    out.write(B_SPACE);
    writeValue(out);
    BS_EOH.write(out);
    if (DsPerf.ON) DsPerf.stop(DsPerf.HEADER_WRITE);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strName = null;
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
    if (DsSipMessage.DEBUG) {
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
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }

    // all of this moved below to the unknownFound() method
    /*
            switch(elementId)
            {
                case HEADER_NAME:
                    m_strName = new DsByteString(buffer,  offset, count);
                    break;
                case SINGLE_VALUE:
                    setValue(buffer,  offset, count);
                    break;
            }
    */
  }

  /*
   * javadoc inherited.
   */
  public void unknownFound(
      byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount, boolean valid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "unknownFound - name [offset, count] = [" + nameOffset + ", " + nameCount + "]");
      System.out.println(
          "unknownFound - name = [" + DsByteString.newString(buffer, nameOffset, nameCount) + "]");
      System.out.println(
          "unknownFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "unknownFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      if (!valid) System.out.println("unknownFound - NOT VALID");
      System.out.println();
    }

    m_strName = new DsByteString(buffer, nameOffset, nameCount);
    setValue(buffer, valueOffset, valueCount);
  }
} // Ends class DsSipUnknownHeader
