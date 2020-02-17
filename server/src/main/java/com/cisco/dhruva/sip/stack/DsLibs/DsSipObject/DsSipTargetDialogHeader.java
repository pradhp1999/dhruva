// Copyright (c) 2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <b>DsSipTargetDialogHeader</b>
 *
 * <p>This class represents the Target-Dialog header as specified in RFC 4538. It provides methods
 * to build, access, modify, serialize, and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Target-Dialog     =  "Target-Dialog" HCOLON callid *(SEMI
 *                          td-param)    ;callid from RFC 3261
 * td-param          =  remote-param / local-param / generic-param
 * remote-param      =  "remote-tag" EQUAL token
 * local-param       =  "local-tag" EQUAL token
 *                          ;token and generic-param from RFC 3261
 * </pre> </code>
 */
public final class DsSipTargetDialogHeader extends DsSipParametricHeader {
  /** Header ID. */
  public static final byte sID = TARGET_DIALOG;

  /** The Call-ID associated with this Target-Dialog header. */
  private DsByteString m_callID = null;

  /** Default constructor. */
  public DsSipTargetDialogHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.
   *
   * <p>The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.
   *
   * <p>If there is an exception during parsing phase, it will set the invalid flag of this header
   * and retain the various components that it already parsed. One should check the valid flag
   * before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipTargetDialogHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.
   *
   * <p>The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.
   *
   * <p>If there is an exception during parsing phase, it will set the invalid flag of this header
   * and retain the various components that it already parsed. One should check the valid flag
   * before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipTargetDialogHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
      CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
      The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.
   *
   * <p>The specified byte string <code>value</code> should be the value part (data after the colon)
   * of this header.
   *
   * <p>If there is an exception during parsing phase, it will set the invalid flag of this header
   * and retain the various components that it already parsed. One should check the valid flag
   * before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipTargetDialogHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return BS_TARGET_DIALOG;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
   */
  public DsByteString getCompactToken() {
    return BS_TARGET_DIALOG;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_TARGET_DIALOG_TOKEN;
  }

  /**
   * Returns the 'Call-ID' value of the Target-Dialog header.
   *
   * @return the string <code>Call-ID</code> value of the protocol of the Target-Dialog.
   */
  public DsByteString getCallID() {
    return m_callID;
  }

  /**
   * Sets the 'Call-ID' value of the Target-Dialog header.
   *
   * @param callID the new string <code>Call-ID</code> value for the callID of the Target-Dialog
   *     header.
   */
  public void setProtocol(DsByteString callID) {
    m_callID = callID;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_callID != null) {
      m_callID.write(out);
    }

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_callID != null) {
      md.getEncoding(m_callID).write(out);
    }

    writeEncodedParameters(out, md);
  }

  /**
   * Copy another header's values to this header.
   *
   * @param header the header whose values should be copied.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipTargetDialogHeader source = (DsSipTargetDialogHeader) header;

    m_callID = source.m_callID;
  }

  /**
   * Returns the unique header ID for this header.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return TARGET_DIALOG;
  }

  /**
   * Returns an indication of the equality of another header to this header.
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

    DsSipTargetDialogHeader header = null;
    try {
      header = (DsSipTargetDialogHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_callID != null && !m_callID.equals(header.m_callID)) {
      return false;
    }

    if (m_paramTable != null && header.m_paramTable != null) {
      if (!m_paramTable.equals(header.m_paramTable)) {
        return false;
      }
    } else if (m_paramTable == null && header.m_paramTable != null) {
      if (!header.m_paramTable.isEmpty()) {
        return false;
      }
    } else if (header.m_paramTable == null && m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Clears all the member data and makes this header reusable. That is, the various components
   * (sub-elements) of this headers can be set again.
   *
   * <p>Already constructed objects (e.g., Parameter Tables, etc.) are reused to avoid expensive
   * object creation.
   */
  public void reInit() {
    super.reInit();

    m_callID = null;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  //

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        m_callID = new DsByteString(buffer, offset, count);
        break;

      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
}
