// Copyright (c) 2003-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.*;

/**
 * This class represents the Cisco-Maintenance-Mode header. It provides methods to build, access,
 * modify, serialize and clone the Cisco-Maintenance-Mode header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Cisco-Maintenance-Mode = "Cisco-Maintenance-Mode" HCOLON
 * maint-mode-hdr
 * maint-mode-hdr  = maint-mode-info *(COMMA maint-mode-info)
 * maint-mode-info = call-trace/test-call/maint-ext-info
 * test-call       = "test" [LWS report-target] *(SEMI test-param)
 * report-target   = LAQUOT absoluteURI RAQUOT
 * absoluteURI     = ;refer to [1] for details
 * test-param      = request-id/test-type/test-info/generic-param
 * generic-param   = ;refer to [1] for details
 * request-id       = "request-id" EQUAL quoted-string
 * test-type       = "test-type" EQUAL test-call
 * test-call       = "rtp-media-loopback"/"rtp-pkt-loopback"/token
 * test-info       = "test-info" EQUAL DQUOTE cid-url DQOATE
 * cid-url         = ; refer to RFC 2392, also see note below:
 * maint-ext-info  = token [LWS report-target] *(SEMI generic-param)
 * call-trace      = "call-trace" [LWS report-target]
 * (SEMI trace-param)
 * trace-param      = request-id/report-types/generic-param
 * report-types     = "report-type" EQUAL DQUOTE report-type *(COMMA
 * report-type)DQUOTE
 *
 * report-type   = "internal-trace"/"sip"/"isup"/"tcap"/"q931"/
 * "h323"/"mgcp"/token
 *
 * </pre> </code>
 */
public final class DsSipCiscoMaintenanceModeHeader extends DsSipParametricHeader {
  /** Header ID. */
  public static final byte sID = CISCO_MAINTENANCE_MODE;

  /** Holds the Maintenance Mode Info value for this header. */
  protected DsByteString m_maintModeInfo;

  /** Hold report target value, without "<" and ">" */
  protected DsByteString m_reportTarget;

  /** Default constructor. */
  public DsSipCiscoMaintenanceModeHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipCiscoMaintenanceModeHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
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
  public DsSipCiscoMaintenanceModeHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipCiscoMaintenanceModeHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return BS_CISCO_MAINTENANCE_MODE;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return BS_CISCO_MAINTENANCE_MODE;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_CISCO_MAINTENANCE_MODE_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CISCO_MAINTENANCE_MODE;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    DsSipCiscoMaintenanceModeHeader source = (DsSipCiscoMaintenanceModeHeader) header;
    super.copy(header);
    this.m_maintModeInfo = source.m_maintModeInfo;
    this.m_reportTarget = source.m_reportTarget;
  }

  /**
   * Returns a deep copy of the header object and all of the other elements on the list that it is
   * associated with. NOTE: This behavior will change when the deprecated methods are removed and it
   * will just clone the single header.
   *
   * @return the cloned DsSipCiscoMaintenanceModeHeader header object
   */
  public Object clone() {
    DsSipCiscoMaintenanceModeHeader clone = (DsSipCiscoMaintenanceModeHeader) super.clone();
    clone.m_maintModeInfo = m_maintModeInfo == null ? null : m_maintModeInfo.copy();
    clone.m_reportTarget = m_reportTarget == null ? null : m_reportTarget.copy();

    return clone;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) // optimize before instanceof
    {
      return true;
    }

    DsSipCiscoMaintenanceModeHeader hdr = null;
    if (!(obj instanceof DsSipCiscoMaintenanceModeHeader)) {
      return false;
    } else {
      hdr = (DsSipCiscoMaintenanceModeHeader) obj;
    }

    if (DsByteString.compareIgnoreNull(m_maintModeInfo, hdr.m_maintModeInfo) != 0) {
      return false;
    }
    if (DsByteString.compareIgnoreNull(m_reportTarget, hdr.m_reportTarget) != 0) {
      return false;
    }

    // check other params
    if (m_paramTable != null && hdr.m_paramTable != null) {
      if (!m_paramTable.equals(hdr.m_paramTable)) {
        return false;
      }
    } else if (m_paramTable == null && hdr.m_paramTable != null) {
      if (!hdr.m_paramTable.isEmpty()) {
        return false;
      }
    } else if (hdr.m_paramTable == null && m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        return false;
      }
    }
    // else both null - ok

    return true;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_maintModeInfo != null) {
      m_maintModeInfo.write(out);
    }
    if (m_reportTarget != null) {
      out.write(B_SPACE);
      out.write(B_LABRACE);
      m_reportTarget.write(out);
      out.write(B_RABRACE);
    }
    // out.write(';'); no need, paramTable writs it out

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_maintModeInfo != null) {
      md.getEncoding(m_maintModeInfo).write(out);
    }
    if (m_reportTarget != null) {
      out.write(B_SPACE);
      out.write(B_LABRACE);
      md.getEncoding(m_reportTarget).write(out);
      out.write(B_RABRACE);
    }
    writeEncodedParameters(out, md);
  }

  /**
   * Gets the Maintenance Mode Info value
   *
   * @return the Maintenance Mode Info value
   */
  public DsByteString getMaintModeInfo() {
    return m_maintModeInfo;
  }

  /**
   * Sets the id parameter value
   *
   * @param value the value
   */
  public void setMaintModeInfo(DsByteString value) {
    this.m_maintModeInfo = value;
  }

  /**
   * Gets the Report Target value
   *
   * @return the Report Target value
   */
  public DsByteString getReportTarget() {
    return m_reportTarget;
  }

  /**
   * Sets the Report Target parameter value
   *
   * @param value the value
   */
  public void setReportTarget(DsByteString value) {
    m_reportTarget = value;
  }

  ///////////////////////////////////////////////////
  // DsSipElementListener Interface Implementation //
  ///////////////////////////////////////////////////

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case DISPLAY_NAME: // maint-mode-info is at DISPLAY_NAME position
        m_maintModeInfo = new DsByteString(buffer, offset, count);
        break;
      case UNKNOWN_URL: // optional URL, usually http
        m_reportTarget = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
}
