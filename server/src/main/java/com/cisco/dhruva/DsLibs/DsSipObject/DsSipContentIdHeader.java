// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import java.io.*;

/**
 * Representation of Content-ID header, RFC 2045.
 *
 * <p>In constructing a high-level user agent, it may be desirable to allow one body to make
 * reference to another. Accordingly, bodies may be labelled using the "Content-ID" header field,
 * which is syntactically identical to the "Message-ID" header field:
 *
 * <p>id := "Content-ID" ":" msg-id
 *
 * <p>Like the Message-ID values, Content-ID values must be generated to be world-unique.
 *
 * @author Michael Zhou (xmzhou@cisco.com)
 * @author Jianren Yang (jryang@cisco.com)
 */
public final class DsSipContentIdHeader extends DsSipStringHeader {
  /** Header ID. */
  public static final byte sID = CONTENT_ID;

  /** Creates a new instance of DsSipContentIdHeader */
  public DsSipContentIdHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipContentIdHeader(byte[] value) {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
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
  public DsSipContentIdHeader(byte[] value, int offset, int count) {
    setValue(new DsByteString(value, offset, count));
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException for parsing errors
   */
  public DsSipContentIdHeader(DsByteString value) throws DsSipParserException {
    this(value.data(), value.offset(), value.length());
  }

  /*
   * javadoc inherited.
   */
  public void parse(DsByteString value) {
    setValue(value);
    this.parse(value.data(), value.offset(), value.length());
  }

  /*
   * javadoc inherited.
   */
  public void parse(byte[] value, int offset, int count) {
    setValue(new DsByteString(value, offset, count));
    try {
      DsSipEnclosedStringHeaderParser.getInstance()
          .parseHeader(this, CONTENT_ID, value, offset, count);
    } catch (DsSipParserListenerException ex) {
    } catch (DsSipParserException ex) {
    }
  }

  /*
   * javadoc inherited.
   */
  public void parse(byte[] value) {
    setValue(new DsByteString(value, 0, value.length));
    this.parse(value, 0, value.length);
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public int getHeaderID() {
    return CONTENT_ID;
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return BS_CONTENT_ID;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return (BS_CONTENT_ID_TOKEN);
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return BS_CONTENT_ID;
  }

  /*
   * javadoc inherited.
   */
  public void copy(DsSipHeader obj) {
    if (!(obj instanceof DsSipContentIdHeader)) return;
    DsSipContentIdHeader hdr = (DsSipContentIdHeader) obj;
    this.cid = hdr.cid;
  }

  /**
   * Sets Content-Id value with angle brackets. No effect is taken if the Content-Id value is not
   * enclosed in angle brackets.
   *
   * @param value Content id value
   */
  public void setValue(DsByteString value) {
    if (value == null) {
      m_strValue = null;
      cid = null;
      return;
    }
    int count = value.length();
    int offset = value.offset();
    byte[] data = value.data();
    if (count > 1 && data[offset] == '<' && data[offset + count - 1] == '>') {
      m_strValue = value;
      cid = new DsByteString(data, offset + 1, count - 2);
    }
  }

  /**
   * Returns the content id value
   *
   * @return the content id value, minus the '<' and '>'
   */
  public DsByteString getContentId() {
    return cid;
  }

  /**
   * Sets content id.
   *
   * @param cid Content id value
   */
  public void setContentId(DsByteString cid) {
    this.cid = cid;
  }

  /*
   * javadoc inherited.
   */
  public void writeValue(OutputStream out) throws IOException {
    BS_LABRACE.write(out);
    if (cid != null) {
      cid.write(out);
    }
    BS_RABRACE.write(out);
  }

  /*
   * javadoc inherited.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof DsSipContentIdHeader)) return false;
    DsSipContentIdHeader hdr = (DsSipContentIdHeader) obj;
    return (DsByteString.compareIgnoreNull(this.cid, hdr.cid) == 0);
  }

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
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
    return null;
  }

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
      System.out.println("elementFound - value = [" + new String(buffer, offset, count) + "]");
      System.out.println();
    }
    switch (elementId) {
      case URI_DATA:
        cid = new DsByteString(buffer, offset, count);
        break;
    }
  }

  /** Content-Id value without angle brackets. */
  private DsByteString cid = null;
}
